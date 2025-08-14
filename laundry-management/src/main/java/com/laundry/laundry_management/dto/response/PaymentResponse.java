package com.laundry.laundry_management.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.laundry.laundry_management.enums.PaymentStatus;

import lombok.Data;

@Data
public class PaymentResponse {
    private Long id;
    private String paymentId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String paymentMethod;
    private LocalDateTime createdAt;
}
