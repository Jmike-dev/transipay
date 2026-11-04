package lab.transipay.payment.mpesa

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class SdkPushResponse(
    @JsonProperty("MerchantRequestID") val merchantRequestId: String,
    @JsonProperty("CheckoutRequestID") val checkoutRequestId: String,
    // Daraja sends "0" as a string; Jackson coerces it to Int
    @JsonProperty("ResponseCode") val responseCode: Int,
    @JsonProperty("ResponseDescription") val responseDescription: String,
    @JsonProperty("CustomerMessage") val customerMessage: String,
)

/** Error body Daraja returns on 4xx/5xx responses. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class DarajaErrorResponse(
    val requestId: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
)

/** What your service returns to callers after initiating an STK push. */
data class SdkPushResult(
    val success: Boolean,
    val amount: BigDecimal,
    val merchantRequestId: String? = null,
    val checkoutRequestId: String? = null,
    val customerMessage: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
)

// ---------------------------------------------------------------------------
// STK callback (Safaricom POSTs this to your callback URL)
// ---------------------------------------------------------------------------

@JsonIgnoreProperties(ignoreUnknown = true)
data class StkCallbackItem(
    @JsonProperty("Name") val name: String,
    // String | number in Daraja; Jackson gives String, Int, Long or Double
    @JsonProperty("Value") val value: Any? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class StkCallbackMetadata(
    @JsonProperty("Item") val items: List<StkCallbackItem> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class StkCallback(
    @JsonProperty("MerchantRequestID") val merchantRequestId: String,
    @JsonProperty("CheckoutRequestID") val checkoutRequestId: String,
    @JsonProperty("ResultCode") val resultCode: Int,
    @JsonProperty("ResultDesc") val resultDesc: String,
    // Only present when the payment succeeded
    @JsonProperty("CallbackMetadata") val callbackMetadata: StkCallbackMetadata? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class StkCallbackEnvelope(
    @JsonProperty("stkCallback") val stkCallback: StkCallback,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class StkCallbackBody(
    @JsonProperty("Body") val body: StkCallbackEnvelope,
)

/** Flattened, parsed version of the callback for use in your own code. */
data class StkCallbackResult(
    val success: Boolean,
    val merchantRequestId: String,
    val checkoutRequestId: String,
    val resultCode: Int,
    val resultDesc: String,
    val amount: BigDecimal? = null,
    val mpesaReceiptNumber: String? = null,
    val transactionDate: Long? = null,
    val phoneNumber: Long? = null,
)

// ---------------------------------------------------------------------------
// STK query
// ---------------------------------------------------------------------------

@JsonIgnoreProperties(ignoreUnknown = true)
data class StkQueryResponse(
    @JsonProperty("ResponseCode") val responseCode: String,
    @JsonProperty("ResponseDescription") val responseDescription: String,
    @JsonProperty("MerchantRequestID") val merchantRequestId: String,
    @JsonProperty("CheckoutRequestID") val checkoutRequestId: String,
    // String | number in Daraja; Jackson coerces numbers to String here
    @JsonProperty("ResultCode") val resultCode: String,
    @JsonProperty("ResultDesc") val resultDesc: String,
)

enum class StkQueryStatus(@get:JsonValue val value: String) {
    SUCCESS("success"),
    FAILED("failed"),
    PENDING("pending"),
    ERROR("error"),
}

data class StkQueryResult(
    val status: StkQueryStatus,
    val resultDesc: String,
    val merchantRequestId: String? = null,
    val checkoutRequestId: String? = null,
    val resultCode: String? = null,
)