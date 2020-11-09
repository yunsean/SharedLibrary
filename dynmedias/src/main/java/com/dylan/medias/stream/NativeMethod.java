package com.dylan.medias.stream;

public class NativeMethod {
	
	public final static int Codec_None = 0;
	public final static int Codec_Unknown = 1;
	public final static int Codec_Avc = 2;
    public final static int Codec_Hevc = 3;
	public final static int Codec_Aac = 4;
    public final static int Codec_Mp3 = 5;
    public final static int Codec_Alaw = 6;

    public static native long native_init();
    public static native boolean native_open(long handle, String url, boolean tcpOnly);
    public static native int native_videoIndex(long handle);
    public static native int native_audioIndex(long handle);
    public static native int native_videoCodec(long handle);
    public static native int native_audioCodec(long handle);
    public static native byte[] native_videoExtraData(long handle);
    public static native byte[] native_audioExtraData(long handle);
    public static native byte[] native_read(long handle);   //4bytes index + 8bytes timestamp + Nbytes data
    public static native long native_interrupt(long handle);
    public static native void native_close(long handle);

    static {
        System.loadLibrary("player");
    }
}
