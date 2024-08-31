package com.idirtrack.backend.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.idirtrack.backend.basics.BasicException;
import com.idirtrack.backend.errors.NotFoundException;
import com.idirtrack.backend.traccar.TraccarUserService;
import com.idirtrack.backend.user.UserService;
import com.idirtrack.backend.user.User;


public class ClientServiceTest {
   @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserService userService;

    @Mock
    private TraccarUserService traccarUserService;

    @InjectMocks
    private ClientService clientService;

    private Client client;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize the Client and User objects
        user = User.builder()
                .id(1L)
                .traccarId(123L)
                .build();

        client = Client.builder()
                .id(1L)
                .user(user)
                .build();
    }

     @Test
    void deleteClient_shouldDeleteClientSuccessfully() throws Exception {
        // Arrange
        Long clientId = 1L;
        String token = "dummyToken";

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        // Act
        clientService.deleteClient(clientId, token);

        // Assert
        verify(traccarUserService, times(1)).deleteUser(client.getUser().getTraccarId(), token);
        verify(userService, times(1)).deleteUser(client.getUser().getId());
        verify(clientRepository, times(1)).delete(client);
    }
    @Test
    void deleteClient_shouldThrowNotFoundException_whenClientNotFound() throws NotFoundException , BasicException {
        // Arrange
        Long clientId = 1L;
        String token = "dummyToken";
    
        // Simulate the repository returning an empty Optional (i.e., client not found)
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());
    
        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            clientService.deleteClient(clientId, token);
        });
    
        // Verify that the exception message matches the expected message
        assertEquals("Client not found with id: " + clientId, exception.getResponse().getMessage());
        
        // Verify that no interactions happened with these services since the client was not found
        verify(traccarUserService, never()).deleteUser(anyLong());
        verify(userService, never()).deleteUser(anyLong());
        verify(clientRepository, never()).delete(any(Client.class));
    }

     @Test
    void deleteClient_shouldHandleExceptionDuringTraccarUserDeletion() throws Exception {
        // Arrange
        Long clientId = 1L;
        String token = "dummyToken";

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        doThrow(new RuntimeException("Traccar deletion failed"))
                .when(traccarUserService).deleteUser(client.getUser().getTraccarId(), token);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clientService.deleteClient(clientId, token);
        });

        assertEquals("Traccar deletion failed", exception.getMessage());
        verify(traccarUserService, times(1)).deleteUser(client.getUser().getTraccarId(), token);
        verify(userService, never()).deleteUser(anyLong());
        verify(clientRepository, never()).delete(any(Client.class));
    }


}
