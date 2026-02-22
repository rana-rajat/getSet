FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY target/notification-service-1.0.0.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
