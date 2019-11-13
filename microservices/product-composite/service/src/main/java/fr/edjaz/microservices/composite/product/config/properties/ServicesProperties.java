package fr.edjaz.microservices.composite.product.config.properties;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static java.util.Collections.emptyList;

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

        public springfox.documentation.service.ApiInfo toSwagger() {
            return new springfox.documentation.service.ApiInfo(
                    common.title,
                    common.description,
                    common.version,
                    common.termsOfServiceUrl,
                    new springfox.documentation.service.Contact(common.contact.name, common.contact.url,  common.contact.email),
                    common.license,
                    common.licenseUrl,
                    emptyList()
            );
        }
    }

}
