package lab.transipay.auth

import lab.transipay.config.AppProperties
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.*
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

data class GoogleUser(val sub: String, val email: String, val name: String?)

@Component
class GoogleTokenVerifier(props: AppProperties) {

    private val clientId = props.google.clientId

    // Not exposed as a bean, so it doesn't clash with our own JwtDecoder
    private val decoder: NimbusJwtDecoder =
        NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs").build().apply {
            val issuer = OAuth2TokenValidator<Jwt> { jwt ->
                val iss = jwt.getClaimAsString("iss")
                if (iss == "https://accounts.google.com" || iss == "accounts.google.com")
                    OAuth2TokenValidatorResult.success()
                else OAuth2TokenValidatorResult.failure(OAuth2Error("invalid_issuer"))
            }
            val audience = OAuth2TokenValidator<Jwt> { jwt ->
                if (jwt.audience?.contains(clientId) == true) OAuth2TokenValidatorResult.success()
                else OAuth2TokenValidatorResult.failure(OAuth2Error("invalid_audience"))
            }
            setJwtValidator(DelegatingOAuth2TokenValidator(JwtTimestampValidator(), issuer, audience))
        }
    fun verify(idToken: String): GoogleUser {
        val jwt = try {
            decoder.decode(idToken)
        } catch (e: JwtException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token")
        }
        if (jwt.getClaimAsBoolean("email_verified") != true)
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google email not verified")

        val sub = jwt.subject
        val email = jwt.getClaimAsString("email")
        if (sub == null || email == null)
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google token missing required claims")

        return GoogleUser(sub = sub, email = email, name = jwt.getClaimAsString("name"))
    }
}