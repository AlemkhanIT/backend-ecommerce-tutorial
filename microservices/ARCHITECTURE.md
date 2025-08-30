# E-commerce Microservices Architecture

## Overview

This project transforms a monolithic Spring Boot e-commerce application into a modern microservices architecture with the following key features:

- **Microservices Architecture**: Each business domain is separated into its own service
- **Event-Driven Communication**: Services communicate via Apache Kafka
- **Caching**: Redis for high-performance caching and session management
- **API Gateway**: Single entry point with routing, authentication, and rate limiting
- **Service Discovery**: Eureka for service registration and discovery
- **Monitoring**: Prometheus, Grafana, and ELK stack for observability
- **Containerization**: Docker for consistent deployment across environments
- **CI/CD**: GitHub Actions for automated testing, building, and deployment

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Applications                      │
│                    (Web, Mobile, API Clients)                  │
└─────────────────────┬───────────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────────┐
│                      API Gateway                                │
│              (Spring Cloud Gateway)                             │
│              - Routing & Load Balancing                         │
│              - Authentication & Authorization                   │
│              - Rate Limiting                                    │
│              - Circuit Breaker                                  │
└─────────────────────┬───────────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
┌───────▼──────┐ ┌───▼────┐ ┌──────▼──────┐
│ User Service │ │ Product│ │ Cart Service │
│              │ │Service │ │              │
│ - Auth       │ │        │ │ - Redis      │
│ - Users      │ │ - CRUD │ │ - Session    │
│ - JWT        │ │ - Cache│ │ - Events     │
└───────┬──────┘ └───┬────┘ └──────┬──────┘
        │             │             │
        └─────────────┼─────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
┌───────▼──────┐ ┌───▼────┐ ┌──────▼──────┐
│Order Service │ │Review  │ │Notification │
│              │ │Service │ │Service      │
│ - Processing │ │        │ │             │
│ - Payment    │ │ - CRUD │ │ - Email     │
│ - Events     │ │ - Cache│ │ - SMS       │
└───────┬──────┘ └───┬────┘ └──────┬──────┘
        │             │             │
        └─────────────┼─────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────────┐
│                    Infrastructure Layer                         │
│                                                                 │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐│
│  │ PostgreSQL  │ │    Redis    │ │    Kafka    │ │   Eureka    ││
│  │             │ │             │ │             │ │             ││
│  │ - User DB   │ │ - Cache     │ │ - Events    │ │ - Discovery ││
│  │ - Product DB│ │ - Sessions  │ │ - Messages  │ │ - Registry  ││
│  │ - Order DB  │ │ - Rate Limit│ │ - Streaming │ │ - Health    ││
│  │ - Review DB │ │             │ │             │ │             ││
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘│
└─────────────────────────────────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────────┐
│                    Monitoring Layer                             │
│                                                                 │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐│
│  │ Prometheus  │ │   Grafana   │ │Elasticsearch│ │   Kibana    ││
│  │             │ │             │ │             │ │             ││
│  │ - Metrics   │ │ - Dashboards│ │ - Logs      │ │ - Log UI    ││
│  │ - Alerts    │ │ - Charts    │ │ - Search    │ │ - Analysis  ││
│  │ - Scraping  │ │ - Monitoring│ │ - Storage   │ │ - Reports   ││
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

## Service Details

### 1. API Gateway (Port 8080)
**Technology**: Spring Cloud Gateway
**Responsibilities**:
- Request routing to appropriate microservices
- Authentication and authorization using JWT
- Rate limiting and throttling
- Circuit breaker pattern for fault tolerance
- Load balancing across service instances
- CORS handling
- Request/response transformation

**Key Features**:
- Dynamic routing configuration
- Fallback mechanisms for service failures
- Request/response logging
- Metrics collection

### 2. User Service (Port 8081)
**Technology**: Spring Boot, PostgreSQL, Kafka
**Responsibilities**:
- User registration and authentication
- JWT token generation and validation
- User profile management
- Email confirmation
- Password management
- User role management

**Database**: `user_db`
**Key Features**:
- Secure password hashing with BCrypt
- Email verification workflow
- JWT-based authentication
- Event publishing for user actions

### 3. Product Service (Port 8082)
**Technology**: Spring Boot, PostgreSQL, Redis, Kafka
**Responsibilities**:
- Product catalog management
- Product CRUD operations
- Inventory management
- Product search and filtering
- Category management
- Image handling

**Database**: `product_db`
**Key Features**:
- Redis caching for improved performance
- Full-text search capabilities
- Category-based filtering
- Price range filtering
- Stock management
- Event publishing for product changes

### 4. Cart Service (Port 8083)
**Technology**: Spring Boot, Redis, Kafka
**Responsibilities**:
- Shopping cart management
- Cart item operations (add, remove, update)
- Session management
- Cart persistence
- Cart synchronization

**Storage**: Redis
**Key Features**:
- Redis-based cart storage
- Session management
- Real-time cart updates
- Cart expiration handling
- Event publishing for cart changes

### 5. Order Service (Port 8084)
**Technology**: Spring Boot, PostgreSQL, Kafka
**Responsibilities**:
- Order creation and processing
- Order status management
- Payment integration
- Order history
- Inventory updates
- Order fulfillment

**Database**: `order_db`
**Key Features**:
- Order state management
- Payment processing integration
- Inventory synchronization
- Order tracking
- Event publishing for order changes

### 6. Notification Service (Port 8085)
**Technology**: Spring Boot, Kafka
**Responsibilities**:
- Email notifications
- SMS notifications
- Push notifications
- Notification templates
- Notification scheduling

**Key Features**:
- Multi-channel notifications
- Template-based messaging
- Notification queuing
- Delivery tracking
- Event-driven notifications

### 7. Review Service (Port 8086)
**Technology**: Spring Boot, PostgreSQL, Kafka
**Responsibilities**:
- Product reviews and ratings
- Comment management
- Review moderation
- Review analytics
- Review aggregation

**Database**: `review_db`
**Key Features**:
- Review and rating system
- Comment threading
- Review moderation
- Rating aggregation
- Event publishing for review changes

## Data Flow

### 1. User Registration Flow
```
Client → API Gateway → User Service → Database
                    ↓
                Kafka Event → Notification Service → Email
```

### 2. Product Purchase Flow
```
Client → API Gateway → Cart Service → Redis
                    ↓
                Order Service → Database
                    ↓
                Kafka Events → Product Service (Stock Update)
                           → Notification Service (Confirmation)
```

### 3. Product Review Flow
```
Client → API Gateway → Review Service → Database
                    ↓
                Kafka Event → Product Service (Rating Update)
```

## Event-Driven Architecture

### Kafka Topics
- `user-events`: User registration, email confirmation
- `product-events`: Product creation, updates, stock changes
- `cart-events`: Cart updates, item additions/removals
- `order-events`: Order creation, status updates
- `review-events`: Review creation, updates

### Event Types
- `USER_REGISTERED`: New user registration
- `USER_EMAIL_CONFIRMED`: Email confirmation
- `PRODUCT_CREATED`: New product added
- `PRODUCT_UPDATED`: Product information updated
- `STOCK_UPDATED`: Product stock changed
- `CART_UPDATED`: Cart contents modified
- `ORDER_CREATED`: New order placed
- `ORDER_STATUS_CHANGED`: Order status updated
- `REVIEW_CREATED`: New review added

## Security Architecture

### Authentication Flow
1. User provides credentials to User Service
2. User Service validates credentials and generates JWT
3. JWT is returned to client
4. Client includes JWT in subsequent requests
5. API Gateway validates JWT and extracts user information
6. User information is forwarded to target services

### Authorization
- Role-based access control (RBAC)
- JWT-based authorization
- Service-to-service authentication
- API Gateway-level authorization

### Security Features
- Password hashing with BCrypt
- JWT token expiration
- CORS configuration
- Input validation and sanitization
- SQL injection prevention
- XSS protection

## Performance Optimization

### Caching Strategy
- **Redis**: Session storage, cart data, frequently accessed data
- **Application-level**: Product catalog caching
- **Database**: Query optimization, indexing

### Database Optimization
- Separate databases per service
- Connection pooling
- Query optimization
- Indexing strategy
- Read replicas (for production)

### Service Optimization
- Connection pooling
- Async processing
- Batch operations
- Resource pooling

## Scalability

### Horizontal Scaling
- Stateless services for easy scaling
- Load balancing at API Gateway level
- Database sharding strategies
- Cache clustering

### Vertical Scaling
- Resource allocation per service
- JVM tuning
- Database optimization
- Memory management

## Monitoring and Observability

### Metrics Collection
- **Prometheus**: Metrics scraping and storage
- **Micrometer**: Application metrics
- **Custom metrics**: Business-specific metrics

### Logging
- **ELK Stack**: Centralized logging
- **Structured logging**: JSON format
- **Log correlation**: Request tracing
- **Log aggregation**: Centralized collection

### Health Checks
- Service health endpoints
- Database connectivity checks
- External service health
- Custom health indicators

### Alerting
- Prometheus alerting rules
- Grafana dashboards
- Email/SMS notifications
- Slack integration

## Deployment Architecture

### Containerization
- Docker containers for all services
- Multi-stage builds for optimization
- Security scanning
- Image versioning

### Orchestration
- Docker Compose for local development
- Kubernetes for production (recommended)
- Service mesh (Istio) for advanced features

### CI/CD Pipeline
- GitHub Actions for automation
- Automated testing
- Security scanning
- Automated deployment
- Rollback capabilities

## Technology Stack

### Backend
- **Java 17**: Programming language
- **Spring Boot 3.3.2**: Application framework
- **Spring Cloud**: Microservices framework
- **Spring Security**: Security framework
- **Spring Data JPA**: Data access layer
- **MapStruct**: Object mapping

### Infrastructure
- **PostgreSQL**: Primary database
- **Redis**: Caching and session storage
- **Apache Kafka**: Message streaming
- **Eureka**: Service discovery
- **Docker**: Containerization

### Monitoring
- **Prometheus**: Metrics collection
- **Grafana**: Metrics visualization
- **ELK Stack**: Logging and analysis
- **Micrometer**: Application metrics

### Development Tools
- **Maven**: Build tool
- **Lombok**: Code generation
- **OpenAPI**: API documentation
- **GitHub Actions**: CI/CD

## Best Practices

### Microservices Design
- Single responsibility principle
- Loose coupling
- High cohesion
- API-first design
- Event-driven communication

### Data Management
- Database per service
- Eventual consistency
- CQRS pattern
- Saga pattern for transactions

### Security
- Defense in depth
- Zero trust architecture
- Regular security audits
- Vulnerability scanning

### Performance
- Caching strategies
- Async processing
- Resource optimization
- Load testing

### Monitoring
- Comprehensive logging
- Metrics collection
- Health monitoring
- Performance tracking

## Future Enhancements

### Planned Features
- Payment gateway integration
- Advanced search with Elasticsearch
- Real-time notifications with WebSocket
- Mobile app support
- Multi-tenant architecture
- Advanced analytics

### Scalability Improvements
- Kubernetes deployment
- Service mesh implementation
- Advanced caching strategies
- Database sharding
- CDN integration

### Security Enhancements
- OAuth2 integration
- Advanced threat detection
- API rate limiting
- Data encryption at rest
- Audit logging

This architecture provides a solid foundation for a scalable, maintainable, and high-performance e-commerce platform that can grow with business needs.
