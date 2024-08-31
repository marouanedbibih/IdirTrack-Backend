package com.idirtrack.backend.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.idirtrack.backend.client.dtos.ClientInfoDTO;
import com.idirtrack.backend.client.dtos.ClientUpdateRequest;
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

    @Mock 
    private ClientCategoryRepository clientCategoryRepository;

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

     @Test
    void updateClient_shouldUpdateClientSuccessfully() throws NotFoundException, BasicException {
        // Arrange
        Long clientId = 1L;
        Client client = new Client();
        client.setId(clientId);
        User user = new User();
        user.setId(2L);
        client.setUser(user);

        ClientUpdateRequest request = new ClientUpdateRequest();
        request.setUsername("newUsername");
        request.setName("New Name");
        request.setEmail("newemail@example.com");
        request.setPhone("123456789");
        request.setCompany("New Company");
        request.setCne("CNE123456");
        request.setCategoryId(1L);
        request.setPassword("newPassword");
        request.setRemarque("Some remark");
        request.setDisabled(true);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientCategoryRepository.findById(1L)).thenReturn(Optional.of(new ClientCategory()));

        // Act
        MyResponse response = clientService.updateClient(clientId, request);

        // Assert
        assertEquals("Client updated successfully", response.getMessage());
        verify(userService).isUsernameTakenExcept(request.getUsername(), user.getId());
        verify(userService).isEmailTakenExcept(request.getEmail(), user.getId());
        verify(userService).isPhoneTakenExcept(request.getPhone(), user.getId());
        verify(clientRepository).save(client);
    }


   @Test
    void getClientInfoById_shouldReturnClientInfo_whenClientExists() throws NotFoundException {
        // Arrange
        Long clientId = 1L;
        ClientCategory category = new ClientCategory();
        category.setName("Category 1");

        User user = new User();
        user.setUsername("testuser");
        user.setName("Test User");
        user.setEmail("testuser@example.com");
        user.setPhone("1234567890");

        Client client = new Client();
        client.setUser(user);
        client.setCompany("Test Company");
        client.setCne("CNE123456");
        client.setCategory(category);
        client.setRemarque("This is a remark");
        client.setDisabled(false);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        // Act
        ClientInfoDTO clientInfo = clientService.getClientInfoById(clientId);

        // Assert
        assertNotNull(clientInfo);
        assertEquals("testuser", clientInfo.getUsername());
        assertEquals("Test User", clientInfo.getName());
        assertEquals("testuser@example.com", clientInfo.getEmail());
        assertEquals("1234567890", clientInfo.getPhone());
        assertEquals("Test Company", clientInfo.getCompany());
        assertEquals("CNE123456", clientInfo.getCne());
        assertEquals("Category 1", clientInfo.getCategory());
        assertEquals("This is a remark", clientInfo.getRemarque());
        assertFalse(clientInfo.isDisabled());

        verify(clientRepository, times(1)).findById(clientId);
    }

    @Test
    void getClientInfoById_shouldThrowNotFoundException_whenClientDoesNotExist() {
        // Arrange
        Long clientId = 1L;
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            clientService.getClientInfoById(clientId);
        });

        assertEquals("Client not found with id: " + clientId, exception.getMessage());
        verify(clientRepository, times(1)).findById(clientId);
    }


}
