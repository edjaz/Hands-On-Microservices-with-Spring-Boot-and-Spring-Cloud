plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java")
    id("jacoco")
    id("org.sonarqube")
    id("com.google.cloud.tools.jib")
    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "fr.edjaz.microservices.composite.product"

base {
    archivesBaseName = "product-composite-service"
}

jib {
    from {
        image = "openjdk:11"
    }
    to {
        image = "edjaz/product-composite/service"
    }
}

tasks {
    jar {
        enabled = false
    }
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
    imageName = "edjaz/product-composite/service"
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

val resilience4jVersion: String by project
val feignReactorSpringCloudVersion: String by project
val springDocOpenapiVersion: String by project

dependencies {
    implementation(project(":event-api"))
    implementation(project(":product-composite-api"))
    implementation(project(":product-api"))
    implementation(project(":review-api"))
    implementation(project(":recommendation-api"))

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation(project(":util"))

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-oauth2-resource-server")
    implementation("org.springframework.security:spring-security-oauth2-jose")

    implementation("org.springframework.cloud:spring-cloud-starter-stream-rabbit")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    implementation("com.playtika.reactivefeign:feign-reactor-spring-cloud-starter:$feignReactorSpringCloudVersion")

    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")

    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.cloud:spring-cloud-sleuth-zipkin")
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
    implementation("org.springframework.retry:spring-retry")

    implementation("org.springframework.cloud:spring-cloud-starter-kubernetes-client")
    implementation("org.springframework.cloud:spring-cloud-starter-kubernetes-client-config")
    implementation("org.springframework.cloud:spring-cloud-starter-kubernetes-client-loadbalancer")

    implementation("org.springdoc:springdoc-openapi-webflux-ui:$springDocOpenapiVersion")
    implementation("org.springdoc:springdoc-openapi-security:$springDocOpenapiVersion")

    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(group = "org.springframework.cloud", name = "spring-cloud-stream", ext = "jar", classifier = "test-binder")

    testImplementation("io.projectreactor:reactor-test")

    implementation("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("org.springframework.boot:spring-boot-properties-migrator")
}

val springCloudVersion: String by project

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}
