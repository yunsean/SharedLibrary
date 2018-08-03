package com.dylan.medias.codec;

import android.annotation.TargetApi;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.nio.ByteBuffer;

public class MxVideoEncoder implements Runnable {
    public interface Callback {
        void onFormatChanged(MediaFormat mediaFormat);
        void onEncodeFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer byteBuffer);
    }

    public static class Builder {
        private String mMime = null;
        private int mWidth = 0;
        private int mHeight = 0;
        private int mBitRate = 0;
        private int mColorFormat = 0;
        private int mFrameRate = 0;
        private int mKeyInterval = 5;
        private int mBitrateMode = MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR;
        public static Builder create() {
            return new Builder();
        }
        public Builder mime(String mime) {
            mMime = mime;
            return this;
        }
        public Builder avc() {
            return mime(MediaFormat.MIMETYPE_VIDEO_AVC);
        }
        public Builder hevc() {
            return mime(MediaFormat.MIMETYPE_VIDEO_HEVC);
        }
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public Builder size(Size size) {
            mWidth = size.getWidth();
            mHeight = size.getHeight();
            return this;
        }
        public Builder size(int width, int height) {
            mWidth = width;
            mHeight = height;
            return this;
        }
        public Builder bitRate(int bitRate) {
            mBitRate = bitRate;
            return this;
        }
        public Builder format(MediaFormat mediaFormat) {
            mWidth = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
            mHeight = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
            mColorFormat = mediaFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT);;
            return this;
        }
        public Builder colorFormat(int colorFormat) {
            mColorFormat = colorFormat;
            return this;
        }
        public Builder yv12() {
            return colorFormat(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        }
        public Builder fromSurface() {
            return colorFormat(MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        }
        public Builder frameRate(int frameRate) {
            mFrameRate = frameRate;
            return this;
        }
        public Builder keyInterval(int keyInterval) {
            mKeyInterval = keyInterval;
            return this;
        }
        public Builder bitRateMode(int mode) {
            mBitrateMode = mode;
            return this;
        }
        public MxVideoEncoder build() {
            if (mMime == null) return null;
            if (mWidth <= 0 || mHeight <= 0) return null;
            if (mBitRate == 0) mBitRate = mWidth * mHeight * 5;
            if (mColorFormat == 0) return null;
            if (mFrameRate == 0) mFrameRate = 25;
            try {
                MediaFormat format = MediaFormat.createVideoFormat(mMime, mWidth, mHeight);
                format.setInteger(MediaFormat.KEY_COLOR_FORMAT, mColorFormat);
                format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
                format.setInteger(MediaFormat.KEY_FRAME_RATE, mFrameRate);
                format.setInteger(MediaFormat.KEY_CAPTURE_RATE, mFrameRate);
                format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, mKeyInterval);
                format.setInteger(MediaFormat.KEY_BITRATE_MODE,  mBitrateMode);
                MxVideoEncoder videoEncoder = new MxVideoEncoder();
                videoEncoder.mediaCodec = MediaCodec.createEncoderByType(mMime);
                videoEncoder.mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                videoEncoder.colorFormat = mColorFormat;
                return videoEncoder;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }
    public int inputFormat() {
        if (Build.VERSION.SDK_INT < 21) {
            return colorFormat;
        } else {
            MediaFormat mediaFormat = mediaCodec.getInputFormat();
            int colorFormat = mediaFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT);
            return colorFormat;
        }
    }
    public Surface surface() {
        return inputSurface;
    }
    public MxVideoEncoder open(Callback callback) {
        if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface && Build.VERSION.SDK_INT >= 24) {
            inputSurface = mediaCodec.createInputSurface();
        }
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
            int maxSize = Math.min(size, byteBuffer.limit());
            byteBuffer.clear();
            byteBuffer.put(datas, offset, maxSize);
            mediaCodec.queueInputBuffer(bufferIndex, 0, size, presentationTimeUs, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void enqueue(Image image) {
        try {
            if (!working) return;
            int bufferIndex = mediaCodec.dequeueInputBuffer(500);
            if (bufferIndex < 0) {
                Log.e("dylan", "Get input buffer failed: " + bufferIndex);
                return;
            }
            ByteBuffer byteBuffer = getInputBuffer(bufferIndex);
            byteBuffer.clear();
            int begin = byteBuffer.position();
            Image.Plane[] planes = image.getPlanes();
            planes[0].getBuffer().reset();
            planes[1].getBuffer().reset();
            planes[2].getBuffer().reset();
            byteBuffer.put(planes[0].getBuffer());
            byteBuffer.put(planes[1].getBuffer());
            byteBuffer.put(planes[2].getBuffer());
            int size = byteBuffer.position() - begin;
            long presentationTimeUs = image.getTimestamp();
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
            if (inputSurface != null) {
                inputSurface.release();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        inputSurface = null;
        mediaCodec = null;
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    private int colorFormat = 0;
    private MediaCodec mediaCodec = null;
    private Surface inputSurface = null;
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
            pull(false);
        }
    }
    private void pull(boolean endOfStream) {
        if (endOfStream && inputSurface != null && Build.VERSION.SDK_INT >= 18) {
            try {
                mediaCodec.signalEndOfInputStream();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        try {
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
                        encodedData.mark();
                        if (callback != null) callback.onEncodeFrame(bufferInfo, encodedData);
                        mediaCodec.releaseOutputBuffer(encoderStatus, false);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
