import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties


plugins {
  java
  jacoco
  id("org.sonarqube")
	kotlin("jvm")
	kotlin("plugin.spring")
  kotlin("kapt")
}


group = "fr.edjaz"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

extra["mapstructVersion"] = "1.3.1.Final"
extra["springCloudVersion"] = "Hoxton.SR10"
extra["resilience4jVersion"] = "1.2.0"

repositories {
    mavenCentral()
}


dependencies {
  compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}


tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "11"
  }
}

jacoco {
    toolVersion = "0.8.5"
}


tasks.jacocoTestReport {
  reports {
    xml.isEnabled = true
  }
}

sonarqube {
  properties {
    val sonarProperties = Properties()
    sonarProperties.load(file("sonar-project.properties").inputStream())
    sonarProperties.forEach {
      property(it.key as String, it.value)
    }
  }
}




