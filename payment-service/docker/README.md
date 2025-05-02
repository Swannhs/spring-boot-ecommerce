# Docker Configuration for Payment Service

This directory contains Docker configurations for the Payment Service.

## Directory Structure

- `local/`: Contains Dockerfile for local development
- `deployment/`: Contains Dockerfile for production deployment
- `docker-compose.yml`: Docker Compose file for running the service with its dependencies

## Local Development

To run the Payment Service for local development:

```bash
cd payment-service/docker
docker-compose up
```

This will:
- Build the Docker image using the local Dockerfile
- Start the Payment Service on port 9093 (mapped to 9093 on the host)
- Enable remote debugging on port 5005
- Start a PostgreSQL database on port 5432 (mapped to 5433 on the host)
- Mount the source code directory for hot reloading
- Use optimized Maven dependency caching

## Deployment

To build a production-ready Docker image:

```bash
cd payment-service
docker build -f docker/deployment/Dockerfile -t payment-service:latest .
```

## Maven Dependency Caching

The local development setup includes optimized Maven dependency caching:

- A custom Maven `settings.xml` file is provided in the `local/` directory
- The Docker Compose configuration mounts a volume for the Maven repository
- Dependencies are downloaded only once and cached for subsequent builds
- The cache persists between container restarts, significantly reducing build times

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
