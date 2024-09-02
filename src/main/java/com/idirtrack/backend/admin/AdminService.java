package com.idirtrack.backend.admin;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.idirtrack.backend.basics.BasicException;
import com.idirtrack.backend.basics.BasicResponse;
import com.idirtrack.backend.traccar.TraccarUserService;
import com.idirtrack.backend.user.User;
import com.idirtrack.backend.user.UserDTO;
import com.idirtrack.backend.user.UserRepository;
import com.idirtrack.backend.user.UserRequest;
import com.idirtrack.backend.user.UserRole;
import com.idirtrack.backend.user.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

        private final TraccarUserService traccarUserService;
        private final UserService userService;
        private final PasswordEncoder passwordEncoder;
        private final UserRepository userRepository;
        private final AdminRepository adminRepository;

        // public BasicResponse createAdmin(UserRequest request) throws BasicException {

        //         // // Check if the username is already taken
        //         // userService.isUsernameTaken(request.getName());
        //         // // Check if the email is already taken
        //         // userService.isEmailTaken(request.getEmail());
        //         // // Check if the phone is already taken
        //         // userService.isPhoneTaken(request.getPhone());

        //         // Buid the user DTO
        //         UserDTO userDTO = UserDTO.builder()
        //                         .username(request.getUsername())
        //                         .name(request.getName())
        //                         .email(request.getEmail())
        //                         .phone(request.getPhone())
        //                         .password(request.getPassword())
        //                         .build();
        //         // Save the user in TracCar
        //         // Map<String, Object> admin = traccarUserService.createAdmin(userDTO);

        //         if () {
        //                 // set the admin role to the user
        //                 userDTO.setRole(UserRole.ADMIN);
        //                 // Hass the password
        //                 userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        //                 // Build the User entity
        //                 User user = User.builder()
        //                                 .username(userDTO.getUsername())
        //                                 .name(userDTO.getName())
        //                                 .email(userDTO.getEmail())
        //                                 .phone(userDTO.getPhone())
        //                                 .password(userDTO.getPassword())
        //                                 .role(userDTO.getRole())
        //                                 .build();

        //                 // Save the user and admin in the database
        //                 userRepository.save(user);

        //                 // Build the Admin entity
        //                 Admin adminEntity = Admin.builder()
        //                                 .user(user)
        //                                 .build();
        //                 // Save the admin in the database
        //                 adminEntity = adminRepository.save(adminEntity);
        //                 // Set the admin entity to the user
        //                 user.setAdmin(adminEntity);
        //                 // Save the user in the database
        //                 userRepository.save(user);

        //                 // Return success response
        //                 return BasicResponse.builder()
        //                                 .message("Admin user created successfully")
        //                                 .status(HttpStatus.CREATED)
        //                                 .build();

        //         }

        //         throw new BasicException(BasicResponse.builder()
        //                         .message("Traccar user creation failed, is null")
        //                         .content(null)
        //                         .status(HttpStatus.INTERNAL_SERVER_ERROR)
        //                         .build());
        // }

        /**
         * Service method to get all admins with pagination
         * 
         * @param page
         * @param size
         * @return BasicResponse
         * @throws BasicException
         */

        public BasicResponse getAdmins(int page, int size) throws BasicException {
                // Create pagination with sorting by id in descending order
                Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());

                // Retrieve admins with pagination
                Page<Admin> adminPage = adminRepository.findAll(pageable);

                // Check if any admins are found
                if (!adminPage.hasContent()) {
                        throw new BasicException(
                                        BasicResponse.builder()
                                                        .message("No admins found")
                                                        .content(null)
                                                        .status(HttpStatus.NOT_FOUND)
                                                        .build());
                }

                // Map Admin entities to AdminDTOs
                List<AdminDTO> adminDTOs = adminPage.getContent().stream()
                                .map(admin -> AdminDTO.builder()
                                                .adminId(admin.getId())
                                                .user(UserDTO.builder()
                                                                .username(admin.getUser().getUsername())
                                                                .name(admin.getUser().getName())
                                                                .email(admin.getUser().getEmail())
                                                                .phone(admin.getUser().getPhone())
                                                                .role(admin.getUser().getRole())
                                                                .build())
                                                .build())
                                .collect(Collectors.toList());

                // Return a successful response with the list of AdminDTOs
                return BasicResponse.builder()
                                .message("Admins retrieved successfully")
                                .content(adminDTOs)
                                .status(HttpStatus.OK)
                                .build();
        }

}
