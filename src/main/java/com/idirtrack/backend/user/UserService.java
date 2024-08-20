package com.idirtrack.backend.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.idirtrack.backend.basics.BasicException;
import com.idirtrack.backend.basics.BasicResponse;
import com.idirtrack.backend.basics.MessageType;
import com.idirtrack.backend.utils.BasicError;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Check if the username is already taken
     */

    public boolean isUsernameTaken(String username) throws BasicException {
        boolean isTaken = userRepository.existsByUsername(username);
        if (isTaken == true) {
            BasicError error = BasicError.of("username", "Username is already taken");
            throw new BasicException(BasicResponse.builder()
                    // .error(error)
                    .messageType(MessageType.ERROR)
                    .status(HttpStatus.CONFLICT)
                    .build());
        }
        return false;
    }

    /**
     * Check if the email is already taken
     */

    public boolean isEmailTaken(String email) throws BasicException {
        boolean isTaken = userRepository.existsByEmail(email);
        if (isTaken == true) {
            BasicError error = BasicError.of("email", "Email is already taken");
            throw new BasicException(BasicResponse.builder()
                    // .error(error)
                    .messageType(MessageType.ERROR)
                    .status(HttpStatus.CONFLICT)
                    .build());
        }
        return false;
    }

    /**
     * Check if the phone is already taken
     */

    public boolean isPhoneTaken(String phone) throws BasicException {
        boolean isTaken = userRepository.existsByPhone(phone);
        if (isTaken == true) {
            BasicError error = BasicError.of("phone", "Phone is already taken");
            throw new BasicException(BasicResponse.builder()
                    // .error(error)
                    .messageType(MessageType.ERROR)
                    .status(HttpStatus.CONFLICT)
                    .build());
        }
        return false;
    }

    /**
     * Save the user in the database
     */

    public User save(User user) {
        if (user.getPassword() != null) {
            // Hash the password
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        // Save the user
        return userRepository.save(user);
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
                            // .error(error)
                            .messageType(MessageType.ERROR)
                            .status(HttpStatus.NOT_FOUND)
                            .build());
                });
    }

    /**
     * Create a new user in the database
     * 
     * @param userDTO
     * @return
     * @throws BasicException
     */
    public User createNewUserInDB(UserDTO userDTO) throws BasicException {
        User user = User.builder()
                .username(userDTO.getUsername())
                .name(userDTO.getName())
                .email(userDTO.getEmail())
                .phone(userDTO.getPhone())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .role(userDTO.getRole())
                .traccarId(userDTO.getTraccarId())
                .build();
        try {
            user = userRepository.save(user);
        } catch (Exception e) {
            throw new BasicException(BasicResponse.builder()
                    // .error(BasicError.of("user", "User not saved in database"))
                    .messageType(MessageType.ERROR)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }

        return user;
    }

    /**
     * Find user by id
     * 
     * @param id
     * @return User
     * @throws BasicException
     */

    public User findUserByID(Long id) throws BasicException {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    BasicError error = BasicError.of("id", "User not found");
                    return new BasicException(BasicResponse.builder()
                            // .error(error)
                            .messageType(MessageType.ERROR)
                            .status(HttpStatus.NOT_FOUND)
                            .build());
                });
    }

    /**
     * Check if the username is already taken, except for the user with the given id
     * 
     * @param username
     * @param id
     */
    public void isUsernameTakenExcept(String username, Long id) throws BasicException {
        boolean isTaken = userRepository.existsByUsernameAndIdNot(username, id);
        if (isTaken) {
            BasicError error = BasicError.of("username", "Username is already taken by another user");
            throw new BasicException(BasicResponse.builder()
                    // .error(error)
                    .messageType(MessageType.ERROR)
                    .status(HttpStatus.CONFLICT)
                    .build());
        }
    }

    /**
     * Check if the email is already taken, except for the user with the given id.
     *
     * @param email The email to check.
     * @param id    The id of the user to exclude from the check.
     * @throws BasicException if the email is already taken by another user.
     */
    public void isEmailTakenExcept(String email, Long id) throws BasicException {
        boolean isTaken = userRepository.existsByEmailAndIdNot(email, id);
        if (isTaken) {
            BasicError error = BasicError.of("email", "Email is already taken by another user");
            throw new BasicException(BasicResponse.builder()
                    // .error(error)
                    .messageType(MessageType.ERROR)
                    .status(HttpStatus.CONFLICT)
                    .build());
        }
    }

    /**
     * Check if the phone number is already taken, except for the user with the
     * given id.
     *
     * @param phone The phone number to check.
     * @param id    The id of the user to exclude from the check.
     * @throws BasicException if the phone number is already taken by another user.
     */
    public void isPhoneTakenExcept(String phone, Long id) throws BasicException {
        boolean isTaken = userRepository.existsByPhoneAndIdNot(phone, id);
        if (isTaken) {
            BasicError error = BasicError.of("phone", "Phone number is already taken by another user");
            throw new BasicException(BasicResponse.builder()
                    // .error(error)
                    .messageType(MessageType.ERROR)
                    .status(HttpStatus.CONFLICT)
                    .build());
        }
    }

    /**
     * Update the user in the database
     * 
     * @param userDTO
     * @param user
     * @return User
     */
    public User updateUserInDB(UserDTO userDTO, Long id) throws BasicException {
        // Find user by id
        User user = userRepository.findById(id).orElseThrow(
                () -> new BasicException(BasicResponse.builder()
                        // .error(BasicError.of("id", "User not found"))
                        .messageType(MessageType.ERROR)
                        .status(HttpStatus.NOT_FOUND)
                        .build()));

        // Check if the password is exist, then encode it
        if (userDTO.getPassword() != null) {
            userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        // Set the new user data
        user.setUsername(userDTO.getUsername());
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setPassword(userDTO.getPassword());
        user.setRole(userDTO.getRole());
        user.setTraccarId(userDTO.getTraccarId());

        // Try to save the user
        try {
            user = userRepository.save(user);
            return user;
        } 
        // If there is an exception, throw a BasicException
        catch (Exception e) {
            throw new BasicException(BasicResponse.builder()
                    // .error(BasicError.of("user", "User not updated in database"))
                    .messageType(MessageType.ERROR)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        }

    }

}
