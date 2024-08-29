package com.idirtrack.backend.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.idirtrack.backend.basics.BasicException;
import com.idirtrack.backend.basics.BasicResponse;
import com.idirtrack.backend.basics.MessageType;
import com.idirtrack.backend.errors.AlreadyExistException;
import com.idirtrack.backend.errors.NotFoundException;
import com.idirtrack.backend.operator.Operator;
import com.idirtrack.backend.operator.OperatorRepository;
import com.idirtrack.backend.sim.https.SimRequest;
import com.idirtrack.backend.stock.Stock;
import com.idirtrack.backend.stock.StockRepository;
import com.idirtrack.backend.utils.ErrorResponse;
import com.idirtrack.backend.utils.FieldErrorDTO;
import com.idirtrack.backend.utils.MyResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SimService {

        private final SimRepository simRepository;
        private final SimStockRepository simStockRepository;
        private final StockRepository stockRepository;
        private final OperatorRepository operatorRepository;
        
        /**
         * Service: Get total SIMs for each status
         * 
         * @return MyResponse
         */
        public MyResponse getTotalSimsByStatus() {
                long totalNonInstalled = simRepository.countByStatus(SimStatus.NON_INSTALLED);
                long totalInstalled = simRepository.countByStatus(SimStatus.INSTALLED);
                long totalInPending = simRepository.countByStatus(SimStatus.PENDING);
                long totalLost = simRepository.countByStatus(SimStatus.LOST);

                Map<String, Long> data = new HashMap<>();
                data.put("nonInstalled", totalNonInstalled);
                data.put("installed", totalInstalled);
                data.put("lost", totalLost);
                data.put("pending", totalInPending);

                return MyResponse.builder()
                                .data(data)
                                .status(HttpStatus.OK)
                                .build();
        }

        /**
         * Service: Get Total SIMs
         * 
         * @return MyResponse
         */

        public MyResponse getTotalSims() {
                long totalSims = simRepository.count();
                return MyResponse.builder()
                                .data(totalSims)
                                .status(HttpStatus.OK)
                                .build();
        }

        /**
         * Service: Create a new sim
         * 
         * @param simRequest
         * @return
         * @throws BasicException
         * @throws AlreadyExistException
         */
        public MyResponse createSim(SimRequest simRequest) throws AlreadyExistException, NotFoundException {
                // Check if the phone number already exists
                this.ifPhoneNumberAlreadyExist(simRequest.getPhone());
                // Check if the CCID already exists
                this.ifCCIDAlreadyExist(simRequest.getCcid());
                // Check if the operator exists
                Operator operator = this.findOperatorById(simRequest.getOperatorId());
                // Build the Sim Entity
                Sim sim = this.tansformRequestToEntity(simRequest, operator);
                // Save the sim
                sim = simRepository.save(sim);
                // Update the stock
                this.updateSimStock(sim);
                // Return the response
                return MyResponse.builder()
                                .message("SIM created successfully")
                                .status(HttpStatus.CREATED)
                                .build();
        }

        // Update the stock
        private void updateSimStock(Sim sim) {
                // Convert LocalDateTime to Date
                java.sql.Date dateEntree = java.sql.Date.valueOf(sim.getCreatedAt().toLocalDate());
                List<Stock> stocks = stockRepository.findByDateEntree(dateEntree);
                Stock stock = null;
                SimStock simStock = null;

                for (Stock s : stocks) {
                        simStock = simStockRepository.findByStockAndOperator(s, sim.getOperator());
                        if (simStock != null) {
                                stock = s;
                                break;
                        }
                }

                if (stock == null) {
                        stock = Stock.builder()
                                        .dateEntree(dateEntree)
                                        .quantity(1)
                                        .build();
                        stock = stockRepository.save(stock);

                        simStock = SimStock.builder()
                                        .operator(sim.getOperator())
                                        .stock(stock)
                                        .build();
                        simStockRepository.save(simStock);
                } else {
                        stock.setQuantity(stock.getQuantity() + 1);
                        stockRepository.save(stock);
                }
        }

        // Update the stock on delete
        private void updateSimStockOnDelete(Sim sim) {
                java.sql.Date dateEntree = java.sql.Date.valueOf(sim.getCreatedAt().toLocalDate());

                List<Stock> stocks = stockRepository.findByDateEntree(dateEntree);
                Stock stock = null;
                SimStock simStock = null;

                for (Stock s : stocks) {
                        simStock = simStockRepository.findByStockAndOperator(s, sim.getOperator());
                        if (simStock != null) {
                                stock = s;
                                break;
                        }
                }

                if (stock != null) {
                        stock.setQuantity(stock.getQuantity() - 1);
                        stockRepository.save(stock);

                        if (stock.getQuantity() <= 0) {
                                simStockRepository.delete(simStock);
                                stockRepository.delete(stock);
                        }
                }
        }

        /**
         * GET SIM BY ID
         * 
         * @param id
         * @return
         * @throws NotFoundException
         */
        public MyResponse getSimById(Long id) throws NotFoundException {
                // Find the sim by id
                Sim sim = this.findSimById(id);

                // Build the entity to DTO
                SimDTO simDTO = this.transformEntityToDTO(sim);

                // Return the response
                return MyResponse.builder()
                                .data(simDTO)
                                .message("Sim retrieved successfully")
                                .status(HttpStatus.OK)
                                .build();
        }

        /**
         * Service : Get all SIMs with pagination
         * 
         * @param page
         * @param size
         * @return
         */
        public MyResponse getAllSimsWithPagination(int page, int size) {
                // Create a pageable object
                Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
                // Get all the sims with pagination
                Page<Sim> simPage = simRepository.findAll(pageable);
                // Check if the page is empty
                if (simPage.isEmpty()) {
                        return MyResponse.builder()
                                        .message("No SIMs found")
                                        .status(HttpStatus.NOT_FOUND)
                                        .build();
                }
                // Else, build the response
                else {
                        List<SimDTO> simDTOs = simPage.getContent().stream().map(sim -> this.transformEntityToDTO(sim))
                                        .collect(Collectors.toList());
                        // Build the Metadata
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("currentPage", simPage.getNumber() + 1);
                        metadata.put("totalPages", simPage.getTotalPages());
                        metadata.put("size", simPage.getSize());
                        // Return the response
                        return MyResponse.builder()
                                        .data(simDTOs)
                                        .metadata(metadata)
                                        .status(HttpStatus.OK)
                                        .build();
                }

        }

        /**
         * Service: Search SIMs
         * 
         * @param query - search query
         * @param page  - page number
         * @param size  - page size
         */
        public MyResponse searchSIMs(String query, int page, int size) {

                // Create a pageable object
                Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
                // Search by any field
                Page<Sim> simPage = simRepository.search(query, pageable);
                // Check if the page is empty
                if (simPage.isEmpty()) {
                        return MyResponse.builder()
                                        .message("No SIMs found")
                                        .status(HttpStatus.OK)
                                        .build();
                }
                // Else, build the response
                else {
                        List<SimDTO> simDTOs = simPage.getContent().stream().map(sim -> this.transformEntityToDTO(sim))
                                        .collect(Collectors.toList());
                        // Build the Metadata
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("currentPage", simPage.getNumber() + 1);
                        metadata.put("totalPages", simPage.getTotalPages());
                        metadata.put("size", simPage.getSize());
                        // Return the response
                        return MyResponse.builder()
                                        .data(simDTOs)
                                        .metadata(metadata)
                                        .status(HttpStatus.OK)
                                        .build();
                }

        }

        /**
         * Service: Update SIM
         * 
         */
        public MyResponse updateSim(Long id, SimRequest request) throws NotFoundException, AlreadyExistException {

                // Find the sim
                Sim sim = this.findSimById(id);
                // Check if the phone number already exists except the current sim
                this.ifPhoneNumberAlreadyExistExceptCurrentSim(request.getPhone(), id);
                // Check if the CCID already exists except the current sim
                this.ifCCIDAlreadyExistExceptCurrentSim(request.getCcid(), id);
                // Find the operator
                Operator operator = this.findOperatorById(request.getOperatorId());
                // Update the sim data
                sim.setPin(request.getPin());
                sim.setPuk(request.getPuk());
                sim.setCcid(request.getCcid());
                sim.setOperator(operator);
                sim.setPhone(request.getPhone());
                // Save sim
                sim = simRepository.save(sim);
                // Build SimDTO
                SimDTO simDTO = this.transformEntityToDTO(sim);
                // Return response
                return MyResponse.builder()
                                .data(simDTO)
                                .message("Sim update successfully")
                                .status(HttpStatus.OK)
                                .build();

        }

        /**
         * Service: Delete SIM
         * 
         * @param id
         * @return MyResponse
         * @throws NotFoundException
         */
        public MyResponse deleteSim(Long id) throws NotFoundException {
                // Find sim by id
                Sim sim = this.findSimById(id);
                // Update the stock on delete
                updateSimStockOnDelete(sim);
                // Delete the sim
                simRepository.delete(sim);
                // Return the response
                return MyResponse.builder()
                                .message("Sim deleted successfully")
                                .status(HttpStatus.OK)
                                .build();
        }

        public BasicResponse countNonInstalledSims() {
                long count = simRepository.countByStatus(SimStatus.NON_INSTALLED);
                return BasicResponse.builder()
                                .content(count)
                                .message("Non-installed SIMs count retrieved successfully")
                                .messageType(MessageType.SUCCESS)
                                .status(HttpStatus.OK)
                                .build();
        }

        /**
         * Service: Get all non-installed SIMs
         * @param page
         * @param size
         * @return MyResponse
         * @throws 
         */
        public MyResponse getAllNonInstalledSims(int page, int size){

                // Create a pageable object
                Pageable pageRequest = PageRequest.of(page - 1, size);
                // Get all non-installed sims
                Page<Sim> simPage = simRepository.findAllByStatus(SimStatus.NON_INSTALLED, pageRequest);

                // Check if the page is empty
                if (simPage.isEmpty()) {
                        return MyResponse.builder()
                                        .message("No non-installed SIMs found")
                                        .status(HttpStatus.NOT_FOUND)
                                        .build();
                }
                // Else retrun the response
                else {
                        List<SimDTO> simDTOs = simPage.getContent().stream().map(sim -> this.transformEntityToDTO(sim))
                                        .collect(Collectors.toList());
                        // Build the Metadata
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("currentPage", simPage.getNumber() + 1);
                        metadata.put("totalPages", simPage.getTotalPages());
                        metadata.put("size", simPage.getSize());
                        // Return the response
                        return MyResponse.builder()
                                        .data(simDTOs)
                                        .metadata(metadata)
                                        .status(HttpStatus.OK)
                                        .build();
                }

        }

        /**
         * Service: Search non-installed SIMs
         * @param query
         * @param page
         * @param size
         * @return MyResponse
         * @throws BasicException
         */
        public MyResponse searchNonInstalledSims(String query, int page, int size) {
                // Create a pageable object
                Pageable pageable = PageRequest.of(page - 1, size);
                // Search non-installed sims
                Page<Sim> simPage = simRepository.findAllByStatusAndPhoneContainingOrCcidContaining(
                                SimStatus.NON_INSTALLED, query, pageable);
                // Check if the page is empty
                if (simPage.isEmpty()) {
                        return MyResponse.builder()
                                        .message("No non-installed SIMs found")
                                        .status(HttpStatus.NOT_FOUND)
                                        .build();
                }
                // Else retrun the response
                else {
                        List<SimDTO> simDTOs = simPage.getContent().stream().map(sim -> this.transformEntityToDTO(sim))
                                        .collect(Collectors.toList());
                        // Build the Metadata
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("currentPage", simPage.getNumber() + 1);
                        metadata.put("totalPages", simPage.getTotalPages());
                        metadata.put("size", simPage.getSize());
                        // Return the response
                        return MyResponse.builder()
                                        .data(simDTOs)
                                        .metadata(metadata)
                                        .status(HttpStatus.OK)
                                        .build();
                }
        }



        public boolean changeSimStatus(Long id, String status) {
                try {
                        // Check if the status is valid
                        SimStatus simStatus = SimStatus.valueOf(status);
                        // Find the sim
                        Sim sim = simRepository.findById(id).orElse(null);
                        // If the sim does not exist, return false
                        if (sim == null) {
                                return false;
                        }
                        // Else, change the status of the sim
                        sim.setStatus(simStatus);
                        simRepository.save(sim);
                        // Return true
                        return true;
                } catch (IllegalArgumentException e) {
                        return false;
                }

        }

        /**
         * Service: Filter SIMs by status,operator
         * 
         * @param status
         * @param operatorId
         * @throws AlreadyExistException
         */

        public MyResponse filterSims(String status, Long operatorId, int page, int size) {
                // Find the operator
                Operator operator = operatorRepository.findById(operatorId).orElse(null);
                // Create a pageable object
                Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
                // Filter the sims
                Page<Sim> simPage = simRepository.filterSim(SimStatus.valueOf(status), operator, pageable);
                // Check if the page is empty
                if (simPage.isEmpty()) {
                        return MyResponse.builder()
                                        .message("No SIMs found")
                                        .status(HttpStatus.NOT_FOUND)
                                        .build();
                }
                // Else, build the response
                else {
                        List<SimDTO> simDTOs = simPage.getContent().stream().map(sim -> this.transformEntityToDTO(sim))
                                        .collect(Collectors.toList());
                        // Build the Metadata
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("currentPage", simPage.getNumber() + 1);
                        metadata.put("totalPages", simPage.getTotalPages());
                        metadata.put("size", simPage.getSize());
                        // Return the response
                        return MyResponse.builder()
                                        .data(simDTOs)
                                        .metadata(metadata)
                                        .status(HttpStatus.OK)
                                        .build();
                }
        }

        /**
         * Utils: Check if the phone number already exists except the current sim
         * 
         * @param phone
         * @param id
         * @throws AlreadyExistException
         * @return void
         */

        public void ifPhoneNumberAlreadyExistExceptCurrentSim(String phone, Long id) throws AlreadyExistException {
                List<FieldErrorDTO> fieldErrors = new ArrayList<>();

                if (simRepository.existsByPhoneAndIdNot(phone, id)) {
                        fieldErrors.add(FieldErrorDTO.builder()
                                        .field("phone")
                                        .message("Phone number already exists")
                                        .build());
                        throw new AlreadyExistException(ErrorResponse.builder()
                                        .fieldErrors(fieldErrors)
                                        .status(HttpStatus.CONFLICT)
                                        .build());
                }
        }

        /**
         * Utils: Check if the CCID already exists except the current sim
         * 
         * @param ccid
         * @param id
         * @throws AlreadyExistException
         * @return void
         */

        public void ifCCIDAlreadyExistExceptCurrentSim(String ccid, Long id) throws AlreadyExistException {
                List<FieldErrorDTO> fieldErrors = new ArrayList<>();
                if (simRepository.existsByCcidAndIdNot(ccid, id)) {
                        fieldErrors.add(FieldErrorDTO.builder()
                                        .field("ccid")
                                        .message("CCID already exists")
                                        .build());
                        throw new AlreadyExistException(ErrorResponse.builder()
                                        .fieldErrors(fieldErrors)
                                        .status(HttpStatus.CONFLICT)
                                        .build());
                }
        }

        /**
         * Utils: Check if the phone number already exists
         * 
         * @param phone
         * @throws AlreadyExistException
         */
        public void ifPhoneNumberAlreadyExist(String phone) throws AlreadyExistException {
                List<FieldErrorDTO> fieldErrors = new ArrayList<>();
                if (simRepository.existsByPhone(phone)) {
                        fieldErrors.add(FieldErrorDTO.builder()
                                        .field("phone")
                                        .message("Phone number already exists")
                                        .build());
                        throw new AlreadyExistException(ErrorResponse.builder()
                                        .fieldErrors(fieldErrors)
                                        .status(HttpStatus.CONFLICT)
                                        .build());
                }

        }

        /**
         * Utils: Check if the CCID already exists
         * 
         * @param ccid
         * @throws AlreadyExistException
         */
        public void ifCCIDAlreadyExist(String ccid) throws AlreadyExistException {
                List<FieldErrorDTO> fieldErrors = new ArrayList<>();
                if (simRepository.existsByCcid(ccid)) {
                        fieldErrors.add(FieldErrorDTO.builder()
                                        .field("ccid")
                                        .message("CCID already exists")
                                        .build());
                        throw new AlreadyExistException(ErrorResponse.builder()
                                        .fieldErrors(fieldErrors)
                                        .status(HttpStatus.CONFLICT)
                                        .build());
                }
        }

        /**
         * Utils: Find an operator by id
         * 
         * @param id
         * @return Operator
         * @throws NotFoundException
         */
        public Operator findOperatorById(Long id) throws NotFoundException {
                List<FieldErrorDTO> fieldErrors = new ArrayList<>();
                return operatorRepository.findById(id).orElseThrow(
                                () -> {
                                        fieldErrors.add(FieldErrorDTO.builder()
                                                        .field("operatorId")
                                                        .message("Operator not found with id: " + id)
                                                        .build());
                                        return new NotFoundException(ErrorResponse.builder()
                                                        .fieldErrors(fieldErrors)
                                                        .status(HttpStatus.NOT_FOUND)
                                                        .build());
                                });
        }

        /**
         * Utils: Transform a SimRequest to a Sim entity
         * 
         * @param request
         * @param operator
         * @return Sim
         */
        public Sim tansformRequestToEntity(SimRequest request, Operator operator) {
                return Sim.builder()
                                .pin(request.getPin())
                                .puk(request.getPuk())
                                .ccid(request.getCcid())
                                .operator(operator)
                                .phone(request.getPhone())
                                .status(SimStatus.NON_INSTALLED)
                                .build();
        }

        /**
         * Utils : Find Sim by id
         */

        public Sim findSimById(Long id) throws NotFoundException {
                return simRepository.findById(id).orElseThrow(
                                () -> new NotFoundException(ErrorResponse.builder()
                                                .message("Sim not found with id: " + id)
                                                .status(HttpStatus.NOT_FOUND)
                                                .build()));
        }

        /**
         * Utils: Transform a Sim entity to a SimDTO
         */

        public SimDTO transformEntityToDTO(Sim sim) {
                return SimDTO.builder()
                                .id(sim.getId())
                                .pin(sim.getPin())
                                .puk(sim.getPuk())
                                .ccid(sim.getCcid())
                                .phone(sim.getPhone())
                                .status(sim.getStatus())
                                .createdAt(sim.getCreatedAt())
                                .operatorId(sim.getOperator().getId())
                                .operatorName(sim.getOperator().getName())
                                .build();

        }
}