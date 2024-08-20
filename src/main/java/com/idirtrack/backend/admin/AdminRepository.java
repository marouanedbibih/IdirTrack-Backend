package com.idirtrack.backend.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    // Find all admins with pagination
    Page<Admin> findAll(Pageable pageable);
    
}
