package lab.transipay.auth

import lab.transipay.auth.dto.AuthResponse
import lab.transipay.staff.StaffEntity
import lab.transipay.staff.StaffRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class AuthService(
    private val verifier: GoogleTokenVerifier,
    private val staffRepository: StaffRepository,
    private val jwtService: JwtService,
) {
    fun loginWithGoogle(idToken: String): AuthResponse {
        val g = verifier.verify(idToken)

        val staff: StaffEntity = staffRepository.findByGoogleSub(g.sub)
            ?: linkByEmail(g)

        if (!staff.active) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Account disabled")

        val (token, ttl) = jwtService.issue(staff)
        return AuthResponse(token, ttl, staff.id!!, staff.company.id!!, staff.role.name)
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
}