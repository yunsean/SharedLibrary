#pragma once
#include <jni.h>
#include "xthread.h"
#include <mutex>
#include <condition_variable>
#include <string>
#include <stdio.h>
#include <vector>
#include <queue>

class COverlayYuv {
public:
	COverlayYuv();
	~COverlayYuv();
public:
    bool init(JNIEnv* env, jobject thiz, jobject weak_thiz, bool motionDetection);
	void logo(unsigned char* logo, int logo_width, int logo_height, int logo_x, int logo_y);
	void timeStamp(unsigned char* stamp, int stamp_width, int stamp_height);
	void overlay(unsigned char* main, int main_width, int main_height, unsigned char* aux, int auxFourCC);

private:
    void addLogo(unsigned char* main, int main_width, int main_height);
    void addTimeStamp(unsigned char* main, int main_width, int main_height);

private:
    unsigned long getTickCount();

private:
	bool m_motionDetection;
	int m_stampWidth;
	int m_stampHeight;
	int m_stampSize;
	unsigned char* m_stampY;
	unsigned char* m_stampU;
	unsigned char* m_stampV;

	int m_logoWidth;
	int m_logoHeight;
	int m_logoSize;
	unsigned char* m_logoKey;
	unsigned char* m_logoY;
	unsigned char* m_logoU;
	unsigned char* m_logoV;
	int m_logoLeft;
	int m_logoTop;

    int m_smallWidth;
    int m_smallHeight;
	unsigned char* m_smallY;
    unsigned char* m_smallU;
    unsigned char* m_smallV;
};