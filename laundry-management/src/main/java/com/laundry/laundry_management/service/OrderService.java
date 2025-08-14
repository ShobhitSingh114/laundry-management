package com.laundry.laundry_management.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.laundry.laundry_management.dto.request.OrderItemRequest;
import com.laundry.laundry_management.dto.request.OrderRequest;
import com.laundry.laundry_management.dto.response.OrderResponse;
import com.laundry.laundry_management.entity.LaundryOrder;
import com.laundry.laundry_management.entity.LaundryService;
import com.laundry.laundry_management.entity.User;
import com.laundry.laundry_management.enums.OrderStatus;
import com.laundry.laundry_management.exception.BadRequestException;
import com.laundry.laundry_management.exception.ResourceNotFoundException;
import com.laundry.laundry_management.repository.LaundryOrderRepository;
import com.laundry.laundry_management.repository.LaundryServiceRepository;
import com.laundry.laundry_management.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final LaundryOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final LaundryServiceRepository serviceRepository;
    private final ModelMapper modelMapper;
    
    @Transactional
    public OrderResponse createOrder(OrderRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        LaundryOrder order = new LaundryOrder();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setPickupAddress(request.getPickupAddress());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setSpecialInstructions(request.getSpecialInstructions());
        
        if (request.getPickupDate() != null) {
            order.setPickupDate(LocalDateTime.parse(request.getPickupDate()));
        }
        if (request.getDeliveryDate() != null) {
            order.setDeliveryDate(LocalDateTime.parse(request.getDeliveryDate()));
        }
        
        // Calculate total amount
        BigDecimal totalAmount = calculateOrderAmount(request.getItems());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        
        LaundryOrder savedOrder = orderRepository.save(order);
        
        return modelMapper.map(savedOrder, OrderResponse.class);
    }
    
    public List<OrderResponse> getUserOrders(Long userId) {
        List<LaundryOrder> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderResponse.class))
                .collect(Collectors.toList());
    }
    
    public OrderResponse getOrder(Long orderId, Long userId) {
        LaundryOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Access denied for this order");
        }
        
        return modelMapper.map(order, OrderResponse.class);
    }
    
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        LaundryOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Access denied for this order");
        }
        
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Order cannot be cancelled at this stage");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        LaundryOrder savedOrder = orderRepository.save(order);
        
        return modelMapper.map(savedOrder, OrderResponse.class);
    }
    
    private String generateOrderNumber() {
        return "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + 
               "-" + String.format("%04d", System.currentTimeMillis() % 10000);
    }
    
    private BigDecimal calculateOrderAmount(List<OrderItemRequest> items) {
        BigDecimal total = BigDecimal.ZERO;
        
        for (OrderItemRequest item : items) {
            LaundryService service = serviceRepository.findById(item.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
            
            BigDecimal itemTotal = service.getPrice().multiply(new BigDecimal(item.getQuantity()));
            total = total.add(itemTotal);
        }
        
        return total;
    }
}
