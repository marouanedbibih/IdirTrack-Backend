package com.idirtrack.backend.manager;

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


import com.idirtrack.backend.errors.MyException;
import com.idirtrack.backend.errors.NotFoundException;
import com.idirtrack.backend.manager.dtos.ManagerDTO;
import com.idirtrack.backend.manager.dtos.ManagerRequest;
import com.idirtrack.backend.manager.dtos.UpdateManagerRequest;
import com.idirtrack.backend.user.User;
import com.idirtrack.backend.user.UserDTO;
import com.idirtrack.backend.user.UserRole;
import com.idirtrack.backend.user.UserService;
import com.idirtrack.backend.utils.MyResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final UserService userService;

    // Service to delete a manager
    @Transactional
    public MyResponse deleteManager(Long id,String bearerToken) throws NotFoundException,MyException {
        // Find the manager by ID
        Manager manager = this.utilToFindManagerById(id);
        // Delete the user from the system
        userService.deleteUserFromSystem(manager.getUser().getId(), bearerToken);
        // Delete the manager from the database
        managerRepository.delete(manager);
        return MyResponse.builder()
                .message("Manager deleted successfully")
                .status(HttpStatus.OK)
                .build();
    }

    // Service to update a manager
    @Transactional
    public MyResponse updateManager(UpdateManagerRequest request, Long id, String bearerToken) throws NotFoundException{
        // Find the manager by ID
        Manager manager = this.utilToFindManagerById(id);
        // Check if the email and username are already in use except for the current manager
        userService.isUserExistInSystemExcept(request.getUsername(), request.getEmail(), manager.getUser().getId());
        // Update the user in the system
        UserDTO userDTO = UserDTO.builder()
                .id(manager.getUser().getId())
                .username(request.getUsername())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(request.getPassword())
                .role(UserRole.MANAGER)
                .build();
        User user = userService.updateUserInSystem(userDTO, bearerToken);
        // Update the manager in the database
        if (user != null) {
            manager.setUser(user);
            managerRepository.save(manager);
            return MyResponse.builder()
                    .message("Manager updated successfully")
                    .status(HttpStatus.OK)
                    .build();
        } else {
            return MyResponse.builder()
                    .message("Failed to update manager")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    // Service to create a new manager
    @Transactional
    public MyResponse createManager(ManagerRequest managerRequest, String bearerToken) {
        // Check if the user exists
        userService.isUserExistInSystem(managerRequest.getUsername(), managerRequest.getEmail());
        // Build the user DTO
        UserDTO userDTO = UserDTO.builder()
                .username(managerRequest.getUsername())
                .name(managerRequest.getName())
                .email(managerRequest.getEmail())
                .phone(managerRequest.getPhone())
                .password(managerRequest.getPassword())
                .role(UserRole.MANAGER)
                .build();
        // Save the user in the system
        User user = userService.saveUserInSystem(userDTO, bearerToken);
        // Save the manager in the database
        if (user != null) {
            Manager manager = Manager.builder()
                    .user(user)
                    .build();
            managerRepository.save(manager);
            return MyResponse.builder()
                    .message("Manager created successfully")
                    .status(HttpStatus.CREATED)
                    .build();
        } else {
            return MyResponse.builder()
                    .message("Failed to create manager")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    // Service to get manager by ID
    public MyResponse getManagerById(Long id) throws NotFoundException {
        // Get the manager from the database
        Manager manager = this.utilToFindManagerById(id);
        // Build the ManagerDTO
        ManagerDTO managerDTO = utilToBuildManagerDTO(manager);
        // Return the ManagerDTO
        return MyResponse.builder()
                .data(managerDTO)
                .status(HttpStatus.OK)
                .build();
    }

    // Service to get all managers
    public MyResponse getListOfManagers(int page, int size) {
        // Get page of managers
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
        Page<Manager> managersPage = managerRepository.findAll(pageable);
        // Check if the managers list is empty
        if (managersPage.getContent().isEmpty()) {
            return MyResponse.builder()
                    .message("No managers found")
                    .status(HttpStatus.OK)
                    .build();
        } else {
            // Build the list of ManagerTableDTO
            List<ManagerDTO> managerTableDTOs = managersPage.getContent().stream()
                    .map(this::utilToBuildManagerDTO)
                    .collect(Collectors.toList());
            // Build the metadata
            Map<String, Object> metadata = new HashMap<>() {
                {
                    put("totalPages", managersPage.getTotalPages());
                    put("totalElements", managersPage.getTotalElements());
                    put("currentPage", page);
                    put("currentElements", managersPage.getNumberOfElements());
                }
            };
            // Return the list of ManagerTableDTO
            return MyResponse.builder()
                    .data(managerTableDTOs)
                    .metadata(metadata)
                    .status(HttpStatus.OK)
                    .build();
        }

    }

    // Util method to build ManagerTableDTO
    private ManagerDTO utilToBuildManagerDTO(Manager manager) {
        User user = manager.getUser();
        return ManagerDTO.builder()
                .userId(user.getId())
                .managerId(manager.getId())
                .traccarId(user.getTraccarId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }

    // Util method to find manager by ID
    private Manager utilToFindManagerById(Long id) throws NotFoundException {
        return managerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Manager not found with ID: " + id));
    }

}
