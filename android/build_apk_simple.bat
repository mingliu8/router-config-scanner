@echo off
chcp 65001
echo ========================================
echo Building APK...
echo ========================================
echo.

echo Setting Java environment...
set JAVA_HOME=C:\Program Files\Java\jdk-25.0.2
set PATH=%PATH%;%JAVA_HOME%\bin

echo.
echo Verifying Java...
java -version
if %errorlevel% neq 0 (
    echo ERROR: Java not found
    pause
    exit /b 1
)

echo.
echo Java configured successfully!
echo.

echo ========================================
echo Starting build...
echo ========================================
echo.

call gradlew.bat assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESS!
    echo ========================================
    echo.
    echo APK location: app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo Copy APK to parent directory and run install_apk.py
) else (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    echo.
    echo Please check errors and try again
)

echo.
pause
