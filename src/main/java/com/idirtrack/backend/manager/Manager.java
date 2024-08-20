package com.idirtrack.backend.manager;

import com.idirtrack.backend.user.User;
import com.idirtrack.backend.utils.BasicEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;


@Entity
@Table(name = "managers")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Manager extends BasicEntity {
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;
}
