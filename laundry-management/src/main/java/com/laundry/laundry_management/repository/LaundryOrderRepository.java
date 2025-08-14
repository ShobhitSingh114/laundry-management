package com.laundry.laundry_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.laundry.laundry_management.entity.LaundryOrder;
import com.laundry.laundry_management.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface LaundryOrderRepository extends JpaRepository<LaundryOrder, Long> {
    List<LaundryOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<LaundryOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status);
    Optional<LaundryOrder> findByOrderNumber(String orderNumber);
}
