package com.dylan.medias.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;

public class MxMp3Decoder {
	
	public interface Callback {
		void onAudioFormat(int channel, int sampleRate, int bitWidth);
		void onAudioBlock(byte[] datas, int offset, int length);
	}

	public MxMp3Decoder(Callback listener) {
		mListener = listener;
	}
	@SuppressWarnings("deprecation")
	public boolean startup() {
		try {
			mDecoder = MediaCodec.createDecoderByType("audio/mpeg"); 
			MediaFormat mediaFormat = MediaFormat.createAudioFormat("audio/mpeg", 0, 0);  
			mDecoder.configure(mediaFormat, null, null, 0);
			mDecoder.start(); 
			mInputBuffers = mDecoder.getInputBuffers();
			mOutputBuffers = mDecoder.getOutputBuffers();
			mStarted = true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
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
	private byte[] mPcmCache = new byte[1];
	private boolean mStarted = false;
}
