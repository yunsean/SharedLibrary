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
LOCAL_PATH          	:= $(call my-dir)
include $(CLEAR_VARS)   

LOCAL_SRC_FILES	    	:=	aac.cpp \
					    	adts.cpp \
					    	h264.cpp \
					    	h264_sps.cpp \
					    	mp3.cpp \
					    	ac3.cpp \
					    	amr.cpp \
					    	h264_slice.cpp \
					    	mbs.cpp \
					    	mpeg4.cpp

LOCAL_MODULE        	:=  $(CODEC_MP4AV_LIB)
#LOCAL_LDLIBS	 		:= -L$(SYSROOT)/usr/lib -llog -L. -lm
#include $(BUILD_SHARED_LIBRARY)
include $(BUILD_STATIC_LIBRARY)





