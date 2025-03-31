// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: services.proto

package org.example.goeverywhere.protocol.grpc;

/**
 * Protobuf type {@code org.example.goeverywhere.protocol.grpc.RideDetails}
 */
public final class RideDetails extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:org.example.goeverywhere.protocol.grpc.RideDetails)
    RideDetailsOrBuilder {
private static final long serialVersionUID = 0L;
  // Use RideDetails.newBuilder() to construct.
  private RideDetails(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private RideDetails() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new RideDetails();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private RideDetails(
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
            org.example.goeverywhere.protocol.grpc.Route.Builder subBuilder = null;
            if (newFullRoute_ != null) {
              subBuilder = newFullRoute_.toBuilder();
            }
            newFullRoute_ = input.readMessage(org.example.goeverywhere.protocol.grpc.Route.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(newFullRoute_);
              newFullRoute_ = subBuilder.buildPartial();
            }

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
    return org.example.goeverywhere.protocol.grpc.Services.internal_static_org_example_goeverywhere_protocol_grpc_RideDetails_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.example.goeverywhere.protocol.grpc.Services.internal_static_org_example_goeverywhere_protocol_grpc_RideDetails_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.example.goeverywhere.protocol.grpc.RideDetails.class, org.example.goeverywhere.protocol.grpc.RideDetails.Builder.class);
  }

  public static final int NEW_FULL_ROUTE_FIELD_NUMBER = 1;
  private org.example.goeverywhere.protocol.grpc.Route newFullRoute_;
  /**
   * <code>.org.example.goeverywhere.protocol.grpc.Route new_full_route = 1;</code>
   * @return Whether the newFullRoute field is set.
   */
  @java.lang.Override
  public boolean hasNewFullRoute() {
    return newFullRoute_ != null;
  }
  /**
   * <code>.org.example.goeverywhere.protocol.grpc.Route new_full_route = 1;</code>
   * @return The newFullRoute.
   */
  @java.lang.Override
  public org.example.goeverywhere.protocol.grpc.Route getNewFullRoute() {
    return newFullRoute_ == null ? org.example.goeverywhere.protocol.grpc.Route.getDefaultInstance() : newFullRoute_;
  }
  /**
   * <code>.org.example.goeverywhere.protocol.grpc.Route new_full_route = 1;</code>
   */
  @java.lang.Override
  public org.example.goeverywhere.protocol.grpc.RouteOrBuilder getNewFullRouteOrBuilder() {
    return getNewFullRoute();
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
    if (newFullRoute_ != null) {
      output.writeMessage(1, getNewFullRoute());
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (newFullRoute_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getNewFullRoute());
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
    if (!(obj instanceof org.example.goeverywhere.protocol.grpc.RideDetails)) {
      return super.equals(obj);
    }
    org.example.goeverywhere.protocol.grpc.RideDetails other = (org.example.goeverywhere.protocol.grpc.RideDetails) obj;

    if (hasNewFullRoute() != other.hasNewFullRoute()) return false;
    if (hasNewFullRoute()) {
      if (!getNewFullRoute()
          .equals(other.getNewFullRoute())) return false;
    }
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
    if (hasNewFullRoute()) {
      hash = (37 * hash) + NEW_FULL_ROUTE_FIELD_NUMBER;
      hash = (53 * hash) + getNewFullRoute().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.example.goeverywhere.protocol.grpc.RideDetails parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.example.goeverywhere.protocol.grpc.RideDetails parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.example.goeverywhere.protocol.grpc.RideDetails parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.example.goeverywhere.protocol.grpc.RideDetails parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.example.goeverywhere.protocol.grpc.RideDetails parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.example.goeverywhere.protocol.grpc.RideDetails parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.example.goeverywhere.protocol.grpc.RideDetails parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.example.goeverywhere.protocol.grpc.RideDetails parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.example.goeverywhere.protocol.grpc.RideDetails parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static org.example.goeverywhere.protocol.grpc.RideDetails parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.example.goeverywhere.protocol.grpc.RideDetails parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.example.goeverywhere.protocol.grpc.RideDetails parseFrom(
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
  public static Builder newBuilder(org.example.goeverywhere.protocol.grpc.RideDetails prototype) {
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
   * Protobuf type {@code org.example.goeverywhere.protocol.grpc.RideDetails}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:org.example.goeverywhere.protocol.grpc.RideDetails)
      org.example.goeverywhere.protocol.grpc.RideDetailsOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.example.goeverywhere.protocol.grpc.Services.internal_static_org_example_goeverywhere_protocol_grpc_RideDetails_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.example.goeverywhere.protocol.grpc.Services.internal_static_org_example_goeverywhere_protocol_grpc_RideDetails_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.example.goeverywhere.protocol.grpc.RideDetails.class, org.example.goeverywhere.protocol.grpc.RideDetails.Builder.class);
    }

    // Construct using org.example.goeverywhere.protocol.grpc.RideDetails.newBuilder()
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
      if (newFullRouteBuilder_ == null) {
        newFullRoute_ = null;
      } else {
        newFullRoute_ = null;
        newFullRouteBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.example.goeverywhere.protocol.grpc.Services.internal_static_org_example_goeverywhere_protocol_grpc_RideDetails_descriptor;
    }

    @java.lang.Override
    public org.example.goeverywhere.protocol.grpc.RideDetails getDefaultInstanceForType() {
      return org.example.goeverywhere.protocol.grpc.RideDetails.getDefaultInstance();
    }

    @java.lang.Override
    public org.example.goeverywhere.protocol.grpc.RideDetails build() {
      org.example.goeverywhere.protocol.grpc.RideDetails result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public org.example.goeverywhere.protocol.grpc.RideDetails buildPartial() {
      org.example.goeverywhere.protocol.grpc.RideDetails result = new org.example.goeverywhere.protocol.grpc.RideDetails(this);
      if (newFullRouteBuilder_ == null) {
        result.newFullRoute_ = newFullRoute_;
      } else {
        result.newFullRoute_ = newFullRouteBuilder_.build();
      }
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
      if (other instanceof org.example.goeverywhere.protocol.grpc.RideDetails) {
        return mergeFrom((org.example.goeverywhere.protocol.grpc.RideDetails)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.example.goeverywhere.protocol.grpc.RideDetails other) {
      if (other == org.example.goeverywhere.protocol.grpc.RideDetails.getDefaultInstance()) return this;
      if (other.hasNewFullRoute()) {
        mergeNewFullRoute(other.getNewFullRoute());
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
      org.example.goeverywhere.protocol.grpc.RideDetails parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (org.example.goeverywhere.protocol.grpc.RideDetails) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private org.example.goeverywhere.protocol.grpc.Route newFullRoute_;
    private com.google.protobuf.SingleFieldBuilderV3<
        org.example.goeverywhere.protocol.grpc.Route, org.example.goeverywhere.protocol.grpc.Route.Builder, org.example.goeverywhere.protocol.grpc.RouteOrBuilder> newFullRouteBuilder_;
    /**
     * <code>.org.example.goeverywhere.protocol.grpc.Route new_full_route = 1;</code>
     * @return Whether the newFullRoute field is set.
     */
    public boolean hasNewFullRoute() {
      return newFullRouteBuilder_ != null || newFullRoute_ != null;
    }
    /**
     * <code>.org.example.goeverywhere.protocol.grpc.Route new_full_route = 1;</code>
     * @return The newFullRoute.
     */
    public org.example.goeverywhere.protocol.grpc.Route getNewFullRoute() {
      if (newFullRouteBuilder_ == null) {
        return newFullRoute_ == null ? org.example.goeverywhere.protocol.grpc.Route.getDefaultInstance() : newFullRoute_;
      } else {
        return newFullRouteBuilder_.getMessage();
      }
    }
    /**
     * <code>.org.example.goeverywhere.protocol.grpc.Route new_full_route = 1;</code>
     */
    public Builder setNewFullRoute(org.example.goeverywhere.protocol.grpc.Route value) {
      if (newFullRouteBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        newFullRoute_ = value;
        onChanged();
      } else {
        newFullRouteBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.org.example.goeverywhere.protocol.grpc.Route new_full_route = 1;</code>
     */
    public Builder setNewFullRoute(
        org.example.goeverywhere.protocol.grpc.Route.Builder builderForValue) {
      if (newFullRouteBuilder_ == null) {
        newFullRoute_ = builderForValue.build();
        onChanged();
      } else {
        newFullRouteBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.org.example.goeverywhere.protocol.grpc.Route new_full_route = 1;</code>
     */
    public Builder mergeNewFullRoute(org.example.goeverywhere.protocol.grpc.Route value) {
      if (newFullRouteBuilder_ == null) {
        if (newFullRoute_ != null) {
          newFullRoute_ =
            org.example.goeverywhere.protocol.grpc.Route.newBuilder(newFullRoute_).mergeFrom(value).buildPartial();
        } else {
          newFullRoute_ = value;
        }
        onChanged();
      } else {
        newFullRouteBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.org.example.goeverywhere.protocol.grpc.Route new_full_route = 1;</code>
     */
    public Builder clearNewFullRoute() {
      if (newFullRouteBuilder_ == null) {
        newFullRoute_ = null;
        onChanged();
      } else {
        newFullRoute_ = null;
        newFullRouteBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.org.example.goeverywhere.protocol.grpc.Route new_full_route = 1;</code>
     */
    public org.example.goeverywhere.protocol.grpc.Route.Builder getNewFullRouteBuilder() {
      
      onChanged();
      return getNewFullRouteFieldBuilder().getBuilder();
    }
    /**
     * <code>.org.example.goeverywhere.protocol.grpc.Route new_full_route = 1;</code>
     */
    public org.example.goeverywhere.protocol.grpc.RouteOrBuilder getNewFullRouteOrBuilder() {
      if (newFullRouteBuilder_ != null) {
        return newFullRouteBuilder_.getMessageOrBuilder();
      } else {
        return newFullRoute_ == null ?
            org.example.goeverywhere.protocol.grpc.Route.getDefaultInstance() : newFullRoute_;
      }
    }
    /**
     * <code>.org.example.goeverywhere.protocol.grpc.Route new_full_route = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        org.example.goeverywhere.protocol.grpc.Route, org.example.goeverywhere.protocol.grpc.Route.Builder, org.example.goeverywhere.protocol.grpc.RouteOrBuilder> 
        getNewFullRouteFieldBuilder() {
      if (newFullRouteBuilder_ == null) {
        newFullRouteBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            org.example.goeverywhere.protocol.grpc.Route, org.example.goeverywhere.protocol.grpc.Route.Builder, org.example.goeverywhere.protocol.grpc.RouteOrBuilder>(
                getNewFullRoute(),
                getParentForChildren(),
                isClean());
        newFullRoute_ = null;
      }
      return newFullRouteBuilder_;
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


    // @@protoc_insertion_point(builder_scope:org.example.goeverywhere.protocol.grpc.RideDetails)
  }

  // @@protoc_insertion_point(class_scope:org.example.goeverywhere.protocol.grpc.RideDetails)
  private static final org.example.goeverywhere.protocol.grpc.RideDetails DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.example.goeverywhere.protocol.grpc.RideDetails();
  }

  public static org.example.goeverywhere.protocol.grpc.RideDetails getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<RideDetails>
      PARSER = new com.google.protobuf.AbstractParser<RideDetails>() {
    @java.lang.Override
    public RideDetails parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new RideDetails(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<RideDetails> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<RideDetails> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public org.example.goeverywhere.protocol.grpc.RideDetails getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

