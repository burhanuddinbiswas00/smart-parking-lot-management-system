#!/bin/bash
echo "============================================"
echo " Smart Parking Lot Management System (SPLMS)"
echo " Compile and Run Script (Linux/macOS)"
echo "============================================"

# Check if Java is installed
if ! command -v javac &> /dev/null; then
    echo "ERROR: Java (javac) not found in PATH."
    echo "Please install JDK 17+ from https://adoptium.net/"
    exit 1
fi

echo "Java found. Compiling..."

# Create directories
mkdir -p out data

# Compile
javac -d out -sourcepath src src/main/Main.java

if [ $? -ne 0 ]; then
    echo ""
    echo "COMPILATION FAILED. Check errors above."
    exit 1
fi

echo ""
echo "Compilation successful!"
echo ""
echo "Starting SPLMS..."
echo ""

# Run
java -cp out main.Main
