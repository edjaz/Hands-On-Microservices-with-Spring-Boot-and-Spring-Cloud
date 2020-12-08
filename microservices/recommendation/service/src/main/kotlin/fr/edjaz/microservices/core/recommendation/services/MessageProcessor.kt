package fr.edjaz.microservices.core.recommendation.services

import fr.edjaz.api.core.recommendation.Recommendation
import fr.edjaz.api.core.recommendation.RecommendationService
import fr.edjaz.api.event.Event
import fr.edjaz.util.exceptions.EventProcessingException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink

@EnableBinding(Sink::class)
class MessageProcessor @Autowired constructor(private val recommendationService: RecommendationService) {

  companion object {
    @Suppress("JAVA_CLASS_ON_COMPANION")
    @JvmStatic
    private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
  }


  @StreamListener(target = Sink.INPUT)
    fun process(event: Event<Int?, Recommendation?>) {
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
