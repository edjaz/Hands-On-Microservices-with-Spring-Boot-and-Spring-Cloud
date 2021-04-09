package fr.edjaz.microservices.core.recommendation.services

import fr.edjaz.api.core.recommendation.Recommendation
import fr.edjaz.api.core.recommendation.RecommendationService
import fr.edjaz.microservices.core.recommendation.persistence.RecommendationEntity
import fr.edjaz.microservices.core.recommendation.persistence.RecommendationRepository
import fr.edjaz.util.exceptions.InvalidInputException
import fr.edjaz.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class RecommendationServiceImpl @Autowired constructor(
    private val repository: RecommendationRepository,
    private val mapper: RecommendationMapper,
    private val serviceUtil: ServiceUtil
) : RecommendationService {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    override fun createRecommendation(body: Recommendation): Recommendation? {
        if (body.productId < 1) throw InvalidInputException("Invalid productId: " + body.productId)
        val entity = mapper.apiToEntity(body)
        val newEntity = repository.save(entity)
            .log()
            .onErrorMap(
                DuplicateKeyException::class.java
            ) { InvalidInputException("Duplicate key, Product Id: " + body.productId + ", Recommendation Id:" + body.recommendationId) }
            .map { e: RecommendationEntity -> mapper.entityToApi(e) }
        return newEntity.block()
    }

    override fun getRecommendations(productId: Int): Flux<Recommendation?>? {
        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")
        return repository.findByProductId(productId)
            .log()
            .map { e: RecommendationEntity -> mapper.entityToApi(e) }
            .map { e: Recommendation? ->
                e!!.serviceAddress = serviceUtil.serviceAddress
                e
            }
    }

    override fun deleteRecommendations(productId: Int) {
        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")
        logger.debug(
            "deleteRecommendations: tries to delete recommendations for the product with productId: {}",
            productId
        )
        repository.deleteAll(repository.findByProductId(productId)).block()
    }
}
