#pragma once
#include <stdint.h>
#include <mutex>
#include "xstring.h"
#include "SmartNal.h"
#include "SmartPtr.h"
#include "SmartArr.h"

struct IFlvWriterCallback {
    virtual void    onFlv(const unsigned char* data, const int size) = 0;
};

class CFlvWriter
{
public:
	CFlvWriter(void);
	~CFlvWriter(void);

public:
    enum CodecType{aac, mp3, avc};

public:
	void			SetCallback(IFlvWriterCallback* callback);
	void			SetOutputUrl(LPCTSTR url);
	void			SetVideoFormat(const int width, const int height, const float fps, const int bitrate, const unsigned char* sps, const int spsSize, const unsigned char* pps, const int ppsSize);
	void			SetAudioFormat(const CodecType codec, const int sampleRate, const int channels, const int bitWidth, const unsigned char* esds = NULL, const int esdsSize = 0);
	bool			StartWrite();
	void			WriteVideoFrame(const unsigned char* data, const int size, const int timecode, bool isKey);
	void			WriteAudioFrame(const unsigned char* data, const int size, const int timecode);
	void			StopWrite();

protected:
	bool			PackHeader();
	bool			PackMetadata();

protected:
	unsigned char*  write_string(unsigned char* buffer, const char* rhs);	
	unsigned char*  write_string_no_marker(unsigned char* buffer,const char* rhs);	
	unsigned char*  write_double(unsigned char* buffer, double rhs);	
	unsigned char*  write_bool(unsigned char* buffer, int rhs);	
	unsigned char*  write_8(unsigned char* buffer, unsigned int v);	
	unsigned char*  write_16(unsigned char* buffer, unsigned int v);	
	unsigned char*  write_24(unsigned char* buffer, unsigned int v);	
	unsigned char*  write_32(unsigned char* buffer, uint32_t v);	
	unsigned char*  write_64(unsigned char* buffer, uint64_t v);

	unsigned char*	flv_tag_start(unsigned char* pBuffer,unsigned char tag_type, unsigned int timestamp,int nStreamID);
	unsigned char*	flv_tag_end(unsigned char* pBuffer,int data_size);

	void			check_make_path(std::xtstring outputFile);
	void			writeFlvData(unsigned char* lpdata, int nlen);

protected:
	int				write_aac_sequence_header();
	int				write_avc_sequence_header();

protected:
	enum {
		amf0_number_marker       = 0x00,
		amf0_boolean_marker      = 0x01,
		amf0_string_marker       = 0x02,
		amf0_object_marker       = 0x03,
		amf0_movieclip_marker    = 0x04,
		amf0_null_marker         = 0x05,
		amf0_undefined_marker    = 0x06,
		amf0_reference_marker    = 0x07,
		amf0_ecma_array_marker   = 0x08,
		amf0_object_end_marker   = 0x09,
		amf0_strict_array_marker = 0x0a,
		amf0_date_marker         = 0x0b,
		amf0_long_string_marker  = 0x0c,
		amf0_unsupported_marker  = 0x0d,
		amf0_recordset_marker    = 0x0e,
		amf0_xml_document_marker = 0x0f,
		amf0_typed_object_marker = 0x10
	};

protected:
	typedef CSmartNal<unsigned char> CByteNal;
 
private:
	IFlvWriterCallback* m_lpCallback;
	std::xtstring m_strOutputFile;
    std::mutex m_mtxFileFlv;
	FILE* m_fileFlv;
	bool m_started;

	CSmartArr<unsigned char> m_saVideoCache;
	int m_szVideoCache;
	CSmartArr<unsigned char> m_saAudioCache;
	int m_szAudioCache;

	int m_nVideoFrame;
	int m_nAudioFrame;
	int m_nLatestAudio;
	int m_nLatestVideo;

	struct {
		bool valid;
		int width;
		int height;
		float fps;
		int bitrate;
		CByteNal sps;
		CByteNal pps;
	} m_fmtVideo;
	struct {
		bool valid;
		CodecType codec;
		int channels;
		int sampleRate;
		CByteNal esds;
	} m_fmtAudio;
};

