#!/bin/bash

# Build the project
./gradlew spotlessApply
./gradlew clean build

# Check if the build succeeded
if [ $? -eq 0 ]; then
    # If the build succeeded, copy the jar file
    scp "build/libs/dpvn-storage-service-0.0.1-SNAPSHOT.jar" root@157.10.198.80:/apps/storage
    echo "Build and Copy progress completed."
else
    # If the build failed, print an error message and exit
    echo "Build failed. Stopping script execution."
    exit 1
fi
