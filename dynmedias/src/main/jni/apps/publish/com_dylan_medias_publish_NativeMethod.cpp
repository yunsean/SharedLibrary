#include <android/log.h>
#include <jni.h>
#include <stdio.h>
#include "xchar.h"
#include "WriteLog.h"
#include <mutex>
#include "RtmpPublisher.h"
#include "UdpPublisher.h"
#include <thread>

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

extern "C" {
	/*void udp() {
        static bool first = true;
        if (!first) return;
        first = false;
		wli("enter udp.");
        AVOutputFormat *ofmt = NULL;
        AVFormatContext *ifmt_ctx = NULL;
        AVFormatContext *ofmt_ctx = NULL;
        AVPacket pkt;
        const char *in_filename = "http://r.yunsean.com:9908/dylan/dylan/brqs.mp4";
        FILE* fp = fopen(in_filename, "rb");
        wli("fp=%p", fp);
        //const char *out_filename = "rtmp://192.168.2.251/live/llsw";
        const char *out_filename = "udp://233.233.233.233:10000";
        int ret, i;
        int videoindex = -1;
        int frame_index = 0;
        int64_t start_time = 0;
        av_register_all();
        avformat_network_init();
        if ((ret = avformat_open_input(&ifmt_ctx, in_filename, 0, 0)) < 0) {
            wle("Could not open input file.");
            goto end;
        }
        if ((ret = avformat_find_stream_info(ifmt_ctx, 0)) < 0) {
            wle("Failed to retrieve input stream information");
            goto end;
        }

        for (i = 0; i < ifmt_ctx->nb_streams; i++) {
            if (ifmt_ctx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
                videoindex = i;
                break;
            }
        }
        av_dump_format(ifmt_ctx, 0, in_filename, 0);
        //avformat_alloc_output_context2(&ofmt_ctx, NULL, "flv", out_filename);
        avformat_alloc_output_context2(&ofmt_ctx, NULL, "mpegts", out_filename);
        if (!ofmt_ctx) {
            wle("Could not create output context\n");
            ret = AVERROR_UNKNOWN;
            goto end;
        }
        ofmt = ofmt_ctx->oformat;
        for (i = 0; i < ifmt_ctx->nb_streams; i++) {
            AVStream *in_stream = ifmt_ctx->streams[i];
            AVStream *out_stream = avformat_new_stream(ofmt_ctx, in_stream->codec->codec);
            if (!out_stream) {
                wle("Failed allocating output stream\n");
                ret = AVERROR_UNKNOWN;
                goto end;
            }
            ret = avcodec_copy_context(out_stream->codec, in_stream->codec);
            if (ret < 0) {
                wle("Failed to copy context from input to output stream codec context\n");
                goto end;
            }
            out_stream->codec->codec_tag = 0;
            if (ofmt_ctx->oformat->flags & AVFMT_GLOBALHEADER)
                out_stream->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;
        }
        av_dump_format(ofmt_ctx, 0, out_filename, 1);
        if (!(ofmt->flags & AVFMT_NOFILE)) {
            ret = avio_open(&ofmt_ctx->pb, out_filename, AVIO_FLAG_WRITE);
            if (ret < 0) {
                wle("Could not open output URL '%s'", out_filename);
                goto end;
            }
        }
        ret = avformat_write_header(ofmt_ctx, NULL);
        if (ret < 0) {
            wle("Error occurred when opening output URL\n");
            goto end;
        }
        start_time = av_gettime();
        while (1) {
            AVStream *in_stream, *out_stream;
            ret = av_read_frame(ifmt_ctx, &pkt);
            if (ret < 0) break;
            if (pkt.pts == AV_NOPTS_VALUE) {
                AVRational time_base1 = ifmt_ctx->streams[videoindex]->time_base;
                int64_t calc_duration = (double)AV_TIME_BASE / av_q2d(ifmt_ctx->streams[videoindex]->r_frame_rate);
                pkt.pts = (double)(frame_index*calc_duration) / (double)(av_q2d(time_base1)*AV_TIME_BASE);
                pkt.dts = pkt.pts;
                pkt.duration = (double)calc_duration / (double)(av_q2d(time_base1)*AV_TIME_BASE);
            }
            if (pkt.stream_index == videoindex) {
                AVRational time_base = ifmt_ctx->streams[videoindex]->time_base;
                AVRational time_base_q = { 1,AV_TIME_BASE };
                int64_t pts_time = av_rescale_q(pkt.dts, time_base, time_base_q);
                int64_t now_time = av_gettime() - start_time;
                if (pts_time > now_time) av_usleep(pts_time - now_time);
            }

            in_stream = ifmt_ctx->streams[pkt.stream_index];
            out_stream = ofmt_ctx->streams[pkt.stream_index];
            pkt.pts = av_rescale_q_rnd(pkt.pts, in_stream->time_base, out_stream->time_base, (AVRounding)(AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
            pkt.dts = av_rescale_q_rnd(pkt.dts, in_stream->time_base, out_stream->time_base, (AVRounding)(AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
            pkt.duration = av_rescale_q(pkt.duration, in_stream->time_base, out_stream->time_base);
            pkt.pos = -1;
            if (pkt.stream_index == videoindex) {
                wle("Send %8d video frames to output URL\n", frame_index);
                frame_index++;
            }
            ret = av_interleaved_write_frame(ofmt_ctx, &pkt);
            if (ret < 0) {
                wle("Error muxing packet\n");
                break;
            }
            av_free_packet(&pkt);
        }
        av_write_trailer(ofmt_ctx);
        end:
        avformat_close_input(&ifmt_ctx);
        if (ofmt_ctx && !(ofmt->flags & AVFMT_NOFILE)) {
            avio_close(ofmt_ctx->pb);
        }
        avformat_free_context(ofmt_ctx);
        if (ret < 0 && ret != AVERROR_EOF) {
            wle("Error occurred.\n");
        }
		wlw("exit udp");
	}//*/

	JNIEXPORT jlong JNICALL JNICALL Java_com_dylan_medias_publish_NativeMethod_native_1open(JNIEnv* env, jobject thiz, jobject weak_this, jboolean useUdp) {
		CBasePublisher* publiser(nullptr);
		if (useUdp) publiser = new CUdpPublisher();
		else publiser = new CRtmpPublisher();
		if (!publiser->init(env, thiz, weak_this)) {
			delete publiser;
			return 0;
		}
/*		wli("Java_com_dylan_medias_publish_NativeMethod_native_1open");
		new std::thread([](){
			udp();
		}); //*/
		return (jlong)publiser;
	}
	
	JNIEXPORT void JNICALL JNICALL Java_com_dylan_medias_publish_NativeMethod_native_1videoAvc(JNIEnv* env, jobject thiz, jlong handle, jint width, jint height, jint bitrate, jbyteArray jsps, jbyteArray jpps) {
		if (handle == 0)return;
		CBasePublisher* publiser((CBasePublisher*)handle);
		jsize szSps(env->GetArrayLength(jsps));
		jbyte* lpSps(env->GetByteArrayElements(jsps, JNI_FALSE));
		jsize szPps(env->GetArrayLength(jpps));
		jbyte* lpPps(env->GetByteArrayElements(jpps, JNI_FALSE));
		unsigned char* data((unsigned char*)lpSps);
		publiser->setupVideo(width, height, bitrate, (unsigned char*)lpSps, szSps, (unsigned char*)lpPps, szPps);
		env->ReleaseByteArrayElements(jsps, lpSps, 0);
		env->ReleaseByteArrayElements(jpps, lpPps, 0);
	}
	JNIEXPORT void JNICALL JNICALL Java_com_dylan_medias_publish_NativeMethod_native_1audioAac(JNIEnv* env, jobject thiz, jlong handle, jint sampleRate, jint channels, jbyteArray jesds) {
		if (handle == 0)return;
		CBasePublisher* publiser((CBasePublisher*)handle);
		jsize szEsds(env->GetArrayLength(jesds));
		jbyte* lpEsds(env->GetByteArrayElements(jesds, JNI_FALSE));
		publiser->setupAudio(channels, sampleRate, (unsigned char*)lpEsds, szEsds);
		env->ReleaseByteArrayElements(jesds, lpEsds, 0);
	}
	JNIEXPORT jboolean JNICALL JNICALL Java_com_dylan_medias_publish_NativeMethod_native_1start(JNIEnv* env, jobject thiz, jlong handle, jstring jurl) {
		if (handle == 0)return JNI_FALSE;
		CBasePublisher* publiser((CBasePublisher*)handle);
		const char* url(env->GetStringUTFChars(jurl, NULL));
		bool result(publiser->connect(url));
		env->ReleaseStringUTFChars(jurl, url);
		if (result)return JNI_TRUE;
		else return JNI_FALSE;
	}
	
	JNIEXPORT void JNICALL JNICALL Java_com_dylan_medias_publish_NativeMethod_native_1videoFrame(JNIEnv* env, jobject thiz, jlong handle, jbyteArray jdatas, jint length, jlong jtimeStamp, jboolean isKey, jboolean wait) {
		if (handle == 0)return;
		CBasePublisher* publiser((CBasePublisher*)handle);
		jbyte* data(env->GetByteArrayElements(jdatas, JNI_FALSE));
		publiser->appendVideo((unsigned char*)data, length, (long)jtimeStamp, isKey, wait);
		env->ReleaseByteArrayElements(jdatas, data, 0);
	}
	JNIEXPORT void JNICALL JNICALL Java_com_dylan_medias_publish_NativeMethod_native_1videoBuffer(JNIEnv* env, jobject thiz, jlong handle, jobject buffer, jint length, jlong jtimeStamp, jboolean isKey, jboolean wait) {
		if (handle == 0)return;
		CBasePublisher* publiser((CBasePublisher*)handle);
		unsigned char* data = (unsigned char*)env->GetDirectBufferAddress(buffer);
		//wli("video=%02x%02x%02x%02x%02x, ts=%ld", data[0], data[1], data[2], data[3], data[4], (long)jtimeStamp);
		publiser->appendVideo(data, length, (long)jtimeStamp, isKey, wait);
	}
	JNIEXPORT void JNICALL JNICALL Java_com_dylan_medias_publish_NativeMethod_native_1audioFrame(JNIEnv* env, jobject thiz, jlong handle, jbyteArray jdatas, jint length, jlong jtimeStamp, jboolean wait) {
		if (handle == 0)return;
		CBasePublisher* publiser((CBasePublisher*)handle);
		jbyte* data1(env->GetByteArrayElements(jdatas, JNI_FALSE));
		unsigned char* data((unsigned char*)data1);
		//wli("audio=%02x%02x%02x%02x%02x, ts=%ld", data[0], data[1], data[2], data[3], data[4], (long)jtimeStamp);
		publiser->appendAudio(data, length, (long)jtimeStamp, wait);
		env->ReleaseByteArrayElements(jdatas, data1, 0);
	}
	JNIEXPORT void JNICALL JNICALL Java_com_dylan_medias_publish_NativeMethod_native_1audioBuffer(JNIEnv* env, jobject thiz, jlong handle, jobject buffer, jint length, jlong jtimeStamp, jboolean wait) {
		if (handle == 0)return;
		CBasePublisher* publiser((CBasePublisher*)handle);
		unsigned char* data = (unsigned char*)env->GetDirectBufferAddress(buffer);
		publiser->appendAudio(data, length, (long)jtimeStamp, wait);
	}
	
	JNIEXPORT void JNICALL JNICALL Java_com_dylan_medias_publish_NativeMethod_native_1stop(JNIEnv* env, jobject thiz, jlong handle) {
		if (handle == 0)return;
		CBasePublisher* publiser((CBasePublisher*)handle);
		publiser->disconnect();
	}
	JNIEXPORT void JNICALL JNICALL Java_com_dylan_medias_publish_NativeMethod_native_1close(JNIEnv* env, jobject thiz, jlong handle) {
		if (handle == 0)return;
		CBasePublisher* publiser((CBasePublisher*)handle);
		publiser->cleanup();
		delete publiser;
	}
}
