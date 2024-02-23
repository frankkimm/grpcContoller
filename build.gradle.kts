import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("jacoco")
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
    id("com.google.protobuf") version "0.9.4"
}

repositories {
    mavenCentral()
}

allprojects {
    group = "de.kimmlingen"
    version = "0.0.1-SNAPSHOT"

    apply {
        plugin("java-library")
        plugin("io.spring.dependency-management")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("org.springframework.boot")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("jacoco")
        plugin("com.gorylenko.gradle-git-properties") // commit information in actuator
        plugin("idea")
        plugin("eclipse")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
    }

    repositories {
        mavenCentral()
    }

    extra["springModulithVersion"] = "1.1.2"
    // https://github.com/protocolbuffers/protobuf/releases
    extra["protobufVersion"] = "3.25.2"
    // https://github.com/grpc/grpc-java/releases
    extra["grpcVersion"] = "1.60.1"

    extra["grpcServerSB"] = "2.13.1.RELEASE"
    extra["grpcServerSbAutoconfigure"] = "2.15.0.RELEASE"

    dependencyManagement {
        imports {
            mavenBom("org.springframework.modulith:spring-modulith-bom:${property("springModulithVersion")}")
            mavenBom("com.google.protobuf:protobuf-bom:${property("protobufVersion")}")
            mavenBom("io.grpc:grpc-bom:${property("grpcVersion")}")
        }
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        debug.set(true)
    }
    tasks.getByName<BootRun>("bootRun") {
        enabled = false
    }

    tasks.getByName<BootJar>("bootJar") {
        enabled = false
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            jvmTarget = "21"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.compileJava {
        dependsOn(tasks.ktlintFormat)
    }

//    tasks.jacocoTestCoverageVerification {
//        violationRules {
//            rule {
//                element = "CLASS"
//                limit {
//                    counter = "LINE"
//                    value = "COVEREDRATIO"
//                    minimum = 0.8.toBigDecimal()
//                }
//                excludes =
//                    listOf(
//                    )
//            }
//        }
//    }
//
//    tasks.test {
//        finalizedBy(tasks.jacocoTestReport)
//    }
//
//    tasks.check {
//        dependsOn(tasks.jacocoTestCoverageVerification)
//    }
}
