package com.dylan.medias.stream;

import android.media.MediaFormat;
import android.util.Log;

import com.dylan.medias.codec.MxAvcConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class MxStreamReader implements Runnable {

	enum Codec{None, Avc, Hevc, Aac, Mp3, Alaw}
	public interface Callback {
		void onPrepered();
		void onVideoFrame(long timecode, byte[] data, int offset, int size, boolean key);
		void onAudioFrame(long timecode, byte[] data, int offset, int size);
		void onReconnecting();
		void onReconnected();
		void onStop();
		void onError(String reason);
	}

	private String streamUrl = null;
	private boolean tcpOnly = true;
	private boolean autoRetry = false;
	private Thread workThread = null;
	private boolean willQuit = false;
	private long nativeHandle = 0;
	private Callback callback = null;
	private MediaFormat videoFormat = null;
	private MediaFormat audioFormat = null;

	public MxStreamReader setTcpOnly(boolean tcpOnly) {
		this.tcpOnly = tcpOnly;
		return this;
	}
	public MxStreamReader setAutoRetry(boolean autoRetry) {
		this.autoRetry = autoRetry;
		return this;
	}
	public MxStreamReader setCallback(Callback callback) {
		this.callback = callback;
		return this;
	}

	public MxStreamReader open(String url) {
		stop();
		streamUrl = url;
		willQuit = false;
		videoFormat = null;
		audioFormat = null;
		workThread = new Thread(this);
		workThread.start();
		return this;
	}
	public boolean isWorking() {
		return workThread != null && workThread.isAlive();
	}
	public MediaFormat video() {
        if (videoFormat != null) resetFormatBuffer(videoFormat);
		return videoFormat;
	}
	public MediaFormat audio() {
        if (audioFormat != null) resetFormatBuffer(audioFormat);
		return audioFormat;
	}
	public MxStreamReader stop() {
		try {
			willQuit = true;
			if (nativeHandle != 0) {
				NativeMethod.native_interrupt(nativeHandle);
			}
			if (workThread != null) {
				workThread.interrupt();
				workThread.join();
			}
			if (nativeHandle != 0) {
				NativeMethod.native_close(nativeHandle);
				nativeHandle = 0;
			}
		} catch (Exception e) {
		}
		return this;
	}

	@Override
	public void run() {
		final String error = runIn();
		if (nativeHandle != 0) {
			NativeMethod.native_interrupt(nativeHandle);
			NativeMethod.native_close(nativeHandle);
			nativeHandle = 0;
		}
		if (error != null && !willQuit) {
			if (callback != null) callback.onError(error);
		} else if (!willQuit) {
			if (callback != null) callback.onStop();
		}
	}

	@SuppressWarnings("unused")
	private String pareseAvcc(byte[] config) {
        if (config == null || config.length < 4) return "Invalid avc config.";
        if (config[0] != 0x01) return "Invalid avCc config.";
        int offset = 0;
        int configurationVersion = config[offset++];
        int profileIndication = config[offset++];
        int profile_compatibility = config[offset++];
        int levelIndication = config[offset++];
        int lengthSizeMinusOne = config[offset++] & 0x03;
        byte[] sps = null;
        byte[] pps = null;

        int numOfSequenceParameterSets = config[offset++] & 0x1f;
        if (numOfSequenceParameterSets > 0) {
            int offsetTmp = offset;
            int spsSize = 0;
            for (int i = 0; i < numOfSequenceParameterSets; i++) {
                int sequenceParameterSetLength = (config[offset++] << 8) + config[offset++];
                offset += sequenceParameterSetLength;
                spsSize += 4 + sequenceParameterSetLength;
            }
            offset = offsetTmp;
            sps = new byte[spsSize];
            int spsOffset = 0;
            final byte[] nalHeader = {0x00, 0x00, 0x00, 0x01};
            for (int i = 0; i < numOfSequenceParameterSets; i++) {
                int sequenceParameterSetLength = (config[offset++] << 8) + config[offset++];
                System.arraycopy(nalHeader, 0, sps, spsOffset, nalHeader.length);
                spsOffset += 4;
                System.arraycopy(config, offset, sps, spsOffset, sequenceParameterSetLength);
                offset += sequenceParameterSetLength;
                spsOffset += sequenceParameterSetLength;
            }
        }

        int numOfPictureParameterSets = config[offset++];
        if (numOfPictureParameterSets > 0) {
            int offsetTmp = offset;
            int ppsSize = 0;
            for (int i = 0; i < numOfPictureParameterSets; i++) {
                int sequenceParameterSetLength = (config[offset++] << 8) + config[offset++];
                offset += sequenceParameterSetLength;
                ppsSize += 4 + sequenceParameterSetLength;
            }
            offset = offsetTmp;
            pps = new byte[ppsSize];
            int ppsOffset = 0;
            final byte[] nalHeader = {0x00, 0x00, 0x00, 0x01};
            for (int i = 0; i < numOfPictureParameterSets; i++) {
                int sequenceParameterSetLength = (config[offset++] << 8) + config[offset++];
                System.arraycopy(nalHeader, 0, pps, ppsOffset, nalHeader.length);
                ppsOffset += 4;
                System.arraycopy(config, offset, pps, ppsOffset, sequenceParameterSetLength);
                offset += sequenceParameterSetLength;
                ppsOffset += sequenceParameterSetLength;
            }
        }
        MxAvcConfig parser = new MxAvcConfig();
        if (!parser.parse(sps)) return "Parse sps sequence failed.";
        videoFormat = MediaFormat.createVideoFormat("video/avc", parser.getWidth(), parser.getHeight());
        if (sps != null)videoFormat.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
        if (pps != null)videoFormat.setByteBuffer("csd-1", ByteBuffer.wrap(pps));
        markFormatBuffer(videoFormat);
        return null;
    }
	private String parseAvcConfig(byte[] config) {
		if (config == null || config.length < 4) return "Invalid avc config.";
        if (config[0] != 0x00 || config[1] != 0x00) return pareseAvcc(config);
		byte[] sps = null;
		byte[] pps = null;
		int begin = -1;
		for (int i = 0; i < config.length - 3; i++) {
			if (config[i + 0] == 0x00 && config[i + 1] == 0x00 && ((config[i + 2] == 0x00 && config[i + 3] == 0x01) || config[i + 2] == 0x01)) {
				if (begin == -1) {
					begin = i;
				} else {
					byte type = config[begin + 2] == 0x01 ? config[begin + 3] : config[begin + 4];
					int len = i - begin;
					if ((type & 0x1f) == 0x07) {
						sps = new byte[len];
						System.arraycopy(config, begin, sps, 0, len);
					} else if ((type & 0x1f) == 0x08) {
						pps = new byte[len];
						System.arraycopy(config, begin, pps, 0, len);
					}
					begin = i;
				}
				i += 3;
			}
		}
		if (config.length - begin > 4) {
			byte type = config[begin + 2] == 0x01 ? config[begin + 3] : config[begin + 4];
			int len = config.length - begin;
			if ((type & 0x1f) == 0x07) {
				sps = new byte[len];
				System.arraycopy(config, begin, sps, 0, len);
			} else if ((type & 0x1f) == 0x08) {
				pps = new byte[len];
				System.arraycopy(config, begin, pps, 0, len);
			}
		}
		MxAvcConfig parser = new MxAvcConfig();
		if (!parser.parse(sps)) return "Parse sps sequence failed.";
		videoFormat = MediaFormat.createVideoFormat("video/avc", 1280, 720);
		if (sps != null)videoFormat.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
		if (pps != null)videoFormat.setByteBuffer("csd-1", ByteBuffer.wrap(pps));
        markFormatBuffer(videoFormat);
		return null;
	}
	private String parseHvcCConfig(byte[] config) {
		return "Unsupport HvcC mode.";
	}
	private String parseHevcConfig(byte[] config) {
		if (config == null || config.length < 4) return "Invalid hevc config.";
		if (config[0] != 0x00 || config[1] != 0x00) return parseHvcCConfig(config);
		videoFormat = MediaFormat.createVideoFormat("video/hevc", 1280, 720);
		videoFormat.setByteBuffer("csd-0", ByteBuffer.wrap(config));
		markFormatBuffer(videoFormat);
		return null;
	}
	void markFormatBuffer(MediaFormat format) {
        if (format == null) return;
        if (format.containsKey("csd-0")) format.getByteBuffer("csd-0").mark();
        if (format.containsKey("csd-1")) format.getByteBuffer("csd-1").mark();
    }
    void resetFormatBuffer(MediaFormat format) {
        if (format == null) return;
        if (format.containsKey("csd-0")) format.getByteBuffer("csd-0").reset();
        if (format.containsKey("csd-1")) format.getByteBuffer("csd-1").reset();
    }
	private String parseAacConfig(byte[] config) {
		if (config == null || config.length < 2) {
			audioFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", 0, 0);
			return null;
		}
		int frequency = ((config[0] << 1) & 0x1e) + ((config[1] >> 7) & 0x01);
		int channel = (config[1] & 0x78) >> 3;
		final int samplerates[] = {96000, 88200, 64000, 48000, 44100, 32000, 24000, 22050, 16000, 12000, 11025, 8000, 7350};
		if (frequency < 0 || frequency >= samplerates.length) return "Invalid audio frequency index.";
		audioFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", samplerates[frequency], channel);
		audioFormat.setInteger(MediaFormat.KEY_IS_ADTS, 0);
		audioFormat.setByteBuffer("csd-0", ByteBuffer.wrap(config));
        markFormatBuffer(audioFormat);
		return null;
	}
	private String parseMp3Config(byte[] config) {
		audioFormat = MediaFormat.createAudioFormat("audio/mpeg", 0, 0);
		return null;
	}
	private String parseAlamConfig(byte[] config) {
		audioFormat = MediaFormat.createAudioFormat("audio/g711-alaw", 8000, 1);
		return null;
	}
	private String runIn() {
		int retryCount = 0;
		boolean firstConnected = true;
		int[] sleepInterval = new int[]{0, 500, 1000, 2000, 4000, 8000};
		do {
			if (nativeHandle != 0) {
				NativeMethod.native_interrupt(nativeHandle);
				NativeMethod.native_close(nativeHandle);
				nativeHandle = 0;
			}
			if (retryCount > 0) {
				sleep(sleepInterval[retryCount % sleepInterval.length]);
				if (callback != null && retryCount == 1) callback.onReconnecting();
				retryCount++;
			}
			nativeHandle = NativeMethod.native_init();
			if (nativeHandle == 0) return "Open native handle failed.";
			if (!NativeMethod.native_open(nativeHandle, streamUrl, tcpOnly)) {
				if (autoRetry && retryCount != 0) continue;
				else return "Open rtsp stream failed.";
			}
			if (willQuit) return null;
			int videoIndex = NativeMethod.native_videoIndex(nativeHandle);
			int audioIndex = NativeMethod.native_audioIndex(nativeHandle);
			int videoCodec = NativeMethod.native_videoCodec(nativeHandle);
			int audioCodec = NativeMethod.native_audioCodec(nativeHandle);
			if (videoIndex == -1) return "Not found video track in stream.";
			if (videoCodec == NativeMethod.Codec_Avc) {
				String error = parseAvcConfig(NativeMethod.native_videoExtraData(nativeHandle));
				if (error != null) return error;
			} else if (videoCodec == NativeMethod.Codec_Hevc) {
				String error = parseHevcConfig(NativeMethod.native_videoExtraData(nativeHandle));
				if (error != null) return error;
			} else {
				videoIndex = -1;
			}
			if (audioCodec == NativeMethod.Codec_Aac) {
				String error = parseAacConfig(NativeMethod.native_audioExtraData(nativeHandle));
				if (error != null) return error;
			} else if (audioCodec == NativeMethod.Codec_Mp3) {
				String error = parseMp3Config(NativeMethod.native_audioExtraData(nativeHandle));
				if (error != null) return error;
			} else if (audioCodec == NativeMethod.Codec_Alaw) {
				String error = parseAlamConfig(NativeMethod.native_audioExtraData(nativeHandle));
				if (error != null) return error;
			} else {
				audioIndex = -1;
			}
			if (callback != null) {
				if (firstConnected) callback.onPrepered();
				else callback.onReconnected();
			}
			retryCount = 1;
			while (!willQuit) {
				byte[] data = NativeMethod.native_read(nativeHandle);
				if (data == null || data.length < 12) {
					if (autoRetry) break;
					else return "Read frame failed.";
				}
				if (willQuit) break;
				int header = lhInt(data, 0);
				int index = header & 0x00ffffff;
				boolean key = ((header >> 24) & 0xff) != 0;
				long timeStamp = lhLong(data, 4);
				if (index == videoIndex) {
					onVideoFrame(timeStamp, data, 12, data.length - 12, key);
				} else if (index == audioIndex) {
					onAudioFrame(timeStamp, data, 12, data.length - 12);
				}
			}
		} while (autoRetry && !willQuit);
		return null;
	}
	private static void sleep(int ms) {
		try { Thread.sleep(ms); } catch (Exception e) { }
	}
	private int nalType(byte[] datas, int offset) {
		if (datas[offset + 2] == 0x01) return datas[offset + 3] & 0x1f;
		else return datas[offset + 4] & 0x1f;
	}
	private void onVideoFrame(long timecode, byte[] datas, int offset, int length, boolean key) {
		int begin = -1;
		for (int i = offset; i < offset + length - 3; i++) {
			if (datas[i + 0] == 0x00 && datas[i + 1] == 0x00 && ((datas[i + 2] == 0x00 && datas[i + 3] == 0x01) || datas[i + 2] == 0x01)) {
				if (begin == -1) {
					begin = i;
				} else {
					if (callback != null && nalType(datas, begin) <= 0x05) callback.onVideoFrame(timecode, datas, begin, i - begin, key);
					begin = i;
				}
				i += 3;
			}
		}
		if (begin >= 0 && begin < (offset + length - 4)) {
			if (callback != null/* && nalType(datas, begin) <= 0x05*/) callback.onVideoFrame(timecode, datas, begin, offset + length - begin, key);
		}
	}
	private void onAudioFrame(long timecode, byte[] data, int offset, int size) {
		if (callback != null) callback.onAudioFrame(timecode, data, offset, size);
	}
    private static int lhInt(byte[] from, int offset) {
        return (from[offset + 0] & 0xFF) | ((from[offset + 1] & 0xFF) << 8) | ((from[offset + 2] & 0xFF) << 16) | ((from[offset + 3] & 0xFF) << 24);
    }
    private static long lhLong(byte[] from, int offset) {
        return (from[offset + 0] & 0xFFL) | ((from[offset + 1] & 0xFFL) << 8) | ((from[offset + 2] & 0xFFL) << 16) | ((from[offset + 3] & 0xFFL) << 24) | ((from[offset + 4] & 0xFFL) << 32) | ((from[offset + 5] & 0xFFL) << 40) | ((from[offset + 6] & 0xFFL) << 48) | ((from[offset + 7] & 0xFFL) << 56);
    }
}
