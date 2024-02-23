plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":common:util"))
    implementation(project(":grpc-lib"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("net.devh:grpc-server-spring-boot-starter:${property("grpcServerSB")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.grpc:grpc-testing:${property("grpcVersion")}")
    testImplementation("net.devh:grpc-client-spring-boot-starter:${property("grpcServerSB")}")
}
