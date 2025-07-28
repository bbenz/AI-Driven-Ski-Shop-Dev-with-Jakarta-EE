#!/bin/bash

# run.sh - Shopping Cart Service startup script

set -e

echo "🛒 Starting Shopping Cart Service..."

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed or not in PATH"
    exit 1
fi

# Function to check if a service is healthy
check_service_health() {
    local service_name=$1
    local max_attempts=30
    local attempt=1
    
    echo "⏳ Waiting for $service_name to be healthy..."
    
    while [ $attempt -le $max_attempts ]; do
        if docker compose ps $service_name | grep -q "healthy"; then
            echo "✅ $service_name is healthy"
            return 0
        fi
        
        echo "⏳ Attempt $attempt/$max_attempts: $service_name not ready yet..."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo "❌ $service_name failed to become healthy within timeout"
    return 1
}

# Start infrastructure services first
echo "🚀 Starting infrastructure services..."
docker compose up -d postgres redis zookeeper kafka

# Wait for infrastructure to be ready
check_service_health postgres
check_service_health redis
check_service_health kafka

# Start the cart service
echo "🚀 Starting Shopping Cart Service..."
docker compose up -d cart-service

# Wait for cart service to be ready
echo "⏳ Waiting for Shopping Cart Service to be ready..."
sleep 10

# Check if the service is running
if curl -f http://localhost:8088/q/health/ready &> /dev/null; then
    echo "✅ Shopping Cart Service is ready!"
    echo ""
    echo "🔗 Service URLs:"
    echo "   API:          http://localhost:8088"
    echo "   Swagger UI:   http://localhost:8088/swagger-ui"
    echo "   Health:       http://localhost:8088/q/health"
    echo "   Metrics:      http://localhost:8088/metrics"
    echo ""
    echo "🔗 Infrastructure URLs:"
    echo "   PostgreSQL:   localhost:5432 (cartuser/cartpass)"
    echo "   Redis:        localhost:6379"
    echo "   Kafka:        localhost:9092"
    echo "   Jaeger UI:    http://localhost:16686"
    echo "   Prometheus:   http://localhost:9090"
    echo ""
    echo "📝 Logs: docker compose logs -f cart-service"
else
    echo "❌ Shopping Cart Service failed to start properly"
    echo "📝 Check logs: docker compose logs cart-service"
    exit 1
fi
