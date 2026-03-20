@echo off
chcp 65001 >nul
echo ========================================
echo 开始构建APK...
echo ========================================
echo.

echo 设置Java环境...
set JAVA_HOME=C:\Program Files\Java\jdk-25.0.2
set PATH=%PATH%;%JAVA_HOME%\bin

echo.
echo Java版本:
java -version
if %errorlevel% neq 0 (
    echo Java环境配置失败！
    pause
    exit /b 1
)

echo.
echo ========================================
echo 开始构建...
echo ========================================
echo.

call gradlew.bat clean
call gradlew.bat assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo 构建成功！
    echo ========================================
    echo.
    echo APK文件位置: app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo 请将APK文件复制到上级目录，然后运行 install_apk.py 安装到手机
) else (
    echo.
    echo ========================================
    echo 构建失败！
    echo ========================================
    echo.
    echo 请检查错误信息
)

echo.
pause
