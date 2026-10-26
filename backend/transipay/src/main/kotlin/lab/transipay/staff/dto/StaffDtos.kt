package lab.transipay.staff.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import lab.transipay.staff.StaffEntity
import java.time.Instant
import java.util.UUID

data class StaffRequest(
    @field:NotBlank @field:Size(max = 100)
    @field:Schema(example = "Jane")
    val firstName: String,

    @field:NotBlank @field:Size(max = 100)
    @field:Schema(example = "Wanjiku")
    val lastName: String,

    @field:NotBlank @field:Email
    @field:Schema(example = "jane@transipay.com")
    val email: String,

    @field:Size(max = 30)
    @field:Schema(example = "+254700000000")
    val phone: String? = null,

    @field:NotBlank
    @field:Schema(example = "ACCOUNTANT")
    val role: String,

    val active: Boolean = true,
)

data class StaffResponse(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val role: String,
    val active: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

fun StaffEntity.toResponse() = StaffResponse(
    id = id!!,
    firstName = firstName,
    lastName = lastName,
    email = email,
    phone = phone,
    role = role,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt,
)