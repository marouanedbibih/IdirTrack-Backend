package com.idirtrack.stock_service.device;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Date;

import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import com.idirtrack.stock_service.basics.BasicException;
import com.idirtrack.stock_service.basics.BasicResponse;

import com.idirtrack.stock_service.basics.BasicValidation;
import com.idirtrack.stock_service.basics.MessageType;
import com.idirtrack.stock_service.device.https.DeviceRequest;
import com.idirtrack.stock_service.device.https.DeviceUpdateRequest;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/stock-api/devices")



public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    // Save Device API
    @PostMapping("/")
    public ResponseEntity<BasicResponse> createDeviceApi(@RequestBody @Valid DeviceRequest deviceRequest, BindingResult bindingResult) {
        try {
            BasicResponse response = deviceService.createDevice(deviceRequest, bindingResult);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (BasicException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getResponse());
        }
    }

    // Update Device API
    @PutMapping("/{id}/")
    public ResponseEntity<BasicResponse> updateDeviceApi(@PathVariable Long id, @RequestBody @Valid DeviceUpdateRequest deviceUpdateRequest, BindingResult bindingResult) {
        try {
            BasicResponse response = deviceService.updateDevice(id, deviceUpdateRequest, bindingResult);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (BasicException e) {
            return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.builder()
                .content(null)
                .message(e.getMessage())
                .messageType(MessageType.ERROR)
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .metadata(null)
                .build());
        }
    }

    // Delete Device API
    @DeleteMapping("/{id}/")
    public ResponseEntity<BasicResponse> deleteDeviceApi(@PathVariable Long id) {
        try {
            BasicResponse response = deviceService.deleteDevice(id);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.builder()
                .content(null)
                .message(e.getMessage())
                .messageType(MessageType.ERROR)
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .metadata(null)
                .build());
        }
    }

    // Get Device by ID API
    @GetMapping("/{id}/")
    public ResponseEntity<BasicResponse> getDeviceApi(@PathVariable Long id) {
        try {
            BasicResponse response = deviceService.getDeviceById(id);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.builder()
                .content(null)
                .message(e.getMessage())
                .messageType(MessageType.ERROR)
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .metadata(null)
                .build());
        }
    }

    // Get All Devices API

    
    @GetMapping("/")

    public ResponseEntity<BasicResponse> getAllBoitiers(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        BasicResponse response = deviceService.getAllDevices(page, size);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Search Devices API
    @GetMapping("/filter")
    public ResponseEntity<BasicResponse> filterDevicesApi(@RequestParam(value = "imei", required = false) String imei,
                                                          @RequestParam(value = "type", required = false) String deviceType,
                                                          @RequestParam(value = "status", required = false) String status,
                                                          @RequestParam(value = "createdTo", required = false) String createdTo,
                                                          @RequestParam(value = "createdFrom", required = false) String createdFrom,
                                                          @RequestParam(value = "page", defaultValue = "1") int page,
                                                          @RequestParam(value = "size", defaultValue = "5") int size) {
        Date dateAt = null;
        Date dateFrom = null;
     
        if (createdTo != null && !createdTo.isEmpty() && createdFrom != null && !createdFrom.isEmpty()) {
            try {
                dateAt = new Date(new SimpleDateFormat("yyyy-MM-dd").parse(createdTo).getTime());
                dateFrom = new Date(new SimpleDateFormat("yyyy-MM-dd").parse(createdFrom).getTime());
            } catch (ParseException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BasicResponse.builder()
                    .content(null)
                    .message("Invalid date format")
                    .messageType(MessageType.ERROR)
                    .status(HttpStatus.BAD_REQUEST)
                    .metadata(null)
                    .build());
            }
        }
        

        BasicResponse response = deviceService.filterDevices(imei, deviceType, status, dateAt, dateFrom, page, size);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Count Devices by Status API
    @GetMapping("/count-non-install/")
    public ResponseEntity<BasicResponse> countNonInstallDevicesApi() {
        BasicResponse response = deviceService.countDevicesNonInstalled();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    //get list of devices by status non-installed
    @GetMapping("/not-installed/")

    public ResponseEntity<BasicResponse> getNonInstalledDevicesApi(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        BasicResponse response = deviceService.getAllDevicesNonInstalled(page, size);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    //search device non installed by imei
    @GetMapping("/not-installed/search/")

    public ResponseEntity<BasicResponse> searchNonInstalledDevicesApi(@RequestParam(value = "imei", required = false) String imei,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {
        BasicResponse response = deviceService.searchNonInstalledDevices(imei, page, size);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Change Device Status to Installed API
    @PutMapping("/status/installed/{id}/")
    public ResponseEntity<BasicResponse> changeDeviceStatusToInstalledApi(@PathVariable Long id) {
        try {
            BasicResponse response = deviceService.changeDeviceStatusInstalled(id);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (BasicException e) {
            return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.builder()
                .content(null)
                .message(e.getMessage())
                .messageType(MessageType.ERROR)
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build());
        }
    }

    /**
     * Endpoint to chnage the status of device
     * @param id
     * @param status
     * @return ResponseEntity<BasicResponse>
     */

    @PutMapping("/status/")
    public ResponseEntity<BasicResponse> changeDeviceStatusApi(@RequestParam Long id, @RequestParam String status) {
        try {
            BasicResponse response = deviceService.changeDeviceStatus(id, status);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (BasicException e) {
            return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.builder()
                .content(null)
                .message(e.getMessage())
                .messageType(MessageType.ERROR)
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build());
        }
    }
     //Search Device by IMEI API
     @GetMapping("/search")
     public ResponseEntity<BasicResponse> searchDeviceByImeiApi(@RequestParam(value = "imei") String imei,
                                                                @RequestParam(value = "page", defaultValue = "1") int page,
                                                                @RequestParam(value = "size", defaultValue = "5") int size) {
         BasicResponse response = deviceService.searchDevices(imei, page, size);
         return ResponseEntity.status(response.getStatus()).body(response);
     }
 }
 

