package com.laundry.laundry_management.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.laundry.laundry_management.dto.request.PaymentRequest;
import com.laundry.laundry_management.dto.response.PaymentResponse;
import com.laundry.laundry_management.security.UserPrincipal;
import com.laundry.laundry_management.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-intent")
    public ResponseEntity<String> createStripePaymentIntent(@RequestParam Long orderId) {
        String clientSecret = paymentService.createStripePaymentIntent(orderId);
        return ResponseEntity.ok(clientSecret);
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        PaymentResponse response = paymentService.confirmPayment(request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(payment);
    }

    // Webhook endpoint for Stripe events
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        paymentService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok("OK");
    }
    
    // ----------------------------
    
    @PostMapping("/create-intent/mock")
    public ResponseEntity<String> createMockPaymentIntent(@RequestParam Long orderId) {
        String clientSecret = paymentService.createMockPaymentIntent(orderId);
        return ResponseEntity.ok(clientSecret);
    }

    @PostMapping("/confirm/mock")
    public ResponseEntity<PaymentResponse> confirmMockPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        PaymentResponse response = paymentService.confirmMockPayment(request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    
    
}























/*
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.laundry.laundry_management.dto.request.PaymentRequest;
import com.laundry.laundry_management.dto.response.PaymentResponse;
import com.laundry.laundry_management.security.UserPrincipal;
import com.laundry.laundry_management.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/create-order")
    public ResponseEntity<String> createRazorpayOrder(@RequestParam Long orderId) {
        String razorpayOrderId = paymentService.createRazorpayOrder(orderId);
        return ResponseEntity.ok(razorpayOrderId);
    }
    
    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        PaymentResponse response = paymentService.verifyPayment(request, currentUser.getId());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(payment);
    }
}
*/










