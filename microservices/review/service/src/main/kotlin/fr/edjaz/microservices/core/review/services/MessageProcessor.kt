package fr.edjaz.microservices.core.review.services

import fr.edjaz.api.core.review.Review
import fr.edjaz.api.core.review.ReviewService
import fr.edjaz.api.event.Event
import fr.edjaz.microservices.core.review.ReviewServiceApplication
import fr.edjaz.util.exceptions.EventProcessingException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink

@EnableBinding(Sink::class)
class MessageProcessor @Autowired constructor(private val reviewService: ReviewService) {
  val LOG = LoggerFactory.getLogger(ReviewServiceApplication::class.java)

    @StreamListener(target = Sink.INPUT)
    fun process(event: Event<Int?, Review?>) {
        LOG.info("Process message created at {}...", event.eventCreatedAt)
        when (event.eventType) {
            Event.Type.CREATE -> {
                val review = event.data
                LOG.info("Create review with ID: {}/{}", review!!.productId, review.reviewId)
                reviewService.createReview(review)
            }
            Event.Type.DELETE -> {
                val productId = event.key!!
                LOG.info("Delete reviews with ProductID: {}", productId)
                reviewService.deleteReviews(productId)
            }
            else -> {
                val errorMessage = "Incorrect event type: " + event.eventType + ", expected a CREATE or DELETE event"
                LOG.warn(errorMessage)
                throw EventProcessingException(errorMessage)
            }
        }
        LOG.info("Message processing done!")
    }
}
