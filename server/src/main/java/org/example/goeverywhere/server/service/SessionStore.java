package org.example.goeverywhere.server.service;

import org.example.goeverywhere.protocol.grpc.UserType;
import org.example.goeverywhere.server.data.model.User;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service class for managing session storage operations.
 */
@Component
public class SessionStore {
    private final Map<UUID, User> sessionStore = new ConcurrentHashMap<>();

    /**
     * Registers a session for the specified user.
     *
     * @param user the user for whom the session is to be registered.
     * @return the UUID of the newly registered session.
     */
    public void registerSession(UUID sessionId, User user) {
        sessionStore.put(sessionId, user);
    }


    /**
     * Retrieves the role associated with the specified session ID.
     *
     * @param sessionId the session ID to look up.
     * @return the role associated with the session ID, or null if no such role exists.
     */
    public UserType getUserType(UUID sessionId) {
        User user = sessionStore.get(sessionId);
        return user.getUserType();

    }

    /**
     * Retrieves the user associated with the specified session ID.
     *
     * @param sessionId the session ID to look up.
     * @return the user associated with the session ID, or null if no such user exists.
     */
    public User getUser(UUID sessionId) {
        return sessionStore.get(sessionId);

    }
}
