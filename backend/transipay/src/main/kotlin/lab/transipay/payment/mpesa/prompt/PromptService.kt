package lab.transipay.payment.mpesa.prompt

import tools.jackson.databind.ObjectMapper
import lab.transipay.payment.mpesa.DarajaErrorResponse
import lab.transipay.payment.mpesa.DarajaProperties
import lab.transipay.payment.mpesa.MpesaService
import lab.transipay.payment.mpesa.SdkPushResponse
import lab.transipay.payment.mpesa.SdkPushResult
import lab.transipay.payment.mpesa.StkCallbackBody
import lab.transipay.payment.mpesa.StkCallbackResult
import lab.transipay.payment.mpesa.StkQueryResponse
import lab.transipay.payment.mpesa.StkQueryResult
import lab.transipay.payment.mpesa.StkQueryStatus
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Base64

@Service
class PromptService(
    private val daraja: DarajaProperties,
    private val mpesa: MpesaService,
    private val objectMapper: ObjectMapper,
    restClientBuilder: RestClient.Builder,
) {
    private val log = LoggerFactory.getLogger(PromptService::class.java)

    private val restClient: RestClient = restClientBuilder.baseUrl(daraja.safUrl).build()

    /** Initiates an STK push (the "enter your PIN" prompt on the customer's phone). */
    fun sdkPush(
        phoneNo: String,
        amount: BigDecimal,
        numberPlate: String,
        isRetry: Boolean = false,
    ): SdkPushResult {
        val amountPaid = amount.setScale(0, RoundingMode.HALF_UP)

        val phone = mpesa.normalizePhone(phoneNo)
            ?: return SdkPushResult(
                success = false,
                amount = amountPaid,
                errorMessage = "Invalid phone number",
            )

        // same normalization as VehicleService (trim + uppercase)
        val plate = numberPlate.trim().uppercase()
        if (plate.isEmpty()) {
            return SdkPushResult(
                success = false,
                amount = amountPaid,
                errorMessage = "Number plate is required",
            )
        }

        val timestamp = timestamp()
        val password = password(timestamp)
        val token = mpesa.mpesaAuth()

        val requestBody = mapOf(
            "BusinessShortCode" to daraja.safShortCode,
            "Password" to password,
            "Timestamp" to timestamp,
            "TransactionType" to "CustomerPayBillOnline",
            "Amount" to amountPaid.toInt(),
            "PartyA" to phone,
            "PartyB" to daraja.safShortCode,
            "PhoneNumber" to phone,
            "CallBackURL" to daraja.safCallbackUrl,
            "AccountReference" to plate.take(12),
            "TransactionDesc" to "Vehicle payment".take(13),
        )

        try {
            val data = restClient.post()
                .uri("/mpesa/stkpush/v1/processrequest")
                .headers { it.setBearerAuth(token) }
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body<SdkPushResponse>()
                ?: throw IllegalStateException("Empty response from Daraja")

            log.info(
                "STK push sent: CheckoutRequestID={} plate={} amount={}",
                data.checkoutRequestId, plate, amountPaid,
            )

            return SdkPushResult(
                success = true,
                amount = amountPaid,
                merchantRequestId = data.merchantRequestId,
                checkoutRequestId = data.checkoutRequestId,
                customerMessage = data.customerMessage,
            )
        } catch (e: RestClientException) {
            val errorCode = e.darajaError()?.errorCode
            val errorMessage = e.darajaError()?.errorMessage ?: DEFAULT_PUSH_ERROR

            // invalid/expired token: clear cache and retry once with a fresh token
            if (errorCode == INVALID_TOKEN_CODE && !isRetry) {
                log.warn("Invalid access token on STK push, refreshing token and retrying once")
                mpesa.invalidateToken()
                return sdkPush(phoneNo, amount, numberPlate, isRetry = true)
            }

            log.error(
                "STK push failed for plate={} amount={} phone={}: {} - {}",
                plate, amountPaid, phone, errorCode ?: e.httpStatus(), errorMessage,
            )

            return SdkPushResult(
                success = false,
                amount = amountPaid,
                errorCode = errorCode,
                errorMessage = errorMessage,
            )
        } catch (e: IllegalStateException) {
            log.error("STK push failed for plate={} amount={} phone={}: {}", plate, amountPaid, phone, e.message)
            return SdkPushResult(success = false, amount = amountPaid, errorMessage = DEFAULT_PUSH_ERROR)
        }
    }

    /** Checks the status of an earlier STK push by querying Daraja. */
    fun stkPushQuery(checkoutRequestId: String, isRetry: Boolean = false): StkQueryResult {
        val timestamp = timestamp()
        val password = password(timestamp)
        val token = mpesa.mpesaAuth()

        val requestBody = mapOf(
            "BusinessShortCode" to daraja.safShortCode,
            "Password" to password,
            "Timestamp" to timestamp,
            "CheckoutRequestID" to checkoutRequestId,
        )

        try {
            val data = restClient.post()
                .uri("/mpesa/stkpushquery/v1/query")
                .headers { it.setBearerAuth(token) }
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body<StkQueryResponse>()
                ?: throw IllegalStateException("Empty response from Daraja")

            log.info(
                "STK query result: CheckoutRequestID={} ResultCode={}",
                checkoutRequestId, data.resultCode,
            )

            return StkQueryResult(
                status = if (data.resultCode.toIntOrNull() == 0) StkQueryStatus.SUCCESS else StkQueryStatus.FAILED,
                merchantRequestId = data.merchantRequestId,
                checkoutRequestId = data.checkoutRequestId,
                resultCode = data.resultCode,
                resultDesc = data.resultDesc,
            )
        } catch (e: RestClientException) {
            log.error(
                "STK query failed for CheckoutRequestID={}: status={} data={}",
                checkoutRequestId, e.httpStatus(), (e as? RestClientResponseException)?.responseBodyAsString,
            )

            if (e.httpStatus() == 429) {
                return StkQueryResult(
                    status = StkQueryStatus.ERROR,
                    checkoutRequestId = checkoutRequestId,
                    resultDesc = "Rate limited by M-Pesa — please wait before retrying",
                )
            }

            val errorCode = e.darajaError()?.errorCode
            val errorMessage = e.darajaError()?.errorMessage

            // invalid/expired token: clear cache and retry once with a fresh token
            if (errorCode == INVALID_TOKEN_CODE && !isRetry) {
                log.warn("Invalid access token on STK query, refreshing token and retrying once")
                mpesa.invalidateToken()
                return stkPushQuery(checkoutRequestId, isRetry = true)
            }

            // Daraja uses this code while the customer hasn't finished the prompt yet
            if (errorCode == STILL_PROCESSING_CODE) {
                return StkQueryResult(
                    status = StkQueryStatus.PENDING,
                    checkoutRequestId = checkoutRequestId,
                    resultDesc = errorMessage ?: "Transaction is still being processed",
                )
            }

            return StkQueryResult(
                status = StkQueryStatus.ERROR,
                checkoutRequestId = checkoutRequestId,
                resultCode = errorCode,
                resultDesc = errorMessage ?: "Failed to query transaction status",
            )
        } catch (e: IllegalStateException) {
            log.error("STK query failed for CheckoutRequestID={}: {}", checkoutRequestId, e.message)
            return StkQueryResult(
                status = StkQueryStatus.ERROR,
                checkoutRequestId = checkoutRequestId,
                resultDesc = "Failed to query transaction status",
            )
        }
    }

    /** Parses the payload Safaricom POSTs to your callback URL into a flat result. */
    fun handleStkCallback(payload: StkCallbackBody): StkCallbackResult {
        val callback = payload.body.stkCallback

        // ResultCode 0 = success; anything else = failure/cancellation
        if (callback.resultCode != 0) {
            log.warn(
                "STK callback failed: CheckoutRequestID={} ResultCode={} ResultDesc={}",
                callback.checkoutRequestId, callback.resultCode, callback.resultDesc,
            )

            return StkCallbackResult(
                success = false,
                merchantRequestId = callback.merchantRequestId,
                checkoutRequestId = callback.checkoutRequestId,
                resultCode = callback.resultCode,
                resultDesc = callback.resultDesc,
            )
        }

        // success: flatten the CallbackMetadata.Item list into a usable object
        val items = callback.callbackMetadata?.items.orEmpty()
        fun valueOf(name: String): Any? = items.firstOrNull { it.name == name }?.value

        val amount = valueOf("Amount")?.toString()?.toBigDecimalOrNull()
        val mpesaReceiptNumber = valueOf("MpesaReceiptNumber")?.toString()
        val transactionDate = valueOf("TransactionDate")?.toString()?.toLongOrNull()
        val phoneNumber = valueOf("PhoneNumber")?.toString()?.toLongOrNull()

        log.info(
            "STK callback success: CheckoutRequestID={} amount={} receipt={}",
            callback.checkoutRequestId, amount, mpesaReceiptNumber,
        )

        return StkCallbackResult(
            success = true,
            merchantRequestId = callback.merchantRequestId,
            checkoutRequestId = callback.checkoutRequestId,
            resultCode = callback.resultCode,
            resultDesc = callback.resultDesc,
            amount = amount,
            mpesaReceiptNumber = mpesaReceiptNumber,
            transactionDate = transactionDate,
            phoneNumber = phoneNumber,
        )
    }

    // -----------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------

    /** yyyyMMddHHmmss, e.g. 20261029143015 */
    private fun timestamp(): String = TIMESTAMP_FORMAT.format(Instant.now())

    private fun password(timestamp: String): String =
        Base64.getEncoder()
            .encodeToString("${daraja.safShortCode}${daraja.safPasskey}$timestamp".toByteArray())

    private fun RestClientException.httpStatus(): Int? =
        (this as? RestClientResponseException)?.statusCode?.value()

    /** Tries to parse Daraja's error JSON from the response body; null if absent or not JSON. */
    private fun RestClientException.darajaError(): DarajaErrorResponse? {
        val body = (this as? RestClientResponseException)?.responseBodyAsString
        if (body.isNullOrBlank()) return null
        return runCatching { objectMapper.readValue(body, DarajaErrorResponse::class.java) }.getOrNull()
    }

    private companion object {
        const val INVALID_TOKEN_CODE = "404.001.03"
        const val STILL_PROCESSING_CODE = "500.001.1001"
        const val DEFAULT_PUSH_ERROR = "Failed to initiate M-Pesa payment. Please try again."

        val TIMESTAMP_FORMAT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC)
    }
}