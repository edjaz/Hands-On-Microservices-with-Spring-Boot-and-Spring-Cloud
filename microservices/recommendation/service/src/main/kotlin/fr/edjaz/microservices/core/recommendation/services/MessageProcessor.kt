package fr.edjaz.microservices.core.recommendation.services

import fr.edjaz.api.core.recommendation.Recommendation
import fr.edjaz.api.core.recommendation.RecommendationService
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
    fun sink(recommendationService: RecommendationService): Consumer<Event<Int, Recommendation>> {
        return Consumer { event: Event<Int, Recommendation> ->
            run {
                logger.info("Process message created at {}...", event.eventCreatedAt)
                when (event.eventType) {
                    Event.Type.CREATE -> {
                        val recommendation = event.data
                        logger.info(
                            "Create recommendation with ID: {}/{}",
                            recommendation!!.productId,
                            recommendation.recommendationId
                        )
                        recommendationService.createRecommendation(recommendation)
                    }
                    Event.Type.DELETE -> {
                        val productId = event.key!!
                        logger.info("Delete recommendations with ProductID: {}", productId)
                        recommendationService.deleteRecommendations(productId)
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
}
