package com.ecommerce.order.service;

import com.ecommerce.order.dto.PaymentIntentResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentService {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public PaymentIntentResponse createPaymentIntent(BigDecimal amount, String currency, String description) {
        try {
            // Convert BigDecimal to cents (Stripe uses smallest currency unit)
            long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency.toLowerCase())
                    .setDescription(description)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            log.info("Created payment intent: {} for amount: {}", paymentIntent.getId(), amount);

            return new PaymentIntentResponse(
                    paymentIntent.getClientSecret(),
                    paymentIntent.getId(),
                    null, // orderId will be set later
                    paymentIntent.getStatus()
            );

        } catch (StripeException e) {
            log.error("Error creating payment intent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create payment intent", e);
        }
    }

    public PaymentIntent retrievePaymentIntent(String paymentIntentId) {
        try {
            return PaymentIntent.retrieve(paymentIntentId);
        } catch (StripeException e) {
            log.error("Error retrieving payment intent {}: {}", paymentIntentId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve payment intent", e);
        }
    }

    public boolean confirmPayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = retrievePaymentIntent(paymentIntentId);
            return "succeeded".equals(paymentIntent.getStatus());
        } catch (Exception e) {
            log.error("Error confirming payment {}: {}", paymentIntentId, e.getMessage(), e);
            return false;
        }
    }

    public void cancelPaymentIntent(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = retrievePaymentIntent(paymentIntentId);
            if ("requires_payment_method".equals(paymentIntent.getStatus()) || 
                "requires_confirmation".equals(paymentIntent.getStatus())) {
                paymentIntent.cancel();
                log.info("Cancelled payment intent: {}", paymentIntentId);
            }
        } catch (StripeException e) {
            log.error("Error cancelling payment intent {}: {}", paymentIntentId, e.getMessage(), e);
        }
    }
}
