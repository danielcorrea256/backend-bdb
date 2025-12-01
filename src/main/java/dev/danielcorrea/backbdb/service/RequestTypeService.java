package dev.danielcorrea.backbdb.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.danielcorrea.backbdb.dto.RequestTypeDTO;
import dev.danielcorrea.backbdb.model.RequestType;
import dev.danielcorrea.backbdb.repository.RequestTypeRepository;
import lombok.RequiredArgsConstructor;

/**
 * Service layer for request type-related business logic.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestTypeService {

    private final RequestTypeRepository requestTypeRepository;

    /**
     * Retrieves all request types from the database.
     * 
     * @return List of RequestTypeDTO containing all request types
     */
    public List<RequestTypeDTO> getAllRequestTypes() {
        List<RequestType> requestTypes = requestTypeRepository.findAll();
        
        return requestTypes.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Maps a RequestType entity to RequestTypeDTO.
     * 
     * @param requestType The RequestType entity
     * @return RequestTypeDTO
     */
    private RequestTypeDTO mapToDTO(RequestType requestType) {
        return new RequestTypeDTO(
            requestType.getId(),
            requestType.getName(),
            requestType.getDescription()
        );
    }
}
