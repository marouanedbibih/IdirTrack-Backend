package com.idirtrack.backend.staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.idirtrack.backend.client.Client;



public interface StaffRepository extends JpaRepository<Staff, Long> {
    boolean existsByClientAndName(Client client, String name);

    /**
     * Search for staff members by name, phone, position, client name, or client company
     * @param search
     * @param pageable
     * @return
     */
    @Query("SELECT s FROM Staff s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.position) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.client.user.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.client.company) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Staff> searchStaff(@Param("search") String search, Pageable pageable);

    boolean existsByName(String name);

}
