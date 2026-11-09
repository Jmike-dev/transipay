# TransiPay

TransiPay is a fleet and fare-collection platform for transit operators such as matatu and shuttle Saccos in Kenya. Operators register a company, manage their staff and vehicles, and (in progress) collect passenger fares through M-PESA.

This repository has two parts:

| Part | Stack | Folder |
|---|---|---|
| **Backend API** | Kotlin, Spring Boot 4, PostgreSQL, Flyway | `backend/transipay` |
| **Mobile app** | Kotlin, Jetpack Compose, Retrofit | `mobile/` (adjust to your folder name) |

---

## Features

**Implemented**

- Company onboarding: create a company, then register its first admin
- Authentication: email and password login, Google sign-in, JWT access tokens
- Role-based access: `ADMIN` and `STAFF`
- Staff management: admins create staff with an initial password
- Fleet management: register and list vehicles per company
- Bottom-tab app shell (Fleet, Staff, Payments, Terminal) with a company and user menu that includes log out

**In progress**

- Payments: M-PESA STK Push collections (a callback endpoint exists on the backend; the app screen is a placeholder)
- Terminal: tap-to-pay fare collection (app screen is a placeholder)

---

## Backend API

### Tech stack

- Kotlin on the JVM, Spring Boot 4.1
- Spring Web MVC, Spring Data JPA (Hibernate)
- PostgreSQL (hosted on Supabase) with Flyway migrations
- Spring Security with the OAuth2 resource server (JWT, HS256)
- BCrypt password hashing (strength 12)
- springdoc-openapi (Swagger UI)
- Deployed on Railway (dev environment)

### Package layout

```
lab.transipay
├── auth/        login, register, password, Google token verification, JWT issuing
├── company/     company entity, repository, controller
├── staff/       staff entity, service, controller, DTOs
├── vehicle/     vehicle entity, service, controller, DTOs
├── config/      SecurityConfig, OpenApiConfig, AppProperties
└── common/      shared exceptions (ConflictException, NotFoundException)
```

### Authentication model

- Passwords live in a separate `staff_password` table (one row per staff member). The table also tracks `failed_attempts` and `locked_until`.
- After 5 failed logins the account is locked for 15 minutes.
- Login runs a BCrypt comparison even for unknown emails, to reduce user enumeration through timing.
- Emails are trimmed and lowercased on register and login.
- Access tokens are JWTs signed with HS256. Claims: `sub` (staff id), `companyId`, `role`.
- Google sign-in links a Google account to an existing staff row by email on first login.

### Endpoints

**Auth** (`/auth`)

| Method | Path | Access | Purpose |
|---|---|---|---|
| POST | `/auth/register` | Public | Create the first ADMIN of a company that has no staff yet |
| POST | `/auth/login` | Public | Email and password login |
| POST | `/auth/google` | Public | Google ID token login |
| POST | `/auth/password` | Authenticated | Create a password for the current user |
| PUT | `/auth/password` | Authenticated | Change the current user's password |

**Companies**

| Method | Path | Access | Purpose |
|---|---|---|---|
| POST | `/api/companies` | Public | Create a company |

**Staff**

| Method | Path | Access | Purpose |
|---|---|---|---|
| POST | `/api/staff` | ADMIN | Create a staff member (optional initial `password`) |
| PUT | `/api/staff/{id}` | ADMIN | Update a staff member |
| GET | `/api/staff` | Authenticated | List staff |
| GET | `/api/staff/{id}` | Authenticated | Get one staff member |

**Vehicles**

| Method | Path | Access | Purpose |
|---|---|---|---|
| POST | `/api/vehicles` | Authenticated | Create a vehicle |
| GET | `/api/companies/{companyId}/vehicles` | Authenticated | List a company's vehicles |
| GET | `/api/vehicles/{id}` | Authenticated | Get one vehicle |
| PUT | `/api/vehicles/{id}` | Authenticated | Update a vehicle's number plate |

**M-PESA**

| Method | Path | Access | Purpose |
|---|---|---|---|
| POST | `/api/mpesa/callback` | Public | Daraja callback receiver |

Interactive docs are at `http://localhost:8080/swagger-ui.html` once the app is running. Use the **Authorize** button to paste a JWT from `/auth/login`.

### Running locally

**Prerequisites:** JDK (the project was built on a recent JDK), Maven, and a PostgreSQL database.

1. Set your configuration. Names below are examples, so match them to your `application.yml`:

   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://<host>:5432/postgres?sslmode=require
       username: <user>
       password: <password>
   app:
     jwt:
       secret: <at least 32 characters>
       expiry-minutes: 60
   ```

2. Start the app:

   ```bash
   ./mvnw spring-boot:run
   ```

3. Flyway applies the migrations in `src/main/resources/db/migration` on startup. The API is served on port 8080.

> Do not commit real database credentials or the JWT secret. Use environment variables or a git-ignored config file.

### First-run flow

1. `POST /api/companies` to create a company and note its `id`.
2. `POST /auth/register` with that `companyId`, your name, email and a password (10 to 72 characters). You become the company's ADMIN and receive a token.
3. Use the token to call the rest of the API, for example `POST /api/staff` to add team members.

---

## Mobile app

### Tech stack

- Kotlin, Jetpack Compose, Material 3
- Navigation Compose (a top-level graph, plus an inner graph for the bottom tabs)
- Retrofit, OkHttp and Gson for networking
- AndroidX Credentials (Google sign-in)
- ViewModel with Compose state

### Package layout

```
com.example.transipay
├── MainActivity.kt
├── navigation/        AppNavigation, BottomNavBar
├── screen/
│   ├── auth/          SignInScreen, SignUpScreen, view models
│   ├── company/       CreateCompanyScreen
│   ├── home/          HomeScreen (top bar, bottom tabs, inner NavHost)
│   ├── fleet/         FleetScreen, FleetViewModel
│   ├── staff/         StaffListScreen, AddStaffScreen, view models
│   ├── payments/      PaymentsScreen (placeholder)
│   ├── terminal/      TerminalScreen (placeholder)
│   └── common/        TransiTopBar, PlaceholderScreen
├── service/
│   ├── ApiClient.kt   Retrofit singleton, bearer-token interceptor, safeApiCall
│   ├── auth/          AuthService, Session, Role
│   ├── staff/         StaffService
│   └── vehicle/       VehicleService
└── ui/theme/
```

### Navigation flow

```
Create company ──► Sign up (first admin) ──► Home
       │                                      ▲
       └────► Log in ─────────────────────────┘

Home (tabs): Fleet | Staff | Payments | Terminal
Staff tab ──► Add staff (full screen, admins only)
Top bar menu ──► Log out ──► Log in
```

### How networking works

- `ApiClient` holds the current JWT in `ApiClient.authToken`. An OkHttp interceptor adds it as `Authorization: Bearer <token>` on every request.
- `safeApiCall` wraps Retrofit calls and returns `ApiResult.Success` or `ApiResult.Error(message, code)`. View models map HTTP codes (401, 403, 409 and so on) to friendly messages.
- `Session` keeps the signed-in user's id, company id, company name, user name and role in memory.

### Running the app

1. Open the mobile project in Android Studio.
2. Start the backend locally (see above).
3. Point the app at it by editing `BASE_URL` in `ApiClient.kt`:

   | Where the app runs | Base URL |
   |---|---|
   | Emulator | `http://10.0.2.2:8080` |
   | Physical phone over USB | run `adb reverse tcp:8080 tcp:8080`, then use `http://localhost:8080` |
   | Dev server | `https://transipay-develop.up.railway.app` |

4. For plain `http://` URLs, add a debug-only network security config that allows cleartext traffic to `10.0.2.2` and `localhost`. Android blocks cleartext HTTP by default.
5. For Google sign-in, the Google client ID used by the app must match the one configured on the backend.

---

## Known limitations and roadmap

- **Sessions are memory-only.** The token is not persisted, so the app opens on the company screen after a cold start. Next step: store the token (DataStore or EncryptedSharedPreferences) and choose the start screen from it.
- **Company scoping is incomplete on the backend.** Several endpoints (`GET /api/staff`, vehicle endpoints) accept a company or record id from the client instead of using the `companyId` claim from the JWT. Enforce this before production.
- **Admin-only rules.** Vehicle creation is open to any signed-in user.
- **Staff onboarding** uses an admin-chosen temporary password. An invite or reset-link flow would be safer.
- **Payments and Terminal** screens are placeholders pending the M-PESA integration.
- Swagger UI and `/v3/api-docs` should be disabled in production (`springdoc.api-docs.enabled=false`, `springdoc.swagger-ui.enabled=false`).
- The HTTP logging interceptor logs request bodies (including passwords). Set it to `NONE` for release builds.

---
hello
