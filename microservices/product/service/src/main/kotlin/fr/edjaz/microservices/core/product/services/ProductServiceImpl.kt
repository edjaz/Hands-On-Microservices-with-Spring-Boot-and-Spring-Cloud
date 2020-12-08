package fr.edjaz.microservices.core.product.services

import fr.edjaz.api.core.product.Product
import fr.edjaz.api.core.product.ProductService
import fr.edjaz.microservices.core.product.persistence.ProductEntity
import fr.edjaz.microservices.core.product.persistence.ProductRepository
import fr.edjaz.util.exceptions.InvalidInputException
import fr.edjaz.util.exceptions.NotFoundException
import fr.edjaz.util.http.ServiceUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.*

@RestController
class ProductServiceImpl @Autowired constructor(
  private val repository: ProductRepository,
  private val mapper: ProductMapper,
  private val serviceUtil: ServiceUtil
) : ProductService {
  val LOG: Logger = LoggerFactory.getLogger(ProductServiceImpl::class.java)

  override fun createProduct(body: Product): Product? {
    if (body.productId < 1) throw InvalidInputException("Invalid productId: " + body.productId)
    val entity = mapper.apiToEntity(body)
    val newEntity = repository.save(entity)
      .log()
      .onErrorMap(
        DuplicateKeyException::class.java
      ) { ex: DuplicateKeyException -> InvalidInputException("Duplicate key, Product Id: " + body.productId) }
      .map { e: ProductEntity -> mapper.entityToApi(e) }
    return newEntity.block()
  }

  override fun getProduct(productId: Int, delay: Int, faultPercent: Int): Mono<Product> {
    if (productId < 1) throw InvalidInputException("Invalid productId: $productId")
    if (delay > 0) simulateDelay(delay)
    if (faultPercent > 0) throwErrorIfBadLuck(faultPercent)
    return repository.findByProductId(productId)
      .switchIfEmpty(Mono.error(NotFoundException("No product found for productId: $productId")))
      .log()
      .map { e: ProductEntity -> mapper.entityToApi(e) }
      .map { e: Product? ->
        e!!.serviceAddress = serviceUtil.serviceAddress
        e
      }
  }

  override fun deleteProduct(productId: Int) {
    if (productId < 1) throw InvalidInputException("Invalid productId: $productId")
    LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId)
    repository.findByProductId(productId).log().map { e: ProductEntity -> repository.delete(e) }
      .flatMap { e: Mono<Void>? -> e }.block()
  }

  private fun simulateDelay(delay: Int) {
    LOG.debug("Sleeping for {} seconds...", delay)
    try {
      Thread.sleep((delay * 1000).toLong())
    } catch (e: InterruptedException) {
    }
    LOG.debug("Moving on...")
  }

  private fun throwErrorIfBadLuck(faultPercent: Int) {
    val randomThreshold = getRandomNumber(1, 100)
    if (faultPercent < randomThreshold) {
      LOG.debug("We got lucky, no error occurred, {} < {}", faultPercent, randomThreshold)
    } else {
      LOG.debug("Bad luck, an error occurred, {} >= {}", faultPercent, randomThreshold)
      throw RuntimeException("Something went wrong...")
    }
  }

  private val randomNumberGenerator = Random()
  private fun getRandomNumber(min: Int, max: Int): Int {
    if (max < min) {
      throw RuntimeException("Max must be greater than min")
    }
    return randomNumberGenerator.nextInt(max - min + 1) + min
  }
}
