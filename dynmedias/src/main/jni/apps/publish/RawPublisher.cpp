#include <stdio.h>
#ifndef _WIN32
#include <android/log.h>
#endif
#include <arpa/inet.h>
#include "RawPublisher.h"
#include "xsystem.h"
#undef LOG_TAG
#define LOG_TAG	"dylan.publish.stream"
#include "WriteLog.h"
#include "log.h"
#include "AVCConfig.h"
#include "Byte.h"

CUdpPublisher::CUdpPublisher()
	: fp(NULL) {
}
CUdpPublisher::~CUdpPublisher() {
    cleanup();
}

bool CUdpPublisher::init(JNIEnv* env, jobject thiz, jobject weak_thiz) {
    fp = fopen("/sdcard/111.dat", "wb+");
	return true;
}
void CUdpPublisher::setupVideo(const int width, const int height, const int bitrate, const unsigned char* sps, const int szSps, const unsigned char* pps, const int szPps) {
    fwrite(&width, 4, 1, fp);
    fwrite(&height, 4, 1, fp);
    fwrite(&bitrate, 4, 1, fp);
    fwrite(&szSps, 4, 1, fp);
    fwrite(sps, szSps, 1, fp);
    fwrite(&szPps, 4, 1, fp);
    fwrite(pps, szPps, 1, fp);
}
void CUdpPublisher::setupAudio(int channels, int sampleRate, const unsigned char* esds, const int szEsds) {
    fwrite(&channels, 4, 1, fp);
    fwrite(&sampleRate, 4, 1, fp);
    fwrite(&szEsds, 4, 1, fp);
    fwrite(esds, szEsds, 1, fp);
}

void CUdpPublisher::appendVideo(const unsigned char* datas, int length, long timecode, bool isKey, bool waitBlock) {
    uint8_t isAudio = false;
    fwrite(&isAudio, 1, 1, fp);
    fwrite(&timecode, 4, 1, fp);
    fwrite(&length, 4, 1, fp);
    fwrite(datas, length, 1, fp);
}
void CUdpPublisher::appendAudio(const unsigned char* datas, int length, long timecode, bool waitBlock) {
    uint8_t isAudio = true;
    fwrite(&isAudio, 1, 1, fp);
    fwrite(&timecode, 4, 1, fp);
    fwrite(&length, 4, 1, fp);
    fwrite(datas, length, 1, fp);
}
bool CUdpPublisher::connect(const char* url){
	return true;
}
void CUdpPublisher::disconnect() {
}
void CUdpPublisher::cleanup() {
    fclose(fp);
    fp = nullptr;
}

