package com.idirtrack.backend.boitier;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoitierRepository extends JpaRepository<Boitier, Long> {
    
    Page<Boitier> findAll(Pageable pageable);

    Page<Boitier> findAllByVehicleIsNull(Pageable pageRequest);

    List<Boitier> findAllByVehicleIsNull();
}
