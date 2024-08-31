package com.idirtrack.backend.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.idirtrack.backend.sim.Sim;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import com.idirtrack.backend.utils.BasicEntity;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "client-category")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ClientCategory extends BasicEntity {
  
  //name 

    @NotBlank(message = "Name is mandatory")
    @Column(name = "name", nullable = false, unique = true)
  private String name;

//one to many
  @OneToMany(mappedBy = "category")
  @JsonBackReference
  private List<Client> clients;


}
