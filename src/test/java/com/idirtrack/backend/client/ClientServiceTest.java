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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import com.idirtrack.backend.basics.BasicException;
import com.idirtrack.backend.errors.NotFoundException;
import com.idirtrack.backend.traccar.TraccarUserService;
import com.idirtrack.backend.user.UserService;
import com.idirtrack.backend.utils.MyResponse;
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

    @Test
    void getActiveAndInactiveClientCount_shouldReturnCorrectCounts() {
        // Arrange
        when(clientRepository.countActiveClients()).thenReturn(42L);
        when(clientRepository.countInactiveClients()).thenReturn(8L);

        // Act
        MyResponse response = clientService.getActiveAndInactiveClientCount();

        // Assert
        Map<String, Object> data = (Map<String, Object>) response.getData();
        assertEquals(42L, data.get("activeClients"));
        assertEquals(8L, data.get("inactiveClients"));
        assertEquals("Successfully retrieved active and inactive client counts", response.getMessage());
        assertEquals(HttpStatus.OK, response.getStatus());
    }

      @Test
    void filterClientsByCategoryAndStatus_shouldReturnFilteredClients() {
        // Arrange
        Long categoryId = 1L;
        boolean isDisabled = false;
        Pageable pageable = PageRequest.of(0, 10);
        Client client1 = new Client(); // Initialize client1 with appropriate values
        Client client2 = new Client(); // Initialize client2 with appropriate values

        Page<Client> clientsPage = new PageImpl<>(List.of(client1, client2), pageable, 2);

        when(clientRepository.findByCategoryAndStatus(categoryId, isDisabled, pageable)).thenReturn(clientsPage);

        // Act
        MyResponse response = clientService.filterClientsByCategoryAndStatus(categoryId, isDisabled, 1, 10);

        // Assert
        List<Client> clients = (List<Client>) response.getData();
        assertEquals(2, clients.size());
        assertEquals("Successfully filtered clients by category and status", response.getMessage());
        assertEquals(HttpStatus.OK, response.getStatus());
    }





}
