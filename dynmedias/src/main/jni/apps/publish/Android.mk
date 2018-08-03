# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH 				:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS 		:= optional
LOCAL_PRELINK_MODULE 	:= false

## all android header file
LOCAL_C_INCLUDES 		:= $(LOCAL_PATH)/ \
						   $(UTILITY_COMMON_ROOT)/ \
						   $(UTILITY_MEDIA_ROOT)/ \
						   $(UTILITY_ANDROID_ROOT)/ \
						   $(CODEC_LIBRTMP_ROOT)/ \
						   $(FFMPEG_INC_ROOT)/

LOCAL_STATIC_LIBRARIES 	+= $(CODEC_MP4AV_LIB) \
						   $(CODEC_LIBRTMP_LIB)	\
						   $(UTILITY_COMMON_LIB) \
						   $(UTILITY_MEDIA_LIB) \
						   $(UTILITY_ANDROID_LIB)

LOCAL_CFLAGS			:= -DNO_ICONV
LOCAL_CPPFLAGS          += --std=c++11
LOCAL_SRC_FILES		 	:=  ./com_dylan_medias_publish_NativeMethod.cpp \
							./RtmpPublisher.cpp \
							./UdpPublisher.cpp \
							./Metadata.cpp

LOCAL_MODULE    		:= $(APPS_PUBLISH_LIB)

LOCAL_LDLIBS	 		:= -L$(SYSROOT)/usr/lib -llog -L. -lm
LOCAL_LDLIBS	 		+= -L$(FFMPEG_LIB_ROOT) -lavformat -lavcodec -lavutil -lswscale -lswresample -lz

include $(BUILD_SHARED_LIBRARY)
