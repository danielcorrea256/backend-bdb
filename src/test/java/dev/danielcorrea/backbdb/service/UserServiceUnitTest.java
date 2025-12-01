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

import dev.danielcorrea.backbdb.dto.UserDTO;
import dev.danielcorrea.backbdb.model.User;
import dev.danielcorrea.backbdb.repository.UserRepository;

/**
 * Unit tests for UserService.
 * Tests user retrieval and DTO mapping functionality.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setUsername("jdoe");
        user1.setFullName("John Doe");
        user1.setEmail("john.doe@example.com");

        user2 = new User();
        user2.setId(2L);
        user2.setUsername("asmith");
        user2.setFullName("Alice Smith");
        user2.setEmail("alice.smith@example.com");
    }

    @Test
    void testGetAllUsers_Success() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        UserDTO dto1 = result.get(0);
        assertEquals(1L, dto1.getId());
        assertEquals("jdoe", dto1.getUsername());
        assertEquals("John Doe", dto1.getFullName());
        assertEquals("john.doe@example.com", dto1.getEmail());
        
        UserDTO dto2 = result.get(1);
        assertEquals(2L, dto2.getId());
        assertEquals("asmith", dto2.getUsername());
        assertEquals("Alice Smith", dto2.getFullName());
        assertEquals("alice.smith@example.com", dto2.getEmail());
        
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetAllUsers_EmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of());

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetAllUsers_NullEmailHandling() {
        // Arrange
        User userWithNullEmail = new User();
        userWithNullEmail.setId(3L);
        userWithNullEmail.setUsername("nomail");
        userWithNullEmail.setFullName("No Mail User");
        userWithNullEmail.setEmail(null);
        
        when(userRepository.findAll()).thenReturn(List.of(userWithNullEmail));

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getEmail());
        verify(userRepository, times(1)).findAll();
    }
}
