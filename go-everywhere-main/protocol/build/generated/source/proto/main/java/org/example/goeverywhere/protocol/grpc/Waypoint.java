// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: services.proto

package org.example.goeverywhere.protocol.grpc;

/**
 * Protobuf type {@code org.example.goeverywhere.protocol.grpc.Waypoint}
 */
public final class Waypoint extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:org.example.goeverywhere.protocol.grpc.Waypoint)
    WaypointOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Waypoint.newBuilder() to construct.
  private Waypoint(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Waypoint() {
    waypointMetadata_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Waypoint();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private Waypoint(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    int mutable_bitField0_ = 0;
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
            com.google.type.LatLng.Builder subBuilder = null;
            if (location_ != null) {
              subBuilder = location_.toBuilder();
            }
            location_ = input.readMessage(com.google.type.LatLng.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(location_);
              location_ = subBuilder.buildPartial();
            }

            break;
          }
          case 17: {

            distanceFromStartKm_ = input.readDouble();
            break;
          }
          case 25: {

            durationFromStartMin_ = input.readDouble();
            break;
          }
          case 34: {
            if (!((mutable_bitField0_ & 0x00000001) != 0)) {
              waypointMetadata_ = new java.util.ArrayList<org.example.goeverywhere.protocol.grpc.WaypointMetadata>();
              mutable_bitField0_ |= 0x00000001;
            }
            waypointMetadata_.add(
                input.readMessage(org.example.goeverywhere.protocol.grpc.WaypointMetadata.parser(), extensionRegistry));
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
      if (((mutable_bitField0_ & 0x00000001) != 0)) {
        waypointMetadata_ = java.util.Collections.unmodifiableList(waypointMetadata_);
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return org.example.goeverywhere.protocol.grpc.Services.internal_static_org_example_goeverywhere_protocol_grpc_Waypoint_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.example.goeverywhere.protocol.grpc.Services.internal_static_org_example_goeverywhere_protocol_grpc_Waypoint_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.example.goeverywhere.protocol.grpc.Waypoint.class, org.example.goeverywhere.protocol.grpc.Waypoint.Builder.class);
  }

  public static final int LOCATION_FIELD_NUMBER = 1;
  private com.google.type.LatLng location_;
  /**
   * <code>.google.type.LatLng location = 1;</code>
   * @return Whether the location field is set.
   */
  @java.lang.Override
  public boolean hasLocation() {
    return location_ != null;
  }
  /**
   * <code>.google.type.LatLng location = 1;</code>
   * @return The location.
   */
  @java.lang.Override
  public com.google.type.LatLng getLocation() {
    return location_ == null ? com.google.type.LatLng.getDefaultInstance() : location_;
  }
  /**
   * <code>.google.type.LatLng location = 1;</code>
   */
  @java.lang.Override
  public com.google.type.LatLngOrBuilder getLocationOrBuilder() {
    return getLocation();
  }

  public static final int DISTANCE_FROM_START_KM_FIELD_NUMBER = 2;
  private double distanceFromStartKm_;
  /**
   * <code>double distance_from_start_km = 2;</code>
   * @return The distanceFromStartKm.
   */
  @java.lang.Override
  public double getDistanceFromStartKm() {
    return distanceFromStartKm_;
  }

  public static final int DURATION_FROM_START_MIN_FIELD_NUMBER = 3;
  private double durationFromStartMin_;
  /**
   * <code>double duration_from_start_min = 3;</code>
   * @return The durationFromStartMin.
   */
  @java.lang.Override
  public double getDurationFromStartMin() {
    return durationFromStartMin_;
  }

  public static final int WAYPOINTMETADATA_FIELD_NUMBER = 4;
  private java.util.List<org.example.goeverywhere.protocol.grpc.WaypointMetadata> waypointMetadata_;
  /**
   * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
   */
  @java.lang.Override
  public java.util.List<org.example.goeverywhere.protocol.grpc.WaypointMetadata> getWaypointMetadataList() {
    return waypointMetadata_;
  }
  /**
   * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
   */
  @java.lang.Override
  public java.util.List<? extends org.example.goeverywhere.protocol.grpc.WaypointMetadataOrBuilder> 
      getWaypointMetadataOrBuilderList() {
    return waypointMetadata_;
  }
  /**
   * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
   */
  @java.lang.Override
  public int getWaypointMetadataCount() {
    return waypointMetadata_.size();
  }
  /**
   * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
   */
  @java.lang.Override
  public org.example.goeverywhere.protocol.grpc.WaypointMetadata getWaypointMetadata(int index) {
    return waypointMetadata_.get(index);
  }
  /**
   * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
   */
  @java.lang.Override
  public org.example.goeverywhere.protocol.grpc.WaypointMetadataOrBuilder getWaypointMetadataOrBuilder(
      int index) {
    return waypointMetadata_.get(index);
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
    if (location_ != null) {
      output.writeMessage(1, getLocation());
    }
    if (java.lang.Double.doubleToRawLongBits(distanceFromStartKm_) != 0) {
      output.writeDouble(2, distanceFromStartKm_);
    }
    if (java.lang.Double.doubleToRawLongBits(durationFromStartMin_) != 0) {
      output.writeDouble(3, durationFromStartMin_);
    }
    for (int i = 0; i < waypointMetadata_.size(); i++) {
      output.writeMessage(4, waypointMetadata_.get(i));
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (location_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getLocation());
    }
    if (java.lang.Double.doubleToRawLongBits(distanceFromStartKm_) != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeDoubleSize(2, distanceFromStartKm_);
    }
    if (java.lang.Double.doubleToRawLongBits(durationFromStartMin_) != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeDoubleSize(3, durationFromStartMin_);
    }
    for (int i = 0; i < waypointMetadata_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(4, waypointMetadata_.get(i));
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
    if (!(obj instanceof org.example.goeverywhere.protocol.grpc.Waypoint)) {
      return super.equals(obj);
    }
    org.example.goeverywhere.protocol.grpc.Waypoint other = (org.example.goeverywhere.protocol.grpc.Waypoint) obj;

    if (hasLocation() != other.hasLocation()) return false;
    if (hasLocation()) {
      if (!getLocation()
          .equals(other.getLocation())) return false;
    }
    if (java.lang.Double.doubleToLongBits(getDistanceFromStartKm())
        != java.lang.Double.doubleToLongBits(
            other.getDistanceFromStartKm())) return false;
    if (java.lang.Double.doubleToLongBits(getDurationFromStartMin())
        != java.lang.Double.doubleToLongBits(
            other.getDurationFromStartMin())) return false;
    if (!getWaypointMetadataList()
        .equals(other.getWaypointMetadataList())) return false;
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
    if (hasLocation()) {
      hash = (37 * hash) + LOCATION_FIELD_NUMBER;
      hash = (53 * hash) + getLocation().hashCode();
    }
    hash = (37 * hash) + DISTANCE_FROM_START_KM_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        java.lang.Double.doubleToLongBits(getDistanceFromStartKm()));
    hash = (37 * hash) + DURATION_FROM_START_MIN_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        java.lang.Double.doubleToLongBits(getDurationFromStartMin()));
    if (getWaypointMetadataCount() > 0) {
      hash = (37 * hash) + WAYPOINTMETADATA_FIELD_NUMBER;
      hash = (53 * hash) + getWaypointMetadataList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.example.goeverywhere.protocol.grpc.Waypoint parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.example.goeverywhere.protocol.grpc.Waypoint parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.example.goeverywhere.protocol.grpc.Waypoint parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.example.goeverywhere.protocol.grpc.Waypoint parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.example.goeverywhere.protocol.grpc.Waypoint parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.example.goeverywhere.protocol.grpc.Waypoint parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.example.goeverywhere.protocol.grpc.Waypoint parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.example.goeverywhere.protocol.grpc.Waypoint parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.example.goeverywhere.protocol.grpc.Waypoint parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static org.example.goeverywhere.protocol.grpc.Waypoint parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.example.goeverywhere.protocol.grpc.Waypoint parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.example.goeverywhere.protocol.grpc.Waypoint parseFrom(
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
  public static Builder newBuilder(org.example.goeverywhere.protocol.grpc.Waypoint prototype) {
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
   * Protobuf type {@code org.example.goeverywhere.protocol.grpc.Waypoint}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:org.example.goeverywhere.protocol.grpc.Waypoint)
      org.example.goeverywhere.protocol.grpc.WaypointOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.example.goeverywhere.protocol.grpc.Services.internal_static_org_example_goeverywhere_protocol_grpc_Waypoint_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.example.goeverywhere.protocol.grpc.Services.internal_static_org_example_goeverywhere_protocol_grpc_Waypoint_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.example.goeverywhere.protocol.grpc.Waypoint.class, org.example.goeverywhere.protocol.grpc.Waypoint.Builder.class);
    }

    // Construct using org.example.goeverywhere.protocol.grpc.Waypoint.newBuilder()
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
        getWaypointMetadataFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      if (locationBuilder_ == null) {
        location_ = null;
      } else {
        location_ = null;
        locationBuilder_ = null;
      }
      distanceFromStartKm_ = 0D;

      durationFromStartMin_ = 0D;

      if (waypointMetadataBuilder_ == null) {
        waypointMetadata_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
      } else {
        waypointMetadataBuilder_.clear();
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.example.goeverywhere.protocol.grpc.Services.internal_static_org_example_goeverywhere_protocol_grpc_Waypoint_descriptor;
    }

    @java.lang.Override
    public org.example.goeverywhere.protocol.grpc.Waypoint getDefaultInstanceForType() {
      return org.example.goeverywhere.protocol.grpc.Waypoint.getDefaultInstance();
    }

    @java.lang.Override
    public org.example.goeverywhere.protocol.grpc.Waypoint build() {
      org.example.goeverywhere.protocol.grpc.Waypoint result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public org.example.goeverywhere.protocol.grpc.Waypoint buildPartial() {
      org.example.goeverywhere.protocol.grpc.Waypoint result = new org.example.goeverywhere.protocol.grpc.Waypoint(this);
      int from_bitField0_ = bitField0_;
      if (locationBuilder_ == null) {
        result.location_ = location_;
      } else {
        result.location_ = locationBuilder_.build();
      }
      result.distanceFromStartKm_ = distanceFromStartKm_;
      result.durationFromStartMin_ = durationFromStartMin_;
      if (waypointMetadataBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          waypointMetadata_ = java.util.Collections.unmodifiableList(waypointMetadata_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.waypointMetadata_ = waypointMetadata_;
      } else {
        result.waypointMetadata_ = waypointMetadataBuilder_.build();
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
      if (other instanceof org.example.goeverywhere.protocol.grpc.Waypoint) {
        return mergeFrom((org.example.goeverywhere.protocol.grpc.Waypoint)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.example.goeverywhere.protocol.grpc.Waypoint other) {
      if (other == org.example.goeverywhere.protocol.grpc.Waypoint.getDefaultInstance()) return this;
      if (other.hasLocation()) {
        mergeLocation(other.getLocation());
      }
      if (other.getDistanceFromStartKm() != 0D) {
        setDistanceFromStartKm(other.getDistanceFromStartKm());
      }
      if (other.getDurationFromStartMin() != 0D) {
        setDurationFromStartMin(other.getDurationFromStartMin());
      }
      if (waypointMetadataBuilder_ == null) {
        if (!other.waypointMetadata_.isEmpty()) {
          if (waypointMetadata_.isEmpty()) {
            waypointMetadata_ = other.waypointMetadata_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureWaypointMetadataIsMutable();
            waypointMetadata_.addAll(other.waypointMetadata_);
          }
          onChanged();
        }
      } else {
        if (!other.waypointMetadata_.isEmpty()) {
          if (waypointMetadataBuilder_.isEmpty()) {
            waypointMetadataBuilder_.dispose();
            waypointMetadataBuilder_ = null;
            waypointMetadata_ = other.waypointMetadata_;
            bitField0_ = (bitField0_ & ~0x00000001);
            waypointMetadataBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getWaypointMetadataFieldBuilder() : null;
          } else {
            waypointMetadataBuilder_.addAllMessages(other.waypointMetadata_);
          }
        }
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
      org.example.goeverywhere.protocol.grpc.Waypoint parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (org.example.goeverywhere.protocol.grpc.Waypoint) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private com.google.type.LatLng location_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.type.LatLng, com.google.type.LatLng.Builder, com.google.type.LatLngOrBuilder> locationBuilder_;
    /**
     * <code>.google.type.LatLng location = 1;</code>
     * @return Whether the location field is set.
     */
    public boolean hasLocation() {
      return locationBuilder_ != null || location_ != null;
    }
    /**
     * <code>.google.type.LatLng location = 1;</code>
     * @return The location.
     */
    public com.google.type.LatLng getLocation() {
      if (locationBuilder_ == null) {
        return location_ == null ? com.google.type.LatLng.getDefaultInstance() : location_;
      } else {
        return locationBuilder_.getMessage();
      }
    }
    /**
     * <code>.google.type.LatLng location = 1;</code>
     */
    public Builder setLocation(com.google.type.LatLng value) {
      if (locationBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        location_ = value;
        onChanged();
      } else {
        locationBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.google.type.LatLng location = 1;</code>
     */
    public Builder setLocation(
        com.google.type.LatLng.Builder builderForValue) {
      if (locationBuilder_ == null) {
        location_ = builderForValue.build();
        onChanged();
      } else {
        locationBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.google.type.LatLng location = 1;</code>
     */
    public Builder mergeLocation(com.google.type.LatLng value) {
      if (locationBuilder_ == null) {
        if (location_ != null) {
          location_ =
            com.google.type.LatLng.newBuilder(location_).mergeFrom(value).buildPartial();
        } else {
          location_ = value;
        }
        onChanged();
      } else {
        locationBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.google.type.LatLng location = 1;</code>
     */
    public Builder clearLocation() {
      if (locationBuilder_ == null) {
        location_ = null;
        onChanged();
      } else {
        location_ = null;
        locationBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.google.type.LatLng location = 1;</code>
     */
    public com.google.type.LatLng.Builder getLocationBuilder() {
      
      onChanged();
      return getLocationFieldBuilder().getBuilder();
    }
    /**
     * <code>.google.type.LatLng location = 1;</code>
     */
    public com.google.type.LatLngOrBuilder getLocationOrBuilder() {
      if (locationBuilder_ != null) {
        return locationBuilder_.getMessageOrBuilder();
      } else {
        return location_ == null ?
            com.google.type.LatLng.getDefaultInstance() : location_;
      }
    }
    /**
     * <code>.google.type.LatLng location = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.type.LatLng, com.google.type.LatLng.Builder, com.google.type.LatLngOrBuilder> 
        getLocationFieldBuilder() {
      if (locationBuilder_ == null) {
        locationBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.google.type.LatLng, com.google.type.LatLng.Builder, com.google.type.LatLngOrBuilder>(
                getLocation(),
                getParentForChildren(),
                isClean());
        location_ = null;
      }
      return locationBuilder_;
    }

    private double distanceFromStartKm_ ;
    /**
     * <code>double distance_from_start_km = 2;</code>
     * @return The distanceFromStartKm.
     */
    @java.lang.Override
    public double getDistanceFromStartKm() {
      return distanceFromStartKm_;
    }
    /**
     * <code>double distance_from_start_km = 2;</code>
     * @param value The distanceFromStartKm to set.
     * @return This builder for chaining.
     */
    public Builder setDistanceFromStartKm(double value) {
      
      distanceFromStartKm_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>double distance_from_start_km = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearDistanceFromStartKm() {
      
      distanceFromStartKm_ = 0D;
      onChanged();
      return this;
    }

    private double durationFromStartMin_ ;
    /**
     * <code>double duration_from_start_min = 3;</code>
     * @return The durationFromStartMin.
     */
    @java.lang.Override
    public double getDurationFromStartMin() {
      return durationFromStartMin_;
    }
    /**
     * <code>double duration_from_start_min = 3;</code>
     * @param value The durationFromStartMin to set.
     * @return This builder for chaining.
     */
    public Builder setDurationFromStartMin(double value) {
      
      durationFromStartMin_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>double duration_from_start_min = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearDurationFromStartMin() {
      
      durationFromStartMin_ = 0D;
      onChanged();
      return this;
    }

    private java.util.List<org.example.goeverywhere.protocol.grpc.WaypointMetadata> waypointMetadata_ =
      java.util.Collections.emptyList();
    private void ensureWaypointMetadataIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        waypointMetadata_ = new java.util.ArrayList<org.example.goeverywhere.protocol.grpc.WaypointMetadata>(waypointMetadata_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        org.example.goeverywhere.protocol.grpc.WaypointMetadata, org.example.goeverywhere.protocol.grpc.WaypointMetadata.Builder, org.example.goeverywhere.protocol.grpc.WaypointMetadataOrBuilder> waypointMetadataBuilder_;

    /**
     * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
     */
    public java.util.List<org.example.goeverywhere.protocol.grpc.WaypointMetadata> getWaypointMetadataList() {
      if (waypointMetadataBuilder_ == null) {
        return java.util.Collections.unmodifiableList(waypointMetadata_);
      } else {
        return waypointMetadataBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
     */
    public int getWaypointMetadataCount() {
      if (waypointMetadataBuilder_ == null) {
        return waypointMetadata_.size();
      } else {
        return waypointMetadataBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
     */
    public org.example.goeverywhere.protocol.grpc.WaypointMetadata getWaypointMetadata(int index) {
      if (waypointMetadataBuilder_ == null) {
        return waypointMetadata_.get(index);
      } else {
        return waypointMetadataBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
     */
    public Builder setWaypointMetadata(
        int index, org.example.goeverywhere.protocol.grpc.WaypointMetadata value) {
      if (waypointMetadataBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureWaypointMetadataIsMutable();
        waypointMetadata_.set(index, value);
        onChanged();
      } else {
        waypointMetadataBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
     */
    public Builder setWaypointMetadata(
        int index, org.example.goeverywhere.protocol.grpc.WaypointMetadata.Builder builderForValue) {
      if (waypointMetadataBuilder_ == null) {
        ensureWaypointMetadataIsMutable();
        waypointMetadata_.set(index, builderForValue.build());
        onChanged();
      } else {
        waypointMetadataBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
     */
    public Builder addWaypointMetadata(org.example.goeverywhere.protocol.grpc.WaypointMetadata value) {
      if (waypointMetadataBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureWaypointMetadataIsMutable();
        waypointMetadata_.add(value);
        onChanged();
      } else {
        waypointMetadataBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
     */
    public Builder addWaypointMetadata(
        int index, org.example.goeverywhere.protocol.grpc.WaypointMetadata value) {
      if (waypointMetadataBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureWaypointMetadataIsMutable();
        waypointMetadata_.add(index, value);
        onChanged();
      } else {
        waypointMetadataBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
     */
    public Builder addWaypointMetadata(
        org.example.goeverywhere.protocol.grpc.WaypointMetadata.Builder builderForValue) {
      if (waypointMetadataBuilder_ == null) {
        ensureWaypointMetadataIsMutable();
        waypointMetadata_.add(builderForValue.build());
        onChanged();
      } else {
        waypointMetadataBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
     */
    public Builder addWaypointMetadata(
        int index, org.example.goeverywhere.protocol.grpc.WaypointMetadata.Builder builderForValue) {
      if (waypointMetadataBuilder_ == null) {
        ensureWaypointMetadataIsMutable();
        waypointMetadata_.add(index, builderForValue.build());
        onChanged();
      } else {
        waypointMetadataBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
     */
    public Builder addAllWaypointMetadata(
        java.lang.Iterable<? extends org.example.goeverywhere.protocol.grpc.WaypointMetadata> values) {
      if (waypointMetadataBuilder_ == null) {
        ensureWaypointMetadataIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, waypointMetadata_);
        onChanged();
      } else {
        waypointMetadataBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
     */
    public Builder clearWaypointMetadata() {
      if (waypointMetadataBuilder_ == null) {
        waypointMetadata_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        waypointMetadataBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
     */
    public Builder removeWaypointMetadata(int index) {
      if (waypointMetadataBuilder_ == null) {
        ensureWaypointMetadataIsMutable();
        waypointMetadata_.remove(index);
        onChanged();
      } else {
        waypointMetadataBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
     */
    public org.example.goeverywhere.protocol.grpc.WaypointMetadata.Builder getWaypointMetadataBuilder(
        int index) {
      return getWaypointMetadataFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
     */
    public org.example.goeverywhere.protocol.grpc.WaypointMetadataOrBuilder getWaypointMetadataOrBuilder(
        int index) {
      if (waypointMetadataBuilder_ == null) {
        return waypointMetadata_.get(index);  } else {
        return waypointMetadataBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
     */
    public java.util.List<? extends org.example.goeverywhere.protocol.grpc.WaypointMetadataOrBuilder> 
         getWaypointMetadataOrBuilderList() {
      if (waypointMetadataBuilder_ != null) {
        return waypointMetadataBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(waypointMetadata_);
      }
    }
    /**
     * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
     */
    public org.example.goeverywhere.protocol.grpc.WaypointMetadata.Builder addWaypointMetadataBuilder() {
      return getWaypointMetadataFieldBuilder().addBuilder(
          org.example.goeverywhere.protocol.grpc.WaypointMetadata.getDefaultInstance());
    }
    /**
     * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
     */
    public org.example.goeverywhere.protocol.grpc.WaypointMetadata.Builder addWaypointMetadataBuilder(
        int index) {
      return getWaypointMetadataFieldBuilder().addBuilder(
          index, org.example.goeverywhere.protocol.grpc.WaypointMetadata.getDefaultInstance());
    }
    /**
     * <code>repeated .org.example.goeverywhere.protocol.grpc.WaypointMetadata waypointMetadata = 4;</code>
     */
    public java.util.List<org.example.goeverywhere.protocol.grpc.WaypointMetadata.Builder> 
         getWaypointMetadataBuilderList() {
      return getWaypointMetadataFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        org.example.goeverywhere.protocol.grpc.WaypointMetadata, org.example.goeverywhere.protocol.grpc.WaypointMetadata.Builder, org.example.goeverywhere.protocol.grpc.WaypointMetadataOrBuilder> 
        getWaypointMetadataFieldBuilder() {
      if (waypointMetadataBuilder_ == null) {
        waypointMetadataBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            org.example.goeverywhere.protocol.grpc.WaypointMetadata, org.example.goeverywhere.protocol.grpc.WaypointMetadata.Builder, org.example.goeverywhere.protocol.grpc.WaypointMetadataOrBuilder>(
                waypointMetadata_,
                ((bitField0_ & 0x00000001) != 0),
                getParentForChildren(),
                isClean());
        waypointMetadata_ = null;
      }
      return waypointMetadataBuilder_;
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


    // @@protoc_insertion_point(builder_scope:org.example.goeverywhere.protocol.grpc.Waypoint)
  }

  // @@protoc_insertion_point(class_scope:org.example.goeverywhere.protocol.grpc.Waypoint)
  private static final org.example.goeverywhere.protocol.grpc.Waypoint DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.example.goeverywhere.protocol.grpc.Waypoint();
  }

  public static org.example.goeverywhere.protocol.grpc.Waypoint getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Waypoint>
      PARSER = new com.google.protobuf.AbstractParser<Waypoint>() {
    @java.lang.Override
    public Waypoint parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new Waypoint(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<Waypoint> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Waypoint> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public org.example.goeverywhere.protocol.grpc.Waypoint getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

