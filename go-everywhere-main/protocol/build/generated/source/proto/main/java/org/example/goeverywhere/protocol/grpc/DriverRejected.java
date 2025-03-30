// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: services.proto

package org.example.goeverywhere.protocol.grpc;

/**
 * Protobuf type {@code org.example.goeverywhere.protocol.grpc.DriverRejected}
 */
public final class DriverRejected extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:org.example.goeverywhere.protocol.grpc.DriverRejected)
    DriverRejectedOrBuilder {
private static final long serialVersionUID = 0L;
  // Use DriverRejected.newBuilder() to construct.
  private DriverRejected(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private DriverRejected() {
    rideId_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new DriverRejected();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private DriverRejected(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 10: {
            java.lang.String s = input.readStringRequireUtf8();

            rideId_ = s;
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return org.example.goeverywhere.protocol.grpc.Services.internal_static_org_example_goeverywhere_protocol_grpc_DriverRejected_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.example.goeverywhere.protocol.grpc.Services.internal_static_org_example_goeverywhere_protocol_grpc_DriverRejected_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.example.goeverywhere.protocol.grpc.DriverRejected.class, org.example.goeverywhere.protocol.grpc.DriverRejected.Builder.class);
  }

  public static final int RIDE_ID_FIELD_NUMBER = 1;
  private volatile java.lang.Object rideId_;
  /**
   * <code>string ride_id = 1;</code>
   * @return The rideId.
   */
  @java.lang.Override
  public java.lang.String getRideId() {
    java.lang.Object ref = rideId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      rideId_ = s;
      return s;
    }
  }
  /**
   * <code>string ride_id = 1;</code>
   * @return The bytes for rideId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getRideIdBytes() {
    java.lang.Object ref = rideId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      rideId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(rideId_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, rideId_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(rideId_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, rideId_);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof org.example.goeverywhere.protocol.grpc.DriverRejected)) {
      return super.equals(obj);
    }
    org.example.goeverywhere.protocol.grpc.DriverRejected other = (org.example.goeverywhere.protocol.grpc.DriverRejected) obj;

    if (!getRideId()
        .equals(other.getRideId())) return false;
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + RIDE_ID_FIELD_NUMBER;
    hash = (53 * hash) + getRideId().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.example.goeverywhere.protocol.grpc.DriverRejected parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.example.goeverywhere.protocol.grpc.DriverRejected parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.example.goeverywhere.protocol.grpc.DriverRejected parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.example.goeverywhere.protocol.grpc.DriverRejected parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.example.goeverywhere.protocol.grpc.DriverRejected parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.example.goeverywhere.protocol.grpc.DriverRejected parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.example.goeverywhere.protocol.grpc.DriverRejected parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.example.goeverywhere.protocol.grpc.DriverRejected parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.example.goeverywhere.protocol.grpc.DriverRejected parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static org.example.goeverywhere.protocol.grpc.DriverRejected parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.example.goeverywhere.protocol.grpc.DriverRejected parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.example.goeverywhere.protocol.grpc.DriverRejected parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(org.example.goeverywhere.protocol.grpc.DriverRejected prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code org.example.goeverywhere.protocol.grpc.DriverRejected}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:org.example.goeverywhere.protocol.grpc.DriverRejected)
      org.example.goeverywhere.protocol.grpc.DriverRejectedOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.example.goeverywhere.protocol.grpc.Services.internal_static_org_example_goeverywhere_protocol_grpc_DriverRejected_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.example.goeverywhere.protocol.grpc.Services.internal_static_org_example_goeverywhere_protocol_grpc_DriverRejected_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.example.goeverywhere.protocol.grpc.DriverRejected.class, org.example.goeverywhere.protocol.grpc.DriverRejected.Builder.class);
    }

    // Construct using org.example.goeverywhere.protocol.grpc.DriverRejected.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      rideId_ = "";

      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.example.goeverywhere.protocol.grpc.Services.internal_static_org_example_goeverywhere_protocol_grpc_DriverRejected_descriptor;
    }

    @java.lang.Override
    public org.example.goeverywhere.protocol.grpc.DriverRejected getDefaultInstanceForType() {
      return org.example.goeverywhere.protocol.grpc.DriverRejected.getDefaultInstance();
    }

    @java.lang.Override
    public org.example.goeverywhere.protocol.grpc.DriverRejected build() {
      org.example.goeverywhere.protocol.grpc.DriverRejected result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public org.example.goeverywhere.protocol.grpc.DriverRejected buildPartial() {
      org.example.goeverywhere.protocol.grpc.DriverRejected result = new org.example.goeverywhere.protocol.grpc.DriverRejected(this);
      result.rideId_ = rideId_;
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof org.example.goeverywhere.protocol.grpc.DriverRejected) {
        return mergeFrom((org.example.goeverywhere.protocol.grpc.DriverRejected)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.example.goeverywhere.protocol.grpc.DriverRejected other) {
      if (other == org.example.goeverywhere.protocol.grpc.DriverRejected.getDefaultInstance()) return this;
      if (!other.getRideId().isEmpty()) {
        rideId_ = other.rideId_;
        onChanged();
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      org.example.goeverywhere.protocol.grpc.DriverRejected parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (org.example.goeverywhere.protocol.grpc.DriverRejected) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object rideId_ = "";
    /**
     * <code>string ride_id = 1;</code>
     * @return The rideId.
     */
    public java.lang.String getRideId() {
      java.lang.Object ref = rideId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        rideId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string ride_id = 1;</code>
     * @return The bytes for rideId.
     */
    public com.google.protobuf.ByteString
        getRideIdBytes() {
      java.lang.Object ref = rideId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        rideId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string ride_id = 1;</code>
     * @param value The rideId to set.
     * @return This builder for chaining.
     */
    public Builder setRideId(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      rideId_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string ride_id = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearRideId() {
      
      rideId_ = getDefaultInstance().getRideId();
      onChanged();
      return this;
    }
    /**
     * <code>string ride_id = 1;</code>
     * @param value The bytes for rideId to set.
     * @return This builder for chaining.
     */
    public Builder setRideIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      rideId_ = value;
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:org.example.goeverywhere.protocol.grpc.DriverRejected)
  }

  // @@protoc_insertion_point(class_scope:org.example.goeverywhere.protocol.grpc.DriverRejected)
  private static final org.example.goeverywhere.protocol.grpc.DriverRejected DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.example.goeverywhere.protocol.grpc.DriverRejected();
  }

  public static org.example.goeverywhere.protocol.grpc.DriverRejected getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<DriverRejected>
      PARSER = new com.google.protobuf.AbstractParser<DriverRejected>() {
    @java.lang.Override
    public DriverRejected parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new DriverRejected(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<DriverRejected> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<DriverRejected> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public org.example.goeverywhere.protocol.grpc.DriverRejected getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

