# Runtime Stage
FROM docker.io/library/eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the pre-built JAR file
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Expose port (Cloud Run defaults to 8080)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
