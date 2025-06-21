# Stage 1: Build the application with Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the entire project
COPY . .

# Build the project, this will also build the frontend
RUN mvn clean package -DskipTests

# Stage 2: Create the final image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 