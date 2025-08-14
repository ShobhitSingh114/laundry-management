package com.laundry.laundry_management.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.laundry.laundry_management.dto.response.LaundryServiceResponse;
import com.laundry.laundry_management.service.LaundryServiceService;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class LaundryServiceController {
    
    private final LaundryServiceService laundryServiceService;
    
    @GetMapping
    public ResponseEntity<List<LaundryServiceResponse>> getAllServices() {
        List<LaundryServiceResponse> services = laundryServiceService.getAllActiveServices();
        return ResponseEntity.ok(services);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<LaundryServiceResponse> getService(@PathVariable Long id) {
        LaundryServiceResponse service = laundryServiceService.getServiceById(id);
        return ResponseEntity.ok(service);
    }
}

