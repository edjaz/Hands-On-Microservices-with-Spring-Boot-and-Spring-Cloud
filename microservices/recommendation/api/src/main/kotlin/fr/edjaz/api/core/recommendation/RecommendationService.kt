package fr.edjaz.api.core.recommendation

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import reactor.core.publisher.Flux

interface RecommendationService {
    fun createRecommendation(@RequestBody body: Recommendation?): Recommendation?

    /**
     * Sample usage:
     *
     * curl $HOST:$PORT/recommendation?productId=1
     *
     * @param productId
     * @return
     */
    @GetMapping(value = ["/recommendation"], produces = ["application/json"])
    fun getRecommendations(@RequestParam(value = "productId", required = true) productId: Int): Flux<Recommendation?>?
    fun deleteRecommendations(@RequestParam(value = "productId", required = true) productId: Int)
}
