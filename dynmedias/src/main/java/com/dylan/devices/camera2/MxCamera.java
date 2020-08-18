package com.dylan.devices.camera2;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;

import com.dylan.exceptions.MxException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MxCamera implements ImageReader.OnImageAvailableListener {

    public class AutoFocusException extends MxException {
        public AutoFocusException(Throwable cause) {
            super(cause);
        }
        public AutoFocusException(String message) {
            super(message);
        }
        @Override
        public String summary() {
            return "自动对焦错误";
        }
    }
    public class AutoExposureException extends MxException {
        public AutoExposureException(Throwable cause) {
            super(cause);
        }
        public AutoExposureException(String message) {
            super(message);
        }
        @Override
        public String summary() {
            return "自动白平衡错误";
        }
    }
    public class SurfaceException extends MxException {
        public SurfaceException(Throwable cause) {
            super(cause);
        }
        @Override
        public String summary() {
            return "绑定预览对象错误";
        }
    }
    public class BuildException extends MxException {
        public BuildException(Throwable cause) {
            super(cause);
        }
        public BuildException(String message) {
            super(message);
        }
        @Override
        public String summary() {
            return "初始化预览错误";
        }
    }
    public class RequestException extends MxException {
        public RequestException(Throwable cause) {
            super(cause);
        }
        @Override
        public String summary() {
            return "构建采集请求错误";
        }
    }
    public class OpenException extends MxException {
        public OpenException(Throwable cause) {
            super(cause);
        }
        public OpenException(String message, Throwable cause) {
            super(message, cause);
        }
        public OpenException(String message) {
            super(message);
        }
        @Override
        public String summary() {
            return "打开摄像头错误";
        }
    }
    public class TakeException extends MxException {
        public TakeException(Throwable cause) {
            super(cause);
        }
        public TakeException(String message) {
            super(message);
        }
        @Override
        public String summary() {
            return "拍摄照片错误";
        }
    }

    public String id() {
        return mCameraId;
    }
    public int[] outputFormats() {
        return getSupportedFormats();
    }
    public Size[] formatSizes(int outputFormat) {
        return getSupportedSizes(outputFormat);
    }
    public Size[] photoSizes() {
        return getSupportedSizes(ImageFormat.JPEG);
    }
    public Size[] surfaceSizes() {
        return getSupportedSizes(SurfaceHolder.class);
    }
    public Size[] textureSizes() {
        return getSupportedSizes(SurfaceTexture.class);
    }
    public Size[] recordSizes() {
        return getSupportedSizes(MediaRecorder.class);
    }
    public Size[] codecSizes() {
        return getSupportedSizes(MediaCodec.class);
    }
    public StreamConfigurationMap configurations(String id) {
        return getConfigurationMap();
    }
    public int orientation() {
        return getOrientation();
    }

    public static Observable<MxCamera> open(final MxCameraConfig config) {
        MxCamera mxCamera = new MxCamera(config);
        return mxCamera.openInternal();
    }
    public Observable<MxCamera> bind(final TextureView textureView) {
        return bindTextureInternal(textureView);
    }
    public void preview() {
        previewInternal();
    }
    public MxCamera clear() {
        closePreviewSession();
        closeImageReader();
        mSurfaces.clear();
        return this;
    }
    public MxCamera bind(Surface surface) {
        mSurfaces.add(surface);
        return this;
    }
    public MxCamera bindImageReader(Size imageSize) {
        setupImageReaderInternal(imageSize);
        return this;
    }
    public MxCamera bindPreview() {
        bindPreviewSurfaceInternal();;
        return this;
    }
    public Observable<MxCamera> request(int templateType) {
        return requestInternal(templateType, null);
    }
    public Observable<MxCamera> requestPhoto(Size imageSize) {
        return requestInternal(CameraDevice.TEMPLATE_STILL_CAPTURE, imageSize);
    }
    public Observable<MxCamera> build() {
        return buildInternal();
    }
    public Observable<MxCamera> focus(int timeout) {
        return focusInternal(timeout);
    }
    public Observable<MxCamera> ae(int timeout) {
        return aeInternal(timeout);
    }
    public Observable<File> shutter(File file) {
        return shutterInternal(file);
    }
    public void close() {
        closeInternal();
    }

    private CameraManager mCameraManager = null;
    private WeakReference<Activity> mActivity = null;
    private String mCameraId = null;
    private Size mPreviewSize = null;
    private boolean mDimensionSwapped = false;
    private MxCamera(MxCameraConfig config) {
        mCameraManager = (CameraManager) config.activity.get().getSystemService(Context.CAMERA_SERVICE);
        mActivity = config.activity;
        mCameraId = config.cameraId;
        mPreviewSize = config.previewSize;
    }

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private CameraDevice mCameraDevice = null;
    private TextureView mTextureView = null;
    private HandlerThread mBackgroundThread = null;
    private Handler mBackgroundHandler = null;
    private Observable<MxCamera> openInternal() {
        return rx(new Observable.OnSubscribe<MxCamera>() {
            @Override
            public void call(final Subscriber<? super MxCamera> subscriber) {
                try {
                    Activity activity = null;
                    if (mActivity == null || (activity = mActivity.get()) == null)
                        throw new IllegalArgumentException("The activity is null.");
                    if (mCameraId == null)
                        throw new IllegalArgumentException("The camera id is not set yet.");
                    if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS))
                        throw new RuntimeException("Time out waiting to lock camera opening.");
                    startBackgroundThread();

                    CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
                    StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    mCameraManager.openCamera(mCameraId, new CameraDevice.StateCallback() {
                        @Override
                        public void onOpened(CameraDevice cameraDevice) {
                            mCameraDevice = cameraDevice;
                            mCameraOpenCloseLock.release();
                            subscriber.onNext(MxCamera.this);
                            subscriber.onCompleted();
                        }
                        @Override
                        public void onDisconnected(CameraDevice cameraDevice) {
                            cameraDevice.close();
                            mCameraDevice = null;
                            mCameraOpenCloseLock.release();
                        }
                        @Override
                        public void onError(CameraDevice cameraDevice, int error) {
                            cameraDevice.close();
                            mCameraDevice = null;
                            mCameraOpenCloseLock.release();
                            subscriber.onError(new OpenException("Open camera [" + mCameraId + "] failed: " + error));
                        }
                    }, mBackgroundHandler);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    subscriber.onError(new OpenException(ex));
                }
            }
        });
    }
    private void closeInternal() {
        closePreviewSession();
        closeCamera();
        closeImageReader();
        stopBackgroundThread();
    }
    private Observable<MxCamera> bindTextureInternal(final TextureView textureView) {
        return rx(new Observable.OnSubscribe<MxCamera>() {
            @Override
            public void call(final Subscriber<? super MxCamera> subscriber) {
                if (textureView.isAvailable()) {
                    setupTextureInternal(subscriber, textureView);
                } else {
                    textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                        @Override
                        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                            setupTextureInternal(subscriber, textureView);
                        }
                        @Override
                        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                            configureTransform(width, height);
                        }
                        @Override
                        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                            return false;
                        }
                        @Override
                        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                        }
                    });
                }
            }
        });
    }
    private void setupTextureInternal(final Subscriber<? super MxCamera> subscriber, final TextureView textureView) {
        try {
            Activity activity = mActivity.get();
            Point displaySize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
            if (mPreviewSize == null) {
                mPreviewSize = new Size(displaySize.x, displaySize.y);
            }
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            boolean swappedDimensions = false;
            if ((displayRotation == Surface.ROTATION_0 || displayRotation == Surface.ROTATION_180) && (sensorOrientation == 90 || sensorOrientation == 270))
                swappedDimensions = true;
            else if ((displayRotation == Surface.ROTATION_90 || displayRotation == Surface.ROTATION_270) && (sensorOrientation == 0 || sensorOrientation == 180))
                swappedDimensions = true;
            int rotatedPreviewWidth = textureView.getWidth();
            int rotatedPreviewHeight = textureView.getHeight();
            int maxPreviewWidth = displaySize.x;
            int maxPreviewHeight = displaySize.y;
            if (swappedDimensions) {
                rotatedPreviewWidth = textureView.getHeight();
                rotatedPreviewHeight = textureView.getWidth();
                maxPreviewWidth = displaySize.y;
                maxPreviewHeight = displaySize.x;
            }
            mPreviewSize = chooseOptimalSize(textureSizes(), rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, mPreviewSize);
            mTextureView = textureView;
            mDimensionSwapped = swappedDimensions;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
                }
            });
            subscriber.onNext(MxCamera.this);
            subscriber.onCompleted();
        } catch (Exception ex) {
            ex.printStackTrace();
            subscriber.onError(new SurfaceException(ex));
        }
    }

    private CameraCaptureSession mPreviewSession = null;
    private CaptureRequest.Builder mPreviewBuilder = null;
    private CaptureRequest mPreviewRequest = null;
    private List<Surface> mSurfaces = new ArrayList<>();
    private ImageReader mImageReader = null;
    private File mPhotoFile = null;
    private void setupImageReaderInternal(final Size photoSize) {
        Size imageSize = photoSize;
        if (imageSize == null) {
            imageSize = Collections.max(Arrays.asList(photoSizes()), new CompareSizesByArea());
        }
        closeImageReader();
        mImageReader = ImageReader.newInstance(imageSize.getWidth(), imageSize.getHeight(), ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(MxCamera.this, mBackgroundHandler);
    }
    private void bindPreviewSurfaceInternal() {
        if (mTextureView != null) {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(texture);
            mSurfaces.add(previewSurface);
        }
    }
    private Observable<MxCamera> requestInternal(final int templateType, final Size photoSize) {
        return rx(new Observable.OnSubscribe<MxCamera>() {
            @Override
            public void call(final Subscriber<? super MxCamera> subscriber) {
                try {
                    if (mCameraDevice == null)
                        throw new IllegalStateException("MxCamera is not opened yet.");
                    if (mTextureView != null && !mTextureView.isAvailable())
                        throw new IllegalStateException("The texture view is not available.");
                    if (mPreviewSize == null)
                        throw new IllegalStateException("The preview size is not set yet.");
                    closePreviewSession();
                    mPreviewBuilder = mCameraDevice.createCaptureRequest(templateType);
                    if (templateType == CameraDevice.TEMPLATE_STILL_CAPTURE) {
                        setupImageReaderInternal(photoSize);
                    }
                    subscriber.onNext(MxCamera.this);
                    subscriber.onCompleted();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    subscriber.onError(new RequestException(ex));
                }
            }
        });
    }
    private Observable<MxCamera> buildInternal() {
        return rx(new Observable.OnSubscribe<MxCamera>() {
            @Override
            public void call(final Subscriber<? super MxCamera> subscriber) {
                try {
                    closePreviewSession();
                    mCameraOpenCloseLock.acquire();
                    for (Surface surface : mSurfaces) {
                        mPreviewBuilder.addTarget(surface);
                    }
                    List<Surface> outputs = new ArrayList<Surface>(mSurfaces);
                    if (mImageReader != null) {
                        outputs.add(mImageReader.getSurface());
                    }
                    mCameraDevice.createCaptureSession(outputs, new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            try {
                                mPreviewSession = cameraCaptureSession;
                                mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
                                mPreviewRequest = mPreviewBuilder.build();
                                mPreviewSession.setRepeatingRequest(mPreviewRequest, null, mBackgroundHandler);
                                subscriber.onNext(MxCamera.this);
                                subscriber.onCompleted();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                subscriber.onError(new BuildException(ex));
                            }
                            mCameraOpenCloseLock.release();
                        }
                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                            mCameraOpenCloseLock.release();
                            subscriber.onError(new BuildException("Create preview session failed."));
                        }
                    }, mBackgroundHandler);
                } catch (Exception ex) {
                    mCameraOpenCloseLock.release();
                    ex.printStackTrace();
                    subscriber.onError(new BuildException(ex));
                }
            }
        });
    }
    private void previewInternal() {
        try {
            clear()
                    .bindPreview()
                    .request(CameraDevice.TEMPLATE_PREVIEW)
                    .flatMap(
                            new Func1<MxCamera, Observable<MxCamera>>() {
                                @Override
                                public Observable<MxCamera> call(MxCamera mxCamera) {
                                    return mxCamera.build();
                                }
                            }).subscribe(new Observer<MxCamera>() {
                                             @Override
                                             public void onCompleted() {
                                             }
                                             @Override
                                             public void onError(Throwable e) {
                                             }
                                             @Override
                                             public void onNext(MxCamera camera) {
                                             }
                                         }
                    );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private Observable<MxCamera> focusInternal(final int timeout) {
        return rx(new Observable.OnSubscribe<MxCamera>() {
            private boolean mFocusLocked = false;
            @Override
            public void call(final Subscriber<? super MxCamera> subscriber) {
                try {
                    mCameraOpenCloseLock.acquire();
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
                    mPreviewSession.capture(mPreviewBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
                            process(partialResult);
                        }
                        @Override
                        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                            process(result);
                        }
                        private void process(CaptureResult result) {
                            Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                            Log.e("dylan", "afState=" + (afState == null ? "null" : afState));
                            if (afState == null || afState == CaptureResult.CONTROL_AF_STATE_INACTIVE || CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                                mCameraOpenCloseLock.release();
                                subscriber.onNext(MxCamera.this);
                                subscriber.onCompleted();
                                mFocusLocked = true;
                            }
                        }
                    }, mBackgroundHandler);
                    if (timeout > 0) {
                        Observable.timer(timeout, TimeUnit.MILLISECONDS).subscribe(new Action1<Long>() {
                            @Override
                            public void call(Long aLong) {
                                if (!mFocusLocked) {
                                    unlockFocus();
                                    mCameraOpenCloseLock.release();
                                    subscriber.onError(new AutoFocusException("Auto focus timeout."));
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    mCameraOpenCloseLock.release();
                    e.printStackTrace();
                    subscriber.onError(new AutoFocusException(e));
                }
            }
        });
    }
    private Observable<MxCamera> aeInternal(final int timeout) {
        return rx(new Observable.OnSubscribe<MxCamera>() {
            private boolean mAELocked = false;
            @Override
            public void call(final Subscriber<? super MxCamera> subscriber) {
                try {
                    mCameraOpenCloseLock.acquire();
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
                    mPreviewSession.capture(mPreviewBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
                            process(partialResult);
                        }
                        @Override
                        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                            process(result);
                        }
                        private void process(CaptureResult result) {
                            Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                            Log.e("dylan", "aeState=" + (aeState == null ? "null" : aeState));
                            if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_LOCKED) {
                                mCameraOpenCloseLock.release();
                                subscriber.onNext(MxCamera.this);
                                subscriber.onCompleted();
                                mAELocked = true;
                            }
                        }
                    }, mBackgroundHandler);
                    if (timeout > 0) {
                        Observable.timer(timeout, TimeUnit.MILLISECONDS).subscribe(new Action1<Long>() {
                            @Override
                            public void call(Long aLong) {
                                if (!mAELocked) {
                                    unlockFocus();
                                    mCameraOpenCloseLock.release();
                                    subscriber.onError(new AutoExposureException("Auto white balance timeout."));
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    mCameraOpenCloseLock.release();
                    e.printStackTrace();
                    subscriber.onError(new AutoExposureException(e));
                }
            }
        });
    }
    private Observable<File> shutterInternal(final File file) {
        return rx(new Observable.OnSubscribe<File>() {
            @Override
            public void call(final Subscriber<? super File> subscriber) {
                try {
                    if (mPhotoFile != null)
                        throw new IllegalStateException("Another still capture is working.");
                    mPhotoFile = file;
                    Activity activity = null;
                    if (mActivity == null || (activity = mActivity.get()) == null)
                        throw new IllegalStateException("The activity is not set yet.");
                    if (mCameraDevice == null)
                        throw new IllegalStateException("The camera is not opened yet.");
                    if (mImageReader == null)
                        throw  new IllegalStateException("The image reader is not set yet.");
                    mCameraOpenCloseLock.acquire();
                    CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                    captureBuilder.addTarget(mImageReader.getSurface());
                    captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                    int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                    captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, IMAGE_ORIENTATIONS.get(rotation));
                    CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                            unlockFocus();
                            mCameraOpenCloseLock.release();
                            subscriber.onNext(file);
                            subscriber.onCompleted();
                            mPhotoFile = null;
                        }
                        @Override
                        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
                            mCameraOpenCloseLock.release();
                            subscriber.onError(new TakeException("Take photo error: " + failure.getReason()));
                            mPhotoFile = null;
                        }
                    };
                    mPreviewSession.stopRepeating();
                    mPreviewSession.capture(captureBuilder.build(), CaptureCallback, mBackgroundHandler);
                } catch (Exception e) {
                    mCameraOpenCloseLock.release();
                    e.printStackTrace();
                    subscriber.onError(new TakeException(e));
                }
            }
        });
    }

    private void startBackgroundThread() {
        stopBackgroundThread();
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    private void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            try {
                mBackgroundThread.quitSafely();
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void unlockFocus() {
        try {
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            mPreviewSession.capture(mPreviewBuilder.build(), null, mBackgroundHandler);
            mPreviewSession.setRepeatingRequest(mPreviewRequest, null, mBackgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mCameraOpenCloseLock.release();
        }
    }
    private void closePreviewSession() {
        if (mPreviewSession != null) {
            try {
                mPreviewSession.abortCaptures();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                mPreviewSession.close();
                mPreviewSession = null;
            }
        }
    }
    private void closeImageReader() {
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mPhotoFile));
    }

    ////////////////////////////////////////////////////////////////
    public <T> Size[] getSupportedSizes(Class<T> klass) {
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            return map.getOutputSizes(klass);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public Size[] getSupportedSizes(int format) {
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            return map.getOutputSizes(format);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public int[] getSupportedFormats() {
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            return map.getOutputFormats();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public StreamConfigurationMap getConfigurationMap() {
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            return map;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public int getOrientation() {
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            return characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    ////////////////////////////////////////////////////////////////
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = mActivity.get();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        Matrix matrix = new Matrix();
        bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
        matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
        float scale = 1.f;
        if (mDimensionSwapped) {
            scale = Math.min((float) viewRect.width() / bufferRect.width(), (float) viewRect.height() / bufferRect.height());
        } else {
            scale = Math.min((float) viewRect.width() / bufferRect.height(), (float) viewRect.height() / bufferRect.width());
        }
        matrix.postScale(scale, scale, centerX, centerY);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth, int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {
        List<Size> bigEnough = new ArrayList<>();
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight && option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            return choices[0];
        }
    }
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }
    public static <T> Observable<T> rx(Observable.OnSubscribe<T> f) {
        return Observable.create(f).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
    private static class ImageSaver implements Runnable {
        private final Image mImage;
        private final File mFile;
        public ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }
        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////
    private static final SparseIntArray IMAGE_ORIENTATIONS = new SparseIntArray();
    static {
        IMAGE_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        IMAGE_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        IMAGE_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        IMAGE_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
}
