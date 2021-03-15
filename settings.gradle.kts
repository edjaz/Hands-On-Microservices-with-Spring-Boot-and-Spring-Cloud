
pluginManagement {
  plugins {
    id ("org.springframework.boot") version("2.3.9.RELEASE")
    id ("io.spring.dependency-management") version( "1.0.11.RELEASE")
    id ("com.google.cloud.tools.jib") version("1.8.0")
    id("org.sonarqube") version "2.8"
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.spring") version "1.3.72"
    kotlin("kapt") version "1.3.72"

  }
}

rootProject.name = "handsOn"

include(":api")
include(":util")

include(":microservices:product:api")
include(":microservices:product:service")
include(":microservices:product-composite:api")
include(":microservices:product-composite:service")
include(":microservices:recommendation:api")
include(":microservices:recommendation:service")
include(":microservices:review:api")
include(":microservices:review:service")
include(":spring-cloud:eureka-server")
include(":spring-cloud:gateway")
include(":spring-cloud:authorization-server")
include(":spring-cloud:config-server")
