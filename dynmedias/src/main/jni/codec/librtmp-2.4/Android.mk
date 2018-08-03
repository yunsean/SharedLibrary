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
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS 		:= optional
LOCAL_PRELINK_MODULE 	:= false

LOCAL_CFLAGS	        := -std=c99 -DNO_LOG2F -DNO_CRYPTO -D__BYTE_ORDER=__LITTLE_ENDIAN -D__FLOAT_WORD_ORDER=__LITTLE_ENDIAN
LOCAL_CFLAGS	        += -DNO_CRYPTO

LOCAL_SRC_FILES			:= amf.c \
					       log.c \
					       parseurl.c \
					       rtmp.c

LOCAL_MODULE            := $(CODEC_LIBRTMP_LIB)
#LOCAL_LDLIBS	 		:= -L$(SYSROOT)/usr/lib -llog -L. -lm
#include $(BUILD_SHARED_LIBRARY)
include $(BUILD_STATIC_LIBRARY)





