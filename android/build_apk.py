#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
使用Python构建APK
"""

import os
import sys
import subprocess

def set_environment():
    """设置环境变量"""
    java_home = r'C:\Program Files\Java\jdk-25.0.2'
    os.environ['JAVA_HOME'] = java_home
    os.environ['PATH'] = java_home + r'\bin;' + os.environ['PATH']
    return java_home

def verify_java():
    """验证Java是否可用"""
    try:
        result = subprocess.run(['java', '-version'], 
                                capture_output=True, text=True)
        if result.returncode == 0:
            print("Java验证成功！")
            return True
        else:
            print("Java验证失败！")
            return False
    except Exception as e:
        print(f"Java验证出错: {e}")
        return False

def build_apk():
    """构建APK"""
    print("\n开始构建APK...")
    try:
        # 使用gradlew.bat构建
        result = subprocess.run(['gradlew.bat', 'assembleDebug'], 
                                capture_output=False, text=True)
        if result.returncode == 0:
            print("\n构建成功！")
            return True
        else:
            print(f"\n构建失败，返回码: {result.returncode}")
            return False
    except Exception as e:
        print(f"构建出错: {e}")
        return False

def main():
    """主函数"""
    print("=" * 50)
    print("开始构建APK...")
    print("=" * 50)
    
    # 设置环境变量
    print("\n设置环境变量...")
    java_home = set_environment()
    print(f"JAVA_HOME: {java_home}")
    
    # 验证Java
    print("\n验证Java环境...")
    if not verify_java():
        print("错误：Java环境验证失败")
        return
    
    # 构建APK
    if build_apk():
        print("\n" + "=" * 50)
        print("构建完成！")
        print("=" * 50)
        print("\nAPK文件位置: app\\build\\outputs\\apk\\debug\\app-debug.apk")
        print("\n请将APK文件复制到上级目录，然后运行 install_apk.py 安装到手机")
    else:
        print("\n" + "=" * 50)
        print("构建失败！")
        print("=" * 50)
        print("\n请检查错误信息并重新尝试")

if __name__ == "__main__":
    main()
