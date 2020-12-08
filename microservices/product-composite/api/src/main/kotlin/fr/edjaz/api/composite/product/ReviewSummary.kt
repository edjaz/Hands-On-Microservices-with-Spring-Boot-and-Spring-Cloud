package fr.edjaz.api.composite.product

data class ReviewSummary (
  var reviewId: Int = 0,
  var author: String? = null,
  var subject: String? = null,
  var content: String? = null
)
