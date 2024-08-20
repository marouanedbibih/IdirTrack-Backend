package com.idirtrack.backend.operator;

import java.sql.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.idirtrack.backend.sim.Sim;
import com.idirtrack.backend.utils.BasicEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper=false)
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "operator")
public class Operator extends BasicEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "operator")
    @JsonBackReference
    private List<Sim> sims;
}
