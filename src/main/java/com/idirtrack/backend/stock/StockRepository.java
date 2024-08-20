package com.idirtrack.backend.stock;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByDateEntree(Date dateEntree);
}
