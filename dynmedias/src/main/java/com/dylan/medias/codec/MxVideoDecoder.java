package com.dylan.medias.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;

public class MxVideoDecoder implements Runnable {

    public interface Callback {
        void onVideoFormat(MediaFormat mediaFormat);
        void onVideoFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer byteBuffer);
        void onError();
    }

    public MxVideoDecoder(Callback listener) {
        callback = listener;
    }
    public boolean open(MediaFormat format, Surface surface) {
        return open(format, surface, false);
    }
    public boolean open(MediaFormat format, Surface surface, boolean async) {
        close();
        try {
            String mime = format.getString(MediaFormat.KEY_MIME);
            ByteBuffer sps = format.getByteBuffer("csd-0");
            ByteBuffer pps = format.getByteBuffer("csd-1");
            mediaCodec = MediaCodec.createDecoderByType(mime);
            mediaCodec.configure(format, surface, null, 0);
            mediaCodec.start();
            if (Build.VERSION.SDK_INT < 21) {
                inputBuffers = mediaCodec.getInputBuffers();
                outputBuffers = mediaCodec.getOutputBuffers();
            }
            started = true;
            if (sps != null) decode(sps, null, 0, 0, 0, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
            if (pps != null) decode(pps, null, 0, 0, 0, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
            if (async) {
                thread = new Thread(this);
                thread.start();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public MediaFormat outputFormat() {
        MediaFormat mediaFormat = mediaCodec.getOutputFormat();
        return mediaFormat;
    }
    public int colorFormat() {
        MediaFormat mediaFormat = mediaCodec.getOutputFormat();
        int colorFormat = mediaFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT);
        return colorFormat;
    }
    public boolean queue(ByteBuffer byteBuffer, boolean isKey, long presentationTimeUs) {
        return queue(byteBuffer, null, 0, 0, presentationTimeUs, isKey ? MediaCodec.BUFFER_FLAG_KEY_FRAME : 0);
    }
    public boolean queue(byte[] datas, int offset, int length, boolean isKey, long presentationTimeUs) {
        return queue(null, datas, offset, length, presentationTimeUs, isKey ? MediaCodec.BUFFER_FLAG_KEY_FRAME : 0);
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
    private boolean queue(ByteBuffer byteBuffer, byte[] datas, int offset, int length, long presentationTimeUs, int flags) {
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
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, length, presentationTimeUs, flags);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean decode(ByteBuffer byteBuffer, boolean isKey, long presentationTimeUs) {
        return decode(byteBuffer, null, 0, 0, presentationTimeUs, isKey ? MediaCodec.BUFFER_FLAG_KEY_FRAME : 0);
    }
    public boolean decode(byte[] datas, int offset, int length, boolean isKey, long presentationTimeUs) {
        return decode(null, datas, offset, length, presentationTimeUs, isKey ? MediaCodec.BUFFER_FLAG_KEY_FRAME : 0);
    }
    private boolean decode(ByteBuffer byteBuffer, byte[] datas, int offset, int length, long presentationTimeUs, int flags) {
        if (!started) {
            Log.e("dylan", "The decoder is not started.");
            return false;
        }
        if (latestTimeStamp == -1) {
            latestTimeStamp = presentationTimeUs;
            startupTimeStamp = presentationTimeUs;
        }
        latestTimeStamp = presentationTimeUs;
        try {
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
                    mediaCodec.queueInputBuffer(inputBufferIndex, bufferBegin, length, presentationTimeUs, flags);
                    feeded = true;
                }
                pull();
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
                if (callback != null) callback.onError();
                break;
            }
        }
    }
    private void pull() {
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (true) {
            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (callback != null) callback.onVideoFormat(mediaCodec.getOutputFormat());
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                if (Build.VERSION.SDK_INT < 21) {
                    inputBuffers = mediaCodec.getInputBuffers();
                    outputBuffers = mediaCodec.getOutputBuffers();
                }
            } else if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break;
            } else if (outputBufferIndex > 0) {
                ByteBuffer buffer = getOutputBuffer(outputBufferIndex);
                if (bufferInfo.size > 0 && buffer != null && callback != null) {
                    callback.onVideoFrame(bufferInfo, buffer);
                }
                mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
            }
        }
    }

    private MediaCodec mediaCodec = null;
    private ByteBuffer[] inputBuffers = null;
    private ByteBuffer[] outputBuffers = null;
    private Callback callback = null;
    private boolean started = false;
    private Thread thread = null;
    private long latestTimeStamp = -1;
    private long startupTimeStamp = 0;
}
