package com.idirtrack.backend.client;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.idirtrack.backend.errors.NotFoundException;
import com.idirtrack.backend.utils.ErrorResponse;
import com.idirtrack.backend.utils.MyResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;

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
}
