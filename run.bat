@echo off
echo ============================================
echo  Smart Parking Lot Management System (SPLMS)
echo  Compile and Run Script
echo ============================================
echo.

REM Check if Java is installed
where javac >nul 2>nul
if errorlevel 1 (
    echo ERROR: Java ^(javac^) not found in PATH.
    echo Please install JDK 17+ from https://adoptium.net/
    echo and add it to your PATH environment variable.
    echo.
    pause
    exit /b 1
)

echo Java found. Compiling...
echo.

REM Create output directory
if not exist out (
    mkdir out
)

REM Create data directory
if not exist data (
    mkdir data
)

REM Compile all sources
javac -d out -sourcepath src src\main\Main.java

if errorlevel 1 (
    echo.
    echo COMPILATION FAILED. Check errors above.
    pause
    exit /b 1
)

echo.
echo Compilation successful!
echo.
echo Starting SPLMS...
echo.

REM Run the application
java -cp out main.Main

pause
