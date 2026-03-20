#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
使用Python构建APK（绕过终端问题）
"""

import os
import sys
import subprocess

def main():
    """主函数"""
    print("=" * 50)
    print("开始构建APK...")
    print("=" * 50)
    
    # 设置环境变量
    print("\n设置环境变量...")
    java_home = r'C:\Program Files\Java\jdk-25.0.2'
    os.environ['JAVA_HOME'] = java_home
    os.environ['PATH'] = java_home + r'\bin;' + os.environ['PATH']
    print(f"JAVA_HOME: {java_home}")
    
    # 验证Java
    print("\n验证Java环境...")
    try:
        result = subprocess.run(['java', '-version'], 
                                capture_output=True, text=True)
        if result.returncode == 0:
            print("Java验证成功！")
        else:
            print("Java验证失败！")
            return
    except Exception as e:
        print(f"Java验证出错: {e}")
        return
    
    # 构建APK
    print("\n开始构建APK...")
    try:
        # 使用subprocess执行gradlew.bat
        process = subprocess.Popen(['cmd', '/c', 'gradlew.bat', 'assembleDebug'], 
                                 cwd=os.path.dirname(os.path.abspath(__file__)),
                                 stdout=subprocess.PIPE,
                                 stderr=subprocess.STDOUT,
                                 text=True)
        
        # 实时输出构建过程
        for line in process.stdout:
            print(line.strip())
        
        # 等待构建完成
        process.wait()
        
        if process.returncode == 0:
            print("\n" + "=" * 50)
            print("构建成功！")
            print("=" * 50)
            apk_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 
                                   'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk')
            print(f"\nAPK文件位置: {apk_path}")
        else:
            print("\n" + "=" * 50)
            print("构建失败！")
            print("=" * 50)
    except Exception as e:
        print(f"构建出错: {e}")

if __name__ == "__main__":
    main()