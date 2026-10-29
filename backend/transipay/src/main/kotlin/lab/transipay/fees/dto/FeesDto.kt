package lab.transipay.fees.dto

import jakarta.validation.constraints.Positive
import lab.transipay.fees.FeesEntity
import java.math.BigDecimal
import java.util.UUID

data class CreateFeeRequest(
    val companyId: UUID,

    @field:Positive(message = "amount must be greater than 0")
    val amount: BigDecimal,
)

data class UpdateFeeRequest(
    @field:Positive(message = "amount must be greater than 0")
    val amount: BigDecimal,
)

data class FeeResponse(
    val id: UUID,
    val companyId: UUID,
    val amount: BigDecimal,
)

fun FeesEntity.toResponse() = FeeResponse(
    id = requireNotNull(id),
    companyId = companyId,
    amount = amount,
)