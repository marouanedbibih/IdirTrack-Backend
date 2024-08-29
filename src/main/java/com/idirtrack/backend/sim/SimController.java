package com.idirtrack.backend.sim;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.idirtrack.backend.basics.BasicException;

import com.idirtrack.backend.errors.AlreadyExistException;
import com.idirtrack.backend.errors.NotFoundException;
import com.idirtrack.backend.sim.https.SimRequest;
import com.idirtrack.backend.utils.MyResponse;
import com.idirtrack.backend.utils.RequestValidation;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sim")
public class SimController {

    @Autowired
    private SimService simService;


    /**
     * Endpoint API to get total number of SIMs by status
     * 
     * @return
     */

    @GetMapping("/statistiques/total-sims-by-status/")
    public ResponseEntity<MyResponse> countSimsByStatus() {
        MyResponse response = simService.getTotalSimsByStatus();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * Endpoint API to get total number of SIMs
     * 
     * @return ResponseEntity<MyResponse>
     */
    @GetMapping("/statistiques/total-sims/")
    public ResponseEntity<MyResponse> countSims() {
        MyResponse response = simService.getTotalSims();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * Endpoint API to filter SIMs by status and operator
     * 
     * @param page
     * @param size
     * @param status
     * @param operatorId
     * @return ResponseEntity<?>
     */
    @GetMapping("/filter/")
    public ResponseEntity<?> filterSim(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long operatorId) {
        MyResponse response = simService.filterSims(status, operatorId, page, size);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * Endpoint API to get List of SIMs with pagination
     * 
     * @param page
     * @param size
     * @return ResponseEntity<MyResponse>
     */
    @GetMapping("/")
    public ResponseEntity<MyResponse> getAllSims(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        MyResponse response = simService.getAllSimsWithPagination(page, size);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * Endpoint API to get SIM by ID
     * 
     * @param id
     * @return ResponseEntity<MyResponse>
     * @throws NotFoundException
     */
    @GetMapping("/{id}/")
    public ResponseEntity<MyResponse> getSimById(@PathVariable Long id) throws NotFoundException {
        MyResponse response = simService.getSimById(id);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

    /**
     * Endpoint API to create SIM
     * 
     * @param simRequest
     * @param bindingResult
     * @return
     * @throws BasicException
     */
    @PostMapping("/")
    public ResponseEntity<?> createSim(
            @Valid @RequestBody SimRequest request,
            BindingResult bindingResult)
            throws AlreadyExistException, NotFoundException {

        if (bindingResult.hasErrors()) {
            return RequestValidation.handleValidationErrors(bindingResult);
        } else {
            try {
                MyResponse response = simService.createSim(request);
                return ResponseEntity.status(response.getStatus()).body(response);
            } catch (AlreadyExistException ex) {
                return ResponseEntity.status(ex.getResponse().getStatus()).body(ex.getResponse());
            } catch (NotFoundException ex) {
                return ResponseEntity.status(ex.getResponse().getStatus()).body(ex.getResponse());
            }
        }

    }

    /**
     * Endpoint API to update SIM
     * 
     * @param id
     * @param request
     * @throws NotFoundException
     */
    @PutMapping("/{id}/")
    public ResponseEntity<?> updateSim(
            @PathVariable Long id,
            @Valid @RequestBody SimRequest request,
            BindingResult bindingResult)
            throws NotFoundException, AlreadyExistException {
        if (bindingResult.hasErrors()) {
            return RequestValidation.handleValidationErrors(bindingResult);
        } else {
            try {
                MyResponse response = simService.updateSim(id, request);
                return ResponseEntity.status(response.getStatus()).body(response);
            } catch (AlreadyExistException ex) {
                return ResponseEntity.status(ex.getResponse().getStatus()).body(ex.getResponse());
            } catch (NotFoundException ex) {
                return ResponseEntity.status(ex.getResponse().getStatus()).body(ex.getResponse());
            }
        }

    }

    /**
     * Endpoint API to delete SIM
     * 
     * @param id
     * @return ResponseEntity<MyResponse>
     * @throws NotFoundException
     */
    @DeleteMapping("/{id}/")
    public ResponseEntity<?> deleteSim(@PathVariable Long id) throws NotFoundException {
        try {
            MyResponse response = simService.deleteSim(id);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (NotFoundException e) {
            return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
        }
    }

    /**
     * GET PENDING SIMS
     * 
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/non-installed/")
    public ResponseEntity<?> getNonInstalledSimsApi(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        MyResponse response = simService.getAllNonInstalledSims(page, size);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

    @GetMapping("/non-installed/search/")
    public ResponseEntity<?> searchNonInstalledSimsApi(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        MyResponse response = simService.searchNonInstalledSims(query, page, size);

        return ResponseEntity.status(response.getStatus()).body(response);

    }

    @GetMapping("/search/")
    public ResponseEntity<MyResponse> searchSIMs(
            @RequestParam(value = "term", required = true) String term,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {

        MyResponse response = simService.searchSIMs(term, page, size);
        return ResponseEntity.status(response.getStatus()).body(response);

    }


}
