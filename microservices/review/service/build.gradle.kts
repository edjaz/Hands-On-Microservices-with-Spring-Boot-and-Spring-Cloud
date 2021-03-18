plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java")
    id("jacoco")
    id("org.sonarqube")
    id("com.google.cloud.tools.jib")
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("kapt")
}

group = "fr.edjaz.microservices.core.review"

base {
    archivesBaseName = "review-service"
}

jib {
    from {
        image = "openjdk:12.0.2"
    }
    to {
        image = "edjaz/review/service"
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

val mapstructVersion: String by project

dependencies {
    implementation(project(":api"))
    implementation(project(":review-api"))
    implementation(project(":util"))

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-rabbit")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-kubernetes")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
    implementation("org.springframework.cloud:spring-cloud-starter-zipkin")
    implementation("org.springframework.retry:spring-retry")
    implementation("mysql:mysql-connector-java")
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    compileOnly("org.mapstruct:mapstruct-processor:$mapstructVersion")
    kapt("org.mapstruct:mapstruct-processor:$mapstructVersion")
    testAnnotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    testImplementation("org.springframework.cloud:spring-cloud-stream-test-support")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.h2database:h2")

    testImplementation(project(":product-api"))

    implementation("io.micrometer:micrometer-registry-prometheus")
    runtime("org.springframework.boot:spring-boot-properties-migrator")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}
