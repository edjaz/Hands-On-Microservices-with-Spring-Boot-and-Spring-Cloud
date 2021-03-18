package fr.edjaz.microservices.core.review.services

import fr.edjaz.api.core.review.Review
import fr.edjaz.api.core.review.ReviewService
import fr.edjaz.api.event.Event
import fr.edjaz.util.exceptions.EventProcessingException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink

@EnableBinding(Sink::class)
class MessageProcessor @Autowired constructor(private val reviewService: ReviewService) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    @StreamListener(target = Sink.INPUT)
    fun process(event: Event<Int?, Review?>) {
        logger.info("Process message created at {}...", event.eventCreatedAt)
        when (event.eventType) {
            Event.Type.CREATE -> {
                val review = event.data
                logger.info("Create review with ID: {}/{}", review!!.productId, review.reviewId)
                reviewService.createReview(review)
            }
            Event.Type.DELETE -> {
                val productId = event.key!!
                logger.info("Delete reviews with ProductID: {}", productId)
                reviewService.deleteReviews(productId)
            }
            else -> {
                val errorMessage = "Incorrect event type: " + event.eventType + ", expected a CREATE or DELETE event"
                logger.warn(errorMessage)
                throw EventProcessingException(errorMessage)
            }
        }
        logger.info("Message processing done!")
    }
}
