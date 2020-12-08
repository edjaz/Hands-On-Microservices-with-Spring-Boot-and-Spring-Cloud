package fr.edjaz.api.core.recommendation



data class Recommendation(
  var productId: Int = 0,
  var recommendationId: Int = 0,
  var author: String? = null,
  var rate: Int = 0,
  var content: String? = null,
  var serviceAddress: String? = null
)
