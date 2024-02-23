plugins {
    id("com.google.protobuf")
}

dependencies {
    implementation("io.grpc:grpc-netty-shaded")
    implementation("io.grpc:grpc-protobuf")
    implementation("io.grpc:grpc-stub")
    if (JavaVersion.current().isJava9Compatible) {
        // Workaround for @javax.annotation.Generated
        // see: https://github.com/grpc/grpc-java/issues/3633
        compileOnly("org.apache.tomcat:annotations-api:6.0.53")
        annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.2"
    }
    generatedFilesBaseDir = "$projectDir/src/generated"
//    clean {
//        delete generatedFilesBaseDir
//    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.60.1"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("grpc")
            }
        }
    }
//    generateProtoTasks {
//        all()*.plugins {
//            grpc {}
//        }
//    }
}

// idea {
//    module {
//        sourceDirs += file('src/generated/main/java')
//        sourceDirs += file('src/generated/main/grpc')
//        generatedSourceDirs += file('src/generated/main/java')
//        generatedSourceDirs += file('src/generated/main/grpc')
//    }
// }
