plugins {
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
    id("java")
    id("jacoco")
    id("org.sonarqube")
    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "fr.edjaz.microservices.composite.product"

base {
    archivesBaseName = "composite-product-api"
}

val developmentOnly by configurations.creating
configurations {
    runtimeClasspath {
        extendsFrom(developmentOnly)
    }
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

val junitVersion: String by project

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("org.springdoc:springdoc-openapi-webflux-ui:1.5.0")
    implementation("org.springdoc:springdoc-openapi-security:1.5.0")
}

apply(plugin = "io.spring.dependency-management")

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}
