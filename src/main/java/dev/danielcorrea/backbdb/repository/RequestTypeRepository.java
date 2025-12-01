package dev.danielcorrea.backbdb.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.danielcorrea.backbdb.model.RequestType;

@Repository
public interface RequestTypeRepository extends JpaRepository<RequestType, Integer> {
    
    // Custom query method to find request type by name
    Optional<RequestType> findByName(String name);
    
    // Check if request type name exists
    boolean existsByName(String name);
}
