# GoEverywhere: project guide

A 2025 academic transportation prototype combining an Android client, a Spring Boot backend, gRPC, and route optimization. This repository preserves the team project's history and contributors; it is not an operating transportation service.

## Start here

- `app/`: Android client, including rider and driver flows.
- `protocol/src/main/proto/services.proto`: shared service and message definitions.
- `server/src/main/java/org/example/goeverywhere/server/flow/`: ride lifecycle state machine.
- `server/src/main/java/org/example/goeverywhere/server/service/routing/`: routing implementations and batching.
- `server/src/test/`: backend test code, including multi-rider scenarios.
- [Companion optimization study](https://github.com/paulvan2000/GoEveryWhere-Darp-Algorithm).

## Build layout

This is a Gradle multi-project build (`app`, `protocol`, and `server`). Open the repository root in Android Studio. The Android app declares compile/target SDK 34 and minimum SDK 24; the Android Gradle plugin is 8.7.3. Use JDK 17 for the Gradle tooling.

From the repository root, the Gradle wrapper commands are:

```sh
./gradlew :server:bootJar
./gradlew :server:test
./gradlew :app:assembleDebug
```

On Windows use `gradlew.bat`. These identify the intended build tasks; they have not been revalidated as part of the portfolio cleanup. An Android SDK is required by this multi-project build. Routing also depends on the native OR-Tools library; the current source explicitly loads `google-ortools-native`, and Windows libraries are included in `server/native_libs`.

The backend reads the Spring property `google.geo.api.key`. Supply your own Google Maps credentials and restrict them appropriately. The Android client currently targets the emulator host at `10.0.2.2:9001`; the server listens on port 9001. A physical device requires a reachable host configuration.

## Prototype limits

Payment and ride-history screens include placeholders. The native-library setup and emulator-specific endpoint need work for portability. Tests are present but this page does not claim that they currently pass. The original README is retained in `docs/original-project-notes.md` as historical project notes.
