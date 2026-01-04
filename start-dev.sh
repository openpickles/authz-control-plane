#!/bin/bash

# Navigate to backend directory
cd backend || exit

# Set default development credentials
# Defaults are now handled in application.yml, but we can still override them here if needed.
# export ADMIN_USERNAME=admin
# export ADMIN_PASSWORD=admin123

echo "Starting Policy Engine Backend (Dev Mode)..."
echo "Credentials: $ADMIN_USERNAME / $ADMIN_PASSWORD"

# Run Maven in background with nohup
nohup mvn clean package spring-boot:run > backend.log 2>&1 &
PID=$!

# Save PID to file in root directory (since we cd'd into backend, root is ../)
echo $PID > ../backend.pid

echo "Backend started with PID $PID"
echo "Logs available in backend/backend.log"
