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
LOCAL_C_INCLUDES 		:=  $(LOCAL_PATH)/ \
                            $(UTILITY_COMMON_ROOT)/ \
                            $(UTILITY_MEDIA_ROOT)/ \
                            $(FFMPEG_INC_ROOT) \
                            $(CODEC_LIBYUV_INC_ROOT) \
                            $(OPENCV_INC_ROOT)

LOCAL_STATIC_LIBRARIES  += $(CODEC_LIBYUV_LIB)

LOCAL_LDLIBS	 		:= -L$(SYSROOT)/usr/lib -llog -L. -lm
LOCAL_LDLIBS	 		+= -L$(FFMPEG_LIB_ROOT)

OPENCV_LIB_TYPE			:= STATIC
include $(OPENCV_MK)

LOCAL_CFLAGS			:= -DNO_ICONV -D__STDC_CONSTANT_MACROS
LOCAL_CPPFLAGS          += -std=gnu++11
LOCAL_SRC_FILES		 	:=  ./com_dylan_live_overlay_NativeMethod.cpp \
                            ./OverlayYuv.cpp

LOCAL_MODULE    		:= $(APPS_JACKER_LIB)

include $(BUILD_SHARED_LIBRARY)
