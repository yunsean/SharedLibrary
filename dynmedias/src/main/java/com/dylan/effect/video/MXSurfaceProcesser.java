package com.dylan.effect.video;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Build;
import android.view.Surface;

import com.dylan.effect.egl.EGLConfigAttrs;
import com.dylan.effect.egl.EGLContextAttrs;
import com.dylan.effect.egl.EglHelper;
import com.dylan.effect.egl.EglUtils;
import com.dylan.effect.egl.FrameBuffer;
import com.dylan.effect.egl.MatrixUtils;
import com.dylan.effect.filter.BaseFilter;
import com.dylan.effect.filter.LazyFilter;
import com.dylan.effect.filter.OesFilter;
import com.dylan.effect.filter.Renderer;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class MXSurfaceProcesser {

    public static final int MATRIX_FITXY = 0;
    public static final int MATRIX_CENTERCROP = 1;
    public static final int MATRIX_CENTERINSIDE = 2;
    public static final int MATRIX_FITSTART = 3;
    public static final int MATRIX_FITEND = 4;

    private EglHelper eglHelper;
    private FrameBuffer sourceFrame;
    private int inputSurfaceTextureId;
    private SurfaceTexture inputSurfaceTexture;
    private int sourceWidth = 0;
    private int sourceHeight = 0;
    private WrapRenderer renderer = null;
    private boolean isLandscape = false;
    private List<SurfaceShower> surfaceShowers = new ArrayList<>();

    public MXSurfaceProcesser() {
        eglHelper = new EglHelper();
        if (!eglHelper.createGLESWithSurface(new EGLConfigAttrs(), new EGLContextAttrs(), new SurfaceTexture(1))) {
            throw new RuntimeException("Create Opengl ES failed");
        }
    }
    public MXSurfaceProcesser setRenderer(Renderer renderer) {
        this.renderer = new WrapRenderer(renderer);
        return this;
    }
    public MXSurfaceProcesser addObserver(Surface surface, int width, int height) {
        surfaceShowers.add(new SurfaceShower(width, height, surface, MATRIX_CENTERCROP));
        return this;
    }
    public MXSurfaceProcesser addObserver(Surface surface, int width, int height, int matrixType) {
        surfaceShowers.add(new SurfaceShower(width, height, surface, matrixType));
        return this;
    }
    public SurfaceTexture createInputSurface() {
        inputSurfaceTextureId = EglUtils.createTextureID(true);
        inputSurfaceTexture = new SurfaceTexture(inputSurfaceTextureId);
        sourceFrame = new FrameBuffer();
        return inputSurfaceTexture;
    }
    public MXSurfaceProcesser start(int width, int height, boolean landscape) {
        isLandscape = landscape;
        if (landscape) {
            sourceWidth = height;
            sourceHeight = width;
        } else {
            sourceWidth = width;
            sourceHeight = height;
        }
        if (renderer == null) renderer = new WrapRenderer(null);
        renderer.create();
        renderer.sizeChanged(sourceWidth, sourceHeight);
        renderer.setFlag(isLandscape ? WrapRenderer.TYPE_CAMERA : WrapRenderer.TYPE_MOVE);
        inputSurfaceTexture.setDefaultBufferSize(sourceWidth, sourceHeight);
        return this;
    }
    public void update() {
        inputSurfaceTexture.updateTexImage();
        inputSurfaceTexture.getTransformMatrix(renderer.getTextureMatrix());
        sourceFrame.bindFrameBuffer(sourceWidth, sourceHeight);
        GLES20.glViewport(0, 0, sourceWidth, sourceHeight);
        renderer.draw(inputSurfaceTextureId);
        sourceFrame.unBindFrameBuffer();
        int textureId = sourceFrame.getCacheTextureId();
        long timeStamp = inputSurfaceTexture.getTimestamp();
        for (SurfaceShower shower : surfaceShowers) {
            shower.show(textureId, timeStamp);
        }
    }
    public void destroy() {
        for (SurfaceShower shower : surfaceShowers) {
            shower.cleanup();
        }
        renderer.destroy();
        EGL14.eglMakeCurrent(eglHelper.getDisplay(), EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroyContext(eglHelper.getDisplay(), eglHelper.getDefaultContext());
        EGL14.eglTerminate(eglHelper.getDisplay());
    }

    private class SurfaceShower {
        private Object mSurface;
        private EGLSurface mShowSurface;
        private BaseFilter mFilter;
        private int mWidth;
        private int mHeight;
        private int mMatrixType = MatrixUtils.TYPE_CENTERINSIDE;

        public SurfaceShower(int width, int height, Object surface, int matrixType) {
            mWidth = width;
            mHeight = height;
            mSurface = surface;
            mMatrixType = matrixType;
        }
        public void show(int textureId, long timestamp) {
            if (mShowSurface == null) {
                mShowSurface = eglHelper.createWindowSurface(mSurface);
                mFilter = new LazyFilter();
                mFilter.create();
                mFilter.sizeChanged(sourceWidth, sourceHeight);
                MatrixUtils.getMatrix(mFilter.getVertexMatrix(), mMatrixType, sourceWidth, sourceHeight, mWidth, mHeight);
                MatrixUtils.flip(mFilter.getVertexMatrix(), false, true);
            }
            eglHelper.makeCurrent(mShowSurface);
            GLES20.glViewport(0, 0, mWidth, mHeight);
            mFilter.draw(textureId);
            eglHelper.swapBuffers(mShowSurface);
            eglHelper.setPresentationTime(mShowSurface, timestamp);
        }
        public void cleanup() {
            if (mShowSurface != null) {
                eglHelper.destroySurface(mShowSurface);
                mShowSurface = null;
            }
        }
    }

    private static class WrapRenderer implements Renderer {
        private Renderer mRenderer;
        private OesFilter mFilter;
        public static final int TYPE_MOVE = 0;
        public static final int TYPE_CAMERA = 1;
        public WrapRenderer(Renderer renderer) {
            this.mRenderer = renderer;
            mFilter = new OesFilter();
            setFlag(TYPE_MOVE);
        }
        public void setFlag(int flag) {
            if (flag == TYPE_MOVE) {
                mFilter.setVertexCo(new float[]{
                        -1.0f, 1.0f,
                        -1.0f, -1.0f,
                        1.0f, 1.0f,
                        1.0f, -1.0f,
                });
            } else if (flag == TYPE_CAMERA) {
                mFilter.setVertexCo(new float[]{
                        -1.0f, -1.0f,
                        1.0f, -1.0f,
                        -1.0f, 1.0f,
                        1.0f, 1.0f,
                });
            }
        }
        public float[] getTextureMatrix() {
            return mFilter.getTextureMatrix();
        }

        @Override
        public void create() {
            mFilter.create();
            if (mRenderer != null) {
                mRenderer.create();
            }
        }
        @Override
        public void sizeChanged(int width, int height) {
            mFilter.sizeChanged(width, height);
            if (mRenderer != null) {
                mRenderer.sizeChanged(width, height);
            }
        }
        @Override
        public void draw(int texture) {
            if (mRenderer != null) {
                mRenderer.draw(mFilter.drawToTexture(texture));
            } else {
                mFilter.draw(texture);
            }
        }
        @Override
        public void destroy() {
            if (mRenderer != null) {
                mRenderer.destroy();
            }
            mFilter.destroy();
        }
    }
}
