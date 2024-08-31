package com.idirtrack.backend.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
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

import com.idirtrack.backend.client.dtos.ClientCategoryDto;
import com.idirtrack.backend.errors.AlreadyExistException;
import com.idirtrack.backend.errors.NotFoundException;
import com.idirtrack.backend.utils.MyResponse;

public class ClientCategoryServiceTest {
  
    @Mock
    private ClientCategoryRepository clientCategoryRepository;

    @InjectMocks
    private ClientCategoryService clientCategoryService;

    @Mock
    private ClientRepository clientRepository; 

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
     @Test
    void getClientCategoryById_shouldReturnCategory_whenExists() {
        // Arrange
        Long id = 1L;
        ClientCategory existingCategory = ClientCategory.builder().id(id).name("CategoryName").build();
        when(clientCategoryRepository.findById(id)).thenReturn(Optional.of(existingCategory));

        // Act
        MyResponse response = clientCategoryService.getClientCategoryById(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("CategoryName", ((ClientCategory) response.getData()).getName());
    }

    @Test
    void getClientCategoryById_shouldReturnNotFound_whenNotExists() {
        // Arrange
        Long id = 1L;
        when(clientCategoryRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        MyResponse response = clientCategoryService.getClientCategoryById(id);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertEquals("Client category not found with id: " + id, response.getMessage());
    }

    @Test
    void createClientCategory_shouldCreateCategory_whenNameIsUnique() {
        // Arrange
        ClientCategory clientCategory = ClientCategory.builder().name("UniqueName").build();
        when(clientCategoryRepository.existsByName(clientCategory.getName())).thenReturn(false);
        when(clientCategoryRepository.save(clientCategory)).thenReturn(clientCategory);

        // Act
        MyResponse response = clientCategoryService.createClientCategory(clientCategory);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals("UniqueName", ((ClientCategory) response.getData()).getName());
        verify(clientCategoryRepository, times(1)).save(clientCategory);
    }

    @Test
    void createClientCategory_shouldReturnConflict_whenNameIsNotUnique() {
        // Arrange
        ClientCategory clientCategory = ClientCategory.builder().name("DuplicateName").build();
        when(clientCategoryRepository.existsByName(clientCategory.getName())).thenReturn(true);

        // Act
        MyResponse response = clientCategoryService.createClientCategory(clientCategory);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatus());
        assertEquals("Category name must be unique", response.getMessage());
        verify(clientCategoryRepository, never()).save(clientCategory);
    }

    @Test
    void updateClientCategory_shouldUpdateCategory_whenNameIsUnique() throws NotFoundException {
        // Arrange
        Long id = 1L;
        ClientCategory existingCategory = ClientCategory.builder().id(id).name("OldName").build();
        ClientCategory updatedDetails = ClientCategory.builder().name("NewName").build();

        when(clientCategoryRepository.findById(id)).thenReturn(Optional.of(existingCategory));
        when(clientCategoryRepository.existsByName(updatedDetails.getName())).thenReturn(false);
        when(clientCategoryRepository.save(existingCategory)).thenReturn(existingCategory);

        // Act
        MyResponse response = clientCategoryService.updateClientCategory(id, updatedDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("NewName", ((ClientCategory) response.getData()).getName());
        verify(clientCategoryRepository, times(1)).save(existingCategory);
    }

    @Test
    void updateClientCategory_shouldReturnConflict_whenNameIsNotUnique() throws NotFoundException {
        // Arrange
        Long id = 1L;
        ClientCategory existingCategory = ClientCategory.builder().id(id).name("OldName").build();
        ClientCategory updatedDetails = ClientCategory.builder().name("DuplicateName").build();

        when(clientCategoryRepository.findById(id)).thenReturn(Optional.of(existingCategory));
        when(clientCategoryRepository.existsByName(updatedDetails.getName())).thenReturn(true);

        // Act
        MyResponse response = clientCategoryService.updateClientCategory(id, updatedDetails);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatus());
        assertEquals("Category name must be unique", response.getMessage());
        verify(clientCategoryRepository, never()).save(existingCategory);
    }

    @Test
    void deleteClientCategory_shouldDeleteCategory_whenExists() throws NotFoundException {
        // Arrange
        Long id = 1L;
        ClientCategory existingCategory = ClientCategory.builder().id(id).name("CategoryName").build();

        when(clientCategoryRepository.findById(id)).thenReturn(Optional.of(existingCategory));

        // Act
        MyResponse response = clientCategoryService.deleteClientCategory(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("Client category deleted successfully", response.getMessage());
        verify(clientCategoryRepository, times(1)).delete(existingCategory);
    }

    @Test
    void deleteClientCategory_shouldReturnNotFound_whenNotExists() {
        // Arrange
        Long id = 1L;
        when(clientCategoryRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        MyResponse response = clientCategoryService.deleteClientCategory(id);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertEquals("Client category not found with id: " + id, response.getMessage());
        verify(clientCategoryRepository, never()).delete(any());
    }

    @Test
    void getCategoriesWithClientCount_shouldReturnPaginatedCategories() {
        // Arrange
        int page = 1;
        int size = 10;
        
        ClientCategory category = ClientCategory.builder()
            .id(1L)
            .name("CategoryName")
            .build();
        
        Page<ClientCategory> categoryPage = new PageImpl<>(Collections.singletonList(category));
        
        when(clientCategoryRepository.findAll(any(Pageable.class))).thenReturn(categoryPage);
        when(clientRepository.countByCategoryId(anyLong())).thenReturn(5L);

        // Act
        MyResponse response = clientCategoryService.getCategoriesWithClientCount(page, size);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getData());
        assertEquals(1, ((java.util.List<ClientCategoryDto>) response.getData()).size());
        assertEquals("CategoryName", ((ClientCategoryDto) ((java.util.List<?>) response.getData()).get(0)).getName());
        verify(clientCategoryRepository, times(1)).findAll(any(Pageable.class));
    }
}