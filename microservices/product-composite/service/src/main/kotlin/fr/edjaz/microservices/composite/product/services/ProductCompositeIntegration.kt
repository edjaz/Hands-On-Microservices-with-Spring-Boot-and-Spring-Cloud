package fr.edjaz.microservices.composite.product.services

import com.fasterxml.jackson.databind.ObjectMapper
import fr.edjaz.api.core.product.Product
import fr.edjaz.api.core.product.ProductService
import fr.edjaz.api.core.recommendation.Recommendation
import fr.edjaz.api.core.recommendation.RecommendationService
import fr.edjaz.api.core.review.Review
import fr.edjaz.api.core.review.ReviewService
import fr.edjaz.api.event.Event
import fr.edjaz.util.exceptions.InvalidInputException
import fr.edjaz.util.exceptions.NotFoundException
import fr.edjaz.util.http.HttpErrorInfo
import fr.edjaz.util.http.ServiceUtil
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.http.HttpStatus
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.IOException
import java.time.Duration

@Component
class ProductCompositeIntegration @Autowired constructor(
    private val webClientBuilder: WebClient.Builder,
    private val mapper: ObjectMapper,
    private val messageSources: MessageSources,
    private val serviceUtil: ServiceUtil,
    @Value("\${app.product-service.timeoutSec}") private val productServiceTimeoutSec: Int
) : ProductService, RecommendationService, ReviewService {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    private val productServiceUrl = "http://product"
    private val recommendationServiceUrl = "http://recommendation"
    private val reviewServiceUrl = "http://review"
    private var webClient: WebClient? = null
        private get() {
            if (field == null) {
                field = webClientBuilder.build()
            }
            return field
        }

    override fun createProduct(body: Product): Product? {
        messageSources.outputProducts(Event(Event.Type.CREATE, body.productId, body))
        return body
    }

    @Retry(name = "product")
    @CircuitBreaker(name = "product")
    override fun getProduct(productId: Int, delay: Int, faultPercent: Int): Mono<Product> {
        val url =
            UriComponentsBuilder.fromUriString("$productServiceUrl/product/{productId}?delay={delay}&faultPercent={faultPercent}")
                .build(productId, delay, faultPercent)
        logger.debug("Will call the getProduct API on URL: {}", url)
        return webClient!!.get().uri(url)
            .retrieve().bodyToMono(Product::class.java).log()
            .onErrorMap(WebClientResponseException::class.java) { ex: WebClientResponseException -> handleException(ex) }
            .timeout(Duration.ofSeconds(productServiceTimeoutSec.toLong()))
    }

    override fun deleteProduct(productId: Int) {
        messageSources.outputProducts(Event(Event.Type.DELETE, productId, null))
    }

    override fun createRecommendation(body: Recommendation?): Recommendation? {
        messageSources.outputRecommendations(Event(Event.Type.CREATE, body!!.productId, body))
        return body
    }

    override fun getRecommendations(productId: Int): Flux<Recommendation?>? {
        val url = UriComponentsBuilder.fromUriString("$recommendationServiceUrl/recommendation?productId={productId}")
            .build(productId)
        logger.debug("Will call the getRecommendations API on URL: {}", url)

        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return webClient!!.get().uri(url).retrieve().bodyToFlux(Recommendation::class.java).log()
            .onErrorResume { error: Throwable? -> Flux.empty() }
    }

    override fun deleteRecommendations(productId: Int) {
        messageSources.outputRecommendations(Event(Event.Type.DELETE, productId, null))
    }

    override fun createReview(body: Review): Review {
        messageSources.outputReviews(Event(Event.Type.CREATE, body.productId, body))
        return body
    }

    override fun getReviews(productId: Int): Flux<Review> {
        val url = UriComponentsBuilder.fromUriString("$reviewServiceUrl/review?productId={productId}").build(productId)
        logger.debug("Will call the getReviews API on URL: {}", url)

        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return webClient!!.get().uri(url).retrieve().bodyToFlux(Review::class.java).log()
            .onErrorResume { error: Throwable? -> Flux.empty() }
    }

    override fun deleteReviews(productId: Int) {
        messageSources.outputReviews(Event(Event.Type.DELETE, productId, null))
    }

    private fun handleException(ex: Throwable): Throwable {
        if (ex !is WebClientResponseException) {
            logger.warn("Got a unexpected error: {}, will rethrow it", ex.toString())
            return ex
        }
        val wcre = ex
        return when (wcre.statusCode) {
            HttpStatus.NOT_FOUND -> NotFoundException(getErrorMessage(wcre))
            HttpStatus.UNPROCESSABLE_ENTITY -> InvalidInputException(getErrorMessage(wcre))
            else -> {
                logger.warn(
                    "Got a unexpected HTTP error: {}, will rethrow it",
                    wcre.statusCode
                )
                logger.warn("Error body: {}", wcre.responseBodyAsString)
                ex
            }
        }
    }

    private fun getErrorMessage(ex: WebClientResponseException): String? {
        return try {
            mapper.readValue(ex.responseBodyAsString, HttpErrorInfo::class.java).message
        } catch (ioex: IOException) {
            ex.message
        } catch (io: Exception) {
            ex.message
        }
    }
}
