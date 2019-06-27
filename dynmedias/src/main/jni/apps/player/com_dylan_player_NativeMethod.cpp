#include <android/log.h>
#include <jni.h>
#include <stdio.h>
#include <inttypes.h>
#include <thread>
#include "WriteLog.h"

extern "C" {
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libswresample/swresample.h"
#include "libswscale/swscale.h"
};

struct MediaFrame {
    int index;
    int key;
    long timeStamp;
    unsigned char* data;
    int size;
};

class CPlayer {
public:
    CPlayer()
        : m_context(NULL) 
        , m_videoIndex(-1)
        , m_audioIndex(-1)
        , m_packet()
    	, m_bsfs(NULL)
    	, m_bsfHotIndex(-1) {
        av_init_packet(&m_packet);
    }
    ~CPlayer() {
        close();
    }

public:
    enum Codec{None, Unknown, Avc, Aac, Mp3, Alaw};

public:
    bool init() {
        m_context = avformat_alloc_context();
        if (m_context == NULL) {
            return wlet(false, "Create format context failed.");
        }
        m_context->opaque = m_context;
        m_context->interrupt_callback.callback = avio_decode_interrupt_cb;
        m_context->interrupt_callback.opaque = &m_context->opaque;
        return true;
    }
    bool open(const char* url, bool tcpOnly) {
        if (m_context == NULL) return false;
        AVDictionary* options = NULL;
        if (tcpOnly) {
            av_dict_set(&options, "rtsp_transport", "tcp", 0);
        }
        av_dict_set(&options, "buffer_size", "327680", 0);
        if (avformat_open_input(&m_context, url, NULL, &options) < 0 || m_context == NULL) {
            av_dict_free(&options);
            return wlet(false, "Could not open the stream: %s", url);
        }
        av_dict_free(&options);
        if (avformat_find_stream_info(m_context, NULL) < 0) {
            return wlet(false, "Could not found any stream info.");
        }
        m_videoIndex = -1;
        m_audioIndex = -1;
        m_bsfs = new AVBSFContext*[m_context->nb_streams];
		memset(m_bsfs, 0, sizeof(AVBSFContext*) * m_context->nb_streams);
		for (int i = 0; i < m_context->nb_streams; i++) {
			if (m_context->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
				if (m_videoIndex == -1) m_videoIndex = i;
			} else if (m_context->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
				if (m_audioIndex == -1) m_audioIndex = i;
			}
			if (m_context->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO &&
				m_context->streams[i]->codecpar->codec_id == AV_CODEC_ID_H264) {
				const AVBitStreamFilter* filter = av_bsf_get_by_name("h264_mp4toannexb");
				if (filter != nullptr) {
					av_bsf_alloc(filter, &m_bsfs[i]);
					avcodec_parameters_copy(m_bsfs[i]->par_in, m_context->streams[i]->codecpar);
					int ret = av_bsf_init(m_bsfs[i]);
					if (ret < 0) {
						av_bsf_free(&m_bsfs[i]);
						m_bsfs[i] = nullptr;
					}
				}
            }
        }
		m_bsfHotIndex = -1;
        return true;
    }
    int videoCodec() {
    	if (m_videoIndex < 0) return None;
        switch (m_context->streams[m_videoIndex]->codecpar->codec_id) {
        case AV_CODEC_ID_H264:
        	return Avc;
        default:
        	return Unknown;
        }
    }
    int audioCodec() {
    	if (m_audioIndex < 0) return None;
    	switch (m_context->streams[m_audioIndex]->codecpar->codec_id) {
    	case AV_CODEC_ID_AAC:
    		return Aac;
    	case AV_CODEC_ID_MP3:
    		return Mp3;
    	case AV_CODEC_ID_PCM_ALAW:
    	    return Alaw;
    	default:
    	    wlw("unknown audio codec: %d", m_context->streams[m_audioIndex]->codecpar->codec_id);
        	return Unknown;
    	}
    }
    int videoIndex() {
        return m_videoIndex;
    }
    int audioIndex() {
        return m_audioIndex;
    }
    uint8_t* videoExtraData(int& size) {
    	if (m_videoIndex < 0) return NULL;
        size = m_context->streams[m_videoIndex]->codecpar->extradata_size;
        return m_context->streams[m_videoIndex]->codecpar->extradata;
    }
    uint8_t* audioExtraData(int& size) {
        if (m_audioIndex < 0) return NULL;
        size = m_context->streams[m_audioIndex]->codecpar->extradata_size;
        return m_context->streams[m_audioIndex]->codecpar->extradata;
    }

    bool read(MediaFrame& frame) {
        if (m_context == NULL) return NULL;
        av_packet_unref(&m_packet);
		if (m_bsfHotIndex >= 0) {
			int error = av_bsf_receive_packet(m_bsfs[m_bsfHotIndex], &m_packet);
			if (error == 0) {
                if (m_packet.dts == 0x8000000000000000) m_packet.dts = 0;
				int64_t pts = (int64_t)(m_packet.dts * av_q2d(m_context->streams[m_bsfHotIndex]->time_base) * 1000);
                frame.index = m_bsfHotIndex;
				frame.timeStamp = pts;
				frame.data = m_packet.data;
				frame.size = m_packet.size;
                frame.key = (m_packet.flags & AV_PKT_FLAG_KEY) ? 0xff : 0x00;
				return true;
			} else {
				m_bsfHotIndex = -1;
			}
		}
        do {
            av_packet_unref(&m_packet);
            if (av_read_frame(m_context, &m_packet) < 0) {
                return wlet(false, "Read failed or eos.");
            }
        } while (m_packet.stream_index != m_videoIndex && m_packet.stream_index != m_audioIndex);
        int index = m_packet.stream_index;
        if (index < 0 || index >= m_context->nb_streams) {
            return wlet(false, "Unknown internal error.");
        }
        if (m_bsfs[index] != nullptr) {
            m_bsfHotIndex = index;
            if (av_bsf_send_packet(m_bsfs[index], &m_packet) < 0) {
                return wlet(false, "Bit stream filter process failed.");
            }
            int error = av_bsf_receive_packet(m_bsfs[index], &m_packet);
            if (error == 0) {
                if (m_packet.dts == 0x8000000000000000) m_packet.dts = 0;
                int64_t pts = (int64_t)(m_packet.dts * av_q2d(m_context->streams[index]->time_base) * 1000);
                frame.index = index;
                frame.timeStamp = pts;
                frame.data = m_packet.data;
                frame.size = m_packet.size;
                frame.key = (m_packet.flags & AV_PKT_FLAG_KEY) ? 0xff : 0x00;
                m_bsfHotIndex = index;
                return true;
            }
        } else {
            if (m_packet.dts == 0x8000000000000000) m_packet.dts = 0;
            int64_t pts = (int64_t)(m_packet.dts * av_q2d(m_context->streams[index]->time_base) * 1000);
            frame.index = index;
            frame.timeStamp = pts;
            frame.data = m_packet.data;
            frame.size = m_packet.size;
            frame.key = (m_packet.flags & AV_PKT_FLAG_KEY) ? 0xff : 0x00;
            return true;
        }
        return wlet(false, "Read failed or eos.");
    }
    void interrupt() {
        if (m_context != NULL) {
		    m_context->opaque = NULL;
	    }
    }
    void close() {
    	if (m_context != NULL) {
			if (m_bsfs != nullptr) {
				for (int i = 0; i < m_context->nb_streams; i++) {
					if (m_bsfs[i] != nullptr) {
						av_bsf_free(&m_bsfs[i]);
					}
				}
			}
			avformat_free_context(m_context);
            m_context = nullptr;
		}
		delete[] m_bsfs;
        m_bsfs = nullptr;
		av_packet_unref(&m_packet);
    }

protected:
    static int avio_decode_interrupt_cb(void* flag) {
        int* iflag = (int*)flag;
        if (*iflag == 0) return 1;
        else return 0;
    }

private:
    AVFormatContext* m_context;
    int m_videoIndex;
    int m_audioIndex;
    AVPacket m_packet;
    AVBSFContext** m_bsfs;
    int m_bsfHotIndex;
};

extern "C" {

int avio_decode_interrupt_cb(void* flag) {
	int* iflag = (int*)flag;
	if (*iflag == 0) return 1;
	else return 0;
}
JNIEXPORT jlong JNICALL Java_com_dylan_medias_stream_NativeMethod_native_1init(JNIEnv* env, jobject thiz) {
    wle("Java_com_dylan_medias_stream_NativeMethod_native_1init");
	av_register_all();
	avformat_network_init();
    CPlayer* player = new CPlayer();
    if (!player->init() ) {
        delete player;
        return 0;
    }
	return (long)player;
}
JNIEXPORT jboolean JNICALL Java_com_dylan_medias_stream_NativeMethod_native_1open(JNIEnv* env, jobject thiz, jlong handle, jstring jurl, jboolean tcpOnly) {
	CPlayer* player = (CPlayer*)handle;
	if (player == NULL) return JNI_FALSE;
    const char* url = env->GetStringUTFChars(jurl, NULL);
    bool result = player->open(url, tcpOnly);
    env->ReleaseStringUTFChars(jurl, url);
    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jint JNICALL Java_com_dylan_medias_stream_NativeMethod_native_1videoIndex(JNIEnv* env, jobject thiz, jlong handle) {
    CPlayer* player = (CPlayer*)handle;
	if (player == NULL) return -1;
    return player->videoIndex();
}
JNIEXPORT jint JNICALL Java_com_dylan_medias_stream_NativeMethod_native_1audioIndex(JNIEnv* env, jobject thiz, jlong handle) {
    CPlayer* player = (CPlayer*)handle;
	if (player == NULL) return -1;
    return player->audioIndex();
}

JNIEXPORT jint JNICALL Java_com_dylan_medias_stream_NativeMethod_native_1videoCodec(JNIEnv* env, jobject thiz, jlong handle) {
    CPlayer* player = (CPlayer*)handle;
	if (player == NULL) return -1;
    return player->videoCodec();
}
JNIEXPORT jint JNICALL Java_com_dylan_medias_stream_NativeMethod_native_1audioCodec(JNIEnv* env, jobject thiz, jlong handle) {
    CPlayer* player = (CPlayer*)handle;
	if (player == NULL) return -1;
    return player->audioCodec();
}

JNIEXPORT jbyteArray JNICALL Java_com_dylan_medias_stream_NativeMethod_native_1videoExtraData(JNIEnv* env, jobject thiz, jlong handle) {
    CPlayer* player = (CPlayer*)handle;
	if (player == NULL) return NULL;
    int size(0);
    uint8_t* data(player->videoExtraData(size));
    if (data == NULL || size <= 0) return NULL;
    jbyteArray result = env->NewByteArray(size);
    char* buffer = (char*)env->GetPrimitiveArrayCritical((jarray)result, 0);
    memcpy(buffer, data, size);
    env->ReleasePrimitiveArrayCritical(result, buffer, 0);
    return result;
}
JNIEXPORT jbyteArray JNICALL Java_com_dylan_medias_stream_NativeMethod_native_1audioExtraData(JNIEnv* env, jobject thiz, jlong handle) {
    CPlayer* player = (CPlayer*)handle;
	if (player == NULL) return NULL;
    int size(0);
    uint8_t* data(player->audioExtraData(size));
    if (data == NULL || size <= 1) return NULL;
    jbyteArray result = env->NewByteArray(size);
    char* buffer = (char*)env->GetPrimitiveArrayCritical((jarray)result, 0);
    memcpy(buffer, data, size);
    env->ReleasePrimitiveArrayCritical(result, buffer, 0);
    return result;
}

JNIEXPORT jbyteArray JNICALL Java_com_dylan_medias_stream_NativeMethod_native_1read(JNIEnv* env, jobject thiz, jlong handle) {
    CPlayer* player = (CPlayer*)handle;
	if (player == NULL) return NULL;

	MediaFrame frame;
    try {
        if (!player->read(frame)) {
            return NULL;
        }
    } catch (...) {
        wle("catch a c++ exception.");
        return NULL;
    }
    int len = frame.size + 4 + 8;
    jbyteArray result = env->NewByteArray(len);
    char* buffer = (char*)env->GetPrimitiveArrayCritical((jarray)result, 0);
    *(int*)buffer = (frame.index & 0x00ffffff) | ((frame.key << 24) & 0xff000000);
    *(int64_t*)(buffer + 4) = frame.timeStamp;
    memcpy(buffer + 12, frame.data, frame.size);
    env->ReleasePrimitiveArrayCritical(result, buffer, 0);
    return result;
}
JNIEXPORT jlong JNICALL Java_com_dylan_medias_stream_NativeMethod_native_1interrupt(JNIEnv* env, jobject thiz, jlong handle) {
	CPlayer* player = (CPlayer*)handle;
	if (player == NULL) return 0;
    player->interrupt();
    return (jlong)player;
}
JNIEXPORT void JNICALL Java_com_dylan_medias_stream_NativeMethod_native_1close(JNIEnv* env, jobject thiz, jlong handle) {
	CPlayer* player = (CPlayer*)handle;
	if (player == NULL) return;
    player->close();
    delete player;
}

}
