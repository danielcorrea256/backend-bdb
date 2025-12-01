package dev.danielcorrea.backbdb.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lightweight DTO for approval request summaries in list views.
 * Excludes heavy fields like description and audit history.
 */
public record RequestSummaryDTO(
    UUID id,
    String title,
    String status,
    String typeName,
    LocalDateTime createdAt,
    String relatedUserName  // Approver name if viewing "My Requests", Requester name if viewing "My Inbox"
) {
}
