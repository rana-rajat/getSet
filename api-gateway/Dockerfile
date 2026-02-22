FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY target/api-gateway-1.0.0.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
