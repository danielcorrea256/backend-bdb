package dev.danielcorrea.backbdb.service;

import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import dev.danielcorrea.backbdb.model.ApprovalRequest;
import dev.danielcorrea.backbdb.model.User;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${mail.from.address}")
    private String fromAddress;

    @Value("${mail.from.name}")
    private String fromName;

    public EmailNotificationService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Sends an email notification when a new request is created.
     * Notifies the approver about the new request.
     *
     * @param request The approval request that was created
     * @param approver The user who needs to approve the request
     */
    @Async
    public void sendRequestCreatedNotification(ApprovalRequest request, User approver) {
        try {
            logger.info("Sending request created notification for request ID: {} to approver email: {}",
                    request.getId(), approver.getEmail());

            Context context = new Context();
            context.setVariable("approverName", approver.getFullName());
            context.setVariable("requestId", request.getId().toString());
            context.setVariable("requestTitle", request.getTitle());
            context.setVariable("requestDescription", request.getDescription());
            context.setVariable("requestType", request.getType().getName());
            context.setVariable("creatorName", request.getRequester().getFullName());
            context.setVariable("createdAt", request.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            String htmlContent = templateEngine.process("request-created", context);

            sendEmail(
                    approver.getEmail(),
                    "New Approval Request: " + request.getTitle(),
                    htmlContent
            );

            logger.info("Request created notification sent successfully to: {}", approver.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send request created notification for request ID: {} to approver: {}",
                    request.getId(), approver.getEmail(), e);
        }
    }

    /**
     * Sends an email notification when a request status is updated (approved or rejected).
     * Notifies the request creator about the status change.
     *
     * @param request The approval request that was updated
     * @param actionPerformer The user who approved or rejected the request
     * @param comments Optional comments provided during approval/rejection
     */
    @Async
    public void sendRequestStatusUpdateNotification(ApprovalRequest request, User actionPerformer, String comments) {
        try {
            logger.info("Sending request status update notification for request ID: {} to creator email: {}",
                    request.getId(), request.getRequester().getEmail());

            Context context = new Context();
            context.setVariable("creatorName", request.getRequester().getFullName());
            context.setVariable("requestId", request.getId().toString());
            context.setVariable("requestTitle", request.getTitle());
            context.setVariable("requestStatus", request.getStatus().toString());
            context.setVariable("actionPerformerName", actionPerformer.getFullName());
            context.setVariable("comments", comments != null ? comments : "No comments provided");
            context.setVariable("isApproved", request.getStatus().toString().equals("APPROVED"));

            String htmlContent = templateEngine.process("request-status-update", context);

            String subject = String.format("Request %s: %s",
                    request.getStatus().toString().equals("APPROVED") ? "Approved" : "Rejected",
                    request.getTitle());

            sendEmail(
                    request.getRequester().getEmail(),
                    subject,
                    htmlContent
            );

            logger.info("Request status update notification sent successfully to: {}", request.getRequester().getEmail());
        } catch (Exception e) {
            logger.error("Failed to send request status update notification for request ID: {} to creator email: {}",
                    request.getId(), request.getRequester().getEmail(), e);
        }
    }

    /**
     * Helper method to send an email using JavaMailSender.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlContent HTML content of the email
     * @throws Exception if email sending fails
     */
    private void sendEmail(String to, String subject, String htmlContent) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromAddress, fromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
