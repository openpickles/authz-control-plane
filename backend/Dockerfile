# Runtime Stage
FROM docker.io/library/eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the pre-built JAR file
# Copy the pre-built JAR files from the staged build context
COPY app.jar app.jar
COPY reference-app.jar reference-app.jar

# Copy and setup startup script
COPY start_both.sh start_both.sh
RUN chmod +x start_both.sh

# Install OPA
ADD https://openpolicyagent.org/downloads/v0.60.0/opa_linux_amd64_static /app/opa
RUN chmod +x /app/opa

# Set ownership to non-root user
RUN chown -R spring:spring /app

USER spring:spring

ENV SPRING_PROFILES_ACTIVE=dev

# Expose ports
# 8080: Control Plane
# 9090: Reference App
EXPOSE 8080 9090

ENTRYPOINT ["./start_both.sh"]
