package lab.transipay.payment.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import lab.transipay.payment.PaymentEntity
import lab.transipay.payment.PaymentStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class InitiatePaymentRequest(
    @field:NotNull
    val vehicleId: UUID,

    @field:Schema(example = "0712345678")
    @field:NotBlank
    val phoneNumber: String,

    @field:Schema(example = "100")
    @field:NotNull @field:DecimalMin("1")
    val amount: BigDecimal,
)

data class PaymentResponse(
    val id: UUID,
    val vehicleId: UUID,
    val vehicleCode: String,
    val numberPlate: String,
    val phoneNumber: String,
    val amount: BigDecimal,
    val status: PaymentStatus,
    val checkoutRequestId: String?,
    val mpesaReceiptNumber: String?,
    val resultCode: Int?,
    val resultDesc: String?,
    val transactionDate: Instant?,
    val createdAt: Instant?,
)

fun PaymentEntity.toResponse() = PaymentResponse(
    id = id!!,
    vehicleId = vehicle.id!!,
    vehicleCode = vehicle.code,
    numberPlate = numberPlate,
    phoneNumber = phoneNumber,
    amount = amount,
    status = status,
    checkoutRequestId = checkoutRequestId,
    mpesaReceiptNumber = mpesaReceiptNumber,
    resultCode = resultCode,
    resultDesc = resultDesc,
    transactionDate = transactionDate,
    createdAt = createdAt,
)