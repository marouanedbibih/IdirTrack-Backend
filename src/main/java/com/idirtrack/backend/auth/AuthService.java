package com.idirtrack.backend.auth;

import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.idirtrack.backend.basics.BasicException;
import com.idirtrack.backend.basics.BasicResponse;
import com.idirtrack.backend.basics.MessageType;
import com.idirtrack.backend.jwt.JwtUtils;
import com.idirtrack.backend.traccar.TracCarSessionService;
import com.idirtrack.backend.user.User;
import com.idirtrack.backend.user.UserDTO;
import com.idirtrack.backend.user.UserService;
import com.idirtrack.backend.utils.BasicError;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final TracCarSessionService tracCarSessionService;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    /**
     * Login service
     */

    public BasicResponse login(AuthRequest request) throws BasicException {
        // Check if the user exists
        User user = userService.findByUsername(request.getUsername());
        // Get the session
        String session = tracCarSessionService.createSession(user.getEmail(), request.getPassword());
        if (session != null) {
            try {
                // Try to authenticate the user
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
                // Build the User DTO
                UserDTO userDTO = UserDTO.builder()
                        .username(user.getUsername())
                        .role(user.getRole())
                        .build();
                // Create the JWT token
                String token = jwtUtils.createToken(userDTO, session);
                String role = userDTO.getRole().toString();
                Map<String, Object> content = Map.of("token", token, "role", role);
                // Return the BasicResponse with the JWT token
                return BasicResponse.builder()
                        .message("Login successful")
                        .content(content)
                        .build();
            } catch (Exception e) {
                // Check if the password is incorrect
                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                    BasicError error = BasicError.of("password", "Incorrect password");
                    throw new BasicException(BasicResponse.builder()
                            // .error(error)
                            .build());

                }
            }

        }

        BasicResponse response = BasicResponse.builder()
                .message("Failed to create session with Traccar")
                .messageType(MessageType.ERROR)
                .build();
        throw new BasicException(response);
    }

}
