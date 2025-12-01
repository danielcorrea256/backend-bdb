package dev.danielcorrea.backbdb.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.danielcorrea.backbdb.model.ApprovalRequest;
import dev.danielcorrea.backbdb.model.RequestStatus;
import dev.danielcorrea.backbdb.model.User;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, UUID> {
    
    // Find all requests by requester
    List<ApprovalRequest> findByRequester(User requester);
    
    // Find all requests by approver
    List<ApprovalRequest> findByApprover(User approver);
    
    // Find all requests by status
    List<ApprovalRequest> findByStatus(RequestStatus status);
    
    // Find requests by requester and status
    List<ApprovalRequest> findByRequesterAndStatus(User requester, RequestStatus status);
    
    // Find requests by approver and status
    List<ApprovalRequest> findByApproverAndStatus(User approver, RequestStatus status);
    
    // Count requests by status
    long countByStatus(RequestStatus status);
    
    // Find all requests created by a specific user (for "My Requests" tab)
    List<ApprovalRequest> findByRequester_IdOrderByCreatedAtDesc(Long requesterId);
    
    // Find all requests assigned to a specific user for approval (for "My Inbox" tab)
    List<ApprovalRequest> findByApprover_IdOrderByCreatedAtDesc(Long approverId);
}
