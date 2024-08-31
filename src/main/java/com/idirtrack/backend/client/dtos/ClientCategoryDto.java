package com.idirtrack.backend.client.dtos;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientCategoryDto {
  
  private Long id;
  private String name;
  private long totalClients;
}
