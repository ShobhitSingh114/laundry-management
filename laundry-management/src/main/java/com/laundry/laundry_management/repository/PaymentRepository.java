package com.laundry.laundry_management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.laundry.laundry_management.entity.Payment;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
//    Optional<Payment> findByOrderId(Long orderId);
//    Optional<Payment> findByPaymentId(String paymentId);
//    List<Payment> findByStatus(PaymentStatus status);
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByStripePaymentIntentId(String paymentIntentId);
    Optional<Payment> findByStripeChargeId(String chargeId);
}
