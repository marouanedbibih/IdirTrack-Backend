package com.idirtrack.backend.traccar;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.idirtrack.backend.user.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Component
public class TracCarUser {
    private Long id;
    private String name;
    private String login;
    private String email;
    private String phone;
    private boolean readonly;
    private boolean administrator;
    private String map;
    private double latitude;
    private double longitude;
    private int zoom;
    private String coordinateFormat;
    private boolean disabled;
    private LocalDateTime expirationTime;
    private int deviceLimit;
    private int userLimit;
    private boolean deviceReadonly;
    private boolean limitCommands;
    private boolean disableReports;
    private boolean fixedEmail;
    private String poiLayer;
    private String totpKey;
    private boolean temporary;
    private String password;
    private Map<String, Object> attributes;

    /**
     * This method is used to setup admin user
     */

    public static TracCarUser buildAdminUser(UserDTO user) {
        return TracCarUser.builder()
                // Admin infos
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .password(user.getPassword())
                // Admin permissions
                .readonly(false)
                .administrator(true)
                .deviceLimit(-1)
                .userLimit(-1)
                .deviceReadonly(false)
                .limitCommands(false)
                .disableReports(false)
                // Admin Map settings
                .map(null)
                .latitude(0.0)
                .longitude(0.0)
                .zoom(0)
                // Admin settings
                .coordinateFormat("string")
                .disabled(false)
                .expirationTime(null)
                .fixedEmail(false)
                .poiLayer("")
                .totpKey(null)
                .temporary(false)
                .login(null)
                // Admin attributes
                .attributes(new HashMap<>()) 
                .build();
    }

    public static TracCarUser buildManagerUser(UserDTO user) {
        return TracCarUser.builder()
                // Manager infos
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .password(user.getPassword())
                // Manager permissions
                .readonly(false)
                .administrator(false)
                .deviceLimit(-1)
                .userLimit(-1)
                .deviceReadonly(false)
                .limitCommands(false)
                .disableReports(false)
                // Manager Map settings
                .map(null)
                .latitude(0.0)
                .longitude(0.0)
                .zoom(0)
                // Manager settings
                .coordinateFormat("string")
                .disabled(false)
                .expirationTime(null)
                .fixedEmail(false)
                .poiLayer("")
                .totpKey(null)
                .temporary(false)
                .login(null)
                // Manager attributes
                .attributes(new HashMap<>()) 
                .build();
    }
    public static TracCarUser buildClient(UserDTO user) {
        return TracCarUser.builder()
                // Client infos
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .password(user.getPassword())
                // Client permissions
                .readonly(true)
                .administrator(false)
                .deviceLimit(-1)
                .userLimit(0)
                .deviceReadonly(true)
                .limitCommands(false)
                .disableReports(false)
                // Client Map settings
                .map(null)
                .latitude(0.0)
                .longitude(0.0)
                .zoom(0)
                // Client settings
                .coordinateFormat("string")
                .disabled(false)
                .expirationTime(null)
                .fixedEmail(false)
                .poiLayer("")
                .totpKey(null)
                .temporary(false)
                .login(null)
                // Client attributes
                .attributes(new HashMap<>()) 
                .build();
    }

}
