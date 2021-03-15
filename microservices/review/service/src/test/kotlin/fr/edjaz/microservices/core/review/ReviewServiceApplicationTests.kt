package fr.edjaz.microservices.core.review

import fr.edjaz.api.core.product.Product
import fr.edjaz.api.core.review.Review
import fr.edjaz.api.event.Event
import fr.edjaz.microservices.core.review.persistence.ReviewRepository
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
  properties = ["eureka.client.enabled=false", "spring.cloud.config.enabled=false", "spring.datasource.url=jdbc:h2:mem:review-db", "spring.cloud.kubernetes.enabled= false"
    , "server.error.include-message=always" ,
    "server.error.include-binding-errors=always"
  ]
)
class ReviewServiceApplicationTests {
  @Autowired
  private val client: WebTestClient? = null

  @Autowired
  private val repository: ReviewRepository? = null

  @Autowired
  private val channels: Sink? = null
  private var input: AbstractMessageChannel? = null

  @BeforeEach
  fun setupDb() {
    input = channels!!.input() as AbstractMessageChannel
    repository!!.deleteAll()
  }

  @Test
  fun reviewsByProductId() {
    val productId = 1
    Assertions.assertEquals(0, repository!!.findByProductId(productId).size)
    sendCreateReviewEvent(productId, 1)
    sendCreateReviewEvent(productId, 2)
    sendCreateReviewEvent(productId, 3)
    Assertions.assertEquals(3, repository.findByProductId(productId).size)
    getAndVerifyReviewsByProductId(productId, HttpStatus.OK)
      .jsonPath("$.length()").isEqualTo(3)
      .jsonPath("$[2].productId").isEqualTo(productId)
      .jsonPath("$[2].reviewId").isEqualTo(3)
  }

  @Test
  fun duplicateError() {
    val productId = 1
    val reviewId = 1
    Assertions.assertEquals(0, repository!!.count())
    sendCreateReviewEvent(productId, reviewId)
    Assertions.assertEquals(1, repository.count())
    try {
      sendCreateReviewEvent(productId, reviewId)
      Assertions.fail<Any>("Expected a MessagingException here!")
    } catch (me: MessagingException) {
      if (me.cause is InvalidInputException) {
        val iie = me.cause as InvalidInputException?
        Assertions.assertEquals("Duplicate key, Product Id: 1, Review Id:1", iie!!.message)
      } else {
        Assertions.fail<Any>("Expected a InvalidInputException as the root cause!")
      }
    }
    Assertions.assertEquals(1, repository.count())
  }

  @Test
  fun deleteReviews() {
    val productId = 1
    val reviewId = 1
    sendCreateReviewEvent(productId, reviewId)
    Assertions.assertEquals(1, repository!!.findByProductId(productId).size)
    sendDeleteReviewEvent(productId)
    Assertions.assertEquals(0, repository.findByProductId(productId).size)
    sendDeleteReviewEvent(productId)
  }

  @Test
  fun reviewsMissingParameter(){
      getAndVerifyReviewsByProductId("", HttpStatus.BAD_REQUEST)
        .jsonPath("$.path").isEqualTo("/review")
        .jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present")
    }

  @Test
  fun reviewsInvalidParameter() {
      getAndVerifyReviewsByProductId("?productId=no-integer", HttpStatus.BAD_REQUEST)
        .jsonPath("$.path").isEqualTo("/review")
        .jsonPath("$.message").isEqualTo("Type mismatch.")
    }

  @Test
  fun reviewsNotFound() {
      getAndVerifyReviewsByProductId("?productId=213", HttpStatus.OK)
        .jsonPath("$.length()").isEqualTo(0)
    }

  @Test
  fun reviewsInvalidParameterNegativeValue() {
      val productIdInvalid = -1
      getAndVerifyReviewsByProductId("?productId=$productIdInvalid", HttpStatus.UNPROCESSABLE_ENTITY)
        .jsonPath("$.path").isEqualTo("/review")
        .jsonPath("$.message").isEqualTo("Invalid productId: $productIdInvalid")
    }

  private fun getAndVerifyReviewsByProductId(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
    return getAndVerifyReviewsByProductId("?productId=$productId", expectedStatus)
  }

  private fun getAndVerifyReviewsByProductId(productIdQuery: String, expectedStatus: HttpStatus): BodyContentSpec {
    return client!!.get()
      .uri("/review$productIdQuery")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(expectedStatus)
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
  }

  private fun sendCreateReviewEvent(productId: Int, reviewId: Int) {
    val review = Review(productId, reviewId, "Author $reviewId", "Subject $reviewId", "Content $reviewId", "SA")
    val event: Event<Int, Review> = Event(Event.Type.CREATE, productId, review)
    input!!.send(GenericMessage(event))
  }

  private fun sendDeleteReviewEvent(productId: Int) {
    val event: Event<Int, Product> = Event(Event.Type.DELETE, productId, null)
    input!!.send(GenericMessage(event))
  }
}
