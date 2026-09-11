@echo off
REM GetSet Backend - Run All Microservices

setlocal enabledelayedexpansion

echo ====================================
echo GetSet Rental Platform - Starting Microservices
echo ====================================
echo.
echo Make sure your Docker infrastructure (MongoDB, Kafka) is running!
echo.
echo The services will each open in a new terminal window.
echo They are started in dependency order. Please wait for the previous one to initialize before the next.
echo.

echo [1/7] Starting user-service (Port 8081)...
start "User Service" cmd /k "title User Service (8081) && cd user-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo [2/7] Starting property-service (Port 8082)...
start "Property Service" cmd /k "title Property Service (8082) && cd property-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo [3/7] Starting enquiry-service (Port 8083)...
start "Enquiry Service" cmd /k "title Enquiry Service (8083) && cd enquiry-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo [4/7] Starting favorite-service (Port 8084)...
start "Favorite Service" cmd /k "title Favorite Service (8084) && cd favorite-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo [5/7] Starting message-service (Port 8085)...
start "Message Service" cmd /k "title Message Service (8085) && cd message-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo [6/7] Starting notification-service (Port 8086)...
start "Notification Service" cmd /k "title Notification Service (8086) && cd notification-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo [7/7] Starting api-gateway (Port 8090)...
start "API Gateway" cmd /k "title API Gateway (8090) && cd api-gateway && mvn spring-boot:run"

echo.
echo ====================================
echo All services are starting up!
echo API Gateway is available at: http://localhost:8090
echo ====================================
