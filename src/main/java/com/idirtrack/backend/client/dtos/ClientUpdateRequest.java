package com.idirtrack.backend.client.dtos;

import lombok.Data;

@Data
public class ClientUpdateRequest {
  private String username;
    private String name;
    private String email;
    private String phone;
    private String company;
    private String cne;
    private Long categoryId;
    private String password;
    private String remarque;
    private boolean isDisabled;

    public boolean isDisabled() {
        return isDisabled;
    }
    public void setDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }
}
