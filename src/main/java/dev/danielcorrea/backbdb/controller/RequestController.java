package dev.danielcorrea.backbdb.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.danielcorrea.backbdb.dto.CreateRequestDTO;
import dev.danielcorrea.backbdb.dto.RequestSummaryDTO;
import dev.danielcorrea.backbdb.service.ApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for approval request operations.
 */
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final ApprovalService approvalService;

    /**
     * Hello endpoint for testing.
     */
    @GetMapping("/hello")
    public String hello() {
        return "Hello from the Spring Boot API!";
    }

    /**
     * Retrieves all requests CREATED BY the user (for "My Requests" tab).
     * Shows requests where the user is the REQUESTER.
     * 
     * @param userId The ID of the user who created the requests
     * @return List of RequestSummaryDTO with approver names
     */
    @GetMapping("/created/{userId}")
    public ResponseEntity<List<RequestSummaryDTO>> getRequestsCreatedByUser(
            @PathVariable("userId") Long userId) {
        
        List<RequestSummaryDTO> requests = approvalService.getRequestsCreatedByUser(userId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Retrieves all requests ASSIGNED TO the user for approval (for "My Inbox" tab).
     * Shows requests where the user is the APPROVER.
     * 
     * @param userId The ID of the user assigned to approve
     * @return List of RequestSummaryDTO with requester names
     */
    @GetMapping("/assigned/{userId}")
    public ResponseEntity<List<RequestSummaryDTO>> getRequestsAssignedToUser(
            @PathVariable("userId") Long userId) {
        
        List<RequestSummaryDTO> requests = approvalService.getRequestsAssignedToUser(userId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Creates a new approval request.
     * 
     * @param dto The CreateRequestDTO containing request details
     * @return ResponseEntity with the created RequestSummaryDTO
     */
    @PostMapping
    public ResponseEntity<RequestSummaryDTO> createRequest(
            @Valid @RequestBody CreateRequestDTO dto) {
        
        try {
            RequestSummaryDTO createdRequest = approvalService.createRequest(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
        } catch (RuntimeException e) {
            // Return 404 if user or request type not found
            return ResponseEntity.notFound().build();
        }
    }
}
