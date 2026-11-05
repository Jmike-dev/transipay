package lab.transipay.auth.dto

import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class GoogleLoginRequest(
    @field:NotBlank val idToken: String,
)

data class AuthResponse(
    val accessToken: String,
    val expiresInSeconds: Long,
    val staffId: UUID,
    val companyId: UUID,
    val role: String,
)