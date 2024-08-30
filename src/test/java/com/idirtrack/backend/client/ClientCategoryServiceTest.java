package com.idirtrack.backend.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.idirtrack.backend.errors.AlreadyExistException;
import com.idirtrack.backend.errors.NotFoundException;

public class ClientCategoryServiceTest {
  
    @Mock
    private ClientCategoryRepository clientCategoryRepository;

    @InjectMocks
    private ClientCategoryService clientCategoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
     @Test
void createClientCategory_shouldCreateCategory_whenNameIsUnique() throws AlreadyExistException {
    // Arrange
    ClientCategory clientCategory = ClientCategory.builder().name("UniqueName").build();
    when(clientCategoryRepository.existsByName(clientCategory.getName())).thenReturn(false);
    when(clientCategoryRepository.save(clientCategory)).thenReturn(clientCategory);

    // Act
    ClientCategory createdCategory = clientCategoryService.createClientCategory(clientCategory);

    // Assert
    assertNotNull(createdCategory);
    assertEquals("UniqueName", createdCategory.getName());
    verify(clientCategoryRepository, times(1)).save(clientCategory);
}

 @Test
    void updateClientCategory_shouldThrowException_whenNameIsNotUnique() throws NotFoundException {
        // Arrange
        Long id = 1L;
        ClientCategory existingCategory = ClientCategory.builder().id(id).name("OldName").build();
        ClientCategory updatedDetails = ClientCategory.builder().name("DuplicateName").build();

        when(clientCategoryRepository.findById(id)).thenReturn(Optional.of(existingCategory));
        when(clientCategoryRepository.existsByName(updatedDetails.getName())).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistException.class, () -> {
            clientCategoryService.updateClientCategory(id, updatedDetails);
        });

        verify(clientCategoryRepository, never()).save(existingCategory);
    }

    @Test
    void getClientCategoryById_shouldReturnCategory_whenExists() throws NotFoundException {
        // Arrange
        Long id = 1L;
        ClientCategory existingCategory = ClientCategory.builder().id(id).name("CategoryName").build();
        when(clientCategoryRepository.findById(id)).thenReturn(Optional.of(existingCategory));

        // Act
        ClientCategory category = clientCategoryService.getClientCategoryById(id);

        // Assert
        assertNotNull(category);
        assertEquals(id, category.getId());
    }

    @Test
    void getClientCategoryById_shouldThrowException_whenNotFound() {
        // Arrange
        Long id = 1L;
        when(clientCategoryRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            clientCategoryService.getClientCategoryById(id);
        });
    }


     @Test
    void deleteClientCategory_shouldDeleteCategory_whenExists() throws NotFoundException {
        // Arrange
        Long id = 1L;
        ClientCategory clientCategory = ClientCategory.builder().id(id).name("CategoryName").build();
        when(clientCategoryRepository.findById(id)).thenReturn(Optional.of(clientCategory));

        // Act
        clientCategoryService.deleteClientCategory(id);

        // Assert
        verify(clientCategoryRepository, times(1)).delete(clientCategory);
    }

    @Test
    void deleteClientCategory_shouldThrowNotFoundException_whenNotExists() {
        // Arrange
        Long id = 1L;
        when(clientCategoryRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            clientCategoryService.deleteClientCategory(id);
        });

        verify(clientCategoryRepository, never()).delete(any(ClientCategory.class));
    }


}
