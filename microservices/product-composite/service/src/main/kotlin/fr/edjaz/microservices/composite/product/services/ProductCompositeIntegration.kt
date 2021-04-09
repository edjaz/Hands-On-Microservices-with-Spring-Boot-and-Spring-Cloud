package fr.edjaz.microservices.composite.product.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.hystrix.exception.HystrixRuntimeException
import feign.FeignException
import fr.edjaz.api.core.product.Product
import fr.edjaz.api.core.product.ProductService
import fr.edjaz.api.core.recommendation.Recommendation
import fr.edjaz.api.core.recommendation.RecommendationService
import fr.edjaz.api.core.review.Review
import fr.edjaz.api.core.review.ReviewService
import fr.edjaz.api.event.Event
import fr.edjaz.microservices.composite.product.client.ProductClient
import fr.edjaz.microservices.composite.product.client.RecommendationClient
import fr.edjaz.microservices.composite.product.client.ReviewClient
import fr.edjaz.util.exceptions.InvalidInputException
import fr.edjaz.util.exceptions.NotFoundException
import fr.edjaz.util.http.HttpErrorInfo
import fr.edjaz.util.http.ServiceUtil
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.IOException
import java.time.Duration

@Component
class ProductCompositeIntegration @Autowired constructor(
    private val mapper: ObjectMapper,
    private val messageSources: MessageSources,
    private val serviceUtil: ServiceUtil,
    @Value("\${app.product-service.timeoutSec}") private val productServiceTimeoutSec: Int,
    private val productClient: ProductClient,
    private val recommendationClient: RecommendationClient,
    private val reviewClient: ReviewClient,
) : ProductService, RecommendationService, ReviewService {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    override fun createProduct(body: Product): Product? {
        body.serviceAddress = serviceUtil.serviceAddress
        messageSources.outputProducts(Event(Event.Type.CREATE, body.productId, body))
        return body
    }

    @Retry(name = "product")
    @CircuitBreaker(name = "product")
    override fun getProduct(productId: Int, delay: Int, faultPercent: Int): Mono<Product> {
        return productClient.getProduct(productId, delay, faultPercent)
            .log()
            .onErrorMap { handleException(it) }
            .timeout(Duration.ofSeconds(productServiceTimeoutSec.toLong()))
    }

    override fun deleteProduct(productId: Int) {
        messageSources.outputProducts(Event(Event.Type.DELETE, productId, null))
    }

    override fun createRecommendation(body: Recommendation): Recommendation? {
        body.serviceAddress = serviceUtil.serviceAddress
        messageSources.outputRecommendations(Event(Event.Type.CREATE, body.productId, body))
        return body
    }

    override fun getRecommendations(productId: Int): Flux<Recommendation?>? {
        return recommendationClient.getRecommendations(productId).log()
            .onErrorResume { Flux.empty() }
    }

    override fun deleteRecommendations(productId: Int) {
        messageSources.outputRecommendations(Event(Event.Type.DELETE, productId, null))
    }

    override fun createReview(body: Review): Review {
        body.serviceAddress = serviceUtil.serviceAddress
        messageSources.outputReviews(Event(Event.Type.CREATE, body.productId, body))
        return body
    }

    override fun getReviews(productId: Int): Flux<Review> {
        return reviewClient.getReviews(productId).log()
            .onErrorResume { Flux.empty() }
    }

    override fun deleteReviews(productId: Int) {
        messageSources.outputReviews(Event(Event.Type.DELETE, productId, null))
    }

    private fun handleException(ex: Throwable): Throwable {
        var rootEx = ex

        if (ex is HystrixRuntimeException) {
            rootEx = ex.cause!!
        }

        if (rootEx is FeignException) {
            val wcre = rootEx
            return when (HttpStatus.valueOf(wcre.status())) {
                HttpStatus.NOT_FOUND -> NotFoundException(getErrorMessage(wcre))
                HttpStatus.UNPROCESSABLE_ENTITY -> InvalidInputException(getErrorMessage(wcre))
                else -> {
                    logger.warn(
                        "Got a unexpected HTTP error: {}, will rethrow it",
                        HttpStatus.valueOf(wcre.status())
                    )
                    logger.warn("Error body: {}", wcre.contentUTF8())
                    ex
                }
            }
        }
        logger.warn("Got a unexpected error: {}, will rethrow it", rootEx.toString())
        return rootEx
    }

    private fun getErrorMessage(ex: FeignException): String? {
        return try {
            mapper.readValue(ex.contentUTF8(), HttpErrorInfo::class.java).message
        } catch (ioex: IOException) {
            ex.message
        } catch (io: Exception) {
            ex.message
        }
    }
}
