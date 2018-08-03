package com.dylan.medias.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.nio.ByteBuffer;

public class MxAudioDecoder implements Runnable {

	public interface Callback {
		void onAudioFormat(int channel, int sampleRate, int bitWidth);
		void onAudioBlock(byte[] datas, int offset, int length);
	}

	public MxAudioDecoder(Callback listener) {
		callback = listener;
	}
	public boolean open(MediaFormat format) {
		return open(format, false);
	}
	public boolean open(MediaFormat format, boolean async) {
		try {
			String mime = format.getString(MediaFormat.KEY_MIME);
			if (mime.equals("audio/g711-alaw")) {
				if (async) return false;
				g711Codec = new MXG711ACodec();
				started = true;
			} else {
				mediaCodec = MediaCodec.createDecoderByType(mime);
				mediaCodec.configure(format, null, null, 0);
				mediaCodec.start();
				if (Build.VERSION.SDK_INT < 21) {
					inputBuffers = mediaCodec.getInputBuffers();
					outputBuffers = mediaCodec.getOutputBuffers();
				}
				started = true;
				if (async) {
					thread = new Thread(this);
					thread.start();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean queue(ByteBuffer byteBuffer) {
		return queue(byteBuffer, null, 0, 0);
	}
	public boolean queue(byte[] datas, int offset, int length) {
		return queue(null, datas, offset, length);
	}
	private ByteBuffer getInputBuffer(int index) {
		if (Build.VERSION.SDK_INT < 21) {
			return inputBuffers[index];
		} else {
			return mediaCodec.getInputBuffer(index);
		}
	}
	private ByteBuffer getOutputBuffer(int index) {
		if (Build.VERSION.SDK_INT < 21) {
			return outputBuffers[index];
		} else {
			return mediaCodec.getOutputBuffer(index);
		}
	}
	private boolean queue(ByteBuffer byteBuffer, byte[] datas, int offset, int length) {
		if (!started) {
			Log.e("dylan", "The decoder is not started.");
			return false;
		}
		try {
			while (started) {
				int inputBufferIndex = mediaCodec.dequeueInputBuffer(500);
				if (inputBufferIndex >= 0) {
					ByteBuffer inputBuffer = getInputBuffer(inputBufferIndex);
					inputBuffer.clear();
					if (byteBuffer != null) {
						inputBuffer.put(byteBuffer);
					} else if (length > 2 && datas[offset + 2] == 0x01) {
						inputBuffer.put((byte) 0x00);
						inputBuffer.put(datas, offset, length);
						length++;
					} else {
						inputBuffer.put(datas, offset, length);
					}
					mediaCodec.queueInputBuffer(inputBufferIndex, 0, length, 0, 0);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean decode(ByteBuffer byteBuffer) {
		return decode(byteBuffer, null, 0, 0);
	}
	public boolean decode(byte[] datas, int offset, int length) {
		return decode(null, datas, offset, length);
	}
	private boolean decode(ByteBuffer byteBuffer, byte[] datas, int offset, int length) {
		if (!started) {
			Log.e("dylan", "The decoder is not started.");
			return false;
		}
		try {
			if (g711Codec != null) {
				if (g711PcmCache == null) {
					if (callback != null) callback.onAudioFormat(1, 8000, 16);
				}
				if (g711PcmCache == null || g711PcmCache.length < length * 2) {
					g711PcmCache = new byte[length * 2];
				}
				int len = MXG711ACodec.decode(datas, offset, length, g711PcmCache);
				if (callback != null) callback.onAudioBlock(g711PcmCache, 0, len);
			} else {
				boolean feeded = false;
				while (!feeded && started) {
					int inputBufferIndex = mediaCodec.dequeueInputBuffer(500);
					if (inputBufferIndex >= 0) {
						ByteBuffer inputBuffer = getInputBuffer(inputBufferIndex);
						inputBuffer.clear();
						int bufferBegin = inputBuffer.position();
						if (byteBuffer != null) {
							inputBuffer.put(byteBuffer);
						} else if (length > 2 && datas[offset + 2] == 0x01) {
							inputBuffer.put((byte) 0x00);
							inputBuffer.put(datas, offset, length);
							length++;
						} else {
							inputBuffer.put(datas, offset, length);
						}
						mediaCodec.queueInputBuffer(inputBufferIndex, bufferBegin, length, 0, 0);
						feeded = true;
					}
					pull();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public void close() {
		try {
			started = false;
			boolean eof = false;
			while (mediaCodec != null && !eof) {
				int inputBufferIndex = mediaCodec.dequeueInputBuffer(500);
				if (inputBufferIndex >= 0) {
					ByteBuffer inputBuffer = getInputBuffer(inputBufferIndex);
					inputBuffer.clear();
					mediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
					eof = true;
				}
				if (thread == null) {
					pull();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (thread != null)thread.join();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		thread = null;
		try {
			if (mediaCodec != null) {
				mediaCodec.stop();
				mediaCodec.release();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		mediaCodec = null;
	}


	@Override
	public void run() {
		while (started) {
			try {
				pull();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	private void pull() {
		MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
		while (true) {
			int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
			if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
				MediaFormat mediaFormat = mediaCodec.getOutputFormat();
				int channel = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
				int sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
				if (callback != null) callback.onAudioFormat(channel, sampleRate, 16);
			} else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
				if (Build.VERSION.SDK_INT < 21) {
					inputBuffers = mediaCodec.getInputBuffers();
					outputBuffers = mediaCodec.getOutputBuffers();
				}
			} else if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
				break;
			} else if (outputBufferIndex > 0) {
				ByteBuffer buffer = getOutputBuffer(outputBufferIndex);
				if (buffer != null) {
					if (pcmBuffer.length < bufferInfo.size) pcmBuffer = new byte[bufferInfo.size];
					buffer.position(bufferInfo.offset);
					buffer.limit(bufferInfo.offset + bufferInfo.size);
					buffer.get(pcmBuffer);
					callback.onAudioBlock(pcmBuffer, 0, bufferInfo.size);
				}
				mediaCodec.releaseOutputBuffer(outputBufferIndex, true);

			}
		}
	}
	
	private MediaCodec mediaCodec = null;
	private MXG711ACodec g711Codec = null;
	private byte[] g711PcmCache = null;
	private Callback callback = null;
	private ByteBuffer[] inputBuffers = null;
	private ByteBuffer[] outputBuffers = null;
	private byte[] pcmBuffer = new byte[1];
	private boolean started = false;
	private Thread thread = null;
}
