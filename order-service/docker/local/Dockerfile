# Dockerfile for order-service (Local Development)
FROM openjdk:17-jdk-slim as build

WORKDIR /app

# Copy the Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make the Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY src src

# Development-specific configuration
ENV SPRING_PROFILES_ACTIVE=dev

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application with dev tools enabled
CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.jvmArguments='-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005'"]