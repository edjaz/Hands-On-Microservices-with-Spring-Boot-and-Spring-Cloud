package fr.edjaz.microservices.composite.product.config.properties

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
class ServicesProperties {
  @ConfigurationProperties(prefix = "app.product-service")
  data class ProductServiceProperties (
    var host: String? = null,
    var port: Int? = null
  )


  @ConfigurationProperties(prefix = "app.recommendation-service")
  data class RecommendationServiceProperties (
    var host: String? = null,
    var port: Int? = null
  )


  @ConfigurationProperties(prefix = "app.review-service")
  data class ReviewServiceProperties (
    var host: String? = null,
    var port: Int? = null
  )

  data class Contact (
    var name: String? = null,
    var url: String? = null,
    var email: String? = null
  )

  data class ApiInfo (
    var version: String? = null,
    var title: String? = null,
    var description: String? = null,
    var termsOfServiceUrl: String? = null,
    var license: String? = null,
    var licenseUrl: String? = null,
    var contact: Contact? = null
  )

  @ConfigurationProperties(prefix = "api")
  data class SwaggerProperties(var common: ApiInfo? = null) {

    fun toSwagger(): OpenAPI {
      return OpenAPI()
        .components(
          Components()
            .addSecuritySchemes(
              "bearer-key",
              SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
            )
        )
        .info(
          Info().title(common!!.title).version(common!!.version).description(
            common!!.description
          ).termsOfService(common!!.termsOfServiceUrl).license(
            License().name(
              common!!.license
            ).url(common!!.licenseUrl)
          )
        )
    }
  }
}
