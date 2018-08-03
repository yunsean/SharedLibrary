#include <string.h>
#include <stdio.h>
#ifdef _WIN32
#include <direct.h> 
#include <io.h>
#pragma comment(lib,"ws2_32.lib")
#else
#include <sys/stat.h>
#include <arpa/inet.h>
#include<unistd.h>
#endif
#include "FlvWriter.h"
#include "xchar.h"
#include "mp4av_h264.h"
#include "WriteLog.h"
#include "AACUtility.h"

#define FLV_AAC_SEQUENCE_HEADER     (0)
#define FLV_AAC_RAW                 (1)
#define FLV_AVC_SEQUENCE_HEADER     (0)
#define FLV_AVC_NALU                (1)

#define AUDIO_FLV                   (8)
#define VIDEO_FLV                   (9)
#define SCRIPTDATA                  (18)

#define VideoCode_AVC               (7)

CFlvWriter::CFlvWriter(void)
	: m_lpCallback()
	, m_strOutputFile()
	, m_fileFlv(NULL)
	, m_started(false)

	, m_saVideoCache()
	, m_szVideoCache(0)
	, m_saAudioCache()
	, m_szAudioCache(0)

	, m_nVideoFrame(0)
	, m_nAudioFrame(0)
	, m_nLatestAudio(0)
	, m_nLatestVideo(0) {
	memset(&m_fmtVideo, 0, sizeof(m_fmtVideo));
	memset(&m_fmtAudio, 0, sizeof(m_fmtAudio));
}
CFlvWriter::~CFlvWriter(void) {
    StopWrite();
}

void CFlvWriter::SetCallback(IFlvWriterCallback* callback) {
	m_lpCallback			= callback;
}
void CFlvWriter::SetOutputUrl(LPCTSTR url) {
	m_strOutputFile			= url;
}

void CFlvWriter::SetVideoFormat(const int width, const int height, const float fps, const int bitrate, const unsigned char* sps, const int spsSize, const unsigned char* pps, const int ppsSize) {
	m_fmtVideo.valid = true;
	m_fmtVideo.width = width;
	m_fmtVideo.height = height;
	m_fmtVideo.fps = fps;
	m_fmtVideo.bitrate = bitrate;
	int sps_header_len(sps[2] == 0x01 ? 3 : 4);
	int pps_header_len(pps[2] == 0x01 ? 3 : 4);
	m_fmtVideo.sps.Copy(sps + sps_header_len, spsSize - sps_header_len);
	m_fmtVideo.pps.Copy(pps + pps_header_len, ppsSize - pps_header_len);
}
void CFlvWriter::SetAudioFormat(const CodecType codec, const int sampleRate, const int channels, const int bitWidth, const unsigned char* esds /* = NULL */, const int esdsSize /* = 0 */) {
	m_fmtAudio.valid = true;
	m_fmtAudio.codec = codec;
	m_fmtAudio.channels = channels;
	m_fmtAudio.sampleRate = sampleRate;
	if (esds != NULL && esdsSize > 0) {
		m_fmtAudio.esds.Copy(esds, esdsSize);
	}
}

bool CFlvWriter::StartWrite() {
	if (m_fmtAudio.codec != aac && m_fmtAudio.codec != mp3)
		return wlet(false, _T("The audio type is error!"));
	if (m_strOutputFile.GetLength() > 0) {
		check_make_path(m_strOutputFile);
		if ((m_fileFlv = _tfopen(m_strOutputFile, _T("wb"))) == NULL) {
			return wlet(false, _T("Open file failed:%s"), m_strOutputFile.c_str());
		}
	}
	if (!PackHeader()) {
		return wlet(false, _T("PackHeader() failed."));
	}
	if (!PackMetadata()) {
		return wlet(false, _T("PackMetadata() failed."));
	}
	if (m_fmtVideo.valid) {
		write_avc_sequence_header();
	}
	if (m_fmtAudio.valid && m_fmtAudio.codec == aac) {
		write_aac_sequence_header();
	}
	m_started = true;
	return true;
}

void CFlvWriter::WriteVideoFrame(const unsigned char* data, const int size, const int timecode, bool isKey) {
	if (m_started) {
		if (size + 1024 > m_szVideoCache) {
			m_saVideoCache = new unsigned char[size + 64 * 1024];
			m_szVideoCache = size + 64 * 1024;
		}
		unsigned char* lpFrameCache(m_saVideoCache + 16);
		int nFrameSize(0);
		int nNalSize(htonl(size - 4));
		memcpy(lpFrameCache + nFrameSize, &nNalSize, 4);
		nFrameSize += 4;
		memcpy(lpFrameCache + nFrameSize, data + 4, size - 4);
		nFrameSize += size - 4;
		static uint32_t latest(0);
		if (timecode - latest > 100) {
			LOGW("timecode error: %u -> %u", latest, timecode);
		}
		latest = timecode;
		unsigned char* lpTagCache(flv_tag_start(m_saVideoCache, VIDEO_FLV, timecode, 0));
		unsigned int is_keyframe(isKey ? 1 : 0);
		unsigned int codec_id(int(VideoCode_AVC));
		write_8(lpTagCache, ((is_keyframe ? 1 : 2) << 4) | codec_id);
		write_8(lpTagCache + 1, FLV_AVC_NALU);
		write_24(lpTagCache + 2, 0);
		lpTagCache += 5;
		int nTagSize(nFrameSize);
		nTagSize += 16;
		lpTagCache = flv_tag_end(m_saVideoCache, nTagSize);
		nTagSize += 4;
		writeFlvData(m_saVideoCache, nTagSize);
	}
}
void CFlvWriter::WriteAudioFrame(const unsigned char* data, const int size, const int timecode) {
	if (m_started) {
		if (size + 1024 > m_szAudioCache) {
			m_saAudioCache = new unsigned char[size * 2 + 1024];
			m_szAudioCache = size * 2 + 1024;
		}
		m_nAudioFrame++;
		if ((m_nAudioFrame - m_nLatestAudio) * 1024 * 1000 / m_fmtAudio.sampleRate > 1000) {
			m_nLatestAudio = m_nAudioFrame;
		}

		unsigned char* lpCache(m_saAudioCache);
		lpCache = flv_tag_start(m_saAudioCache, AUDIO_FLV, (uint32_t)timecode, 1);
		if (m_fmtAudio.codec == mp3) {
			write_8(lpCache, 0x2f);
			memcpy(lpCache, data, size);
			lpCache += size;
		} else {
			write_8(lpCache, 0xaf);
			write_8(lpCache + 1, FLV_AAC_RAW);
			lpCache += 2;
			memcpy(lpCache, data, size);
			lpCache += size;
		}
		lpCache = flv_tag_end(m_saAudioCache, lpCache - m_saAudioCache);
		writeFlvData(m_saAudioCache, lpCache - m_saAudioCache);
	}
}

void CFlvWriter::StopWrite() {
	m_started = false;
	if (m_fileFlv) {
		fclose(m_fileFlv);
		m_fileFlv = NULL;
	}
}

bool CFlvWriter::PackHeader() {
	unsigned char				flags(0);
	if (m_fmtVideo.valid)		flags += (1 << 0);
	if (m_fmtAudio.valid)		flags += (1 << 2);
	char						flv_header[] = { 'F', 'L', 'V', 0x01, (char)flags, 0x00, 0x00, 0x00, 0x09, 0x00, 0x00, 0x00, 0x00};
	writeFlvData((unsigned char*)flv_header, 13);
	return true;
}
bool CFlvWriter::PackMetadata() {
	CSmartArr<unsigned char>saCache(new unsigned char[8192 + 1024]);
	unsigned char* lpCache(flv_tag_start(saCache, SCRIPTDATA, 0, 0));
	lpCache = write_string(lpCache, "onMetaData");
    lpCache = write_8(lpCache, amf0_ecma_array_marker);
    lpCache = write_32(lpCache, 10);
	lpCache = write_string_no_marker(lpCache, "author");
	lpCache = write_string(lpCache, "yunsean");
	lpCache = write_string_no_marker(lpCache, "copyright");
	lpCache = write_string(lpCache, "yunsean");
	lpCache = write_string_no_marker(lpCache, "description");
	lpCache = write_string(lpCache, " ");
	lpCache = write_string_no_marker(lpCache, "keywords");
	lpCache = write_string(lpCache, " ");
	lpCache = write_string_no_marker(lpCache, "rating");
	lpCache = write_string(lpCache, " ");
	lpCache = write_string_no_marker(lpCache, "title");
	lpCache = write_string(lpCache, " ");
 
	lpCache = write_string_no_marker(lpCache, "presetname");
	lpCache = write_string(lpCache, "Custom");
	lpCache = write_string_no_marker(lpCache, "creationdate");
	lpCache = write_string(lpCache, "");

	lpCache = write_string_no_marker(lpCache, "videodevice");
	lpCache = write_string(lpCache, "Android camera");

	lpCache = write_string_no_marker(lpCache, "framerate");
	lpCache = write_double(lpCache, m_fmtVideo.fps);
	lpCache = write_string_no_marker(lpCache, "width");
	lpCache = write_double(lpCache, m_fmtVideo.width);
	lpCache = write_string_no_marker(lpCache, "height");
	lpCache = write_double(lpCache, m_fmtVideo.height);
	lpCache = write_string_no_marker(lpCache, "videocodecid");
	lpCache = write_double(lpCache, (double)7);
	lpCache = write_string_no_marker(lpCache, "videodatarate");
	lpCache = write_double(lpCache, (double)m_fmtVideo.bitrate);

	lpCache = write_string_no_marker(lpCache, "audiodevice");
	lpCache = write_string(lpCache, "(High Definition Audio )");
	lpCache = write_string_no_marker(lpCache, "audiodatarate");
	lpCache = write_double(lpCache, (double)m_fmtAudio.channels * m_fmtAudio.sampleRate * 16 / 8 * 8 / 1024);
	lpCache = write_string_no_marker(lpCache, "audiochannels");
	lpCache = write_double(lpCache, (double)m_fmtAudio.channels);

	lpCache = write_string_no_marker(lpCache, "stereo");
	if (m_fmtAudio.channels == 1)lpCache= write_bool(lpCache, 0);
	else lpCache = write_bool(lpCache, 1);	
	lpCache = write_string_no_marker(lpCache, "audiosamplerate");
	lpCache = write_double(lpCache, (double)m_fmtAudio.sampleRate);
	lpCache = write_string_no_marker(lpCache, "audioinputvolume");
	lpCache = write_double(lpCache, (double)75);
	lpCache = write_string_no_marker(lpCache, "audiocodecid");
    int audiocodecid(10);
    if (m_fmtAudio.codec == aac) audiocodecid = 10;
    else if (m_fmtAudio.codec == mp3) audiocodecid = 2;
	lpCache = write_double(lpCache, (double)audiocodecid);

	lpCache = write_24(lpCache, 0x000009);
	lpCache = flv_tag_end(saCache, lpCache- saCache);

	writeFlvData(saCache, lpCache - saCache);
	return true;
}

int CFlvWriter::write_aac_sequence_header() {
	CSmartArr<unsigned char>    saCache(100);
	unsigned char*				lpCache(saCache);

	if (m_fmtAudio.esds.GetSize() != 2)return -1;
	lpCache = flv_tag_start(saCache, AUDIO_FLV, 0, 0);
	lpCache = write_8(lpCache, 0xaf);
	lpCache = write_8(lpCache, FLV_AAC_SEQUENCE_HEADER);
	memcpy(lpCache, m_fmtAudio.esds.GetArr(), m_fmtAudio.esds.GetSize());
	lpCache += m_fmtAudio.esds.GetSize();
	lpCache = flv_tag_end(saCache, lpCache - saCache);

	writeFlvData(saCache, lpCache - saCache);
	return 1;
}

int CFlvWriter::write_avc_sequence_header() {
	if (m_fmtVideo.sps.GetSize() < 7)return -1;
	CSmartArr<unsigned char>saCache(m_fmtVideo.sps.GetSize() + m_fmtVideo.pps.GetSize() + 1024);
	unsigned char* lpCache(saCache);

	int profileIndication(m_fmtVideo.sps[1]);
	int profileCompatibility(m_fmtVideo.sps[2]);
	int levelIndication(m_fmtVideo.sps[3]);
	lpCache = flv_tag_start(saCache, VIDEO_FLV, 0, 0);
	lpCache = write_8(lpCache, 0x17);
	lpCache = write_8(lpCache, FLV_AVC_SEQUENCE_HEADER);
	lpCache = write_24(lpCache, 0);
	lpCache = write_8(lpCache, 1);
	lpCache = write_8(lpCache, profileIndication);
	lpCache = write_8(lpCache, profileCompatibility);
	lpCache = write_8(lpCache, levelIndication);

	lpCache[0] = 0xFF;
	lpCache[1] = 0xE1;
	lpCache += 2;
	lpCache = write_16(lpCache, m_fmtVideo.sps.GetSize());
	memcpy(lpCache, m_fmtVideo.sps.GetArr(), m_fmtVideo.sps.GetSize());
	lpCache += m_fmtVideo.sps.GetSize();

	lpCache = write_8(lpCache, 1);
	lpCache = write_16(lpCache, m_fmtVideo.pps.GetSize());
	memcpy(lpCache, m_fmtVideo.pps.GetArr(), m_fmtVideo.pps.GetSize());
	lpCache += m_fmtVideo.pps.GetSize();
	lpCache = flv_tag_end(saCache, lpCache - saCache);

	writeFlvData(saCache, lpCache - saCache);
	return 1;
}

unsigned char* CFlvWriter::write_string(unsigned char* buffer, const char* rhs) {
	size_t					size(strlen(rhs));
	if (size == 0) {
		buffer				= write_8(buffer, amf0_null_marker);
	} else if(size < 65536) {
		buffer				= write_8(buffer, amf0_string_marker);
		buffer				= write_16(buffer, size);
	} else {
		buffer				= write_8(buffer, amf0_long_string_marker);
		buffer				= write_32(buffer, size);
	}
	memcpy(buffer, rhs, size);
	buffer					+= size;
	return buffer;
}
unsigned char* CFlvWriter::write_string_no_marker(unsigned char* buffer,  const char* rhs) {
	size_t					size(strlen(rhs));
	buffer					= write_16(buffer, size);
	memcpy(buffer, rhs, size);
	buffer					+= size;
	return buffer;
}
unsigned char* CFlvWriter::write_double(unsigned char* buffer, double rhs) {
	union {
		uint64_t	integer_;
		double		double_;
	} val;
	buffer					= write_8(buffer, amf0_number_marker);
	val.double_				= rhs;
	buffer					= write_64(buffer, val.integer_);
	return buffer;
}
unsigned char* CFlvWriter::write_bool(unsigned char* buffer, int rhs) {
	buffer					= write_8(buffer, amf0_boolean_marker);
	buffer					= write_8(buffer, rhs == 0 ? 0 : 1);
	return buffer;
}
unsigned char* CFlvWriter::write_8(unsigned char* buffer, unsigned int v) {
	buffer[0]				= (uint8_t)v;
	return buffer + 1;
}
unsigned char* CFlvWriter::write_16(unsigned char* buffer, unsigned int v) {
	buffer[0]				= (uint8_t)(v >> 8);
	buffer[1]				= (uint8_t)(v >> 0);
	return buffer + 2;
}
unsigned char* CFlvWriter::write_24(unsigned char* buffer, unsigned int v) {
	buffer[0]				= (uint8_t)(v >> 16);
	buffer[1]				= (uint8_t)(v >> 8);
	buffer[2]				= (uint8_t)(v >> 0);
	return buffer + 3;
}
unsigned char* CFlvWriter::write_32(unsigned char* buffer, uint32_t v) {
	buffer[0]				= (uint8_t)(v >> 24);
	buffer[1]				= (uint8_t)(v >> 16);
	buffer[2]				= (uint8_t)(v >> 8);
	buffer[3]				= (uint8_t)(v >> 0);
	return buffer + 4;
}
unsigned char* CFlvWriter::write_64(unsigned char* buffer, uint64_t v) {
	write_32(buffer + 0, (uint32_t)(v >> 32));
	write_32(buffer + 4, (uint32_t)(v >> 0));
	return buffer + 8;
}

unsigned char* CFlvWriter::flv_tag_start(unsigned char* pBuffer, unsigned char tag_type, unsigned int timestamp, int nStreamID) {
	write_8(pBuffer + 0, tag_type);
	write_24(pBuffer + 4, timestamp);
	write_8(pBuffer + 7, timestamp >> 24);
	write_24(pBuffer + 8, nStreamID);
	pBuffer				+= 11;
	return pBuffer;
}
unsigned char* CFlvWriter::flv_tag_end(unsigned char* pBuffer, int data_size) {
	write_24(pBuffer + 1, data_size - 11);
	write_32(pBuffer + data_size, data_size);
	pBuffer				+= (data_size + 4);
	return pBuffer;
}

void CFlvWriter::check_make_path(std::xtstring outputFile) {
    std::xtstring   path(outputFile);
#ifdef WIN32
    TCHAR           ctmp('\\');
#else
    TCHAR           ctmp('/');
#endif 
    int             pos(path.Find(ctmp));
    while (pos > 0) {
        std::xtstring   parent(path.Left(pos));
        if (_taccess(parent, 0) == -1)
            _tmkdir(parent);
        pos         = path.Find(ctmp, pos + 1);
    }
}
void CFlvWriter::writeFlvData(unsigned char* lpdata, int nlen) {
    std::lock_guard<std::mutex> lock(m_mtxFileFlv);
	if (m_fileFlv != NULL) {
		fwrite(lpdata, nlen, 1, m_fileFlv);
		fflush(m_fileFlv);
	}
	if (m_lpCallback) m_lpCallback->onFlv(lpdata, nlen);
}
