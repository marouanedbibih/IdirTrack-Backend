package com.idirtrack.backend.client;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
                                .role(UserRole.MANAGER)
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



}
