import java.net.URI;

plugins {
	id ("org.springframework.boot") version "2.2.11.RELEASE" apply false
	id ("io.spring.dependency-management") version "1.0.10.RELEASE"
	id ("java")
	id ("jacoco")
	id ("org.sonarqube")
  kotlin("jvm")
  kotlin("plugin.spring")
}

group = "fr.edjaz.microservices.core.review"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

base {
	archivesBaseName =  "review-api"
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
repositories {
	mavenCentral()

}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	compileOnly ("com.google.code.findbugs:jsr305:3.0.2")
	compileOnly ("org.projectlombok:lombok")
	annotationProcessor ("org.projectlombok:lombok")

	implementation ("org.springframework.boot:spring-boot-starter-webflux")
}

apply(plugin = "io.spring.dependency-management")

dependencyManagement {
  imports {
    mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
  }
}

tasks.jacocoTestReport {
  reports {
    xml.isEnabled = true
    csv.isEnabled = false
    html.isEnabled = true
  }
}

sonarqube {
	properties {
		property ("sonar.sources", "src/main/")
	}
}
