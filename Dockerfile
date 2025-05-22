# Use OpenJDK 17
FROM eclipse-temurin:17-jdk-jammy

# Set working directory
WORKDIR /app

# Copy the JAR file (update 'your-spring-boot-app.jar' to match your JAR name)
COPY target/tech.jar app.jar

# Expose port 8080 (default for Spring Boot)
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
