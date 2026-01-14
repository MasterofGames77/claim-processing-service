# Claim Processing Service

A backend service for processing insurance claim events, built with Spring Boot, PostgreSQL, and Docker. This service demonstrates REST API design, asynchronous processing, caching, and performance monitoring.

## Features

- **REST API Endpoints**
  - `POST /api/claims` - Create and submit new claim events
  - `GET /api/claims/summary` - Retrieve aggregated claim statistics
  - `GET /api/claims/health` - Health check endpoint
  - `GET /api/claims/metrics` - Performance metrics

- **Asynchronous Processing**
  - Claims are processed asynchronously after creation
  - Simulates fraud scoring and validation
  - Updates claim status based on processing results

- **Caching**
  - In-memory caching for summary endpoint
  - Cache invalidation on new claim creation
  - Cache hit/miss metrics

- **Performance Monitoring**
  - Response time tracking for all endpoints
  - Average response time calculations
  - Per-endpoint performance metrics

- **Database**
  - PostgreSQL for persistent storage
  - JPA/Hibernate for ORM
  - Automatic schema management

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **PostgreSQL 15**
- **Docker & Docker Compose**
- **Maven**

## Prerequisites

- Java 17 or higher
- Maven 3.6+ (or use included Maven wrapper)
- Docker and Docker Compose (for containerized setup)
- PostgreSQL (if running without Docker)

## Quick Start with Docker

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd claim-processing-service
   ```

2. **Start the services**
   ```bash
   docker-compose up -d
   ```

   This will start:
   - PostgreSQL database on port 5432
   - Claim Processing Service on port 8080

3. **Verify the service is running**
   ```bash
   curl http://localhost:8080/api/claims/health
   ```

## Local Development Setup

1. **Start PostgreSQL database**
   ```bash
   # Using Docker
   docker run -d \
     --name postgres-claims \
     -e POSTGRES_DB=claimsdb \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 \
     postgres:15-alpine
   ```

2. **Configure environment variables** (optional, defaults provided)
   ```bash
   export DATABASE_URL=jdbc:postgresql://localhost:5432/claimsdb
   export DATABASE_USERNAME=postgres
   export DATABASE_PASSWORD=postgres
   export SERVER_PORT=8080
   ```

3. **Build and run the application**
   ```bash
   # Using Maven wrapper
   ./mvnw clean install
   ./mvnw spring-boot:run
   
   # Or using system Maven
   mvn clean install
   mvn spring-boot:run
   ```

## API Usage Examples

### Create a Claim

```bash
curl -X POST http://localhost:8080/api/claims \
  -H "Content-Type: application/json" \
  -d '{
    "type": "AUTO",
    "amount": 5000.00,
    "status": "PENDING",
    "timestamp": "2024-01-15T10:30:00"
  }'
```

**Response:**
```json
{
  "id": 1,
  "type": "AUTO",
  "amount": 5000.00,
  "timestamp": "2024-01-15T10:30:00",
  "status": "PENDING",
  "processedAt": null,
  "fraudScore": null,
  "isValid": null
}
```

### Get Claim Summary

```bash
curl http://localhost:8080/api/claims/summary
```

**Response:**
```json
{
  "totalClaims": 10,
  "claimsByStatus": {
    "PENDING": 3,
    "APPROVED": 5,
    "REJECTED": 2
  },
  "totalAmount": 45000.00,
  "amountByStatus": {
    "PENDING": 15000.00,
    "APPROVED": 25000.00,
    "REJECTED": 5000.00
  },
  "cacheHitCount": 5,
  "cacheMissCount": 2
}
```

### Get Performance Metrics

```bash
curl http://localhost:8080/api/claims/metrics
```

**Response:**
```json
{
  "averageResponseTime": 45.5,
  "totalRequests": 25,
  "endpointAverages": {
    "POST /api/claims": 52.3,
    "GET /api/claims/summary": 12.1
  }
}
```

### Health Check

```bash
curl http://localhost:8080/api/claims/health
```

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2024-01-15T10:30:00"
}
```

## Project Structure

```
claim-processing-service/
├── src/
│   ├── main/
│   │   ├── java/com/libertymutual/claims/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data transfer objects
│   │   │   ├── model/           # Entity models
│   │   │   ├── repository/      # Data access layer
│   │   │   └── service/         # Business logic
│   │   └── resources/
│   │       └── application.yml  # Application configuration
│   └── test/                    # Test files
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Configuration

The application uses environment variables for configuration (AWS-style). Key variables:

- `DATABASE_URL` - PostgreSQL connection URL (default: `jdbc:postgresql://localhost:5432/claimsdb`)
- `DATABASE_USERNAME` - Database username (default: `postgres`)
- `DATABASE_PASSWORD` - Database password (default: `postgres`)
- `SERVER_PORT` - Application port (default: `8080`)
- `DDL_AUTO` - Hibernate DDL mode (default: `update`)
- `LOG_LEVEL` - Logging level (default: `INFO`)

## Asynchronous Processing

When a claim is created, it's processed asynchronously:

1. **Fraud Scoring**: Generates a fraud score (0.0 to 1.0)
2. **Validation**: Determines if claim is valid (fraud score < 0.7)
3. **Status Update**: Updates claim status based on results:
   - `REJECTED` if invalid
   - `REVIEW` if fraud score > 0.5
   - Original status if valid and low fraud score

Processing typically takes 1-3 seconds to simulate real-world processing time.

## Caching Strategy

- Summary endpoint uses Spring's `@Cacheable` annotation
- Cache is evicted when new claims are created
- Cache metrics track hit/miss ratios
- Simple in-memory cache (can be upgraded to Redis for production)

## Performance Monitoring

- All endpoints track response times
- Metrics include:
  - Overall average response time
  - Total request count
  - Per-endpoint averages
- Metrics are stored in-memory (can be exported to Prometheus/Grafana)

## Testing

```bash
# Run tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

## Building for Production

```bash
# Build JAR
./mvnw clean package -DskipTests

# Run JAR
java -jar target/claim-processing-service-1.0.0.jar
```

## Docker Commands

```bash
# Build and start
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Rebuild after code changes
docker-compose up -d --build
```

## Future Enhancements

- [ ] Add unit and integration tests
- [ ] Implement Redis for distributed caching
- [ ] Add Prometheus metrics export
- [ ] Implement pagination for claims listing
- [ ] Add authentication/authorization
- [ ] Add API documentation with Swagger/OpenAPI
- [ ] Implement rate limiting
- [ ] Add database migrations with Flyway
- [ ] Implement event-driven architecture with Kafka

## License

This project is created for demonstration purposes.

## Author

Built for Liberty Mutual Insurance Associate Software Engineer position application.
