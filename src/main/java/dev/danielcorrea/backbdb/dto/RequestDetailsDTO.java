package dev.danielcorrea.backbdb.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for detailed request information.
 * Includes full request details, comments, and related user information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDetailsDTO {
    
    private UUID id;
    private String title;
    private String description;
    private String status;
    private String typeName;
    private LocalDateTime createdAt;
    private String relatedUserName;
    private String comments;
}
