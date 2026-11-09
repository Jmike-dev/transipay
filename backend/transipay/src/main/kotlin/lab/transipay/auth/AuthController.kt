package lab.transipay.auth

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import lab.transipay.auth.dto.*
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "auth", description = "auth endpoints")
@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {
//
//    @PostMapping("/google")
//    fun google(@Valid @RequestBody req: GoogleLoginRequest): AuthResponse =
//        authService.loginWithGoogle(req.idToken)

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: PasswordLoginRequest): AuthResponse =
        authService.loginWithPassword(req)

    // Requires a valid JWT (e.g. user signed in via Google first)
    @PostMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun createPassword(@AuthenticationPrincipal jwt: Jwt, @Valid @RequestBody req: SetPasswordRequest) =
        authService.createPassword(UUID.fromString(jwt.subject), req.password)

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun changePassword(@AuthenticationPrincipal jwt: Jwt, @Valid @RequestBody req: ChangePasswordRequest) =
        authService.changePassword(UUID.fromString(jwt.subject), req)

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody req: RegisterRequest): AuthResponse =
        authService.registerFirstAdmin(req)
}