
package com.idirtrack.backend.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.aspectj.lang.annotation.Before;
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
import com.idirtrack.backend.client.dtos.ClientRequest;
import com.idirtrack.backend.client.dtos.ClientUpdateRequest;
import com.idirtrack.backend.errors.AlreadyExistException;
import com.idirtrack.backend.errors.MyException;
import com.idirtrack.backend.errors.NotFoundException;
import com.idirtrack.backend.traccar.TraccarUserService;
import com.idirtrack.backend.user.User;
import com.idirtrack.backend.user.UserDTO;
import com.idirtrack.backend.user.UserRole;
import com.idirtrack.backend.user.UserService;
import com.idirtrack.backend.utils.MyResponse;
import com.idirtrack.backend.vehicle.Vehicle;

/**
 * ClientServiceTest
 */
public class ClientServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private TraccarUserService traccarUserService;

    @Mock
    private ClientCategoryRepository clientCategoryRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService; 
    private Client client;
    private User user;

    private ClientCategory category;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize the Client and User objects
        user = User.builder()
                .id(1L)
                .traccarId(123L)
                .username("testuser")
                .build();

        client = Client.builder()
                .id(1L)
                .user(user)
                .vehicles(new ArrayList<>())  // Empty list of vehicles
                .company("Test Company")
                .build();
    }

    // Test the createClient method
   @Test
    void createClient_shouldCreateClientSuccessfully_whenValidRequest() throws AlreadyExistException, NotFoundException, MyException {
        // Arrange
        ClientRequest clientRequest = new ClientRequest();
        clientRequest.setUsername("new_user");
        clientRequest.setEmail("user@example.com");
        clientRequest.setCategoryId(1L);
        clientRequest.setCompany("New Company");
        clientRequest.setCne("CNE123456");

        ClientCategory category = new ClientCategory();
        category.setId(1L);
        category.setName("Test Category");

        UserDTO userDTO = UserDTO.builder()
                .username(clientRequest.getUsername())
                .email(clientRequest.getEmail())
                .role(UserRole.CLIENT)
                .build();

        User user = new User();
        user.setId(1L);
        user.setUsername("new_user");

          // Mock the isUserExistInSystem to return false (user does not exist)
    when(userService.isUserExistInSystem(clientRequest.getUsername(), clientRequest.getEmail()))
    .thenReturn(false);


     // Mock the clientCategoryRepository to return a valid category
     category  = new ClientCategory();
     category.setId(1L);
     when(clientCategoryRepository.findById(clientRequest.getCategoryId()))
         .thenReturn(Optional.of(category));
 

// Act
MyResponse response = clientService.createClient(clientRequest, "dummyToken");

// Assert
assertNotNull(response);
assertEquals("Client created successfully", response.getMessage());
    }

    //test user alerady exist 
    @Test
void createClient_shouldThrowAlreadyExistException_whenUserExists() throws AlreadyExistException, NotFoundException, MyException {
    // Arrange
    ClientRequest clientRequest = new ClientRequest();
    clientRequest.setUsername("existing_user");
    clientRequest.setEmail("user@example.com");

    // Mock user existence
    doThrow(new AlreadyExistException("User already exists")).when(userService).isUserExistInSystem(clientRequest.getUsername(), clientRequest.getEmail());

    // Act & Assert
    AlreadyExistException exception = assertThrows(AlreadyExistException.class, () -> {
        clientService.createClient(clientRequest, "Bearer token");
    });

    assertEquals("User already exists", exception.getMessage());
    verify(clientRepository, never()).save(any(Client.class));
}


  //test category not found 
  @Test
void createClient_shouldThrowNotFoundException_whenCategoryNotFound() throws AlreadyExistException, NotFoundException, MyException {
    // Arrange
    ClientRequest clientRequest = new ClientRequest();
    clientRequest.setCategoryId(99L); // Non-existent category ID

    // Mock category not found
    when(clientCategoryRepository.findById(clientRequest.getCategoryId())).thenReturn(Optional.empty());

    // Act & Assert
    NotFoundException exception = assertThrows(NotFoundException.class, () -> {
        clientService.createClient(clientRequest, "Bearer token");
    });

    assertEquals("Category not found with id: " + clientRequest.getCategoryId(), exception.getResponse().getMessage());
    verify(clientRepository, never()).save(any(Client.class));
}

  //test handeling general exception
  @Test
void createClient_shouldHandleException_whenUserSavingFails() throws AlreadyExistException, NotFoundException, MyException {
    // Arrange
    ClientRequest clientRequest = new ClientRequest();
    clientRequest.setUsername("new_user");
    clientRequest.setEmail("user@example.com");
    clientRequest.setCategoryId(1L);

    ClientCategory category = new ClientCategory();
    category.setId(1L);

    // Mock user saving failure
    when(clientCategoryRepository.findById(clientRequest.getCategoryId())).thenReturn(Optional.of(category));
    when(userService.saveUserInSystem(any(UserDTO.class), anyString())).thenThrow(new RuntimeException("User saving failed"));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        clientService.createClient(clientRequest, "Bearer token");
    });

    assertEquals("User saving failed", exception.getMessage());
    verify(clientRepository, never()).save(any(Client.class));
}

//test update client
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

        ClientCategory newCategory = new ClientCategory();
        newCategory.setId(2L);
        newCategory.setName("New Category");

        // Mocking the necessary calls
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientCategoryRepository.findById(request.getCategoryId())).thenReturn(Optional.of(newCategory));

        // Act
        MyResponse response = clientService.updateClient(clientId, request, "dummyToken");

        // Assert
        assertEquals("Client updated successfully", response.getMessage());
        assertEquals(HttpStatus.OK, response.getStatus());

        // Verify that the userService was called to update the user
        verify(userService).updateUserInSystem(any(UserDTO.class), eq("dummyToken"));

        // Verify that the clientRepository saved the updated client
        verify(clientRepository).save(client);

        // Check if the client details were updated correctly
        assertEquals("New Company", client.getCompany());
        assertEquals("CNE123456", client.getCne());
        assertEquals("Some remark", client.getRemarque());
        assertEquals(newCategory, client.getCategory());
        assertTrue(client.isDisabled());
    }

    //test delete client
    @Test
    void deleteClient_shouldDeleteClientSuccessfully_whenNoVehicles() throws Exception {
        // Arrange
        Long clientId = 1L;
        String token = "dummyToken";

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        doNothing().when(userService).deleteUserFromSystem(client.getUser().getId(), token);
        doNothing().when(clientRepository).delete(client);

        // Act
        MyResponse response = clientService.deleteClient(clientId, token);

        // Assert
        assertEquals("Client deleted successfully", response.getMessage());
        assertEquals(HttpStatus.OK, response.getStatus());
        verify(userService, times(1)).deleteUserFromSystem(client.getUser().getId(), token);
        verify(clientRepository, times(1)).delete(client);
    }


    //test delete client with vehicles
      @Test
    void deleteClient_shouldThrowMyException_whenClientHasVehicles() throws Exception {
        // Arrange
        Long clientId = 1L;
        String token = "dummyToken";

        client.getVehicles().add(new Vehicle()); // Add a vehicle to the client
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        // Act & Assert
        MyException exception = assertThrows(MyException.class, () -> {
            clientService.deleteClient(clientId, token);
        });

        assertEquals("Client has vehicles. Please delete the vehicles first", exception.getResponse().getMessage());
        assertEquals(HttpStatus.NOT_ACCEPTABLE, exception.getResponse().getStatus());
        verify(userService, never()).deleteUserFromSystem(anyLong(), anyString());
        verify(clientRepository, never()).delete(any(Client.class));
    }
    //test delete client not found
    @Test
    void deleteClient_shouldThrowNotFoundException_whenClientNotFound() throws Exception {
        // Arrange
        Long clientId = 1L;
        String token = "dummyToken";

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            clientService.deleteClient(clientId, token);
        });

        assertEquals("Client not found with id: " + clientId, exception.getResponse().getMessage());
        verify(userService, never()).deleteUserFromSystem(anyLong(), anyString());
        verify(clientRepository, never()).delete(any(Client.class));
    }

    //test delete client with exception
    @Test
    void deleteClient_shouldHandleExceptionDuringUserDeletion() throws Exception {
        // Arrange
        Long clientId = 1L;
        String token = "dummyToken";

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        doThrow(new RuntimeException("User deletion failed")).when(userService).deleteUserFromSystem(client.getUser().getId(), token);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clientService.deleteClient(clientId, token);
        });

        assertEquals("User deletion failed", exception.getMessage());
        verify(userService, times(1)).deleteUserFromSystem(client.getUser().getId(), token);
        verify(clientRepository, never()).delete(any(Client.class));
    }

    //test get active and inactive client count

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

    //test get client by id
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
        Client clientInfo = clientService.findClientById(clientId);

        // Assert
        assertNotNull(clientInfo);
        assertEquals("testuser", user.getUsername());
        assertEquals("Test User", user.getName());
        assertEquals("testuser@example.com", user.getEmail());
        assertEquals("1234567890", user.getPhone());
        assertEquals("Test Company", clientInfo.getCompany());
        assertEquals("CNE123456", clientInfo.getCne());
        assertEquals("Category 1", clientInfo.getCategory().getName());
        assertEquals("This is a remark", clientInfo.getRemarque());
        assertFalse(clientInfo.isDisabled());

        verify(clientRepository, times(1)).findById(clientId);
    }

    //test get client by id not found
    @Test
    void getClientInfoById_shouldThrowNotFoundException_whenClientDoesNotExist() {
        // Arrange
        Long clientId = 1L;
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            clientService.getClientById(clientId);
        });

        assertEquals("Client not found with id: " + clientId, exception.getMessage());
        verify(clientRepository, times(1)).findById(clientId);
    }

    //test filter client
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