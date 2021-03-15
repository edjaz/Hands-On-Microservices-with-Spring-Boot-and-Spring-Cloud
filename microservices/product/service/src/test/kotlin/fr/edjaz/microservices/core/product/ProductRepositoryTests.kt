package fr.edjaz.microservices.core.product

import fr.edjaz.microservices.core.product.persistence.ProductEntity
import fr.edjaz.microservices.core.product.persistence.ProductRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.test.StepVerifier

@ExtendWith(SpringExtension::class)
@DataMongoTest(properties = ["spring.cloud.config.enabled=false", "spring.cloud.kubernetes.enabled= false", "spring.data.mongodb.auto-index-creation= true"])
class ProductRepositoryTests {
    @Autowired
    private lateinit var repository: ProductRepository
    private lateinit var savedEntity: ProductEntity

    @BeforeEach

    fun setupDb() {
        StepVerifier.create(repository.deleteAll()).verifyComplete()
        val entity = ProductEntity(1, "n", 1)
        StepVerifier.create(repository.save(entity))
            .expectNextMatches { createdEntity: ProductEntity ->
                savedEntity = createdEntity
                areProductEqual(entity, savedEntity)
            }
            .verifyComplete()
    }

    @Test
    fun create() {
        val newEntity = ProductEntity(2, "n", 2)
        StepVerifier.create(repository.save(newEntity))
            .expectNextMatches { createdEntity: ProductEntity -> newEntity.productId == createdEntity.productId }
            .verifyComplete()
        StepVerifier.create(repository.findById(newEntity.id!!))
            .expectNextMatches { foundEntity: ProductEntity? -> areProductEqual(newEntity, foundEntity) }
            .verifyComplete()
        StepVerifier.create(repository.count()).expectNext(2L).verifyComplete()
    }

    @Test
    fun update() {
        savedEntity.name = "n2"
        StepVerifier.create(repository.save<ProductEntity>(savedEntity))
            .expectNextMatches { updatedEntity: ProductEntity -> updatedEntity.name == "n2" }
            .verifyComplete()
        StepVerifier.create(repository.findById(savedEntity.id!!))
            .expectNextMatches { foundEntity: ProductEntity -> foundEntity.version == 1 && foundEntity.name == "n2" }
            .verifyComplete()
    }

    @Test
    fun delete() {
        StepVerifier.create(repository.delete(savedEntity)).verifyComplete()
        StepVerifier.create(repository.existsById(savedEntity.id!!)).expectNext(false).verifyComplete()
    }

    @Test
    fun byProductId() {
            StepVerifier.create(repository.findByProductId(savedEntity.productId))
                .expectNextMatches { foundEntity: ProductEntity? -> areProductEqual(savedEntity, foundEntity) }
                .verifyComplete()
        }

    @Test
    fun duplicateError() {
        val entity = ProductEntity(savedEntity.productId, "n", 1)
        StepVerifier.create(repository.save(entity)).expectError(DuplicateKeyException::class.java).verify()
    }

    @Test
    fun optimisticLockError() {

        // Store the saved entity in two separate entity objects
        val entity1 = repository.findById(savedEntity.id!!).block()!!
        val entity2 = repository.findById(savedEntity.id!!).block()!!

        // Update the entity using the first entity object
        entity1.name = "n1"
        repository.save(entity1).block()

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        StepVerifier.create(repository.save(entity2)).expectError(
            OptimisticLockingFailureException::class.java
        ).verify()

        // Get the updated entity from the database and verify its new sate
        StepVerifier.create(repository.findById(savedEntity.id!!))
            .expectNextMatches { foundEntity: ProductEntity -> foundEntity.version == 1 && foundEntity.name == "n1" }
            .verifyComplete()
    }

    private fun areProductEqual(expectedEntity: ProductEntity?, actualEntity: ProductEntity?): Boolean {
        return expectedEntity!!.id == actualEntity!!.id &&
                expectedEntity.version === actualEntity.version &&
                expectedEntity.productId == actualEntity.productId &&
                expectedEntity.name == actualEntity.name &&
                expectedEntity.weight == actualEntity.weight
    }
}
