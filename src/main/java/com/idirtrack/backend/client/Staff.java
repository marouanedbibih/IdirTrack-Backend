package com.idirtrack.backend.client;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.idirtrack.backend.utils.BasicEntity;
@Entity
@Table(name = "Staff")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Staff extends BasicEntity {
   
    
    private String name;
    private String phone;
    private String position;
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
    
}
