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

REM Use absolute path to gradlew.bat
%~dp0gradlew.bat assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESS!
    echo ========================================
    echo.
    echo APK location: %~dp0app\build\outputs\apk\debug\app-debug.apk
    echo.
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