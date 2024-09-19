package com.idirtrack.backend.client;

import java.util.List;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.idirtrack.backend.errors.AlreadyExistException;
import com.idirtrack.backend.errors.MyException;
import com.idirtrack.backend.errors.NotFoundException;
import com.idirtrack.backend.utils.ErrorResponse;
import com.idirtrack.backend.utils.MyResponse;

import com.idirtrack.backend.traccar.TraccarUserService;
import com.idirtrack.backend.user.User;
import com.idirtrack.backend.user.UserDTO;
import com.idirtrack.backend.user.UserRole;
import com.idirtrack.backend.user.UserService;

import org.springframework.data.domain.Page;

import jakarta.transaction.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

import com.idirtrack.backend.client.dtos.ClientDTO;
import com.idirtrack.backend.client.dtos.ClientRequest;
import com.idirtrack.backend.client.dtos.ClientTableDTO;
import com.idirtrack.backend.client.dtos.ClientUpdateRequest;

@Service
@RequiredArgsConstructor
public class ClientService {

        private final ClientRepository clientRepository;
        private final UserService userService;
        private final ClientCategoryRepository clientCategoryRepository;

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
                // Get Clients
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

        // Get list of clients with pagination
        public MyResponse getListOfClients(int page, int size) {

                Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
                Page<Client> clientsPage = clientRepository.findAll(pageable);

                if (clientsPage.getContent().isEmpty()) {
                        return MyResponse.builder()
                                        .message("No clients found")
                                        .status(HttpStatus.OK)
                                        .build();
                } else {
                        List<ClientTableDTO> clientsDto = clientsPage.stream()
                                        .map(client -> {
                                                return this.buildClientTableDTO(client);
                                        }).collect(Collectors.toList());

                        Map<String, Object> metaData = Map.of(
                                        "currentPage", clientsPage.getNumber() + 1,
                                        "size", clientsPage.getSize(),
                                        "totalPages", clientsPage.getTotalPages(),
                                        "totalElements", clientsPage.getTotalElements());

                        return MyResponse.builder()
                                        .data(clientsDto)
                                        .metadata(metaData)
                                        .message("Successfully retrieved clients")
                                        .status(HttpStatus.OK)
                                        .build();
                }

        }

        // Service to create a new client
        @Transactional
        public MyResponse createClient(ClientRequest clientRequest, String bearerToken)
                        throws AlreadyExistException, NotFoundException, MyException {

                // Check if the username, email are already taken
                userService.isUserExistInSystem(
                                clientRequest.getUsername(),
                                clientRequest.getEmail());

                // Verify category exists
                ClientCategory category = clientCategoryRepository.findById(clientRequest.getCategoryId())
                                .orElseThrow(() -> new NotFoundException(ErrorResponse.builder()
                                                .message("Category not found with id: " + clientRequest.getCategoryId())
                                                .status(HttpStatus.NOT_FOUND)
                                                .build()));

                // Save the user in the System
                UserDTO userDTO = UserDTO.builder()
                                .username(clientRequest.getUsername())
                                .name(clientRequest.getName())
                                .email(clientRequest.getEmail())
                                .phone(clientRequest.getPhone())
                                .password(clientRequest.getPassword())
                                .role(UserRole.CLIENT)
                                .build();
                User user = userService.saveUserInSystem(userDTO, bearerToken);

                // Save the client in the database
                Client client = Client.builder()
                                .user(user)
                                .company(clientRequest.getCompany())
                                .cne(clientRequest.getCne())
                                .category(category)
                                .remarque(clientRequest.getRemarque())
                                .build();
                clientRepository.save(client);

                // Return the response
                return MyResponse.builder()
                                .message("Client created successfully")
                                .status(HttpStatus.CREATED)
                                .build();

        }

        // search clients
        public MyResponse searchClients(String keyword, int page, int size) {
                Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
                Page<Client> clientsPage = clientRepository.searchClients(keyword, pageable);

                if (clientsPage.getContent().isEmpty()) {
                        return MyResponse.builder()
                                        .message("No clients found")
                                        .status(HttpStatus.OK)
                                        .build();
                } else {
                        List<ClientTableDTO> clientsDto = clientsPage.stream()
                                        .map(client -> {
                                                return this.buildClientTableDTO(client);
                                        }).collect(Collectors.toList());

                        Map<String, Object> metaData = Map.of(
                                        "currentPage", clientsPage.getNumber() + 1,
                                        "size", clientsPage.getSize(),
                                        "totalPages", clientsPage.getTotalPages(),
                                        "totalElements", clientsPage.getTotalElements());

                        return MyResponse.builder()
                                        .data(clientsDto)
                                        .metadata(metaData)
                                        .message("Successfully retrieved clients")
                                        .status(HttpStatus.OK)
                                        .build();
                }

        }

        // Service to delete a client
        @Transactional
        public MyResponse deleteClient(Long id, String bearerToken) throws NotFoundException, MyException {
                // Find the client by ID or throw a NotFoundException if not found
                Client client = this.utilToFindClientById(id);
                // Check if the client has vehicles
                if (!client.getVehicles().isEmpty()) {
                        throw new MyException(ErrorResponse.builder()
                                        .message("Client has vehicles. Please delete the vehicles first")
                                        .status(HttpStatus.NOT_ACCEPTABLE)
                                        .build());
                }
                // Delete the user from the System
                userService.deleteUserFromSystem(client.getUser().getId(), bearerToken);
                clientRepository.delete(client);
                // Return the response
                return MyResponse.builder()
                                .message("Client deleted successfully")
                                .status(HttpStatus.OK)
                                .build();
        }

        // get total clients
        public long getTotalClients() {
                return clientRepository.count();
        }

        // get number of clients active and inactive
        public MyResponse getActiveAndInactiveClientCount() {
                long activeClients = clientRepository.countActiveClients();
                long inactiveClients = clientRepository.countInactiveClients();

                Map<String, Object> data = Map.of(
                                "activeClients", activeClients,
                                "inactiveClients", inactiveClients);

                return MyResponse.builder()
                                .data(data)
                                .message("Successfully retrieved active and inactive client counts")
                                .status(HttpStatus.OK)
                                .build();
        }

        // Filter clients by category and active status
        public MyResponse filterClients(Long categoryId, boolean isDisabled, int page, int size) {
                Pageable pageable = PageRequest.of(page - 1, size);
                Page<Client> clients = clientRepository.filterClients(categoryId, isDisabled, pageable);

                if (clients.getContent().isEmpty()) {
                        return MyResponse.builder()
                                        .message("No clients found")
                                        .status(HttpStatus.OK)
                                        .build();
                } else {
                        List<ClientTableDTO> clientsDto = clients.stream()
                                        .map(client -> {
                                                return this.buildClientTableDTO(client);
                                        }).collect(Collectors.toList());

                        Map<String, Object> metaData = Map.of(
                                        "currentPage", clients.getNumber() + 1,
                                        "size", clients.getSize(),
                                        "totalPages", clients.getTotalPages(),
                                        "totalElements", clients.getTotalElements());

                        return MyResponse.builder()
                                        .data(clientsDto)
                                        .metadata(metaData)
                                        .message("Successfully retrieved clients")
                                        .status(HttpStatus.OK)
                                        .build();
                }
        }

        // Update client info
        @Transactional
        public MyResponse updateClient(Long clientId, ClientUpdateRequest request, String bearerToken)
                        throws NotFoundException, AlreadyExistException {
                // Find the client by ID or throw a NotFoundException if not found
                Client client = this.utilToFindClientById(clientId);
                // Find the Category by ID or throw a NotFoundException if not found
                ClientCategory category = clientCategoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> new NotFoundException(
                                                "Category not found with id: " + request.getCategoryId()));

                // Check if the username and email are already taken except for the current
                // client
                userService.isUserExistInSystemExcept(request.getUsername(), request.getEmail(),
                                client.getUser().getId());

                // Update user in the System
                UserDTO userDTO = UserDTO.builder()
                                .id(client.getUser().getId())
                                .username(request.getUsername())
                                .name(request.getName())
                                .email(request.getEmail())
                                .phone(request.getPhone())
                                .password(request.getPassword())
                                .role(client.getUser().getRole())
                                .traccarId(client.getUser().getTraccarId())
                                .build();
                userService.updateUserInSystem(userDTO, bearerToken);

                // Update the client in the database
                client.setCompany(request.getCompany());
                client.setCne(request.getCne());
                client.setCategory(category);
                client.setRemarque(request.getRemarque());
                client.setDisabled(request.isDisabled());
                clientRepository.save(client);

                return MyResponse.builder()
                                .message("Client updated successfully")
                                .status(HttpStatus.OK)
                                .build();
        }

        // get client by id
        public MyResponse getClientById(Long id) throws NotFoundException {

                Client client = clientRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Client not found with id: " + id));
                ClientDTO clientDTO = this.builClientDTO(client);

                return MyResponse.builder()
                                .data(clientDTO)
                                .status(HttpStatus.OK)
                                .build();
        }

        // Build the client DTO for Table
        private ClientTableDTO buildClientTableDTO(Client client) {
                return ClientTableDTO.builder()
                                .clientId(client.getId())
                                .userId(client.getUser().getId())
                                .username(client.getUser().getUsername())
                                .name(client.getUser().getName())
                                .email(client.getUser().getEmail())
                                .phone(client.getUser().getPhone())
                                .company(client.getCompany())
                                .cne(client.getCne())
                                .categoryName(client.getCategory().getName())
                                .remarque(client.getRemarque())
                                .isDisabled(client.isDisabled())
                                .totalVehicles(client.getVehicles().size())
                                .build();
        }

        private ClientDTO builClientDTO(Client client) {
                return ClientDTO.builder()
                                .id(client.getId())
                                .username(client.getUser().getUsername())
                                .name(client.getUser().getName())
                                .email(client.getUser().getEmail())
                                .phone(client.getUser().getPhone())
                                .company(client.getCompany())
                                .cne(client.getCne())
                                .categoryId(client.getCategory().getId())
                                .categoryName(client.getCategory().getName())
                                .remarque(client.getRemarque())
                                .isDisabled(client.isDisabled())
                                .build();
        }

        public MyResponse searchClientsDropdown(String keyword) {
                List<Client> clients = clientRepository.searchClientsDropdown(keyword);

                if (clients.isEmpty()) {
                        return MyResponse.builder()
                                        .message("No clients found")
                                        .status(HttpStatus.OK)
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

        private Client utilToFindClientById(Long id) throws NotFoundException {
                return clientRepository.findById(id)
                                .orElseThrow(() -> {
                                        return new NotFoundException(ErrorResponse.builder()
                                                        .message("Client not found with id: " + id)
                                                        .build());
                                });
        }

        //Filter clients by category and active status

        public MyResponse filterClientsByCategoryAndStatus(Long categoryId, boolean isDisabled, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Client> clients = clientRepository.findByCategoryAndStatus(categoryId, isDisabled, pageable);

        Map<String, Object> metadata = Map.of(
        "totalPages", clients.getTotalPages(),
        "totalElements", clients.getTotalElements(),
        "currentPage", clients.getNumber(),
        "size", clients.getSize()
        );

        return MyResponse.builder()
        .data(clients.getContent())
        .metadata(metadata)
        .message("Successfully filtered clients by category and status")
        .status(HttpStatus.OK)
        .build();
        }

}
