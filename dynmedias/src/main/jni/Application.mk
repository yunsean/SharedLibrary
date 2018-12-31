# Build both ARMv5TE and ARMv7-A machine code.

# 打开下边这句，所有的静态库文件也会生成出来
# module-class-is-installable = $(if $(NDK_MODULE_CLASS.$1.INSTALLABLE),$(true),$(true))

#项目路径,必须的
APP_PROJECT_PATH	:= $(call my-dir)/..

#ndk 编译平台
APP_PLATFORM 		:= android-10

# CPU指令集
APP_ABI 			:= armeabi-v7a # armeabi arm64-v8a x86_64  #x86 #libyuv无法编译x86

#使用c++异常
APP_CPPFLAGS        := -std=c++11 -frtti -fexceptions


# c++ 库指定: 默认情况下，NDK的编译系统为最小的C++运行时库（/system/lib/libstdc++.so）提供C++头文件。
# 然而，NDK的C++的实现,可以让你使用或着链接在自己的应用程序中。
# APP_STL 			:= stlport_static
APP_STL 			:= c++_static

# 通过APP_MODULES显示指定只编译这些模块，同时也可以通过改选项强制编译那些默认未被编译的静态库
# APP_MODULES			:= libPocoFoundation libPocoNet
