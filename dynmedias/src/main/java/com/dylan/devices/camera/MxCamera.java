package com.dylan.devices.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.dylan.exceptions.MxException;
import com.dylan.medias.codec.MxFrame;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.view.Surface.ROTATION_0;

public class MxCamera implements Camera.PreviewCallback {
    public class CameraException extends MxException {
        public CameraException(Throwable cause) {
            super(cause);
        }
        public CameraException(String message) {
            super(message);
        }
        @Override
        public String summary() {
            return "摄像头错误";
        }
    }

    private Context mContext = null;
    public static Observable<MxCamera> open(MxCameraConfig config) {
        MxCamera camera = new MxCamera(config);
        return camera.open();
    }
    public void preview(SurfaceView surfaceView) {
        preview(surfaceView, null);
    }
    public void preview(SurfaceView surfaceView, Activity rotateRefer) {
        if (rotateRefer != null) {
            mDisplayRotation = rotateRefer.getWindowManager().getDefaultDisplay().getRotation();
        }
        mContext = surfaceView.getContext();
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                setHolder(holder);
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                setHolder(holder);
            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                try {
                    if (mCamera != null) {
                        mCamera.stopPreview();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        if (surfaceView.getHolder().getSurface() != null) {
            setHolder(surfaceView.getHolder());
        }
    }
    public void read(Callback callback) {
        synchronized (this) {
            mCallback = callback;
        }
    }
    public Observable<Boolean> focus() {
        return rx(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        subscriber.onNext(success);
                        subscriber.onCompleted();
                    }
                });
            }
        });
    }
    public Observable<File> shutter(final File file){
        return rx(new Observable.OnSubscribe<File>() {
            @Override
            public void call(final Subscriber<? super File> subscriber) {
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        try {
                            FileOutputStream fos = new FileOutputStream(file.getAbsoluteFile());
                            fos.write(data);
                            fos.close();
                            subscriber.onNext(file);
                            subscriber.onCompleted();
                        } catch (Exception e) {
                            e.printStackTrace();
                            subscriber.onError(new CameraException(e));
                        }
                        try {
                            mCamera.startPreview();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        });
    }
    public void close() {
        mWorking = false;
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public class Frame implements MxFrame {
        private ByteBuffer buffer = null;
        private byte[] datas = null;
        private int imageFormat = 0;
        private long timeStamp = 0;
        private int width = 0;
        private int height = 0;
        private Camera camera = null;

        private Frame(int width, int height, Camera camera) {
            this.buffer = ByteBuffer.allocateDirect(width * height * 3 / 2);
            this.buffer.position(0);
            this.datas = this.buffer.array();
            this.width = width;
            this.height = height;
            this.camera = camera;
        }
        private void setImageFormat(int imageFormat) {
            this.imageFormat = imageFormat;
        }
        public int getFormat() {
            return imageFormat;
        }
        @Override
        public ByteBuffer getDirectBuffer() {
            return buffer;
        }
        public byte[] getDatas() {
            return datas;
        }
        public long getTimeStamp() {
            return timeStamp;
        }
        public int getWidth() {
            return width;
        }
        public int getHeight() {
            return height;
        }
        public void close() {
            camera.addCallbackBuffer(datas);
        }
    }

    public interface Callback {
        boolean onFrame(Frame frame);
    }

    private int mCameraId = 0;
    private Camera mCamera;
    private int mDisplayRotation = 90;
    private Size mPreviewSize;
    private Size mVideoSize;
    private Size mPictureSize;
    private int[] mPreviewFps = null;
    private int mPreviewFormat = ImageFormat.YV12;
    private int mPictureFormat = ImageFormat.JPEG;
    private int mCacheCount;
    private boolean mWorking = true;
    private Callback mCallback = null;

    private Map<byte[], Frame> mFrameCache = new HashMap<>();

    private MxCamera(MxCameraConfig config) {
        mCameraId = config.cameraId;
        mPreviewSize = config.previewSize;
        mVideoSize = config.videoSize;
        mPictureSize = config.pictureSize;
        mPreviewFps = config.previewFps;
        mPreviewFormat = config.previewFormat;
        mPictureFormat = config.pictureFormat;
        mCacheCount = config.cacheCount;
    }
    private Observable<MxCamera> open() {
        return rx(new Observable.OnSubscribe<MxCamera>() {
            @Override
            public void call(Subscriber<? super MxCamera> subscriber) {
                try {
                    mCamera = MxCameraUtils.camera(mCameraId);
                    if (mCamera == null) throw new Exception("Open camera failed.");
                    mFrameCache.clear();
                    for (int i = 0; i < mCacheCount; i++) {
                        Frame frame = new Frame(mPreviewSize.getWidth(), mPreviewSize.getHeight(), mCamera);
                        mFrameCache.put(frame.getDatas(), frame);
                        mCamera.addCallbackBuffer(frame.getDatas());
                    }
                    try {
                        Camera.Parameters p = mCamera.getParameters();
                        p.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                        if (mPictureSize != null) {
                            p.setPictureSize(mPictureSize.getWidth(), mPictureSize.getHeight());
                        }
                        p.setPreviewFormat(mPreviewFormat);
                        p.setPictureFormat(mPictureFormat);
                        if (mPreviewFps != null && mPreviewFps.length == 2) {
                            p.setPreviewFpsRange(mPreviewFps[0] * 1000, mPreviewFps[1] * 1000);
                        }
                        mCamera.setParameters(p);
                    } catch (Exception ex) {
                        Camera.Parameters p = mCamera.getParameters();
                        p.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                        if (mPictureSize != null) {
                            p.setPictureSize(mPictureSize.getWidth(), mPictureSize.getHeight());
                        }
                        p.setPreviewFormat(mPreviewFormat);
                        p.setPictureFormat(mPictureFormat);
                        mCamera.setParameters(p);
                    }
                    int format = mCamera.getParameters().getPreviewFormat();
                    for (byte[] key : mFrameCache.keySet()) {
                        mFrameCache.get(key).setImageFormat(format);
                    }
                    subscriber.onNext(MxCamera.this);
                    subscriber.onCompleted();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    subscriber.onError(new CameraException(ex));
                }
            }
        });
    }
    private void setRotation() {
        if (mCamera == null) return;
        int degrees = 0;
        switch (mDisplayRotation) {
            case ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, info);
        int result = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
    }
    private void setHolder(SurfaceHolder holder) {
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            if (mCamera != null) {
                setRotation();
                holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                mCamera.setPreviewDisplay(holder);
                mCamera.setPreviewCallbackWithBuffer(MxCamera.this);
                mCamera.startPreview();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private long mLatestTimeStamp = Surface.ROTATION_0;
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (!mWorking) return;
        Frame frame = mFrameCache.get(data);
        if (frame == null) return;
        frame.timeStamp = System.currentTimeMillis() * 1000;
        if (frame.timeStamp <= mLatestTimeStamp) {
            frame.timeStamp = mLatestTimeStamp + 1;
        }
        mLatestTimeStamp = frame.timeStamp;
        synchronized (MxCamera.this) {
            if (mCallback == null || !mCallback.onFrame(frame)) {
                mCamera.addCallbackBuffer(data);
            }
        }
    }

    private static <T> Observable<T> rx(Observable.OnSubscribe<T> f) {
        return Observable.create(f).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
