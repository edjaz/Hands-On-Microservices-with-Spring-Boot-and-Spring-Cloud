package fr.edjaz.springcloud.gateway.config

import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.context.annotation.Bean
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import java.lang.Exception

@EnableWebFluxSecurity
class SecurityConfig {
    @Bean
    @Throws(Exception::class)
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http
            .csrf().disable()
            .authorizeExchange()
            .pathMatchers("/headerrouting/**").permitAll()
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers("/eureka/**").permitAll()
            .pathMatchers("/oauth/**").permitAll()
            .pathMatchers("/config/**").permitAll()
            .anyExchange().authenticated()
            .and()
            .oauth2ResourceServer()
            .jwt()
        return http.build()
    }
}
