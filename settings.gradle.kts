pluginManagement {
    val detektVersion: String by settings
    val dokkaVersion: String by settings
    val kotlinVersion: String by settings
    val ktlintPluginVersion: String by settings
    val mavenPluginDevelopmentVersion: String by settings
    val nexusPublishPluginVersion: String by settings
    val pluginPublishPluginVersion: String by settings
    val springBootVersion: String by settings
    val stagingPluginVersion: String by settings
    val jibVersion: String by settings
    val springDependencyManagementVersion: String by settings
    val sonarqubeVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("kapt") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        id("com.gradle.plugin-publish") version pluginPublishPluginVersion
        id("de.benediktritter.maven-plugin-development") version mavenPluginDevelopmentVersion
        id("de.marcphilipp.nexus-publish") version nexusPublishPluginVersion
        id("io.codearte.nexus-staging") version stagingPluginVersion
        id("io.gitlab.arturbosch.detekt") version detektVersion
        id("org.jetbrains.dokka") version dokkaVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintPluginVersion
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
        id("com.google.cloud.tools.jib") version jibVersion
        id("org.sonarqube") version sonarqubeVersion
    }
}

rootProject.name = "handsOn"

include(":event-api")
include(":util")

include(":product-api")
include(":product-service")
include(":product-composite-api")
include(":product-composite-service")
include(":recommendation-api")
include(":recommendation-service")
include(":review-api")
include(":review-service")
include(":eureka-server")
include(":gateway")
include(":authorization-server")
include(":config-server")

project(":event-api").projectDir = file("event-api")
project(":util").projectDir = file("util")
project(":product-api").projectDir = file("microservices/product/api")
project(":product-service").projectDir = file("microservices/product/service")
project(":product-composite-api").projectDir = file("microservices/product-composite/api")
project(":product-composite-service").projectDir = file("microservices/product-composite/service")
project(":recommendation-api").projectDir = file("microservices/recommendation/api")
project(":recommendation-service").projectDir = file("microservices/recommendation/service")
project(":review-api").projectDir = file("microservices/review/api")
project(":review-service").projectDir = file("microservices/review/service")
project(":eureka-server").projectDir = file("spring-cloud/eureka-server")
project(":gateway").projectDir = file("spring-cloud/gateway")
project(":authorization-server").projectDir = file("spring-cloud/authorization-server")
project(":config-server").projectDir = file("spring-cloud/config-server")
