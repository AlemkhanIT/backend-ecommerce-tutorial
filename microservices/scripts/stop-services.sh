#!/bin/bash

# E-commerce Microservices Stop Script

set -e

echo "🛑 Stopping E-commerce Microservices..."

# Stop all services
docker-compose down

echo "✅ All services stopped successfully!"
echo ""
echo "🧹 To remove all data volumes: docker-compose down -v"
echo "🗑️  To remove all images: docker-compose down --rmi all"
