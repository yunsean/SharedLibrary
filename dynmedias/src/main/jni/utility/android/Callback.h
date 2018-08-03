#pragma once
#include <mutex>
#include <vector>
#include <jni.h>
#include "xcritic.h"
#include "xstring.h"

class CCallback {
public:
    CCallback();
    ~CCallback();
 
public:
	bool init(JNIEnv* env, jobject thiz);
	bool init(JNIEnv* env, jobject thiz, jobject weak_thiz);
	bool init(JNIEnv* env, const char* className, jobject weak_thiz);
	void lock();
	JNIEnv* env();
	jmethodID bindStaticMethod(const char* methodName, const char* methodParam);
	jmethodID bindMethod(const char* methodName, const char* methodParam);
	jstring wrap(const char* str);
	jbyteArray wrap(const unsigned char* data, const int length);
	jobject wrapDirect(void* pointer, const int length);
	jbyteArray wrap(const int length);
	void* beginFill(jbyteArray array);
	void endFill(jbyteArray array, void* pointer);
	void unwrap(jobject object);
	void callVoidStaticMethod(const jmethodID methodID, ...);
	void callVoidMethod(const jmethodID methodID, ...);
    void unlock();
    jobject weak_thiz();

public:
	jclass mClass; 
	jobject mObject;
	jobject mWeakThiz;
    std::vector<jmethodID> mMethods;
	JavaVM* mJVM;
	std::xcritic mCritic;

    JNIEnv* mJNIEnv;
    bool mAttached;
    std::vector<jobject> mWrappedParam;
};