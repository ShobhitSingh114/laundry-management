package com.laundry.laundry_management.dto.response;

import java.math.BigDecimal;

import com.laundry.laundry_management.enums.ServiceType;

import lombok.Data;

@Data
public class OrderItemResponse {
    private Long id;
    private Long serviceId;
    private String serviceName;
    private ServiceType serviceType;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String unit; // per kg, per piece, etc.
}
