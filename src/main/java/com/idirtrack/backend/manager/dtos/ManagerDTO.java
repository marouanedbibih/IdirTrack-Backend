package com.idirtrack.backend.manager.dtos;

import com.idirtrack.backend.user.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ManagerDTO  {
    
    private Long id;
    private UserDTO user;
}
