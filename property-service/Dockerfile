FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY target/property-service-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
