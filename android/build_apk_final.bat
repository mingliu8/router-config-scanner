@echo off
chcp 65001
echo Building APK...

REM 设置Java环境变量
set JAVA_HOME=C:\Program Files\Java\jdk-25.0.2
set PATH=%PATH%;%JAVA_HOME%\bin

REM 验证Java
java -version
if %errorlevel% neq 0 (
    echo Java not found!
    pause
    exit /b 1
)

REM 执行构建
echo Starting build...

REM 使用绝对路径执行gradlew.bat
%~dp0gradlew.bat assembleDebug

if %errorlevel% equ 0 (
    echo BUILD SUCCESS!
    echo APK location: %~dp0app\build\outputs\apk\debug\app-debug.apk
) else (
    echo BUILD FAILED!
)

pause