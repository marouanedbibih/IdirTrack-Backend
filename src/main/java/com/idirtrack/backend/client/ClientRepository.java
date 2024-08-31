package com.idirtrack.backend.client;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

  long countByCategoryId(Long categoryId);

  // search client list with pagination
  @Query("SELECT c FROM Client c WHERE c.user.name LIKE %:keyword% OR c.user.email LIKE %:keyword% OR c.user.phone LIKE %:keyword% or c.user.username LIKE %:keyword% or c.company LIKE %:keyword% or c.category.name LIKE %:keyword% or c.cne LIKE %:keyword%")
  Page<Client> searchClients(@Param("keyword") String keyword, Pageable pageable);

  @Query("SELECT COUNT(c) FROM Client c WHERE c.isDisabled = false")
  long countActiveClients();

  @Query("SELECT COUNT(c) FROM Client c WHERE c.isDisabled = true")
  long countInactiveClients();

  @Query("SELECT c FROM Client c WHERE c.category.id = :categoryId AND c.isDisabled = :isDisabled")
  Page<Client> findByCategoryAndStatus(Long categoryId, boolean isDisabled, Pageable pageable);

}
