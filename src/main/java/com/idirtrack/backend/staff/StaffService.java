package com.idirtrack.backend.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.idirtrack.backend.basics.BasicException;
import com.idirtrack.backend.basics.BasicResponse;
import com.idirtrack.backend.basics.MessageType;
import com.idirtrack.backend.client.Client;
import com.idirtrack.backend.client.ClientRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.idirtrack.backend.utils.BasicValidation;
import com.idirtrack.backend.utils.Error;

import com.idirtrack.backend.basics.MetaData;

@Service
public class StaffService {

        @Autowired
        private StaffRepository staffRepository;

        @Autowired
        private ClientRepository clientRepository;

        // Create a new staff member
        @Transactional
        public BasicResponse createStaff(StaffCreateRequest request) throws BasicException {

                // Check if the staff already exists
                boolean staffExists = staffRepository.existsByName(request.getName());
                if (staffExists) {
                        List<Error> errors = new ArrayList<>();
                        Error error = Error.builder()
                                        .key("name")
                                        .message("Staff already exists")
                                        .build();
                        errors.add(error);

                        throw new BasicException(BasicResponse.builder()
                                        .messageType(MessageType.ERROR)
                                        .status(HttpStatus.CONFLICT)
                                        .errorsList(errors)
                                        .build());
                }

                // Check if the client exists
                Client client = clientRepository.findById(request.getClientId()).orElseThrow(
                                () -> {
                                        Error error = Error.builder()
                                                        .key("client")
                                                        .message("Client not found")
                                                        .build();
                                        return new BasicException(BasicResponse.builder()
                                                        .messageType(MessageType.ERROR)
                                                        .status(HttpStatus.NOT_FOUND)
                                                        .errorObject(error)
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

                // Build staff DTO
                StaffDTO staffDTO = this.buildDTO(staff);

                // Retrun response
                return BasicResponse.builder()
                                .content(staffDTO)
                                .message("Staff created successfully")
                                .messageType(MessageType.SUCCESS)
                                .status(HttpStatus.CREATED)
                                .build();

        }

        // Update an existing staff member
        public BasicResponse updateStaff(Long staffId, StaffDTO staffDTO, BindingResult bindingResult)
                        throws BasicException {
                Optional<Staff> optionalStaff = staffRepository.findById(staffId);
                if (optionalStaff.isEmpty()) {
                        throw new BasicException(BasicResponse.builder()
                                        .content(null)
                                        .message("Staff not found")
                                        .messageType(MessageType.ERROR)
                                        .status(HttpStatus.NOT_FOUND)
                                        .redirectUrl(null)
                                        .build());
                }

                Map<String, String> messagesList = BasicValidation.getValidationsErrors(bindingResult);
                if (!messagesList.isEmpty()) {
                        throw new BasicException(BasicResponse.builder()
                                        .content(null)
                                        .message("Invalid request")
                                        .messagesObject(messagesList)
                                        .messageType(MessageType.ERROR)
                                        .status(HttpStatus.BAD_REQUEST)
                                        .redirectUrl(null)
                                        .build());
                }

                Staff existingStaff = optionalStaff.get();
                existingStaff.setName(staffDTO.getName());
                existingStaff.setPhone(staffDTO.getPhone());
                existingStaff.setPosition(staffDTO.getPosition());

                if (staffDTO.getClient() != null) {
                        Optional<Client> clientOptional = clientRepository.findById(staffDTO.getClient().getId());
                        if (clientOptional.isEmpty()) {
                                throw new BasicException(BasicResponse.builder()
                                                .content(null)
                                                .message("Client not found")
                                                .messageType(MessageType.ERROR)
                                                .status(HttpStatus.NOT_FOUND)
                                                .redirectUrl(null)
                                                .build());
                        }
                        existingStaff.setClient(clientOptional.get());
                }

                staffRepository.save(existingStaff);

                return BasicResponse.builder()
                                .content(buildDTO(existingStaff))
                                .message("Staff updated successfully")
                                .messageType(MessageType.SUCCESS)
                                .status(HttpStatus.OK)
                                .redirectUrl(null)
                                .build();
        }

        // Delete a staff member
        @Transactional
        public BasicResponse deleteStaff(Long staffId) throws BasicException {

                // Check if the staff member exists
                boolean staffExists = staffRepository.existsById(staffId);
                if (!staffExists) {
                        throw new BasicException(BasicResponse.builder()
                                        .message("Staff not found")
                                        .messageType(MessageType.ERROR)
                                        .status(HttpStatus.NOT_FOUND)
                                        .build());
                }

                // Delete the staff member
                staffRepository.deleteById(staffId);

                // Return a BasicResponse object
                return BasicResponse.builder()
                                .message("Staff deleted successfully")
                                .messageType(MessageType.SUCCESS)
                                .status(HttpStatus.OK)
                                .build();

        }

        // Get a staff member by ID
        public BasicResponse getStaffById(Long staffId) throws BasicException {
                Optional<Staff> optionalStaff = staffRepository.findById(staffId);
                if (optionalStaff.isEmpty()) {
                        throw new BasicException(BasicResponse.builder()
                                        .content(null)
                                        .message("Staff not found")
                                        .messageType(MessageType.ERROR)
                                        .status(HttpStatus.NOT_FOUND)
                                        .redirectUrl(null)
                                        .build());
                }

                Staff staff = optionalStaff.get();

                return BasicResponse.builder()
                                .content(buildDTO(staff))
                                .message("Staff found")
                                .messageType(MessageType.SUCCESS)
                                .status(HttpStatus.OK)
                                .redirectUrl(null)
                                .build();
        }

        // Search a staff member by name, phone, position, client name, or client
        // company
        public BasicResponse searchStaff(String search, Integer page, Integer size) throws BasicException {
                // Create a Pageable object to handle pagination
                Pageable pageable = PageRequest.of(page - 1, size);

                // Get a page of staff members that match the search criteria
                Page<Staff> staffPage = staffRepository.searchStaff(search, pageable);

                // If the page is empty, return an empty BasicResponse object
                if (staffPage.isEmpty()) {
                        return BasicResponse.builder()
                                        .message("No staff members found")
                                        .messageType(MessageType.INFO)
                                        .status(HttpStatus.OK)
                                        .build();
                }

                // Build a list of StaffDTO objects from the page of Staff objects
                List<StaffDTO> staffDTOs = staffPage.getContent().stream()
                                .map(this::buildDTO)
                                .collect(Collectors.toList());

                // Build a MetaData object
                MetaData metaData = MetaData.builder()
                                .currentPage(staffPage.getNumber() + 1)
                                .totalPages(staffPage.getTotalPages())
                                .size(staffPage.getSize())
                                .totalElements((int) staffPage.getTotalElements())
                                .build();

                // Build and return a BasicResponse object
                return BasicResponse.builder()
                                .content(staffDTOs)
                                .metadata(metaData)
                                .status(HttpStatus.OK)
                                .build();
        }

        // Get list of all staff members with pagination
        public BasicResponse getAllStaff(Integer page, Integer size) throws BasicException {
                // Create a Pageable object to handle pagination
                Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());

                // Get a page of staff members
                Page<Staff> staffPage = staffRepository.findAll(pageable);

                // If the page is empty, return an empty BasicResponse object
                if (staffPage.isEmpty()) {
                        return BasicResponse.builder()
                                        .message("No staff members found")
                                        .messageType(MessageType.INFO)
                                        .status(HttpStatus.OK)
                                        .build();
                }

                // Build a list of StaffDTO objects from the page of Staff objects
                List<StaffDTO> staffDTOs = staffPage.getContent().stream()
                                .map(this::buildDTO)
                                .collect(Collectors.toList());

                // Build a MetaData object
                MetaData metaData = MetaData.builder()
                                .currentPage(staffPage.getNumber() + 1)
                                .totalPages(staffPage.getTotalPages())
                                .size(staffPage.getSize())
                                .totalElements((int) staffPage.getTotalElements())
                                .build();

                // Build and return a BasicResponse object
                return BasicResponse.builder()
                                .content(staffDTOs)
                                .metadata(metaData)
                                .status(HttpStatus.OK)
                                .build();
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
}
