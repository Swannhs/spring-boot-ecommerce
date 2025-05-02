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
- Kafka on ports 9092 (internal) and 29092 (external)
- ZooKeeper on port 2181

## Service Ports

When running all services together:

| Service | Application Port | Debug Port | Database Port |
|---------|------------------|------------|--------------|
| Order Service | 8081 | 5005 | 5432 |
| Payment Service | 8082 | 5006 | 5433 |
| Product Service | 8083 | 5007 | 5434 |
| Kafka | 9092 (internal), 29092 (external) | - | - |
| ZooKeeper | 2181 | - | - |

## Kafka Configuration

The application uses Kafka for asynchronous communication between microservices:

### Kafka Topics

- `order-created`: Published by Order Service when a new order is created, consumed by Payment Service
- `payment-processed`: Published by Payment Service after processing a payment, can be consumed by other services

### Microservice Communication Flow

1. Order Service creates an order and publishes an `OrderCreatedEvent` to the `order-created` topic
2. Payment Service consumes the `OrderCreatedEvent`, processes the payment, and publishes a `PaymentProcessedEvent` to the `payment-processed` topic
3. Other services can consume the `PaymentProcessedEvent` to update their state accordingly

### Accessing Kafka

- From within Docker containers: `kafka:9092`
- From the host machine: `localhost:29092`

## Docker Configurations

Each service has two Dockerfiles:
- `local/Dockerfile`: Optimized for local development with hot reloading and debugging
- `deployment/Dockerfile`: Optimized for production deployment with a smaller footprint

### Maven Dependency Caching

The Docker configuration includes optimized Maven dependency caching to speed up builds and reduce network usage:

- A shared Maven repository volume (`maven-repo`) is used across all services
- Each service has a custom Maven `settings.xml` file that configures the repository location
- Dependencies are downloaded only once and shared between services
- The cache persists between container restarts, significantly reducing build times

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
