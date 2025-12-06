FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy Maven build
COPY target/getset-backend-0.0.1-SNAPSHOT.jar app.jar

# Create non-root user for security
RUN addgroup -g 1001 appuser && \
    adduser -D -u 1001 -G appuser appuser

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
