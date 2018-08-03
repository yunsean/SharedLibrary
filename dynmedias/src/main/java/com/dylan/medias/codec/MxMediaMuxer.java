package com.dylan.medias.codec;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;

import com.dylan.devices.camera2.MxCamera;
import com.dylan.devices.camera2.MxCameraUtils;

import java.io.File;
import java.nio.ByteBuffer;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MxMediaMuxer {
    public static class Builder {
        private String mOutputPath = null;
        private int mOutputFormat = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4;
        private int mOrientationHint = 0;
        private float mLatitude = .0f;
        private float mLongitud = .0f;
        private boolean mSetLocation = false;
        private MediaFormat mVideoFormat = null;
        private MediaFormat mAudioFormat = null;

        public static Builder create() {
            return new Builder();
        }

        public MediaFormat videoFormat() {
            return mVideoFormat;
        }
        public MediaFormat audioFormat() {
            return mAudioFormat;
        }
        public Builder video(MediaFormat format) {
            mVideoFormat = format;
            return this;
        }
        public Builder video(String mime, int width, int height) {
            return video(MediaFormat.createVideoFormat(mime, width, height));
        }
        public Builder avc(int width, int height, byte[] sps, byte[] pps) {
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            format.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
            format.setByteBuffer("csd-1", ByteBuffer.wrap(pps));
            return video(format);
        }
        public Builder mpeg4(int width, int height, byte[] esds) {
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            format.setByteBuffer("csd-0", ByteBuffer.wrap(esds));
            return video(format);
        }
        public Builder hevc(int width, int height, byte[] vps, byte[] sps, byte[] pps) {
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            ByteBuffer buffer = ByteBuffer.allocate(vps.length + sps.length + pps.length);
            buffer.put(vps);
            buffer.put(sps);
            buffer.put(pps);
            format.setByteBuffer("csd-0", buffer);
            return video(format);
        }
        public Builder audio(MediaFormat format) {
            mAudioFormat = format;
            return this;
        }
        public Builder aac(int sampleRate, int channelCount, byte[] esds) {
            MediaFormat format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channelCount);
            format.setByteBuffer("csd-0", ByteBuffer.wrap(esds));
            return audio(format);
        }
        public Builder file(File file) {
            mOutputPath = file.getAbsolutePath();
            return this;
        }
        public Builder file(String path) {
            mOutputPath = path;
            return this;
        }
        public Builder format(int format) {
            mOutputFormat = format;
            return this;
        }
        public Builder mp4() {
            return format(MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        }
        public Builder orientationHint(int degrees) {
            mOrientationHint = degrees;
            return this;
        }
        public Builder orientationHint(MxCamera camera, Activity activity) {
            int sensorOrientation = camera.getOrientation();
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            switch (sensorOrientation) {
                case 90:
                    mOrientationHint = DEFAULT_ORIENTATIONS.get(rotation);
                    break;
                case 270:
                    mOrientationHint = INVERSE_ORIENTATIONS.get(rotation);
                    break;
            }
            return this;
        }
        public Builder orientationHint(Context context, String cameraid, Activity activity) {
            int sensorOrientation = MxCameraUtils.with(context).getOrientation(cameraid);
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            switch (sensorOrientation) {
                case 90:
                    mOrientationHint = DEFAULT_ORIENTATIONS.get(rotation);
                    break;
                case 270:
                    mOrientationHint = INVERSE_ORIENTATIONS.get(rotation);
                    break;
            }
            return this;
        }
        public Builder orientationHint(String cameraId, Activity activity) {
            int sensorOrientation = MxCameraUtils.with(activity).getOrientation(cameraId);
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            switch (sensorOrientation) {
                case 90:
                    mOrientationHint = DEFAULT_ORIENTATIONS.get(rotation);
                    break;
                case 270:
                    mOrientationHint = INVERSE_ORIENTATIONS.get(rotation);
                    break;
            }
            return this;
        }
        public Builder location(float latitude, float longitude) {
            mLatitude = latitude;
            mLongitud = longitude;
            mSetLocation = true;
            return this;
        }
        public MxMediaMuxer build() {
            if (mVideoFormat == null && mAudioFormat == null) return null;
            if (mOutputPath == null || mOutputPath.length() < 1) return null;
            try {
                MxMediaMuxer mediaMuxer = new MxMediaMuxer();
                mediaMuxer.mediaMuxer = new MediaMuxer(mOutputPath, mOutputFormat);
                if (mVideoFormat != null) mediaMuxer.videoTrack = mediaMuxer.mediaMuxer.addTrack(mVideoFormat);
                if (mAudioFormat != null) mediaMuxer.audioTrack = mediaMuxer.mediaMuxer.addTrack(mAudioFormat);
                mediaMuxer.mediaMuxer.setOrientationHint(mOrientationHint);
                if (mSetLocation && Build.VERSION.SDK_INT >= 19) mediaMuxer.mediaMuxer.setLocation(mLatitude, mLongitud);
                return mediaMuxer;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
        private Builder() {

        }
    }

    private MxMediaMuxer() {
    }
    public MxMediaMuxer start() {
        mediaMuxer.start();
        return this;
    }
    public int videoTrack() {
        return videoTrack;
    }
    public int audioTrack() {
        return audioTrack;
    }
    public void writeVideo(MediaCodec.BufferInfo bufferInfo, ByteBuffer byteBuf) {
        write(videoTrack, bufferInfo, byteBuf);
    }
    public void write(int trackIndex, MediaCodec.BufferInfo bufferInfo, ByteBuffer byteBuf) {
        try {
            if (trackIndex == videoTrack && bufferInfo.presentationTimeUs <= latestVideoTimecode) {
                Log.w("dylan", "invalid timestamp: " + bufferInfo.presentationTimeUs + " <= " + latestVideoTimecode);
                return;
            } else if (trackIndex == videoTrack) {
                latestVideoTimecode = bufferInfo.presentationTimeUs;
            }
            if (trackIndex == audioTrack && bufferInfo.presentationTimeUs <= latestAudioTimecode) {
                Log.w("dylan", "invalid timestamp: " + bufferInfo.presentationTimeUs + " <= " + latestAudioTimecode);
                return;
            } else if (trackIndex == audioTrack) {
                latestAudioTimecode = bufferInfo.presentationTimeUs;
            }
            if (bufferInfo.size < 0 || bufferInfo.offset < 0 || (bufferInfo.offset + bufferInfo.size) > byteBuf.capacity() || bufferInfo.presentationTimeUs < 0) {
                Log.e("dylan", "invalid data");
                return;
            }
            mediaMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
        } catch (Exception ex) {
            Log.e("dylan", "invalidbuffer: bufferSize=" + bufferInfo.size + ", offset=" + bufferInfo.offset + ", capacity=" + byteBuf.capacity() + ", timestamp=" + bufferInfo.presentationTimeUs);
            ex.printStackTrace();
        }
    }
    public void writeVideo(long timecode, byte[] data, int offset, int size, boolean key) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data, offset, size);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        bufferInfo.flags = key ? MediaCodec.BUFFER_FLAG_KEY_FRAME : 0;
        bufferInfo.presentationTimeUs = timecode * 1000;
        bufferInfo.offset = byteBuffer.position();
        bufferInfo.size = size;
        write(videoTrack, bufferInfo, byteBuffer);
    }
    public void writeAudio(MediaCodec.BufferInfo bufferInfo, ByteBuffer byteBuf) {
        write(audioTrack, bufferInfo, byteBuf);
    }
    public void writeAudio(long timecode, byte[] data, int offset, int size) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data, offset, size);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        bufferInfo.presentationTimeUs = timecode * 1000;
        bufferInfo.offset = byteBuffer.position();
        bufferInfo.size = size;
        write(audioTrack, bufferInfo, byteBuffer);
    }
    public void stop() {
        try {
            if (mediaMuxer != null) {
                mediaMuxer.stop();
                mediaMuxer.release();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        mediaMuxer = null;
    }

    private MediaMuxer mediaMuxer = null;
    private int videoTrack = -1;
    private int audioTrack = -1;
    private long latestVideoTimecode = -1;
    private long latestAudioTimecode = -1;

    //////////////////////////////////////////////////////////////////
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }
}
