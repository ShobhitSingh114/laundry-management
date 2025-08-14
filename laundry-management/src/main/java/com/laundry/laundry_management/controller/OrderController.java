package com.laundry.laundry_management.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.laundry.laundry_management.dto.request.OrderRequest;
import com.laundry.laundry_management.dto.response.OrderResponse;
import com.laundry.laundry_management.security.UserPrincipal;
import com.laundry.laundry_management.service.OrderService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        OrderResponse response = orderService.createOrder(request, currentUser.getId());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<OrderResponse> orders = orderService.getUserOrders(currentUser.getId());
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        OrderResponse order = orderService.getOrder(orderId, currentUser.getId());
        return ResponseEntity.ok(order);
    }
    
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        OrderResponse order = orderService.cancelOrder(orderId, currentUser.getId());
        return ResponseEntity.ok(order);
    }
}
