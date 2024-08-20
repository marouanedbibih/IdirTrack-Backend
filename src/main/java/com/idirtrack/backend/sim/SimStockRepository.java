package com.idirtrack.backend.sim;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.idirtrack.backend.operator.Operator;
import com.idirtrack.backend.stock.Stock;

@Repository
public interface SimStockRepository extends JpaRepository<SimStock, Long> {

    SimStock findByStockAndOperator(Stock stock, Operator operator);

}
