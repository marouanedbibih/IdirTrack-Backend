package com.idirtrack.backend.client;

import org.springframework.stereotype.Service;

import com.idirtrack.backend.errors.AlreadyExistException;
import com.idirtrack.backend.errors.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientCategoryService {
  private final ClientCategoryRepository clientCategoryRepository;
  

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
}
