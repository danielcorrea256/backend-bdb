package dev.danielcorrea.backbdb.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.danielcorrea.backbdb.dto.CreateRequestDTO;
import dev.danielcorrea.backbdb.dto.RequestActionDTO;
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

    /**
     * Approves a request.
     * 
     * @param id The ID of the request to approve
     * @param actionDTO The RequestActionDTO containing comments and approver ID
     * @return ResponseEntity with the approved RequestSummaryDTO
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<RequestSummaryDTO> approveRequest(
            @PathVariable("id") UUID id,
            @RequestBody RequestActionDTO actionDTO) {
        
        try {
            RequestSummaryDTO approvedRequest = approvalService.approveRequest(
                id, 
                actionDTO.getComments(), 
                actionDTO.getApproverId()
            );
            return ResponseEntity.ok(approvedRequest);
        } catch (RuntimeException e) {
            String message = e.getMessage();
            
            // Return appropriate error responses
            if (message.contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (message.contains("not in PENDING status")) {
                return ResponseEntity.badRequest().build();
            } else if (message.contains("not authorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Rejects a request.
     * 
     * @param id The ID of the request to reject
     * @param actionDTO The RequestActionDTO containing comments and approver ID
     * @return ResponseEntity with the rejected RequestSummaryDTO
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<RequestSummaryDTO> rejectRequest(
            @PathVariable("id") UUID id,
            @RequestBody RequestActionDTO actionDTO) {
        
        try {
            RequestSummaryDTO rejectedRequest = approvalService.rejectRequest(
                id, 
                actionDTO.getComments(), 
                actionDTO.getApproverId()
            );
            return ResponseEntity.ok(rejectedRequest);
        } catch (RuntimeException e) {
            String message = e.getMessage();
            
            // Return appropriate error responses
            if (message.contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (message.contains("not in PENDING status")) {
                return ResponseEntity.badRequest().build();
            } else if (message.contains("not authorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Fetches full details of a specific request by its ID.
     * 
     * @param id The ID of the request
     * @return ResponseEntity with RequestDetailsDTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<dev.danielcorrea.backbdb.dto.RequestDetailsDTO> getRequestDetails(
            @PathVariable("id") UUID id) {
        
        try {
            dev.danielcorrea.backbdb.dto.RequestDetailsDTO requestDetails = 
                approvalService.getRequestDetails(id);
            return ResponseEntity.ok(requestDetails);
        } catch (RuntimeException e) {
            // Return 404 if request not found
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().build();
        }
    }
}
