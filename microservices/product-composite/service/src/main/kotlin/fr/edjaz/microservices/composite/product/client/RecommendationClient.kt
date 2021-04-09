package fr.edjaz.microservices.composite.product.client

import fr.edjaz.api.core.recommendation.Recommendation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import reactivefeign.spring.config.ReactiveFeignClient
import reactor.core.publisher.Flux

@ReactiveFeignClient(name = "recommendation", url = "\${reactive.feign.client.config.recommendation.url}")
interface RecommendationClient {
    @GetMapping(value = ["/recommendation"], produces = ["application/json"])
    fun getRecommendations(@RequestParam(value = "productId", required = true) productId: Int): Flux<Recommendation>
}
