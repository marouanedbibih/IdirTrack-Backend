package com.idirtrack.backend.staff;
import com.idirtrack.backend.client.Client;
import com.idirtrack.backend.utils.BasicEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "staffs")
@SuperBuilder
public class Staff extends BasicEntity {

    private String name;
    private String phone;
    private String position;
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
}

