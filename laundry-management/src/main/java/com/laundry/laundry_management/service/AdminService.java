package com.laundry.laundry_management.service;

import com.laundry.laundry_management.dto.response.OrderResponse;
import com.laundry.laundry_management.dto.response.UserResponse;
import com.laundry.laundry_management.entity.LaundryOrder;
import com.laundry.laundry_management.entity.User;
import com.laundry.laundry_management.enums.OrderStatus;
import com.laundry.laundry_management.exception.ResourceNotFoundException;
import com.laundry.laundry_management.repository.LaundryOrderRepository;
import com.laundry.laundry_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final UserRepository userRepository;
    private final LaundryOrderRepository orderRepository;
    private final ModelMapper modelMapper;
    
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(user -> modelMapper.map(user, UserResponse.class));
    }
    
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        Page<LaundryOrder> orders = orderRepository.findAll(pageable);
        return orders.map(order -> modelMapper.map(order, OrderResponse.class));
    }
    
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        LaundryOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        order.setStatus(status);
        LaundryOrder savedOrder = orderRepository.save(order);
        
        return modelMapper.map(savedOrder, OrderResponse.class);
    }
    
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        List<LaundryOrder> orders = orderRepository.findByStatusOrderByCreatedAtDesc(status);
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderResponse.class))
                .collect(Collectors.toList());
    }
}
