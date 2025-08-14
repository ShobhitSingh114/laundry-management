package com.laundry.laundry_management.dto.request;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long serviceId;
    private Integer quantity;
}
