package lab.transipay.fees

import jakarta.validation.Valid
import lab.transipay.fees.dto.CreateFeeRequest
import lab.transipay.fees.dto.FeeResponse
import lab.transipay.fees.dto.UpdateFeeRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/fees")
class FeesController(
    private val feesService: FeesService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateFeeRequest): FeeResponse =
        feesService.create(request)

    @GetMapping
    fun findAll(): List<FeeResponse> =
        feesService.findAll()

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): FeeResponse =
        feesService.findById(id)

    @GetMapping("/company/{companyId}")
    fun findByCompany(@PathVariable companyId: UUID): List<FeeResponse> =
        feesService.findByCompany(companyId)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateFeeRequest,
    ): FeeResponse = feesService.update(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID) {
        feesService.delete(id)
    }
}