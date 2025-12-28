# Runtime Stage
FROM docker.io/library/eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the pre-built JAR file
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Install OPA
ADD https://openpolicyagent.org/downloads/v0.60.0/opa_linux_amd64_static /app/opa
RUN chmod +x /app/opa

# Set ownership to non-root user
RUN chown -R spring:spring /app

USER spring:spring

# Expose port (Cloud Run defaults to 8080)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
