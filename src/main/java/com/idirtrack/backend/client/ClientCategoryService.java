package com.idirtrack.backend.client;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.idirtrack.backend.client.dtos.ClientCategoryDto;
import com.idirtrack.backend.errors.AlreadyExistException;
import com.idirtrack.backend.errors.NotFoundException;
import com.idirtrack.backend.utils.MyResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientCategoryService {
  private final ClientCategoryRepository clientCategoryRepository;
  private final ClientRepository clientRepository;
  

  public ClientCategory getClientCategoryById(Long id) throws NotFoundException {
    return clientCategoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Client category not found with id: " + id));
}


// Create a new ClientCategory
public ClientCategory createClientCategory(ClientCategory clientCategory) throws AlreadyExistException {
  if (clientCategoryRepository.existsByName(clientCategory.getName())) {
      throw new AlreadyExistException("Category name must be unique");
  }
  return clientCategoryRepository.save(clientCategory);
}

// Update an existing ClientCategory
public ClientCategory updateClientCategory(Long id, ClientCategory clientCategoryDetails) throws NotFoundException, AlreadyExistException {
  ClientCategory clientCategory = getClientCategoryById(id);

  // Check for name uniqueness only if the name has changed
  if (!clientCategory.getName().equals(clientCategoryDetails.getName()) &&
      clientCategoryRepository.existsByName(clientCategoryDetails.getName())) {
      throw new AlreadyExistException("Category name must be unique");
  }

  clientCategory.setName(clientCategoryDetails.getName());
  // Update other fields if necessary

  return clientCategoryRepository.save(clientCategory);

}

  public void deleteClientCategory(Long id) throws NotFoundException {
    ClientCategory clientCategory = clientCategoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Client category not found with id: " + id));
    clientCategoryRepository.delete(clientCategory);
  }

  //get all categories with pagination and total count
  public MyResponse getCategoriesWithClientCount(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("name").ascending());
        Page<ClientCategoryDto> categories = clientCategoryRepository.findAll(pageable)
            .map(category -> ClientCategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .totalClients(clientRepository.countByCategoryId(category.getId()))
                .build());

        Map<String, Object> metadata = Map.of(
            "totalPages", categories.getTotalPages(),
            "totalElements", categories.getTotalElements(),
            "currentPage", categories.getNumber(),
            "size", categories.getSize()
        );

        return MyResponse.builder()
            .data(categories.getContent())
            .metadata(metadata)
            .message("Categories retrieved successfully")
            .status(HttpStatus.OK)
            .build();
    }
}
