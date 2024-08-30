package com.idirtrack.backend.vehicle;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VehicleRepository extends JpaRepository<Vehicle, Long>{

    boolean existsByMatricule(String matricule);

    boolean existsByMatriculeAndIdNot(String matricule, Long id);

    @Query("SELECT v FROM Vehicle v WHERE v.matricule LIKE %?1% or v.client.user.name LIKE %?1% or v.client.user.phone LIKE %?1% or v.client.user.email LIKE %?1% or v.client.company LIKE %?1%")
    Page<Vehicle> search(String search, Pageable pageable);
    
    
}
