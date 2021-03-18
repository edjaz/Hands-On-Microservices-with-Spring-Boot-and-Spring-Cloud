plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java")
    id("jacoco")
    id("org.sonarqube")
    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "fr.edjaz.springcloud"

dependencies {
    runtime("org.springframework.boot:spring-boot-properties-migrator")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
    implementation("org.springframework.cloud:spring-cloud-starter-zipkin")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-rabbit")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka")
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.glassfish.jaxb:jaxb-runtime")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}
