#!/usr/bin/env bash
# GetSet Backend - Build & Deploy Script

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}=====================================${NC}"
echo -e "${BLUE}GetSet Rental Platform - Backend${NC}"
echo -e "${BLUE}=====================================${NC}"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java is not installed${NC}"
    echo "Please install Java 21 or later"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ Maven is not installed${NC}"
    echo "Please install Maven 3.8+"
    exit 1
fi

# Check if Docker is installed
if command -v docker &> /dev/null; then
    echo -e "${GREEN}✅ Docker is installed${NC}"
    DOCKER_AVAILABLE=true
else
    echo -e "${YELLOW}⚠️  Docker is not installed (optional)${NC}"
    DOCKER_AVAILABLE=false
fi

echo ""
echo -e "${BLUE}Select build option:${NC}"
echo "1) Build with Maven"
echo "2) Build Docker image"
echo "3) Run with Docker Compose (includes MongoDB)"
echo "4) Run locally (requires MongoDB running separately)"
echo ""

read -p "Enter choice (1-4): " choice

case $choice in
    1)
        echo -e "${BLUE}Building with Maven...${NC}"
        cd getset-backend
        mvn clean package -DskipTests
        echo -e "${GREEN}✅ Build complete!${NC}"
        echo -e "${BLUE}To run: mvn spring-boot:run${NC}"
        ;;
    2)
        if [ "$DOCKER_AVAILABLE" = true ]; then
            echo -e "${BLUE}Building Docker image...${NC}"
            cd getset-backend
            mvn clean package -DskipTests
            cd ..
            docker build -t getset-backend:latest .
            echo -e "${GREEN}✅ Docker image built!${NC}"
            echo -e "${BLUE}To run: docker run -p 8080:8080 getset-backend:latest${NC}"
        else
            echo -e "${YELLOW}Docker is not installed${NC}"
        fi
        ;;
    3)
        if [ "$DOCKER_AVAILABLE" = true ]; then
            echo -e "${BLUE}Starting with Docker Compose...${NC}"
            docker-compose up
        else
            echo -e "${YELLOW}Docker is not installed${NC}"
        fi
        ;;
    4)
        echo -e "${BLUE}Running locally...${NC}"
        echo -e "${YELLOW}Make sure MongoDB is running on localhost:27017${NC}"
        cd getset-backend
        mvn spring-boot:run
        ;;
    *)
        echo -e "${YELLOW}Invalid choice${NC}"
        exit 1
        ;;
esac

echo ""
echo -e "${GREEN}=====================================${NC}"
echo -e "${GREEN}✨ GetSet Backend Setup Complete!${NC}"
echo -e "${GREEN}=====================================${NC}"
echo ""
echo -e "${BLUE}API URL:${NC} http://localhost:8080/api/v1"
echo -e "${BLUE}Swagger UI:${NC} http://localhost:8080/api/v1/swagger-ui.html"
echo -e "${BLUE}Health Check:${NC} http://localhost:8080/actuator/health"
