package dev.danielcorrea.backbdb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.danielcorrea.backbdb.model.ApprovalRequest;
import dev.danielcorrea.backbdb.model.RequestLog;
import dev.danielcorrea.backbdb.model.User;

@Repository
public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {
    
    // Find all logs for a specific request
    List<RequestLog> findByRequest(ApprovalRequest request);
    
    // Find all logs by a specific user
    List<RequestLog> findByUser(User user);
    
    // Find all logs for a request ordered by action date
    List<RequestLog> findByRequestOrderByActionDateDesc(ApprovalRequest request);
}
