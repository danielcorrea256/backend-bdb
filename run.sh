#!/bin/bash

# Load environment variables from .env file
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
    echo "✓ Environment variables loaded from .env"
else
    echo "⚠ Warning: .env file not found"
fi

# Run the Spring Boot application
echo "🚀 Starting Spring Boot application..."
./mvnw spring-boot:run
