package com.ecommerce.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-service")
    public Mono<ResponseEntity<Map<String, String>>> userServiceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "User service is temporarily unavailable",
                        "message", "Please try again later",
                        "service", "user-service"
                )));
    }

    @GetMapping("/product-service")
    public Mono<ResponseEntity<Map<String, String>>> productServiceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Product service is temporarily unavailable",
                        "message", "Please try again later",
                        "service", "product-service"
                )));
    }

    @GetMapping("/cart-service")
    public Mono<ResponseEntity<Map<String, String>>> cartServiceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Cart service is temporarily unavailable",
                        "message", "Please try again later",
                        "service", "cart-service"
                )));
    }

    @GetMapping("/order-service")
    public Mono<ResponseEntity<Map<String, String>>> orderServiceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Order service is temporarily unavailable",
                        "message", "Please try again later",
                        "service", "order-service"
                )));
    }

    @GetMapping("/review-service")
    public Mono<ResponseEntity<Map<String, String>>> reviewServiceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Review service is temporarily unavailable",
                        "message", "Please try again later",
                        "service", "review-service"
                )));
    }
}
