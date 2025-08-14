package com.laundry.laundry_management.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.laundry.laundry_management.dto.response.UserResponse;
import com.laundry.laundry_management.security.UserPrincipal;
import com.laundry.laundry_management.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserResponse user = userService.getCurrentUser(currentUser.getId());
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestBody UserResponse request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        UserResponse user = userService.updateProfile(request, currentUser.getId());
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        userService.changePassword(oldPassword, newPassword, currentUser.getId());
        return ResponseEntity.ok("Password changed successfully");
    }
}
