package com.dylan.live.overlay;

import android.graphics.Bitmap;

import com.dylan.medias.codec.MxFormatConvert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import static com.dylan.live.overlay.MxOverlay.OverlayFlag_Mono;
import static com.dylan.live.overlay.MxOverlay.OverlayFlag_Opacity;
import static com.dylan.live.overlay.MxOverlay.OverlayMode_YUV;

class NativeMethod {

    private long nativeHandle = 0;

    public void init(int mainFourCC) {
        nativeHandle = native_init(MxOverlay.OverlayMode_YUV, mainFourCC);
    }
    public void setLogo(int index, Bitmap logo, int logoX, int logoY, int flags) {
        int[] argb = null;
        int pitch = 0;
        int height = 0;
        if (logo != null) {
            pitch = logo.getWidth();
            height = logo.getHeight();
            if (pitch % 4 != 0) pitch += (4 - pitch % 4);
            argb = new int[pitch * logo.getHeight()];
            logo.getPixels(argb, 0, pitch, 0, 0, logo.getWidth(), logo.getHeight());
        }
        native_set_overlay(nativeHandle, index, argb, pitch, height, logoX, logoY, flags);
    }
    public void setAux(int fourCC, ByteBuffer buffer) {
        native_set_aux(nativeHandle, fourCC, buffer);
    }
    public void render(ByteBuffer main, int width, int height) {
        native_render(nativeHandle, main, width, height);
    }
    public void render(ByteBuffer main, int width, int height, Bitmap stamp) {
        if (stamp != null) {
            int stampLeft = (width - stamp.getWidth()) >> 1;
            int stampTop = 10;
            setLogo(-1, stamp, stampLeft, stampTop, MxOverlay.OverlayFlag_Mono | MxOverlay.OverlayFlag_Opacity);
        }
        native_render(nativeHandle, main, width, height);
    }
    public void clean() {
        if (nativeHandle != 0) {
            native_cleanup(nativeHandle);
            nativeHandle = 0;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        clean();
    }

    private static native long native_init(int mode, int fourcc);
    private static native void native_set_overlay(long handle, int index, int[] logo, int width, int height, int x, int y, int flags);
    private static native void native_set_aux(long handle, int fourcc, Object aux);
    private static native void native_render(long handle, Object main, int width, int height);
    private static native void native_cleanup(long handle);

    static {
        System.loadLibrary("jacker");
    }
}
