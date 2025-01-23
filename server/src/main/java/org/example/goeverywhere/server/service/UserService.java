package org.example.goeverywhere.server.service;

import jakarta.transaction.Transactional;
import org.example.goeverywhere.protocol.grpc.LoginRequest;
import org.example.goeverywhere.protocol.grpc.LoginResponse;
import org.example.goeverywhere.protocol.grpc.SignUpRequest;
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
    private org.example.goeverywhere.server.service.SessionStore sessionStore;

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
        var userOpt = userRepository.findByEmail(request.getEmail());
        var rawPassword = request.getPassword();
        if (userOpt.isPresent()) {
            // Validate userOpt credentials
            var encodedPassword = userOpt.get().getPassword();
            if (passwordEncoder.matches(rawPassword, encodedPassword)) {
                // Generate a new session token
                var sessionId = UUID.randomUUID();
                sessionStore.registerSession(sessionId, userOpt.get());

                // Send the session token back to the client
                return LoginResponse.newBuilder().setSessionId(sessionId.toString()).setUserType(userOpt.get().getUserType()).build();
            } else {
                throw new FailedAuthenticationException();
            }
        }
        throw new FailedAuthenticationException();
    }
    /**
     * Handles user sign-up by saving user details in the database.
     *
     * @param request the sign-up request containing user information.
     * @throws Exception if an error occurs while saving the user details.
     */
    @Transactional
    public void signUp(SignUpRequest request) {
        System.out.println("Request received from client:\n" + request);
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserType(request.getUserType());
        userRepository.save(user);
    }


}