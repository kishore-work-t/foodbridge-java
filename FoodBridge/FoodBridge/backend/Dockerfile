# Stage 1: Build the Spring Boot application using Maven
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Pre-fetch dependencies to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production jar
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Add a non-root system user for security
RUN addgroup -S foodbridge && adduser -S foodbridge -G foodbridge

# Copy built artifact from builder stage
COPY --from=builder /build/target/*.jar app.jar
RUN chown -R foodbridge:foodbridge /app

USER foodbridge

# Render sets PORT dynamically (default 8080)
ENV PORT=8080
EXPOSE 8080

# Health check probe
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:${PORT}/api/health || exit 1

ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT} -jar app.jar"]
