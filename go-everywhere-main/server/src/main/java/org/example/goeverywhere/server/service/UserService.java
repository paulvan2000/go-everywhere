package org.example.goeverywhere.server.service;

import jakarta.transaction.Transactional;
import org.example.goeverywhere.protocol.grpc.LoginRequest;
import org.example.goeverywhere.protocol.grpc.LoginResponse;
import org.example.goeverywhere.protocol.grpc.SignUpRequest;
import org.example.goeverywhere.protocol.grpc.UserType;
import org.example.goeverywhere.server.data.model.User;
import org.example.goeverywhere.server.data.repository.UserRepository;
import org.example.goeverywhere.server.exceptions.FailedAuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SessionStore sessionStore;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Handles user login by validating credentials and generating a session ID.
     *
     * @param request the login request containing user credentials.
     * @return the login response containing the session ID.
     * @throws FailedAuthenticationException if the login credentials are invalid.
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            System.out.println("Login attempt: " + request.getEmail());
            
            var userOpt = userRepository.findByEmail(request.getEmail());
            if (!userOpt.isPresent()) {
                System.out.println("User not found: " + request.getEmail());
                throw new FailedAuthenticationException();
            }
            
            var user = userOpt.get();
            System.out.println("Found user: " + user.getEmail() + ", userType: " + user.getUserType());
            
            var rawPassword = request.getPassword();
            var encodedPassword = user.getPassword();
            
            if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
                System.out.println("Password mismatch for user: " + request.getEmail());
                throw new FailedAuthenticationException();
            }
            
            // Generate a new session token
            var sessionId = UUID.randomUUID();
            sessionStore.registerSession(sessionId.toString(), user);
            System.out.println("Created session: " + sessionId + " for user: " + user.getEmail());

            // Get the user type for the response
            UserType userType = user.getUserType();
            String typeStr = (userType == UserType.DRIVER) ? "DRIVER" : "RIDER";
            int typeValue = (userType == UserType.DRIVER) ? 0 : 1;
            System.out.println("User type for response: " + typeStr + " (value: " + typeValue + ")");
            
            // Build the login response
            LoginResponse.Builder responseBuilder = LoginResponse.newBuilder()
                .setSessionId(sessionId.toString());
            
            // Set the user type in the response
            responseBuilder.setUserType(userType);
            
            LoginResponse response = responseBuilder.build();
            System.out.println("Response built with userType: " + response.getUserType() + " (value: " + response.getUserTypeValue() + ")");
            
            return response;
        } catch (FailedAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error during login: " + e.getMessage());
            e.printStackTrace();
            throw new FailedAuthenticationException();
        }
    }
    
    /**
     * Handles user sign-up by saving user details in the database.
     *
     * @param request the sign-up request containing user information.
     * @throws Exception if an error occurs while saving the user details.
     */
    @Transactional
    public void signUp(SignUpRequest request) {
        try {
            System.out.println("Sign-up request received from client");
            System.out.println("User email: " + request.getEmail());
            
            // Get and validate user type
            UserType userType = request.getUserType();
            int userTypeValue = request.getUserTypeValue();
            String userTypeStr = (userType == UserType.DRIVER) ? "DRIVER" : "RIDER";
            
            System.out.println("User type from request: " + userTypeStr + " (value: " + userTypeValue + ")");
            
            // Create user entity
            User user = new User();
            user.setEmail(request.getEmail());
            user.setName(request.getName());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            
            // Set user type explicitly based on the request value
            if (userTypeValue == 0) {
                user.setUserType(UserType.DRIVER);
                System.out.println("Setting user type to DRIVER (value: 0)");
            } else {
                user.setUserType(UserType.RIDER);
                System.out.println("Setting user type to RIDER (value: 1)");
            }
            
            // Save the user
            User savedUser = userRepository.save(user);
            System.out.println("User saved successfully: " + savedUser);
            
            // Verify the saved user type
            System.out.println("Verifying saved user type: " + savedUser.getUserType() + 
                " (" + (savedUser.getUserType() == UserType.DRIVER ? "DRIVER (0)" : "RIDER (1)") + ")");
            
        } catch (Exception e) {
            System.err.println("Error saving user: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}