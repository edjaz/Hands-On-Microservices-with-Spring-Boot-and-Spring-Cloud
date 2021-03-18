plugins {
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
    id("java")
    id("jacoco")
    id("org.sonarqube")
    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "fr.edjaz.microservices.core.product"

base {
    archivesBaseName = "product-api"
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
}

apply(plugin = "io.spring.dependency-management")

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}
