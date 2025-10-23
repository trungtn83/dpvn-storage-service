#!/bin/bash

SERVICE_DIR="dpvn-storage-service"

# Function to start each script
start_scripts() {
  echo "Starting spring boot service in $SERVICE_DIR"
  ./gradlew bootrun
  echo "Service is started $SERVICE_DIR"
}

# Function to stop all running scripts
stop_scripts() {
  pids=$(pgrep -f "/dpvn-report/$SERVICE_DIR")
  if [ -n "$pids" ]; then
    echo "Killing process(es) for $SERVICE_DIR: $pids"
    kill -9 $pids
    echo "Service is stopped."
  else
    echo "No running process found for $SERVICE_DIR"
  fi
}

# Check for the stop argument
if [ "$1" == "stop" ]; then
  stop_scripts
else
  start_scripts
fi