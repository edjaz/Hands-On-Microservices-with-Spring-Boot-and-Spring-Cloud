package fr.edjaz.api.composite.product

data class ProductAggregate (
  var productId: Int = 0,
  var name: String? = null,
  var weight: Int = 0,
  var recommendations: List<RecommendationSummary>? = null,
  var reviews: List<ReviewSummary>? = null,
  var serviceAddresses: ServiceAddresses? = null
)
