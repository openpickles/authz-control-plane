#!/bin/bash

# Function to stop a process by PID file
stop_process() {
    local SERVICE_NAME=$1
    local PID_FILE=$2

    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null; then
            echo "Stopping $SERVICE_NAME (PID $PID)..."
            kill "$PID"
            
            # Wait for process to exit
            sleep 2
            if ps -p "$PID" > /dev/null; then
               echo "Force killing $SERVICE_NAME..."
               kill -9 "$PID"
            fi
            
            echo "$SERVICE_NAME stopped."
            rm "$PID_FILE"
        else
            echo "Process $PID for $SERVICE_NAME not found. Cleaning up stale PID file."
            rm "$PID_FILE"
        fi
    else
        echo "No $PID_FILE found for $SERVICE_NAME. Is it running?"
    fi
}

# Stop Backend
stop_process "Backend" "backend.pid"

# Stop Reference App
stop_process "Reference App" "reference-app.pid"

echo "Development environment stopped."
