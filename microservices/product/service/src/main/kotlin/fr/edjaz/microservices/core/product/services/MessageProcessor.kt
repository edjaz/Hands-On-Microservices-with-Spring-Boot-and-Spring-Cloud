package fr.edjaz.microservices.core.product.services

import fr.edjaz.api.core.product.Product
import fr.edjaz.api.core.product.ProductService
import fr.edjaz.api.event.Event
import fr.edjaz.util.exceptions.EventProcessingException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink

@EnableBinding(Sink::class)
class MessageProcessor @Autowired constructor(private val productService: ProductService) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    @StreamListener(target = Sink.INPUT)
    fun process(event: Event<Int, Product>) {
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
