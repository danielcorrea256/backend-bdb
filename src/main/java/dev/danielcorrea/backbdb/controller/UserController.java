package dev.danielcorrea.backbdb.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.danielcorrea.backbdb.dto.UserDTO;
import dev.danielcorrea.backbdb.service.UserService;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for user operations.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Retrieves all users in the system.
     * Used by frontend to populate dropdowns for selecting approvers/requesters.
     * 
     * @return List of UserDTO
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            List<UserDTO> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            // Log the error (you can use a logger here)
            // Return 500 Internal Server Error if something goes wrong
            return ResponseEntity.internalServerError().build();
        }
    }
}
