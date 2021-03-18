/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.edjaz.springcloud.uaa.config

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPair
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest

/**
 * An instance of Legacy Authorization Server (spring-security-oauth2) that uses a single,
 * not-rotating key and exposes a JWK endpoint.
 *
 * See
 * [
 * Spring Security OAuth Autoconfig's documentation](https://docs.spring.io/spring-security-oauth2-boot/docs/current-SNAPSHOT/reference/htmlsingle/) for additional detail
 *
 * @author Josh Cummings
 * @since 5.1
 */
@EnableAuthorizationServer
@Configuration
class AuthorizationServerConfiguration(
    authenticationConfiguration: AuthenticationConfiguration,
    keyPair: KeyPair,
    @Value("\${security.oauth2.authorizationserver.jwt.enabled:true}") jwtEnabled: Boolean
) : AuthorizationServerConfigurerAdapter() {
    lateinit var authenticationManager: AuthenticationManager
    lateinit var keyPair: KeyPair
    var jwtEnabled: Boolean = false

    @Throws(Exception::class)
    override fun configure(clients: ClientDetailsServiceConfigurer) {

        // Password is prefixed with {noop} to indicate to DelegatingPasswordEncoder that
        // NoOpPasswordEncoder should be used.
        // This is not safe for production, but makes reading
        // in samples easier.
        // Normally passwords should be hashed using BCrypt

        // @formatter:off
        clients.inMemory()
            .withClient("reader")
            .authorizedGrantTypes("code", "authorization_code", "implicit", "password")
            .redirectUris("http://my.redirect.uri")
            .secret("{noop}secret")
            .scopes("product:read")
            .accessTokenValiditySeconds(600000000)
            .and()
            .withClient("writer")
            .authorizedGrantTypes("code", "authorization_code", "implicit", "password")
            .redirectUris("http://my.redirect.uri")
            .secret("{noop}secret")
            .scopes("product:read", "product:write")
            .accessTokenValiditySeconds(600000000)
            .and()
            .withClient("noscopes")
            .authorizedGrantTypes("code", "authorization_code", "implicit", "password")
            .redirectUris("http://my.redirect.uri")
            .secret("{noop}secret")
            .scopes("none")
            .accessTokenValiditySeconds(600000000)
        // @formatter:on
    }

    @Throws(Exception::class)
    override fun configure(endpoints: AuthorizationServerEndpointsConfigurer) {
        // @formatter:off
        endpoints
            .authenticationManager(authenticationManager)
            .tokenStore(tokenStore())
        if (jwtEnabled) {
            endpoints
                .accessTokenConverter(accessTokenConverter())
        }
        // @formatter:on
    }

    @Bean
    fun tokenStore(): TokenStore {
        return if (jwtEnabled) {
            JwtTokenStore(accessTokenConverter())
        } else {
            InMemoryTokenStore()
        }
    }

    @Bean
    fun accessTokenConverter(): JwtAccessTokenConverter {
        val converter = JwtAccessTokenConverter()
        converter.setKeyPair(keyPair)
        val accessTokenConverter = DefaultAccessTokenConverter()
        accessTokenConverter.setUserTokenConverter(SubjectAttributeUserTokenConverter())
        converter.accessTokenConverter = accessTokenConverter
        return converter
    }

    init {
        authenticationManager = authenticationConfiguration.authenticationManager
        this.keyPair = keyPair
        this.jwtEnabled = jwtEnabled
    }
}

/**
 * For configuring the end users recognized by this Authorization Server
 */
@Configuration
internal class UserConfig : WebSecurityConfigurerAdapter() {
    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
            .authorizeRequests()
            .antMatchers("/actuator/**").permitAll()
            .mvcMatchers("/.well-known/jwks.json").permitAll()
            .anyRequest().authenticated()
            .and()
            .httpBasic()
            .and()
            .csrf()
            .ignoringRequestMatchers(RequestMatcher { request: HttpServletRequest -> "/introspect" == request.requestURI })
    }

    @Bean
    public override fun userDetailsService(): UserDetailsService {
        return InMemoryUserDetailsManager(
            User.withDefaultPasswordEncoder()
                .username("dkahn")
                .password("password")
                .roles("USER")
                .build()
        )
    }
}

/**
 * Legacy Authorization Server (spring-security-oauth2) does not support any
 * Token Introspection endpoint.
 *
 * This class adds ad-hoc support in order to better support the other samples in the repo.
 */
@FrameworkEndpoint
internal class IntrospectEndpoint(var tokenStore: TokenStore) {
    @PostMapping("/introspect")
    @ResponseBody
    fun introspect(@RequestParam("token") token: String?): Map<String, Any> {
        val accessToken = tokenStore.readAccessToken(token)
        val attributes: MutableMap<String, Any> = HashMap()
        if (accessToken == null || accessToken.isExpired) {
            attributes["active"] = false
            return attributes
        }
        val authentication = tokenStore.readAuthentication(token)
        attributes["active"] = true
        attributes["exp"] = accessToken.expiration.time
        attributes["scope"] = accessToken.scope.stream().collect(Collectors.joining(" "))
        attributes["sub"] = authentication.name
        return attributes
    }
}

/**
 * Legacy Authorization Server (spring-security-oauth2) does not support any
 * [JWK Set](https://tools.ietf.org/html/rfc7517#section-5) endpoint.
 *
 * This class adds ad-hoc support in order to better support the other samples in the repo.
 */
@FrameworkEndpoint
internal class JwkSetEndpoint(var keyPair: KeyPair) {
    @get:ResponseBody
    @get:GetMapping("/.well-known/jwks.json")
    val key: Map<String, Any>
        get() {
            val publicKey = keyPair.public as RSAPublicKey
            val key = RSAKey.Builder(publicKey).build()
            return JWKSet(key).toJSONObject()
        }
}

/**
 * An Authorization Server will more typically have a key rotation strategy, and the keys will not
 * be hard-coded into the application code.
 *
 * For simplicity, though, this fr.edjaz.springcloud.uaa.sample doesn't demonstrate key rotation.
 */
@Configuration
internal class KeyConfig {
    @Bean
    fun keyPair(): KeyPair {
        return try {
            val privateExponent =
                "385161202179131259679163193556987854020339369125331134205246378881443380539079460475310971979005240860702953014900445137" +
                    "784640673641327092359691675632197792230338134461340782085432219059278733519358163232372813547967992887159691184100582734" +
                    "843078325002601335435076087867872391511996601994707265178200070292709673522835617156353213116241436631001255431275603644" +
                    "105440400492067819907782257505104327308862140568795008186181970080991223886386794741564183811542562480867183431211478549" +
                    "9017269379478439158796130804789241476050832773822038351367878951389438751088021113551495469440016698505614123035099067172" +
                    "660197922333993"
            val modulus =
                "180443989614795377550885111274174801550725435945148520569084508776561261208018089936167382733491074918063402900404106605" +
                    "153992392797424073571928753634336598108511475575043897601922734580655875035085967143898899717586520479275035250070769109" +
                    "253061864219711800131593263068101743673755960432676603316775309219913433493360966430438402243524516154522513876118207501" +
                    "713523531899733154438893525578073293365764212113703505541955303743601105833270937117218571291700405272369515221274889809" +
                    "700854017737815305559223857557225346854795012408423925314553551648960230704590247379089293087074354741970691994213733638" +
                    "01477026083786683"
            val exponent = "65537"
            val publicSpec = RSAPublicKeySpec(BigInteger(modulus), BigInteger(exponent))
            val privateSpec = RSAPrivateKeySpec(BigInteger(modulus), BigInteger(privateExponent))
            val factory = KeyFactory.getInstance("RSA")
            KeyPair(factory.generatePublic(publicSpec), factory.generatePrivate(privateSpec))
        } catch (e: Exception) {
            throw IllegalArgumentException(e)
        }
    }
}

/**
 * Legacy Authorization Server does not support a custom name for the user parameter, so we'll need
 * to extend the default. By default, it uses the attribute `user_name`, though it would be
 * better to adhere to the `sub` property defined in the
 * [JWT Specification](https://tools.ietf.org/html/rfc7519).
 */
internal class SubjectAttributeUserTokenConverter : DefaultUserAuthenticationConverter() {
    override fun convertUserAuthentication(authentication: Authentication): Map<String, *> {
        val response: MutableMap<String, Any> = LinkedHashMap()
        response["sub"] = authentication.name
        if (authentication.authorities != null && !authentication.authorities.isEmpty()) {
            response[AUTHORITIES] = AuthorityUtils.authorityListToSet(authentication.authorities)
        }
        return response
    }
}
