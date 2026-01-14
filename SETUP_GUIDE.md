# Step-by-Step Setup Guide

This guide walks you through setting up and running the Claim Processing Service from scratch.

## Prerequisites Check

Before starting, ensure you have:
- ✅ Java 17 or higher installed (`java -version`)
- ✅ Maven 3.6+ installed (`mvn -version`) OR use the included Maven wrapper
- ✅ Docker and Docker Compose installed (for containerized setup)
- ✅ PostgreSQL (if running without Docker)

## Option 1: Quick Start with Docker (Recommended)

### Step 1: Navigate to Project Directory

**In Git Bash on Windows:**
```bash
# Option 1: Use Unix-style path (recommended for Git Bash)
cd /c/Users/mgamb/"Visual Studio Code"/claim-processing-service

# Option 2: Use Windows path with quotes
cd "c:/Users/mgamb/Visual Studio Code/claim-processing-service"

# Option 3: Navigate step by step
cd /c/Users/mgamb
cd "Visual Studio Code"
cd claim-processing-service
```

**In PowerShell:**
```powershell
cd "c:\Users\mgamb\Visual Studio Code\claim-processing-service"
```

**In Command Prompt:**
```cmd
cd "c:\Users\mgamb\Visual Studio Code\claim-processing-service"
```

**Note:** The quotes are necessary because the directory name contains spaces ("Visual Studio Code").

### Step 2: Start All Services

**Note:** If you get "command not found", see the [Docker Issues](#docker-issues) troubleshooting section below.

```bash
# Try Docker Compose V2 first (newer versions)
docker compose up -d

# If that doesn't work, try V1 (older versions)
docker-compose up -d
```

This command will:
- Pull PostgreSQL 15 image
- Create and start PostgreSQL container
- Build the Spring Boot application
- Start the application container
- Wait for database to be healthy before starting the app

### Step 3: Verify Services are Running
```bash
# Check container status (use 'docker compose' for V2 or 'docker-compose' for V1)
docker compose ps
# or
docker-compose ps

# View application logs
docker compose logs -f app
# or
docker-compose logs -f app

# Check database logs
docker compose logs -f postgres
# or
docker-compose logs -f postgres
```

### Step 4: Test the Service
```bash
# Health check
curl http://localhost:8080/api/claims/health

# Create a test claim
curl -X POST http://localhost:8080/api/claims \
  -H "Content-Type: application/json" \
  -d '{
    "type": "AUTO",
    "amount": 5000.00,
    "status": "PENDING"
  }'

# Get summary
curl http://localhost:8080/api/claims/summary
```

### Step 5: Stop Services (when done)
```bash
# V2
docker compose down
# or V1
docker-compose down
```

To remove volumes (clears database):
```bash
# V2
docker compose down -v
# or V1
docker-compose down -v
```

## Option 2: Local Development Setup

### Step 1: Start PostgreSQL Database

**Using Docker (easiest):**
```bash
docker run -d \
  --name postgres-claims \
  -e POSTGRES_DB=claimsdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15-alpine
```

**Or install PostgreSQL locally:**
- Download from https://www.postgresql.org/download/
- Create database: `createdb claimsdb`
- Note your username and password

### Step 2: Configure Environment Variables (Optional)

The application has defaults, but you can override:

**Windows (PowerShell):**
```powershell
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/claimsdb"
$env:DATABASE_USERNAME="postgres"
$env:DATABASE_PASSWORD="postgres"
$env:SERVER_PORT="8080"
```

**Linux/Mac:**
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/claimsdb
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres
export SERVER_PORT=8080
```

### Step 3: Build the Project

**Using Maven Wrapper (recommended):**
```bash
# Windows
.\mvnw.cmd clean install

# Linux/Mac
./mvnw clean install
```

**Using system Maven:**
```bash
mvn clean install
```

### Step 4: Run the Application

**Using Maven Wrapper:**
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

**Using system Maven:**
```bash
mvn spring-boot:run
```

**Or run the JAR directly:**
```bash
java -jar target/claim-processing-service-1.0.0.jar
```

### Step 5: Verify It's Running

You should see output like:
```
Started ClaimProcessingServiceApplication in X.XXX seconds
```

Then test:
```bash
curl http://localhost:8080/api/claims/health
```

## Project Structure Overview

```
claim-processing-service/
├── src/
│   ├── main/
│   │   ├── java/com/libertymutual/claims/
│   │   │   ├── config/              # Async configuration
│   │   │   ├── controller/          # REST API endpoints
│   │   │   ├── dto/                 # Request/Response DTOs
│   │   │   ├── model/               # JPA entities
│   │   │   ├── repository/          # Data access layer
│   │   │   └── service/             # Business logic
│   │   └── resources/
│   │       └── application.yml      # Configuration
│   └── test/                         # Unit tests
├── .mvn/                             # Maven wrapper
├── Dockerfile                        # Container build instructions
├── docker-compose.yml                # Multi-container setup
├── pom.xml                           # Maven dependencies
└── README.md                         # Project documentation
```

## Key Components Explained

### 1. **Claim Entity** (`model/Claim.java`)
- JPA entity representing a claim in the database
- Fields: id, type, amount, timestamp, status, fraudScore, isValid
- Auto-generates timestamp on creation

### 2. **ClaimController** (`controller/ClaimController.java`)
- REST endpoints for claim operations
- Tracks response times for performance monitoring
- Validates request data

### 3. **ClaimService** (`service/ClaimService.java`)
- Business logic for claim operations
- Implements caching for summary endpoint
- Manages cache invalidation

### 4. **ClaimProcessingService** (`service/ClaimProcessingService.java`)
- Asynchronous claim processing
- Simulates fraud scoring (0.0 to 1.0)
- Updates claim status based on validation

### 5. **PerformanceMonitoringService** (`service/PerformanceMonitoringService.java`)
- Tracks response times per endpoint
- Calculates average response times
- Provides metrics endpoint

## Testing the Features

### Test Async Processing
1. Create a claim:
```bash
curl -X POST http://localhost:8080/api/claims \
  -H "Content-Type: application/json" \
  -d '{"type":"AUTO","amount":5000,"status":"PENDING"}'
```

2. Check the claim immediately - `processedAt` will be null
3. Wait 1-3 seconds
4. Query the claim again - it should have `fraudScore` and `isValid` populated

### Test Caching
1. Get summary (first call - cache miss):
```bash
curl http://localhost:8080/api/claims/summary
```

2. Get summary again (should be faster - cache hit):
```bash
curl http://localhost:8080/api/claims/summary
```

3. Create a new claim (invalidates cache):
```bash
curl -X POST http://localhost:8080/api/claims \
  -H "Content-Type: application/json" \
  -d '{"type":"HOME","amount":3000,"status":"PENDING"}'
```

4. Get summary again (cache miss, fresh data):
```bash
curl http://localhost:8080/api/claims/summary
```

### Test Performance Metrics
```bash
# Make several requests, then check metrics
curl http://localhost:8080/api/claims/metrics
```

## Troubleshooting

### Port Already in Use
If port 8080 is already in use:
```bash
# Change port in docker-compose.yml or set environment variable
export SERVER_PORT=8081
```

### Database Connection Issues
1. Verify PostgreSQL is running:
```bash
docker ps  # if using Docker
# or
pg_isready  # if installed locally
```

2. Check connection string in `application.yml` or environment variables

3. Verify database credentials

### Build Failures
1. Ensure Java 17+ is installed:
```bash
java -version
```

2. Clear Maven cache:
```bash
rm -rf ~/.m2/repository  # Linux/Mac
# or
rmdir /s %USERPROFILE%\.m2\repository  # Windows
```

3. Rebuild:
```bash
./mvnw clean install
```

### Docker Issues

#### Docker Not Found / Command Not Found

If you get "command not found" when running `docker` or `docker-compose`:

1. **Verify Docker Desktop is installed:**
   - Download from: https://www.docker.com/products/docker-desktop/
   - Install Docker Desktop for Windows
   - Make sure Docker Desktop is running (check system tray)

2. **Restart Git Bash after installing Docker:**
   - Close and reopen Git Bash to refresh PATH

3. **Verify Docker is accessible:**
   ```bash
   # Test Docker
   docker --version
   
   # Test Docker Compose (V2 - newer versions)
   docker compose version
   
   # OR test Docker Compose (V1 - older versions)
   docker-compose --version
   ```

4. **If Docker is installed but not found in Git Bash:**
   - Docker Desktop should add itself to PATH automatically
   - Try restarting your computer if PATH isn't updated
   - Or manually add Docker to PATH in Git Bash:
     ```bash
     export PATH="/c/Program Files/Docker/Docker/resources/bin:$PATH"
     ```

#### Docker Compose Command Differences

**Docker Compose V2 (newer, recommended):**
```bash
docker compose up -d
docker compose down
docker compose ps
```

**Docker Compose V1 (older):**
```bash
docker-compose up -d
docker-compose down
docker-compose ps
```

If `docker-compose` doesn't work, try `docker compose` (without hyphen).

#### Other Docker Issues

1. Ensure Docker Desktop is running:
   ```bash
   docker ps
   ```

2. Check Docker Compose version:
   ```bash
   docker compose version  # V2
   # or
   docker-compose --version  # V1
   ```

3. Rebuild containers:
   ```bash
   # V2
   docker compose down
   docker compose up -d --build
   
   # V1
   docker-compose down
   docker-compose up -d --build
   ```

## Next Steps

Once the service is running:
1. Explore the API endpoints using the examples in README.md
2. Review the code structure to understand the implementation
3. Add unit tests for your use case
4. Consider adding:
   - Swagger/OpenAPI documentation
   - More comprehensive error handling
   - Integration tests
   - CI/CD pipeline configuration

## Support

For issues or questions:
- Check the README.md for detailed API documentation
- Review application logs for error messages
- Verify all prerequisites are installed correctly
