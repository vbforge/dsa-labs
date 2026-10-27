# ==========================================
# Stage 1: Build the application
# ==========================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml first to cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Create the runtime image
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built jar and the dependencies folder from the builder stage
COPY --from=builder /app/target/dsa-visual-simple.jar .
COPY --from=builder /app/target/lib ./lib

# Expose the default port
EXPOSE 8080

# Run the server
ENTRYPOINT ["java", "-jar", "dsa-visual-simple.jar"]