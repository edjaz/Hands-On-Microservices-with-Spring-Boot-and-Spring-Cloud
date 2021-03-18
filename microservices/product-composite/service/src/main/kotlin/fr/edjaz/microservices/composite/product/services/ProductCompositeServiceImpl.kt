package fr.edjaz.microservices.composite.product.services

import fr.edjaz.api.composite.product.ProductAggregate
import fr.edjaz.api.composite.product.ProductCompositeService
import fr.edjaz.api.composite.product.RecommendationSummary
import fr.edjaz.api.composite.product.ReviewSummary
import fr.edjaz.api.composite.product.ServiceAddresses
import fr.edjaz.api.core.product.Product
import fr.edjaz.api.core.recommendation.Recommendation
import fr.edjaz.api.core.review.Review
import fr.edjaz.util.exceptions.NotFoundException
import fr.edjaz.util.http.ServiceUtil
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.reactor.retry.RetryExceptionWrapper
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class ProductCompositeServiceImpl @Autowired constructor(
    private val serviceUtil: ServiceUtil,
    private val integration: ProductCompositeIntegration
) : ProductCompositeService {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    private val nullSC: SecurityContext = SecurityContextImpl()
    override fun createCompositeProduct(body: ProductAggregate): Mono<Void> {
        return ReactiveSecurityContextHolder.getContext()
            .doOnSuccess { sc: SecurityContext? -> internalCreateCompositeProduct(sc, body) }
            .then()
    }

    fun internalCreateCompositeProduct(sc: SecurityContext?, body: ProductAggregate) {
        try {
            logAuthorizationInfo(sc)
            logger.debug(
                "createCompositeProduct: creates a new composite entity for productId: {}",
                body.productId
            )
            val product = Product(body.productId, body.name, body.weight, null)
            integration.createProduct(product)
            if (body.recommendations != null) {
                body.recommendations!!.forEach(
                    Consumer { (recommendationId, author, rate, content) ->
                        val recommendation = Recommendation(body.productId, recommendationId, author, rate, content, null)
                        integration.createRecommendation(recommendation)
                    }
                )
            }
            if (body.reviews != null) {
                body.reviews!!.forEach(
                    Consumer { (reviewId, author, subject, content) ->
                        val review = Review(body.productId, reviewId, author, subject, content, null)
                        integration.createReview(review)
                    }
                )
            }
            logger.debug(
                "createCompositeProduct: composite entities created for productId: {}",
                body.productId
            )
        } catch (re: RuntimeException) {
            logger.warn("createCompositeProduct failed: {}", re.toString())
            throw re
        }
    }

    override fun getCompositeProduct(productId: Int, delay: Int, faultPercent: Int): Mono<ProductAggregate> {
        return Mono.zip(
            Function { values: Array<Any> ->
                createProductAggregate(
                    values[0] as SecurityContext,
                    values[1] as Product,
                    values[2] as List<Recommendation>,
                    values[3] as List<Review>,
                    serviceUtil.serviceAddress
                )
            },
            ReactiveSecurityContextHolder.getContext().defaultIfEmpty(nullSC),
            integration.getProduct(productId, delay, faultPercent)
                .onErrorMap(RetryExceptionWrapper::class.java) { retryException: RetryExceptionWrapper -> retryException.cause }
                .onErrorReturn(CallNotPermittedException::class.java, getProductFallbackValue(productId)),
            integration.getRecommendations(productId)!!.collectList(),
            integration.getReviews(productId).collectList()
        )
            .doOnError { ex: Throwable ->
                logger.warn(
                    "getCompositeProduct failed: {}",
                    ex.toString()
                )
            }
            .log()
    }

    private fun getProductFallbackValue(productId: Int): Product {
        logger.warn("Creating a fallback product for productId = {}", productId)
        if (productId == 13) {
            val errMsg = "Product Id: $productId not found in fallback cache!"
            logger.warn(errMsg)
            throw NotFoundException(errMsg)
        }
        return Product(productId, "Fallback product$productId", productId, serviceUtil.serviceAddress)
    }

    override fun deleteCompositeProduct(productId: Int): Mono<Void> {
        return ReactiveSecurityContextHolder.getContext()
            .doOnSuccess { sc: SecurityContext? -> internalDeleteCompositeProduct(sc, productId) }
            .then()
    }

    private fun internalDeleteCompositeProduct(sc: SecurityContext?, productId: Int) {
        try {
            logAuthorizationInfo(sc)
            logger.debug(
                "deleteCompositeProduct: Deletes a product aggregate for productId: {}",
                productId
            )
            integration.deleteProduct(productId)
            integration.deleteRecommendations(productId)
            integration.deleteReviews(productId)
            logger.debug(
                "deleteCompositeProduct: aggregate entities deleted for productId: {}",
                productId
            )
        } catch (re: RuntimeException) {
            logger.warn("deleteCompositeProduct failed: {}", re.toString())
            throw re
        }
    }

    private fun createProductAggregate(
        sc: SecurityContext,
        product: Product,
        recommendations: List<Recommendation>?,
        reviews: List<Review>?,
        serviceAddress: String?
    ): ProductAggregate {
        logAuthorizationInfo(sc)

        // 1. Setup product info
        val productId = product.productId
        val name = product.name
        val weight = product.weight

        // 2. Copy summary recommendation info, if available
        val recommendationSummaries = recommendations?.stream()?.map { (_, recommendationId, author, rate, content) ->
            RecommendationSummary(
                recommendationId, author, rate, content
            )
        }?.collect(Collectors.toList())

        // 3. Copy summary review info, if available
        val reviewSummaries = reviews?.stream()
            ?.map { (_, reviewId, author, subject, content) -> ReviewSummary(reviewId, author, subject, content) }
            ?.collect(Collectors.toList())

        // 4. Create info regarding the involved microservices addresses
        val productAddress = product.serviceAddress
        val reviewAddress = if (reviews != null && reviews.size > 0) reviews[0].serviceAddress else ""
        val recommendationAddress =
            if (recommendations != null && recommendations.size > 0) recommendations[0].serviceAddress else ""
        val serviceAddresses = ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress)
        return ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses)
    }

    private fun logAuthorizationInfo(sc: SecurityContext?) {
        if (sc != null && sc.authentication != null && sc.authentication is JwtAuthenticationToken) {
            val jwtToken = (sc.authentication as JwtAuthenticationToken).token
            logAuthorizationInfo(jwtToken)
        } else {
            logger.warn("No JWT based Authentication supplied, running tests are we?")
        }
    }

    private fun logAuthorizationInfo(jwt: Jwt?) {
        if (jwt == null) {
            logger.warn("No JWT supplied, running tests are we?")
        } else {
            if (logger.isDebugEnabled()) {
                val issuer = jwt.issuer
                val audience = jwt.audience
                val subject = jwt.claims["sub"]
                val scopes = jwt.claims["scope"]
                val expires = jwt.claims["exp"]
                logger.debug(
                    "Authorization info: Subject: {}, scopes: {}, expires {}: issuer: {}, audience: {}",
                    subject,
                    scopes,
                    expires,
                    issuer,
                    audience
                )
            }
        }
    }
}
