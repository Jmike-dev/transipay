package lab.transipay.payment.mpesa

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
data class DarajaAuthResponse(
    @JsonProperty("access_token") val accessToken: String,
    // Daraja returns this as a string, e.g. "3599"
    @JsonProperty("expires_in") val expiresIn: String,
)