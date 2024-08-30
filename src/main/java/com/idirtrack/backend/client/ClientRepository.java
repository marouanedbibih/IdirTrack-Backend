package com.idirtrack.backend.client;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ClientRepository  extends JpaRepository<Client, Long> {
   

  //search client  list with pagination
  @Query("SELECT c FROM Client c WHERE " +
  "(c.user.name LIKE CONCAT('%', :keyword, '%') OR " +
  "c.user.username LIKE CONCAT('%', :keyword, '%') OR " +
  "c.user.email LIKE CONCAT('%', :keyword, '%') OR " +
  "c.user.phone LIKE CONCAT('%', :keyword, '%') OR " +
  "c.company LIKE CONCAT('%', :keyword, '%') OR " +
  "c.category.name LIKE CONCAT('%', :keyword, '%'))")
Page<Client> searchClients(@Param("keyword") String keyword, Pageable pageable);

  
}
