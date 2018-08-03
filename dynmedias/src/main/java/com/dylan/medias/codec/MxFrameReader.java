package com.dylan.medias.codec;

import android.content.Context;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;

import com.dylan.exceptions.MxException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;

public class MxFrameReader {
    public interface Callback {
        boolean onFrame(Image image);
    }

    public class ReadException extends MxException {
        public ReadException(Throwable cause) {
            super(cause);
        }
        public ReadException(String message, Throwable cause) {
            super(message, cause);
        }
        public ReadException(String message) {
            super(message);
        }
        @Override
        public String summary() {
            return "帧序列读取错误";
        }
    }

    private Size mImageSize = null;
    private int mImageFormat = ImageFormat.UNKNOWN;
    private int mImageCount = 2;
    private boolean mAllowDropFrame = false;

    private ImageReader mImageReader = null;
    private Surface mReaderSurface = null;
    private ReplaySubject<Image> mSubject = null;

    public static MxFrameReader with(Context context) {
        return new MxFrameReader();
    }
    public MxFrameReader size(int width, int height) {
        mImageSize = new Size(width, height);
        return this;
    }
    public MxFrameReader size(Size size) {
        mImageSize = size;
        return this;
    }
    public MxFrameReader format(int format) {
        mImageFormat = format;
        return this;
    }
    public MxFrameReader yv12() {
        return format(ImageFormat.YV12);
    }
    public MxFrameReader nv21() {
        return format(ImageFormat.NV21);
    }
    public MxFrameReader cacheCount(int count) {
        mImageCount = count;
        return this;
    }
    public MxFrameReader allowDrop(boolean allow) {
        mAllowDropFrame = allow;
        return this;
    }
    public MxFrameReader allowDrop() {
        return allowDrop(true);
    }
    public MxFrameReader open() throws MxException {
        if (mImageFormat == ImageFormat.UNKNOWN)
            throw new ReadException("The image format is not set yet.");
        if (mImageSize == null || mImageSize.getWidth() < 1 || mImageSize.getHeight() < 1)
            throw new ReadException("The image size is not set yet.");
        if (mImageCount < 1)
            mImageCount = 1;
        close();
        mImageReader = ImageReader.newInstance(mImageSize.getWidth(), mImageSize.getHeight(), mImageFormat, mImageCount);
        mReaderSurface = mImageReader.getSurface();
        return this;
    }
    public Surface surface() {
        return mReaderSurface;
    }
    public Observable<Image> start() {
        if (mImageReader == null) return null;
        startBackgroundThread();
        mSubject = ReplaySubject.create();
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = null;
                try {
                    image = reader.acquireNextImage();
                    if (mSubject != null && mSubject.hasObservers()) {
                        Image.Plane[] planes = image.getPlanes();
                        for (Image.Plane plane : planes) {
                            plane.getBuffer().mark();
                        }
                        mSubject.onNext(image);
                    } else {
                        image.close();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    if (!mAllowDropFrame) {
                        ex.printStackTrace();
                        if (mSubject != null && mSubject.hasObservers()) {
                            mSubject.onError(new ReadException(ex));
                        }
                        mSubject = null;
                    }
                    if (image != null)image.close();
                }
            }
        }, mBackgroundHandler);
        return mSubject.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
    public MxFrameReader start(final Callback callback) {
        if (mImageReader == null) {
            throw new RuntimeException("The image reader is not inited.");
        }
        startBackgroundThread();
        mSubject = ReplaySubject.create();
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = null;
                boolean needClose = true;
                try {
                    image = reader.acquireNextImage();
                    if (callback != null) {
                        Image.Plane[] planes = image.getPlanes();
                        for (Image.Plane plane : planes) {
                            plane.getBuffer().mark();
                        }
                        needClose = !callback.onFrame(image);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if (image != null && needClose)image.close();
                }
            }
        }, mBackgroundHandler);
        return this;
    }
    public void recycle(Image image) {
        image.close();
    }
    public void close() {
        mReaderSurface = null;
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
        stopBackgroundThread();
    }

    private HandlerThread mBackgroundThread = null;
    private Handler mBackgroundHandler = null;
    private void startBackgroundThread() {
        stopBackgroundThread();
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.setPriority(Thread.MAX_PRIORITY);
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
}
