package com.dylan.devices.camera2;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.util.Size;
import android.view.SurfaceHolder;

public class MxCameraUtils {
    public static MxCameraUtils with(Context context) {
        return new MxCameraUtils(context);
    }
    public String[] ids() {
        return getCameraList();
    }
    public String backCamera() {
        return getCameraWithFacing(CameraCharacteristics.LENS_FACING_BACK);
    }
    public String frontCamera() {
        return getCameraWithFacing(CameraCharacteristics.LENS_FACING_FRONT);
    }
    public int[] outputFormats(String id) {
        return getSupportedFormats(id);
    }
    public Size[] formatSizes(String id, int outputFormat) {
        return getSupportedSizes(id, outputFormat);
    }
    public Size[] photoSizes(String id) {
        return getSupportedSizes(id, ImageFormat.JPEG);
    }
    public Size[] surfaceSizes(String id) {
        return getSupportedSizes(id, SurfaceHolder.class);
    }
    public Size[] textureSizes(String id) {
        return getSupportedSizes(id, SurfaceTexture.class);
    }
    public Size[] recordSizes(String id) {
        return getSupportedSizes(id, MediaRecorder.class);
    }
    public Size[] codecSizes(String id) {
        return getSupportedSizes(id, MediaCodec.class);
    }
    public StreamConfigurationMap configurations(String id) {
        return getConfigurationMap(id);
    }
    public Integer orientation(String id) {
        return getOrientation(id);
    }

    private CameraManager mCameraManager = null;
    private MxCameraUtils(Context context) {
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }
    private String[] getCameraList() {
        try {
            return mCameraManager.getCameraIdList();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    private String getCameraWithFacing(int facing) {
        try {
            for (final String cameraId : mCameraManager.getCameraIdList()){
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == facing) {
                    return cameraId;
                }
            }
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public <T> Size[] getSupportedSizes(String cameraId, Class<T> klass) {
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            return map.getOutputSizes(klass);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public Size[] getSupportedSizes(String cameraId, int format) {
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            return map.getOutputSizes(format);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public int[] getSupportedFormats(String cameraId) {
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            return map.getOutputFormats();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public int getOrientation(String cameraId) {
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
            return characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }
    public StreamConfigurationMap getConfigurationMap(String cameraId) {
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            return map;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
