#!/bin/bash

# Allowed environments
VALID_ENVS=("local" "test" "command" "production")

# Function to check if a value is in an array
function contains() {
  local value=$1
  shift
  for element; do
    if [[ $element == $value ]]; then
      return 0
    fi
  done
  return 1
}

# Kill process using port 8251 if exists
function free_port_8251() {
  local pid
  pid=$(lsof -t -i:8251)
  if [ -n "$pid" ]; then
    echo "Port 8251 is in use by PID $pid. Killing..."
    kill -9 "$pid"
  else
    echo "Port 8251 is free."
  fi
}

# Get the environment from the first argument, if provided
ENVIRONMENT=$1

# Free port before running
free_port_8251

# Check if the environment is provided and valid
if [ -n "$ENVIRONMENT" ]; then
  if contains "$ENVIRONMENT" "${VALID_ENVS[@]}"; then
    # Run with the specified environment profile if valid
    ./gradlew bootrun -Dspring.profiles.active=$ENVIRONMENT
  else
    echo "Invalid environment: $ENVIRONMENT"
    echo "Allowed values are: ${VALID_ENVS[*]}"
    exit 1
  fi
else
  # Run without the environment profile if no argument is passed
  ./gradlew bootrun
fi
