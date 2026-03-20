@echo off
:: 直接构建APK

echo 开始构建APK...

:: 检查Java是否可用
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：未找到Java
    echo 请先安装Java JDK
    pause
    exit /b 1
)

echo Java环境正常

:: 运行Gradle构建
echo 执行构建命令...
call gradlew assembleRelease

if %errorlevel% equ 0 (
    echo 构建成功！
    echo APK文件位置：app\build\outputs\apk\release\app-release.apk
    echo 请将APK文件传输到手机并安装
) else (
    echo 构建失败，请检查错误信息
)

pause
