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
    // Get category by id
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/api/v1/client/category/{id}")
    public ResponseEntity<MyResponse> getClientCategoryById(@PathVariable Long id) {
        MyResponse response = categoryService.getClientCategoryById(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

     // Create a new category
     @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
     @PostMapping("/api/v1/client/category")
     public ResponseEntity<MyResponse> createClientCategory(@RequestBody @Valid ClientCategory clientCategory) {
         MyResponse response = categoryService.createClientCategory(clientCategory);
         return ResponseEntity.status(response.getStatus()).body(response);
     }

    // Update category by id
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @PutMapping("/api/v1/client/category/{id}")
    public ResponseEntity<MyResponse> updateClientCategory(@PathVariable Long id,
            @RequestBody @Valid ClientCategory clientCategoryDetails) {
        MyResponse response = categoryService.updateClientCategory(id, clientCategoryDetails);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

   // Delete category by id
   @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
   @DeleteMapping("/api/v1/client/category/{id}")
   public ResponseEntity<MyResponse> deleteClientCategory(@PathVariable Long id) {
       MyResponse response = categoryService.deleteClientCategory(id);
       return ResponseEntity.status(response.getStatus()).body(response);
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
