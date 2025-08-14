package com.laundry.laundry_management.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - Laundry Management System");
            
            String resetUrl = "http://localhost:3000/reset-password?token=" + resetToken;
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "You have requested to reset your password for the Laundry Management System.\n\n" +
                "Please click on the following link to reset your password:\n" +
                "%s\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not request this password reset, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Laundry Management Team",
                userName, resetUrl
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            log.info("Password reset email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email");
        }
    }
    
    public void sendOrderConfirmationEmail(String toEmail, String userName, String orderNumber) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Order Confirmation - " + orderNumber);
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "Thank you for placing your order with us!\n\n" +
                "Your order number is: %s\n\n" +
                "We will process your order and notify you about pickup and delivery updates.\n\n" +
                "You can track your order status by logging into your account.\n\n" +
                "Best regards,\n" +
                "Laundry Management Team",
                userName, orderNumber
            );
            
            message.setText(emailBody);
            mailSender.send(message);
            
            log.info("Order confirmation email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to: {}", toEmail, e);
        }
    }
}
