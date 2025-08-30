package com.ecommerce.user.service;

import com.ecommerce.user.model.User;
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

    public void sendConfirmationEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Confirm Your Email Address");
            message.setText(buildConfirmationEmailBody(user));

            mailSender.send(message);
            log.info("Confirmation email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send confirmation email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send confirmation email", e);
        }
    }

    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Password Reset Request");
            message.setText(buildPasswordResetEmailBody(user, resetToken));

            mailSender.send(message);
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String buildConfirmationEmailBody(User user) {
        return String.format("""
                Hello %s %s,
                
                Thank you for registering with our e-commerce platform!
                
                Please confirm your email address by clicking the link below:
                http://localhost:3000/confirm-email?code=%s&email=%s
                
                This link will expire in 24 hours.
                
                If you didn't create an account, please ignore this email.
                
                Best regards,
                E-commerce Team
                """, user.getFirstName(), user.getLastName(), user.getConfirmationCode(), user.getEmail());
    }

    private String buildPasswordResetEmailBody(User user, String resetToken) {
        return String.format("""
                Hello %s %s,
                
                You requested a password reset for your account.
                
                Please click the link below to reset your password:
                http://localhost:3000/reset-password?token=%s
                
                This link will expire in 1 hour.
                
                If you didn't request this password reset, please ignore this email.
                
                Best regards,
                E-commerce Team
                """, user.getFirstName(), user.getLastName(), resetToken);
    }
}
