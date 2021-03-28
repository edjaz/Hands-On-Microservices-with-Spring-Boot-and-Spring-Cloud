package fr.edjaz.microservices.composite.product.services

import fr.edjaz.api.core.product.Product
import fr.edjaz.api.core.recommendation.Recommendation
import fr.edjaz.api.core.review.Review
import fr.edjaz.api.event.Event
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.stereotype.Service

@Service
class MessageSources @Autowired constructor(val streamBridge: StreamBridge) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    fun outputProducts(event: Event<*, Product>) {
        logger.debug("send event {}", event)
        streamBridge.send("output-products-out-0", event);
    }

    fun outputRecommendations(event: Event<*, Recommendation>) {
        logger.debug("send event {}", event)
        streamBridge.send("output-recommendations-out-0", event);
    }

    fun outputReviews(event: Event<*, Review>) {
        logger.debug("send event {}", event)
        streamBridge.send("output-reviews-out-0", event);
    }
}
