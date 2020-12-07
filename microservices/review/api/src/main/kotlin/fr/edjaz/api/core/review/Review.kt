package fr.edjaz.api.core.review

data class Review(
  var productId: Int = 0,
  var reviewId: Int = 0,
  var author: String? = null,
  var subject: String? = null,
  var content: String? = null,
  var serviceAddress: String? = null
)
