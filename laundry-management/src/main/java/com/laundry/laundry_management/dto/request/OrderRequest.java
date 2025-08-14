package com.laundry.laundry_management.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private String pickupAddress;
    private String deliveryAddress;
    private String pickupDate;
    private String deliveryDate;
    private String specialInstructions;
    private List<OrderItemRequest> items;
}
