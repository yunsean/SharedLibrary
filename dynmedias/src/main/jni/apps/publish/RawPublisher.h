#pragma once
#ifndef _WIN32
#include <jni.h>
#include "Callback.h"
#endif
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
#include "BasePublisher.h"


class CRawPublisher : public CBasePublisher {
public:
	CRawPublisher();
	~CRawPublisher();

public:
    bool init(JNIEnv* env, jobject thiz, jobject weak_thiz);
    void setupVideo(const int width, const int height, const int bitrate, const unsigned char* sps, const int szSps, const unsigned char* pps, const int szPps);
	void setupAudio(int channels, int sampleRate, const unsigned char* esds, const int szEsds);
    void appendVideo(const unsigned char* datas, int length, long timecode, bool isKey, bool waitBlock = false);
	void appendAudio(const unsigned char* datas, int length, long timecode, bool waitBlock = false);
    bool connect(const char* url);
    void disconnect();
    void cleanup();

private:
	FILE* fp;
};