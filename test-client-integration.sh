#!/bin/bash

# Configuration
BACKEND_URL="http://localhost:8080"
DRIVER_URL="http://localhost:9090"
DRIVER_JAR="policy-engine-reference-app/target/policy-engine-reference-app-0.0.1-SNAPSHOT.jar"

echo "Starting Integration Test..."

# 1. Check Backend
if ! curl -s "$BACKEND_URL/actuator/health" > /dev/null; then
    echo "❌ Backend is not running on $BACKEND_URL. Please start it first."
    exit 1
fi
echo "✅ Backend is UP"

# 2. Build Driver (if needed)
if [ ! -f "$DRIVER_JAR" ]; then
    echo "Building Driver..."
    cd policy-engine-reference-app && mvn clean package -DskipTests -q && cd ..
fi

# 3. Start Driver
echo "Starting Driver App..."
java -jar $DRIVER_JAR > driver.log 2>&1 &
DRIVER_PID=$!
echo "Driver PID: $DRIVER_PID"

# Wait for Driver
echo "Waiting for Driver to start..."
for i in {1..30}; do
    if curl -s "$DRIVER_URL/verification/status" > /dev/null; then
        echo "✅ Driver is UP"
        break
    fi
    sleep 1
done

# 4. Trigger Bundle Build (Simulate Backend Action)
# First creates a policy so the bundle isn't empty (optional, but good practice)
echo "Creating Test Policy..."
curl -X POST "$BACKEND_URL/api/v1/policies" \
     -H "Content-Type: application/json" \
     -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
     -d '{"name": "integration-test-policy", "content": "package test\nallow = true", "type": "OPA", "sourceType": "MANUAL"}' \
     -s > /dev/null

echo "Triggering Bundle Build..."
# Create Bundle definition
curl -X POST "$BACKEND_URL/api/v1/bundles" \
     -H "Content-Type: application/json" \
     -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
     -d '{"name": "reference-app-bundle", "bindingIds": [], "wasmEnabled": false, "description": "Integration Test Bundle"}' \
     -s > /dev/null

# Build it (This should trigger the WebSocket notification)
curl -X POST "$BACKEND_URL/api/v1/bundles/reference-app-bundle/build" \
     -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
     -s > /dev/null

echo "Build Triggered. Waiting for Client to receive update..."

# 5. Verify Receipt
SUCCESS=false
for i in {1..30}; do
    STATUS=$(curl -s "$DRIVER_URL/verification/status")
    SIZE=$(echo $STATUS | grep -o '"receivedContentSize":[0-9]*' | awk -F: '{print $2}')
    
    if [ "$SIZE" -gt "0" ]; then
        echo "✅ SUCCESS! Client received bundle update. Content Size: $SIZE bytes."
        SUCCESS=true
        break
    fi
    echo "Waiting... ($i/30)"
    sleep 1
done

# Cleanup
kill $DRIVER_PID

if [ "$SUCCESS" = true ]; then
    exit 0
else
    echo "❌ FAILED: Client did not receive bundle update time."
    exit 1
fi
