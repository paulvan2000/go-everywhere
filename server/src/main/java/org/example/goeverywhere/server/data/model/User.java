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
     * The role of the user (e.g., buyer or seller).
     */
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType userType;

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
        this.userType = userType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
