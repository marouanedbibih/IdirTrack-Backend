package com.idirtrack.backend.client.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientUpdateRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Company is required")
    private String company;

    @NotBlank(message = "CNE is required")
    private String cne;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @Size(max = 250, message = "Remarque must be less than 250 characters")
    private String remarque;

    private String password;  // Optional field, no validation needed

    @NotNull(message = "isDisabled is required")
    private boolean isDisabled;  
}
