package com.dylan.medias.codec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class MxAudioEncoder implements Runnable {
    public interface Callback {
        void onFormatChanged(MediaFormat mediaFormat);
        void onEncodeFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer byteBuffer);
    }
    public static class Builder {
        private String mMime = null;
        private int mSampleRate = 0;
        private int mChannelCount = 0;
        private int mBitRate = 0;
        private Map<String, Integer> mProfileParams = new HashMap<>();
        public static Builder create() {
            return new Builder();
        }
        public Builder mime(String mime) {
            mMime = mime;
            return this;
        }
        public Builder aac() {
            return mime(MediaFormat.MIMETYPE_AUDIO_AAC);
        }
        public Builder ac3() {
            return mime(MediaFormat.MIMETYPE_AUDIO_AC3);
        }
        public Builder sampleRate(int sampleRate) {
            mSampleRate = sampleRate;
            return this;
        }
        public Builder channel(int channel) {
            mChannelCount = channel;
            return this;
        }
        public Builder bitRate(int bitRate) {
            mBitRate = bitRate;
            return this;
        }
        public Builder addParam(String key, Integer value) {
            mProfileParams.put(key, value);
            return this;
        }
        public MxAudioEncoder build() {
            if (mMime == null) return null;
            if (mSampleRate == 0) return null;
            if (mChannelCount == 0) return null;
            if (mBitRate == 0) return null;
            try {
                MediaFormat format = MediaFormat.createAudioFormat(mMime, mSampleRate, mChannelCount);
                format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
                boolean hasAacProfile = false;
                for (Map.Entry<String, Integer> entry : mProfileParams.entrySet()) {
                    if (entry.getKey() == MediaFormat.KEY_AAC_PROFILE) hasAacProfile = true;
                    format.setInteger(entry.getKey(), entry.getValue());
                }
                if (mMime.equals(MediaFormat.MIMETYPE_AUDIO_AAC) && !hasAacProfile) {
                    format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectMain);
                }
                MxAudioEncoder audioEncoder = new MxAudioEncoder();
                audioEncoder.mediaCodec = MediaCodec.createEncoderByType(mMime);
                audioEncoder.mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                return audioEncoder;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }
    public MxAudioEncoder open(Callback callback) {
        mediaCodec.start();
        if (Build.VERSION.SDK_INT < 21) {
            inputBuffers = mediaCodec.getInputBuffers();
            outputBuffers = mediaCodec.getOutputBuffers();
        }
        working = true;
        this.callback = callback;
        thread = new Thread(this);
        thread.start();
        return this;
    }
    public void enqueue(byte[] datas, long presentationTimeUs) {
        enqueue(datas, 0, datas.length, presentationTimeUs);
    }
    public void enqueue(byte[] datas, int offset, int size, long presentationTimeUs) {
        try {
            if (!working) return;
            int bufferIndex = mediaCodec.dequeueInputBuffer(500);
            if (bufferIndex < 0) {
                Log.e("dylan", "Get input buffer failed: " + bufferIndex);
                return;
            }
            ByteBuffer byteBuffer = getInputBuffer(bufferIndex);
            byteBuffer.clear();
            byteBuffer.put(datas, offset, size);
            mediaCodec.queueInputBuffer(bufferIndex, 0, size, presentationTimeUs, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void close() {
        working = false;
        try {
            if (thread != null) thread.join();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        thread = null;
        try {
            if (mediaCodec != null) {
                mediaCodec.stop();
                mediaCodec.release();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        mediaCodec = null;
    }
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    private MediaCodec mediaCodec = null;
    private Thread thread = null;
    private boolean working = true;
    private Callback callback = null;
    private ByteBuffer[] inputBuffers = null;
    private ByteBuffer[] outputBuffers = null;

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

    @Override
    public void run() {
        while (working) {
            pull();
        }
    }
    private void pull() {
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (true) {
            int encoderStatus = mediaCodec.dequeueOutputBuffer(bufferInfo, 10 * 1000);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break;
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat mediaFormat = mediaCodec.getOutputFormat();
                if (callback != null) callback.onFormatChanged(mediaFormat);
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                if (Build.VERSION.SDK_INT < 21) {
                    inputBuffers = mediaCodec.getInputBuffers();
                    outputBuffers = mediaCodec.getOutputBuffers();
                }
            } else if (encoderStatus >= 0) {
                int bufferIndex = encoderStatus;
                ByteBuffer encodedData = getOutputBuffer(bufferIndex);
                if (encodedData != null) {
                    encodedData.position(bufferInfo.offset);
                    encodedData.limit(bufferInfo.offset + bufferInfo.size);
                    boolean needRelease = true;
                    if (callback != null) callback.onEncodeFrame(bufferInfo, encodedData);
                    mediaCodec.releaseOutputBuffer(encoderStatus, false);
                }
            }
        }
    }
}
