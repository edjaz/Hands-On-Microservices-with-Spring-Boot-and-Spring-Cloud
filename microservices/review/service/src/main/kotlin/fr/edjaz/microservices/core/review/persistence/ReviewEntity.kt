package fr.edjaz.microservices.core.review.persistence


import javax.persistence.*

@Entity
@Table(
    name = "reviews",
    indexes = [Index(name = "reviews_unique_idx", unique = true, columnList = "productId,reviewId")]
)
class ReviewEntity {
    @Id
    @GeneratedValue
    var id = 0

    @Version
    var version = 0
    var productId = 0
    var reviewId = 0
    var author: String? = null
    var subject: String? = null
    var content: String? = null

    constructor() {}
    constructor(productId: Int, reviewId: Int, author: String?, subject: String?, content: String?) {
        this.productId = productId
        this.reviewId = reviewId
        this.author = author
        this.subject = subject
        this.content = content
    }

    override fun toString(): String {
        return String.format("ReviewEntity: %s/%d", productId, reviewId)
    }
}
