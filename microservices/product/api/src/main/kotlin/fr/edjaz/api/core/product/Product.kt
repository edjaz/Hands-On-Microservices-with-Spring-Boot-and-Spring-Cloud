package fr.edjaz.api.core.product

data class  Product (
  var productId: Int = 0,
  var name: String? = null,
  var weight: Int = 0,
  var serviceAddress: String? = null
)
