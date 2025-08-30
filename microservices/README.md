# E-commerce Microservices Architecture

This project has been transformed from a monolithic Spring Boot application into a microservices architecture with the following services:

## Services Overview

### 1. API Gateway (`api-gateway`)
- **Port**: 8080
- **Purpose**: Entry point for all client requests
- **Features**: 
  - Request routing
  - Authentication & authorization
  - Rate limiting
  - Load balancing
  - Circuit breaker pattern

### 2. User Service (`user-service`)
- **Port**: 8081
- **Purpose**: User management and authentication
- **Features**:
  - User registration/login
  - JWT token generation
  - Password management
  - Email confirmation
  - User profile management

### 3. Product Service (`product-service`)
- **Port**: 8082
- **Purpose**: Product catalog and inventory
- **Features**:
  - Product CRUD operations
  - Image upload/management
  - Inventory tracking
  - Product search and filtering
  - Category management

### 4. Cart Service (`cart-service`)
- **Port**: 8083
- **Purpose**: Shopping cart management
- **Features**:
  - Add/remove items from cart
  - Cart persistence with Redis
  - Cart synchronization
  - Session management

### 5. Order Service (`order-service`)
- **Port**: 8084
- **Purpose**: Order processing and management
- **Features**:
  - Order creation with Stripe payment integration
  - Email confirmation validation
  - Order status tracking
  - Payment processing and confirmation
  - Order history
  - Inventory updates
  - Secure payment handling

### 6. Notification Service (`notification-service`)
- **Port**: 8085
- **Purpose**: Email and messaging
- **Features**:
  - Email notifications
  - Order confirmations
  - Password reset emails
  - Marketing emails
  - SMS notifications (future)

### 7. Review Service (`review-service`)
- **Port**: 8086
- **Purpose**: Product reviews and comments
- **Features**:
  - Product reviews
  - Comment management
  - Rating system
  - Review moderation

## Technology Stack

- **Framework**: Spring Boot 3.3.2
- **Database**: PostgreSQL (per service)
- **Cache**: Redis
- **Message Broker**: Apache Kafka
- **API Gateway**: Spring Cloud Gateway
- **Service Discovery**: Eureka (optional)
- **Payment Processing**: Stripe
- **Containerization**: Docker
- **Orchestration**: Docker Compose
- **CI/CD**: GitHub Actions
- **Monitoring**: Prometheus + Grafana
- **Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)

## Infrastructure Components

- **Kafka**: Event streaming and inter-service communication
- **Redis**: Caching and session storage
- **PostgreSQL**: Primary database for each service
- **Nginx**: Load balancer and reverse proxy
- **Prometheus**: Metrics collection
- **Grafana**: Metrics visualization
- **Jaeger**: Distributed tracing

## Getting Started

1. **Prerequisites**:
   - Docker and Docker Compose
   - Java 17+
   - Maven 3.8+

2. **Local Development**:
   ```bash
   # Start all services
   docker-compose up -d
   
   # Or start individual services
   cd api-gateway && ./mvnw spring-boot:run
   ```

3. **Production Deployment**:
   ```bash
   # Build and deploy with CI/CD
   git push origin main
   ```

## API Documentation

Each service exposes its own API documentation:
- API Gateway: http://localhost:8080/swagger-ui.html
- User Service: http://localhost:8081/swagger-ui.html
- Product Service: http://localhost:8082/swagger-ui.html
- Cart Service: http://localhost:8083/swagger-ui.html
- Order Service: http://localhost:8084/swagger-ui.html
- Notification Service: http://localhost:8085/swagger-ui.html
- Review Service: http://localhost:8086/swagger-ui.html

## Event Flow

1. **User Registration**: User Service → Kafka → Notification Service
2. **Order Creation**: Order Service → Stripe → Kafka → Notification Service, Product Service
3. **Payment Confirmation**: Order Service → Stripe → Kafka → Product Service (Stock Update)
4. **Cart Updates**: Cart Service → Redis → Order Service
5. **Product Updates**: Product Service → Kafka → Cart Service, Order Service

## Security

- JWT-based authentication
- Email confirmation required for orders
- Stripe payment processing
- Service-to-service authentication
- API rate limiting
- CORS configuration
- Input validation and sanitization
- Secure payment handling

## Monitoring and Observability

- Health checks for all services
- Metrics collection with Micrometer
- Distributed tracing with Jaeger
- Centralized logging with ELK
- Performance monitoring with Prometheus
