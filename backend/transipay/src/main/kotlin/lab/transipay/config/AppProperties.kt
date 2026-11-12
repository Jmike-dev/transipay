package lab.transipay.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "transipay")
data class AppProperties(val google: Google, val jwt: Jwt) {
    data class Google(val clientId: String)
    data class Jwt(val secret: String, val expiryMinutes: Long = 60)
}