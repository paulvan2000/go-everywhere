// protocol/build.gradle.kts

plugins {
    id("java")
    id("com.google.protobuf") version "0.9.4" // Keep plugin version unless problematic
}

group = "org.example.goeverywhere.protocol"
version = "1.0.0"

// Define target versions
val grpcVersion = "1.71.0" // Keep the runtime gRPC version from main app
val protobufVersion = "3.19.4" // Use the version from the main app

dependencies {
    // Use the defined versions
    implementation("io.grpc:grpc-netty:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("com.google.protobuf:protobuf-java:$protobufVersion") // Align Protobuf runtime
    implementation("javax.annotation:javax.annotation-api:1.3.2") // Keep if needed
}

protobuf {
    protoc {
        // Align protoc compiler version with the chosen protobuf runtime version
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        create("grpc") {
            // Align gRPC plugin version with the gRPC runtime version
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc") // Apply the gRPC plugin
            }
        }
    }
}

tasks.compileJava {
    dependsOn(tasks.getByName("generateProto")) // Ensure code is generated before compilation
}