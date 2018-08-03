package com.dylan.devices.camera;

import android.hardware.Camera;
import android.util.Size;

import java.util.List;

public class MxCameraUtils {
    public static int count() {
        return Camera.getNumberOfCameras();
    }
    public static Camera defaultCamera() {
        return Camera.open();
    }
    public static int backCameraId() {
        return getCameraIdWithFacing(Camera.CameraInfo.CAMERA_FACING_BACK);
    }
    public static int frontCameraId() {
        return getCameraIdWithFacing(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }
    public static Camera backCamera() {
        return getCameraWithFacing(Camera.CameraInfo.CAMERA_FACING_BACK);
    }
    public static Camera frontCamera() {
        return getCameraWithFacing(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }
    public static Camera camera(int index) {
        if (index == -1)return null;
        try {
            Camera camera = Camera.open(index);
            if (camera != null) return camera;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    public static int[] previewFormats(Camera camera) {
        return getSupportedPreviewFormats(camera);
    }
    public static int[] pictureFormats(Camera camera) {
        return getSupportedPictureFormats(camera);
    }
    public static Size[] previewSizes(Camera camera) {
        return getSupportedPreviewSizes(camera);
    }
    public static Size[] surfaceSizes(Camera camera) {
        return getSupportedPreviewSizes(camera);
    }
    public static Size[] codecSizes(Camera camera) {
        return getSupportedPreviewSizes(camera);
    }
    public static Size[] photoSizes(Camera camera) {
        return getSupportedPictureSizes(camera);
    }
    public static Size[] recordSizes(Camera camera) {
        return getSupportedVideoSizes(camera);
    }

    private static String[] getCameraList() {
        try {
            int count = Camera.getNumberOfCameras();
            String[] list = new String[count];
            for (int i = 0; i < count; i++) {
                list[i] = String.valueOf(i);
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    private static Camera getCameraWithFacing(int facing) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int count = Camera.getNumberOfCameras();
        for (int index = 0; index < count; index++) {
            Camera.getCameraInfo(index, cameraInfo);
            if (cameraInfo.facing != facing)continue;
            try {
                Camera camera = Camera.open(index);
                if (camera != null) return camera;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
    private static int getCameraIdWithFacing(int facing) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int count = Camera.getNumberOfCameras();
        for (int index = 0; index < count; index++) {
            Camera.getCameraInfo(index, cameraInfo);
            if (cameraInfo.facing != facing)continue;
            return index;
        }
        return -1;
    }
    public static Size[] getSupportedVideoSizes(Camera camera) {
        if (camera == null) return null;
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedVideoSizes();
        Size[] result = new Size[sizes.size()];
        for (int i = 0; i < sizes.size(); i++) {
            Camera.Size size = sizes.get(i);
            result[i] = new Size(size.width, size.height);
        }
        return result;
    }
    public static Size[] getSupportedPictureSizes(Camera camera) {
        if (camera == null) return null;
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        Size[] result = new Size[sizes.size()];
        for (int i = 0; i < sizes.size(); i++) {
            Camera.Size size = sizes.get(i);
            result[i] = new Size(size.width, size.height);
        }
        return result;
    }
    public static Size[] getSupportedPreviewSizes(Camera camera) {
        if (camera == null) return null;
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Size[] result = new Size[sizes.size()];
        for (int i = 0; i < sizes.size(); i++) {
            Camera.Size size = sizes.get(i);
            result[i] = new Size(size.width, size.height);
        }
        return result;
    }

    public static int[] getSupportedPictureFormats(Camera camera) {
        if (camera == null) return null;
        Camera.Parameters parameters = camera.getParameters();
        List<Integer> formats = parameters.getSupportedPictureFormats();
        int[] result = new int[formats.size()];
        for (int i = 0; i < formats.size(); i++) {
            result[i] = formats.get(i);
        }
        return result;
    }
    public static int[] getSupportedPreviewFormats(Camera camera) {
        if (camera == null) return null;
        Camera.Parameters parameters = camera.getParameters();
        List<Integer> formats = parameters.getSupportedPreviewFormats();
        int[] result = new int[formats.size()];
        for (int i = 0; i < formats.size(); i++) {
            result[i] = formats.get(i);
        }
        return result;
    }
}
