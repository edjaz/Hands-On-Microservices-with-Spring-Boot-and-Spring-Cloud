package fr.edjaz.microservices.core.recommendation

import fr.edjaz.api.core.product.Product
import fr.edjaz.api.core.recommendation.Recommendation
import fr.edjaz.api.event.Event
import fr.edjaz.microservices.core.recommendation.persistence.RecommendationRepository
import fr.edjaz.util.exceptions.InvalidInputException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.integration.channel.AbstractMessageChannel
import org.springframework.messaging.MessagingException
import org.springframework.messaging.support.GenericMessage
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.data.mongodb.port: 0",
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.cloud.kubernetes.enabled= false",
        "spring.cloud.kubernetes.discovery.enabled=false",
        "spring.cloud.kubernetes.loadbalancer.enabled=false",
        "spring.data.mongodb.auto-index-creation= true",
        "server.error.include-message=always",
        "server.error.include-binding-errors=always"
    ]
)
class RecommendationServiceApplicationTests {
    @Autowired
    private val client: WebTestClient? = null

    @Autowired
    private val repository: RecommendationRepository? = null

    @Autowired
    private val channels: Sink? = null
    private var input: AbstractMessageChannel? = null

    @BeforeEach
    fun setupDb() {
        input = channels!!.input() as AbstractMessageChannel
        repository!!.deleteAll().block()
    }

    @Test
    fun recommendationsByProductId() {
        val productId = 1
        sendCreateRecommendationEvent(productId, 1)
        sendCreateRecommendationEvent(productId, 2)
        sendCreateRecommendationEvent(productId, 3)
        Assertions.assertEquals(3, repository!!.findByProductId(productId).count().block() as Long)
        getAndVerifyRecommendationsByProductId(productId, HttpStatus.OK)
            .jsonPath("$.length()").isEqualTo(3)
            .jsonPath("$[2].productId").isEqualTo(productId)
            .jsonPath("$[2].recommendationId").isEqualTo(3)
    }

    @Test
    fun duplicateError() {
        val productId = 1
        val recommendationId = 1
        sendCreateRecommendationEvent(productId, recommendationId)
        Assertions.assertEquals(1, repository!!.count().block() as Long)
        try {
            sendCreateRecommendationEvent(productId, recommendationId)
            Assertions.fail<Any>("Expected a MessagingException here!")
        } catch (me: MessagingException) {
            if (me.cause is InvalidInputException) {
                val iie = me.cause as InvalidInputException?
                Assertions.assertEquals("Duplicate key, Product Id: 1, Recommendation Id:1", iie!!.message)
            } else {
                Assertions.fail<Any>("Expected a InvalidInputException as the root cause!")
            }
        }
        Assertions.assertEquals(1, repository.count().block() as Long)
    }

    @Test
    fun deleteRecommendations() {
        val productId = 1
        val recommendationId = 1
        sendCreateRecommendationEvent(productId, recommendationId)
        Assertions.assertEquals(1, repository!!.findByProductId(productId).count().block() as Long)
        sendDeleteRecommendationEvent(productId)
        Assertions.assertEquals(0, repository.findByProductId(productId).count().block() as Long)
        sendDeleteRecommendationEvent(productId)
    }

    @Test
    fun recommendationsMissingParameter() {
        getAndVerifyRecommendationsByProductId("", HttpStatus.BAD_REQUEST)
            .jsonPath("$.path").isEqualTo("/recommendation")
            .jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present")
    }

    @Test
    fun recommendationsInvalidParameter() {
        getAndVerifyRecommendationsByProductId("?productId=no-integer", HttpStatus.BAD_REQUEST)
            .jsonPath("$.path").isEqualTo("/recommendation")
            .jsonPath("$.message").isEqualTo("Type mismatch.")
    }

    @Test
    fun recommendationsNotFound() {
        getAndVerifyRecommendationsByProductId("?productId=113", HttpStatus.OK)
            .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun recommendationsInvalidParameterNegativeValue() {
        val productIdInvalid = -1
        getAndVerifyRecommendationsByProductId("?productId=$productIdInvalid", HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/recommendation")
            .jsonPath("$.message").isEqualTo("Invalid productId: $productIdInvalid")
    }

    private fun getAndVerifyRecommendationsByProductId(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        return getAndVerifyRecommendationsByProductId("?productId=$productId", expectedStatus)
    }

    private fun getAndVerifyRecommendationsByProductId(
        productIdQuery: String,
        expectedStatus: HttpStatus
    ): BodyContentSpec {
        return client!!.get()
            .uri("/recommendation$productIdQuery")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
    }

    private fun sendCreateRecommendationEvent(productId: Int, recommendationId: Int) {
        val recommendation = Recommendation(
            productId,
            recommendationId,
            "Author $recommendationId",
            recommendationId,
            "Content $recommendationId",
            "SA"
        )
        val event: Event<Int, Recommendation> = Event(Event.Type.CREATE, productId, recommendation)
        input!!.send(GenericMessage(event))
    }

    private fun sendDeleteRecommendationEvent(productId: Int) {
        val event: Event<Int, Product> = Event(Event.Type.DELETE, productId, null)
        input!!.send(GenericMessage(event))
    }
}
