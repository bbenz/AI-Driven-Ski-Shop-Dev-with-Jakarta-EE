# Dockerfile - Multi-stage build for AI Support Service

# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy parent pom first and install it to local repository
COPY pom.xml ./parent-pom.xml
RUN mvn install:install-file -Dfile=parent-pom.xml \
    -DgroupId=com.jakartaone2025.ski \
    -DartifactId=ski-resort-management-system \
    -Dversion=1.0.0-SNAPSHOT \
    -Dpackaging=pom

# Copy AI Support Service
COPY 11-ai-support-service/ ./11-ai-support-service/

# Change to service directory and build
WORKDIR /app/11-ai-support-service

# Build the application with Java 21 preview features enabled
RUN mvn clean package -DskipTests \
    -Dquarkus.package.jar.type=uber-jar \
    -Dmaven.compiler.release=21 \
    -Dmaven.compiler.enablePreview=true

# Runtime stage  
FROM registry.access.redhat.com/ubi8/openjdk-21-runtime:1.19

# Install curl for health checks
USER root
RUN microdnf install curl && microdnf clean all

WORKDIR /app

# Copy the built application
# Copy the jar file from builder stage
COPY --from=builder /app/11-ai-support-service/target/*-runner.jar quarkus-run.jar

# Create non-root user
USER 1001

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8091/q/health/ready || exit 1

# Expose port
EXPOSE 8091

# JVM tuning for containers with Java 21 preview features
ENV JAVA_OPTS="-Xms256m -Xmx1g -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 --enable-preview -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

# Application startup with preview features
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS --enable-preview -Djava.util.logging.manager=org.jboss.logmanager.LogManager -jar quarkus-run.jar"]
