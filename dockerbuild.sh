#!/bin/bash

IMAGE_NAME="docker.io/muragesh/openpickles-authz-policy:0.0.5"
# Define temporary directory
TMP_DIR="disk_tmp"

# Clean up previous build execution
rm -rf "$TMP_DIR"
mkdir -p "$TMP_DIR"

echo "Staging files in $TMP_DIR..."

# Copy Dockerfile
cp backend/Dockerfile "$TMP_DIR/Dockerfile"

# Copy startup script
cp backend/start_both.sh "$TMP_DIR/start_both.sh"

# Copy Backend JAR
# Assuming only one jar exists in target, or pick the specific one
cp backend/target/*.jar "$TMP_DIR/app.jar"

# Copy Reference App JAR
cp policy-engine-reference-app/target/*.jar "$TMP_DIR/reference-app.jar"

echo "Building Docker image..."
cd "$TMP_DIR" || exit
#docker build -t policy-engine:latest .
container build -a amd64 -t $IMAGE_NAME . 


# Optional: Clean up
# cd ..
# rm -rf "$TMP_DIR"

echo "Build complete. Image 'policy-engine:latest' created."
