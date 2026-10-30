# GoEverywhere Ride-Sharing Application

## Overview

GoEverywhere is a prototype ride-sharing application designed to connect riders needing transportation with available drivers. It consists of:

1.  **Android Client:** Provides the user interface for both riders and drivers to interact with the system.
2.  **Java Spring Boot Server:** Handles the backend logic, including user management, ride requests, driver assignments, routing, real-time updates, and data persistence.

Communication between the client and server is handled via gRPC, utilizing Protocol Buffers for efficient data serialization and service definitions. The server employs Spring Statemachine to manage the lifecycle of a ride.

## Features

**General:**

*   User Authentication (Login/Sign Up)
*   Role Selection (Rider or Driver)
*   Real-time communication via gRPC streams

**Rider Features:**

*   Book Immediate Rides
*   Schedule Rides for Future Pickup (Batch Processing)
*   Enter Origin and Destination Addresses (Geocoding used)
*   Specify Number of Passengers
*   View Assigned Driver's Location and ETA in Real-time
*   Receive Updates on Ride Status (Driver Assigned, En Route, Arrived, Started, Completed, Cancelled)
*   Profile Management:
    *   Payment Options (UI Placeholder)
    *   Ride History (UI Placeholder)
    *   Safety & Accessibility Preferences (Wheelchair, Service Animal, etc.)
    *   Notification Preferences

**Driver Features:**

*   Go Online/Offline to indicate availability
*   View Available Ride Requests (including batched/optimized routes)
*   Accept or Reject Ride Requests
*   View Detailed Route Information on a Map
*   Update Ride Status (Arrived at Pickup, Started Ride, Completed Ride)
*   Real-time Location Updates sent to the server

## Architecture

*   **Client:** Android Application (presumably written in Kotlin or Java using Android Studio). Interacts with the server via gRPC. Uses Google Maps SDK for map display.
*   **Server:** Java Spring Boot application.
    *   **API Layer:** gRPC services defined in `.proto` files (located in `protocol/`) and implemented in the `server/grpc` package.
    *   **Business Logic:** Contained within the `server/service` package. Key components include:
        *   `UserService`: Handles login, sign-up.
        *   `RiderService`: Handles incoming ride requests from riders.
        *   `DriverServiceGprcImpl`: Handles driver actions (accept/reject, status updates).
        *   `UserRegistry`: Manages connected users (drivers/riders), their state (location, current route), and stream observers for real-time updates. Crucial for coordinating communication.
        *   `EventProcessor`: Contains the core logic triggered by state machine transitions (finding drivers, notifying users, updating routes).
        *   `RideStateMachineService` & `flow` package: Manages the lifecycle of rides using Spring Statemachine (`RideState`, `RideEvent`).
        *   `routing` package: Handles route calculation and optimization.
            *   `RouteService` interface.
            *   `GoogleMapsRouteService`: Uses Google Maps Directions API for basic routing.
            *   `OptimizedRouteService`: Uses Google OR-Tools for solving Vehicle Routing Problems (VRP), enabling merging multiple rides into an optimized route.
            *   `MockRouteService`: Provides simple mock routes for testing.
        *   `RideBatchScheduler`: Processes scheduled ride requests periodically, groups them, calculates an optimized merged route using `OptimizedRouteService`, and assigns them to drivers.
        *   `GeocodingService`: Converts addresses to latitude/longitude using Google Geocoding API.
        *   `DriverLocationUpdateService`: Periodically sends driver location updates to relevant riders.
        *   `SessionStore`: Manages user session information.
    *   **Data Persistence:** Uses Spring Data JPA with Hibernate.
        *   `User` entity (`server/data/model`) represents users in the database.
        *   `UserRepository` (`server/data/repository`) provides database access methods.
        *   The configuration appears to use SQLite (inferred from `UserServiceGrpcImpl` error handling).
    *   **Configuration:** Spring Boot configuration (`application.properties` likely located in `resources/`). Requires Google API keys.

*   **Communication Protocol:** gRPC with Protocol Buffers. Enables efficient, strongly-typed communication and supports bi-directional streaming for real-time features like location updates.

## Client UI Flow

The Android client provides distinct interfaces for riders and drivers:

1.  **Role Selection:** User chooses "I WANT TO DRIVE" or "I NEED A RIDE".
2.  **Login/Sign Up:** Standard email/password authentication.
3.  **Rider Home:** Options to "BOOK RIDE NOW" or "PLAN TRIP" (schedule).
4.  **Rider Profile:** Access to Payment Options, Ride History, Safety/Accessibility, Notifications.
5.  **Ride Booking:** Enter origin, destination, and passenger count. Geocoding converts addresses.
6.  **Waiting Screen:** Shown after booking while searching for a driver.
7.  **Driver Tracking:** Displays driver's vehicle details, ETA, and real-time location on a map. Option to cancel.
8.  **Driver Dashboard:** Welcome screen, option to "VIEW RIDE REQUESTS" or "GO OFFLINE".
9.  **Available Ride Requests:** List of pending rides (potentially batched/optimized) with details and Accept/Reject options.
10. **Ride Details (Driver):** Shows pickup/dropoff locations, passenger count, status, and map overview. Accept/Reject buttons.
11. **Active Ride Map (Driver):** Shows the route on the map during an active ride.

## Technologies Used

*   **Server:**
    *   Java (JDK 17+ likely)
    *   Spring Boot 3+
    *   Spring Data JPA / Hibernate
    *   Spring Statemachine
    *   gRPC-Java
    *   Google OR-Tools (for `OptimizedRouteService`)
    *   Google Maps API (Directions, Geocoding)
    *   SQLite (Database, based on code)
    *   BCrypt (Password Encoding)
    *   Maven or Gradle (Build Tool)
*   **Client:**
    *   Android SDK
    *   Kotlin or Java
    *   gRPC-Android / gRPC-stub
    *   Google Maps SDK for Android
    *   Android Studio (IDE)
*   **Communication:**
    *   gRPC
    *   Protocol Buffers (Proto3)

## Setup & Running

**Prerequisites:**

*   Java Development Kit (JDK 17 or later recommended)
*   Maven or Gradle installed and configured
*   Android Studio installed
*   Google Cloud Platform Account
    *   API Key with **Directions API** and **Geocoding API** enabled.
    *   (For Client) API Key with **Maps SDK for Android** enabled.

**Server Setup:**

1.  Clone the repository.
2.  Navigate to the `server/` directory.
3.  Configure API Key: Add your Google Maps API key to the `server/src/main/resources/application.properties` file (create it if it doesn't exist):
    ```properties
    google.geo.api.key=YOUR_GOOGLE_API_KEY
    # Add any other required Spring Boot or Database properties
    spring.datasource.url=jdbc:sqlite:goeverywhere.db # Example for SQLite
    spring.datasource.driverClassName=org.sqlite.JDBC
    spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect # Adjust if using a different dialect
    spring.jpa.hibernate.ddl-auto=update # Or 'validate', 'create' for initial setup
    ```
4.  Build the server:
    *   If using Maven: `mvn clean package`
    *   If using Gradle: `./gradlew clean build`
5.  Run the server: `java -jar target/goeverywhere-server-*.jar` (adjust filename)
6.  The gRPC server should start on port `9001` (as configured in `GoEverywhereServer.java`).

**Client Setup:**

1.  Clone the repository.
2.  Open the Android project (likely located in a separate directory, e.g., `client/` or `android-app/`) in Android Studio.
3.  Configure API Key: Add your Google Maps SDK for Android API key, typically in the `local.properties` file or directly in the `AndroidManifest.xml` (check project specifics).
4.  Ensure gRPC client dependencies and Protobuf code generation are correctly configured in the `build.gradle` files. You might need to rebuild the project (`Build > Rebuild Project`) for generated code to be recognized.
5.  Ensure the client is configured to connect to the server's IP address and port (e.g., `localhost:9001` if running on the same machine, or the server's network IP if different). This is typically set when creating the gRPC channel.
6.  Run the application on an Android emulator or physical device.

## Future Work & Improvements

*   **Robust Error Handling:** Implement more comprehensive error handling on both client and server (e.g., network issues, API failures, invalid inputs).
*   **Payment Integration:** Integrate a real payment gateway (Stripe, Braintree, etc.).
*   **Advanced Routing:**
    *   Incorporate real-time traffic data.
    *   Add time window constraints for scheduled pickups/dropoffs in OR-Tools.
    *   Consider vehicle capacity constraints.
*   **Testing:** Add comprehensive unit tests (JUnit, Mockito), integration tests (Spring Boot Test), and potentially end-to-end tests.
*   **Push Notifications:** Use Firebase Cloud Messaging (FCM) for more reliable background notifications instead of relying solely on open gRPC streams, especially for ride status updates when the app is not in the foreground.
*   **UI/UX Refinements:** Improve the user interface and experience based on user feedback.
*   **Security:**
    *   Implement more robust authentication/authorization (e.g., JWT, OAuth).
    *   Secure API keys properly (don't commit them directly).
*   **Scalability:**
    *   Consider switching from SQLite to a more production-ready database (PostgreSQL, MySQL) if scaling is anticipated.
    *   Implement connection pooling and optimize database queries.
*   **Deployment:** Dockerize the server application for easier deployment.

## Contributing

This project is intended as a learning prototype. If you wish to contribute, please follow standard Git practices:

1.  Fork the repository.
2.  Create a new branch for your feature or bug fix (`git checkout -b feature/your-feature-name`).
3.  Make your changes and commit them (`git commit -m 'Add some feature'`).
4.  Push to your branch (`git push origin feature/your-feature-name`).
5.  Open a Pull Request against the main repository branch.

## License

Copyright 2025 FAU

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
