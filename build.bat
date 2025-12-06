@echo off
REM GetSet Backend - Build & Deploy Script for Windows

setlocal enabledelayedexpansion

echo ====================================
echo GetSet Rental Platform - Backend
echo ====================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed
    echo Please install Java 21 or later
    exit /b 1
)

REM Check if Maven is installed
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed
    echo Please install Maven 3.8+
    exit /b 1
)

REM Check if Docker is installed
docker --version >nul 2>&1
if %errorlevel% equ 0 (
    echo [SUCCESS] Docker is installed
    set DOCKER_AVAILABLE=true
) else (
    echo [WARNING] Docker is not installed (optional)
    set DOCKER_AVAILABLE=false
)

echo.
echo Select build option:
echo 1) Build with Maven
echo 2) Build Docker image
echo 3) Run with Docker Compose (includes MongoDB)
echo 4) Run locally (requires MongoDB running separately)
echo.

set /p choice="Enter choice (1-4): "

if "%choice%"=="1" (
    echo Building with Maven...
    cd getset-backend
    call mvn clean package -DskipTests
    if %errorlevel% equ 0 (
        echo [SUCCESS] Build complete!
        echo To run: mvn spring-boot:run
    ) else (
        echo [ERROR] Build failed
        exit /b 1
    )
) else if "%choice%"=="2" (
    if "%DOCKER_AVAILABLE%"=="true" (
        echo Building Docker image...
        cd getset-backend
        call mvn clean package -DskipTests
        cd ..
        call docker build -t getset-backend:latest .
        if %errorlevel% equ 0 (
            echo [SUCCESS] Docker image built!
            echo To run: docker run -p 8080:8080 getset-backend:latest
        ) else (
            echo [ERROR] Docker build failed
            exit /b 1
        )
    ) else (
        echo [WARNING] Docker is not installed
    )
) else if "%choice%"=="3" (
    if "%DOCKER_AVAILABLE%"=="true" (
        echo Starting with Docker Compose...
        call docker-compose up
    ) else (
        echo [WARNING] Docker is not installed
    )
) else if "%choice%"=="4" (
    echo Running locally...
    echo [WARNING] Make sure MongoDB is running on localhost:27017
    cd getset-backend
    call mvn spring-boot:run
) else (
    echo [WARNING] Invalid choice
    exit /b 1
)

echo.
echo ====================================
echo [SUCCESS] GetSet Backend Setup Complete!
echo ====================================
echo.
echo API URL: http://localhost:8080/api/v1
echo Swagger UI: http://localhost:8080/api/v1/swagger-ui.html
echo Health Check: http://localhost:8080/actuator/health
