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
#include "rtmp.h"
#include "xthread.h"
#include "xstring.h"
#include "BasePublisher.h"

class CRtmpPublisher : public CBasePublisher {
public:
	CRtmpPublisher();
	~CRtmpPublisher();

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

public:
	unsigned char*  write_8(unsigned char* buffer, unsigned int v);
	unsigned char*  write_16(unsigned char* buffer, unsigned int v);
	unsigned char*  write_24(unsigned char* buffer, unsigned int v);
	unsigned char*  write_32(unsigned char* buffer, uint32_t v);

protected:
	void appendNal(const unsigned char* datas, int length, long timecode, bool isKey, bool waitBlock = false);
	RTMPPacket* DequeuePacket(int channel, unsigned int packetType, unsigned int size, unsigned int timestamp, bool waitBlock = false);
	void EnqueuePacket(RTMPPacket* packet, bool isDirect, bool isAudio);
	void EnqueueVideoConfigPacket();
	void EnqueueAudioConfigPacket();
	void EnqueueRtmpHeader();

	bool doConnect(CSmartHdr<RTMP*, void>& shRtmp);
	void rtmpThread();
	int headerSize(const unsigned char* data, const int size);

protected:
	static void	rtmpClean(RTMP* rtmp);

protected:
    enum {mp3 = 0, aac};

protected:
	class CRTMPPacket : public RTMPPacket {
	public:
		CRTMPPacket() {
			memset(this, 0, sizeof(RTMPPacket));
		}
	};
	
private:
	RTMPMetadata mMetadata;
	std::xstring<char> mRtmpServer;

	std::recursive_mutex mPacketMutex;
	CSmartBlk<CRTMPPacket> mAllPackets;

#ifndef _WIN32
    CCallback mCallback;
    jmethodID mStatistics;
	jmethodID mOnError;
#endif

	CSmartHdr<RTMP*, void> mRtmpClient;
	bool mStarted;
	std::xthread mRtmpThread;

	unsigned int mSentBlock;
	unsigned int mLostBlock;
};