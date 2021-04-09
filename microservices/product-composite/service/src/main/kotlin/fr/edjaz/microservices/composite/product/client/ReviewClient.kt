package fr.edjaz.microservices.composite.product.client

import fr.edjaz.api.core.review.Review
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import reactivefeign.spring.config.ReactiveFeignClient
import reactor.core.publisher.Flux

@ReactiveFeignClient(name = "review", url = "\${reactive.feign.client.config.review.url}")
interface ReviewClient {
    @GetMapping(value = ["/review"], produces = ["application/json"])
    fun getReviews(@RequestParam(value = "productId", required = true) productId: Int): Flux<Review>
}
