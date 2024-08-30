package com.idirtrack.backend.client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientCategoryRepository extends JpaRepository<ClientCategory, Long> {
    ClientCategory findByName(String name);
  
    boolean existsByName(String name);

}
