package com.idirtrack.stock_service.sim;
import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, Long> {
    boolean existsByName(String name);
    Optional<Operator> findByName(String name);
    Optional<Operator> findByNameAndCreatedAt(String name, Date createdAt);

}
