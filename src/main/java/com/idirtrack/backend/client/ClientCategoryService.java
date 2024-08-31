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
  

  // Get ClientCategory by ID
  public MyResponse getClientCategoryById(Long id) {
    try {
        ClientCategory clientCategory = clientCategoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Client category not found with id: " + id));
        return MyResponse.builder()
            .data(clientCategory)
            .message("Client category retrieved successfully")
            .status(HttpStatus.OK)
            .build();
    } catch (NotFoundException e) {
        return MyResponse.builder()
            .message(e.getMessage())
            .status(HttpStatus.NOT_FOUND)
            .build();
    }
}


// Create a new ClientCategory
public MyResponse createClientCategory(ClientCategory clientCategory) {
  try {
      if (clientCategoryRepository.existsByName(clientCategory.getName())) {
          throw new AlreadyExistException("Category name must be unique");
      }
      ClientCategory savedCategory = clientCategoryRepository.save(clientCategory);
      return MyResponse.builder()
          .data(savedCategory)
          .message("Client category created successfully")
          .status(HttpStatus.CREATED)
          .build();
  } catch (AlreadyExistException e) {
      return MyResponse.builder()
          .message(e.getMessage())
          .status(HttpStatus.CONFLICT)
          .build();
  }
}

// Update an existing ClientCategory
public MyResponse updateClientCategory(Long id, ClientCategory clientCategoryDetails) {
  try {
      ClientCategory clientCategory = getClientCategoryByIdInternal(id);

      if (!clientCategory.getName().equals(clientCategoryDetails.getName()) &&
          clientCategoryRepository.existsByName(clientCategoryDetails.getName())) {
          throw new AlreadyExistException("Category name must be unique");
      }

      clientCategory.setName(clientCategoryDetails.getName());
      ClientCategory updatedCategory = clientCategoryRepository.save(clientCategory);

      return MyResponse.builder()
          .data(updatedCategory)
          .message("Client category updated successfully")
          .status(HttpStatus.OK)
          .build();
  } catch (NotFoundException | AlreadyExistException e) {
      return MyResponse.builder()
          .message(e.getMessage())
          .status(e instanceof NotFoundException ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT)
          .build();
  }
}

  // Delete a ClientCategory
  public MyResponse deleteClientCategory(Long id) {
    try {
        ClientCategory clientCategory = getClientCategoryByIdInternal(id);
        clientCategoryRepository.delete(clientCategory);
        return MyResponse.builder()
            .message("Client category deleted successfully")
            .status(HttpStatus.OK)
            .build();
    } catch (NotFoundException e) {
        return MyResponse.builder()
            .message(e.getMessage())
            .status(HttpStatus.NOT_FOUND)
            .build();
    }
}
// Helper method to retrieve a ClientCategory by ID and throw NotFoundException if not found
private ClientCategory getClientCategoryByIdInternal(Long id) throws NotFoundException {
  return clientCategoryRepository.findById(id)
      .orElseThrow(() -> new NotFoundException("Client category not found with id: " + id));
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
            "currentPage", categories.getNumber() + 1,
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
