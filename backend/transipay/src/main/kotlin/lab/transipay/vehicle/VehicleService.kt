package lab.transipay.vehicle

import lab.transipay.company.CompanyRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class VehicleService(
    private val vehicleRepository: VehicleRepository,
    private val companyRepository: CompanyRepository,
) {

    @Transactional
    fun create(request: CreateVehicleRequest): VehicleResponse {
        val plate = normalize(request.numberPlate)

        val company = companyRepository.findById(request.companyId)
            .orElseThrow { notFound("Company ${request.companyId} not found") }

        if (vehicleRepository.existsByNumberPlate(plate)) {
            throw conflict("Vehicle with number plate $plate already exists")
        }

        val vehicle = VehicleEntity(
            code = generateCode(),
            numberPlate = plate,
            company = company,
        )
        return vehicleRepository.save(vehicle).toResponse()
    }

    @Transactional
    fun update(id: UUID, request: UpdateVehicleRequest): VehicleResponse {
        val vehicle = vehicleRepository.findById(id)
            .orElseThrow { notFound("Vehicle $id not found") }

        val plate = normalize(request.numberPlate)

        if (vehicleRepository.existsByNumberPlateAndIdNot(plate, id)) {
            throw conflict("Vehicle with number plate $plate already exists")
        }

        vehicle.numberPlate = plate
        return vehicleRepository.save(vehicle).toResponse()
    }

    @Transactional(readOnly = true)
    fun getById(id: UUID): VehicleResponse =
        vehicleRepository.findById(id)
            .orElseThrow { notFound("Vehicle $id not found") }
            .toResponse()

    @Transactional(readOnly = true)
    fun getByCompany(companyId: UUID): List<VehicleResponse> =
        vehicleRepository.findAllByCompanyId(companyId).map { it.toResponse() }

    @Transactional
    fun delete(id: UUID) {
        if (!vehicleRepository.existsById(id)) throw notFound("Vehicle $id not found")
        vehicleRepository.deleteById(id)
    }

    // sm001, sm002, ... (goes to sm1000 and beyond automatically)
    private fun generateCode(): String =
        "sm%03d".format(vehicleRepository.nextCodeNumber())

    private fun normalize(plate: String) = plate.trim().uppercase()

    private fun notFound(msg: String) = ResponseStatusException(HttpStatus.NOT_FOUND, msg)
    private fun conflict(msg: String) = ResponseStatusException(HttpStatus.CONFLICT, msg)
}