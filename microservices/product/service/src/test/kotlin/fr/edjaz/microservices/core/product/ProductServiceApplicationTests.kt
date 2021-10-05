package fr.edjaz.microservices.core.product

import fr.edjaz.api.core.product.Product
import fr.edjaz.api.event.Event
import fr.edjaz.microservices.core.product.persistence.ProductRepository
import fr.edjaz.util.exceptions.InvalidInputException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessagingException
import org.springframework.messaging.support.GenericMessage
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec

@ExtendWith(SpringExtension::class)
@Import(TestChannelBinderConfiguration::class)
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
        "kubernetes.manifests.enabled=false",
        "kubernetes.informer.enabled=false",
        "spring.cloud.stream.bindings.sink-in-0.destination=products",
        "spring.cloud.stream.bindings.sink-in-0.group=productsGroup",
        "spring.cloud.stream.bindings.sink-in-0.consumer.auto-bind-dlq=true"
    ]
)
class ProductServiceApplicationTests {
    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var repository: ProductRepository

    @Autowired
    @Qualifier("sink-in-0")
    private lateinit var input: MessageChannel

    @BeforeEach
    fun setupDb() {
        repository.deleteAll().block()
    }

    @Test
    fun productById() {
        val productId = 1
        Assertions.assertNull(repository.findByProductId(productId).block())
        Assertions.assertEquals(0, repository.count().block() as Long)
        sendCreateProductEvent(productId)
        Assertions.assertNotNull(repository.findByProductId(productId).block())
        Assertions.assertEquals(1, repository.count().block() as Long)
        getAndVerifyProduct(productId, HttpStatus.OK)
            .jsonPath("$.productId").isEqualTo(productId)
    }

    @Test
    fun duplicateError() {
        val productId = 1
        Assertions.assertNull(repository.findByProductId(productId).block())
        sendCreateProductEvent(productId)
        Assertions.assertNotNull(repository.findByProductId(productId).block())
        try {
            sendCreateProductEvent(productId)
            Assertions.fail<Any>("Expected a MessagingException here!")
        } catch (me: MessagingException) {
            if (me.cause is InvalidInputException) {
                val iie = me.cause as InvalidInputException
                Assertions.assertEquals("Duplicate key, Product Id: $productId", iie.message)
            } else {
                Assertions.fail<Any>("Expected a InvalidInputException as the root cause!")
            }
        }
    }

    @Test
    fun deleteProduct() {
        val productId = 1
        sendCreateProductEvent(productId)
        Assertions.assertNotNull(repository.findByProductId(productId).block())
        sendDeleteProductEvent(productId)
        Assertions.assertNull(repository.findByProductId(productId).block())
        sendDeleteProductEvent(productId)
    }

    @Test
    fun productInvalidParameterString() {
        getAndVerifyProduct("/no-integer", HttpStatus.BAD_REQUEST)
            .jsonPath("$.path").isEqualTo("/product/no-integer")
            .jsonPath("$.message").isEqualTo("Type mismatch.")
    }

    @Test
    fun productNotFound() {
        val productIdNotFound = 13
        getAndVerifyProduct(productIdNotFound, HttpStatus.NOT_FOUND)
            .jsonPath("$.path").isEqualTo("/product/$productIdNotFound")
            .jsonPath("$.message").isEqualTo("No product found for productId: $productIdNotFound")
    }

    @Test
    fun productInvalidParameterNegativeValue() {
        val productIdInvalid = -1
        getAndVerifyProduct(productIdInvalid, HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/product/$productIdInvalid")
            .jsonPath("$.message").isEqualTo("Invalid productId: $productIdInvalid")
    }

    private fun getAndVerifyProduct(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        return getAndVerifyProduct("/$productId", expectedStatus)
    }

    private fun getAndVerifyProduct(productIdPath: String, expectedStatus: HttpStatus): BodyContentSpec {
        return client.get()
            .uri("/product$productIdPath")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
    }

    private fun sendCreateProductEvent(productId: Int) {
        val product = Product(productId, "Name $productId", productId, "SA")
        val event: Event<Int, Product> = Event(Event.Type.CREATE, productId, product)
        input.send(GenericMessage(event))
    }

    private fun sendDeleteProductEvent(productId: Int) {
        val event: Event<Int, Product> = Event(Event.Type.DELETE, productId, null)
        input.send(GenericMessage(event))
    }
}
