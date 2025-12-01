package dev.danielcorrea.backbdb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for approve/reject request action.
 * Contains optional comments from the approver.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestActionDTO {
    
    private String comments;
    private Long approverId;
}
