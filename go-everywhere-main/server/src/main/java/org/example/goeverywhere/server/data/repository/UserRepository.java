package org.example.goeverywhere.server.data.repository;


import org.example.goeverywhere.server.data.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for accessing User entities from the database.
 */
public interface UserRepository extends CrudRepository<User, UUID> {
    /**
     * Finds a user by their login.
     *
     * @param email the email of the user
     * @return an optional containing the user if found, or empty if not found.
     */
    Optional<User> findByEmail(String email);
}
