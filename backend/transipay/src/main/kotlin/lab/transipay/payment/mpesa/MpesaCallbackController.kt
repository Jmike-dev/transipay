package lab.transipay.payment.mpesa

import io.swagger.v3.oas.annotations.Hidden
import lab.transipay.payment.PaymentService
import lab.transipay.payment.mpesa.prompt.PromptService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

//@Hidden // Safaricom calls this, not your API consumers
@RestController
@RequestMapping("/api/mpesa")
class MpesaCallbackController(
    private val promptService: PromptService,
    private val paymentService: PaymentService,
) {
    private val log = LoggerFactory.getLogger(MpesaCallbackController::class.java)

    @PostMapping("/callback")
    fun stkCallback(@RequestBody payload: StkCallbackBody): Map<String, Any> {
        try {
            val result = promptService.handleStkCallback(payload)
            paymentService.applyCallback(result)
        } catch (e: Exception) {
            // never let Safaricom see an error; log it and rely on /payments/{id}/sync to recover
            log.error("Failed to process STK callback", e)
        }
        return mapOf("ResultCode" to 0, "ResultDesc" to "Accepted")
    }
}