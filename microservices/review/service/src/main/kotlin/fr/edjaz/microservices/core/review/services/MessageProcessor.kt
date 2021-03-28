package fr.edjaz.microservices.core.review.services

import fr.edjaz.api.core.review.Review
import fr.edjaz.api.core.review.ReviewService
import fr.edjaz.api.event.Event
import fr.edjaz.util.exceptions.EventProcessingException
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

@Configuration
class MessageProcessor {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    @Bean
    fun sink(reviewService: ReviewService): Consumer<Event<Int, Review>> {
        return Consumer { event: Event<Int, Review> ->
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
}

