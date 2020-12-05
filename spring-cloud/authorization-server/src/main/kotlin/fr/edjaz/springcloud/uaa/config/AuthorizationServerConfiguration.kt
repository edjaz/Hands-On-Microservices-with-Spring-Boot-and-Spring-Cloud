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
import java.util.*
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
                "3851612021791312596791631935569878540203393691253311342052463788814433805390794604753109719790052408607029530149004451377846406736413270923596916756321977922303381344613407820854322190592787335193581632323728135479679928871596911841005827348430783250026013354350760878678723915119966019947072651782000702927096735228356171563532131162414366310012554312756036441054404004920678199077822575051043273088621405687950081861819700809912238863867947415641838115425624808671834312114785499017269379478439158796130804789241476050832773822038351367878951389438751088021113551495469440016698505614123035099067172660197922333993"
            val modulus =
                "18044398961479537755088511127417480155072543594514852056908450877656126120801808993616738273349107491806340290040410660515399239279742407357192875363433659810851147557504389760192273458065587503508596714389889971758652047927503525007076910925306186421971180013159326306810174367375596043267660331677530921991343349336096643043840224352451615452251387611820750171352353189973315443889352557807329336576421211370350554195530374360110583327093711721857129170040527236951522127488980970085401773781530555922385755722534685479501240842392531455355164896023070459024737908929308707435474197069199421373363801477026083786683"
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
