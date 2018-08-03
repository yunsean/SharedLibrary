#pragma once
#ifdef _WIN32
#define __android_log_print(...) 1
#else
#include <android/log.h>
#endif
#include "SmartRet.h"

#ifndef LOG_TAG
#define LOG_TAG	"dylan"
#endif
#define wle(...)            ReturnAs(__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))
#define wlf(...)            ReturnAs(__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))
#define wlw(...)            ReturnAs(__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__))
#define wli(...)            ReturnAs(__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__))
#define wld(...)            ReturnAs(__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define dbg(...)            ReturnAs(__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

#define wlet(ret, ...)      (__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__), (ret))
#define wlft(ret, ...)      (__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__), (ret))
#define wlwt(ret, ...)      (__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__), (ret))
#define wlit(ret, ...)      (__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__), (ret))
#define wldt(ret, ...)      (__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__), (ret))
#define dbgt(ret, ...)      (__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__), (ret))

#define LOGE(...) 			__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGW(...) 			__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGI(...) 			__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGD(...) 			__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGV(...) 			__android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
