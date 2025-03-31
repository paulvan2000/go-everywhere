package com.example.goeverywhere;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.goeverywhere.protocol.grpc.LoginResponse;

import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicReference;

@Module
@InstallIn(SingletonComponent.class)
public class GrpcModule {
    @Provides
    @Singleton
    public ManagedChannel provideManagedChannel() {
        return ManagedChannelBuilder
                .forAddress("10.0.2.2", 9001) // FIXME: configure from environment
                .usePlaintext() // Disable TLS for development
                .build();
    }

    @Provides
    @Singleton
    public AtomicReference<LoginResponse> sessionHolder() {
        return new AtomicReference<>();
    }
}
