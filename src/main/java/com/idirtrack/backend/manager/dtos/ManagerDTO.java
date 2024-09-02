package com.idirtrack.backend.manager.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ManagerDTO {
    private Long userId;
    private Long managerId;
    private Long traccarId;
    private String username;
    private String name;
    private String email;
    private String phone;
}
