# Multi-stage build for optimized image size
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy dependency configuration first for better caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage with optimized JRE
FROM eclipse-temurin:21.0.3_9-jre-alpine

# Create non-root user for security
RUN addgroup -g 1001 -S spring && \
    adduser -u 1001 -S spring -G spring

WORKDIR /app

# Create directory and set permissions
RUN mkdir -p /app && chown spring:spring /app

# Copy the JAR file from build stage
COPY --from=build --chown=spring:spring /app/target/sgca-backend-0.0.1-SNAPSHOT.jar app.jar

# Switch to non-root user
USER spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run with optimized JVM settings for Java 21
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseParallelGC", \
    "-Xlog:gc*:gc.log:time", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]