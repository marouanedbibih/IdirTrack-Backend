package com.idirtrack.backend.manager;

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
import com.idirtrack.backend.basics.MetaData;
import com.idirtrack.backend.manager.dtos.ManagerDTO;
import com.idirtrack.backend.manager.dtos.ManagerRequest;
import com.idirtrack.backend.manager.dtos.UpdateManagerRequest;
import com.idirtrack.backend.traccar.TraccarUserService;
import com.idirtrack.backend.user.User;
import com.idirtrack.backend.user.UserDTO;
import com.idirtrack.backend.user.UserRole;
import com.idirtrack.backend.user.UserService;
import com.idirtrack.backend.utils.BasicError;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ManagerService {

        private final ManagerRepository managerRepository;
        private final UserService userService;
        private final TraccarUserService traccarUserService;

        /**
         * Delete manager by ID
         * 
         * @param id
         * @return BasicResponse
         * @throws BasicException
         */

        @Transactional
        public BasicResponse deleteManager(Long id) throws BasicException {
                // Get the user from the database
                User user = userService.findUserByID(id);
                // Get the manager from the database
                Manager manager = user.getManager();
                // Delete the user from Traccar
                boolean deleted = traccarUserService.deleteUser(user.getTraccarId());
                // Delete the user from the database
                if (deleted) {
                        managerRepository.delete(manager);
                        return BasicResponse.builder()
                                        .message("Manager deleted successfully")
                                        .status(HttpStatus.OK)
                                        .build();
                }
                // Throw an exception if the user could not be deleted from Traccar
                throw new BasicException(BasicResponse.builder()
                                // .error(BasicError.of("manager", "Failed to delete manager in Traccar"))
                                .messageType(MessageType.ERROR)
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .build());
        }

        /**
         * Service to update existing manager
         * 
         * @param managerRequest
         * @return BasicResponse
         * @throws BasicException
         */

        @Transactional
        public BasicResponse updateManager(UpdateManagerRequest request, Long id) throws BasicException {
                // Get the manager from the database
                User user = userService.findUserByID(id);
                // Check if the user exists, except for the current user
                userService.isUsernameTakenExcept(request.getUsername(), id);
                userService.isEmailTakenExcept(request.getEmail(), id);
                userService.isPhoneTakenExcept(request.getPhone(), id);

                // Build the user DTO
                UserDTO userDTO = UserDTO.builder()
                                .username(request.getUsername())
                                .name(request.getName())
                                .email(request.getEmail())
                                .phone(request.getPhone())
                                .password(request.getPassword())
                                .role(UserRole.MANAGER)
                                .traccarId(user.getTraccarId())
                                .build();

                // Update the user in Traccar
                Map<String, Object> managerTracCar = traccarUserService.updateUser(userDTO);
                // Update the user in the database
                if (managerTracCar != null) {
                        // Save the user in the database
                        User updatedUser = userService.updateUserInDB(userDTO, id);

                        // Return a success response
                        return BasicResponse.builder()
                                        .message("Manager updated successfully")
                                        .status(HttpStatus.OK)
                                        .build();
                }
                // Throw an exception if the user could not be updated in Traccar
                throw new BasicException(BasicResponse.builder()
                                // .error(BasicError.of("manager", "Failed to update manager in Traccar"))
                                .messageType(MessageType.ERROR)
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .build());
        }

        /**
         * Service method to create a new manager
         * This method will check if the user already exists in the database
         * 
         * @param managerRequest
         * @return BasicResponse
         * @throws BasicException
         */

        @Transactional
        public BasicResponse createManager(ManagerRequest managerRequest,String token) throws BasicException {
                // Check if the user exists
                userService.isUsernameTaken(managerRequest.getUsername());
                userService.isEmailTaken(managerRequest.getEmail());
                userService.isPhoneTaken(managerRequest.getPhone());

                // Build the user DTO
                UserDTO userDTO = UserDTO.builder()
                                .username(managerRequest.getUsername())
                                .name(managerRequest.getName())
                                .email(managerRequest.getEmail())
                                .phone(managerRequest.getPhone())
                                .password(managerRequest.getPassword())
                                .role(UserRole.MANAGER)
                                .build();

                // Save the user in Traccar
                Map<String, Object> managerTracCar = traccarUserService.createUser(userDTO, UserRole.MANAGER, token);
                if (managerTracCar != null) {
                        // Get ID from response
                        Long id = Long.parseLong(managerTracCar.get("id").toString());
                        userDTO.setTraccarId(id);
                        // Save user in database
                        User user = userService.createNewUserInDB(userDTO);

                        // Save manager in database
                        Manager manager = Manager.builder()
                                        .user(user)
                                        .build();
                        manager = managerRepository.save(manager);

                        // Return a success response
                        return BasicResponse.builder()
                                        .message("Manager created successfully")
                                        .status(HttpStatus.CREATED)
                                        .build();
                }

                throw new BasicException(BasicResponse.builder()
                                // .error(BasicError.of("manager", "Failed to create manager in Traccar"))
                                .messageType(MessageType.ERROR)
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .build());
        }

        /**
         * Service method to get manager by ID
         * 
         * @param id
         * @return
         * @throws BasicException
         */
        public BasicResponse getManagerById(Long id) throws BasicException {
                // Get the User from the database
                User user = userService.findUserByID(id);
                // Build the User DTO
                UserDTO userDTO = UserDTO.builder()
                                .id(user.getId())
                                .username(user.getUsername())
                                .name(user.getName())
                                .email(user.getEmail())
                                .phone(user.getPhone())
                                .role(user.getRole())
                                .traccarId(user.getTraccarId())
                                .build();
                // Build the Manager DTO
                ManagerDTO managerDTO = ManagerDTO.builder()
                                .id(user.getManager().getId())
                                .user(userDTO)
                                .build();
                // Return the manager DTO
                return BasicResponse.builder()
                                .content(managerDTO)
                                .status(HttpStatus.OK)
                                .build();

        }

        /**
         * Service method to get all managers
         * 
         * @param page
         * @param size
         * @return
         * @throws BasicException
         */
        public BasicResponse getAllManagers(int page, int size) throws BasicException {
                // Create page request
                Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
                // Get all managers from the database
                Page<Manager> managers = managerRepository.findAll(pageable);
                // Check if the managers list is empty
                if (managers.isEmpty()) {
                        throw new BasicException(BasicResponse.builder()
                                        .message("No managers found")
                                        .messageType(MessageType.ERROR)
                                        .status(HttpStatus.NOT_FOUND)
                                        .build());
                }
                // Build the list of Manager DTOs
                List<ManagerDTO> managerDTOs = managers.stream().map(manager -> {
                        User user = manager.getUser();
                        UserDTO userDTO = UserDTO.builder()
                                        .id(user.getId())
                                        .username(user.getUsername())
                                        .name(user.getName())
                                        .email(user.getEmail())
                                        .phone(user.getPhone())
                                        .role(user.getRole())
                                        .traccarId(user.getTraccarId())
                                        .build();
                        return ManagerDTO.builder()
                                        .id(manager.getId())
                                        .user(userDTO)
                                        .build();
                }).collect(Collectors.toList());
                // Build the metadata
                MetaData metaData = MetaData.builder()
                                .currentPage(page)
                                .size(size)
                                .totalPages(managers.getTotalPages())
                                .totalElements((int) managers.getTotalElements())
                                .build();
                // Return the list of Manager DTOs
                return BasicResponse.builder()
                                .content(managerDTOs)
                                .metadata(metaData)
                                .status(HttpStatus.OK)
                                .build();

        }

}
