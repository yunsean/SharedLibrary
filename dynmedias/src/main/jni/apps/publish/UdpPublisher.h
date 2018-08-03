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

extern "C" {
#include "libavformat/avformat.h"
#include "libavformat/avio.h"
#include "libavcodec/avcodec.h"
#include "libswscale/swscale.h"
#include "libavutil/avutil.h"
#include "libavutil/mathematics.h"
#include "libswresample/swresample.h"
#include "libavutil/opt.h"
#include "libavutil/channel_layout.h"
#include "libavutil/samplefmt.h"
#include "libavfilter/avfilter.h"
#include "libavutil/error.h"
#include "libavutil/mathematics.h"
#include "libavutil/time.h"
#include "inttypes.h"
#include "stdint.h"
};

class CUdpPublisher : public CBasePublisher {
public:
	CUdpPublisher();
	~CUdpPublisher();

public:
#ifndef _WIN32
    bool init(JNIEnv* env, jobject thiz, jobject weak_thiz);
#endif
    void setupVideo(const int width, const int height, const int bitrate, const unsigned char* sps, const int szSps, const unsigned char* pps, const int szPps);
	void setupAudio(int channels, int sampleRate, const unsigned char* esds, const int szEsds);
    void appendVideo(const unsigned char* datas, int length, long timecode, bool isKey, bool waitBlock = false);
	void appendAudio(const unsigned char* datas, int length, long timecode, bool waitBlock = false);
    bool connect(const char* url);
    void disconnect();
    void cleanup();

private:
    CSmartHdr<AVFormatContext*, void> mFormatContext;
    AVStream* mVideoStream;
    AVStream* mAudioStream;
    int mVideoTrackIndex;
    int mAudioTrackIndex;
    AVPacket mPacket;

#ifndef _WIN32
    CCallback mCallback;
    jmethodID mStatistics;
	jmethodID mOnError;
#endif
};