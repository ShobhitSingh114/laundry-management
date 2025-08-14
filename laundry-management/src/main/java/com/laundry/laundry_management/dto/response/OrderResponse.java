package com.laundry.laundry_management.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.laundry.laundry_management.enums.OrderStatus;

import lombok.Data;

@Data
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private String userEmail;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String pickupAddress;
    private String deliveryAddress;
    private LocalDateTime pickupDate;
    private LocalDateTime deliveryDate;
    private String specialInstructions;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
    private PaymentResponse payment;
}
