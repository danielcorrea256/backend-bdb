package dev.danielcorrea.backbdb.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

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

/**
 * Unit tests for ApprovalService.
 * Tests core business logic, data validation, and error handling.
 */
@ExtendWith(MockitoExtension.class)
class ApprovalServiceUnitTest {

    @Mock
    private ApprovalRequestRepository approvalRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RequestTypeRepository requestTypeRepository;

    @Mock
    private RequestLogRepository requestLogRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private ApprovalService approvalService;

    private User requester;
    private User approver;
    private RequestType requestType;
    private ApprovalRequest pendingRequest;
    private ApprovalRequest approvedRequest;
    private ApprovalRequest rejectedRequest;
    private CreateRequestDTO validCreateDTO;

    @BeforeEach
    void setUp() {
        // Setup test users
        requester = User.builder()
                .id(1L)
                .username("requester_user")
                .fullName("Test Requester")
                .email("requester@test.com")
                .build();

        approver = User.builder()
                .id(2L)
                .username("approver_user")
                .fullName("Test Approver")
                .email("approver@test.com")
                .build();

        // Setup request type
        requestType = RequestType.builder()
                .id(1)
                .name("ACCESS")
                .description("Access request")
                .build();

        // Setup test requests
        pendingRequest = ApprovalRequest.builder()
                .id(UUID.randomUUID())
                .title("Test Request")
                .description("Test Description")
                .status(RequestStatus.PENDING)
                .requester(requester)
                .approver(approver)
                .type(requestType)
                .build();

        approvedRequest = ApprovalRequest.builder()
                .id(UUID.randomUUID())
                .title("Approved Request")
                .description("Already approved")
                .status(RequestStatus.APPROVED)
                .requester(requester)
                .approver(approver)
                .type(requestType)
                .build();

        rejectedRequest = ApprovalRequest.builder()
                .id(UUID.randomUUID())
                .title("Rejected Request")
                .description("Already rejected")
                .status(RequestStatus.REJECTED)
                .requester(requester)
                .approver(approver)
                .type(requestType)
                .build();

        // Setup valid DTO
        validCreateDTO = new CreateRequestDTO(
                "New Request",
                "Please approve this",
                1L,
                2L,
                1
        );
    }

    // ==================== REQUEST CREATION TESTS ====================

    @Test
    void testCreateRequest_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(userRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(requestTypeRepository.findById(1)).thenReturn(Optional.of(requestType));
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenReturn(pendingRequest);

        // Act
        RequestSummaryDTO result = approvalService.createRequest(validCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Test Request", result.title());
        assertEquals("PENDING", result.status());
        verify(approvalRequestRepository, times(1)).save(any(ApprovalRequest.class));
        verify(emailNotificationService, times(1)).sendRequestCreatedNotification(any(), any());
    }

    @Test
    void testCreateRequest_RequesterNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> approvalService.createRequest(validCreateDTO));
        
        assertTrue(exception.getMessage().contains("Requester not found"));
        verify(approvalRequestRepository, never()).save(any());
    }

    @Test
    void testCreateRequest_ApproverNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> approvalService.createRequest(validCreateDTO));
        
        assertTrue(exception.getMessage().contains("Approver not found"));
        verify(approvalRequestRepository, never()).save(any());
    }

    @Test
    void testCreateRequest_RequestTypeNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(userRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(requestTypeRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> approvalService.createRequest(validCreateDTO));
        
        assertTrue(exception.getMessage().contains("Request type not found"));
        verify(approvalRequestRepository, never()).save(any());
    }

    // ==================== REQUEST APPROVAL TESTS ====================

    @Test
    void testApproveRequest_Success() {
        // Arrange
        UUID requestId = pendingRequest.getId();
        when(approvalRequestRepository.findById(requestId)).thenReturn(Optional.of(pendingRequest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenReturn(pendingRequest);

        // Act
        RequestSummaryDTO result = approvalService.approveRequest(requestId, "Looks good!", 2L);

        // Assert
        assertNotNull(result);
        assertEquals(RequestStatus.APPROVED, pendingRequest.getStatus());
        verify(requestLogRepository, times(1)).save(any(RequestLog.class));
        verify(emailNotificationService, times(1)).sendRequestStatusUpdateNotification(any(), any(), any());
    }

    @Test
    void testApproveRequest_RequestNotFound() {
        // Arrange
        UUID requestId = UUID.randomUUID();
        when(approvalRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> approvalService.approveRequest(requestId, "Approved", 2L));
        
        assertTrue(exception.getMessage().contains("Request not found"));
    }

    @Test
    void testApproveRequest_RequestNotPending() {
        // Arrange
        UUID requestId = approvedRequest.getId();
        when(approvalRequestRepository.findById(requestId)).thenReturn(Optional.of(approvedRequest));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> approvalService.approveRequest(requestId, "Approved", 2L));
        
        assertTrue(exception.getMessage().contains("not in PENDING status"));
    }

    @Test
    void testApproveRequest_UnauthorizedApprover() {
        // Arrange
        UUID requestId = pendingRequest.getId();
        Long wrongApproverId = 999L;
        when(approvalRequestRepository.findById(requestId)).thenReturn(Optional.of(pendingRequest));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> approvalService.approveRequest(requestId, "Approved", wrongApproverId));
        
        assertTrue(exception.getMessage().contains("not authorized to approve"));
    }

    // ==================== REQUEST REJECTION TESTS ====================

    @Test
    void testRejectRequest_Success() {
        // Arrange
        UUID requestId = pendingRequest.getId();
        when(approvalRequestRepository.findById(requestId)).thenReturn(Optional.of(pendingRequest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenReturn(pendingRequest);

        // Act
        RequestSummaryDTO result = approvalService.rejectRequest(requestId, "Not approved", 2L);

        // Assert
        assertNotNull(result);
        assertEquals(RequestStatus.REJECTED, pendingRequest.getStatus());
        verify(requestLogRepository, times(1)).save(any(RequestLog.class));
        verify(emailNotificationService, times(1)).sendRequestStatusUpdateNotification(any(), any(), any());
    }

    @Test
    void testRejectRequest_RequestNotFound() {
        // Arrange
        UUID requestId = UUID.randomUUID();
        when(approvalRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> approvalService.rejectRequest(requestId, "Rejected", 2L));
        
        assertTrue(exception.getMessage().contains("Request not found"));
    }

    @Test
    void testRejectRequest_RequestNotPending() {
        // Arrange
        UUID requestId = rejectedRequest.getId();
        when(approvalRequestRepository.findById(requestId)).thenReturn(Optional.of(rejectedRequest));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> approvalService.rejectRequest(requestId, "Rejected", 2L));
        
        assertTrue(exception.getMessage().contains("not in PENDING status"));
    }

    @Test
    void testRejectRequest_UnauthorizedApprover() {
        // Arrange
        UUID requestId = pendingRequest.getId();
        Long wrongApproverId = 999L;
        when(approvalRequestRepository.findById(requestId)).thenReturn(Optional.of(pendingRequest));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> approvalService.rejectRequest(requestId, "Rejected", wrongApproverId));
        
        assertTrue(exception.getMessage().contains("not authorized to reject"));
    }

    // ==================== REQUEST RETRIEVAL TESTS ====================

    @Test
    void testGetRequestsCreatedByUser_EmptyList() {
        // Arrange
        when(approvalRequestRepository.findByRequester_IdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        // Act
        List<RequestSummaryDTO> results = approvalService.getRequestsCreatedByUser(1L);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testGetRequestsCreatedByUser_WithResults() {
        // Arrange
        when(approvalRequestRepository.findByRequester_IdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(pendingRequest, approvedRequest));

        // Act
        List<RequestSummaryDTO> results = approvalService.getRequestsCreatedByUser(1L);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void testGetRequestsAssignedToUser_EmptyList() {
        // Arrange
        when(approvalRequestRepository.findByApprover_IdOrderByCreatedAtDesc(2L))
                .thenReturn(List.of());

        // Act
        List<RequestSummaryDTO> results = approvalService.getRequestsAssignedToUser(2L);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testGetRequestsAssignedToUser_WithResults() {
        // Arrange
        when(approvalRequestRepository.findByApprover_IdOrderByCreatedAtDesc(2L))
                .thenReturn(List.of(pendingRequest));

        // Act
        List<RequestSummaryDTO> results = approvalService.getRequestsAssignedToUser(2L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test Requester", results.get(0).relatedUserName());
    }

    @Test
    void testGetRequestDetails_Success() {
        // Arrange
        UUID requestId = pendingRequest.getId();
        when(approvalRequestRepository.findById(requestId)).thenReturn(Optional.of(pendingRequest));
        when(requestLogRepository.findByRequestOrderByActionDateDesc(pendingRequest))
                .thenReturn(List.of());

        // Act
        var result = approvalService.getRequestDetails(requestId);

        // Assert
        assertNotNull(result);
        assertEquals("Test Request", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void testGetRequestDetails_NotFound() {
        // Arrange
        UUID requestId = UUID.randomUUID();
        when(approvalRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> approvalService.getRequestDetails(requestId));
        
        assertTrue(exception.getMessage().contains("Request not found"));
    }
}
