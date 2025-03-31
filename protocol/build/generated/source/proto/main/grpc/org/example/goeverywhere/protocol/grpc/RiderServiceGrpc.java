package org.example.goeverywhere.protocol.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.71.0)",
    comments = "Source: services.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class RiderServiceGrpc {

  private RiderServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "org.example.goeverywhere.protocol.grpc.RiderService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.RideRequest,
      org.example.goeverywhere.protocol.grpc.RiderEvent> getRequestRideMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "requestRide",
      requestType = org.example.goeverywhere.protocol.grpc.RideRequest.class,
      responseType = org.example.goeverywhere.protocol.grpc.RiderEvent.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.RideRequest,
      org.example.goeverywhere.protocol.grpc.RiderEvent> getRequestRideMethod() {
    io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.RideRequest, org.example.goeverywhere.protocol.grpc.RiderEvent> getRequestRideMethod;
    if ((getRequestRideMethod = RiderServiceGrpc.getRequestRideMethod) == null) {
      synchronized (RiderServiceGrpc.class) {
        if ((getRequestRideMethod = RiderServiceGrpc.getRequestRideMethod) == null) {
          RiderServiceGrpc.getRequestRideMethod = getRequestRideMethod =
              io.grpc.MethodDescriptor.<org.example.goeverywhere.protocol.grpc.RideRequest, org.example.goeverywhere.protocol.grpc.RiderEvent>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "requestRide"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.goeverywhere.protocol.grpc.RideRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.goeverywhere.protocol.grpc.RiderEvent.getDefaultInstance()))
              .setSchemaDescriptor(new RiderServiceMethodDescriptorSupplier("requestRide"))
              .build();
        }
      }
    }
    return getRequestRideMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static RiderServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RiderServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RiderServiceStub>() {
        @java.lang.Override
        public RiderServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RiderServiceStub(channel, callOptions);
        }
      };
    return RiderServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static RiderServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RiderServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RiderServiceBlockingV2Stub>() {
        @java.lang.Override
        public RiderServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RiderServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return RiderServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static RiderServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RiderServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RiderServiceBlockingStub>() {
        @java.lang.Override
        public RiderServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RiderServiceBlockingStub(channel, callOptions);
        }
      };
    return RiderServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static RiderServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RiderServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RiderServiceFutureStub>() {
        @java.lang.Override
        public RiderServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RiderServiceFutureStub(channel, callOptions);
        }
      };
    return RiderServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void requestRide(org.example.goeverywhere.protocol.grpc.RideRequest request,
        io.grpc.stub.StreamObserver<org.example.goeverywhere.protocol.grpc.RiderEvent> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRequestRideMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service RiderService.
   */
  public static abstract class RiderServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return RiderServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service RiderService.
   */
  public static final class RiderServiceStub
      extends io.grpc.stub.AbstractAsyncStub<RiderServiceStub> {
    private RiderServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RiderServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RiderServiceStub(channel, callOptions);
    }

    /**
     */
    public void requestRide(org.example.goeverywhere.protocol.grpc.RideRequest request,
        io.grpc.stub.StreamObserver<org.example.goeverywhere.protocol.grpc.RiderEvent> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getRequestRideMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service RiderService.
   */
  public static final class RiderServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<RiderServiceBlockingV2Stub> {
    private RiderServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RiderServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RiderServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     */
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/10918")
    public io.grpc.stub.BlockingClientCall<?, org.example.goeverywhere.protocol.grpc.RiderEvent>
        requestRide(org.example.goeverywhere.protocol.grpc.RideRequest request) {
      return io.grpc.stub.ClientCalls.blockingV2ServerStreamingCall(
          getChannel(), getRequestRideMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service RiderService.
   */
  public static final class RiderServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<RiderServiceBlockingStub> {
    private RiderServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RiderServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RiderServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public java.util.Iterator<org.example.goeverywhere.protocol.grpc.RiderEvent> requestRide(
        org.example.goeverywhere.protocol.grpc.RideRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getRequestRideMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service RiderService.
   */
  public static final class RiderServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<RiderServiceFutureStub> {
    private RiderServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RiderServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RiderServiceFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_REQUEST_RIDE = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REQUEST_RIDE:
          serviceImpl.requestRide((org.example.goeverywhere.protocol.grpc.RideRequest) request,
              (io.grpc.stub.StreamObserver<org.example.goeverywhere.protocol.grpc.RiderEvent>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getRequestRideMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              org.example.goeverywhere.protocol.grpc.RideRequest,
              org.example.goeverywhere.protocol.grpc.RiderEvent>(
                service, METHODID_REQUEST_RIDE)))
        .build();
  }

  private static abstract class RiderServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    RiderServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.example.goeverywhere.protocol.grpc.Services.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("RiderService");
    }
  }

  private static final class RiderServiceFileDescriptorSupplier
      extends RiderServiceBaseDescriptorSupplier {
    RiderServiceFileDescriptorSupplier() {}
  }

  private static final class RiderServiceMethodDescriptorSupplier
      extends RiderServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    RiderServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (RiderServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new RiderServiceFileDescriptorSupplier())
              .addMethod(getRequestRideMethod())
              .build();
        }
      }
    }
    return result;
  }
}
