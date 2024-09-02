package com.idirtrack.backend.client;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Nullable;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

  long countByCategoryId(Long categoryId);

  // Search client list with pagination
  @Query("SELECT c FROM Client c WHERE c.user.name LIKE %:keyword% OR c.user.email LIKE %:keyword% OR c.user.phone LIKE %:keyword% or c.user.username LIKE %:keyword% or c.company LIKE %:keyword% or c.category.name LIKE %:keyword% or c.cne LIKE %:keyword%")
  Page<Client> searchClients(@Param("keyword") String keyword, Pageable pageable);

  @Query("SELECT COUNT(c) FROM Client c WHERE c.isDisabled = false")
  long countActiveClients();

  @Query("SELECT COUNT(c) FROM Client c WHERE c.isDisabled = true")
  long countInactiveClients();

  // Filter clients by category and is disabled
  @Query("SELECT c FROM Client c WHERE (:categoryId IS NULL OR c.category.id = :categoryId) AND (:isDisabled IS NULL OR c.isDisabled = :isDisabled)")
  Page<Client> filterClients(@Nullable Long categoryId, @Nullable Boolean isDisabled, Pageable pageable);

  // Search clients for dropdown
  @Query("SELECT c FROM Client c WHERE c.user.name LIKE %:keyword% OR c.user.email LIKE %:keyword% OR c.user.phone LIKE %:keyword% or c.user.username LIKE %:keyword% or c.company LIKE %:keyword% or c.category.name LIKE %:keyword% or c.cne LIKE %:keyword%")
  List<Client> searchClientsDropdown(@Param("keyword") String keyword);

}
