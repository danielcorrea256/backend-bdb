package dev.danielcorrea.backbdb.service;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.danielcorrea.backbdb.dto.RequestTypeDTO;
import dev.danielcorrea.backbdb.model.RequestType;
import dev.danielcorrea.backbdb.repository.RequestTypeRepository;

/**
 * Unit tests for RequestTypeService.
 * Tests request type retrieval and DTO mapping functionality.
 */
@ExtendWith(MockitoExtension.class)
class RequestTypeServiceUnitTest {

    @Mock
    private RequestTypeRepository requestTypeRepository;

    @InjectMocks
    private RequestTypeService requestTypeService;

    private RequestType type1;
    private RequestType type2;

    @BeforeEach
    void setUp() {
        type1 = new RequestType();
        type1.setId(1);
        type1.setName("Purchase Request");
        type1.setDescription("Request for purchasing equipment");

        type2 = new RequestType();
        type2.setId(2);
        type2.setName("Travel Request");
        type2.setDescription("Request for business travel approval");
    }

    @Test
    void testGetAllRequestTypes_Success() {
        // Arrange
        when(requestTypeRepository.findAll()).thenReturn(Arrays.asList(type1, type2));

        // Act
        List<RequestTypeDTO> result = requestTypeService.getAllRequestTypes();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        RequestTypeDTO dto1 = result.get(0);
        assertEquals(1, dto1.getId());
        assertEquals("Purchase Request", dto1.getName());
        assertEquals("Request for purchasing equipment", dto1.getDescription());
        
        RequestTypeDTO dto2 = result.get(1);
        assertEquals(2, dto2.getId());
        assertEquals("Travel Request", dto2.getName());
        assertEquals("Request for business travel approval", dto2.getDescription());
        
        verify(requestTypeRepository, times(1)).findAll();
    }

    @Test
    void testGetAllRequestTypes_EmptyList() {
        // Arrange
        when(requestTypeRepository.findAll()).thenReturn(List.of());

        // Act
        List<RequestTypeDTO> result = requestTypeService.getAllRequestTypes();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(requestTypeRepository, times(1)).findAll();
    }

    @Test
    void testGetAllRequestTypes_NullDescriptionHandling() {
        // Arrange
        RequestType typeWithNullDescription = new RequestType();
        typeWithNullDescription.setId(3);
        typeWithNullDescription.setName("Minimal Type");
        typeWithNullDescription.setDescription(null);
        
        when(requestTypeRepository.findAll()).thenReturn(List.of(typeWithNullDescription));

        // Act
        List<RequestTypeDTO> result = requestTypeService.getAllRequestTypes();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Minimal Type", result.get(0).getName());
        assertNull(result.get(0).getDescription());
        verify(requestTypeRepository, times(1)).findAll();
    }
}
