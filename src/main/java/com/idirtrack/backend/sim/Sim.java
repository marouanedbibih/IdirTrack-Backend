package com.idirtrack.backend.sim;


import com.idirtrack.backend.operator.Operator;
import com.idirtrack.backend.utils.BasicEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sim")
public class Sim extends BasicEntity {

    @Column(name = "pin", nullable = false)
    private String pin;

    @Column(name = "puk", nullable = false)
    private String puk;

    @Column(name = "ccid", nullable = false, unique = true)
    private String ccid;

    @ManyToOne
    @JoinColumn(name = "operator_id")
    private Operator operator;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SimStatus status;
}
