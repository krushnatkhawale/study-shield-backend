FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY ss-content-service/build.gradle ss-content-service/build.gradle

RUN chmod +x gradlew && chmod -R +x gradle/wrapper

COPY ss-content-service ss-content-service/

RUN ./gradlew :ss-content-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=builder /app/ss-content-service/build/libs/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=30s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
