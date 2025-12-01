package dev.danielcorrea.backbdb.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.danielcorrea.backbdb.dto.UserDTO;
import dev.danielcorrea.backbdb.model.User;
import dev.danielcorrea.backbdb.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * Service layer for user-related business logic.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * Retrieves all users from the database.
     * 
     * @return List of UserDTO containing all users
     */
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        return users.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Maps a User entity to UserDTO.
     * 
     * @param user The User entity
     * @return UserDTO
     */
    private UserDTO mapToDTO(User user) {
        return new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getEmail()
        );
    }
}
