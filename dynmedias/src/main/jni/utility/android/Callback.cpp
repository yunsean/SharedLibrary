#include "Callback.h"
#include "SmartLck.h"
#include "WriteLog.h"

CCallback::CCallback() 
    : mClass(NULL)
	, mObject(NULL)
    , mWeakThiz(NULL)
    , mJVM(NULL)
    , mMethods()
    , mCritic() 
    
    , mJNIEnv(NULL)
    , mAttached(false)
    , mWrappedParam() {
}
CCallback::~CCallback() {
    CSmartLck lock(mCritic);
    JNIEnv* env(NULL);
	bool attached(false);
	int status(mJVM->GetEnv((void**)&env, JNI_VERSION_1_6));
	if(status < 0) {
		mJVM->AttachCurrentThread(&env, NULL);
		attached = true;
	}
	if(env != NULL) {
        if (mClass != NULL) env->DeleteGlobalRef(mClass);
        if (mWeakThiz != NULL) env->DeleteGlobalRef(mWeakThiz);
		if (mObject != NULL) env->DeleteGlobalRef(mObject);
		if(attached) {
			mJVM->DetachCurrentThread();
		}
	}
}

bool CCallback::init(JNIEnv* env, jobject thiz) {
	jclass clazz(env->GetObjectClass(thiz));
	if (clazz == NULL)return false;
	mObject = env->NewGlobalRef(thiz);
	mClass = (jclass)env->NewGlobalRef(clazz);
	env->GetJavaVM(&mJVM);
	return true;
}
bool CCallback::init(JNIEnv* env, jobject thiz, jobject weak_thiz) {
	jclass clazz(env->GetObjectClass(thiz));
	if (clazz == NULL)return false;
	mObject = env->NewGlobalRef(thiz);
	mClass = (jclass)env->NewGlobalRef(clazz);
	mWeakThiz = env->NewGlobalRef(weak_thiz);
	env->GetJavaVM(&mJVM);
	return true;
}
bool CCallback::init(JNIEnv* env, const char* className, jobject weak_thiz){
	jclass clazz = env->FindClass("com/dylan/medias/live/NativeMethod");
	if (clazz == NULL)return false;
	mClass = (jclass)env->NewGlobalRef(clazz);
	mWeakThiz = env->NewGlobalRef(weak_thiz);
	env->GetJavaVM(&mJVM);
	return true;
}
JNIEnv* CCallback::env() {
	return mJNIEnv;
}
jmethodID CCallback::bindStaticMethod(const char* methodName, const char* methodParam) {
    CSmartLck lock(mCritic);
    if (mJNIEnv != NULL) {
        jmethodID methodID = mJNIEnv->GetStaticMethodID(mClass, methodName, methodParam);
        mMethods.push_back(methodID);
        return methodID;
    } else {
        JNIEnv* env(NULL);
        bool attached(false);
        int status(mJVM->GetEnv((void**)&env, JNI_VERSION_1_6));
        if(status < 0) {
            mJVM->AttachCurrentThread(&env, NULL);
            attached = true;
        }
        if(env != NULL) {
            jmethodID methodID = env->GetStaticMethodID(mClass, methodName, methodParam);
            mMethods.push_back(methodID);
            if(attached) {
                mJVM->DetachCurrentThread();
            }
            return methodID;
        }
        return NULL;
    }
}
jmethodID CCallback::bindMethod(const char* methodName, const char* methodParam) {
	CSmartLck lock(mCritic);
	if (mJNIEnv != NULL) {
		jmethodID methodID = mJNIEnv->GetMethodID(mClass, methodName, methodParam);
		mMethods.push_back(methodID);
		return methodID;
	}
	else {
		JNIEnv* env(NULL);
		bool attached(false);
		int status(mJVM->GetEnv((void**)&env, JNI_VERSION_1_6));
		if (status < 0) {
			mJVM->AttachCurrentThread(&env, NULL);
			attached = true;
		}
		if (env != NULL) {
			jmethodID methodID = env->GetMethodID(mClass, methodName, methodParam);
			mMethods.push_back(methodID);
			if (attached) {
				mJVM->DetachCurrentThread();
			}
			return methodID;
		}
		return NULL;
	}
}

void CCallback::lock() {
    mCritic.lock();
    if (mJNIEnv == NULL) {
        int status(mJVM->GetEnv((void**)&mJNIEnv, JNI_VERSION_1_6));
        if(status < 0) {
            mJVM->AttachCurrentThread(&mJNIEnv, NULL);
            mAttached = true;
        }
    }
}
jstring CCallback::wrap(const char* str) {
    jstring result = mJNIEnv->NewStringUTF(str);
    mWrappedParam.push_back(result);
    return result;
}
jbyteArray CCallback::wrap(const unsigned char* datas, const int length) {
	jbyteArray result = mJNIEnv->NewByteArray(length);
	mJNIEnv->SetByteArrayRegion(result, 0, length, (const jbyte*)datas);
	mWrappedParam.push_back(result);
	return result;
}
jobject CCallback::wrapDirect(void* pointer, const int length) {
	return mJNIEnv->NewDirectByteBuffer(pointer, length);
}
jbyteArray CCallback::wrap(const int length) {
	jbyteArray result = mJNIEnv->NewByteArray(length);
	mWrappedParam.push_back(result);
	return result;
}
void* CCallback::beginFill(jbyteArray array) {
	return mJNIEnv->GetPrimitiveArrayCritical((jarray)array, 0);
}
void CCallback::endFill(jbyteArray array, void* pointer) {
	mJNIEnv->ReleasePrimitiveArrayCritical(array, pointer, 0);
}
void CCallback::unwrap(jobject object) {
	std::remove_if(mWrappedParam.begin(), mWrappedParam.end(), [object](jobject obj) {
		return obj == object;
	});
	mJNIEnv->DeleteLocalRef(object);
}
void CCallback::callVoidStaticMethod(const jmethodID methodID, ...) {
	CSmartLck lock(mCritic);
	va_list ap;
	va_start(ap, methodID);
	if (mJNIEnv != NULL) {
		mJNIEnv->CallStaticVoidMethodV(mClass, methodID, ap);
	}
	else {
		JNIEnv* env(NULL);
		bool attached(false);
		int status(mJVM->GetEnv((void**)&env, JNI_VERSION_1_6));
		if (status < 0) {
			mJVM->AttachCurrentThread(&env, NULL);
			attached = true;
		}
		if (env != NULL) {
			env->CallStaticVoidMethodV(mClass, methodID, ap);
			if (attached) {
				mJVM->DetachCurrentThread();
			}
		}
	}
	va_end(ap);
}
void CCallback::callVoidMethod(const jmethodID methodID, ...) {
	CSmartLck lock(mCritic);
	va_list ap;
	va_start(ap, methodID);
	if (mJNIEnv != NULL) {
		mJNIEnv->CallVoidMethodV(mClass, methodID, ap);
	}
	else {
		JNIEnv* env(NULL);
		bool attached(false);
		int status(mJVM->GetEnv((void**)&env, JNI_VERSION_1_6));
		if (status < 0) {
			mJVM->AttachCurrentThread(&env, NULL);
			attached = true;
		}
		if (env != NULL) {
			env->CallVoidMethodV(mObject, methodID, ap);
			if (attached) {
				mJVM->DetachCurrentThread();
			}
		}
	}
	va_end(ap);
}
void CCallback::unlock() {
    for (jobject object : mWrappedParam) {
        mJNIEnv->DeleteLocalRef(object);
    }
    if(mJNIEnv != NULL) {
		if(mAttached) {
			mJVM->DetachCurrentThread();
		}
        mJNIEnv = NULL;
	}
    mCritic.unlock();
}
jobject CCallback::weak_thiz() {
    return mWeakThiz;
}



