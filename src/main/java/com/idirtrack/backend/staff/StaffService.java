package com.idirtrack.backend.staff;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idirtrack.backend.client.Client;
import com.idirtrack.backend.client.ClientRepository;
import com.idirtrack.backend.errors.AlreadyExistException;
import com.idirtrack.backend.errors.NotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import com.idirtrack.backend.utils.ErrorResponse;
import com.idirtrack.backend.utils.FieldErrorDTO;
import com.idirtrack.backend.utils.MyResponse;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class StaffService {

        private final StaffRepository staffRepository;
        private final ClientRepository clientRepository;

        // Create a new staff member
        @Transactional
        public MyResponse createStaff(StaffRequest request) throws AlreadyExistException {

                // Check if the staff already exists
                boolean staffExists = staffRepository.existsByName(request.getName());
                if (staffExists) {
                        throw new AlreadyExistException(ErrorResponse.builder()
                                        .fieldErrors(List.of(FieldErrorDTO.builder()
                                                        .field("name")
                                                        .message("Staff already exists").build()))
                                        .status(HttpStatus.CONFLICT)
                                        .build());
                }

                // Check if the client exists
                Client client = clientRepository.findById(request.getClientId()).orElseThrow(
                                () -> {
                                        return new AlreadyExistException(ErrorResponse.builder()
                                                        .fieldErrors(List.of(FieldErrorDTO.builder()
                                                                        .field("clientId")
                                                                        .message("Client not found").build()))
                                                        .status(HttpStatus.NOT_FOUND)
                                                        .build());
                                });

                // Save the staff into database
                Staff staff = Staff.builder()
                                .name(request.getName())
                                .phone(request.getPhone())
                                .position(request.getPosition())
                                .client(client)
                                .build();
                staff = staffRepository.save(staff);

                // Retrun response
                return MyResponse.builder()
                                .message("Staff created successfully")
                                .status(HttpStatus.CREATED)
                                .build();

        }

        // Update an existing staff member
        @Transactional
        public MyResponse updateStaff(Long staffId, StaffRequest request)
                        throws NotFoundException {
                // Find the staff member by ID
                Staff staff = this.utilToFindStaffById(staffId);
                // Find the client by ID
                Client client = clientRepository.findById(request.getClientId()).orElseThrow(
                                () -> new NotFoundException(ErrorResponse.builder()
                                                .message("Client not found with id: " + request.getClientId())
                                                .status(HttpStatus.NOT_FOUND)
                                                .build()));
                // Save the updated staff member
                staff.setName(request.getName());
                staff.setPhone(request.getPhone());
                staff.setPosition(request.getPosition());
                staff.setClient(client);
                staff = staffRepository.save(staff);
                // Return a Response object
                return MyResponse.builder()
                                .message("Staff updated successfully")
                                .status(HttpStatus.OK)
                                .build();
        }

        // Delete a staff member
        @Transactional
        public MyResponse deleteStaff(Long staffId) throws NotFoundException {

                // Check if the staff member exists
                boolean staffExists = staffRepository.existsById(staffId);
                if (!staffExists) {
                        throw new NotFoundException(ErrorResponse.builder()
                                        .message("Staff not found with id: " + staffId)
                                        .status(HttpStatus.NOT_FOUND)
                                        .build());
                }
                // Delete the staff member
                staffRepository.deleteById(staffId);
                // Return a Response object
                return MyResponse.builder()
                                .message("Staff deleted successfully")
                                .status(HttpStatus.OK)
                                .build();

        }

        // Get a staff member by ID
        public MyResponse getStaffById(Long staffId) throws NotFoundException {
                // Find the staff member by ID
                Staff staff = this.utilToFindStaffById(staffId);
                // Build a StaffDTO object
                StaffDTO staffDTO = this.buildDTO(staff);
                // Return a Response object
                return MyResponse.builder()
                                .data(staffDTO)
                                .status(HttpStatus.OK)
                                .build();
        }

        // Search a staff member by name, phone, position, client name, or client
        // company
        public MyResponse searchStaff(String search, Integer page, Integer size) {
                // Get page of staff members that match the search criteria
                Pageable pageable = PageRequest.of(page - 1, size,Sort.by("id").descending());
                Page<Staff> staffPage = staffRepository.searchStaff(search, pageable);

                // If the page is empty, return a Response object with a message
                if (staffPage.getContent().isEmpty()) {
                        return MyResponse.builder()
                                        .message("No staff members found with this search keyword" + search)
                                        .status(HttpStatus.OK)
                                        .build();
                } else{
                        // Build a list of StaffDTO objects from the page of Staff objects
                        List<StaffDTO> staffDTOs = staffPage.getContent().stream()
                                        .map(this::buildDTO)
                                        .collect(Collectors.toList());
                        // Build a MetaData object
                        Map<String, Object> metaData = new HashMap<>();
                        metaData.put("currentPage", staffPage.getNumber() + 1);
                        metaData.put("totalPages", staffPage.getTotalPages());
                        metaData.put("size", staffPage.getSize());
                        metaData.put("totalElements", (int) staffPage.getTotalElements());
                        // Return a Response object
                        return MyResponse.builder()
                                        .data(staffDTOs)
                                        .metadata(metaData)
                                        .status(HttpStatus.OK)
                                        .build();
                }

        }

        // Get list of all staff members
        public MyResponse getAllStaff(Integer page, Integer size) {
                // Get page of staff members
                Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
                Page<Staff> staffPage = staffRepository.findAll(pageable);

                // If the page is empty, return an empty BasicResponse object
                if (staffPage.getContent().isEmpty()) {
                        return MyResponse.builder()
                                        .message("No staff members found")
                                        .status(HttpStatus.OK)
                                        .build();
                } else {
                        // Build a list of StaffDTO objects from the page of Staff objects
                        List<StaffDTO> staffDTOs = staffPage.getContent().stream()
                                        .map(this::buildDTO)
                                        .collect(Collectors.toList());
                        // Build a MetaData object
                        Map<String, Object> metaData = new HashMap<>();
                        metaData.put("currentPage", staffPage.getNumber() + 1);
                        metaData.put("totalPages", staffPage.getTotalPages());
                        metaData.put("size", staffPage.getSize());
                        metaData.put("totalElements", (int) staffPage.getTotalElements());
                        // Return a Response object
                        return MyResponse.builder()
                                        .data(staffDTOs)
                                        .metadata(metaData)
                                        .status(HttpStatus.OK)
                                        .build();
                }
        }

        // Build a StaffDTO object from a Staff object
        private StaffDTO buildDTO(Staff staff) {
                return StaffDTO.builder()
                                .id(staff.getId())
                                .name(staff.getName())
                                .phone(staff.getPhone())
                                .position(staff.getPosition())
                                .clientId(staff.getClient().getId())
                                .clientName(staff.getClient().getUser().getName())
                                .clientCompany(staff.getClient().getCompany())
                                .build();
        }

        // Utils to find staff by ID
        private Staff utilToFindStaffById(Long staffId) throws NotFoundException {
                return staffRepository.findById(staffId).orElseThrow(
                                () -> new NotFoundException(ErrorResponse.builder()
                                                .message("Staff not found with id: " + staffId)
                                                .status(HttpStatus.NOT_FOUND)
                                                .build()));
        }
}
