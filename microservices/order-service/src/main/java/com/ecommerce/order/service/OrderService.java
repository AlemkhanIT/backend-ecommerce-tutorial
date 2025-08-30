package com.ecommerce.order.service;

import com.ecommerce.order.client.CartServiceClient;
import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.client.UserServiceClient;
import com.ecommerce.order.dto.*;
import com.ecommerce.order.exception.EmailNotConfirmedException;
import com.ecommerce.order.exception.EmptyCartException;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.exception.PaymentFailedException;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final StripePaymentService stripePaymentService;
    private final UserServiceClient userServiceClient;
    private final CartServiceClient cartServiceClient;
    private final ProductServiceClient productServiceClient;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Transactional
    public PaymentIntentResponse createOrder(CreateOrderRequest request) {
        // Validate user email confirmation
        UserDTO user = userServiceClient.getUserById(request.getUserId());
        if (user == null) {
            throw new OrderNotFoundException("User not found with id: " + request.getUserId());
        }
        
        if (!user.isEmailConfirmed()) {
            throw new EmailNotConfirmedException("User email must be confirmed before placing an order");
        }

        // Get cart for order
        CartDTO cart = cartServiceClient.getCartForOrder(request.getUserId());
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new EmptyCartException("Cannot create an order with an empty cart");
        }

        // Create order
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setAddress(request.getAddress());
        order.setPhoneNumber(request.getPhoneNumber());
        order.setStatus(Order.OrderStatus.PENDING_PAYMENT);
        order.setTotalAmount(cart.getTotalAmount());

        // Create order items
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItemDTO cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setImageUrl(cartItem.getImageUrl());
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Create Stripe payment intent
        PaymentIntentResponse paymentIntent = stripePaymentService.createPaymentIntent(
                savedOrder.getTotalAmount(),
                "usd",
                "E-commerce Order #" + savedOrder.getId()
        );

        // Update order with payment intent details
        savedOrder.setStripePaymentIntentId(paymentIntent.getPaymentIntentId());
        savedOrder.setStripeClientSecret(paymentIntent.getClientSecret());
        orderRepository.save(savedOrder);

        // Set order ID in payment response
        paymentIntent.setOrderId(savedOrder.getId());

        // Publish order created event
        OrderDTO orderDTO = orderMapper.toDTO(savedOrder);
        OrderEvent event = OrderEvent.orderCreated(orderDTO);
        kafkaTemplate.send("order-events", event);

        log.info("Order created successfully: {} for user: {}", savedOrder.getId(), request.getUserId());
        return paymentIntent;
    }

    @Transactional
    public OrderDTO confirmPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (order.getStripePaymentIntentId() == null) {
            throw new PaymentFailedException("No payment intent found for order: " + orderId);
        }

        // Confirm payment with Stripe
        boolean paymentConfirmed = stripePaymentService.confirmPayment(order.getStripePaymentIntentId());
        
        if (!paymentConfirmed) {
            throw new PaymentFailedException("Payment confirmation failed for order: " + orderId);
        }

        // Update order status
        order.setStatus(Order.OrderStatus.PAYMENT_CONFIRMED);
        Order savedOrder = orderRepository.save(order);

        // Update product stock
        updateProductStock(order);

        // Publish payment confirmed event
        OrderDTO orderDTO = orderMapper.toDTO(savedOrder);
        OrderEvent event = OrderEvent.orderPaymentConfirmed(orderDTO);
        kafkaTemplate.send("order-events", event);

        log.info("Payment confirmed for order: {}", orderId);
        return orderDTO;
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);

        // Publish status updated event
        OrderEvent event = OrderEvent.orderStatusUpdated(orderId, status);
        kafkaTemplate.send("order-events", event);

        log.info("Order status updated to {} for order: {}", status, orderId);
        return orderMapper.toDTO(savedOrder);
    }

    public OrderDTO getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toDTO(order);
    }

    public List<OrderDTO> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orderMapper.toDTOList(orders);
    }

    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        return orderMapper.toDTOList(orders);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == Order.OrderStatus.PENDING_PAYMENT) {
            // Cancel payment intent if still pending
            if (order.getStripePaymentIntentId() != null) {
                stripePaymentService.cancelPaymentIntent(order.getStripePaymentIntentId());
            }
        } else if (order.getStatus() == Order.OrderStatus.PAYMENT_CONFIRMED || 
                   order.getStatus() == Order.OrderStatus.PREPARING) {
            // Restore product stock
            restoreProductStock(order);
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Publish order cancelled event
        OrderEvent event = OrderEvent.orderStatusUpdated(orderId, Order.OrderStatus.CANCELLED);
        kafkaTemplate.send("order-events", event);

        log.info("Order cancelled: {}", orderId);
    }

    private void updateProductStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Map<String, Object> stockUpdateRequest = new HashMap<>();
            stockUpdateRequest.put("productId", item.getProductId());
            stockUpdateRequest.put("quantity", item.getQuantity());
            stockUpdateRequest.put("operation", "decrease");
            
            productServiceClient.updateStock(stockUpdateRequest);
        }
    }

    private void restoreProductStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Map<String, Object> stockUpdateRequest = new HashMap<>();
            stockUpdateRequest.put("productId", item.getProductId());
            stockUpdateRequest.put("quantity", item.getQuantity());
            stockUpdateRequest.put("operation", "increase");
            
            productServiceClient.updateStock(stockUpdateRequest);
        }
    }
}
