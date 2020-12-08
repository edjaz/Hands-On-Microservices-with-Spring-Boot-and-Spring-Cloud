package fr.edjaz.microservices.core.product.services

import fr.edjaz.api.core.product.Product
import fr.edjaz.api.core.product.ProductService
import fr.edjaz.api.event.Event
import fr.edjaz.util.exceptions.EventProcessingException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink

@EnableBinding(Sink::class)
class MessageProcessor @Autowired constructor(private val productService: ProductService) {
  val LOG: Logger = LoggerFactory.getLogger(MessageProcessor::class.java)

    @StreamListener(target = Sink.INPUT)
    fun process(event: Event<Int, Product>) {
        LOG.info("Process message created at {}...", event.eventCreatedAt)
        when (event.eventType) {
            Event.Type.CREATE -> {
                val product = event.data
                LOG.info("Create product with ID: {}", product!!.productId)
                productService.createProduct(product)
            }
            Event.Type.DELETE -> {
                val productId = event.key!!
                LOG.info("Delete recommendations with ProductID: {}", productId)
                productService.deleteProduct(productId)
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
