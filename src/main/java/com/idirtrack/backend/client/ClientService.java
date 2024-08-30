package com.idirtrack.backend.client;

import java.util.List;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.idirtrack.backend.errors.NotFoundException;
import com.idirtrack.backend.utils.ErrorResponse;
import com.idirtrack.backend.utils.MyResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.idirtrack.backend.basics.BasicResponse;
import com.idirtrack.backend.basics.MessageType;
import com.idirtrack.backend.basics.MetaData;
import com.idirtrack.backend.traccar.TraccarUserService;
import com.idirtrack.backend.user.User;
import com.idirtrack.backend.user.UserDTO;
import com.idirtrack.backend.user.UserRole;
import com.idirtrack.backend.user.UserService;
import com.idirtrack.backend.basics.BasicException;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import com.idirtrack.backend.utils.MyResponse;

import jakarta.transaction.TransactionScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.idirtrack.backend.client.dtos.ClientDto;
import com.idirtrack.backend.client.dtos.ClientRequest;



@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final UserService userService;
    private final TraccarUserService traccarUserService;

    public Client findClientById(Long id) throws NotFoundException {
        return clientRepository.findById(id)
                .orElseThrow(
                        () -> {
                            return new NotFoundException(ErrorResponse.builder()
                                    .message("Client not found with id: " + id)
                                    .build());
                        });
    }

    // Get list of clients to use in select dropdown
    public MyResponse getClientsDropdown() {

        Pageable page = PageRequest.of(0, 50, Sort.by("id").descending());
        List<Client> clients = clientRepository.findAll(page).getContent();

        if (clients.isEmpty()) {
            return MyResponse.builder()
                    .message("No clients found")
                    .build();
        } else {
            List<ClientDTO> clientDTOs = clients.stream()
                    .map(client -> {
                        return ClientDTO.builder()
                                .id(client.getId())
                                .name(client.getUser().getName())
                                .company(client.getCompany())
                                .build();
                    })
                    .toList();
            return MyResponse.builder()
                    .data(clientDTOs)
                    .status(HttpStatus.OK)
                    .build();
        }
    }




  /**
   * service get all clients
   * @param page
   * @param size
   * @return
   * @throws BasicException
   * 
   */

  public BasicResponse getAllClients(int page, int size) throws BasicException {

    //Create page request
      Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());

    //Get all clients
    Page<Client> clients = clientRepository.findAll(pageable);
    if (clients.isEmpty()) {
      MyResponse response = MyResponse.builder()
        .data(null)
        .message("No clients found")
        .status(HttpStatus.OK)
        .build();
  
    }
    //build the list of clients DtOs
    List<ClientDto> clientsDto = clients.stream()
      .map(client -> {
        User user = client.getUser();
        UserDTO userDto = UserDTO.builder()
          .id(user.getId())
          .username(user.getUsername())
          .name(user.getName())
          .email(user.getEmail())
          .phone(user.getPhone())
          .password(user.getPassword())
          .traccarId(user.getTraccarId())
          .role(user.getRole())
          .build();
        return ClientDto.builder()
          .id(client.getId())
          .user(userDto)
          .build();
      }).collect(Collectors.toList());
     // Build the metadata
                MetaData metaData = MetaData.builder()
                                .currentPage(page)
                                .size(size)
                                .totalPages(clients.getTotalPages())
                                .totalElements((int) clients.getTotalElements())
                                .build();
                // Return the list of Manager DTOs
                return BasicResponse.builder()
                                .content(clientsDto)
                                .metadata(metaData)
                                .status(HttpStatus.OK)
                                .build();

    
  }

  /**
   * Service to create a client with a one device in traccar
   * @param clientRequest 
   * @return BasicResponse
   * @throws BasicException
   */
  @Transactional
  public BasicResponse createClient(ClientRequest clientRequest, String token) throws BasicException {
    // Get the user from the request
     userService.isUsernameTaken(clientRequest.getUsername());
                userService.isEmailTaken(clientRequest.getEmail());
                userService.isPhoneTaken(clientRequest.getPhone());

                // Build the user DTO
                UserDTO userDTO = UserDTO.builder()
                                .username(clientRequest.getUsername())
                                .name(clientRequest.getName())
                                .email(clientRequest.getEmail())
                                .phone(clientRequest.getPhone())
                                .password(clientRequest.getPassword())
                                .role(UserRole.CLIENT)
                                .build();

                // Save the user in Traccar
                Map<String, Object> clientTracCar = traccarUserService.createUser(userDTO, UserRole.CLIENT, token);
                if (clientTracCar != null) {
                        // Get ID from response
                        Long id = Long.parseLong(clientTracCar.get("id").toString());
                        userDTO.setTraccarId(id);
                        // Save user in database
                        User user = userService.createNewUserInDB(userDTO);

                        // Save client in database
                        Client client  = Client.builder()
                                        .user(user)
                                        .build();
                        client = clientRepository.save(client);

                        // Return a success response
                        return BasicResponse.builder()
                                        .message("Client created successfully")
                                        .status(HttpStatus.CREATED)
                                        .build();
                }

                throw new BasicException(BasicResponse.builder()
                                // .error(BasicError.of("client", "Failed to create manager in Traccar"))
                                .messageType(MessageType.ERROR)
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .build());
        }

        
        //search clients
        public BasicResponse searchClients(String keyword, int page, int size) throws BasicException {
                // Create page request
                Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());

                // Search clients
                Page<Client> clients = clientRepository.searchClients(keyword, pageable);
                if (clients.isEmpty()) {
                        return BasicResponse.builder()
                                        .content(null)
                                        .message("No clients found")
                                        .status(HttpStatus.OK)
                                        .build();
                }

                // Build the list of clients DTOs
                List<ClientDto> clientsDto = clients.stream()
                                .map(client -> {
                                        User user = client.getUser();
                                        UserDTO userDto = UserDTO.builder()
                                                        .id(user.getId())
                                                        .username(user.getUsername())
                                                        .name(user.getName())
                                                        .email(user.getEmail())
                                                        .phone(user.getPhone())
                                                        .password(user.getPassword())
                                                        .traccarId(user.getTraccarId())
                                                        .role(user.getRole())
                                                        .build();
                                        return ClientDto.builder()
                                                        .id(client.getId())
                                                        .user(userDto)
                                                        .build();
                                }).collect(Collectors.toList());

                // Build the metadata
                MetaData metaData = MetaData.builder()
                                .currentPage(page)
                                .size(size)
                                .totalPages(clients.getTotalPages())
                                .totalElements((int) clients.getTotalElements())
                                .build();

                // Return the list of Manager DTOs
                return BasicResponse.builder()
                                .content(clientsDto)
                                .metadata(metaData)
                                .status(HttpStatus.OK)
                                .build();
        }



}
