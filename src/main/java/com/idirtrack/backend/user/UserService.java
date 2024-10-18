package com.idirtrack.backend.user;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.idirtrack.backend.basics.BasicException;
import com.idirtrack.backend.basics.BasicResponse;
import com.idirtrack.backend.basics.MessageType;
import com.idirtrack.backend.errors.AlreadyExistException;
import com.idirtrack.backend.errors.MyException;
import com.idirtrack.backend.errors.NotFoundException;
import com.idirtrack.backend.traccar.TraccarUserService;
import com.idirtrack.backend.utils.ErrorResponse;
import com.idirtrack.backend.utils.FieldErrorDTO;
import com.idirtrack.backend.basics.BasicError;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TraccarUserService traccarUserService;

    // Service to save a user in the system
    public User saveUserInSystem(UserDTO userDTO, String bearerToken) throws MyException {

        // Save the user in Traccar system
        Long traccarId = traccarUserService.createUser(userDTO, bearerToken);

        if (traccarId == null) {
            throw new MyException(ErrorResponse.builder()
                    .message("Failed to create user in Traccar: Traccar ID is null")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }

        // Set the traccar id to the userDTO
        userDTO.setTraccarId(traccarId);

        // Encode the password
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        // Save the user in the database
        User user = User.builder()
                .username(userDTO.getUsername())
                .name(userDTO.getName())
                .email(userDTO.getEmail())
                .phone(userDTO.getPhone())
                .password(userDTO.getPassword())
                .role(userDTO.getRole())
                .traccarId(userDTO.getTraccarId())
                .build();

        try {
            user = userRepository.save(user);
            return user;
        } catch (Exception e) {
            throw new MyException(ErrorResponse.builder()
                    .message("Failed to save user in database")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }

    }

    // Service to update a user in the system
    public User updateUserInSystem(UserDTO userDTO, String bearerToken) throws MyException {


         // Fetch the user from the database to get the traccarId if it's not in the userDTO
        if (userDTO.getTraccarId() == null) {
            User existingUser = userRepository.findById(userDTO.getId())
                    .orElseThrow(() -> new MyException(ErrorResponse.builder()
                            .message("User not found in the system")
                            .status(HttpStatus.NOT_FOUND)
                            .build()));
            userDTO.setTraccarId(existingUser.getTraccarId());
        }

        if (userDTO.getTraccarId() == null) {
            throw new MyException(ErrorResponse.builder()
                    .message("Traccar ID cannot be null for updating a user in Traccar")
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        }
            // Update the user in Traccar system
            traccarUserService.updateUser(userDTO, bearerToken);

            // Encode the password
            if (userDTO.getPassword() != null) {
                userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));

            }

        // Save the user in the database
        User user = User.builder()
                .id(userDTO.getId())
                .username(userDTO.getUsername())
                .name(userDTO.getName())
                .email(userDTO.getEmail())
                .phone(userDTO.getPhone())
                .password(userDTO.getPassword())
                .role(userDTO.getRole())
                .traccarId(userDTO.getTraccarId())
                .build();

        try {
            user = userRepository.save(user);
            return user;
        } catch (Exception e) {
            throw new MyException(ErrorResponse.builder()
                    .message("Failed to update user in database")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }

    }

    // Service to delete the user from the system
    public void deleteUserFromSystem(Long id, String bearerToken) throws NotFoundException, MyException {
        // Find the user by id
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorResponse.builder()
                        .message("User not found with id: " + id)
                        .build()));
        // Delete the user from Traccar system
        traccarUserService.deleteUser(user.getTraccarId(), bearerToken);
        // Delete the user from the database
        try {
            userRepository.delete(user);
        } catch (Exception e) {
            throw new MyException(ErrorResponse.builder()
                    .message("Failed to delete user from database")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }

    }


    /**
     * Find the user by username and return it
     * If not found, throw an BasicException
     */

    public User findByUsername(String username) throws BasicException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    BasicError error = BasicError.of("username", "Username not found");
                    return new BasicException(BasicResponse.builder()
                            .error(error)
                            .messageType(MessageType.ERROR)
                            .status(HttpStatus.NOT_FOUND)
                            .build());
                });
    }




    public User findUserByID(Long id) throws BasicException {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    BasicError error = BasicError.of("id", "User not found");
                    return new BasicException(BasicResponse.builder()
                            .error(error)
                            .messageType(MessageType.ERROR)
                            .status(HttpStatus.NOT_FOUND)
                            .build());
                });
    }

    // Check if the use already exist in system
    public boolean isUserExistInSystem(String username, String email) {

        this.isUsernameTaken(username);
        this.isEmailTaken(email);

        return false;
    }

    // Check if the username already taken
    private boolean isUsernameTaken(String username) throws AlreadyExistException {
        if (userRepository.existsByUsername(username)) {
            throw new AlreadyExistException(ErrorResponse.builder()
                    .fieldErrors(List.of(FieldErrorDTO.builder()
                            .field("username")
                            .message("Username is already taken")
                            .build()))
                    .build() // Added this to complete the builder chain
            );
        }
        return false;
    }

    // Check if the email already taken
    private boolean isEmailTaken(String email) throws AlreadyExistException {
        if (userRepository.existsByEmail(email)) {
            throw new AlreadyExistException(ErrorResponse.builder()
                    .fieldErrors(List.of(FieldErrorDTO.builder()
                            .field("email")
                            .message("Email is already taken")
                            .build()))
                    .build() // Added this to complete the builder chain
            );
        }
        return false;
    }

    // Check if the user exists in the system except for the user with the given id
    public boolean isUserExistInSystemExcept(String username, String email, Long id) throws AlreadyExistException {
        this.isUsernameTakenExcept(username, id);
        this.isEmailTakenExcept(email, id);

        return false;

    }

    // Check if the username is already taken, except for the user with the given id
    private boolean isUsernameTakenExcept(String username, Long id) throws AlreadyExistException {
        if (userRepository.existsByUsernameAndIdNot(username, id)) {
            throw new AlreadyExistException(ErrorResponse.builder()
                    .fieldErrors(List.of(FieldErrorDTO.builder()
                            .field("username")
                            .message("Username is already taken by another user")
                            .build()))
                    .build());
        }
        return false;
    }

    // Check if the email is already taken, except for the user with the given id
    private boolean isEmailTakenExcept(String email, Long id) throws AlreadyExistException {
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new AlreadyExistException(ErrorResponse.builder()
                    .fieldErrors(List.of(FieldErrorDTO.builder()
                            .field("email")
                            .message("Email is already taken by another user")
                            .build()))
                    .build());
        }
        return false;
    }


}
