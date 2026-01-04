#!/bin/bash

PID_FILE="backend.pid"

if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null; then
        echo "Stopping Policy Engine Backend (PID $PID)..."
        kill "$PID"
        
        # Wait for process to exit
        sleep 2
        if ps -p "$PID" > /dev/null; then
           echo "Force killing..."
           kill -9 "$PID"
        fi
        
        echo "Backend stopped."
        rm "$PID_FILE"
    else
        echo "Process $PID not found. Cleaning up stale PID file."
        rm "$PID_FILE"
    fi
else
    echo "No $PID_FILE found. Is the server running?"
fi
