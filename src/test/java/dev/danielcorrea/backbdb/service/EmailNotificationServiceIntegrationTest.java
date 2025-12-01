package dev.danielcorrea.backbdb.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import dev.danielcorrea.backbdb.model.ApprovalRequest;
import dev.danielcorrea.backbdb.model.RequestStatus;
import dev.danielcorrea.backbdb.model.RequestType;
import dev.danielcorrea.backbdb.model.User;

/**
 * Integration test for EmailNotificationService.
 * This test sends actual emails to verify the email functionality works correctly.
 * 
 * Note: This test requires valid SMTP credentials and database connection.
 * These tests only run in CI/CD when DB_URL environment variable is set to a MySQL JDBC URL.
 * 
 * To run locally, set the DB_URL environment variable:
 * export DB_URL=jdbc:mysql://localhost:3306/approval_flow_test
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = "jdbc:mysql://.*", disabledReason = "Integration test - requires MySQL database. Set DB_URL environment variable to run.")
public class EmailNotificationServiceIntegrationTest {

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Test
    public void testSendRequestCreatedNotification() throws InterruptedException {
        // Create dummy user objects
        User requester = User.builder()
                .id(1L)
                .username("john.doe")
                .fullName("John Doe")
                .email("john.doe@example.com")
                .build();

        User approver = User.builder()
                .id(2L)
                .username("daniel.correa")
                .fullName("Daniel Correa")
                .email("danielcorrea2048@gmail.com")
                .build();

        // Create dummy request type
        RequestType requestType = RequestType.builder()
                .id(1)
                .name("DEPLOYMENT")
                .description("Deployment request")
                .build();

        // Create dummy approval request
        ApprovalRequest request = ApprovalRequest.builder()
                .id(UUID.randomUUID())
                .title("Test Request - Email Notification")
                .description("This is a test request to verify email notification functionality. The system is working correctly!")
                .status(RequestStatus.PENDING)
                .requester(requester)
                .approver(approver)
                .type(requestType)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Send the notification
        System.out.println("Sending request created notification to: " + approver.getEmail());
        emailNotificationService.sendRequestCreatedNotification(request, approver);
        
        // Wait a bit for async operation to complete
        Thread.sleep(3000);
        System.out.println("Email sent successfully! Check inbox: " + approver.getEmail());
    }

    @Test
    public void testSendRequestApprovedNotification() throws InterruptedException {
        // Create dummy user objects
        User requester = User.builder()
                .id(1L)
                .username("john.doe")
                .fullName("John Doe")
                .email("danielcorrea2048@gmail.com")
                .build();

        User approver = User.builder()
                .id(2L)
                .username("jane.smith")
                .fullName("Jane Smith")
                .email("jane.smith@example.com")
                .build();

        // Create dummy request type
        RequestType requestType = RequestType.builder()
                .id(1)
                .name("BUDGET_APPROVAL")
                .description("Budget approval request")
                .build();

        // Create dummy approval request (APPROVED)
        ApprovalRequest request = ApprovalRequest.builder()
                .id(UUID.randomUUID())
                .title("Budget Request for Q1 2025")
                .description("Requesting approval for Q1 2025 marketing budget of $50,000")
                .status(RequestStatus.APPROVED)
                .requester(requester)
                .approver(approver)
                .type(requestType)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();

        String comments = "Approved! The budget allocation looks reasonable and aligns with our quarterly goals.";

        // Send the notification
        System.out.println("Sending request approved notification to: " + requester.getEmail());
        emailNotificationService.sendRequestStatusUpdateNotification(request, approver, comments);
        
        // Wait a bit for async operation to complete
        Thread.sleep(3000);
        System.out.println("Email sent successfully! Check inbox: " + requester.getEmail());
    }

    @Test
    public void testSendRequestRejectedNotification() throws InterruptedException {
        // Create dummy user objects
        User requester = User.builder()
                .id(1L)
                .username("john.doe")
                .fullName("John Doe")
                .email("danielcorrea2048@gmail.com")
                .build();

        User approver = User.builder()
                .id(2L)
                .username("jane.smith")
                .fullName("Jane Smith")
                .email("jane.smith@example.com")
                .build();

        // Create dummy request type
        RequestType requestType = RequestType.builder()
                .id(1)
                .name("ACCESS_REQUEST")
                .description("System access request")
                .build();

        // Create dummy approval request (REJECTED)
        ApprovalRequest request = ApprovalRequest.builder()
                .id(UUID.randomUUID())
                .title("Admin Access Request - Production Database")
                .description("Requesting admin access to production database for troubleshooting")
                .status(RequestStatus.REJECTED)
                .requester(requester)
                .approver(approver)
                .type(requestType)
                .createdAt(LocalDateTime.now().minusHours(2))
                .updatedAt(LocalDateTime.now())
                .build();

        String comments = "Request rejected. Admin access to production requires additional security clearance. Please contact the security team first.";

        // Send the notification
        System.out.println("Sending request rejected notification to: " + requester.getEmail());
        emailNotificationService.sendRequestStatusUpdateNotification(request, approver, comments);
        
        // Wait a bit for async operation to complete
        Thread.sleep(3000);
        System.out.println("Email sent successfully! Check inbox: " + requester.getEmail());
    }
}
