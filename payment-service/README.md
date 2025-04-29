# Payment Service

This service handles payment processing for the e-commerce application.

## Database Configuration

The application uses PostgreSQL as its database. The database is configured using Docker Compose.

### Running with Docker Compose

To start the application and database using Docker Compose:

```bash
# Build and start the containers
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the containers
docker-compose down
```

### Database Details

- **Database Name**: payment_db
- **Username**: postgres
- **Password**: postgres
- **Port**: 5432

## Local Development

For local development without Docker:

1. Make sure you have PostgreSQL installed and running on your machine
2. Create a database named `payment_db`
3. The application is configured to connect to the database at `localhost:5432` with the credentials mentioned above
4. Run the application using Maven:

```bash
./mvnw spring-boot:run
```

## Building the Application

```bash
./mvnw clean package
```

This will create a JAR file in the `target` directory that can be used by the Docker build.