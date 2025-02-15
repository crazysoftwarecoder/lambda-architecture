#!/bin/bash

# Build the project, skipping tests
echo "Building project..."
mvn clean package -DskipTests

# Check if the build was successful
if [ $? -ne 0 ]; then
    echo "Build failed"
    exit 1
fi

# Get the generated jar file (assuming it's in target directory)
JAR_FILE="target/lambda-architecture-1.0-SNAPSHOT.jar"

# Check if jar file exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Jar file not found: $JAR_FILE"
    exit 1
fi

# Execute the jar with the required JVM parameters
echo "Running application..."
java \
    --add-exports=java.base/sun.nio.ch=ALL-UNNAMED \
    --add-opens=java.base/java.nio=ALL-UNNAMED \
    --add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
    -jar "$JAR_FILE" "$@"