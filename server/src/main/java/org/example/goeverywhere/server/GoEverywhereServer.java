package org.example.goeverywhere.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.example.goeverywhere.server.grpc.DriverServiceGprcImpl;
import org.example.goeverywhere.server.grpc.RiderServiceGrpcImpl;
import org.example.goeverywhere.server.grpc.UserServiceGrpcImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main server class for the goeverywhere application.
 * Initializes and starts the gRPC server to handle incoming client requests.
 */
@SpringBootApplication
public class GoEverywhereServer {

    @Autowired
    private UserServiceGrpcImpl userServiceGrpc;
    @Autowired
    private RiderServiceGrpcImpl riderServiceGrpc;
    @Autowired
    private DriverServiceGprcImpl driverServiceGrpc;
    private ExecutorService serverDestroyer;

    /**
     * Starts the gRPC server on the specified port.
     *
     * @throws IOException if an I/O error occurs when starting the server.
     */
    @PostConstruct
    public void init()  throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(9001)
                .addService(userServiceGrpc)
                .addService(riderServiceGrpc)
                .addService(driverServiceGrpc)
                .build();
        System.out.println("Starting server...");
        server.start();
        System.out.println("Server started!");
        serverDestroyer = Executors.newSingleThreadExecutor();
        serverDestroyer.execute(() -> {
            try {
                server.awaitTermination();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

    }

    @PreDestroy
    public void shutdown() {
        serverDestroyer.shutdown();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        SpringApplication.run(GoEverywhereServer.class, args);
    }

}
