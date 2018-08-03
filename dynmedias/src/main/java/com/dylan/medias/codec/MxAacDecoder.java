package com.dylan.medias.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;

public class MxAacDecoder {
	
	public interface Callback {
		void onAudioFormat(int channel, int sampleRate, int bitWidth);
		void onAudioBlock(byte[] datas, int offset, int length);
	}

	public MxAacDecoder(Callback listener) {
		mListener = listener;
	}
	public boolean startup() {
		try {
			mDecoder = MediaCodec.createDecoderByType("audio/mp4a-latm"); 
			mStarted = false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	@SuppressWarnings("deprecation")
	public boolean startup(byte[] config, int offset, int length) {
		try {
			if (config == null || length < 2) {
				Log.e("dylan", "Invalid aac config.");
				return false;
			}
			mDecoder = MediaCodec.createDecoderByType("audio/mp4a-latm");  
			if (config != null) {
				int frequency = ((config[offset] << 1) & 0x1e) + ((config[offset + 1] >> 7) & 0x01);
				int channel = (config[offset + 1] & 0x78) >> 3;
				final int samplerates[] = {96000, 88200, 64000, 48000, 44100, 32000, 24000, 22050, 16000, 12000, 11025, 8000, 7350};
				if (frequency < 0 || frequency >= samplerates.length) {
					Log.e("dylan", "Invalid audio frequency index.");
					return false;
				}
				MediaFormat mediaFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", samplerates[frequency], channel);  
				mediaFormat.setInteger(MediaFormat.KEY_IS_ADTS, 0);
				if (offset != 0 || length != config.length) {
					byte[] swap = new byte[length];
					System.arraycopy(config, offset, swap, 0, length);
					config = swap;
				}
				mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(config));
				mDecoder.configure(mediaFormat, null, null, 0);
				mDecoder.start(); 
				mInputBuffers = mDecoder.getInputBuffers();
				mOutputBuffers = mDecoder.getOutputBuffers();
				mFoundAacHeader = false;
				mPreviousLastByte = 0x00;
				mStarted = true;
			}   
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public boolean decodeAdtsBlock(byte[] datas, int offset, int length) {
		if (length < 1)return false;
		if (!mFoundAacHeader) {
			if ((mPreviousLastByte & 0xff) == 0xff && (datas[offset] & 0xf1) == 0xf1) {
				mAacCache = new byte[1024];
				mAacCache[0] = (byte) 0xff;
				mAacCacheRemain = 1;
				mFoundAacHeader = true;
			} else {
				for (int i = offset; i < offset + length - 1; i++) {
					if ((datas[i] & 0xff) == 0xff && (datas[i + 1] & 0xf1) == 0xf1) {
						length -= (i - offset);
						offset = i;
						mAacCacheRemain = 0;
						mFoundAacHeader = true;
						break;
					}
				}
			}
			if (!mFoundAacHeader) {
				mPreviousLastByte = datas[offset + length - 1];
				return true;
			}
		}
		while (length > 0) {
			if (mAacCacheRemain + length < 7)break;
			int frameLen = calcAdtsLength(mAacCache, 0, mAacCacheRemain, datas, offset, length);
			if (frameLen == -1) {
				Log.e("dylan", "Calculate adts frame length error.");
				return false;
			}
			if (mAacCacheRemain + length < frameLen)break;
			if (mAacCacheRemain > 0) {
				int consumed = frameLen - mAacCacheRemain;
				appendAacData(datas, offset, consumed);
				if (!mStarted && !startDecoder(datas, offset)) {
					Log.e("dylan", "start aac decoder failed.");
					return false;
				}
				decodeAFrame(mAacCache, 0, mAacCacheRemain);
				offset += consumed;
				length -= consumed;
				mAacCacheRemain = 0;
			} else {
				int consumed = frameLen;
				if (!mStarted && !startDecoder(datas, offset)) {
					Log.e("dylan", "start aac decoder failed.");
					return false;
				}
				decodeAFrame(datas, offset, consumed);
				offset += consumed;
				length -= consumed;
			}
		};
		if (length > 0) {
			appendAacData(datas, offset, length);
		}		
		return true;
	}
	public boolean decodeAdtsFrame(byte[] datas, int offset, int length) {
		if (!mStarted) {
			if (!startDecoder(datas, offset)) {
				Log.e("dylan", "Invalid adts header.");
				return false;
			}
		}
		while (length > 0) {
			if (((datas[offset] & 0xff) != 0xff) || ((datas[offset + 1] & 0xf0) != 0xf0)) {
				//Log.e("dylan", "invalid adts header: " + datas[offset] + "," + (datas[offset + 1] & 0xf0) + ", remain length: " + length);
				return true;
			}
			int frameLen = calcAdtsLength(datas, offset, length, null, 0, 0);
			if (frameLen == -1) {
				Log.e("dylan", "Calculate adts frame length error.");
				return false;
			}
			if (length < frameLen) {
				Log.e("dylan", "Invalid adts frame.");
				return false;
			}
			decodeAFrame(datas, offset, frameLen);
			offset += frameLen;
			length -= frameLen;
		}
		return true;
	}
	@SuppressWarnings("deprecation")
	public boolean decodeAFrame(byte[] datas, int offset, int length) {
		if (!mStarted) {
			Log.e("dylan", "The decoder is not started.");
			return false;
		}
		try { 
			boolean feeded = false;
			while (!feeded) {
		        int inputBufferIndex = mDecoder.dequeueInputBuffer(20 * 1000); 
		        if (inputBufferIndex >= 0) {
		        	ByteBuffer inputBuffer = mInputBuffers[inputBufferIndex];
		        	inputBuffer.clear();
		        	inputBuffer.put(datas, offset, length);  
		        	mDecoder.queueInputBuffer(inputBufferIndex, 0, length, 0, 0);
		        	feeded = true;
		        }  
	
		        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();  
		        boolean over = false;
		        do {  
			        int outputBufferIndex = mDecoder.dequeueOutputBuffer(bufferInfo, 0);
		        	switch (outputBufferIndex) {
					case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
						if (mListener != null) {
			        		MediaFormat mediaFormat = mDecoder.getOutputFormat();
			        		int channel = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
			        		int sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
			        		mListener.onAudioFormat(channel, sampleRate, 16);
			        	}
						break;
					case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
						mOutputBuffers = mDecoder.getOutputBuffers();
						break; 
					case MediaCodec.INFO_TRY_AGAIN_LATER:
						over = true;
						break;
					default:
						ByteBuffer buffer = mOutputBuffers[outputBufferIndex];
						if (mListener != null && buffer == null) {
							mListener.onAudioBlock(null, bufferInfo.offset, bufferInfo.size);
						} else if (mListener != null) {
							if (mPcmCache.length < bufferInfo.size) {
								mPcmCache = new byte[bufferInfo.size];
							}
							buffer.position(bufferInfo.offset);
							buffer.limit(bufferInfo.offset + bufferInfo.size);
						    buffer.get(mPcmCache);
							mListener.onAudioBlock(mPcmCache, 0, bufferInfo.size);
						}
						mDecoder.releaseOutputBuffer(outputBufferIndex, true);
					}
		        } while (!over);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	@SuppressWarnings("deprecation")
	public void shutdown() {
		try {
			if (mDecoder != null) {
				int inputBufferIndex = mDecoder.dequeueInputBuffer(-1); 
		        if (inputBufferIndex >= 0) {
		            ByteBuffer inputBuffer = mInputBuffers[inputBufferIndex];
		            inputBuffer.clear();
		        	mDecoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);  
		        }
				MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();  
		        boolean over = false;
		        do {  
			        int outputBufferIndex = mDecoder.dequeueOutputBuffer(bufferInfo, 0);
		        	switch (outputBufferIndex) {
					case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
						if (mListener != null) {
			        		MediaFormat mediaFormat = mDecoder.getOutputFormat();
			        		int channel = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
			        		int sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
			        		mListener.onAudioFormat(channel, sampleRate, 16);
			        	}
						break;
					case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
						mOutputBuffers = mDecoder.getOutputBuffers();
						break; 
					case MediaCodec.INFO_TRY_AGAIN_LATER:
						over = true;
						break;
					default:
						ByteBuffer buffer = mOutputBuffers[outputBufferIndex];
						if (mListener != null && buffer == null) {
							mListener.onAudioBlock(null, bufferInfo.offset, bufferInfo.size);
						} else if (mListener != null) {
							if (mPcmCache.length < bufferInfo.size) {
								mPcmCache = new byte[bufferInfo.size];
							}
							buffer.position(bufferInfo.offset);
							buffer.limit(bufferInfo.offset + bufferInfo.size);
						    buffer.get(mPcmCache);
							mListener.onAudioBlock(mPcmCache, 0, bufferInfo.size);
						}
						mDecoder.releaseOutputBuffer(outputBufferIndex, true);
					}
		        } while (!over);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (mDecoder != null) {
				mDecoder.stop();
				mDecoder.release();
				mDecoder = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private MediaCodec mDecoder = null;
	private Callback mListener = null;
	private ByteBuffer[] mInputBuffers = null;
	private ByteBuffer[] mOutputBuffers = null;
	private byte mPreviousLastByte = 0x00;
	private boolean mFoundAacHeader = false;
	private byte[] mAacCache = null;
	private int mAacCacheRemain = 0;
	private byte[] mPcmCache = new byte[1];
	private boolean mStarted = false;
	@SuppressWarnings("deprecation")
	private boolean startDecoder(byte[] datas, int offset) {
		try {
			int profile = ((datas[offset + 2] & 0xff) >> 6) + 1;
			int frequency = ((datas[offset + 2] & 0xff) >> 2) & 0x0f;
			int channel = ((datas[offset + 2] & 0x01) << 2) + (((datas[offset + 3] & 0xff) >> 6) & 0x03);			
			final int samplerates[] = {96000, 88200, 64000, 48000, 44100, 32000, 24000, 22050, 16000, 12000, 11025, 8000, 7350};
			if (frequency < 0 || frequency >= samplerates.length) {
				Log.e("dylan", "Invalid audio frequency index.");
				return false;
			}
			byte[] config = new byte[2];
			config[0] = (byte) (((profile << 3) & 0xf8) + ((frequency >> 1) & 0x07));
			config[1] = (byte) (((frequency << 7) & 0x80) + ((channel << 3) & 0x78)); 
			MediaFormat mediaFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", samplerates[frequency], channel);  
			mediaFormat.setInteger(MediaFormat.KEY_IS_ADTS, 1);
			mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(config));
			mDecoder.configure(mediaFormat, null, null, 0);
			mDecoder.start(); 
			mStarted = true;
			mInputBuffers = mDecoder.getInputBuffers();
			mOutputBuffers = mDecoder.getOutputBuffers();
			mFoundAacHeader = false;
			mPreviousLastByte = 0x00;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	private void appendAacData(byte[] datas, int offset, int length) {
		if (length < 1)return;
		if (mAacCache != null && mAacCache.length > mAacCacheRemain + length) {
			System.arraycopy(datas, offset, mAacCache, mAacCacheRemain, length);
			mAacCacheRemain += length;
		} else {
			byte[] newCache = new byte[(mAacCacheRemain + length) << 1];
			if (mAacCacheRemain > 0)System.arraycopy(mAacCache, 0, newCache, 0, mAacCacheRemain);
			System.arraycopy(datas, offset, newCache, mAacCacheRemain, length);
			mAacCacheRemain += length;
			mAacCache = newCache;
		}
	}
	private int calcAdtsLength(byte[] data1, int off1, int len1, byte[] data2, int off2, int len2) {
		if (len1 < 0 || len2 < 0)return -1;
		if (len1 + len2 < 7)return -1;
		int length = 0;
		if (len1 >= 4) {
			length += (data1[3 + off1] & 0x03);
		} else {
			length += (data2[3 + off2 - len1] & 0x03);
		}
		length <<= 8;
		if (len1 >= 5) {
			length += (data1[4 + off1] & 0xff);
		} else {
			length += (data2[4 + off2 - len1] & 0xff);
		}
		length <<= 3;
		if (len1 >= 6) {
			length += ((data1[5 + off1] & 0xe0) >> 5);
		} else {
			length += ((data2[5 + off2 - len1] & 0xe0) >> 5);
		}
		return length;
	}
}
