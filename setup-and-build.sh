#!/bin/bash

# Script to build and upload Aim Assist mod to GitHub
# Usage: ./setup-and-build.sh [github-username] [repo-name]

GITHUB_USER=${1:-"your-username"}
REPO_NAME=${2:-"aim-assist-mod"}

echo "========================================="
echo "  Aim Assist Mod - Build & Upload Script"
echo "========================================="
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed!"
    echo "Please install JDK 21: https://adoptium.net/"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
echo "Java version: $JAVA_VERSION"

if [ "$JAVA_VERSION" -lt 21 ] 2>/dev/null; then
    echo "WARNING: Java 21+ is recommended. Current version: $JAVA_VERSION"
fi

# Check if git is installed
if ! command -v git &> /dev/null; then
    echo "ERROR: Git is not installed!"
    echo "Please install Git: https://git-scm.com/"
    exit 1
fi

echo ""
echo "Step 1: Building the mod..."
echo "-----------------------------------------"

# Make gradlew executable
chmod +x gradlew

# Build the mod
./gradlew build --no-daemon

if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Build failed!"
    exit 1
fi

echo ""
echo "Build successful!"
echo "JAR file: build/libs/aim-assist-1.0.0.jar"
echo ""

# Ask about GitHub upload
echo "========================================="
echo "  Upload to GitHub"
echo "========================================="
echo ""
read -p "Do you want to upload to GitHub? (y/n): " upload_choice

if [ "$upload_choice" = "y" ] || [ "$upload_choice" = "Y" ]; then
    echo ""
    read -p "GitHub username [$GITHUB_USER]: " input_user
    GITHUB_USER=${input_user:-$GITHUB_USER}
    
    read -p "Repository name [$REPO_NAME]: " input_repo
    REPO_NAME=${input_repo:-$REPO_NAME}
    
    echo ""
    echo "Step 2: Initializing Git repository..."
    echo "-----------------------------------------"
    
    git init
    git add .
    git commit -m "Initial commit - Aim Assist Mod for Minecraft 1.21.4 Fabric"
    git branch -M main
    
    echo ""
    echo "Step 3: Creating GitHub repository..."
    echo "-----------------------------------------"
    echo "Please create a repository at: https://github.com/new"
    echo "Repository name: $REPO_NAME"
    echo ""
    read -p "Press Enter after creating the repository..."
    
    git remote add origin "https://github.com/${GITHUB_USER}/${REPO_NAME}.git"
    git push -u origin main
    
    echo ""
    echo "========================================="
    echo "  Done!"
    echo "========================================="
    echo ""
    echo "Repository: https://github.com/${GITHUB_USER}/${REPO_NAME}"
    echo "JAR file: build/libs/aim-assist-1.0.0.jar"
    echo ""
    echo "The GitHub Actions workflow will automatically build the mod."
    echo "You can download the built JAR from the Actions tab."
else
    echo ""
    echo "========================================="
    echo "  Build Complete!"
    echo "========================================="
    echo ""
    echo "JAR file location: build/libs/aim-assist-1.0.0.jar"
    echo ""
    echo "To upload to GitHub manually:"
    echo "  1. Create a new repository on GitHub"
    echo "  2. Run: git init && git add . && git commit -m 'Initial commit'"
    echo "  3. Run: git remote add origin https://github.com/YOUR_USER/YOUR_REPO.git"
    echo "  4. Run: git push -u origin main"
fi
