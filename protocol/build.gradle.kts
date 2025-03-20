plugins {
    id("java")
    id("com.google.protobuf") version "0.9.4" // Apply the protobuf plugin
}

group = "org.example.goeverywhere.protocol"
version = "1.0.0"

dependencies {
    implementation("io.grpc:grpc-netty:1.71.0") // gRPC transport
    implementation("io.grpc:grpc-protobuf:1.71.0") // gRPC Protobuf runtime
    implementation("io.grpc:grpc-stub:1.71.0") // gRPC stubs
    implementation("com.google.protobuf:protobuf-java:3.24.3") // Protobuf Java runtime
    implementation("javax.annotation:javax.annotation-api:1.3.2")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.22.2" // Protobuf compiler
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.58.0"
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
    dependsOn("generateProto") // Ensure code is generated before compilation
}