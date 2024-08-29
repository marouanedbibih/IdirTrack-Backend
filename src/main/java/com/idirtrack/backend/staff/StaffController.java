package com.idirtrack.backend.staff;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.idirtrack.backend.basics.BasicException;
import com.idirtrack.backend.basics.BasicResponse;
import com.idirtrack.backend.utils.ValidUtils;

import jakarta.validation.Valid;

import com.idirtrack.backend.utils.Error;

@RestController
public class StaffController {

    @Autowired
    private StaffService staffService;

    // Endpoint to create a new staff member
    @PostMapping("/api/v1/staff")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<BasicResponse> createStaff(@Valid @RequestBody StaffCreateRequest request,
            BindingResult bindingResult) {
        // check if there are errors in the request
        if (bindingResult.hasErrors()) {
            // map this errors and set list of my Error class by key filed and message
            List<Error> errors = ValidUtils.extractErrorsFromBindingResult(bindingResult);
            // return a response with the errors
            return ResponseEntity.badRequest().body(BasicResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .errorsList(errors)
                    .build());
        } 
        // if there are no errors
        else {
            // try to create a new staff member
            try {
                // call the service to create a new staff member
                BasicResponse response = staffService.createStaff(request);
                // return the response
                return ResponseEntity.status(response.getStatus()).body(response);
            }
            // catch BasicException and return the response
            catch (BasicException e) {
                return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
            }
            // catch any exception and return an internal server error
            catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.builder()
                        .message(e.getMessage())
                        .content(null)
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
            }
        }

    }

    // Endpoint to update an existing staff member
    @PutMapping("/api/v1/staff/{staffId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<BasicResponse> updateStaff(@PathVariable Long staffId, @RequestBody StaffDTO staffDTO,
            BindingResult bindingResult) {
        // check if there are errors in the request
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(BasicResponse.builder()
                    .message("Validation failed")
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        }
        // if there are no errors
        else {
            // try to update the staff member
            try {
                BasicResponse response = staffService.updateStaff(staffId, staffDTO, bindingResult);
                return ResponseEntity.status(response.getStatus()).body(response);
            }
            // catch BasicException and return the response
            catch (BasicException e) {
                return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
            }
            // catch any exception and return an internal server error
            catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.builder()
                        .message(e.getMessage())
                        .content(null)
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
            }
        }

    }

    // Endpoint to delete a staff member
    @DeleteMapping("/api/v1/staff/{staffId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<BasicResponse> deleteStaff(@PathVariable Long staffId) {
        try {
            BasicResponse response = staffService.deleteStaff(staffId);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (BasicException e) {
            return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.builder()
                    .message(e.getMessage())
                    .content(null)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }
    }

    // Endpoint to get all staff members
    @GetMapping("api/v1/staffs")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<BasicResponse> getAllStaff(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            BasicResponse response = staffService.getAllStaff(page, size);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (BasicException e) {
            return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.builder()
                    .message(e.getMessage())
                    .content(null)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }
    }

    // Endpoint to get a staff member by ID
    @GetMapping("/api/v1/staff/{staffId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<BasicResponse> getStaffById(@PathVariable Long staffId) {
        try {
            BasicResponse response = staffService.getStaffById(staffId);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.builder()
                    .message(e.getMessage())
                    .content(null)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }
    }

    // Endpoint to search for staff members
    @GetMapping("/api/v1/staffs/search")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<BasicResponse> searchStaff(
            @RequestParam String search,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            BasicResponse response = staffService.searchStaff(search, page, size);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (BasicException e) {
            return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.builder()
                    .message(e.getMessage())
                    .content(null)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }
    }

}
