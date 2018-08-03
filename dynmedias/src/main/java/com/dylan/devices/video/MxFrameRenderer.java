package com.dylan.devices.video;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.ImageFormat;
import android.media.Image;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.AttributeSet;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MxFrameRenderer extends GLSurfaceView implements Renderer {
    public static boolean isSupportOpenGLES20(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return (info.reqGlEsVersion >= 0x20000);
    }

    private GLProgram prog = new GLProgram(0);
    private int mVideoWidth = 0;
    private int mVideoHeight = 0;
    private ByteBuffer mPlaneBuffer[] = null;
    private int mPlaneSize[] = null;

    public MxFrameRenderer(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        setRenderer(this);
    }
    public MxFrameRenderer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        setRenderer(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (!prog.isProgramBuilt()) {
            prog.buildProgram();
        }
    }
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }
    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized (this) {
            try {
                if (mPlaneBuffer != null) {
                    mPlaneBuffer[0].position(0);
                    mPlaneBuffer[1].position(0);
                    mPlaneBuffer[2].position(0);
                    prog.buildTextures(mPlaneBuffer[0], mPlaneBuffer[2], mPlaneBuffer[1], mVideoWidth, mVideoHeight);
                    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                    prog.drawFrame();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public MxFrameRenderer setSize(int imageWidth, int imageHeight) {
        if ((imageWidth != mVideoWidth || imageHeight != mVideoHeight) && imageWidth > 0 && imageHeight > 0) {
            int width = getWidth();
            int height = getHeight();
            if (width > 0 && height > 0) {
                float f1 = 1f * height / width;
                float f2 = 1f * imageHeight / imageWidth;
                if (f1 == f2) {
                    prog.createBuffers(GLProgram.squareVertices);
                } else if (f1 < f2) {
                    float widScale = f1 / f2;
                    prog.createBuffers(new float[] { -widScale, -1.0f, widScale, -1.0f, -widScale, 1.0f, widScale, 1.0f, });
                } else {
                    float heightScale = f2 / f1;
                    prog.createBuffers(new float[] { -1.0f, -heightScale, 1.0f, -heightScale, -1.0f, heightScale, 1.0f, heightScale, });
                }
            }
            this.mVideoWidth = imageWidth;
            this.mVideoHeight = imageHeight;
            int yarraySize = imageWidth * imageHeight;
            int uvarraySize = yarraySize / 4;
            synchronized (this) {
                mPlaneBuffer = new ByteBuffer[3];
                mPlaneBuffer[0] = ByteBuffer.allocate(yarraySize);
                mPlaneBuffer[1] = ByteBuffer.allocate(uvarraySize);
                mPlaneBuffer[2] = ByteBuffer.allocate(uvarraySize);
                mPlaneSize = new int[3];
                mPlaneSize[0] = mVideoWidth * mVideoHeight;
                mPlaneSize[1] = mPlaneSize[0] >> 2;
                mPlaneSize[2] = mPlaneSize[1];
            }
        }
        return this;
    }
    public MxFrameRenderer update(byte[] ydata, byte[] udata, byte[] vdata) {
        synchronized (this) {
            mPlaneBuffer[0].clear();
            mPlaneBuffer[1].clear();
            mPlaneBuffer[2].clear();
            mPlaneBuffer[0].put(ydata, 0, ydata.length);
            mPlaneBuffer[1].put(udata, 0, udata.length);
            mPlaneBuffer[2].put(vdata, 0, vdata.length);
        }
        requestRender();
        return this;
    }
    public MxFrameRenderer update(byte[] yv12) {
        synchronized (this) {
            mPlaneBuffer[0].clear();
            mPlaneBuffer[1].clear();
            mPlaneBuffer[2].clear();
            mPlaneBuffer[0].put(yv12, 0, mPlaneSize[0]);
            mPlaneBuffer[1].put(yv12, mPlaneSize[0], mPlaneSize[1]);
            mPlaneBuffer[2].put(yv12, mPlaneSize[0] + mPlaneSize[1], mPlaneSize[2]);
        }
        requestRender();
        return this;
    }
    public MxFrameRenderer update(Image image) {
        if (image.getFormat() != ImageFormat.YV12) return this;
        Image.Plane[] planes = image.getPlanes();
        if (planes == null || planes.length != 3) return this;
        synchronized (this) {
            if (image.getWidth() != mVideoWidth || image.getHeight() != mVideoHeight) {
                setSize(image.getWidth(), image.getHeight());
            }
            mPlaneBuffer[0].clear();
            mPlaneBuffer[1].clear();
            mPlaneBuffer[2].clear();
            mPlaneBuffer[0].put(planes[0].getBuffer());
            mPlaneBuffer[1].put(planes[1].getBuffer());
            mPlaneBuffer[2].put(planes[2].getBuffer());
        }
        requestRender();
        return this;
    }
}
