# Ski Resort Management System - Microservices Architecture

## Overview

This is a comprehensive microservices-based ski resort management system built
with Jakarta EE 11, Java 21 LTS, and WildFly 31.0.1. The system is designed
to handle all aspects of ski resort operations including user management,
equipment rental, lift tickets, lessons, payments, and customer support.

## Architecture

The system follows a microservices architecture pattern with the following components:

### 🚪 01. API Gateway Service (Port 8080)

- **Purpose**: Central entry point for all client requests
- **Features**: Request routing, authentication, rate limiting, CORS handling
- **Technology**: Jakarta EE 11, JAX-RS, JWT, MicroProfile
- **Endpoints**: Routes to all downstream services based on path prefixes

### 👥 02. User Management Service (Port 8081)

- **Purpose**: User registration, profile management, authentication data
- **Features**: User CRUD, profile management, role management, skill level tracking
- **Technology**: Jakarta EE 11, JPA/Hibernate, PostgreSQL
- **Entities**: User, UserProfile, UserPreferences

### 🎿 03. Product Catalog Service (Port 8082)

- **Purpose**: Equipment catalog, lift tickets, lesson packages
- **Features**: Product management, pricing, availability, categories
- **Technology**: Jakarta EE 11, JPA/Hibernate, PostgreSQL
- **Entities**: Product, Category, PriceRule, ProductVariant

### 🔐 04. Authentication Service (Port 8083)

- **Purpose**: User authentication, JWT token management
- **Features**: Login/logout, token refresh, session management
- **Technology**: Jakarta EE 11, JWT, BCrypt, Redis for session storage
- **Features**: Multi-factor authentication, password reset, account verification

### 📦 05. Inventory Management Service (Port 8084)

- **Purpose**: Equipment inventory, availability tracking
- **Features**: Stock management, reservations, maintenance tracking
- **Technology**: Jakarta EE 11, JPA/Hibernate, PostgreSQL
- **Entities**: Equipment, InventoryItem, Reservation, MaintenanceRecord

### 🛒 06. Order Management Service (Port 8085)

- **Purpose**: Order processing, booking management
- **Features**: Order lifecycle, booking confirmations, cancellations
- **Technology**: Jakarta EE 11, JPA/Hibernate, Event sourcing
- **Entities**: Order, OrderItem, Booking, Reservation

### 💳 07. Payment Service (Port 8086)

- **Purpose**: Payment processing, transaction management
- **Features**: Multiple payment methods, refunds, payment tracking
- **Technology**: Jakarta EE 11, Payment gateway integration, PCI compliance
- **Entities**: Payment, Transaction, PaymentMethod, Refund

### 🛍️ 08. Shopping Cart Service (Port 8087)

- **Purpose**: Shopping cart management, session handling
- **Features**: Cart persistence, item management, pricing calculation
- **Technology**: Jakarta EE 11, Redis for cart storage, real-time updates
- **Features**: Cart sharing, saved carts, automatic cleanup

### 🎫 09. Coupon/Discount Service (Port 8088)

- **Purpose**: Promotional codes, discount management
- **Features**: Coupon validation, discount calculation, usage tracking
- **Technology**: Jakarta EE 11, JPA/Hibernate, PostgreSQL
- **Entities**: Coupon, Discount, PromotionRule, UsageRecord

### ⭐ 10. Points/Loyalty Service (Port 8089)

- **Purpose**: Customer loyalty program, points management
- **Features**: Points earning, redemption, tier management
- **Technology**: Jakarta EE 11, JPA/Hibernate, PostgreSQL
- **Entities**: LoyaltyAccount, PointsTransaction, LoyaltyTier

### 🤖 11. AI Support Service (Port 8090)

- **Purpose**: Intelligent customer support, chatbot
- **Features**: Natural language processing, automated responses, escalation
- **Technology**: Jakarta EE 11, AI/ML integration, WebSocket for real-time chat
- **Features**: Multi-language support, sentiment analysis

### 🖥️ 12. Frontend Service (Port 8091)

- **Purpose**: Web frontend, customer and admin interfaces
- **Features**: Responsive UI, real-time updates, PWA capabilities
- **Technology**: Modern web technologies, WebSocket, Service Worker
- **Interfaces**: Customer portal, admin dashboard, staff interface

## Technology Stack

### Core Technologies

- **Java 21 LTS** with Virtual Threads and modern language features
- **Jakarta EE 11** for enterprise functionality
- **WildFly 31.0.1** as the application server
- **MicroProfile 6.1** for microservices specifications

### Database & Persistence

- **PostgreSQL** for primary data storage
- **Hibernate 6.4.1** for ORM
- **Redis** for caching and session storage
- **H2** for testing

### Security

- **JWT** for authentication tokens
- **BCrypt** for password hashing
- **HTTPS/TLS** for secure communication
- **OAuth2/OpenID Connect** for external authentication

### Communication

- **JAX-RS** for REST APIs
- **WebSocket** for real-time communication
- **JMS** for asynchronous messaging
- **gRPC** for high-performance inter-service communication

### Monitoring & Observability

- **MicroProfile Health** for health checks
- **MicroProfile Metrics** for application metrics
- **MicroProfile OpenTracing** for distributed tracing
- **Structured logging** with JSON format

## Getting Started

### Prerequisites

- Java 21 LTS
- Maven 3.9+
- PostgreSQL 15+
- Redis 7+
- WildFly 31.0.1

### Building the Project

```bash
# Clone the repository
git clone https://github.com/jakartaone2025/ski-resort-system.git
cd ski-resort-system/microservices

# Build all services
mvn clean compile

# Run tests
mvn test

# Package all services
mvn package
```

### Running Services

#### Option 1: Individual Services

```bash
# API Gateway
cd 01-api-gateway-service
mvn wildfly:deploy

# User Management
cd 02-user-management-service
mvn wildfly:deploy
```

#### Option 2: Docker Compose

```bash
# Start all services with dependencies
docker-compose up -d
```

### Service URLs

- API Gateway: <http://localhost:8080>
- User Management: <http://localhost:8081>
- Product Catalog: <http://localhost:8083>
- Authentication: <http://localhost:8084>
- Inventory Management: <http://localhost:8085>
- Order Management: <http://localhost:8086>
- Payment Service: <http://localhost:8087>
- Shopping Cart: <http://localhost:8088>
- Coupon/Discount: <http://localhost:8089>
- Points/Loyalty: <http://localhost:8090>
- AI Support: <http://localhost:8091>
- Frontend: <http://localhost:8092>

## API Documentation

Each service provides OpenAPI documentation accessible at:

- `http://localhost:<port>/<service-name>/api/openapi`

## Configuration

Configuration is managed through MicroProfile Config:

- **Environment variables** for production deployment
- **Properties files** for development
- **Kubernetes ConfigMaps** for container environments

## Database Schema

Each service manages its own database schema:

- **User Management**: User profiles, authentication data
- **Product Catalog**: Products, categories, pricing
- **Inventory**: Equipment, stock levels, reservations
- **Orders**: Order history, bookings, fulfillment
- **Payments**: Transactions, payment methods
- **Loyalty**: Points, tiers, rewards

## Security & Authentication

### Authentication Flow

1. User authenticates via Authentication Service
2. JWT token issued with user claims
3. API Gateway validates tokens for all requests
4. Service-to-service communication uses JWT forwarding

### Authorization

- **Role-based access control (RBAC)**
- **Resource-level permissions**
- **API key authentication for service-to-service**

## Monitoring

### Health Checks

- `/health` - MicroProfile Health endpoint
- `/health/live` - Liveness probe
- `/health/ready` - Readiness probe

### Metrics

- `/metrics` - Prometheus-compatible metrics
- Custom business metrics for each service

### Logging

- Structured JSON logging
- Correlation IDs for request tracing
- Centralized log aggregation

## Development Guidelines

### Code Standards

- Java 21 best practices
- Constructor injection over field injection
- Comprehensive error handling
- Unit and integration testing

### Testing Strategy

- **Unit tests** with JUnit 5 and Mockito
- **Integration tests** with TestContainers
- **Contract testing** with Pact
- **End-to-end testing** with REST Assured

### CI/CD Pipeline

- **Build**: Maven compilation and testing
- **Quality**: SonarQube code analysis
- **Security**: OWASP dependency scanning
- **Deploy**: Blue-green deployment strategy

## Scalability & Performance

### Horizontal Scaling

- Stateless service design
- Database connection pooling
- Load balancing with HAProxy/NGINX

### Caching Strategy

- **Redis** for session and frequently accessed data
- **CDN** for static content
- **Application-level** caching with Caffeine

### Performance Optimization

- **Java 21 Virtual Threads** for improved concurrency
- **Connection pooling** for database efficiency
- **Async processing** for long-running operations

## Deployment

### Container Strategy

- **Docker** containers for each service
- **Multi-stage builds** for optimized images
- **Health checks** integrated in containers

### Kubernetes

- **Helm charts** for deployment templates
- **ConfigMaps** for configuration management
- **Secrets** for sensitive data
- **Ingress** for external access

### Monitoring in Production

- **Prometheus** for metrics collection
- **Grafana** for dashboards
- **ELK Stack** for log analysis
- **Jaeger** for distributed tracing

## Contributing

1. Fork the repository
2. Create a feature branch
3. Implement changes with tests
4. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For questions and support:

- **Documentation**: [Wiki](https://github.com/jakartaone2025/ski-resort-system/wiki)
- **Issues**: [GitHub Issues](https://github.com/jakartaone2025/ski-resort-system/issues)
- **Discussions**: [GitHub Discussions](https://github.com/jakartaone2025/ski-resort-system/discussions)
