package com.dylan.medias.codec;

import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;

import com.dylan.devices.camera2.MxCamera;
import com.dylan.devices.camera2.MxCameraUtils;

import java.io.FileDescriptor;
import java.io.IOException;

public class MxMediaRecorder {

    private MediaRecorder mMediaRecorder = null;
    public static MxMediaRecorder with(Context context) {
        return new MxMediaRecorder();
    }
    public MxMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
    }
    public MxMediaRecorder audioSource(int audioSource) {
        mMediaRecorder.setAudioSource(audioSource);
        return this;
    }
    public MxMediaRecorder audioMic() {
        return audioSource(MediaRecorder.AudioSource.MIC);
    }
    public MxMediaRecorder audio() {
        return audioSource(MediaRecorder.AudioSource.DEFAULT);
    }
    public MxMediaRecorder videoSource(int videoSource) {
        mMediaRecorder.setVideoSource(videoSource);
        return this;
    }
    public MxMediaRecorder videoSurface() {
        return videoSource(MediaRecorder.VideoSource.SURFACE);
    }
    public MxMediaRecorder videoCamera() {
        return videoSource(MediaRecorder.VideoSource.CAMERA);
    }
    public MxMediaRecorder video() {
        return videoSource(MediaRecorder.VideoSource.DEFAULT);
    }
    public MxMediaRecorder outputFormat(int outputFormat) {
        mMediaRecorder.setOutputFormat(outputFormat);
        return this;
    }
    public MxMediaRecorder mp4() {
        return outputFormat(MediaRecorder.OutputFormat.MPEG_4);
    }
    public MxMediaRecorder mp4(String file) {
        outputFormat(MediaRecorder.OutputFormat.MPEG_4);
        return file(file);
    }
    public MxMediaRecorder fps(double fps) {
        mMediaRecorder.setCaptureRate(fps);
        mMediaRecorder.setVideoFrameRate((int)fps);
        return this;
    }
    public MxMediaRecorder orientationHint(int degrees) {
        mMediaRecorder.setOrientationHint(degrees);
        return this;
    }
    public MxMediaRecorder orientationHint(MxCamera camera, Activity activity) {
        int sensorOrientation = camera.getOrientation();
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (sensorOrientation) {
            case 90:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case 270:
                mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }
        return this;
    }
    public MxMediaRecorder orientationHint(String cameraId, Activity activity) {
        int sensorOrientation = MxCameraUtils.with(activity).getOrientation(cameraId);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (sensorOrientation) {
            case 90:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case 270:
                mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }
        return this;
    }
    public MxMediaRecorder location(float latitude, float longitude) {
        mMediaRecorder.setLocation(latitude, longitude);
        return this;
    }
    public MxMediaRecorder videoSize(int width, int height) {
        mMediaRecorder.setVideoSize(width, height);
        return this;
    }
    public MxMediaRecorder videoSize(Size size) {
        mMediaRecorder.setVideoSize(size.getWidth(), size.getHeight());
        return this;
    }
    public MxMediaRecorder audioEncoder(int audioEncoder) {
        mMediaRecorder.setAudioEncoder(audioEncoder);
        return this;
    }
    public MxMediaRecorder aac() {
        return audioEncoder(MediaRecorder.AudioEncoder.AAC);
    }
    public MxMediaRecorder videoEncoder(int videoEncoder) {
        mMediaRecorder.setVideoEncoder(videoEncoder);
        return this;
    }
    public MxMediaRecorder avc() {
        return videoEncoder(MediaRecorder.VideoEncoder.H264);
    }
    public MxMediaRecorder sampleRate(int sampleRate) {
        mMediaRecorder.setAudioSamplingRate(sampleRate);
        return this;
    }
    public MxMediaRecorder audioChannel(int channel) {
        mMediaRecorder.setAudioChannels(channel);
        return this;
    }
    public MxMediaRecorder audioBitrate(int bitrate) {
        mMediaRecorder.setAudioEncodingBitRate(bitrate);
        return this;
    }
    public MxMediaRecorder videoBitrate(int bitrate) {
        mMediaRecorder.setVideoEncodingBitRate(bitrate);
        return this;
    }
    public MxMediaRecorder file(String outputFile) {
        mMediaRecorder.setOutputFile(outputFile);
        return this;
    }
    public MxMediaRecorder fd(FileDescriptor fd) {
        mMediaRecorder.setOutputFile(fd);
        return this;
    }
    public MxMediaRecorder previewIn(Surface surface) {
        mMediaRecorder.setPreviewDisplay(surface);
        return this;
    }
    public MxMediaRecorder prepare() throws IOException {
        mMediaRecorder.prepare();
        return this;
    }
    public MxMediaRecorder start() {
        mMediaRecorder.start();
        return this;
    }
    public Surface surface() {
        return mMediaRecorder.getSurface();
    }
    public MxMediaRecorder stop() {
        try {
            mMediaRecorder.stop();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return this;
    }
    public MxMediaRecorder reset() {
        try {
            mMediaRecorder.reset();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return this;
    }
    public void release() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }
    public MediaRecorder recorder() {
        return mMediaRecorder;
    }

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
