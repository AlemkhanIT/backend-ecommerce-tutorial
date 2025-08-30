# E-commerce Microservices Deployment Guide

This guide provides comprehensive instructions for deploying the e-commerce microservices architecture.

## Prerequisites

### System Requirements
- **Docker**: Version 20.10 or higher
- **Docker Compose**: Version 2.0 or higher
- **Java**: Version 17 or higher (for local development)
- **Maven**: Version 3.8 or higher (for local development)
- **Memory**: Minimum 8GB RAM recommended
- **Storage**: Minimum 20GB free disk space

### Required Ports
- 8080: API Gateway
- 8081: User Service
- 8082: Product Service
- 8083: Cart Service
- 8084: Order Service
- 8085: Notification Service
- 8086: Review Service
- 5432: PostgreSQL
- 6379: Redis
- 9092: Kafka
- 8761: Eureka
- 9090: Prometheus
- 3000: Grafana
- 5601: Kibana
- 9200: Elasticsearch

## Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd ecommerce-tutorial/microservices
```

### 2. Start All Services
```bash
# Make scripts executable
chmod +x scripts/*.sh

# Start all services
./scripts/start-services.sh
```

### 3. Verify Deployment
```bash
# Check service health
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

## Manual Deployment

### 1. Infrastructure Services
```bash
# Start infrastructure services first
docker-compose up -d postgres redis kafka zookeeper eureka

# Wait for services to be ready
sleep 30
```

### 2. Application Services
```bash
# Start application services
docker-compose up -d api-gateway user-service product-service cart-service
```

### 3. Monitoring Services
```bash
# Start monitoring services
docker-compose up -d prometheus grafana elasticsearch kibana logstash
```

## Environment Configuration

### Development Environment
```bash
# Set environment variables
export SPRING_PROFILES_ACTIVE=dev
export DB_HOST=localhost
export REDIS_HOST=localhost
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### Production Environment
```bash
# Set production environment variables
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=your-db-host
export REDIS_HOST=your-redis-host
export KAFKA_BOOTSTRAP_SERVERS=your-kafka-hosts
export JWT_SECRET=your-secure-jwt-secret
```

## Service Configuration

### API Gateway
- **Port**: 8080
- **Configuration**: `microservices/api-gateway/src/main/resources/application.yml`
- **Health Check**: `http://localhost:8080/actuator/health`

### User Service
- **Port**: 8081
- **Database**: `user_db`
- **Configuration**: `microservices/user-service/src/main/resources/application.yml`
- **Health Check**: `http://localhost:8081/actuator/health`

### Product Service
- **Port**: 8082
- **Database**: `product_db`
- **Configuration**: `microservices/product-service/src/main/resources/application.yml`
- **Health Check**: `http://localhost:8082/actuator/health`

### Cart Service
- **Port**: 8083
- **Cache**: Redis
- **Configuration**: `microservices/cart-service/src/main/resources/application.yml`
- **Health Check**: `http://localhost:8083/actuator/health`

## Database Setup

### PostgreSQL Databases
The system creates separate databases for each service:
- `user_db`: User management data
- `product_db`: Product catalog data
- `order_db`: Order management data
- `notification_db`: Notification data
- `review_db`: Review and comment data

### Database Initialization
```bash
# Initialize databases
docker-compose exec postgres psql -U user -d ecommerce -f /docker-entrypoint-initdb.d/01-init-databases.sql
```

## Monitoring and Observability

### Prometheus Metrics
- **URL**: http://localhost:9090
- **Configuration**: `monitoring/prometheus.yml`
- **Metrics Endpoints**: `/actuator/prometheus` on each service

### Grafana Dashboards
- **URL**: http://localhost:3000
- **Default Credentials**: admin/admin
- **Data Source**: Prometheus (http://prometheus:9090)

### ELK Stack
- **Elasticsearch**: http://localhost:9200
- **Kibana**: http://localhost:5601
- **Logstash**: Configured to collect logs from all services

## API Documentation

### Swagger UI
Each service provides its own API documentation:
- **API Gateway**: http://localhost:8080/swagger-ui.html
- **User Service**: http://localhost:8081/swagger-ui.html
- **Product Service**: http://localhost:8082/swagger-ui.html
- **Cart Service**: http://localhost:8083/swagger-ui.html

## Testing the Deployment

### 1. Health Checks
```bash
# Check all service health
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

### 2. Service Discovery
```bash
# Check Eureka dashboard
open http://localhost:8761
```

### 3. API Testing
```bash
# Test user registration
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'

# Test product listing
curl http://localhost:8080/api/products
```

## Troubleshooting

### Common Issues

#### 1. Services Not Starting
```bash
# Check service logs
docker-compose logs [service-name]

# Check service status
docker-compose ps
```

#### 2. Database Connection Issues
```bash
# Check PostgreSQL logs
docker-compose logs postgres

# Test database connection
docker-compose exec postgres psql -U user -d ecommerce -c "SELECT 1;"
```

#### 3. Redis Connection Issues
```bash
# Check Redis logs
docker-compose logs redis

# Test Redis connection
docker-compose exec redis redis-cli ping
```

#### 4. Kafka Connection Issues
```bash
# Check Kafka logs
docker-compose logs kafka

# List Kafka topics
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

### Performance Optimization

#### 1. JVM Tuning
```bash
# Add JVM options to docker-compose.yml
environment:
  - JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC
```

#### 2. Database Optimization
```bash
# PostgreSQL configuration
environment:
  - POSTGRES_SHARED_BUFFERS=256MB
  - POSTGRES_EFFECTIVE_CACHE_SIZE=1GB
```

#### 3. Redis Optimization
```bash
# Redis configuration
command: redis-server --maxmemory 512mb --maxmemory-policy allkeys-lru
```

## Scaling

### Horizontal Scaling
```bash
# Scale specific services
docker-compose up -d --scale user-service=3
docker-compose up -d --scale product-service=2
```

### Load Balancing
The API Gateway automatically load balances requests across multiple instances of the same service.

## Security Considerations

### 1. JWT Secret
```bash
# Generate secure JWT secret
openssl rand -base64 32
```

### 2. Database Passwords
```bash
# Use strong passwords
export DB_PASSWORD=$(openssl rand -base64 32)
```

### 3. Network Security
```bash
# Use Docker networks for service isolation
docker network create ecommerce-network
```

## Backup and Recovery

### Database Backup
```bash
# Backup all databases
docker-compose exec postgres pg_dumpall -U user > backup.sql
```

### Redis Backup
```bash
# Backup Redis data
docker-compose exec redis redis-cli BGSAVE
```

## Maintenance

### Log Rotation
```bash
# Configure log rotation in docker-compose.yml
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
```

### Health Monitoring
```bash
# Set up health check alerts
# Configure in Prometheus and Grafana
```

## Production Deployment

### 1. Use Production Images
```bash
# Build production images
docker-compose -f docker-compose.prod.yml build
```

### 2. Configure SSL/TLS
```bash
# Add SSL certificates
# Configure in API Gateway
```

### 3. Set Up Monitoring
```bash
# Configure production monitoring
# Set up alerting rules
```

## Support

For issues and questions:
1. Check the logs: `docker-compose logs [service-name]`
2. Review the health checks: `http://localhost:8080/actuator/health`
3. Check the monitoring dashboards
4. Review the API documentation

## Cleanup

### Stop All Services
```bash
./scripts/stop-services.sh
```

### Remove All Data
```bash
docker-compose down -v
docker system prune -a
```
