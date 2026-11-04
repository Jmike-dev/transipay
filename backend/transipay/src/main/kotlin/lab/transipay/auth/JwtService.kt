package lab.transipay.auth

import com.nimbusds.jose.jwk.source.ImmutableSecret
import lab.transipay.config.AppProperties
import lab.transipay.staff.StaffEntity
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.*
import org.springframework.stereotype.Service
import java.time.Instant
import javax.crypto.spec.SecretKeySpec

@Service
class JwtService(private val props: AppProperties) {

    private val key = SecretKeySpec(props.jwt.secret.toByteArray(), "HmacSHA256")
    private val encoder = NimbusJwtEncoder(ImmutableSecret(key))

    @get:Bean
    val jwtDecoder: JwtDecoder
        get() = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build()

    fun issue(staff: StaffEntity): Pair<String, Long> {
        val ttl = props.jwt.expiryMinutes * 60
        val now = Instant.now()
        val claims = JwtClaimsSet.builder()
            .issuer("transipay")
            .subject(staff.id.toString())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(ttl))
            .claim("companyId", staff.company.id.toString())
            .claim("role", staff.role)
            .build()
        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        return encoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue to ttl
    }
}