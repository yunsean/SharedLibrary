#include <stdio.h>
#include <android/log.h>
#include "libyuv.h"
#include "OverlayYuv.h"
#include "xsystem.h"
#undef LOG_TAG
#define LOG_TAG	"dylan.jacker.overlay"
#include "WriteLog.h"

#define min(x, y)	((x < y) ? x : y)
#define MAKE_FOURCC(a, b, c, d) (((uint32_t)d) | (((uint32_t)c) << 8) | (((uint32_t)b) << 16) | ( ((uint32_t)a) << 24))
const static char*	C_GENTLY_MODE_CHANGE_METHOD("gentlyModeChanged");
const static char*	C_GENTLY_MODE_CHANGE_PARAMETER("(Ljava/lang/Object;ZD)V");
COverlayYuv::COverlayYuv()
    : m_motionDetection(false)
    , m_stampWidth(0)
    , m_stampHeight(0)
    , m_stampSize(0)
    , m_stampY(NULL)
    , m_stampU(NULL)
    , m_stampV(NULL)

    , m_logoWidth(0)
    , m_logoHeight(0)
    , m_logoSize(0)
    , m_logoKey(NULL)
    , m_logoY(NULL)
    , m_logoU(NULL)
    , m_logoV(NULL)
    , m_logoLeft(0)
    , m_logoTop(0)

    , m_smallWidth(0)
    , m_smallHeight(0)
    , m_smallY(NULL)
    , m_smallU(NULL)
    , m_smallV(NULL) {
}
COverlayYuv::~COverlayYuv() {
    delete[] m_stampY;
    delete[] m_logoKey;
    delete[] m_logoY;
    delete[] m_smallY;
}

bool COverlayYuv::init(JNIEnv* env, jobject thiz, jobject weak_thiz, bool motionDetection){
	return true;
}
void COverlayYuv::logo(unsigned char* logo, int logo_width, int logo_height, int logo_x, int logo_y) {
    delete[] m_logoY;
    m_logoWidth = logo_width;
    m_logoHeight = logo_height;
    m_logoSize = m_logoWidth * m_logoHeight;
    m_logoY = new unsigned char[m_logoSize * 3 / 2];
    m_logoV = m_logoY + m_logoSize;
    m_logoU = m_logoV + (m_logoSize >> 2);
    libyuv::ARGBToI420(logo, m_logoWidth << 2,
        m_logoY, m_logoWidth,
        m_logoU, m_logoWidth >> 1,
        m_logoV, m_logoWidth >> 1,
        m_logoWidth, m_logoHeight);
    m_logoLeft = logo_x;
    m_logoTop = logo_y;
    if (m_logoLeft < 0) m_logoLeft = 0;
    if (m_logoTop < 0) m_logoTop = 0;
    m_logoKey = new unsigned char[m_logoSize << 1];
    unsigned char* key = m_logoKey;
    unsigned char* src = logo;
    for (int row = 0; row < logo_height; row++) {
        for (int col = 0; col < logo_width; col++) {
            if (*src < 16 || *src > 225) *key = 0;
            else *key = *src;
            src += 4;
            key += 1;
        }
    }
    key = m_logoKey + m_logoSize;
    src = m_logoKey;
    for (int row = 0; row < logo_height; row += 2) {
        int rowStart = row * logo_width;
        int keyRowStart = (row * logo_width) >> 2;
        for (int col = 0; col < logo_width; col += 2) {
            key[keyRowStart + (col >> 1)] = (src[rowStart + col] + src[rowStart + col + 1] +
                src[rowStart + logo_width + col] + src[rowStart + logo_width + col + 1]) >> 2;
        }
    }
}
void COverlayYuv::timeStamp(unsigned char* stamp, int stamp_width, int stamp_height) {
    if (m_stampWidth != stamp_width || m_stampHeight != stamp_height || m_stampY == NULL) {
        delete[] m_stampY;
        m_stampWidth = stamp_width;
        m_stampHeight = stamp_height;
        m_stampSize = m_stampWidth * m_stampHeight;
        m_stampY = new unsigned char[m_stampSize * 3 / 2];
        m_stampV = m_stampY + m_stampSize;
        m_stampU = m_stampV + (m_stampSize >> 2);
    }
    libyuv::ARGBToI420(stamp, m_stampWidth << 2,
        m_stampY, m_stampWidth,
        m_stampU, m_stampWidth >> 1,
        m_stampV, m_stampWidth >> 1, m_stampWidth, m_stampHeight);
}
void COverlayYuv::addLogo(unsigned char* main, int main_width, int main_height) {
    if (m_logoY != NULL) {
        int mainSize = main_width * main_height;
        int row = min(main_height - m_logoTop, m_logoHeight);
        int pitch = min(main_width - m_logoLeft, m_logoWidth);
        unsigned char* desY = main + main_width * m_logoTop + m_logoLeft;
        unsigned char* srcY = m_logoY;
        unsigned char* keyY = m_logoKey;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < pitch; j++) {
                if (keyY[j]) desY[j] = srcY[j];
            }
            desY += main_width;
            srcY += m_logoWidth;
            keyY += m_logoWidth;
        }

        unsigned char* srcV = m_logoV;
        unsigned char* desV = main + mainSize + (main_width >> 1) * (m_logoTop >> 1) + (m_logoLeft >> 1);
        unsigned char* srcU = m_logoU;
        unsigned char* desU = main + mainSize + (mainSize >> 2) + (main_width >> 1) * (m_logoTop >> 1) + (m_logoLeft >> 1);
        unsigned char* keyV = m_logoKey + m_logoSize;
        for (int i = 0; i < row >> 1; i++) {
            for (int j = 0; j < pitch >> 1; j++) {
                if (keyV[j]) {
                    desU[j] = srcU[j];
                    desV[j] = srcV[j];
                }
            }
            desU += (main_width >> 1);
            srcU += (m_logoWidth >> 1);
            desV += (main_width >> 1);
            srcV += (m_logoWidth >> 1);
            keyV += (m_logoWidth >> 1);
        }
    }
}
void COverlayYuv::addTimeStamp(unsigned char* main, int main_width, int main_height) {
    if (m_stampY != NULL) {
        int lineOffset = (main_width - m_stampWidth) >> 1;
        unsigned char* desY = main + (lineOffset > 0 ? lineOffset : 0);
        unsigned char* srcY = m_stampY;
        int row = min(main_height, m_stampHeight);
        int pitch = min(main_width, m_stampWidth);
        for (int i = 0; i < row; i++) {
            memcpy(desY, srcY, pitch);
            desY += main_width;
            srcY += m_stampWidth;
        }
    }
}
unsigned long COverlayYuv::getTickCount() {
   struct timeval tv;
   gettimeofday(&tv, NULL);
   return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}
void COverlayYuv::overlay(unsigned char* main, int main_width, int main_height, unsigned char* aux, int auxFourCC) {
    addLogo(main, main_width, main_height);
    addTimeStamp(main, main_width, main_height);

    int mainSize = main_width * main_height;
    if (auxFourCC == MAKE_FOURCC('I', '4', '2', '0')) {
        unsigned char* i420Y = aux;
        unsigned char* i420U = i420Y + mainSize;
        unsigned char* i420V = i420U + (mainSize >> 2);
        unsigned char* yv12Y = main;
        unsigned char* yv12U = yv12Y + mainSize;
        unsigned char* yv12V = yv12U + (mainSize >> 2);
        memcpy(i420Y, yv12Y, mainSize);
        memcpy(i420U, yv12V, mainSize >> 2);
        memcpy(i420V, yv12U, mainSize >> 2);
    } else if (auxFourCC == MAKE_FOURCC('N', 'V', '1', '2')) {
        unsigned char* nv12Y = aux;
        unsigned char* nv12UV = nv12Y + mainSize;
        unsigned char* yv12Y = main;
        unsigned char* yv12U = yv12Y + mainSize;
        unsigned char* yv12V = yv12U + (mainSize >> 2);
        libyuv::I420ToNV12(	yv12Y, main_width,
                            yv12V, main_width >> 1,
                            yv12U, main_width >> 1,
                            nv12Y, main_width,
                            nv12UV, main_width,
                            main_width, main_height);
    } else if (auxFourCC == MAKE_FOURCC('N', 'V', '2', '1')) {
        unsigned char* nv21Y = aux;
        unsigned char* nv21UV = nv21Y + mainSize;
        unsigned char* yv12Y = main;
        unsigned char* yv12U = yv12Y + mainSize;
        unsigned char* yv12V = yv12U + (mainSize >> 2);
        libyuv::I420ToNV21(	yv12Y, main_width,
                            yv12V, main_width >> 1,
                            yv12U, main_width >> 1,
                            nv21Y, main_width,
                            nv21UV, main_width,
                            main_width, main_height);
    } else if (auxFourCC != 0) {
        wli("unknown format:%c%c%c%c", (auxFourCC >> 24) & 0xff, (auxFourCC >> 16) & 0xff,
            (auxFourCC >> 8) & 0xff, (auxFourCC >> 0) & 0xff);
    }
}