# Multi-stage build for Study Shield Modulith
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copy Gradle wrapper and build files first for better caching
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle .

# Create stub directories for modules not in this build (settings.gradle needs them)
RUN mkdir -p ss-content-service/src/main/java \
    && mkdir -p ss-api-gateway/src/main/java \
    && mkdir -p ss-user-service/src/main/java \
    && mkdir -p ss-quiz-attempts/src/main/java \
    && mkdir -p ss-tv-device-service/src/main/java \
    && mkdir -p ss-regression-suite/src/main/java

# Copy modulith source
COPY ss-modulith/ ss-modulith/

# Build the bootJar
RUN chmod +x gradlew && ./gradlew :ss-modulith:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/ss-modulith/build/libs/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
