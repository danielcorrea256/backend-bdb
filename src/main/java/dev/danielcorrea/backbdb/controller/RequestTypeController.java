package dev.danielcorrea.backbdb.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.danielcorrea.backbdb.dto.RequestTypeDTO;
import dev.danielcorrea.backbdb.service.RequestTypeService;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for request type operations.
 */
@RestController
@RequestMapping("/api/request-types")
@RequiredArgsConstructor
public class RequestTypeController {

    private final RequestTypeService requestTypeService;

    /**
     * Retrieves all request types from the database.
     * Used by frontend to populate dropdowns for selecting request types.
     * 
     * @return List of RequestTypeDTO
     */
    @GetMapping
    public ResponseEntity<List<RequestTypeDTO>> getAllRequestTypes() {
        try {
            List<RequestTypeDTO> requestTypes = requestTypeService.getAllRequestTypes();
            return ResponseEntity.ok(requestTypes);
        } catch (Exception e) {
            // Log the error (you can use a logger here)
            // Return 500 Internal Server Error if something goes wrong
            return ResponseEntity.internalServerError().build();
        }
    }
}
