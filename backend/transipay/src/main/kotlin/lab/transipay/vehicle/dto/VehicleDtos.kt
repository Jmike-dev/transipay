package lab.transipay.vehicle

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateVehicleRequest(
    @field:NotBlank @field:Size(max = 20)
    val numberPlate: String,
    val companyId: UUID,
)

data class UpdateVehicleRequest(
    @field:NotBlank @field:Size(max = 20)
    val numberPlate: String,
)

data class VehicleResponse(
    val id: UUID,
    val code: String,
    val numberPlate: String,
    val companyId: UUID,
)

fun VehicleEntity.toResponse() = VehicleResponse(
    id = id!!,
    code = code,
    numberPlate = numberPlate,
    companyId = company.id!!,
)