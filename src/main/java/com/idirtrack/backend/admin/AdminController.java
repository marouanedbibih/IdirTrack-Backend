package com.idirtrack.backend.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.idirtrack.backend.basics.BasicException;
import com.idirtrack.backend.basics.BasicResponse;
import com.idirtrack.backend.user.UserRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user-api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * Simple endpoint to test the admin controller with hello world
     * @return
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello Admin");
    }

    /**
     * Endpoint API to retrieve all admins with pagination
     * 
     * @param page
     * @param size
     * @return
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<BasicResponse> getAdmins(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        // Try to get the admins
        try {
            BasicResponse response = adminService.getAdmins(page, size);
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        // Catch BasicException and return the response
        catch (BasicException e) {
            return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
        }
        // Catch any exception and return an internal server error
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.builder()
                    .message(e.getMessage())
                    .content(null)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }
    }

    /**
     * Endpoint API to create an admin
     * 
     * @param request UserRequest
     * @return ResponseEntity<BasicResponse>
     */
    // @PostMapping("/")
    // public ResponseEntity<BasicResponse> createAdmin(@RequestBody UserRequest request) {
    //     // Try to create the admin
    //     try {
    //         BasicResponse response = adminService.createAdmin(request);
    //         return ResponseEntity.status(response.getStatus()).body(response);
    //     }
    //     // Catch BasicException and return the response
    //     catch (BasicException e) {
    //         return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
    //     }
    //     // Catch any exception and return an internal server error
    //     catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.builder()
    //                 .message(e.getMessage())
    //                 .content(null)
    //                 .status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .build());
    //     }
    // }
}
