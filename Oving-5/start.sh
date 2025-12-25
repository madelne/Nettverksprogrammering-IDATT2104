#!/bin/bash

echo "Building Docker image..."
docker build -t code-runner .

echo "Running Docker container..."
docker run -p 5000:5000 code-runner