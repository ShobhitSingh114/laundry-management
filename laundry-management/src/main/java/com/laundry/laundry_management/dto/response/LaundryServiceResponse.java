package com.laundry.laundry_management.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.laundry.laundry_management.enums.ServiceType;

@Data
public class LaundryServiceResponse {
    private Long id;
    private String name;
    private String description;
    private ServiceType type;
    private BigDecimal price;
    private String unit;
    private Boolean active;
    private LocalDateTime createdAt;
}
