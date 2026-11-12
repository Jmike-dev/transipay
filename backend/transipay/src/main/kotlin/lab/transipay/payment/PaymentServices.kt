package lab.transipay.payment

import lab.transipay.payment.dto.InitiatePaymentRequest
import lab.transipay.payment.dto.PaymentResponse
import lab.transipay.payment.dto.toResponse
import lab.transipay.payment.mpesa.MpesaService
import lab.transipay.payment.mpesa.StkCallbackResult
import lab.transipay.payment.mpesa.StkQueryStatus
import lab.transipay.payment.mpesa.prompt.PromptService
import lab.transipay.vehicle.VehicleRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val vehicleRepository: VehicleRepository,
    private val promptService: PromptService,
    private val mpesa: MpesaService,
) {
    private val log = LoggerFactory.getLogger(PaymentService::class.java)

    /**
     * Sends the STK push and records the payment.
     * Deliberately NOT @Transactional so we don't hold a DB connection during the HTTP call.
     */
    fun initiate(request: InitiatePaymentRequest): PaymentResponse {
        val vehicle = vehicleRepository.findById(request.vehicleId)
            .orElseThrow { notFound("Vehicle ${request.vehicleId} not found") }

        val phone = mpesa.normalizePhone(request.phoneNumber)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid phone number")

        val push = promptService.sdkPush(phone, request.amount, vehicle.numberPlate)

        val payment = PaymentEntity(
            vehicle = vehicle,
            numberPlate = vehicle.numberPlate,
            phoneNumber = phone,
            amount = push.amount,
            status = if (push.success) PaymentStatus.PENDING else PaymentStatus.FAILED,
            merchantRequestId = push.merchantRequestId,
            checkoutRequestId = push.checkoutRequestId,
            resultCode = push.errorCode?.toIntOrNull(),
            resultDesc = if (push.success) push.customerMessage else push.errorMessage,
        )
        return paymentRepository.save(payment).toResponse()
    }

    /** Called by the M-Pesa callback endpoint. Safe to call repeatedly with the same payload. */
    @Transactional
    fun applyCallback(result: StkCallbackResult) {
        val payment = paymentRepository.findByCheckoutRequestId(result.checkoutRequestId)
        if (payment == null) {
            log.warn("Callback for unknown CheckoutRequestID={}", result.checkoutRequestId)
            return
        }

        if (payment.status != PaymentStatus.PENDING) {
            log.info("Duplicate callback ignored: payment={} already {}", payment.id, payment.status)
            return
        }

        payment.resultCode = result.resultCode
        payment.resultDesc = result.resultDesc
        payment.merchantRequestId = payment.merchantRequestId ?: result.merchantRequestId
        payment.status = statusFor(result.resultCode)

        if (payment.status == PaymentStatus.SUCCESS) {
            payment.mpesaReceiptNumber = result.mpesaReceiptNumber
            payment.transactionDate = result.transactionDate?.let(::parseMpesaDate)

            if (result.amount != null && result.amount.compareTo(payment.amount) != 0) {
                log.warn(
                    "Amount mismatch on payment={}: expected={} received={}",
                    payment.id, payment.amount, result.amount,
                )
            }
        }

        paymentRepository.save(payment)
        log.info("Payment {} updated to {}", payment.id, payment.status)
    }

    /**
     * Fallback for missed callbacks: asks Daraja for the status of a PENDING payment
     * and updates it.
     */
    @Transactional
    fun sync(id: UUID): PaymentResponse {
        val payment = paymentRepository.findById(id).orElseThrow { notFound("Payment $id not found") }
        val checkoutId = payment.checkoutRequestId

        if (payment.status != PaymentStatus.PENDING || checkoutId == null) {
            return payment.toResponse()
        }

        val query = promptService.stkPushQuery(checkoutId)
        when (query.status) {
            StkQueryStatus.SUCCESS -> {
                payment.status = PaymentStatus.SUCCESS
                payment.resultCode = 0
                payment.resultDesc = query.resultDesc
            }
            StkQueryStatus.FAILED -> {
                val code = query.resultCode?.toIntOrNull()
                payment.status = statusFor(code)
                payment.resultCode = code
                payment.resultDesc = query.resultDesc
            }
            else -> Unit // still PENDING or query error: leave as is
        }
        return paymentRepository.save(payment).toResponse()
    }

    @Transactional(readOnly = true)
    fun getById(id: UUID): PaymentResponse =
        paymentRepository.findById(id).orElseThrow { notFound("Payment $id not found") }.toResponse()

    @Transactional(readOnly = true)
    fun list(status: PaymentStatus?, pageable: Pageable): Page<PaymentResponse> {
        val page = if (status != null) paymentRepository.findAllByStatus(status, pageable)
        else paymentRepository.findAll(pageable)
        return page.map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun listByVehicle(vehicleId: UUID, pageable: Pageable): Page<PaymentResponse> =
        paymentRepository.findAllByVehicleId(vehicleId, pageable).map { it.toResponse() }

    // 0 = success, 1032 = customer cancelled the prompt, anything else = failed
    private fun statusFor(code: Int?): PaymentStatus = when (code) {
        0 -> PaymentStatus.SUCCESS
        1032 -> PaymentStatus.CANCELLED
        else -> PaymentStatus.FAILED
    }

    // Daraja sends yyyyMMddHHmmss in East Africa Time
    private fun parseMpesaDate(raw: Long): Instant? = runCatching {
        LocalDateTime.parse(raw.toString(), DATE_FORMAT).atZone(EAT).toInstant()
    }.getOrNull()

    private fun notFound(msg: String) = ResponseStatusException(HttpStatus.NOT_FOUND, msg)

    private companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val EAT: ZoneId = ZoneId.of("Africa/Nairobi")
    }
}