package com.idirtrack.backend.client.dtos;

import com.idirtrack.backend.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientDto {
    private Long id;
    private UserDTO user;
}
