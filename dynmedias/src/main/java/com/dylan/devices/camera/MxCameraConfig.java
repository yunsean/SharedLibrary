package com.dylan.devices.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.util.Size;

import java.lang.ref.WeakReference;

public class MxCameraConfig {
    public WeakReference<Context> context;
    public int cameraId;
    public Size previewSize;
    public Size videoSize;
    public Size pictureSize;
    public int[] previewFps;
    public int previewFormat;
    public int pictureFormat;
    public int cacheCount;

    public static class Builder {
        private Context context = null;
        private int cameraId;
        private Size previewSize;
        private Size videoSize;
        private Size pictureSize;
        private int[] previewFps = null;
        private int previewFormat = ImageFormat.NV21;
        private int pictureFormat = ImageFormat.JPEG;
        private int cacheCount = 2;

        public Builder(Context context) {
            this.context = context;
        }
        public Builder useFrontCamera() {
            return useCameraId(MxCameraUtils.frontCameraId());
        }
        public Builder useBackCamera() {
            return useCameraId(MxCameraUtils.backCameraId());
        }
        public Builder useCameraId(int cameraId) {
            this.cameraId = cameraId;
            return this;
        }
        public Builder videoSize(int width, int height) {
            this.videoSize = new Size(width, height);
            return this;
        }
        public Builder videoSize(Size size) {
            this.videoSize = size;
            return this;
        }
        public Builder pictureSize(int width, int height) {
            this.pictureSize = new Size(width, height);
            return this;
        }
        public Builder pictureSize(Size size) {
            this.pictureSize = size;
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
        public Builder previewFps(int min, int max) {
            this.previewFps = new int[]{min, max};
            return this;
        }
        public Builder previewFormat(int imageFormat) {
            this.previewFormat = imageFormat;
            return this;
        }
        public Builder pictureFormat(int imageFormat) {
            this.pictureFormat = imageFormat;
            return this;
        }
        public Builder cacheCount(int cacheCount) {
            this.cacheCount = cacheCount;
            return this;
        }

        public MxCameraConfig build() {
            return new MxCameraConfig(this);
        }
    }

    private MxCameraConfig(Builder builder) {
        this.context = new WeakReference<Context>(builder.context);
        this.cameraId = builder.cameraId;
        this.previewSize = builder.previewSize;
        this.videoSize = builder.videoSize;
        this.pictureSize = builder.pictureSize;
        this.previewFps = builder.previewFps;
        this.previewFormat = builder.previewFormat;
        this.pictureFormat = builder.pictureFormat;
        this.cacheCount = builder.cacheCount;
    }
}
