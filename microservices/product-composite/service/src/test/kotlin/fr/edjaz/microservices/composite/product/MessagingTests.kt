package fr.edjaz.microservices.composite.product

import fr.edjaz.api.composite.product.ProductAggregate
import fr.edjaz.api.composite.product.RecommendationSummary
import fr.edjaz.api.composite.product.ReviewSummary
import fr.edjaz.api.core.product.Product
import fr.edjaz.api.core.recommendation.Recommendation
import fr.edjaz.api.core.review.Review
import fr.edjaz.api.event.Event
import fr.edjaz.microservices.composite.product.services.MessageSources
import java.util.concurrent.BlockingQueue
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.cloud.stream.test.binder.MessageCollector
import org.springframework.cloud.stream.test.matcher.MessageQueueMatcher
import org.springframework.http.HttpStatus
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    classes = [ProductCompositeServiceApplication::class, TestSecurityConfig::class],
    properties = [
        "spring.main.allow-bean-definition-overriding=true",
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.cloud.kubernetes.enabled= false",
        "spring.cloud.kubernetes.discovery.enabled=false",
        "spring.cloud.kubernetes.loadbalancer.enabled=false",
        "kubernetes.manifests.enabled=false",
        "kubernetes.informer.enabled=false"
    ]
)
class MessagingTests {
    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var channels: MessageSources

    @Autowired
    private lateinit var collector: MessageCollector

    lateinit var queueProducts: BlockingQueue<Message<*>>
    lateinit var queueRecommendations: BlockingQueue<Message<*>>
    lateinit var queueReviews: BlockingQueue<Message<*>>

    @BeforeEach
    fun setUp() {
        queueProducts = getQueue(channels.outputProducts())
        queueRecommendations = getQueue(channels.outputRecommendations())
        queueReviews = getQueue(channels.outputReviews())
    }

    @Test
    fun createCompositeProduct1() {
        val composite = ProductAggregate(1, "name", 1, null, null, null)
        postAndVerifyProduct(composite, HttpStatus.OK)

        // Assert one expected new product events queued up
        Assertions.assertEquals(1, queueProducts.size)
        val expectedEvent: Event<Int, Product> = Event(
            Event.Type.CREATE,
            composite.productId,
            Product(composite.productId, composite.name, composite.weight, null)
        )
        MatcherAssert.assertThat(
            queueProducts,
            Matchers.`is`(
                MessageQueueMatcher.receivesPayloadThat(
                    IsSameEvent.sameEventExceptCreatedAt(expectedEvent)
                )
            )
        )

        // Assert none recommendations and review events
        Assertions.assertEquals(0, queueRecommendations.size)
        Assertions.assertEquals(0, queueReviews.size)
    }

    @Test
    fun createCompositeProduct2() {
        val composite = ProductAggregate(
            1,
            "name",
            1,
            listOf(
                RecommendationSummary(
                    1,
                    "a",
                    1,
                    "c"
                )
            ),
            listOf(
                ReviewSummary(
                    1,
                    "a",
                    "s",
                    "c"
                )
            ),
            null
        )
        postAndVerifyProduct(composite, HttpStatus.OK)

        // Assert one create product event queued up
        Assertions.assertEquals(1, queueProducts.size)
        val expectedProductEvent: Event<Int, Product> = Event(
            Event.Type.CREATE,
            composite.productId,
            Product(composite.productId, composite.name, composite.weight, null)
        )
        MatcherAssert.assertThat(
            queueProducts,
            MessageQueueMatcher.receivesPayloadThat(
                IsSameEvent.sameEventExceptCreatedAt(expectedProductEvent)
            )
        )

        // Assert one create recommendation event queued up
        Assertions.assertEquals(1, queueRecommendations.size)
        val (recommendationId, author, rate, content) = composite.recommendations!![0]
        val expectedRecommendationEvent: Event<Int, Recommendation> = Event(
            Event.Type.CREATE,
            composite.productId,
            Recommendation(composite.productId, recommendationId, author, rate, content, null)
        )
        MatcherAssert.assertThat(
            queueRecommendations,
            MessageQueueMatcher.receivesPayloadThat(
                IsSameEvent.sameEventExceptCreatedAt(expectedRecommendationEvent)
            )
        )

        // Assert one create review event queued up
        Assertions.assertEquals(1, queueReviews.size)
        val (reviewId, author1, subject, content1) = composite.reviews!![0]
        val expectedReviewEvent: Event<Int, Review> = Event(
            Event.Type.CREATE,
            composite.productId,
            Review(composite.productId, reviewId, author1, subject, content1, null)
        )
        MatcherAssert.assertThat(
            queueReviews,
            MessageQueueMatcher.receivesPayloadThat(
                IsSameEvent.sameEventExceptCreatedAt(expectedReviewEvent)
            )
        )
    }

    @Test
    fun deleteCompositeProduct() {
        deleteAndVerifyProduct(1, HttpStatus.OK)

        // Assert one delete product event queued up
        Assertions.assertEquals(1, queueProducts.size)
        val expectedEvent: Event<Int, Product> = Event(Event.Type.DELETE, 1, null)
        MatcherAssert.assertThat(
            queueProducts,
            Matchers.`is`(
                MessageQueueMatcher.receivesPayloadThat(
                    IsSameEvent.sameEventExceptCreatedAt(expectedEvent)
                )
            )
        )

        // Assert one delete recommendation event queued up
        Assertions.assertEquals(1, queueRecommendations.size)
        val expectedRecommendationEvent: Event<Int, Recommendation> = Event(Event.Type.DELETE, 1, null)
        MatcherAssert.assertThat(
            queueRecommendations,
            MessageQueueMatcher.receivesPayloadThat(
                IsSameEvent.sameEventExceptCreatedAt(expectedRecommendationEvent)
            )
        )

        // Assert one delete review event queued up
        Assertions.assertEquals(1, queueReviews.size)
        val expectedReviewEvent: Event<Int, Review> = Event(Event.Type.DELETE, 1, null)
        MatcherAssert.assertThat(
            queueReviews,
            MessageQueueMatcher.receivesPayloadThat(
                IsSameEvent.sameEventExceptCreatedAt(expectedReviewEvent)
            )
        )
    }

    private fun getQueue(messageChannel: MessageChannel): BlockingQueue<Message<*>> {
        return collector.forChannel(messageChannel)
    }

    private fun postAndVerifyProduct(compositeProduct: ProductAggregate, expectedStatus: HttpStatus) {
        client.post()
            .uri("/product-composite")
            .body(Mono.just(compositeProduct), ProductAggregate::class.java)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
    }

    private fun deleteAndVerifyProduct(productId: Int = 1, expectedStatus: HttpStatus = HttpStatus.OK) {
        client.delete()
            .uri("/product-composite/$productId")
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
    }
}
