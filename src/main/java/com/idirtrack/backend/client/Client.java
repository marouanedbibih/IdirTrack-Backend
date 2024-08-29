package com.idirtrack.backend.client;

import com.idirtrack.backend.user.User;
import com.idirtrack.backend.utils.BasicEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "clients")
@Data
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Client extends BasicEntity {
    
        private String company;

        @OneToOne()
        @JoinColumn(name = "user_id")
        private User user;
    
}
