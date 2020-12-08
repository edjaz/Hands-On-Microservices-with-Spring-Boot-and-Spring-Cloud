package fr.edjaz.microservices.core.product.services

import fr.edjaz.api.core.product.Product
import fr.edjaz.microservices.core.product.persistence.ProductEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings

@Mapper(componentModel = "spring")
interface ProductMapper {
    @Mappings(Mapping(target = "serviceAddress", ignore = true))
    fun entityToApi(entity: ProductEntity): Product

    @Mappings(Mapping(target = "id", ignore = true), Mapping(target = "version", ignore = true))
    fun apiToEntity(api: Product): ProductEntity
}
