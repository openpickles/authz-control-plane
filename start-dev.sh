#!/bin/bash

# Root directory
ROOT_DIR=$(pwd)

echo "Setting up development environment..."

# 1. Start Backend
echo "Starting Backend..."
cd "$ROOT_DIR/backend" || exit
nohup mvn clean package spring-boot:run -Dspring-boot.run.profiles=dev -DskipTests > backend.log 2>&1 &
BACKEND_PID=$!
echo $BACKEND_PID > "$ROOT_DIR/backend.pid"
echo "Backend started with PID $BACKEND_PID. Logs: backend/backend.log"

# 2. Build Client
echo "Building Client..."
cd "$ROOT_DIR/policy-engine-client" || exit
mvn clean install > client-build.log 2>&1
if [ $? -eq 0 ]; then
    echo "Client built successfully."
else
    echo "Client build failed. Check policy-engine-client/client-build.log"
    exit 1
fi

# 2.5 Wait for Backend to be ready
echo "Waiting for Backend to start on port 8080..."
max_retries=30
count=0
while ! nc -z localhost 8080; do   
  sleep 2
  count=$((count+1))
  if [ $count -ge $max_retries ]; then
    echo "Backend failed to start within 60 seconds."
    exit 1
  fi
done
echo "Backend is up!"

# 3. Start Reference App
echo "Starting Reference App..."
cd "$ROOT_DIR/policy-engine-reference-app" || exit
nohup mvn clean package spring-boot:run -Dspring-boot.run.profiles=dev > reference-app.log 2>&1 &
REF_APP_PID=$!
echo $REF_APP_PID > "$ROOT_DIR/reference-app.pid"
echo "Reference App started with PID $REF_APP_PID. Logs: policy-engine-reference-app/reference-app.log"

echo "Development environment started."
