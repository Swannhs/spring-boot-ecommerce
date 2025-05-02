# Dockerfile for product-service (Local Development)
FROM openjdk:17-jdk-slim as build

WORKDIR /app

# Create .m2 directory for Maven cache
RUN mkdir -p /root/.m2/repository

# Copy Maven settings.xml for optimized dependency caching
COPY docker/local/settings.xml /root/.m2/settings.xml

# Copy the Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make the Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN ./mvnw -s /root/.m2/settings.xml dependency:go-offline -B

# Copy the source code
COPY src src

# Development-specific configuration
ENV SPRING_PROFILES_ACTIVE=dev

# Expose the port the app runs on
EXPOSE 9091

# Command to run the application with dev tools enabled
CMD ["./mvnw", "-s", "/root/.m2/settings.xml", "spring-boot:run", "-Dspring-boot.run.jvmArguments='-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005'"]
