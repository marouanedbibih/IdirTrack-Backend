package com.idirtrack.backend.staff;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;



import jakarta.validation.Valid;

import com.idirtrack.backend.utils.MyResponse;

@RestController
public class StaffController {

    @Autowired
    private StaffService staffService;

    // Endpoint to create a new staff member
    @PostMapping("/api/v1/staff")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<MyResponse> createStaff(@Valid @RequestBody StaffRequest request) {

        MyResponse response = staffService.createStaff(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Endpoint to update an existing staff member
    @PutMapping("/api/v1/staff/{staffId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<MyResponse> updateStaff(@PathVariable Long staffId, @RequestBody StaffRequest request) {

        MyResponse response = staffService.updateStaff(staffId, request);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

    // Endpoint to delete a staff member
    @DeleteMapping("/api/v1/staff/{staffId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<MyResponse> deleteStaff(@PathVariable Long staffId) {
        MyResponse response = staffService.deleteStaff(staffId);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

    // Endpoint to get list of all staff members
    @GetMapping("api/v1/staffs")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<MyResponse> getAllStaff(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer size) {
        MyResponse response = staffService.getAllStaff(page, size);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

    // Endpoint to get a staff member by ID
    @GetMapping("/api/v1/staff/{staffId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<MyResponse> getStaffById(@PathVariable Long staffId) {
        MyResponse response = staffService.getStaffById(staffId);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

    // Endpoint to search for staff members
    @GetMapping("/api/v1/staffs/search")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<MyResponse> searchStaff(
            @RequestParam String search,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer size) {
        MyResponse response = staffService.searchStaff(search, page, size);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

}
