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

group = "fr.edjaz.springcloud"

jib {
    from {
        image = "openjdk:11"
    }
    to {
        image = "edjaz/auth-server"
    }
}

tasks {
    jar {
        enabled = false
    }
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
    imageName = "edjaz/auth-server"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("org.springframework.security:spring-security-jwt")
    implementation("org.springframework.security.oauth:spring-security-oauth2")
    implementation("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.cloud:spring-cloud-sleuth-zipkin")
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
//    implementation("org.springframework.cloud:spring-cloud-starter-stream-rabbit")
//    implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka")
    implementation("org.springframework.retry:spring-retry")

    implementation("org.springframework.cloud:spring-cloud-starter-kubernetes-client")
    implementation("org.springframework.cloud:spring-cloud-starter-kubernetes-client-config")

    implementation("javax.xml.bind:jaxb-api")
//    implementation("com.sun.xml.bind:jaxb-core")
//    implementation("com.sun.xml.bind:jaxb-impl")
    implementation("com.nimbusds:nimbus-jose-jwt:6.7")

    implementation("io.micrometer:micrometer-registry-prometheus")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

val springCloudVersion: String by project

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}
