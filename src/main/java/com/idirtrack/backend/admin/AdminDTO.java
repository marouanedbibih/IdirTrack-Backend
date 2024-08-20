package com.idirtrack.backend.admin;

import com.idirtrack.backend.user.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDTO {
    private Long adminId;
    private UserDTO user;
}
