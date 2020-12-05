package fr.edjaz.springcloud.eurekaserver.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.password.NoOpPasswordEncoder

@Configuration
class SecurityConfig @Autowired constructor(
    @Value("\${app.eureka-username}") private val username: String,
    @Value("\${app.eureka-password}") private val password: String
) : WebSecurityConfigurerAdapter() {
    @Throws(Exception::class)
    public override fun configure(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication()
            .passwordEncoder(NoOpPasswordEncoder.getInstance())
            .withUser(username).password(password)
            .authorities("USER")
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http // Disable CRCF to allow services to register themselves with Eureka
            .csrf()
            .disable()
            .authorizeRequests()
            .antMatchers("/actuator/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .httpBasic()
    }
}
