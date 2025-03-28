package org.example.goeverywhere.protocol.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.58.0)",
    comments = "Source: services.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class DriverServiceGrpc {

  private DriverServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "org.example.goeverywhere.protocol.grpc.DriverService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.SubscribeForRideEventsRequest,
      org.example.goeverywhere.protocol.grpc.DriverEvent> getSubscribeForRideEventsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "subscribeForRideEvents",
      requestType = org.example.goeverywhere.protocol.grpc.SubscribeForRideEventsRequest.class,
      responseType = org.example.goeverywhere.protocol.grpc.DriverEvent.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.SubscribeForRideEventsRequest,
      org.example.goeverywhere.protocol.grpc.DriverEvent> getSubscribeForRideEventsMethod() {
    io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.SubscribeForRideEventsRequest, org.example.goeverywhere.protocol.grpc.DriverEvent> getSubscribeForRideEventsMethod;
    if ((getSubscribeForRideEventsMethod = DriverServiceGrpc.getSubscribeForRideEventsMethod) == null) {
      synchronized (DriverServiceGrpc.class) {
        if ((getSubscribeForRideEventsMethod = DriverServiceGrpc.getSubscribeForRideEventsMethod) == null) {
          DriverServiceGrpc.getSubscribeForRideEventsMethod = getSubscribeForRideEventsMethod =
              io.grpc.MethodDescriptor.<org.example.goeverywhere.protocol.grpc.SubscribeForRideEventsRequest, org.example.goeverywhere.protocol.grpc.DriverEvent>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "subscribeForRideEvents"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.goeverywhere.protocol.grpc.SubscribeForRideEventsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.goeverywhere.protocol.grpc.DriverEvent.getDefaultInstance()))
              .setSchemaDescriptor(new DriverServiceMethodDescriptorSupplier("subscribeForRideEvents"))
              .build();
        }
      }
    }
    return getSubscribeForRideEventsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.AcceptRideRequest,
      com.google.protobuf.Empty> getAcceptRideMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "acceptRide",
      requestType = org.example.goeverywhere.protocol.grpc.AcceptRideRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.AcceptRideRequest,
      com.google.protobuf.Empty> getAcceptRideMethod() {
    io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.AcceptRideRequest, com.google.protobuf.Empty> getAcceptRideMethod;
    if ((getAcceptRideMethod = DriverServiceGrpc.getAcceptRideMethod) == null) {
      synchronized (DriverServiceGrpc.class) {
        if ((getAcceptRideMethod = DriverServiceGrpc.getAcceptRideMethod) == null) {
          DriverServiceGrpc.getAcceptRideMethod = getAcceptRideMethod =
              io.grpc.MethodDescriptor.<org.example.goeverywhere.protocol.grpc.AcceptRideRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "acceptRide"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.goeverywhere.protocol.grpc.AcceptRideRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new DriverServiceMethodDescriptorSupplier("acceptRide"))
              .build();
        }
      }
    }
    return getAcceptRideMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.RejectRideRequest,
      com.google.protobuf.Empty> getRejectRideMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "rejectRide",
      requestType = org.example.goeverywhere.protocol.grpc.RejectRideRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.RejectRideRequest,
      com.google.protobuf.Empty> getRejectRideMethod() {
    io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.RejectRideRequest, com.google.protobuf.Empty> getRejectRideMethod;
    if ((getRejectRideMethod = DriverServiceGrpc.getRejectRideMethod) == null) {
      synchronized (DriverServiceGrpc.class) {
        if ((getRejectRideMethod = DriverServiceGrpc.getRejectRideMethod) == null) {
          DriverServiceGrpc.getRejectRideMethod = getRejectRideMethod =
              io.grpc.MethodDescriptor.<org.example.goeverywhere.protocol.grpc.RejectRideRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "rejectRide"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.goeverywhere.protocol.grpc.RejectRideRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new DriverServiceMethodDescriptorSupplier("rejectRide"))
              .build();
        }
      }
    }
    return getRejectRideMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.DriverArrivedRequest,
      com.google.protobuf.Empty> getDriverArrivedMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "driverArrived",
      requestType = org.example.goeverywhere.protocol.grpc.DriverArrivedRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.DriverArrivedRequest,
      com.google.protobuf.Empty> getDriverArrivedMethod() {
    io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.DriverArrivedRequest, com.google.protobuf.Empty> getDriverArrivedMethod;
    if ((getDriverArrivedMethod = DriverServiceGrpc.getDriverArrivedMethod) == null) {
      synchronized (DriverServiceGrpc.class) {
        if ((getDriverArrivedMethod = DriverServiceGrpc.getDriverArrivedMethod) == null) {
          DriverServiceGrpc.getDriverArrivedMethod = getDriverArrivedMethod =
              io.grpc.MethodDescriptor.<org.example.goeverywhere.protocol.grpc.DriverArrivedRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "driverArrived"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.goeverywhere.protocol.grpc.DriverArrivedRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new DriverServiceMethodDescriptorSupplier("driverArrived"))
              .build();
        }
      }
    }
    return getDriverArrivedMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.RideStartedRequest,
      com.google.protobuf.Empty> getRideStartedMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "rideStarted",
      requestType = org.example.goeverywhere.protocol.grpc.RideStartedRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.RideStartedRequest,
      com.google.protobuf.Empty> getRideStartedMethod() {
    io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.RideStartedRequest, com.google.protobuf.Empty> getRideStartedMethod;
    if ((getRideStartedMethod = DriverServiceGrpc.getRideStartedMethod) == null) {
      synchronized (DriverServiceGrpc.class) {
        if ((getRideStartedMethod = DriverServiceGrpc.getRideStartedMethod) == null) {
          DriverServiceGrpc.getRideStartedMethod = getRideStartedMethod =
              io.grpc.MethodDescriptor.<org.example.goeverywhere.protocol.grpc.RideStartedRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "rideStarted"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.goeverywhere.protocol.grpc.RideStartedRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new DriverServiceMethodDescriptorSupplier("rideStarted"))
              .build();
        }
      }
    }
    return getRideStartedMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.RideCompletedRequest,
      com.google.protobuf.Empty> getRideCompletedMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "rideCompleted",
      requestType = org.example.goeverywhere.protocol.grpc.RideCompletedRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.RideCompletedRequest,
      com.google.protobuf.Empty> getRideCompletedMethod() {
    io.grpc.MethodDescriptor<org.example.goeverywhere.protocol.grpc.RideCompletedRequest, com.google.protobuf.Empty> getRideCompletedMethod;
    if ((getRideCompletedMethod = DriverServiceGrpc.getRideCompletedMethod) == null) {
      synchronized (DriverServiceGrpc.class) {
        if ((getRideCompletedMethod = DriverServiceGrpc.getRideCompletedMethod) == null) {
          DriverServiceGrpc.getRideCompletedMethod = getRideCompletedMethod =
              io.grpc.MethodDescriptor.<org.example.goeverywhere.protocol.grpc.RideCompletedRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "rideCompleted"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.goeverywhere.protocol.grpc.RideCompletedRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new DriverServiceMethodDescriptorSupplier("rideCompleted"))
              .build();
        }
      }
    }
    return getRideCompletedMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static DriverServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DriverServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DriverServiceStub>() {
        @java.lang.Override
        public DriverServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DriverServiceStub(channel, callOptions);
        }
      };
    return DriverServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static DriverServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DriverServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DriverServiceBlockingStub>() {
        @java.lang.Override
        public DriverServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DriverServiceBlockingStub(channel, callOptions);
        }
      };
    return DriverServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static DriverServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DriverServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DriverServiceFutureStub>() {
        @java.lang.Override
        public DriverServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DriverServiceFutureStub(channel, callOptions);
        }
      };
    return DriverServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void subscribeForRideEvents(org.example.goeverywhere.protocol.grpc.SubscribeForRideEventsRequest request,
        io.grpc.stub.StreamObserver<org.example.goeverywhere.protocol.grpc.DriverEvent> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSubscribeForRideEventsMethod(), responseObserver);
    }

    /**
     */
    default void acceptRide(org.example.goeverywhere.protocol.grpc.AcceptRideRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAcceptRideMethod(), responseObserver);
    }

    /**
     */
    default void rejectRide(org.example.goeverywhere.protocol.grpc.RejectRideRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRejectRideMethod(), responseObserver);
    }

    /**
     */
    default void driverArrived(org.example.goeverywhere.protocol.grpc.DriverArrivedRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDriverArrivedMethod(), responseObserver);
    }

    /**
     */
    default void rideStarted(org.example.goeverywhere.protocol.grpc.RideStartedRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRideStartedMethod(), responseObserver);
    }

    /**
     */
    default void rideCompleted(org.example.goeverywhere.protocol.grpc.RideCompletedRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRideCompletedMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service DriverService.
   */
  public static abstract class DriverServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return DriverServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service DriverService.
   */
  public static final class DriverServiceStub
      extends io.grpc.stub.AbstractAsyncStub<DriverServiceStub> {
    private DriverServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DriverServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DriverServiceStub(channel, callOptions);
    }

    /**
     */
    public void subscribeForRideEvents(org.example.goeverywhere.protocol.grpc.SubscribeForRideEventsRequest request,
        io.grpc.stub.StreamObserver<org.example.goeverywhere.protocol.grpc.DriverEvent> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getSubscribeForRideEventsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void acceptRide(org.example.goeverywhere.protocol.grpc.AcceptRideRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAcceptRideMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void rejectRide(org.example.goeverywhere.protocol.grpc.RejectRideRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRejectRideMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void driverArrived(org.example.goeverywhere.protocol.grpc.DriverArrivedRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDriverArrivedMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void rideStarted(org.example.goeverywhere.protocol.grpc.RideStartedRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRideStartedMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void rideCompleted(org.example.goeverywhere.protocol.grpc.RideCompletedRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRideCompletedMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service DriverService.
   */
  public static final class DriverServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<DriverServiceBlockingStub> {
    private DriverServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DriverServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DriverServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public java.util.Iterator<org.example.goeverywhere.protocol.grpc.DriverEvent> subscribeForRideEvents(
        org.example.goeverywhere.protocol.grpc.SubscribeForRideEventsRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getSubscribeForRideEventsMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty acceptRide(org.example.goeverywhere.protocol.grpc.AcceptRideRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAcceptRideMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty rejectRide(org.example.goeverywhere.protocol.grpc.RejectRideRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRejectRideMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty driverArrived(org.example.goeverywhere.protocol.grpc.DriverArrivedRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDriverArrivedMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty rideStarted(org.example.goeverywhere.protocol.grpc.RideStartedRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRideStartedMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty rideCompleted(org.example.goeverywhere.protocol.grpc.RideCompletedRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRideCompletedMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service DriverService.
   */
  public static final class DriverServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<DriverServiceFutureStub> {
    private DriverServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DriverServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DriverServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> acceptRide(
        org.example.goeverywhere.protocol.grpc.AcceptRideRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAcceptRideMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> rejectRide(
        org.example.goeverywhere.protocol.grpc.RejectRideRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRejectRideMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> driverArrived(
        org.example.goeverywhere.protocol.grpc.DriverArrivedRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDriverArrivedMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> rideStarted(
        org.example.goeverywhere.protocol.grpc.RideStartedRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRideStartedMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> rideCompleted(
        org.example.goeverywhere.protocol.grpc.RideCompletedRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRideCompletedMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SUBSCRIBE_FOR_RIDE_EVENTS = 0;
  private static final int METHODID_ACCEPT_RIDE = 1;
  private static final int METHODID_REJECT_RIDE = 2;
  private static final int METHODID_DRIVER_ARRIVED = 3;
  private static final int METHODID_RIDE_STARTED = 4;
  private static final int METHODID_RIDE_COMPLETED = 5;

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
        case METHODID_SUBSCRIBE_FOR_RIDE_EVENTS:
          serviceImpl.subscribeForRideEvents((org.example.goeverywhere.protocol.grpc.SubscribeForRideEventsRequest) request,
              (io.grpc.stub.StreamObserver<org.example.goeverywhere.protocol.grpc.DriverEvent>) responseObserver);
          break;
        case METHODID_ACCEPT_RIDE:
          serviceImpl.acceptRide((org.example.goeverywhere.protocol.grpc.AcceptRideRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_REJECT_RIDE:
          serviceImpl.rejectRide((org.example.goeverywhere.protocol.grpc.RejectRideRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_DRIVER_ARRIVED:
          serviceImpl.driverArrived((org.example.goeverywhere.protocol.grpc.DriverArrivedRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_RIDE_STARTED:
          serviceImpl.rideStarted((org.example.goeverywhere.protocol.grpc.RideStartedRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_RIDE_COMPLETED:
          serviceImpl.rideCompleted((org.example.goeverywhere.protocol.grpc.RideCompletedRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
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
          getSubscribeForRideEventsMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              org.example.goeverywhere.protocol.grpc.SubscribeForRideEventsRequest,
              org.example.goeverywhere.protocol.grpc.DriverEvent>(
                service, METHODID_SUBSCRIBE_FOR_RIDE_EVENTS)))
        .addMethod(
          getAcceptRideMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.example.goeverywhere.protocol.grpc.AcceptRideRequest,
              com.google.protobuf.Empty>(
                service, METHODID_ACCEPT_RIDE)))
        .addMethod(
          getRejectRideMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.example.goeverywhere.protocol.grpc.RejectRideRequest,
              com.google.protobuf.Empty>(
                service, METHODID_REJECT_RIDE)))
        .addMethod(
          getDriverArrivedMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.example.goeverywhere.protocol.grpc.DriverArrivedRequest,
              com.google.protobuf.Empty>(
                service, METHODID_DRIVER_ARRIVED)))
        .addMethod(
          getRideStartedMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.example.goeverywhere.protocol.grpc.RideStartedRequest,
              com.google.protobuf.Empty>(
                service, METHODID_RIDE_STARTED)))
        .addMethod(
          getRideCompletedMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.example.goeverywhere.protocol.grpc.RideCompletedRequest,
              com.google.protobuf.Empty>(
                service, METHODID_RIDE_COMPLETED)))
        .build();
  }

  private static abstract class DriverServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    DriverServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.example.goeverywhere.protocol.grpc.Services.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("DriverService");
    }
  }

  private static final class DriverServiceFileDescriptorSupplier
      extends DriverServiceBaseDescriptorSupplier {
    DriverServiceFileDescriptorSupplier() {}
  }

  private static final class DriverServiceMethodDescriptorSupplier
      extends DriverServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    DriverServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (DriverServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new DriverServiceFileDescriptorSupplier())
              .addMethod(getSubscribeForRideEventsMethod())
              .addMethod(getAcceptRideMethod())
              .addMethod(getRejectRideMethod())
              .addMethod(getDriverArrivedMethod())
              .addMethod(getRideStartedMethod())
              .addMethod(getRideCompletedMethod())
              .build();
        }
      }
    }
    return result;
  }
}
