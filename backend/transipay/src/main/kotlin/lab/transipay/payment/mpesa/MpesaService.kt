package lab.transipay.payment.mpesa

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body
import org.springframework.web.server.ResponseStatusException


@Service
class MpesaService(
    private val daraja: DarajaProperties,
    restClientBuilder: RestClient.Builder,
) {
    private val log = LoggerFactory.getLogger(MpesaService::class.java)

    private val restClient: RestClient

    /** Immutable snapshot so token + expiry are always read together. */
    private data class CachedToken(val value: String, val expiresAtMillis: Long)

    @Volatile
    private var cachedToken: CachedToken? = null
    private val tokenLock = Any()

    init {
        validateConfig()
        restClient = restClientBuilder.baseUrl(daraja.safUrl).build()
    }

    /** Returns a valid Daraja access token, refreshing it when it is about to expire. */
    fun mpesaAuth(): String {
        cachedToken?.takeIf { it.isFresh() }?.let { return it.value }

        // Unlike Node, requests run on multiple threads, so guard the refresh
        // to avoid several threads hitting the OAuth endpoint at once.
        synchronized(tokenLock) {
            cachedToken?.takeIf { it.isFresh() }?.let { return it.value }

            val fresh = fetchToken()
            cachedToken = fresh
            return fresh.value
        }
    }

    /** Drops the cached token so the next mpesaAuth() call fetches a fresh one. */
    fun invalidateToken() {
        synchronized(tokenLock) { cachedToken = null }
    }

    private fun fetchToken(): CachedToken {
        try {
            val response = restClient.get()
                .uri("/oauth/v1/generate?grant_type=client_credentials")
                .headers { it.setBasicAuth(daraja.consumerKey, daraja.consumerSecret) }
                .retrieve()
                .body<DarajaAuthResponse>()
                ?: throw IllegalStateException("Empty response from Daraja")

            return CachedToken(
                value = response.accessToken,
                expiresAtMillis = System.currentTimeMillis() + response.expiresIn.toLong() * 1000,
            )
        } catch (e: RestClientResponseException) {
            log.error("Daraja auth failed: {} {}", e.statusCode.value(), e.responseBodyAsString)
            throw authFailure()
        } catch (e: RestClientException) {
            log.error("Daraja auth failed: {}", e.message)
            throw authFailure()
        } catch (e: IllegalStateException) {
            log.error("Daraja auth failed: {}", e.message)
            throw authFailure()
        }
    }

    private fun authFailure() = ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Failed to authenticate with Mpesa",
    )

    private fun CachedToken.isFresh(): Boolean =
        System.currentTimeMillis() < expiresAtMillis - EXPIRY_BUFFER_MS

    private fun validateConfig() {
        with(daraja) {
            val missing = listOf(
                "consumerKey" to consumerKey,
                "consumerSecret" to consumerSecret,
                "safUrl" to safUrl,
                "safShortCode" to safShortCode,
                "safPasskey" to safPasskey,
                "safCallbackUrl" to safCallbackUrl,
            ).filter { it.second.isBlank() }.map { it.first }

            if (missing.isNotEmpty()) {
                throw ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Daraja credentials are not configured: ${missing.joinToString()}",
                )
            }
        }
    }

    /** Converts 07xx / 01xx / +254 / 00254 / 7xx formats into 2547XXXXXXXX or 2541XXXXXXXX. */
    fun normalizePhone(phone: Any): String? {
        var cleaned = phone.toString().replace(NON_DIGITS, "")

        if (cleaned.startsWith("00254")) {
            cleaned = cleaned.drop(2)
        }

        cleaned = when {
            cleaned.startsWith("0") -> "254" + cleaned.drop(1)
            SHORT_LOCAL.matches(cleaned) -> "254$cleaned"
            else -> cleaned
        }

        return cleaned.takeIf { KENYAN_MSISDN.matches(it) }
    }

    private companion object {
        const val EXPIRY_BUFFER_MS = 60_000L
        val NON_DIGITS = Regex("\\D")
        val SHORT_LOCAL = Regex("^[17]\\d{8}$")
        val KENYAN_MSISDN = Regex("^254[17]\\d{8}$")
    }
}