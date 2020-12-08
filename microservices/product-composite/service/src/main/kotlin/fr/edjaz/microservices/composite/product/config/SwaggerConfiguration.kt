package fr.edjaz.microservices.composite.product.config

import fr.edjaz.microservices.composite.product.config.properties.ServicesProperties.SwaggerProperties
import io.swagger.v3.oas.models.OpenAPI
import org.springdoc.core.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfiguration {
    /**
     * Will exposed on $HOST:$PORT/swagger-ui.html
     *
     * @return
     */
    @Bean
    fun springShopOpenAPI(swaggerProperties: SwaggerProperties): OpenAPI {
        return swaggerProperties.toSwagger()
    }

    @Bean
    fun apiDocumentation(swaggerProperties: SwaggerProperties?): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("product-composite")
            .pathsToMatch("/**")
            .packagesToScan("fr.edjaz.microservices.composite.product")
            .build()
    }
}
