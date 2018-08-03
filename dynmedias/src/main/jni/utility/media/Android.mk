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
						   $(CODEC_MP4AV_ROOT)/  \
						   $(UTILITY_COMMON_ROOT)/

LOCAL_STATIC_LIBRARIES 	+= $(CODEC_MP4AV_LIB)
LOCAL_STATIC_LIBRARIES	+= $(UTILITY_COMMON_LIB)

LOCAL_CFLAGS			:= -DNO_ICONV
LOCAL_SRC_FILES		 	:=  ./BitReader.cpp \
							./BitWriter.cpp \
							./H264Utility.cpp \
						    ./AACUtility.cpp \
							./FlvWriter.cpp \
							./AVCConfig.cpp
LOCAL_CPPFLAGS          += -std=gnu++11

LOCAL_MODULE    		:= $(UTILITY_MEDIA_LIB)

include $(BUILD_STATIC_LIBRARY)
#include $(BUILD_SHARED_LIBRARY)
