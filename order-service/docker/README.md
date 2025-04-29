# Docker Configuration for Order Service

This directory contains Docker configurations for the Order Service.

## Directory Structure

- `local/`: Contains Dockerfile for local development
- `deployment/`: Contains Dockerfile for production deployment
- `docker-compose.yml`: Docker Compose file for running the service with its dependencies

## Local Development

To run the Order Service for local development:

```bash
cd order-service/docker
docker-compose up
```

This will:
- Build the Docker image using the local Dockerfile
- Start the Order Service on port 8080 (mapped to 8080 on the host)
- Enable remote debugging on port 5005
- Start a PostgreSQL database on port 5432
- Mount the source code directory for hot reloading

## Deployment

To build a production-ready Docker image:

```bash
cd order-service
docker build -f docker/deployment/Dockerfile -t order-service:latest .
```

## Environment Variables

The following environment variables can be configured:

- `SPRING_DATASOURCE_URL`: JDBC URL for the database
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `SPRING_JPA_HIBERNATE_DDL_AUTO`: Hibernate DDL auto (create, update, validate, etc.)
- `SPRING_PROFILES_ACTIVE`: Spring profile to activate (dev, prod, etc.)

## Running with All Services

To run all services together, use the root docker-compose.yml file:

```bash
# From the project root
docker-compose up
```

This will start all services (order, payment, product) and their databases.