package fr.edjaz.api.composite.product

data class RecommendationSummary (
  var recommendationId: Int = 0,
  var author: String? = null,
  var rate: Int = 0,
  var content: String? = null
)
