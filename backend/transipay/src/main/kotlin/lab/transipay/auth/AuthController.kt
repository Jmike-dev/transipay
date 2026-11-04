package lab.transipay.auth

import jakarta.validation.Valid
import lab.transipay.auth.dto.AuthResponse
import lab.transipay.auth.dto.GoogleLoginRequest
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/google")
    fun google(@Valid @RequestBody req: GoogleLoginRequest): AuthResponse =
        authService.loginWithGoogle(req.idToken)
}