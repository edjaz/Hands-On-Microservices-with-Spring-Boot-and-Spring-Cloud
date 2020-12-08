package fr.edjaz.microservices.core.recommendation.persistence

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface RecommendationRepository : ReactiveCrudRepository<RecommendationEntity, String> {
    fun findByProductId(productId: Int): Flux<RecommendationEntity>
}
