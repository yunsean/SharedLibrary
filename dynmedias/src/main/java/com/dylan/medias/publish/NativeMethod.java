package com.dylan.medias.publish;

import android.util.Log;

import java.lang.ref.WeakReference;

class NativeMethod {

    public boolean open(MxRtmpPublish publisher, boolean useUdp) {
        WeakReference<MxRtmpPublish> weak = new WeakReference<MxRtmpPublish>(publisher);
        mNativeHandler = native_open(weak, useUdp);
        statistics(weak, 0, 0, 0);
        if (mNativeHandler == 0)return false;
        return true;
    }
    public void videoAvc(int width, int height, byte[] sps, byte[] pps) {
        if (mNativeHandler == 0)return;
        native_videoAvc(mNativeHandler, width, height, 0, sps, pps);
    }
    public void audioAac(int channels, int sampleRate, byte[] esds) {
        if (mNativeHandler == 0)return;
        native_audioAac(mNativeHandler, channels, sampleRate, esds);
    }
    public void videoFrame(byte[] datas, int length, long timecode, boolean isKey, boolean wait) {
        if (mNativeHandler == 0)return;
        native_videoFrame(mNativeHandler, datas, length, timecode, isKey, wait);
    }
    public void audioFrame(byte[] datas, int length, long timecode, boolean wait) {
        if (mNativeHandler == 0)return;
        native_audioFrame(mNativeHandler, datas, length, timecode, wait);
    }
    public void videoBuffer(Object buffer, int length, long timecode, boolean isKey, boolean wait) {
        if (mNativeHandler == 0)return;
        native_videoBuffer(mNativeHandler, buffer, length, timecode, isKey, wait);
    }
    public void audioBuffer(Object buffer, int length, long timecode, boolean wait) {
        if (mNativeHandler == 0)return;
        native_audioBuffer(mNativeHandler, buffer, length, timecode, wait);
    }
    public boolean start(String url) {
        if (mNativeHandler == 0)return false;
        Log.i("dylan", "0");
        boolean result = native_start(mNativeHandler, url);
        Log.i("dylan", "a");
        return true;
    }
    public void stop() {
        Log.i("dylan", "b");
        if (mNativeHandler == 0)return;
        native_stop(mNativeHandler);
    }
    public void close() {
        Log.i("dylan", "c");
        if (mNativeHandler == 0)return;
        native_close(mNativeHandler);
        mNativeHandler = 0;
    }

    private long mNativeHandler = 0;
    private native long native_open(Object context, boolean useUdp);
    private native void native_videoAvc(long handle, int width, int height, int bitrate, byte[] sps, byte[] pps);
    private native void native_audioAac(long handle, int channels, int sampleRate, byte[] esds);
    private native boolean native_prepare(long handle);
    private native void native_videoFrame(long handle, byte[] datas, int length, long timecode, boolean isKey, boolean wait);
    private native void native_audioFrame(long handle, byte[] datas, int length, long timecode, boolean wait);
    private native void native_videoBuffer(long handle, Object buffer, int length, long timecode, boolean isKey, boolean wait);
    private native void native_audioBuffer(long handle, Object buffer, int length, long timecode, boolean wait);
    private native boolean native_start(long handle, String url);
    private native void native_stop(long handle);
    private native void native_close(long handle);
    static {
        System.loadLibrary("publish");
    }
    private static final void statistics(Object me, long send, long lost, int bitRate){
        @SuppressWarnings("unchecked")
        MxRtmpPublish thiz = ((WeakReference<MxRtmpPublish>)me).get();
        if (thiz != null) {
            thiz.statistics(send, lost, bitRate);
        }
    }
    private static final void onError(Object me) {
        @SuppressWarnings("unchecked")
        MxRtmpPublish thiz = ((WeakReference<MxRtmpPublish>)me).get();
        if (thiz != null) {
            thiz.onError();
        }
    }
}
