package fr.edjaz.microservices.core.review.services

import fr.edjaz.api.core.review.Review
import fr.edjaz.api.core.review.ReviewService
import fr.edjaz.microservices.core.review.persistence.ReviewEntity
import fr.edjaz.microservices.core.review.persistence.ReviewRepository
import fr.edjaz.util.exceptions.InvalidInputException
import fr.edjaz.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.scheduler.Scheduler
import java.util.function.Consumer

@RestController
class ReviewServiceImpl @Autowired constructor(
    private val scheduler: Scheduler,
    private val repository: ReviewRepository,
    private val mapper: ReviewMapper,
    private val serviceUtil: ServiceUtil
) : ReviewService {

  val LOG = LoggerFactory.getLogger(ReviewService::class.java)

    override fun createReview(body: Review): Review {
        if (body.productId < 1) throw InvalidInputException("Invalid productId: " + body.productId)
        return try {
            val entity = mapper.apiToEntity(body)
            val newEntity = repository.save(entity)
            LOG.debug("createReview: created a review entity: {}/{}", body.productId, body.reviewId)
            mapper.entityToApi(newEntity)
        } catch (dive: DataIntegrityViolationException) {
            throw InvalidInputException("Duplicate key, Product Id: " + body.productId + ", Review Id:" + body.reviewId)
        }
    }

    override fun getReviews(productId: Int): Flux<Review> {
        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")
        return asyncFlux(getByProductId(productId)).log()
    }

    protected fun getByProductId(productId: Int): List<Review> {
        val entityList = repository.findByProductId(productId)
        val list = mapper.entityListToApiList(entityList)
        list.forEach(Consumer { e: Review -> e.serviceAddress = serviceUtil.serviceAddress })
        LOG.debug("getReviews: response size: {}", list.size)
        return list
    }

    override fun deleteReviews(productId: Int) {
        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")
        LOG.debug(
            "deleteReviews: tries to delete reviews for the product with productId: {}",
            productId
        )
        repository.deleteAll(repository.findByProductId(productId))
    }

    private fun <T> asyncFlux(iterable: Iterable<T>): Flux<T> {
        return Flux.fromIterable(iterable).publishOn(scheduler)
    }
}
