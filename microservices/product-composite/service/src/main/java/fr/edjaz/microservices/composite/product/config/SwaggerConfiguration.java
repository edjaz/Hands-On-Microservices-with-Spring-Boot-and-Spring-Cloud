package fr.edjaz.microservices.composite.product.config;

import fr.edjaz.microservices.composite.product.config.properties.ServicesProperties;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

  /**
   * Will exposed on $HOST:$PORT/swagger-ui.html
   *
   * @return
   */
  @Bean
  public OpenAPI springShopOpenAPI(ServicesProperties.SwaggerProperties swaggerProperties) {
    return swaggerProperties.toSwagger();
  }


  @Bean
  public GroupedOpenApi apiDocumentation(ServicesProperties.SwaggerProperties swaggerProperties) {
    return GroupedOpenApi.builder()
      .group("product-composite")
      .pathsToMatch("/**")
      .packagesToScan("fr.edjaz.microservices.composite.product")
      .build();
  }
}
