package com.laundry.laundry_management.dto.response;


import lombok.Data;
import java.time.LocalDateTime;

import com.laundry.laundry_management.enums.Role;

@Data
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private LocalDateTime expiresAt;
}
