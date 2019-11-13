package fr.edjaz.microservices.composite.product.config;

import fr.edjaz.microservices.composite.product.config.properties.ServicesProperties;
import io.swagger.models.auth.In;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiKey;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Arrays;

import static java.util.Collections.emptyList;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;
import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

@Configuration
public class SwaggerConfiguration {

    /**
     * Will exposed on $HOST:$PORT/swagger-ui.html
     *
     * @return
     */
    @Bean
    public Docket apiDocumentation(ServicesProperties.SwaggerProperties swaggerProperties) {


        return new Docket(SWAGGER_2)
                .securitySchemes(Arrays.asList(new ApiKey("Token Access", HttpHeaders.AUTHORIZATION, In.HEADER.name())))
                .select()
                .apis(basePackage("fr.edjaz.microservices.composite.product"))
                .paths(PathSelectors.any())
                .build()
                .globalResponseMessage(POST, emptyList())
                .globalResponseMessage(GET, emptyList())
                .globalResponseMessage(DELETE, emptyList())
                .apiInfo(swaggerProperties.toSwagger());
    }
}
