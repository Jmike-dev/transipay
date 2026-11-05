package lab.transipay.auth

import lab.transipay.auth.dto.AuthResponse
import lab.transipay.auth.dto.ChangePasswordRequest
import lab.transipay.auth.dto.PasswordLoginRequest
import lab.transipay.auth.dto.RegisterRequest
import lab.transipay.common.exception.ConflictException
import lab.transipay.common.exception.NotFoundException
import lab.transipay.company.CompanyRepository
import lab.transipay.staff.Role
import lab.transipay.staff.StaffEntity
import lab.transipay.staff.StaffRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class AuthService(
    private val verifier: GoogleTokenVerifier,
    private val staffRepository: StaffRepository,
    private val companyRepository: CompanyRepository,
    private val passwordRepository: StaffPasswordRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    companion object {
        private const val MAX_FAILED_ATTEMPTS = 5
        private val LOCK_DURATION: Duration = Duration.ofMinutes(15)
    }

    // Keeps response time similar when the email doesn't exist (prevents user enumeration)
    private val dummyHash: String by lazy { passwordEncoder.encode("not-a-real-password")!! }

    // ---------- Google ----------

    fun loginWithGoogle(idToken: String): AuthResponse {
        val g = verifier.verify(idToken)

        val staff: StaffEntity = staffRepository.findByGoogleSub(g.sub)
            ?: linkByEmail(g)

        if (!staff.active) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Account disabled")

        return issue(staff)
    }

    // First login: staff are pre-created by an admin, so match on email and link the Google account
    private fun linkByEmail(g: GoogleUser): StaffEntity {
        val staff = staffRepository.findByEmail(g.email)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No staff account for this email")
        if (staff.googleSub != null && staff.googleSub != g.sub)
            throw ResponseStatusException(HttpStatus.CONFLICT, "Email linked to another Google account")
        staff.googleSub = g.sub
        return staff
    }

    // ---------- Register (first admin of a company) ----------

    fun registerFirstAdmin(req: RegisterRequest): AuthResponse {
        val company = companyRepository.findById(req.companyId)
            .orElseThrow { NotFoundException("Company ${req.companyId} not found") }

        // Only the very first account of a company can self-register
        if (staffRepository.existsByCompanyId(company.id!!))
            throw ConflictException("This company already has an admin. Ask them to add you")

        val email = req.email.trim().lowercase()
        if (staffRepository.existsByEmail(email))
            throw ConflictException("Email $email already in use")

        val staff = staffRepository.save(
            StaffEntity(
                company = company,
                firstName = req.firstName.trim(),
                lastName = req.lastName.trim(),
                email = email,
                phone = req.phone?.trim()?.ifBlank { null },
                role = Role.ADMIN,
                active = true,
            )
        )
        passwordRepository.save(
            StaffPasswordEntity(staff = staff, passwordHash = passwordEncoder.encode(req.password)!!)
        )
        return issue(staff)
    }

    // ---------- Password login ----------

    // noRollbackFor: otherwise the failedAttempts update is rolled back when we throw 401
    @Transactional(noRollbackFor = [ResponseStatusException::class])
    fun loginWithPassword(req: PasswordLoginRequest): AuthResponse {
        val staff = staffRepository.findByEmail(req.email.trim().lowercase())
        val cred = staff?.id?.let { passwordRepository.findByStaffId(it) }

        // Always run a BCrypt compare so timing doesn't reveal whether the account exists
        val matches = passwordEncoder.matches(req.password, cred?.passwordHash ?: dummyHash)

        if (staff == null || cred == null) throw invalidCredentials()

        val now = Instant.now()
        if (cred.lockedUntil?.isAfter(now) == true)
            throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many attempts. Try again later")

        if (!matches) {
            cred.failedAttempts++
            if (cred.failedAttempts >= MAX_FAILED_ATTEMPTS) {
                cred.lockedUntil = now.plus(LOCK_DURATION)
                cred.failedAttempts = 0
            }
            throw invalidCredentials()
        }

        if (!staff.active) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Account disabled")

        cred.failedAttempts = 0
        cred.lockedUntil = null

        return issue(staff)
    }

    // ---------- Create / change password ----------

    fun createPassword(staffId: UUID, password: String) {
        val staff = staffRepository.findById(staffId)
            .orElseThrow { NotFoundException("Staff $staffId not found") }
        if (passwordRepository.findByStaffId(staffId) != null)
            throw ConflictException("Password already set. Use the change password endpoint")

        passwordRepository.save(
            StaffPasswordEntity(staff = staff, passwordHash = passwordEncoder.encode(password)!!)
        )
    }

    @Transactional(noRollbackFor = [ResponseStatusException::class])
    fun changePassword(staffId: UUID, req: ChangePasswordRequest) {
        val cred = passwordRepository.findByStaffId(staffId)
            ?: throw NotFoundException("No password set for this account")

        if (!passwordEncoder.matches(req.currentPassword, cred.passwordHash))
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect")

        cred.passwordHash = passwordEncoder.encode(req.newPassword)!!
    }

    // ---------- helpers ----------

    private fun issue(staff: StaffEntity): AuthResponse {
        val (token, ttl) = jwtService.issue(staff)
        return AuthResponse(
            token, ttl, staff.id!!, staff.company.id!!, staff.role.name,
            staffName = "${staff.firstName} ${staff.lastName}",
            companyName = staff.company.companyName,   // use your CompanyEntity's actual field name
        )
    }

    private fun invalidCredentials() =
        ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password")
}