package fr.edjaz.microservices.core.recommendation


import fr.edjaz.microservices.core.recommendation.persistence.RecommendationEntity
import fr.edjaz.microservices.core.recommendation.persistence.RecommendationRepository
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@DataMongoTest(properties = ["spring.cloud.config.enabled=false", "spring.cloud.kubernetes.enabled= false"])
class RecommendationRepositoryTests {
    @Autowired
    private  lateinit var repository: RecommendationRepository
    private lateinit var savedEntity: RecommendationEntity
    @BeforeEach
    fun setupDb() {
        repository.deleteAll().block()
        val entity = RecommendationEntity(1, 2, "a", 3, "c")
        savedEntity = repository.save(entity).block()!!
        assertEqualsRecommendation(entity, savedEntity)
    }

    @Test
    fun create() {
        val newEntity = RecommendationEntity(1, 3, "a", 3, "c")
        repository.save(newEntity).block()
        val foundEntity = repository.findById(newEntity.id!!).block()
        assertEqualsRecommendation(newEntity, foundEntity)
        Assertions.assertEquals(2, repository.count().block() as Long)
    }

    @Test
    fun update() {
        savedEntity.author = "a2"
        repository.save(savedEntity).block()
        val foundEntity = repository.findById(savedEntity.id!!).block()
        Assertions.assertEquals(1, foundEntity!!.version as Int)
        Assertions.assertEquals("a2", foundEntity.author)
    }

    @Test
    fun delete() {
        repository.delete(savedEntity).block()
        Assertions.assertFalse(repository.existsById(savedEntity.id!!).block()!!)
    }

    @Test
    fun byProductId() {
            val entityList: List<RecommendationEntity> =
                repository.findByProductId(savedEntity.productId).collectList().block()!!
            MatcherAssert.assertThat(entityList, Matchers.hasSize(1))
            assertEqualsRecommendation(savedEntity, entityList[0])
        }

    @Test
    fun duplicateError() {
        val entity = RecommendationEntity(1, 2, "a", 3, "c")
        Assertions.assertThrows(DuplicateKeyException::class.java) { repository.save(entity).block() }
    }

    @Test
    fun optimisticLockError() {

        // Store the saved entity in two separate entity objects
        val entity1 = repository.findById(savedEntity.id!!).block()
        val entity2 = repository.findById(savedEntity.id!!).block()

        // Update the entity using the first entity object
        entity1!!.author = "a1"
        repository.save(entity1).block()

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            entity2!!.author = "a2"
            repository.save(entity2).block()
            Assertions.fail<Any>("Expected an OptimisticLockingFailureException")
        } catch (e: OptimisticLockingFailureException) {
        }

        // Get the updated entity from the database and verify its new sate
        val updatedEntity = repository.findById(savedEntity.id!!).block()
        Assertions.assertEquals(1, updatedEntity!!.version as Int)
        Assertions.assertEquals("a1", updatedEntity.author)
    }

    private fun assertEqualsRecommendation(expectedEntity: RecommendationEntity?, actualEntity: RecommendationEntity?) {
        Assertions.assertEquals(expectedEntity!!.id, actualEntity!!.id)
        Assertions.assertEquals(expectedEntity.version, actualEntity.version)
        Assertions.assertEquals(expectedEntity.productId, actualEntity.productId)
        Assertions.assertEquals(expectedEntity.recommendationId, actualEntity.recommendationId)
        Assertions.assertEquals(expectedEntity.author, actualEntity.author)
        Assertions.assertEquals(expectedEntity.rating, actualEntity.rating)
        Assertions.assertEquals(expectedEntity.content, actualEntity.content)
    }
}
