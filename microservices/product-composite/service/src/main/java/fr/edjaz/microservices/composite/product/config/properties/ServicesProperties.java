package fr.edjaz.microservices.composite.product.config.properties;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServicesProperties {

    @Data
    @ConfigurationProperties(prefix = "app.product-service")
    public class ProductServiceProperties {
        private String host;
        private Integer port;
    }

    @Data
    @ConfigurationProperties(prefix = "app.recommendation-service")
    public class RecommendationServiceProperties {
        private String host;
        private Integer port;
    }

    @Data
    @ConfigurationProperties(prefix = "app.review-service")
    public class ReviewServiceProperties {
        private String host;
        private Integer port;
    }


    @Getter
    @Setter
    @RequiredArgsConstructor
    static class Contact {
        private String name;
        private String url;
        private String email;
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    static public class ApiInfo {
        private String version;
        private String title;
        private String description;
        private String termsOfServiceUrl;
        private String license;
        private String licenseUrl;
        private Contact contact;
    }

    @Data
    @ConfigurationProperties(prefix = "api")
    public class SwaggerProperties {
        private ApiInfo common;

        public OpenAPI toSwagger() {
            return new OpenAPI()
              .components(new Components()
                .addSecuritySchemes("bearer-key",
                  new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
              .info(new Info().title(common.title).version(common.version) .description(common.description).termsOfService(common.termsOfServiceUrl).license(new License().name(common.license).url(common.licenseUrl)));
        }
    }

}
