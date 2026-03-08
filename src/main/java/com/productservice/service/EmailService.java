package com.productservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Product Service!");
            message.setText(String.format(
                "Hi %s,\n\nWelcome! Your account has been created successfully.\n\nHappy shopping!\nProduct Service Team",
                firstName
            ));
            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendOrderConfirmationEmail(String toEmail, String firstName, Long orderId, String total) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Order Confirmed - #" + orderId);
            message.setText(String.format(
                "Hi %s,\n\nYour order #%d has been confirmed!\nTotal: $%s\n\nThank you for your purchase!\nProduct Service Team",
                firstName, orderId, total
            ));
            mailSender.send(message);
            log.info("Order confirmation email sent to: {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send order confirmation email: {}", e.getMessage());
        }
    }

    @Async
    public void sendOrderStatusUpdateEmail(String toEmail, String firstName, Long orderId, String status) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Order #" + orderId + " Update — " + status);
            message.setText(String.format(
                "Hi %s,\n\nYour order #%d status has been updated to: %s\n\nProduct Service Team",
                firstName, orderId, status
            ));
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send status update email: {}", e.getMessage());
        }
    }
}
