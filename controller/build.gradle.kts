import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("org.springframework.boot") version "3.2.2"
}

dependencies {
    implementation(project(":common:util"))
    implementation(project(":github"))
    implementation(project(":grpc-server"))
    implementation(project(":grpc-lib"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("net.devh:grpc-server-spring-boot-starter:${property("grpcServerSB")}")
    implementation("net.devh:grpc-server-spring-boot-autoconfigure:${property("grpcServerSbAutoconfigure")}")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("org.springframework.modulith:spring-modulith-actuator")
    runtimeOnly("org.springframework.modulith:spring-modulith-observability")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("net.devh:grpc-client-spring-boot-starter:${property("grpcServerSB")}")
}

tasks.getByName<BootRun>("bootRun") {
    environment.put("SPRING_PROFILES_ACTIVE", environment.get("SPRING_PROFILES_ACTIVE") ?: "local")
    workingDir = rootProject.projectDir
    println("workingdir: $workingDir")
    enabled = true
}
tasks.getByName<BootJar>("bootJar") {
    enabled = true
}

tasks.withType<Test> {
    useJUnitPlatform()
}
