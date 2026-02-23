@echo off
REM GetSet Backend - Build All Microservices

setlocal enabledelayedexpansion

echo ====================================
echo GetSet Rental Platform - Microservices Build
echo ====================================
echo.

echo [1/8] Building getset-common (Required first)...
cd getset-common
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Failed to build getset-common
    exit /b 1
)
cd ..

echo [2/8] Building user-service...
cd user-service
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Failed to build user-service
    exit /b 1
)
cd ..

echo [3/8] Building property-service...
cd property-service
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Failed to build property-service
    exit /b 1
)
cd ..

echo [4/8] Building enquiry-service...
cd enquiry-service
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Failed to build enquiry-service
    exit /b 1
)
cd ..

echo [5/8] Building favorite-service...
cd favorite-service
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Failed to build favorite-service
    exit /b 1
)
cd ..

echo [6/8] Building message-service...
cd message-service
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Failed to build message-service
    exit /b 1
)
cd ..

echo [7/8] Building notification-service...
cd notification-service
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Failed to build notification-service
    exit /b 1
)
cd ..

echo [8/8] Building api-gateway...
cd api-gateway
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Failed to build api-gateway
    exit /b 1
)
cd ..

echo.
echo ====================================
echo [SUCCESS] All Microservices Built Successfully!
echo ====================================
echo.
