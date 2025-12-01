package dev.danielcorrea.backbdb.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.danielcorrea.backbdb.dto.CreateRequestDTO;
import dev.danielcorrea.backbdb.dto.RequestSummaryDTO;
import dev.danielcorrea.backbdb.model.ApprovalRequest;
import dev.danielcorrea.backbdb.model.RequestStatus;
import dev.danielcorrea.backbdb.model.RequestType;
import dev.danielcorrea.backbdb.model.User;
import dev.danielcorrea.backbdb.repository.ApprovalRequestRepository;
import dev.danielcorrea.backbdb.repository.RequestTypeRepository;
import dev.danielcorrea.backbdb.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * Service layer for approval request business logic.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApprovalService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final UserRepository userRepository;
    private final RequestTypeRepository requestTypeRepository;

    /**
     * Retrieves all requests CREATED BY the user (for "My Requests" tab).
     * The relatedUserName will be the APPROVER's name.
     * 
     * @param userId The ID of the requester
     * @return List of RequestSummaryDTO
     */
    public List<RequestSummaryDTO> getRequestsCreatedByUser(Long userId) {
        List<ApprovalRequest> requests = approvalRequestRepository
            .findByRequester_IdOrderByCreatedAtDesc(userId);
        
        return requests.stream()
            .map(this::mapToDTOForCreated)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves all requests ASSIGNED TO the user for approval (for "My Inbox" tab).
     * The relatedUserName will be the REQUESTER's name.
     * 
     * @param userId The ID of the approver
     * @return List of RequestSummaryDTO
     */
    public List<RequestSummaryDTO> getRequestsAssignedToUser(Long userId) {
        List<ApprovalRequest> requests = approvalRequestRepository
            .findByApprover_IdOrderByCreatedAtDesc(userId);
        
        return requests.stream()
            .map(this::mapToDTOForAssigned)
            .collect(Collectors.toList());
    }

    /**
     * Maps an ApprovalRequest to DTO for "My Requests" view.
     * Related user = Approver (the person who will approve my request).
     */
    private RequestSummaryDTO mapToDTOForCreated(ApprovalRequest request) {
        return new RequestSummaryDTO(
            request.getId(),
            request.getTitle(),
            request.getStatus().name(),
            request.getType().getName(),
            request.getCreatedAt(),
            request.getApprover() != null ? request.getApprover().getFullName() : "Unassigned"
        );
    }

    /**
     * Maps an ApprovalRequest to DTO for "My Inbox" view.
     * Related user = Requester (the person who sent me the request).
     */
    private RequestSummaryDTO mapToDTOForAssigned(ApprovalRequest request) {
        return new RequestSummaryDTO(
            request.getId(),
            request.getTitle(),
            request.getStatus().name(),
            request.getType().getName(),
            request.getCreatedAt(),
            request.getRequester().getFullName()
        );
    }

    /**
     * Creates a new approval request.
     * 
     * @param dto The CreateRequestDTO containing request details
     * @return RequestSummaryDTO of the created request
     * @throws RuntimeException if requester, approver, or request type not found
     */
    @Transactional
    public RequestSummaryDTO createRequest(CreateRequestDTO dto) {
        // Validate requester exists
        User requester = userRepository.findById(dto.getRequesterId())
            .orElseThrow(() -> new RuntimeException(
                "Requester not found with ID: " + dto.getRequesterId()));

        // Validate approver exists
        User approver = userRepository.findById(dto.getApproverId())
            .orElseThrow(() -> new RuntimeException(
                "Approver not found with ID: " + dto.getApproverId()));

        // Validate request type exists
        RequestType requestType = requestTypeRepository.findById(dto.getRequestTypeId())
            .orElseThrow(() -> new RuntimeException(
                "Request type not found with ID: " + dto.getRequestTypeId()));

        // Create and save the approval request
        ApprovalRequest request = ApprovalRequest.builder()
            .title(dto.getTitle())
            .description(dto.getDescription())
            .status(RequestStatus.PENDING)
            .requester(requester)
            .approver(approver)
            .type(requestType)
            .build();

        ApprovalRequest savedRequest = approvalRequestRepository.save(request);

        // Return as DTO (from requester's perspective)
        return mapToDTOForCreated(savedRequest);
    }
}
