// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: services.proto

package org.example.goeverywhere.protocol.grpc;

public interface RideCompletedRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:org.example.goeverywhere.protocol.grpc.RideCompletedRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string session_id = 1;</code>
   * @return The sessionId.
   */
  java.lang.String getSessionId();
  /**
   * <code>string session_id = 1;</code>
   * @return The bytes for sessionId.
   */
  com.google.protobuf.ByteString
      getSessionIdBytes();

  /**
   * <code>string ride_id = 2;</code>
   * @return The rideId.
   */
  java.lang.String getRideId();
  /**
   * <code>string ride_id = 2;</code>
   * @return The bytes for rideId.
   */
  com.google.protobuf.ByteString
      getRideIdBytes();
}
