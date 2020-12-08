package fr.edjaz.microservices.core.recommendation.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "recommendations")
@CompoundIndex(name = "prod-rec-id", unique = true, def = "{'productId': 1, 'recommendationId' : 1}")
class RecommendationEntity {
    @Id
    var id: String? = null

    @Version
    var version: Int? = null
    var productId = 0
    var recommendationId = 0
    var author: String? = null
    var rating = 0
    var content: String? = null

    constructor() {}
    constructor(productId: Int, recommendationId: Int, author: String?, rating: Int, content: String?) {
        this.productId = productId
        this.recommendationId = recommendationId
        this.author = author
        this.rating = rating
        this.content = content
    }

    override fun toString(): String {
        return String.format("RecommendationEntity: %s/%d", productId, recommendationId)
    }
}
