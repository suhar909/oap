#!/bin/bash
# Quick setup script - generates Gradle wrapper and builds the mod
# Run this if you have Gradle installed globally

echo "=== Aim Assist Mod - Quick Setup ==="
echo ""

# Check if gradle is installed
if command -v gradle &> /dev/null; then
    echo "Gradle found. Generating wrapper..."
    gradle wrapper --gradle-version=8.12
    echo "Building mod..."
    ./gradlew build
elif command -v java &> /dev/null; then
    echo "Gradle not found. Downloading wrapper..."
    
    # Download gradle wrapper jar
    mkdir -p gradle/wrapper
    
    # Use curl to download gradle wrapper
    if command -v curl &> /dev/null; then
        curl -L -o gradle/wrapper/gradle-wrapper.jar \
            "https://github.com/gradle/gradle/raw/v8.12.0/gradle/wrapper/gradle-wrapper.jar" 2>/dev/null
    elif command -v wget &> /dev/null; then
        wget -q -O gradle/wrapper/gradle-wrapper.jar \
            "https://github.com/gradle/gradle/raw/v8.12.0/gradle/wrapper/gradle-wrapper.jar"
    fi
    
    if [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
        chmod +x gradlew
        echo "Building mod..."
        ./gradlew build
    else
        echo ""
        echo "ERROR: Could not download Gradle wrapper."
        echo "Please install Gradle manually:"
        echo "  1. Download from https://gradle.org/install/"
        echo "  2. Run: gradle wrapper --gradle-version=8.12"
        echo "  3. Run: ./gradlew build"
        exit 1
    fi
else
    echo "ERROR: Neither Java nor Gradle found!"
    echo "Please install JDK 21 from https://adoptium.net/"
    exit 1
fi

if [ -f "build/libs/aim-assist-1.0.0.jar" ]; then
    echo ""
    echo "========================================="
    echo "  ✅ BUILD SUCCESSFUL!"
    echo "========================================="
    echo ""
    echo "JAR file: build/libs/aim-assist-1.0.0.jar"
    echo ""
    echo "To install:"
    echo "  1. Copy the JAR to your Minecraft 'mods' folder"
    echo "  2. Make sure Fabric API is also installed"
    echo "  3. Launch Minecraft with Fabric"
    echo ""
    echo "To upload to GitHub:"
    echo "  git init && git add . && git commit -m 'Aim Assist Mod'"
    echo "  git remote add origin https://github.com/YOUR_USER/aim-assist-mod.git"
    echo "  git push -u origin main"
else
    echo ""
    echo "ERROR: Build failed. Check the output above for errors."
    exit 1
fi
