package com.dylan.medias.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;

public class MxAvcDecoder {
	
	public interface Callback {
		void onVideoFormat(int width, int height, int colorFormat);
		void onVideoFrame(byte[] datas, int offset, int length);
	}
	
	public MxAvcDecoder(Callback listener) {
		mListener = listener;
	}
	public void setPresentationTimeUs(int presentationTimeUs) {
		mPresentationTimeUs = presentationTimeUs;
	}
	public boolean startup(Surface surface) {
		shutdown();
		try {
			mDecoder = MediaCodec.createDecoderByType("video/avc");
			mSurface = surface;
			mStarted = false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public boolean startup(byte[] sps, byte[] pps, Surface surface) {
		if (sps == null) {
			Log.e("dylan", "Invalid sps and pps sequence.");
			return false;
		}
		shutdown();
		try {
			mDecoder = MediaCodec.createDecoderByType("video/avc");
			mSpsNal = sps;
			mPpsNal = pps;
			mSurface = surface;
			mStarted = false;
			return startDecoder();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	public boolean startup(byte[] config, int offset, int length, Surface surface) {
		mStarted = false;
		int begin = -1;
		for (int i = offset; i < offset + length - 3; i++) {
			if (config[i + 0] == 0x00 && config[i + 1] == 0x00 && ((config[i + 2] == 0x00 && config[i + 3] == 0x01) || config[i + 2] == 0x01)) {
				if (begin == -1) {
					begin = i;
				} else {
					if ((config[begin + 2] == 0x00 && (config[begin + 4] & 0x1f) == 0x07) ||
						(config[begin + 2] == 0x01 && (config[begin + 3] & 0x1f) == 0x07)) {
						mSpsNal = new byte[i - begin];
						System.arraycopy(config, begin, mSpsNal, 0, i - begin);
					} else if ((config[begin + 2] == 0x00 && (config[begin + 4] & 0x1f) == 0x08) ||
							   (config[begin + 2] == 0x01 && (config[begin + 3] & 0x1f) == 0x08)) {
						mPpsNal = new byte[i - begin];
						System.arraycopy(config, begin, mPpsNal, 0, i - begin);
					}
					begin = i;
				}
				i += 3;
			}
		}
		if (begin >= 0 && begin < (offset + length - 4)) {
			if ((config[begin + 2] == 0x00 && (config[begin + 4] & 0x1f) == 0x07) ||
					(config[begin + 2] == 0x01 && (config[begin + 3] & 0x1f) == 0x07)) {
					mSpsNal = new byte[offset + length - begin];
					System.arraycopy(config, begin, mSpsNal, 0, offset + length - begin);
				} else if ((config[begin + 2] == 0x00 && (config[begin + 4] & 0x1f) == 0x08) ||
						   (config[begin + 2] == 0x01 && (config[begin + 3] & 0x1f) == 0x08)) {
					mPpsNal = new byte[offset + length - begin];
					System.arraycopy(config, begin, mPpsNal, 0, offset + length - begin);
				}
		}
		if (mSpsNal == null) {
			Log.e("dylan", "No sps found in config.");
			return false;
		}
		try {
			mDecoder = MediaCodec.createDecoderByType("video/avc");
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
		mSurface = surface;
		return startDecoder();
	}
	public boolean decodeAvcBlock(byte[] datas, int offset, int length) {
		if (length > 4) {
			if (datas[0] == 0x00 && datas[1] == 0x00 && ((datas[2] == 0x00 && datas[3] == 0x01) || datas[2] == 0x01)) {
				if (mAvcCache != null && !appendANal(mAvcCache, 0, mAvcCacheRemain))return false;
				mAvcCacheRemain = 0;
			}
		}		
		if (mAvcCache == null || mAvcCacheRemain + length > mAvcCache.length) {
			byte[] newCache = new byte[(mAvcCacheRemain + length) << 1];
			if (mAvcCacheRemain > 0)System.arraycopy(mAvcCache, 0, newCache, 0, mAvcCacheRemain);
			mAvcCache = newCache;
		}
		System.arraycopy(datas, offset, mAvcCache, mAvcCacheRemain, length);
		mAvcCacheRemain += length;
		int begin = -1;
		for (int i = 0; i < mAvcCacheRemain - 3; i++) {
			if (mAvcCache[i + 0] == 0x00 && mAvcCache[i + 1] == 0x00 && ((mAvcCache[i + 2] == 0x00 && mAvcCache[i + 3] == 0x01) || mAvcCache[i + 2] == 0x01)) {
				if (begin == -1) {
					begin = i;
				} else if (!appendANal(mAvcCache, begin, i - begin)) {
					return false;
				} else {
					begin = i;
				}
				i += 3;
			}
		}
		if (begin == -1) {
			mAvcCacheRemain = 0;
		} else if (begin > 0) {
			mAvcCacheRemain -= begin;
			System.arraycopy(mAvcCache, begin, mAvcCache, 0, mAvcCacheRemain);
		}
		return true;
	}
	public boolean decodeNals(byte[] datas, int offset, int length, int presentationTimeUs) {
		mPresentationTimeUs = presentationTimeUs;
		int begin = -1;
		for (int i = offset; i < offset + length - 3; i++) {
			if (datas[i + 0] == 0x00 && datas[i + 1] == 0x00 && ((datas[i + 2] == 0x00 && datas[i + 3] == 0x01) || datas[i + 2] == 0x01)) {
				if (begin == -1) {
					begin = i;
				} else if (!appendANal(datas, begin, i - begin)) {
					return false;
				} else {
					begin = i;
				}
				i += 3;
			}
		}
		if (begin >= 0 && begin < (offset + length - 4)) {
			return appendANal(datas, begin, offset + length - begin);
		}
		return true;
	}
	public boolean decodeANal(byte[] datas, int offset, int length, int presentationTimeUs) {
		return decodeANal(datas, offset, length, presentationTimeUs, 0);
	}
	@SuppressWarnings("deprecation")
	public boolean decodeANal(byte[] datas, int offset, int length, int presentationTimeUs, int flags) {
		if (!mStarted) {
			Log.e("dylan", "The decoder is not started.");
			return false;
		}
		//Log.i("dylan", "decodeNal(" + length + ")");
		try {
			boolean feeded = false;
			while (!feeded) {
		        int inputBufferIndex = mDecoder.dequeueInputBuffer(10);
		        if (inputBufferIndex >= 0) {
		        	ByteBuffer inputBuffer = null;
		        	inputBuffer = mInputBuffers[inputBufferIndex];
		        	inputBuffer.clear();
		        	if (length > 2 && datas[offset + 2] == 0x01) {
		        		inputBuffer.put((byte) 0x00);
		        		inputBuffer.put(datas, offset, length);
		        		length++;
		        	} else {
		        		inputBuffer.put(datas, offset, length);
		        	}
		        	mDecoder.queueInputBuffer(inputBufferIndex, 0, length, presentationTimeUs, flags);  
		        	feeded = true;
		        }  
	
		        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();  
		        boolean over = false;
		        do {  
					long startMs = System.currentTimeMillis();
			        int outputBufferIndex = mDecoder.dequeueOutputBuffer(bufferInfo, 0);
		        	switch (outputBufferIndex) {
					case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
						if (mListener != null) {
			        		MediaFormat mediaFormat = mDecoder.getOutputFormat();
			        		int width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
			        		int height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
			        		int colorFormat = mediaFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT);
			        		mListener.onVideoFormat(width, height, colorFormat);
			        	}
						break;
					case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
						mOutputBuffers = mDecoder.getOutputBuffers();
						break; 
					case MediaCodec.INFO_TRY_AGAIN_LATER:
						over = true;
						break;
					default:
						boolean hasBuffer = false;
						if (mOutputBuffers != null) {
							ByteBuffer buffer = mOutputBuffers[outputBufferIndex];
							if (buffer != null) {
								if (mYUVBuffer == null || mYUVBuffer.length < bufferInfo.size) {
									mYUVBuffer = new byte[bufferInfo.size];
								}
								try {
									buffer.position(bufferInfo.offset);
									buffer.limit(bufferInfo.offset + bufferInfo.size);
									buffer.get(mYUVBuffer);
									hasBuffer = true;
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
						if (mListener != null) {
							mListener.onVideoFrame(hasBuffer ? mYUVBuffer : null, bufferInfo.offset, bufferInfo.size);
						}
						while (presentationTimeUs > 0 && (bufferInfo.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs)) {
							try {
								Thread.sleep(10);
							} catch (Exception e) {
								e.printStackTrace();
					        }
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
			if (mDecoder != null && mStarted) {
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
			        		int width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
			        		int height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
			        		int colorFormat = mediaFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT);
			        		mListener.onVideoFormat(width, height, colorFormat);
			        	}
						break;
					case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
						mOutputBuffers = mDecoder.getOutputBuffers();
						break; 
					case MediaCodec.INFO_TRY_AGAIN_LATER:
						over = true;
						break;
					default:
						boolean hasBuffer = false;
						ByteBuffer buffer = mOutputBuffers[outputBufferIndex];
						if (buffer != null) {
							if (mYUVBuffer == null || mYUVBuffer.length < bufferInfo.size) {
								mYUVBuffer = new byte[bufferInfo.size];
							}
							try {
								buffer.position(bufferInfo.offset);
								buffer.limit(bufferInfo.offset + bufferInfo.size);
								buffer.get(mYUVBuffer);
								hasBuffer = true;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}if (mListener != null) {
							mListener.onVideoFrame(hasBuffer ? mYUVBuffer : null, bufferInfo.offset, bufferInfo.size);
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
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mDecoder = null;
		}
	}

	private MediaCodec mDecoder = null;
	private Callback mListener = null;
	private Surface mSurface = null;
	private ByteBuffer[] mInputBuffers = null;
	private ByteBuffer[] mOutputBuffers = null;
	private byte[] mYUVBuffer = null;
	private boolean mStarted = false;
	private byte[] mAvcCache = null;
	private int mAvcCacheRemain = 0;
	private int mPresentationTimeUs = 0;
	private byte[] mSpsNal = null;
	private byte[] mPpsNal = null;
	@SuppressWarnings("deprecation")
	private boolean startDecoder() {
		try { 
			MxAvcConfig parser = new MxAvcConfig();
			if (!parser.parse(mSpsNal)) {
				Log.e("dylan", "Parse sps sequence failed.");
				return false;
			}
			MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", parser.getWidth(), parser.getHeight());  
			if (mSpsNal != null)mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(mSpsNal));
			if (mPpsNal != null)mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(mPpsNal));
			mDecoder.configure(mediaFormat, mSurface, null, 0);  
			mDecoder.start(); 
			mInputBuffers = mDecoder.getInputBuffers();
			mOutputBuffers = mDecoder.getOutputBuffers();
			mStarted = true;
			decodeANal(mSpsNal, 0, mSpsNal.length, 0, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
			if (mPpsNal != null)decodeANal(mPpsNal, 0, mPpsNal.length, 0, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	private boolean appendANal(byte[] datas, int offset, int length) {
		int headerLen = 4;
		if (datas[offset + 2] == 0x01) {
			headerLen--;
		}
		if (!mStarted) {
			if ((datas[offset + headerLen] & 0x1f) == 0x07) {
				mSpsNal = new byte[length - headerLen + 4];
				if (headerLen == 3) {
					mSpsNal[0] = 0x00;
					System.arraycopy(datas, offset, mSpsNal, 1, length);
				} else {
					System.arraycopy(datas, offset, mSpsNal, 0, length);
				}
			} else if ((datas[offset + headerLen] & 0x1f) == 0x08) {
				mPpsNal = new byte[length - headerLen + 4];
				if (headerLen == 3) {
					mPpsNal[0] = 0x00;
					System.arraycopy(datas, offset, mPpsNal, 1, length);
				} else {
					System.arraycopy(datas, offset, mPpsNal, 0, length);
				}
			} else if (mSpsNal != null) {
				if (!startDecoder()) {
					Log.e("dylan", "start avc decoder failed.");
					return false;
				}
				if ((datas[offset + headerLen] & 0x1f) > 0x05)return true;
				else return decodeANal(datas, offset, length, mPresentationTimeUs);
			}
		} else {
			if ((datas[offset + headerLen] & 0x1f) > 0x05)return true;
			else return decodeANal(datas, offset, length, mPresentationTimeUs);
		}
		return true;
	}
}
