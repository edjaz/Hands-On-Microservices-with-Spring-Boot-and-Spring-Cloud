import io.gitlab.arturbosch.detekt.detekt
import java.net.URI
import java.time.Instant
import java.util.Properties
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

description = "HandsOn Ms"
group = "fr.edjaz"
extra["springCloudVersion"] = "Hoxton.SR10"

plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    jacoco
    id("org.sonarqube")
}

allprojects {
    buildscript {
        repositories {
            mavenLocal()
            mavenCentral()
            jcenter()
        }
    }

    repositories {
        mavenCentral()
        jcenter()
        maven { url = URI("https://repo.spring.io/milestone") }
    }
}

subprojects {
    val kotlinCoroutinesVersion: String by project
    val kotlinJvmVersion: String by project
    val kotlinVersion: String by project
    val detektVersion: String by project
    val ktlintVersion: String by project
    val jacocoVersion: String by project

    val currentProject = this

    apply(plugin = "kotlin")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "jacoco")
    apply(plugin = "org.sonarqube")
    apply(plugin = "java-library")

    version = "0.0.1-SNAPSHOT"
    java.sourceCompatibility = JavaVersion.VERSION_11

    sonarqube {
        properties {
            property("sonar.sources", "src/main/")
            property("sonar.tests", "src/test/")
            property("sonar.exclusions", "**/*Configuration.kt,**/*Application.kt,**/*Entity.kt,**/*Config.kt,**/*Exception.kt")
        }
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = kotlinJvmVersion
                freeCompilerArgs = listOf("-Xjsr305=strict")
            }
        }
        check {
            dependsOn(jacocoTestCoverageVerification)
        }
        detekt {
            toolVersion = detektVersion
            config = files("${rootProject.projectDir}/detekt.yml")
        }
        ktlint {
            version.set(ktlintVersion)
        }
        jacoco {
            toolVersion = jacocoVersion
        }
        jar {
            manifest {
                attributes["Built-By"] = "Edjaz"
                attributes["Build-Jdk"] = "${System.getProperty("java.version")} (${System.getProperty("java.vendor")} ${System.getProperty("java.vm.version")})"
                attributes["Build-Timestamp"] = Instant.now().toString()
                attributes["Created-By"] = "Gradle ${gradle.gradleVersion}"
                attributes["Implementation-Title"] = currentProject.name
                attributes["Implementation-Version"] = project.version
            }
        }
        test {
            useJUnitPlatform()
            finalizedBy(jacocoTestReport)
        }
        jacocoTestReport {
            reports {
                xml.isEnabled = true
                csv.isEnabled = true
                html.isEnabled = true
            }
            dependsOn(test)
        }
    }

    dependencies {
        implementation(kotlin("stdlib", kotlinVersion))
        implementation(kotlin("reflect", kotlinVersion))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinCoroutinesVersion")
        testImplementation(kotlin("test", kotlinVersion))
        testImplementation(kotlin("test-junit5", kotlinVersion))
        testImplementation("org.springframework.boot:spring-boot-starter-test") {
            exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        }
        compileOnly("com.google.code.findbugs:jsr305:3.0.2")
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

task<JacocoReport>("jacocoRootReport") {
    dependsOn(subprojects.map { it.tasks.withType<Test>() })
    dependsOn(subprojects.map { it.tasks.withType<JacocoReport>() })
    additionalSourceDirs.setFrom(subprojects.map { it.the<SourceSetContainer>()["main"].allSource.srcDirs })
    sourceDirectories.setFrom(subprojects.map { it.the<SourceSetContainer>()["main"].allSource.srcDirs })
    classDirectories.setFrom(subprojects.map { it.the<SourceSetContainer>()["main"].output })
    executionData.setFrom(project.fileTree(".") { include("**/build/jacoco/test.exec") })
    reports {
        xml.isEnabled = true
        csv.isEnabled = true
        html.isEnabled = true
        html.destination = file("$buildDir/reports/jacoco/html")
    }
}
