LOCAL_PATH:= $(call my-dir)

# header file
CODEC_ROOT				:= $(LOCAL_PATH)/codec/
CODEC_MP4AV_ROOT		:= $(CODEC_ROOT)/mp4av-1.5.0.1/
CODEC_LIBRTMP_ROOT		:= $(CODEC_ROOT)/librtmp-2.4/
CODEC_LIBYUV_ROOT		:= $(CODEC_ROOT)/libyuv/
CODEC_LIBYUV_INC_ROOT   := $(CODEC_LIBYUV_ROOT)/include/

FFMPEG_ROOT             := $(LOCAL_PATH)/ffmpeg/
FFMPEG_INC_ROOT         := $(FFMPEG_ROOT)/include/
FFMPEG_LIB_ROOT         := $(FFMPEG_ROOT)/lib/

OPENCV_ROOT             := $(LOCAL_PATH)/../opencv/
OPENCV_MK               := $(OPENCV_ROOT)/native/jni/OpenCV.mk

UTILITY_ROOT			:= $(LOCAL_PATH)/utility/
UTILITY_COMMON_ROOT		:= $(UTILITY_ROOT)/common/
UTILITY_MEDIA_ROOT		:= $(UTILITY_ROOT)/media/
UTILITY_ANDROID_ROOT    := $(UTILITY_ROOT)/android/

APPS_ROOT				:= $(LOCAL_PATH)/apps
APPS_LIVERTMP_ROOT		:= $(APPS_ROOT)/livertmp/

# lib files
LIB_PATH				:= $(LOCAL_PATH)/lib/
CODEC_MP4AV_LIB			:= mp4av
CODEC_LIBRTMP_LIB       := librtmp
CODEC_LIBYUV_LIB        := libyuv

UTILITY_COMMON_LIB		:= common
UTILITY_MEDIA_LIB		:= media
UTILITY_ANDROID_LIB     := android

APPS_PUBLISH_LIB		:= publish
APPS_PLAYER_LIB         := player
APPS_JACKER_LIB         := jacker

include $(call all-subdir-makefiles)
