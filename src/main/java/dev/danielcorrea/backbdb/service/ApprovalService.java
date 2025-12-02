package dev.danielcorrea.backbdb.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.danielcorrea.backbdb.dto.CreateRequestDTO;
import dev.danielcorrea.backbdb.dto.RequestSummaryDTO;
import dev.danielcorrea.backbdb.model.ApprovalRequest;
import dev.danielcorrea.backbdb.model.RequestLog;
import dev.danielcorrea.backbdb.model.RequestStatus;
import dev.danielcorrea.backbdb.model.RequestType;
import dev.danielcorrea.backbdb.model.User;
import dev.danielcorrea.backbdb.repository.ApprovalRequestRepository;
import dev.danielcorrea.backbdb.repository.RequestLogRepository;
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
    private final RequestLogRepository requestLogRepository;
    private final EmailNotificationService emailNotificationService;

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
        approvalRequestRepository.flush(); // Ensure timestamps are set by Hibernate

        // Send email notification to approver
        emailNotificationService.sendRequestCreatedNotification(savedRequest, approver);

        // Return as DTO (from requester's perspective)
        return mapToDTOForCreated(savedRequest);
    }

    /**
     * Approves a request.
     * 
     * @param requestId The ID of the request to approve
     * @param comments Optional comments from the approver
     * @param approverId The ID of the user approving the request
     * @return RequestSummaryDTO of the approved request
     * @throws RuntimeException if request not found, not pending, or wrong approver
     */
    @Transactional
    public RequestSummaryDTO approveRequest(UUID requestId, String comments, Long approverId) {
        // Fetch the request
        ApprovalRequest request = approvalRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found with ID: " + requestId));

        // Validate request is in PENDING status
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request is not in PENDING status. Current status: " + request.getStatus());
        }

        // Validate the approver is the assigned approver
        if (!request.getApprover().getId().equals(approverId)) {
            throw new RuntimeException("User with ID " + approverId + " is not authorized to approve this request");
        }

        // Update request status
        request.setStatus(RequestStatus.APPROVED);
        ApprovalRequest updatedRequest = approvalRequestRepository.save(request);

        // Create log entry
        User approver = userRepository.findById(approverId)
            .orElseThrow(() -> new RuntimeException("Approver not found with ID: " + approverId));

        RequestLog log = RequestLog.builder()
            .actionTaken("APPROVED")
            .comments(comments)
            .request(request)
            .user(approver)
            .build();

        requestLogRepository.save(log);

        // Send email notification to requester
        emailNotificationService.sendRequestStatusUpdateNotification(updatedRequest, approver, comments);

        // Return as DTO (from requester's perspective)
        return mapToDTOForCreated(updatedRequest);
    }

    /**
     * Rejects a request.
     * 
     * @param requestId The ID of the request to reject
     * @param comments Optional comments from the approver
     * @param approverId The ID of the user rejecting the request
     * @return RequestSummaryDTO of the rejected request
     * @throws RuntimeException if request not found, not pending, or wrong approver
     */
    @Transactional
    public RequestSummaryDTO rejectRequest(UUID requestId, String comments, Long approverId) {
        // Fetch the request
        ApprovalRequest request = approvalRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found with ID: " + requestId));

        // Validate request is in PENDING status
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request is not in PENDING status. Current status: " + request.getStatus());
        }

        // Validate the approver is the assigned approver
        if (!request.getApprover().getId().equals(approverId)) {
            throw new RuntimeException("User with ID " + approverId + " is not authorized to reject this request");
        }

        // Update request status
        request.setStatus(RequestStatus.REJECTED);
        ApprovalRequest updatedRequest = approvalRequestRepository.save(request);

        // Create log entry
        User approver = userRepository.findById(approverId)
            .orElseThrow(() -> new RuntimeException("Approver not found with ID: " + approverId));

        RequestLog log = RequestLog.builder()
            .actionTaken("REJECTED")
            .comments(comments)
            .request(request)
            .user(approver)
            .build();

        requestLogRepository.save(log);

        // Send email notification to requester
        emailNotificationService.sendRequestStatusUpdateNotification(updatedRequest, approver, comments);

        // Return as DTO (from requester's perspective)
        return mapToDTOForCreated(updatedRequest);
    }

    /**
     * Fetches full details of a specific request by its ID.
     * 
     * @param requestId The ID of the request
     * @return RequestDetailsDTO containing full request details
     * @throws RuntimeException if request not found
     */
    @Transactional(readOnly = true)
    public dev.danielcorrea.backbdb.dto.RequestDetailsDTO getRequestDetails(UUID requestId) {
        // Fetch the request
        ApprovalRequest request = approvalRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found with ID: " + requestId));

        // Fetch the most recent comment from approval history
        List<RequestLog> logs = requestLogRepository.findByRequestOrderByActionDateDesc(request);
        String mostRecentComment = logs.isEmpty() ? null : logs.get(0).getComments();

        // Determine related user name (approver's name for this view)
        String relatedUserName = request.getApprover() != null 
            ? request.getApprover().getFullName() 
            : "Unassigned";

        // Map to DTO
        return new dev.danielcorrea.backbdb.dto.RequestDetailsDTO(
            request.getId(),
            request.getTitle(),
            request.getDescription(),
            request.getStatus().name(),
            request.getType().getName(),
            request.getCreatedAt(),
            relatedUserName,
            mostRecentComment
        );
    }
}
