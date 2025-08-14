package com.laundry.laundry_management.dto.response;

import java.time.LocalDateTime;

import com.laundry.laundry_management.enums.Role;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private Role role;
    private LocalDateTime createdAt;
}
