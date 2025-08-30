#!/bin/bash

# E-commerce Microservices Startup Script

set -e

echo "🚀 Starting E-commerce Microservices..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose and try again."
    exit 1
fi

# Create necessary directories
echo "📁 Creating necessary directories..."
mkdir -p monitoring/grafana/dashboards
mkdir -p monitoring/grafana/datasources
mkdir -p monitoring/logstash/pipeline
mkdir -p monitoring/logstash/config

# Start infrastructure services first
echo "🏗️  Starting infrastructure services..."
docker-compose up -d postgres redis kafka zookeeper eureka

# Wait for infrastructure services to be ready
echo "⏳ Waiting for infrastructure services to be ready..."
sleep 30

# Start application services
echo "🚀 Starting application services..."
docker-compose up -d api-gateway user-service product-service cart-service order-service

# Wait for application services to be ready
echo "⏳ Waiting for application services to be ready..."
sleep 30

# Start monitoring services
echo "📊 Starting monitoring services..."
docker-compose up -d prometheus grafana elasticsearch kibana logstash

echo "✅ All services started successfully!"
echo ""
echo "🌐 Service URLs:"
echo "  API Gateway:     http://localhost:8080"
echo "  User Service:    http://localhost:8081"
echo "  Product Service: http://localhost:8082"
echo "  Cart Service:    http://localhost:8083"
echo "  Order Service:   http://localhost:8084"
echo "  Eureka:          http://localhost:8761"
echo "  Prometheus:      http://localhost:9090"
echo "  Grafana:         http://localhost:3000 (admin/admin)"
echo "  Kibana:          http://localhost:5601"
echo ""
echo "📚 API Documentation:"
echo "  API Gateway:     http://localhost:8080/swagger-ui.html"
echo "  User Service:    http://localhost:8081/swagger-ui.html"
echo "  Product Service: http://localhost:8082/swagger-ui.html"
echo "  Cart Service:    http://localhost:8083/swagger-ui.html"
echo "  Order Service:   http://localhost:8084/swagger-ui.html"
echo ""
echo "🔍 To view logs: docker-compose logs -f [service-name]"
echo "🛑 To stop all services: docker-compose down"
