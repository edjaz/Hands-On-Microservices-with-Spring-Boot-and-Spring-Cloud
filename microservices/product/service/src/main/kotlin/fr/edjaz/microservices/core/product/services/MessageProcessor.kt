package fr.edjaz.microservices.core.product.services

import fr.edjaz.api.core.product.Product
import fr.edjaz.api.core.product.ProductService
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
    fun sink(productService: ProductService): Consumer<Event<Int, Product>> {
        return Consumer { event: Event<Int, Product> ->
            run {
                logger.info("Process message created at {}...", event.eventCreatedAt)
                when (event.eventType) {
                    Event.Type.CREATE -> {
                        val product = event.data
                        logger.info("Create product with ID: {}", product!!.productId)
                        productService.createProduct(product)
                    }
                    Event.Type.DELETE -> {
                        val productId = event.key!!
                        logger.info("Delete recommendations with ProductID: {}", productId)
                        productService.deleteProduct(productId)
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
