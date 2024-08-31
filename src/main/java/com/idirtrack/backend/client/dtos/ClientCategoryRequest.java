package com.idirtrack.backend.client.dtos;

import jakarta.validation.constraints.NotBlank;

public class ClientCategoryRequest {

  @NotBlank(message = "Name is mandatory")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
