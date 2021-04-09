package fr.edjaz.microservices.composite.product.client

import fr.edjaz.api.core.product.Product
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import reactivefeign.spring.config.ReactiveFeignClient
import reactor.core.publisher.Mono

@ReactiveFeignClient(name = "product", url = "\${reactive.feign.client.config.product.url}")
interface ProductClient {
    @GetMapping(value = ["/product/{productId}"], produces = ["application/json"])
    fun getProduct(
        @PathVariable("productId") productId: Int,
        @RequestParam(value = "delay", required = false, defaultValue = "0") delay: Int,
        @RequestParam(value = "faultPercent", required = false, defaultValue = "0") faultPercent: Int
    ): Mono<Product>
}
