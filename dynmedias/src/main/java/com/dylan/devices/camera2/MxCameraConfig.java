package com.dylan.devices.camera2;

import android.app.Activity;
import android.util.Size;

import java.lang.ref.WeakReference;

public class MxCameraConfig {
    public WeakReference<Activity> activity;
    public String cameraId;
    public Size previewSize;

    public static class Builder {
        private MxCameraUtils mMxCameraUtils = null;
        private Activity activity = null;
        private String cameraId;
        private Size previewSize;

        public Builder(Activity activity) {
            this.activity = activity;
            mMxCameraUtils = MxCameraUtils.with(activity);
        }
        public Builder useFrontCamera() {
            this.cameraId = mMxCameraUtils.frontCamera();
            return this;
        }
        public Builder useBackCamera() {
            this.cameraId = mMxCameraUtils.backCamera();
            return this;
        }
        public Builder useCameraId(String cameraId) {
            this.cameraId = cameraId;
            return this;
        }
        public Builder previewSize(int width, int height) {
            this.previewSize = new Size(width, height);
            return this;
        }
        public Builder previewSize(Size size) {
            this.previewSize = size;
            return this;
        }

        public MxCameraConfig build() {
            return new MxCameraConfig(this);
        }
    }

    private MxCameraConfig(Builder builder) {
        this.activity = new WeakReference<Activity>(builder.activity);
        this.cameraId = builder.cameraId;
        this.previewSize = builder.previewSize;
    }
}
