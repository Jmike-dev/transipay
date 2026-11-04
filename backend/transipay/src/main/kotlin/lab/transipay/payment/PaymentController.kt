package lab.transipay.payment

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import lab.transipay.payment.dto.InitiatePaymentRequest
import lab.transipay.payment.dto.PaymentResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Payments", description = "Initiate and track M-Pesa vehicle payments")
@RestController
@RequestMapping("/api")
class PaymentController(
    private val paymentService: PaymentService,
) {

    @Operation(summary = "Send an STK push to the customer's phone for a vehicle")
    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    fun initiate(@Valid @RequestBody request: InitiatePaymentRequest): PaymentResponse =
        paymentService.initiate(request)

    @GetMapping("/payments/{id}")
    fun getById(@PathVariable id: UUID): PaymentResponse = paymentService.getById(id)

    @Operation(summary = "Re-check a PENDING payment with M-Pesa (use if the callback never arrived)")
    @PostMapping("/payments/{id}/sync")
    fun sync(@PathVariable id: UUID): PaymentResponse = paymentService.sync(id)

    @Operation(summary = "List payments, optionally filtered by status (e.g. SUCCESS)")
    @GetMapping("/payments")
    fun list(
        @RequestParam(required = false) status: PaymentStatus?,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): Page<PaymentResponse> = paymentService.list(status, pageable)

    @GetMapping("/vehicles/{vehicleId}/payments")
    fun listByVehicle(
        @PathVariable vehicleId: UUID,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): Page<PaymentResponse> = paymentService.listByVehicle(vehicleId, pageable)
}