package com.laundry.laundry_management.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
//    private Long orderId;
//    private String razorpayOrderId;
//    private String razorpayPaymentId;
//    private String razorpaySignature;
    @NotNull
    private Long orderId;
    
    @NotNull
    private String paymentIntentId; // Stripe Payment Intent ID
    
    // Optional: if you want to include payment method ID
    private String paymentMethodId;

}
