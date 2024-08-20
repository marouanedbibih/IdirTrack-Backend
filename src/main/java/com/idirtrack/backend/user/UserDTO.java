package com.idirtrack.backend.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String password;
    private Long traccarId;

    private UserRole role;
}
