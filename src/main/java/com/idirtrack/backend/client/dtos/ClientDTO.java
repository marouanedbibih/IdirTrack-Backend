package com.idirtrack.backend.client.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientDTO {

    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String company;
    private String cne;
    private Long categoryId;
    private String categoryName;
    private String remarque;
    private boolean isDisabled;
}
