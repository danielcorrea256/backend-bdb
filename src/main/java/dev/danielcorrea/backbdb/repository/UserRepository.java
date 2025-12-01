package dev.danielcorrea.backbdb.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.danielcorrea.backbdb.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Custom query method to find user by username
    Optional<User> findByUsername(String username);
    
    // Custom query method to find user by email
    Optional<User> findByEmail(String email);
    
    // Check if username exists
    boolean existsByUsername(String username);
}
