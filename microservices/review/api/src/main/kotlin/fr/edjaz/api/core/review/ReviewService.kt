package fr.edjaz.api.core.review

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import reactor.core.publisher.Flux

interface ReviewService {
  fun createReview(@RequestBody body: Review): Review

  /**
   * Sample usage: curl $HOST:$PORT/review?productId=1
   *
   * @param productId
   * @return
   */
  @GetMapping(value = ["/review"], produces = ["application/json"])
  fun getReviews(@RequestParam(value = "productId", required = true) productId: Int): Flux<Review>
  fun deleteReviews(@RequestParam(value = "productId", required = true) productId: Int)
}
