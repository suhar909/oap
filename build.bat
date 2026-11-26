@echo off
REM Script to build Aim Assist mod on Windows

echo =========================================
echo   Aim Assist Mod - Build Script (Windows)
echo =========================================
echo.

REM Check Java
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not installed!
    echo Please install JDK 21: https://adoptium.net/
    pause
    exit /b 1
)

echo Step 1: Building the mod...
echo -----------------------------------------

gradlew.bat build --no-daemon

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo Build successful!
echo JAR file: build\libs\aim-assist-1.0.0.jar
echo.
echo =========================================
echo   Build Complete!
echo =========================================
echo.
echo To upload to GitHub:
echo   1. Create a new repository on GitHub
echo   2. Run: git init
echo   3. Run: git add .
echo   4. Run: git commit -m "Initial commit"
echo   5. Run: git remote add origin https://github.com/YOUR_USER/YOUR_REPO.git
echo   6. Run: git push -u origin main
echo.

pause
