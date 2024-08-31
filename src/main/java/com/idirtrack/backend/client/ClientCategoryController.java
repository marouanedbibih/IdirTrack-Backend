package com.idirtrack.backend.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.idirtrack.backend.basics.BasicResponse;
import com.idirtrack.backend.errors.AlreadyExistException;
import com.idirtrack.backend.errors.NotFoundException;
import com.idirtrack.backend.utils.MyResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ClientCategoryController {

    private final ClientCategoryService categoryService;

    // Get category by id
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/api/v1/client/category/{id}")
    public ResponseEntity<ClientCategory> getClientCategoryById(@PathVariable Long id) {
        try {
            ClientCategory clientCategory = categoryService.getClientCategoryById(id);
            return ResponseEntity.ok(clientCategory);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    // Create a new category
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @PostMapping("/api/v1/client/category")
    public ResponseEntity<BasicResponse> createClientCategory(@RequestBody @Valid ClientCategory clientCategory) {
        try {
            categoryService.createClientCategory(clientCategory);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    BasicResponse.builder().message("Category created successfully").status(HttpStatus.CREATED)
                            .build());
        } catch (AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    BasicResponse.builder().message(e.getMessage()).status(HttpStatus.CONFLICT).build());
        }
    }

    // Update category by id
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @PutMapping("/api/v1/client/category/{id}")
    public ResponseEntity<BasicResponse> updateClientCategory(@PathVariable Long id,
            @RequestBody @Valid ClientCategory clientCategoryDetails) {
        try {
            categoryService.updateClientCategory(id, clientCategoryDetails);
            return ResponseEntity.ok(
                    BasicResponse.builder().message("Category updated successfully").status(HttpStatus.OK).build());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    BasicResponse.builder().message(e.getMessage()).status(HttpStatus.NOT_FOUND).build());
        } catch (AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    BasicResponse.builder().message(e.getMessage()).status(HttpStatus.CONFLICT).build());
        }
    }

    // Delete category by id
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @DeleteMapping("/api/v1/client/category/{id}")
    public ResponseEntity<BasicResponse> deleteClientCategory(@PathVariable Long id) {
        try {
            categoryService.deleteClientCategory(id);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(BasicResponse.builder()
                            .message("Client category deleted successfully")
                            .status(HttpStatus.OK)
                            .build());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BasicResponse.builder()
                            .message("Client category not found with id: " + id)
                            .status(HttpStatus.NOT_FOUND)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BasicResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message(e.getMessage())
                            .build());
        }
    }

    // Get all categories with pagination and total count of clients
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/api/v1/client/categories")
    public ResponseEntity<?> getCategoriesWithClientCount(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        MyResponse response = categoryService.getCategoriesWithClientCount(page, size);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
