package dev.danielcorrea.backbdb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new approval request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Requester ID is required")
    private Long requesterId;

    @NotNull(message = "Approver ID is required")
    private Long approverId;

    @NotNull(message = "Request Type ID is required")
    private Integer requestTypeId;
}
