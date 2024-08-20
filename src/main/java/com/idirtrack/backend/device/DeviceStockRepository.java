package com.idirtrack.backend.device;

import java.sql.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.idirtrack.backend.deviceType.DeviceType;
import com.idirtrack.backend.stock.Stock;


@Repository
public interface DeviceStockRepository  extends JpaRepository<DeviceStock, Long>{

  DeviceStock findByStockAndDeviceType(Stock stock, DeviceType deviceType);



}
