#!/bin/sh

# Start the Reference App in the background
echo "Starting Reference App..."
java -jar reference-app.jar &

# Wait for a moment to let it initialize (optional, but good practice)
sleep 5

# Start the Control Plane (Backend) in the foreground
echo "Starting Control Plane..."
exec java -jar app.jar
