#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
使用Python构建APK并保存日志
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
        # 保存输出到文件
        log_file = os.path.join(android_dir, 'build_log.txt')
        with open(log_file, 'w', encoding='utf-8') as f:
            # 执行构建命令
            result = subprocess.run(cmd, capture_output=True, text=True, cwd=android_dir)
            
            # 写入输出到文件
            f.write("=== 构建输出 ===\n")
            f.write(result.stdout)
            f.write("\n=== 错误输出 ===\n")
            f.write(result.stderr)
            f.write(f"\n=== 构建结果 ===\n")
            f.write(f"返回码: {result.returncode}")
        
        print(f"构建日志已保存到: {log_file}")
        
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