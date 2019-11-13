
package fr.edjaz.springcloud.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
@Slf4j
public class SecurityConfig {


	@Bean
	SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) throws Exception {
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
				.jwt();
		return http.build();
	}

}