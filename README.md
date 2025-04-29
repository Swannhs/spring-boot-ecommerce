# Spring Boot E-commerce Microservices

This project consists of three microservices for an e-commerce application:
- Order Service
- Payment Service
- Product Service

## Docker Configuration

Each service has its own Docker configuration in its respective `docker` directory:
- `order-service/docker/`
- `payment-service/docker/`
- `product-service/docker/`

### Individual Service Development

Each service can be run independently for development:

```bash
cd <service-name>/docker
docker-compose up
```

For example, to run just the Order Service:
```bash
cd order-service/docker
docker-compose up
```

### Running All Services Together

To run all services together:

```bash
# From the project root
docker-compose up
```

This will start:
- Order Service on port 8081
- Payment Service on port 8082
- Product Service on port 8083
- PostgreSQL databases for each service

## Service Ports

When running all services together:

| Service | Application Port | Debug Port | Database Port |
|---------|------------------|------------|--------------|
| Order Service | 8081 | 5005 | 5432 |
| Payment Service | 8082 | 5006 | 5433 |
| Product Service | 8083 | 5007 | 5434 |

## Docker Configurations

Each service has two Dockerfiles:
- `local/Dockerfile`: Optimized for local development with hot reloading and debugging
- `deployment/Dockerfile`: Optimized for production deployment with a smaller footprint

## Building for Production

To build a production-ready Docker image for a service:

```bash
cd <service-name>
docker build -f docker/deployment/Dockerfile -t <service-name>:latest .
```

For example, to build the Order Service:
```bash
cd order-service
docker build -f docker/deployment/Dockerfile -t order-service:latest .
```