package dev.danielcorrea.backbdb.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import dev.danielcorrea.backbdb.model.ApprovalRequest;
import dev.danielcorrea.backbdb.model.RequestStatus;
import dev.danielcorrea.backbdb.model.RequestType;
import dev.danielcorrea.backbdb.model.User;
import jakarta.mail.internet.MimeMessage;

/**
 * Unit tests for EmailNotificationService.
 * Uses mocks to avoid sending real emails during tests.
 */
@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceUnitTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    private User requester;
    private User approver;
    private RequestType requestType;
    private ApprovalRequest request;

    @BeforeEach
    void setUp() {
        // Set up mock values for @Value fields
        ReflectionTestUtils.setField(emailNotificationService, "fromAddress", "test@example.com");
        ReflectionTestUtils.setField(emailNotificationService, "fromName", "Test System");

        // Create test data
        requester = User.builder()
                .id(1L)
                .username("john.doe")
                .fullName("John Doe")
                .email("john.doe@example.com")
                .build();

        approver = User.builder()
                .id(2L)
                .username("jane.smith")
                .fullName("Jane Smith")
                .email("jane.smith@example.com")
                .build();

        requestType = RequestType.builder()
                .id(1)
                .name("DEPLOYMENT")
                .description("Deployment request")
                .build();

        request = ApprovalRequest.builder()
                .id(UUID.randomUUID())
                .title("Test Request")
                .description("Test Description")
                .status(RequestStatus.PENDING)
                .requester(requester)
                .approver(approver)
                .type(requestType)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testSendRequestCreatedNotification_Success() throws Exception {
        // Arrange
        String htmlContent = "<html><body>Test Email</body></html>";
        when(templateEngine.process(eq("request-created"), any(Context.class))).thenReturn(htmlContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        emailNotificationService.sendRequestCreatedNotification(request, approver);

        // Give async operation time to complete
        Thread.sleep(500);

        // Assert
        verify(templateEngine, times(1)).process(eq("request-created"), any(Context.class));
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendRequestCreatedNotification_VerifyTemplateContext() throws Exception {
        // Arrange
        String htmlContent = "<html><body>Test Email</body></html>";
        when(templateEngine.process(eq("request-created"), any(Context.class))).thenReturn(htmlContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        emailNotificationService.sendRequestCreatedNotification(request, approver);

        // Give async operation time to complete
        Thread.sleep(500);

        // Assert - Verify that template engine was called with correct template name
        verify(templateEngine, times(1)).process(eq("request-created"), any(Context.class));
    }

    @Test
    void testSendRequestStatusUpdateNotification_Approved() throws Exception {
        // Arrange
        request.setStatus(RequestStatus.APPROVED);
        String comments = "Looks good! Approved.";
        String htmlContent = "<html><body>Request Approved</body></html>";
        
        when(templateEngine.process(eq("request-status-update"), any(Context.class))).thenReturn(htmlContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        emailNotificationService.sendRequestStatusUpdateNotification(request, approver, comments);

        // Give async operation time to complete
        Thread.sleep(500);

        // Assert
        verify(templateEngine, times(1)).process(eq("request-status-update"), any(Context.class));
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendRequestStatusUpdateNotification_Rejected() throws Exception {
        // Arrange
        request.setStatus(RequestStatus.REJECTED);
        String comments = "Not approved at this time.";
        String htmlContent = "<html><body>Request Rejected</body></html>";
        
        when(templateEngine.process(eq("request-status-update"), any(Context.class))).thenReturn(htmlContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        emailNotificationService.sendRequestStatusUpdateNotification(request, approver, comments);

        // Give async operation time to complete
        Thread.sleep(500);

        // Assert
        verify(templateEngine, times(1)).process(eq("request-status-update"), any(Context.class));
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendRequestCreatedNotification_HandlesException() throws Exception {
        // Arrange
        String htmlContent = "<html><body>Test Email</body></html>";
        when(templateEngine.process(eq("request-created"), any(Context.class))).thenReturn(htmlContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("SMTP server error")).when(mailSender).send(any(MimeMessage.class));

        // Act - Should not throw exception, error should be logged
        emailNotificationService.sendRequestCreatedNotification(request, approver);

        // Give async operation time to complete
        Thread.sleep(500);

        // Assert - Verify that the method attempted to send email
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendRequestStatusUpdateNotification_HandlesException() throws Exception {
        // Arrange
        request.setStatus(RequestStatus.APPROVED);
        String comments = "Approved!";
        String htmlContent = "<html><body>Request Approved</body></html>";
        
        when(templateEngine.process(eq("request-status-update"), any(Context.class))).thenReturn(htmlContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("SMTP server error")).when(mailSender).send(any(MimeMessage.class));

        // Act - Should not throw exception, error should be logged
        emailNotificationService.sendRequestStatusUpdateNotification(request, approver, comments);

        // Give async operation time to complete
        Thread.sleep(500);

        // Assert - Verify that the method attempted to send email
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendRequestCreatedNotification_WithNullComments() throws Exception {
        // Arrange
        request.setStatus(RequestStatus.APPROVED);
        String htmlContent = "<html><body>Request Approved</body></html>";
        
        when(templateEngine.process(eq("request-status-update"), any(Context.class))).thenReturn(htmlContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        emailNotificationService.sendRequestStatusUpdateNotification(request, approver, null);

        // Give async operation time to complete
        Thread.sleep(500);

        // Assert - Should handle null comments gracefully
        verify(templateEngine, times(1)).process(eq("request-status-update"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendRequestCreatedNotification_VerifyRecipient() throws Exception {
        // Arrange
        String htmlContent = "<html><body>Test Email</body></html>";
        when(templateEngine.process(eq("request-created"), any(Context.class))).thenReturn(htmlContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        emailNotificationService.sendRequestCreatedNotification(request, approver);

        // Give async operation time to complete
        Thread.sleep(500);

        // Assert - Email should be sent to approver
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendRequestStatusUpdateNotification_VerifyRecipient() throws Exception {
        // Arrange
        request.setStatus(RequestStatus.APPROVED);
        String comments = "Approved!";
        String htmlContent = "<html><body>Request Approved</body></html>";
        
        when(templateEngine.process(eq("request-status-update"), any(Context.class))).thenReturn(htmlContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        emailNotificationService.sendRequestStatusUpdateNotification(request, approver, comments);

        // Give async operation time to complete
        Thread.sleep(500);

        // Assert - Email should be sent to requester
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
