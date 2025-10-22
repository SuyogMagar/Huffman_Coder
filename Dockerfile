# Stage 1: Build backend and frontend
FROM maven:3.9-eclipse-temurin-21 AS build

# Install Node.js for frontend build
RUN apt-get update && \
    apt-get install -y curl gnupg && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs

WORKDIR /app

# Copy entire project
COPY . .

# Build frontend
WORKDIR /app/frontend
RUN npm install && npm run build

# Copy frontend build output into Spring Boot resources
WORKDIR /app
RUN mkdir -p src/main/resources/static && \
    rm -rf src/main/resources/static/* && \
    cp -r frontend/build/* src/main/resources/static/

# Build backend (Spring Boot jar)
RUN mvn clean package -DskipTests

# Stage 2: Final runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built jar
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Start the app
ENTRYPOINT ["java", "-jar", "app.jar"]
