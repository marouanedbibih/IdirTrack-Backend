package com.idirtrack.backend.admin;
import com.idirtrack.backend.user.User;
import com.idirtrack.backend.utils.BasicEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "admins")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Admin extends BasicEntity {

    @OneToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;
}
