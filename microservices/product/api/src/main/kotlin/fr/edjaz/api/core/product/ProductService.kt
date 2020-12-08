package fr.edjaz.api.core.product

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import reactor.core.publisher.Mono

interface ProductService {
    fun createProduct(@RequestBody body: Product): Product?

    /**
     * Sample usage: curl $HOST:$PORT/product/1
     *
     * @param productId
     * @return the product, if found, else null
     */
    @GetMapping(value = ["/product/{productId}"], produces = ["application/json"])
    fun getProduct(
        @PathVariable productId: Int,
        @RequestParam(value = "delay", required = false, defaultValue = "0") delay: Int,
        @RequestParam(value = "faultPercent", required = false, defaultValue = "0") faultPercent: Int
    ): Mono<Product>

    fun deleteProduct(@PathVariable productId: Int)
}
