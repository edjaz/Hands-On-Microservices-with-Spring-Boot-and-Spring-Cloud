package fr.edjaz.microservices.composite.product

import fr.edjaz.api.core.product.Product
import fr.edjaz.api.core.recommendation.Recommendation
import fr.edjaz.api.core.review.Review
import fr.edjaz.microservices.composite.product.services.ProductCompositeIntegration
import fr.edjaz.util.exceptions.InvalidInputException
import fr.edjaz.util.exceptions.NotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    classes = [ProductCompositeServiceApplication::class, TestSecurityConfig::class],
    properties = [
        "spring.main.allow-bean-definition-overriding=true",
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.cloud.kubernetes.enabled=false",
        "spring.cloud.kubernetes.discovery.enabled=false",
        "spring.cloud.kubernetes.loadbalancer.enabled=false",
        "kubernetes.manifests.enabled=false",
        "kubernetes.informer.enabled=false"
    ]
)
class ProductCompositeServiceApplicationTests {
    @Autowired
    private val client: WebTestClient? = null

    @MockBean
    private val compositeIntegration: ProductCompositeIntegration? = null

    @BeforeEach
    fun setUp() {
        Mockito.`when`(
            compositeIntegration!!.getProduct(
                ArgumentMatchers.eq(PRODUCT_ID_OK),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt()
            )
        ).thenReturn(
            Mono.just(
                Product(
                    PRODUCT_ID_OK,
                    "name",
                    1,
                    "mock-address"
                )
            )
        )
        Mockito.`when`(compositeIntegration.getRecommendations(PRODUCT_ID_OK)).thenReturn(
            Flux.fromIterable(
                listOf(
                    Recommendation(
                        PRODUCT_ID_OK,
                        1,
                        "author",
                        1,
                        "content",
                        "mock address"
                    )
                )
            )
        )
        Mockito.`when`(compositeIntegration.getReviews(PRODUCT_ID_OK)).thenReturn(
            Flux.fromIterable(
                listOf(
                    Review(
                        PRODUCT_ID_OK,
                        1,
                        "author",
                        "subject",
                        "content",
                        "mock address"
                    )
                )
            )
        )
        Mockito.`when`(
            compositeIntegration.getProduct(
                ArgumentMatchers.eq(PRODUCT_ID_NOT_FOUND),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt()
            )
        ).thenThrow(
            NotFoundException("NOT FOUND: $PRODUCT_ID_NOT_FOUND")
        )
        Mockito.`when`(
            compositeIntegration.getProduct(
                ArgumentMatchers.eq(PRODUCT_ID_INVALID),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt()
            )
        ).thenThrow(InvalidInputException("INVALID: $PRODUCT_ID_INVALID"))
    }

    @Test
    fun contextLoads() {
    }

    @Test
    fun productById() {
        getAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.OK)
            .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
            .jsonPath("$.recommendations.length()").isEqualTo(1)
            .jsonPath("$.reviews.length()").isEqualTo(1)
    }

    @Test
    fun productNotFound() {
        getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, HttpStatus.NOT_FOUND)
            .jsonPath("$.path").isEqualTo("/product-composite/$PRODUCT_ID_NOT_FOUND")
            .jsonPath("$.message").isEqualTo("NOT FOUND: $PRODUCT_ID_NOT_FOUND")
    }

    @Test
    fun productInvalidInput() {
        getAndVerifyProduct(PRODUCT_ID_INVALID, HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/product-composite/$PRODUCT_ID_INVALID")
            .jsonPath("$.message").isEqualTo("INVALID: $PRODUCT_ID_INVALID")
    }

    private fun getAndVerifyProduct(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        return client!!.get()
            .uri("/product-composite/$productId")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
    }

    companion object {
        private const val PRODUCT_ID_OK = 1
        private const val PRODUCT_ID_NOT_FOUND = 2
        private const val PRODUCT_ID_INVALID = 3
    }
}
