# Dockerfile for payment-service (Deployment)
FROM openjdk:17-jdk-slim as build

WORKDIR /app

# Create .m2 directory for Maven cache
RUN mkdir -p /root/.m2/repository

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

# Build the application
RUN ./mvnw package -DskipTests

# Second stage: minimal runtime
FROM openjdk:17-jre-slim

WORKDIR /app

# Copy the built artifact from the build stage
COPY --from=build /app/target/*.jar app.jar

# Production-specific configuration
ENV SPRING_PROFILES_ACTIVE=prod

# Expose the port the app runs on
EXPOSE 9093

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
