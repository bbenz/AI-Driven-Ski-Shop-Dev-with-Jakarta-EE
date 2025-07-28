# Dockerfile - Multi-stage build for Shopping Cart Service

# Build stage
FROM registry.access.redhat.com/ubi8/openjdk-21:latest AS builder

USER root
WORKDIR /app

# Copy Maven configuration
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -Dquarkus.package.type=uber-jar

# Runtime stage  
FROM registry.access.redhat.com/ubi8/openjdk-21-runtime:latest

WORKDIR /app

# Copy the built application
COPY --from=builder /app/target/*-runner.jar app.jar

# Create non-root user
USER 1001

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8088/q/health/ready || exit 1

# Expose port
EXPOSE 8088

# JVM tuning for containers
ENV JAVA_OPTS="-Xms256m -Xmx1g -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Application startup
ENTRYPOINT ["java", "-jar", "app.jar"]
