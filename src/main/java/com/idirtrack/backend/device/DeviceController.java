package com.idirtrack.backend.device;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import com.idirtrack.backend.basics.BasicException;
import com.idirtrack.backend.basics.BasicResponse;
import com.idirtrack.backend.basics.MessageType;
import com.idirtrack.backend.device.https.DeviceRequest;
import com.idirtrack.backend.device.https.DeviceUpdateRequest;
import com.idirtrack.backend.utils.MyResponse;
import com.idirtrack.backend.utils.ValidationUtils;

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
@RequestMapping("/api/device")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    // Filter Devices API
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/filter/")
    public ResponseEntity<BasicResponse> filterDevices(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "type", required = false) Long deviceTypeId,
            @RequestParam(value = "createdFrom", required = false) String createdFrom,
            @RequestParam(value = "createdTo", required = false) String createdTo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) throws BasicException {

        // Try to filter the devices
        try {
            BasicResponse response = deviceService.filterDevices(status, deviceTypeId, createdFrom, createdTo, page,
                    size);
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        // Catch any BasicException and return the response
        catch (BasicException e) {
            return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
        }
        // Catch any exception and return a 500 error
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.builder()
                    .content(null)
                    .message(e.getMessage())
                    .messageType(MessageType.ERROR)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .metadata(null)
                    .build());
        }
    }

    // Count Devices for each status API
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/quantity-of-status/")
    public ResponseEntity<BasicResponse> getQuantityOfStatus() {
        BasicResponse response = deviceService.countDevicesGroupByStatus();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Count total of devices API
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/total/")
    public ResponseEntity<BasicResponse> getTotalDevices() {
        BasicResponse response = deviceService.countDevices();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Create Device API
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @PostMapping("/")
    public ResponseEntity<BasicResponse> createDeviceApi(
            @RequestBody @Valid DeviceRequest deviceRequest,
            BindingResult bindingResult) throws BasicException {

        // Handle validation errors
        ResponseEntity<BasicResponse> errorResponse = ValidationUtils.handleValidationErrors(bindingResult);
        if (errorResponse != null) {
            return errorResponse;
        }
        // If there are no validation errors, call the service to create the device
        else {
            // Try to create the device
            try {
                BasicResponse response = deviceService.createDevice(deviceRequest);
                return ResponseEntity.status(response.getStatus()).body(response);
            }
            // Catch any BasicException and return the response
            catch (BasicException e) {
                return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
            }
            // Catch any exception and return a 500 error
            catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.builder()
                        .content(null)
                        .message(e.getMessage())
                        .messageType(MessageType.ERROR)
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .metadata(null)
                        .build());
            }
        }

    }

    // Update Device API
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @PutMapping("/{id}/")
    public ResponseEntity<BasicResponse> updateDeviceApi(
            @PathVariable Long id,
            @RequestBody @Valid DeviceUpdateRequest deviceUpdateRequest,
            BindingResult bindingResult) throws BasicException {

        // Handle validation errors
        ResponseEntity<BasicResponse> errorResponse = ValidationUtils.handleValidationErrors(bindingResult);
        if (errorResponse != null) {
            return errorResponse;
        }
        // If there are no validation errors, call the service to update the device
        else {
            // Try to update the device
            try {
                BasicResponse response = deviceService.updateDevice(id, deviceUpdateRequest);
                return ResponseEntity.status(response.getStatus()).body(response);
            }
            // Catch any BasicException and return the response
            catch (BasicException e) {
                return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
            }
            // Catch any exception and return a 500 error
            catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.builder()
                        .content(null)
                        .message(e.getMessage())
                        .messageType(MessageType.ERROR)
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .metadata(null)
                        .build());
            }
        }

    }

    // Delete Device API
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @DeleteMapping("/{id}/")
    public ResponseEntity<BasicResponse> deleteDeviceApi(@PathVariable Long id) throws BasicException {

        // Call the service to delete the device, and return the response
        BasicResponse response = deviceService.deleteDevice(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Get Device by ID API
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/{id}/")
    public ResponseEntity<BasicResponse> getDeviceApi(@PathVariable Long id) throws BasicException {

        // Call the service to get the device by ID, and return the response
        BasicResponse response = deviceService.getDeviceById(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // API to get list of all devices with pagination
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/")
    public ResponseEntity<BasicResponse> getAllBoitiers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) throws BasicException {

        // Call the service to get all devices, and return the response
        BasicResponse response = deviceService.getAllDevices(page, size);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

    // API to search devices by any field
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
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
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<BasicResponse> countNonInstallDevicesApi() {
        BasicResponse response = deviceService.countDevicesNonInstalled();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Get list of devices not installed API
    @GetMapping("/non-installed/")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<?> getNonInstalledDevicesApi(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        MyResponse response = deviceService.getAllDevicesNonInstalled(page, size);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // API to search non-installed devices
    @GetMapping("/non-installed/search/")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<?> searchNonInstalledDevicesApi(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {
        MyResponse response = deviceService.searchNonInstalledDevices(query, page, size);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Search Device by IMEI API
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    @GetMapping("/search/")
    public ResponseEntity<BasicResponse> searchDeviceByImeiApi(
            @RequestParam(value = "search") String search,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {
        // Try to search the device by IMEI
        try {
            BasicResponse response = deviceService.searchDevices(search, page, size);
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        // Catch any BasicException and return the response
        catch (BasicException e) {
            return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
        }
        // Catch any exception and return a 500 error
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.builder()
                    .content(null)
                    .message(e.getMessage())
                    .messageType(MessageType.ERROR)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }
    }
}
