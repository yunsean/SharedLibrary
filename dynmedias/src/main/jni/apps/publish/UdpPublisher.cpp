#include <stdio.h>
#ifndef _WIN32
#include <android/log.h>
#endif
#include <arpa/inet.h>
#include "UdpPublisher.h"
#include "xsystem.h"
#undef LOG_TAG
#define LOG_TAG	"dylan.publish.stream"
#include "WriteLog.h"
#include "log.h"
#include "AVCConfig.h"
#include "Byte.h"

const static char*	C_STATISTICS_METHOD("statistics");
const static char*	C_STATISTICS_PARAMETER("(Ljava/lang/Object;JJI)V");
const static char*	C_ONERROR_METHOD("onError");
const static char*	C_ONERROR_PARAMETER("(Ljava/lang/Object;)V");

CUdpPublisher::CUdpPublisher()
	: mFormatContext(avformat_free_context)
	, mVideoStream(nullptr)
	, mAudioStream(nullptr)
	, mVideoTrackIndex(-1)
	, mAudioTrackIndex(-1)

#ifndef _WIN32
    , mCallback()
    , mStatistics(NULL)
    , mOnError(NULL)
#endif
{
}
CUdpPublisher::~CUdpPublisher() {
    cleanup();
}

bool CUdpPublisher::init(JNIEnv* env, jobject thiz, jobject weak_thiz){
	if (!mCallback.init(env, thiz, weak_thiz)) return wlet(false, _T("Initialze callback failed."));
	mStatistics = mCallback.bindStaticMethod(C_STATISTICS_METHOD, C_STATISTICS_PARAMETER);
	mOnError = mCallback.bindStaticMethod(C_ONERROR_METHOD, C_ONERROR_PARAMETER);

	av_register_all();
	avformat_network_init();
	AVFormatContext* formatContext = nullptr;
	int result = avformat_alloc_output_context2(&formatContext, NULL, "mpegts", NULL);
    if (result < 0) return wlet(false, "Could not open output file.");
    mFormatContext.Attach(formatContext);
	return true;
}
void CUdpPublisher::setupVideo(const int width, const int height, const int bitrate, const unsigned char* sps, const int szSps, const unsigned char* pps, const int szPps) {
    CAVCConfig config;
    config.AddSps(sps, szSps);
    config.AddPps(pps, szPps);
    CSmartNal<unsigned char> extradata;
    config.Serialize(extradata);
	wli("sps=%s, pps=%s", CByte::toHex(sps, szSps, 4).c_str(), CByte::toHex(pps, szPps, 4).c_str());
    wli("setupVideo(w=%d,h=%d,br=%d,avCc=%s(%dbytes))", width, height, bitrate, CByte::toHex(extradata.GetArr(), extradata.GetSize(), 4).c_str(), extradata.GetSize());

    AVCodec* codec = avcodec_find_encoder(AV_CODEC_ID_H264);
	AVStream* stream = avformat_new_stream(mFormatContext, codec);
	AVCodecContext* codecContext = avcodec_alloc_context3(codec);
	codecContext->codec_type = AVMEDIA_TYPE_VIDEO;
	codecContext->codec_id = AV_CODEC_ID_H264;
	codecContext->width = width;
	codecContext->height = height;
	codecContext->pix_fmt = AV_PIX_FMT_YUV420P;
	codecContext->gop_size = 12;
	codecContext->time_base = AVRational{ 1, 30 };
	codecContext->bit_rate = bitrate;
	CSmartArr<uint8_t> data(std::max(AV_INPUT_BUFFER_PADDING_SIZE, szSps));
	memcpy(data, sps, szSps);
	codecContext->extradata = extradata.GetArr();
	codecContext->extradata_size = extradata.GetSize();
	avcodec_parameters_from_context(stream->codecpar, codecContext);
	stream->time_base = AVRational{ 1, 1000 };
	mVideoStream = stream;
	mVideoTrackIndex = stream->index;
}
void CUdpPublisher::setupAudio(int channels, int sampleRate, const unsigned char* esds, const int szEsds) {
    wli("setupAudio(c=%d,sr=%d,esds=%s(%dbytes))", channels, sampleRate, CByte::toHex(esds, szEsds, 4).c_str(), szEsds);
    AVCodec* codec = avcodec_find_encoder(AV_CODEC_ID_AAC);
	AVStream* stream = avformat_new_stream(mFormatContext, codec);
	AVCodecContext* codecContext = avcodec_alloc_context3(codec);
	codecContext->codec_type = AVMEDIA_TYPE_AUDIO;
	codecContext->codec_id = AV_CODEC_ID_AAC;
	codecContext->channels = channels;
	codecContext->sample_rate = sampleRate;
	codecContext->sample_fmt = AV_SAMPLE_FMT_S16;
	CSmartArr<uint8_t> data(std::max(AV_INPUT_BUFFER_PADDING_SIZE, szEsds));
    memcpy(data, esds, szEsds);
	codecContext->extradata = data;
	codecContext->extradata_size = szEsds;
	avcodec_parameters_from_context(stream->codecpar, codecContext);
	stream->time_base = AVRational{ 1, 1000 };
	mAudioStream = stream;
	mAudioTrackIndex = stream->index;
}

void CUdpPublisher::appendVideo(const unsigned char* datas, int length, long timecode, bool isKey, bool waitBlock) {
    static AVRational time_base_q = { 1, 1000 };
    mPacket.data = const_cast<unsigned char*>(datas);
	*(uint32_t*)mPacket.data = htonl(length - 4);
    mPacket.size = length;
    mPacket.stream_index = mVideoTrackIndex;
    mPacket.pts = av_rescale_q(timecode, time_base_q, mVideoStream->time_base);
    wli("av_interleaved_write_frame(audio=%d, size=%s(%dbytes), pts=%ld, %lld)", mPacket.stream_index, CByte::toHex(datas, 10, 4).c_str(), length, timecode, mPacket.pts);
    if (av_interleaved_write_frame(mFormatContext, &mPacket) < 0) {
        wle("av_interleaved_write_frame(audio=false, size=%d, pts=%lld) failed", length, mPacket.pts);
    }
    av_packet_unref(&mPacket);
}
void CUdpPublisher::appendAudio(const unsigned char* datas, int length, long timecode, bool waitBlock) {
    static AVRational time_base_q = { 1, 1000 };
    mPacket.data = const_cast<unsigned char*>(datas);
    mPacket.size = length;
    mPacket.stream_index = mAudioTrackIndex;
    mPacket.pts = av_rescale_q(timecode, time_base_q, mAudioStream->time_base);
    wli("av_interleaved_write_frame(audio=%d, size=%d, pts=%ld, %lld)", mPacket.stream_index, length, timecode, mPacket.pts);
    if (av_interleaved_write_frame(mFormatContext, &mPacket) < 0) {
        wle("av_interleaved_write_frame(audio=true, size=%d, pts=%lld) failed", length, mPacket.pts);
    }
    av_packet_unref(&mPacket);
}
bool CUdpPublisher::connect(const char* url){
    wli("connecting to %s", url);
    if (!(mFormatContext->oformat->flags & AVFMT_NOFILE)) {
        int result = avio_open(&mFormatContext->pb, url, AVIO_FLAG_WRITE);
        if (result < 0) return wlet(false, "Could not open output %s", url);
    }
    wli("connected to %s", url);
    av_dump_format(mFormatContext, 0, url, 1);
    wli("will writer header to %s", url);
    int result = avformat_write_header(mFormatContext, NULL);
    if (result < 0) return wlet(false, "Error occurred when opening output %s", url);
    av_init_packet(&mPacket);
	return true;
}
void CUdpPublisher::disconnect() {
}
void CUdpPublisher::cleanup() {
	mFormatContext = nullptr;
}

