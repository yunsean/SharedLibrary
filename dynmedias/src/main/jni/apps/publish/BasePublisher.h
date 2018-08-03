#pragma once
#include <jni.h>
#include <string>
#include <stdio.h>
#include <vector>
#include <queue>
#include "Metadata.h"
#include "SmartPtr.h"
#include "SmartHdr.h"
#include "SmartBlk.h"
#include "xthread.h"
#include "xstring.h"

class CBasePublisher {
public:
	virtual ~CBasePublisher() {}

public:
    virtual bool init(JNIEnv* env, jobject thiz, jobject weak_thiz) = 0;
    virtual void setupVideo(const int width, const int height, const int bitrate, const unsigned char* sps, const int szSps, const unsigned char* pps, const int szPps) = 0;
	virtual void setupAudio(int channels, int sampleRate, const unsigned char* esds, const int szEsds) = 0;
    virtual void appendVideo(const unsigned char* datas, int length, long timecode, bool isKey, bool waitBlock = false) = 0;
	virtual void appendAudio(const unsigned char* datas, int length, long timecode, bool waitBlock = false) = 0;
    virtual bool connect(const char* url) = 0;
    virtual void disconnect() = 0;
    virtual void cleanup() = 0;
};