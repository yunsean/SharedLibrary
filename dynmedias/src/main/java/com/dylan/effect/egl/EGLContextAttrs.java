package com.dylan.effect.egl;

import android.opengl.EGL14;

import javax.microedition.khronos.egl.EGL10;


public class EGLContextAttrs {
    private int version = 2;
    private boolean isDefault;

    public EGLContextAttrs version(int v) {
        this.version = v;
        return this;
    }
    public EGLContextAttrs makeDefault(boolean def) {
        this.isDefault = def;
        return this;
    }

    public boolean isDefault() {
        return isDefault;
    }

    int[] build() {
        return new int[]{EGL14.EGL_CONTEXT_CLIENT_VERSION, version, EGL10.EGL_NONE};
    }
}
