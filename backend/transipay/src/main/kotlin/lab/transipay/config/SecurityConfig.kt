package lab.transipay.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(12)

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/auth/google", "/auth/login", "/auth/register").permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/mpesa/callback").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/companies").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/staff").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/staff/**").hasRole("ADMIN")
                    .anyRequest().authenticated() // includes /auth/password
            }
            .oauth2ResourceServer { rs -> rs.jwt { it.jwtAuthenticationConverter(converter()) } }
        return http.build()
    }

    private fun converter() = JwtAuthenticationConverter().apply {
        setJwtGrantedAuthoritiesConverter(JwtGrantedAuthoritiesConverter().apply {
            setAuthoritiesClaimName("role")
            setAuthorityPrefix("ROLE_")
        })
    }
}