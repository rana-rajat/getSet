FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY target/message-service-1.0.0.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
