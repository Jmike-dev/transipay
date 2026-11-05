package lab.transipay.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

data class RegisterRequest(
    @field:NotNull val companyId: UUID,
    @field:NotBlank @field:Size(max = 100) val firstName: String,
    @field:NotBlank @field:Size(max = 100) val lastName: String,
    @field:NotBlank @field:Email val email: String,
    @field:Size(max = 30) val phone: String? = null,
    @field:NotBlank @field:Size(min = 10, max = 72) val password: String,
) {
    override fun toString() = "RegisterRequest(email=$email, companyId=$companyId)"
}