package org.example.goeverywhere.server.data.model;

import jakarta.persistence.*;
import org.example.goeverywhere.protocol.grpc.UserType;

import java.util.UUID;

/**
 * Represents a user in the system.
 */
@Entity
@Table(
        name = "user",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"email"})}
)
public class User {
    /**
     * Unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * The email of the user used as the login.
     */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * The password of the user.
     */
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    /**
     * The role of the user (driver or rider).
     * Important: This is stored as a STRING in the database but corresponds to proto enum values:
     * "DRIVER" (proto value 0)
     * "RIDER" (proto value 1)
     */
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType userType = UserType.RIDER; // Default to RIDER

    // Custom callbacks to validate the user type
    @PrePersist
    @PreUpdate
    private void validateUserType() {
        if (userType == null) {
            System.out.println("WARNING: userType was null, defaulting to RIDER");
            userType = UserType.RIDER;
        }
        
        // Log what's being saved
        String typeStr = (userType == UserType.DRIVER) ? "DRIVER" : "RIDER";
        int typeValue = (userType == UserType.DRIVER) ? 0 : 1;
        System.out.println("Validating user type before save: " + typeStr + " (value: " + typeValue + ")");
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String login) {
        this.email = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        if (userType == null) {
            System.out.println("WARNING: Attempted to set null userType, using RIDER instead");
            this.userType = UserType.RIDER;
            return;
        }
        
        if (userType == UserType.DRIVER) {
            System.out.println("Setting user type to DRIVER (explicit)");
        } else if (userType == UserType.RIDER) {
            System.out.println("Setting user type to RIDER (explicit)");
        } else {
            System.out.println("WARNING: Unknown user type value: " + userType + ", defaulting to RIDER");
            this.userType = UserType.RIDER;
            return;
        }
        
        this.userType = userType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", userType=" + userType +
                '}';
    }
}
