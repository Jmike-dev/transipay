package lab.transipay.company.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import lab.transipay.company.CompanyEntity
import java.util.UUID

data class CompanyRequest(
    @field:NotBlank @field:Size(max = 255)
    @field:Schema(example = "Transipay Ltd")
    val companyName: String,
)

data class CompanyResponse(
    val id: UUID,
    val companyName: String,
)

fun CompanyEntity.toResponse() = CompanyResponse(
    id = id!!,
    companyName = companyName,
)