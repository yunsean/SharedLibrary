package com.dylan.medias.publish;

import android.content.Context;
import android.media.MediaFormat;
import android.util.Size;

import java.nio.ByteBuffer;

public class MxRtmpPublish {
    public interface Callback {
        void onStatistics(long send, long lost, int bitRate);
        void onError();
    }

    public static class Builder {
        private enum VideoType {None, Avc};
        private enum AudioType {None, Aac};
        private VideoType videoType = VideoType.None;
        private AudioType audioType = AudioType.None;
        private int width = 0;
        private int height = 0;
        private byte[] sps = null;
        private byte[] pps = null;
        private int sampleRate = 0;
        private int channels = 0;
        private byte[] esds = null;
        private boolean useUdp = false;
        private Builder () {
        }
        public static Builder with(Context context) {
            return new Builder();
        }
        public Builder avc() {
            this.videoType = VideoType.Avc;
            return this;
        }
        public Builder udp() {
            this.useUdp = true;
            return this;
        }
        public Builder rtmp() {
            this.useUdp = false;
            return this;
        }
        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }
        public Builder size(Size size) {
            this.width = size.getWidth();
            this.height = size.getHeight();
            return this;
        }
        public Builder sps(byte[] datas, int offset, int length) {
            this.sps = new byte[length];
            System.arraycopy(datas, offset, this.sps, 0, length);
            return this;
        }
        public Builder pps(byte[] datas, int offset, int length) {
            this.pps = new byte[length];
            System.arraycopy(datas, offset, this.pps, 0, length);
            return this;
        }
        public Builder sps(byte[] sps) {
            return sps(sps, 0, sps.length);
        }
        public Builder pps(byte[] pps) {
            return pps(pps, 0, pps.length);
        }
        public Builder sps(ByteBuffer buffer) {
            return sps(buffer.array(), buffer.position(), buffer.limit() - buffer.position());
        }
        public Builder pps(ByteBuffer buffer) {
            return pps(buffer.array(), buffer.position(), buffer.limit() - buffer.position());
        }
        public Builder video(MediaFormat format) {
            return this
                    .avc()
                    .size(format.getInteger(MediaFormat.KEY_WIDTH), format.getInteger(MediaFormat.KEY_HEIGHT))
                    .sps(format.getByteBuffer("csd-0"))
                    .pps(format.getByteBuffer("csd-1"));
        }
        public Builder audio(MediaFormat format) {
            return this
                    .aac()
                    .sampleRate(format.getInteger(MediaFormat.KEY_SAMPLE_RATE))
                    .channels(format.getInteger(MediaFormat.KEY_CHANNEL_COUNT))
                    .esds(format.getByteBuffer("csd-0"));
        }
        public boolean hasVideo() {
            return videoType != VideoType.None;
        }
        public boolean hasAudio() {
            return audioType != AudioType.None;
        }
        public Builder aac() {
            this.audioType = AudioType.Aac;
            return this;
        }
        public Builder sampleRate(int sampleRate) {
            this.sampleRate = sampleRate;
            return this;
        }
        public Builder channels(int channels) {
            this.channels = channels;
            return this;
        }
        public Builder esds(byte[] datas, int offset, int lenght) {
            this.esds = new byte[lenght];
            System.arraycopy(datas, offset, this.esds, 0, lenght);
            return this;
        }
        public Builder esds(byte[] esds) {
            return esds(esds, 0, esds.length);
        }
        public Builder esds(ByteBuffer buffer) {
            return esds(buffer.array(), buffer.position(), buffer.limit() - buffer.position());
        }
        public MxRtmpPublish build(){
            if (videoType == VideoType.None && audioType == AudioType.None) {
                throw new RuntimeException("Must contain a video track or an audio track.");
            }
            MxRtmpPublish publish = new MxRtmpPublish();
            try {;
                if (!publish.mNative.open(publish, useUdp)) throw new IllegalStateException();
                if (videoType == VideoType.Avc) {
                    publish.mNative.videoAvc(width, height, sps, pps);
                }
                if (audioType == AudioType.Aac) {
                    publish.mNative.audioAac(sampleRate, channels, esds);
                }
                return publish;
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
    }
    public boolean start(String url) {
        return mNative.start(url);
    }
    public void appendVideo(byte[] datas, int length, long timecode, boolean isKey, boolean wait) {
        mNative.videoFrame(datas, length, timecode, isKey, wait);
    }
    public void appendAudio(byte[] datas, int length, long timecode, boolean wait) {
        mNative.audioFrame(datas, length, timecode, wait);
    }
    public ByteBuffer videoBuffer(int size) {
        if (mVideoBuffer == null || mVideoBuffer.capacity() < size) {
            mVideoBuffer = ByteBuffer.allocateDirect(size * 2);
        }
        mVideoBuffer.position(0);
        return mVideoBuffer;
    }
    public void appendVideo(int length, long timecode, boolean isKey, boolean wait) {
        mNative.videoBuffer(mVideoBuffer, length, timecode, isKey, wait);
    }
    public ByteBuffer audioBuffer(int size) {
        if (mAudioBuffer == null || mAudioBuffer.capacity() < size) {
            mAudioBuffer = ByteBuffer.allocateDirect(size * 3);
        }
        mAudioBuffer.position(0);
        return mAudioBuffer;
    }
    public void appendAudio(int length, long timecode, boolean wait) {
        mNative.audioBuffer(mAudioBuffer, length, timecode, wait);
    }
    public MxRtmpPublish setCallback(Callback callback) {
        this.mCallback = callback;
        return this;
    }
    public void stop() {
        mNative.stop();
    }
    public void close() {
        mNative.close();
    }

    private NativeMethod mNative = new NativeMethod();
    private ByteBuffer mVideoBuffer = null;
    private ByteBuffer mAudioBuffer = null;
    private Callback mCallback = null;
    private MxRtmpPublish() {
    }

    void statistics(long send, long lost, int bitRate) {
        if (mCallback != null) {
            mCallback.onStatistics(send, lost, bitRate);
        }
    }
    void onError() {
        if (mCallback != null) {
            mCallback.onError();
        }
    }
}
