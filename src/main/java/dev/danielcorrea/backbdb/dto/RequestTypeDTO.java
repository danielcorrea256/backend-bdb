package dev.danielcorrea.backbdb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for request type information.
 * Used for returning request type data to the frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestTypeDTO {
    
    private Integer id;
    private String name;
    private String description;
}
