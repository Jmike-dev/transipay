package lab.transipay.payment.mpesa

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "daraja")
data class DarajaProperties(
    val consumerKey: String = "",
    val consumerSecret: String = "",
    val safUrl: String = "",
    val safShortCode: String = "",
    val safPasskey: String = "",
    val safCallbackUrl: String = "",
)