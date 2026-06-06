# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# Stage 2: Create the production image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built jar from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Auth service port (from your application.yml)
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]