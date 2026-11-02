package lab.transipay.vehicle

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Vehicles", description = "Create, update and manage company vehicles")
@RestController
@RequestMapping("/api")
class VehicleController(
    private val vehicleService: VehicleService,
) {

    @PostMapping("/vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateVehicleRequest): VehicleResponse =
        vehicleService.create(request)

    @GetMapping("/vehicles/{id}")
    fun getById(@PathVariable id: UUID): VehicleResponse =
        vehicleService.getById(id)

    @GetMapping("/companies/{companyId}/vehicles")
    fun getByCompany(@PathVariable companyId: UUID): List<VehicleResponse> =
        vehicleService.getByCompany(companyId)

    @PutMapping("/vehicles/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateVehicleRequest,
    ): VehicleResponse = vehicleService.update(id, request)

//    @DeleteMapping("/vehicles/{id}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    fun delete(@PathVariable id: UUID) {
//        vehicleService.delete(id)
//    }
}