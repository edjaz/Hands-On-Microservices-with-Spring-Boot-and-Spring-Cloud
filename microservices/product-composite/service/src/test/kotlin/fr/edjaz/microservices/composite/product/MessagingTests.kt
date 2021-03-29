package fr.edjaz.microservices.composite.product

import fr.edjaz.api.composite.product.ProductAggregate
import fr.edjaz.api.composite.product.RecommendationSummary
import fr.edjaz.api.composite.product.ReviewSummary
import fr.edjaz.api.core.product.Product
import fr.edjaz.api.core.recommendation.Recommendation
import fr.edjaz.api.core.review.Review
import fr.edjaz.api.event.Event
import fr.edjaz.microservices.composite.product.services.MessageSources
import fr.edjaz.util.http.ServiceUtil
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.cloud.stream.binder.test.OutputDestination
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@ExtendWith(SpringExtension::class)
@Import(TestChannelBinderConfiguration::class)
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    classes = [ProductCompositeServiceApplication::class, TestSecurityConfig::class, MessageSources::class],
    properties = [
        "spring.main.allow-bean-definition-overriding=true",
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.cloud.kubernetes.enabled= false",
        "spring.cloud.kubernetes.discovery.enabled=false",
        "spring.cloud.kubernetes.loadbalancer.enabled=false",
        "kubernetes.manifests.enabled=false",
        "kubernetes.informer.enabled=false",
        "spring.cloud.stream.bindings.output-products-out-0.destination= products",
        "spring.cloud.stream.bindings.output-recommendations-out-0.destination= recommendations",
        "spring.cloud.stream.bindings.output-reviews-out-0.destination= reviews"
    ]
)
class MessagingTests {
    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var output: OutputDestination

    @Autowired
    private lateinit var serviceUtil: ServiceUtil


    @Test
    fun createCompositeProduct1() {
        val composite = ProductAggregate(1, "name", 1, null, null, null)
        postAndVerifyProduct(composite, HttpStatus.OK)

        val expectedEvent: Event<Int, Product> = Event(
            Event.Type.CREATE,
            composite.productId,
            Product(composite.productId, composite.name, composite.weight, serviceUtil.serviceAddress)
        )

        val payload = output.receive(1, "products").payload
        MatcherAssert.assertThat(
            String(payload),
            IsSameEvent.sameEventExceptCreatedAt(expectedEvent)
        )
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

        val expectedProductEvent: Event<Int, Product> = Event(
            Event.Type.CREATE,
            composite.productId,
            Product(composite.productId, composite.name, composite.weight, serviceUtil.serviceAddress)
        )

        val payloadProduct = output.receive(1, "products").payload
        MatcherAssert.assertThat(
            String(payloadProduct),
            IsSameEvent.sameEventExceptCreatedAt(expectedProductEvent)
        )

        val (recommendationId, author, rate, content) = composite.recommendations!![0]
        val expectedRecommendationEvent: Event<Int, Recommendation> = Event(
            Event.Type.CREATE,
            composite.productId,
            Recommendation(composite.productId, recommendationId, author, rate, content, serviceUtil.serviceAddress)
        )
        val payloadRecommendation = output.receive(1, "recommendations").payload
        MatcherAssert.assertThat(
            String(payloadRecommendation),
            IsSameEvent.sameEventExceptCreatedAt(expectedRecommendationEvent)
        )

        val (reviewId, author1, subject, content1) = composite.reviews!![0]
        val expectedReviewEvent: Event<Int, Review> = Event(
            Event.Type.CREATE,
            composite.productId,
            Review(composite.productId, reviewId, author1, subject, content1, serviceUtil.serviceAddress)
        )
        val payloadReviews = output.receive(1, "reviews").payload
        MatcherAssert.assertThat(
            String(payloadReviews),
            IsSameEvent.sameEventExceptCreatedAt(expectedReviewEvent)
        )
    }

    @Test
    fun deleteCompositeProduct() {
        deleteAndVerifyProduct(1, HttpStatus.OK)

        // Assert one delete product event queued up
        val expectedEvent: Event<Int, Product> = Event(Event.Type.DELETE, 1, null)
        val payloadProduct = output.receive(1, "products").payload
        MatcherAssert.assertThat(
            String(payloadProduct),
            IsSameEvent.sameEventExceptCreatedAt(expectedEvent)
        )

        val expectedRecommendationEvent: Event<Int, Recommendation> = Event(Event.Type.DELETE, 1, null)
        val payloadRecommendation = output.receive(1, "recommendations").payload
        MatcherAssert.assertThat(
            String(payloadRecommendation),
            IsSameEvent.sameEventExceptCreatedAt(expectedRecommendationEvent)
        )

        val expectedReviewEvent: Event<Int, Review> = Event(Event.Type.DELETE, 1, null)
        val payloadReviews = output.receive(1, "reviews").payload
        MatcherAssert.assertThat(
            String(payloadReviews),
            IsSameEvent.sameEventExceptCreatedAt(expectedReviewEvent)
        )
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
