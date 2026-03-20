#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
使用Python的subprocess模块构建APK
"""

import os
import sys
import subprocess

def build_apk():
    """构建APK"""
    # 设置工作目录
    android_dir = os.path.dirname(os.path.abspath(__file__))
    os.chdir(android_dir)
    
    # 设置环境变量
    java_home = r'C:\Program Files\Java\jdk-25.0.2'
    os.environ['JAVA_HOME'] = java_home
    os.environ['PATH'] = java_home + r'\bin;' + os.environ['PATH']
    
    # 构建命令
    cmd = ['gradlew.bat', 'assembleDebug']
    
    print(f"执行命令: {' '.join(cmd)}")
    print(f"工作目录: {os.getcwd()}")
    
    # 执行构建
    try:
        result = subprocess.run(cmd, capture_output=True, text=True, cwd=android_dir)
        
        print("\n=== 构建输出 ===")
        print(result.stdout)
        print("\n=== 错误输出 ===")
        print(result.stderr)
        print(f"\n=== 构建结果 ===")
        print(f"返回码: {result.returncode}")
        
        if result.returncode == 0:
            print("\n构建成功！")
            # 查找APK文件
            apk_path = os.path.join(android_dir, 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk')
            if os.path.exists(apk_path):
                print(f"APK文件位置: {apk_path}")
            else:
                print("APK文件未找到，可能构建失败或路径错误")
        else:
            print("\n构建失败！")
    except Exception as e:
        print(f"执行命令时出错: {e}")

if __name__ == "__main__":
    build_apk()