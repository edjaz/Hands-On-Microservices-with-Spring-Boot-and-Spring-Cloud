plugins {
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
    id("java")
    id("jacoco")
    id("org.sonarqube")
    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "fr.edjaz.microservices.event-api"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

apply(plugin = "io.spring.dependency-management")

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}
