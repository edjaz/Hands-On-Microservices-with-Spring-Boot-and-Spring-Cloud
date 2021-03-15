package fr.edjaz.microservices.core.product.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "products")
class ProductEntity {
    @Id
    var id: String? = null

    @Version
    var version: Int? = null

    @Indexed(name = "productId_index_unique", unique = true)
    var productId = 0
    var name: String? = null
    var weight = 0

    constructor() {}
    constructor(productId: Int, name: String?, weight: Int) {
        this.productId = productId
        this.name = name
        this.weight = weight
    }

    override fun toString(): String {
        return String.format("ProductEntity: %s", productId)
    }
}
