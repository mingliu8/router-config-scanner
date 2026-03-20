@echo off
echo Building APK...
echo.

set JAVA_HOME=C:\Program Files\Java\jdk-25.0.2
set PATH=%PATH%;%JAVA_HOME%\bin

echo Java version:
java -version
if %errorlevel% neq 0 (
    echo Java not found!
    pause
    exit /b 1
)

echo.
echo Starting build...
echo.

call gradlew.bat assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo BUILD SUCCESS!
    echo.
    echo APK location: app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo Copy APK to parent directory and run install_apk.py
) else (
    echo.
    echo BUILD FAILED!
    echo.
    echo Check error messages
)

echo.
pause
