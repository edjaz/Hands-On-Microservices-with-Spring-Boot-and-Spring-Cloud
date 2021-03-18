package fr.edjaz.microservices.core.review

import fr.edjaz.api.core.review.Review
import fr.edjaz.microservices.core.review.services.ReviewMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mapstruct.factory.Mappers

class MapperTests {
    private val mapper = Mappers.getMapper(ReviewMapper::class.java)

    @Test
    fun mapperTests() {
        Assertions.assertNotNull(mapper)
        val api = Review(1, 2, "a", "s", "C", "adr")
        val entity = mapper.apiToEntity(api)
        Assertions.assertEquals(api.productId, entity.productId)
        Assertions.assertEquals(api.reviewId, entity.reviewId)
        Assertions.assertEquals(api.author, entity.author)
        Assertions.assertEquals(api.subject, entity.subject)
        Assertions.assertEquals(api.content, entity.content)
        val api2 = mapper.entityToApi(entity)
        Assertions.assertEquals(api.productId, api2.productId)
        Assertions.assertEquals(api.reviewId, api2.reviewId)
        Assertions.assertEquals(api.author, api2.author)
        Assertions.assertEquals(api.subject, api2.subject)
        Assertions.assertEquals(api.content, api2.content)
        Assertions.assertNull(api2.serviceAddress)
    }

    @Test
    fun mapperListTests() {
        Assertions.assertNotNull(mapper)
        val api = Review(1, 2, "a", "s", "C", "adr")
        val apiList: List<Review> = listOf(api)
        val entityList = mapper.apiListToEntityList(apiList)
        Assertions.assertEquals(apiList.size, entityList.size)
        val entity = entityList[0]
        Assertions.assertEquals(api.productId, entity.productId)
        Assertions.assertEquals(api.reviewId, entity.reviewId)
        Assertions.assertEquals(api.author, entity.author)
        Assertions.assertEquals(api.subject, entity.subject)
        Assertions.assertEquals(api.content, entity.content)
        val api2List = mapper.entityListToApiList(entityList)
        Assertions.assertEquals(apiList.size, api2List.size)
        val api2 = api2List[0]
        Assertions.assertEquals(api.productId, api2.productId)
        Assertions.assertEquals(api.reviewId, api2.reviewId)
        Assertions.assertEquals(api.author, api2.author)
        Assertions.assertEquals(api.subject, api2.subject)
        Assertions.assertEquals(api.content, api2.content)
        Assertions.assertNull(api2.serviceAddress)
    }
}
