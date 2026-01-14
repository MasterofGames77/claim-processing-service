# Quick Start Guide

## 🚀 Get Running in 2 Minutes

### Using Docker (Easiest)
```bash
docker-compose up -d
```

Wait 30 seconds, then test:
```bash
curl http://localhost:8080/api/claims/health
```

### Using Local Setup
```bash
# 1. Start PostgreSQL (Docker)
docker run -d --name postgres-claims \
  -e POSTGRES_DB=claimsdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15-alpine

# 2. Build and run
./mvnw spring-boot:run
```

## 📋 API Endpoints

### Create Claim
```bash
curl -X POST http://localhost:8080/api/claims \
  -H "Content-Type: application/json" \
  -d '{"type":"AUTO","amount":5000,"status":"PENDING"}'
```

### Get Summary
```bash
curl http://localhost:8080/api/claims/summary
```

### Get Metrics
```bash
curl http://localhost:8080/api/claims/metrics
```

### Health Check
```bash
curl http://localhost:8080/api/claims/health
```

## 🎯 Key Features Demonstrated

✅ REST API with Spring Boot  
✅ PostgreSQL database with JPA  
✅ Asynchronous processing  
✅ In-memory caching  
✅ Performance monitoring  
✅ Docker containerization  
✅ Environment-based configuration  

## 📚 Full Documentation

- **README.md** - Complete project documentation
- **SETUP_GUIDE.md** - Detailed setup instructions

## 🛑 Stop Services

```bash
# Docker Compose
docker-compose down

# Local PostgreSQL
docker stop postgres-claims
docker rm postgres-claims
```
