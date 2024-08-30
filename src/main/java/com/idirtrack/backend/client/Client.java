package com.idirtrack.backend.client;

import com.idirtrack.backend.staff.Staff;
import com.idirtrack.backend.user.User;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import com.idirtrack.backend.utils.BasicEntity;
import java.util.List;

@Entity
@Table(name = "client")
@Data
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Client  extends BasicEntity{

  //company
  private String company;
  //cne
  private String cne;
  //remarque
  private String remarque;
  //isDisabled
  private boolean isDisabled;

  //category
  @ManyToOne
  @JoinColumn(name = "category_id")
  private ClientCategory category;

  //user
    @OneToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    //staff
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Staff> staff;
}
