package fr.edjaz.microservices.core.product.persistence

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface ProductRepository : ReactiveCrudRepository<ProductEntity, String> {
    fun findByProductId(productId: Int): Mono<ProductEntity>
}
