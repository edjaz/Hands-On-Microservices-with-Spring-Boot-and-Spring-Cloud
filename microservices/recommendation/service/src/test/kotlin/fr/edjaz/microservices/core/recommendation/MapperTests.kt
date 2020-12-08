package fr.edjaz.microservices.core.recommendation

import fr.edjaz.api.core.recommendation.Recommendation
import fr.edjaz.microservices.core.recommendation.services.RecommendationMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mapstruct.factory.Mappers

class MapperTests {
    private val mapper = Mappers.getMapper(RecommendationMapper::class.java)
    @Test
    fun mapperTests() {
        Assertions.assertNotNull(mapper)
        val api = Recommendation(1, 2, "a", 4, "C", "adr")
        val entity = mapper.apiToEntity(api)
        Assertions.assertEquals(api.productId, entity.productId)
        Assertions.assertEquals(api.recommendationId, entity.recommendationId)
        Assertions.assertEquals(api.author, entity.author)
        Assertions.assertEquals(api.rate, entity.rating)
        Assertions.assertEquals(api.content, entity.content)
        val api2 = mapper.entityToApi(entity)
        Assertions.assertEquals(api.productId, api2.productId)
        Assertions.assertEquals(api.recommendationId, api2.recommendationId)
        Assertions.assertEquals(api.author, api2.author)
        Assertions.assertEquals(api.rate, api2.rate)
        Assertions.assertEquals(api.content, api2.content)
        Assertions.assertNull(api2.serviceAddress)
    }

    @Test
    fun mapperListTests() {
        Assertions.assertNotNull(mapper)
        val api = Recommendation(1, 2, "a", 4, "C", "adr")
        val apiList: List<Recommendation> = listOf(api)
        val entityList = mapper.apiListToEntityList(apiList)
        Assertions.assertEquals(apiList.size, entityList.size)
        val entity = entityList[0]
        Assertions.assertEquals(api.productId, entity.productId)
        Assertions.assertEquals(api.recommendationId, entity.recommendationId)
        Assertions.assertEquals(api.author, entity.author)
        Assertions.assertEquals(api.rate, entity.rating)
        Assertions.assertEquals(api.content, entity.content)
        val api2List = mapper.entityListToApiList(entityList)
        Assertions.assertEquals(apiList.size, api2List.size)
        val api2 = api2List[0]
        Assertions.assertEquals(api.productId, api2.productId)
        Assertions.assertEquals(api.recommendationId, api2.recommendationId)
        Assertions.assertEquals(api.author, api2.author)
        Assertions.assertEquals(api.rate, api2.rate)
        Assertions.assertEquals(api.content, api2.content)
        Assertions.assertNull(api2.serviceAddress)
    }
}
