package com.laundry.laundry_management.service;


import java.math.BigDecimal;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.laundry.laundry_management.dto.request.PaymentRequest;
import com.laundry.laundry_management.dto.response.PaymentResponse;
import com.laundry.laundry_management.entity.LaundryOrder;
import com.laundry.laundry_management.entity.Payment;
import com.laundry.laundry_management.enums.PaymentStatus;
import com.laundry.laundry_management.exception.BadRequestException;
import com.laundry.laundry_management.exception.ResourceNotFoundException;
import com.laundry.laundry_management.repository.LaundryOrderRepository;
import com.laundry.laundry_management.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final LaundryOrderRepository orderRepository;
    private final ModelMapper modelMapper;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public String createStripePaymentIntent(Long orderId) {
        LaundryOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        try {
            // Convert amount to cents (Stripe uses cents)
            long amountInCents = order.getTotalAmount().multiply(new BigDecimal("100")).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("inr")
                    .putMetadata("order_id", order.getId().toString())
                    .putMetadata("order_number", order.getOrderNumber())
                    .setDescription("Payment for laundry order " + order.getOrderNumber())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            
            // Store payment intent ID in order or create a pending payment record
            createPendingPayment(order, intent.getId());
            
            return intent.getClientSecret();

        } catch (StripeException e) {
            log.error("Error creating Stripe PaymentIntent", e);
            throw new BadRequestException("Failed to create payment intent");
        }
    }

    @Transactional
    public PaymentResponse confirmPayment(PaymentRequest request, Long userId) {
        LaundryOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Access denied for this order");
        }

        try {
            // Retrieve the PaymentIntent from Stripe
            PaymentIntent intent = PaymentIntent.retrieve(request.getPaymentIntentId());

            if (!"succeeded".equals(intent.getStatus())) {
                throw new BadRequestException("Payment not succeeded");
            }

            // Verify the amount matches
            BigDecimal expectedAmount = order.getTotalAmount().multiply(new BigDecimal("100"));
            if (!expectedAmount.equals(new BigDecimal(intent.getAmount()))) {
                throw new BadRequestException("Payment amount mismatch");
            }

            // Create or update payment record
            Payment payment = paymentRepository.findByStripePaymentIntentId(intent.getId())
                    .orElse(new Payment());

            payment.setOrder(order);
            payment.setPaymentId("pay_" + System.currentTimeMillis());
            payment.setAmount(order.getTotalAmount());
            payment.setStripePaymentIntentId(intent.getId());
            payment.setStripeChargeId(intent.getLatestCharge());
            payment.setPaymentMethod("STRIPE");
            payment.setStatus(PaymentStatus.SUCCESS);

            Payment savedPayment = paymentRepository.save(payment);
            return modelMapper.map(savedPayment, PaymentResponse.class);

        } catch (StripeException e) {
            log.error("Error confirming payment with Stripe", e);
            throw new BadRequestException("Payment confirmation failed");
        }
    }

    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order"));

        return modelMapper.map(payment, PaymentResponse.class);
    }

    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (Exception e) {
            log.error("Invalid webhook signature", e);
            throw new BadRequestException("Invalid signature");
        }

        // Handle the event
        switch (event.getType()) {
            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event);
                break;
            case "payment_intent.payment_failed":
                handlePaymentIntentFailed(event);
                break;
            default:
                log.info("Unhandled event type: {}", event.getType());
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (intent != null) {
            log.info("PaymentIntent succeeded: {}", intent.getId());
            // Update payment status if needed
            updatePaymentStatus(intent.getId(), PaymentStatus.SUCCESS);
        }
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (intent != null) {
            log.info("PaymentIntent failed: {}", intent.getId());
            updatePaymentStatus(intent.getId(), PaymentStatus.FAILED);
        }
    }

   /* private void createPendingPayment(LaundryOrder order, String paymentIntentId) {
        // Create a pending payment record
        Payment pendingPayment = new Payment();
        pendingPayment.setOrder(order);
        pendingPayment.setPaymentId("pay_pending_" + System.currentTimeMillis());
        pendingPayment.setAmount(order.getTotalAmount());
        pendingPayment.setStripePaymentIntentId(paymentIntentId);
        pendingPayment.setPaymentMethod("STRIPE");
        pendingPayment.setStatus(PaymentStatus.PENDING);
        
        paymentRepository.save(pendingPayment);
    } */

    private void updatePaymentStatus(String paymentIntentId, PaymentStatus status) {
        paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .ifPresent(payment -> {
                    payment.setStatus(status);
                    paymentRepository.save(payment);
                });
    }
    
    
    
 // --------------------------------
    
    
/*    public String createMockPaymentIntent(Long orderId) {
        LaundryOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Create mock payment intent
        String mockIntentId = "pi_test_mock_" + System.currentTimeMillis();
        createPendingPayment(order, mockIntentId);
        
        return "mock_client_secret_" + mockIntentId;
    }

    public PaymentResponse confirmMockPayment(PaymentRequest request, Long userId) {
        LaundryOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Access denied for this order");
        }

        // Create mock successful payment
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentId("pay_mock_" + System.currentTimeMillis());
        payment.setAmount(order.getTotalAmount());
        payment.setStripePaymentIntentId(request.getPaymentIntentId());
        payment.setStripeChargeId("ch_mock_" + System.currentTimeMillis());
        payment.setPaymentMethod("STRIPE");
        payment.setStatus(PaymentStatus.SUCCESS);

        Payment savedPayment = paymentRepository.save(payment);
        return modelMapper.map(savedPayment, PaymentResponse.class);
    }
*/
    // Helper method (if not already present)
/*    private void createPendingPayment(LaundryOrder order, String paymentIntentId) {
        Payment pendingPayment = new Payment();
        pendingPayment.setOrder(order);
        pendingPayment.setPaymentId("pay_pending_" + System.currentTimeMillis());
        pendingPayment.setAmount(order.getTotalAmount());
        pendingPayment.setStripePaymentIntentId(paymentIntentId);
        pendingPayment.setPaymentMethod("STRIPE");
        pendingPayment.setStatus(PaymentStatus.PENDING);
        
        paymentRepository.save(pendingPayment);
    }
*/    
    
    
    
 // ADD THIS METHOD - Mock payment intent creation
    public String createMockPaymentIntent(Long orderId) {
        LaundryOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Create mock payment intent
        String mockIntentId = "pi_test_mock_" + System.currentTimeMillis();
        createPendingPayment(order, mockIntentId);
        
        return "mock_client_secret_" + mockIntentId;
    }

    // ADD THIS METHOD - Mock payment confirmation  
    public PaymentResponse confirmMockPayment(PaymentRequest request, Long userId) {
        LaundryOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Access denied for this order");
        }

        // Check if payment already exists for this order
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(request.getOrderId());
        
        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();
            
            // If payment is pending, update it to success
            if (payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStripePaymentIntentId(request.getPaymentIntentId());
                payment.setStripeChargeId("ch_mock_" + System.currentTimeMillis());
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setPaymentMethod("STRIPE");
                
                Payment savedPayment = paymentRepository.save(payment);
                return modelMapper.map(savedPayment, PaymentResponse.class);
            } else {
                // Payment already completed
                throw new BadRequestException("Payment already completed for this order");
            }
        }

        // Create new payment if none exists
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentId("pay_mock_" + System.currentTimeMillis());
        payment.setAmount(order.getTotalAmount());
        payment.setStripePaymentIntentId(request.getPaymentIntentId());
        payment.setStripeChargeId("ch_mock_" + System.currentTimeMillis());
        payment.setPaymentMethod("STRIPE");
        payment.setStatus(PaymentStatus.SUCCESS);

        Payment savedPayment = paymentRepository.save(payment);
        return modelMapper.map(savedPayment, PaymentResponse.class);
    }

    // ADD THIS HELPER METHOD - Create pending payment
    private void createPendingPayment(LaundryOrder order, String paymentIntentId) {
        // Check if payment already exists for this order
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(order.getId());
        
        if (existingPayment.isPresent()) {
            // Update existing payment with new payment intent ID
            Payment payment = existingPayment.get();
            payment.setStripePaymentIntentId(paymentIntentId);
            payment.setStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);
        } else {
            // Create new pending payment record
            Payment pendingPayment = new Payment();
            pendingPayment.setOrder(order);
            pendingPayment.setPaymentId("pay_pending_" + System.currentTimeMillis());
            pendingPayment.setAmount(order.getTotalAmount());
            pendingPayment.setStripePaymentIntentId(paymentIntentId);
            pendingPayment.setPaymentMethod("STRIPE");
            pendingPayment.setStatus(PaymentStatus.PENDING);
            
            paymentRepository.save(pendingPayment);
        }
    }
    
    
    
}




































/*
import org.springframework.stereotype.Service;
import com.laundry.laundry_management.dto.request.PaymentRequest;
import com.laundry.laundry_management.dto.response.PaymentResponse;
import com.laundry.laundry_management.entity.LaundryOrder;
import com.laundry.laundry_management.entity.Payment;
import com.laundry.laundry_management.enums.PaymentStatus;
import com.laundry.laundry_management.exception.BadRequestException;
import com.laundry.laundry_management.exception.ResourceNotFoundException;
import com.laundry.laundry_management.repository.LaundryOrderRepository;
import com.laundry.laundry_management.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final LaundryOrderRepository orderRepository;
    private final ModelMapper modelMapper;
    
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;
    
    public String createRazorpayOrder(Long orderId) {
        LaundryOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", order.getTotalAmount().multiply(new BigDecimal("100")).intValue()); // amount in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "order_" + order.getOrderNumber());
            
            Order razorpayOrder = razorpay.orders.create(orderRequest);
            return razorpayOrder.get("id");
            
        } catch (RazorpayException e) {
            log.error("Error creating Razorpay order", e);
            throw new BadRequestException("Failed to create payment order");
        }
    }
    
    @Transactional
    public PaymentResponse verifyPayment(PaymentRequest request, Long userId) {
        LaundryOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Access denied for this order");
        }
        
        // Verify Razorpay signature
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());
            
            boolean isValidSignature = Utils.verifyPaymentSignature(options, razorpayKeySecret);
            
            if (!isValidSignature) {
                throw new BadRequestException("Invalid payment signature");
            }
            
            // Create or update payment record
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setPaymentId("pay_" + System.currentTimeMillis());
            payment.setAmount(order.getTotalAmount());
            payment.setRazorpayOrderId(request.getRazorpayOrderId());
            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setRazorpaySignature(request.getRazorpaySignature());
            payment.setPaymentMethod("RAZORPAY");
            payment.setStatus(PaymentStatus.SUCCESS);
            
            Payment savedPayment = paymentRepository.save(payment);
            return modelMapper.map(savedPayment, PaymentResponse.class);
            
        } catch (RazorpayException e) {
            log.error("Error verifying payment", e);
            throw new BadRequestException("Payment verification failed");
        }
    }
    
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order"));
        
        return modelMapper.map(payment, PaymentResponse.class);
    }
}
*/