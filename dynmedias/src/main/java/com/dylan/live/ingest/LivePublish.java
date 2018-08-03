package com.dylan.live.ingest;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import com.dylan.medias.publish.MxRtmpPublish;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

class LivePublish {
    public interface Callback {
        void onStatistics(long send, long lost, int bitRate);
        void onError(Exception ex);
    }

    private MxRtmpPublish.Builder builder = null;
    private MxRtmpPublish rtmpPublish = null;
    private String url = null;
    private Callback callback = null;

    public LivePublish setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    private LivePublish(Context context) {
        this.builder = MxRtmpPublish.Builder.with(context);
    }
    public static LivePublish with(Context context) {
        return new LivePublish(context);
    }
    public LivePublish open(String url) {
        this.url = url;
        if (url.startsWith("udp://")) this.builder.udp();
//        try {
//            fos = new FileOutputStream("/sdcard/media.dat");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        return this;
    }

    public void setAudioFormat(MediaFormat mediaFormat) {
        builder.audio(mediaFormat);
        if (fos != null) {
            synchronized (fos) {
                ByteBuffer esds = mediaFormat.getByteBuffer("csd-0");
                try {
                    fos.write(0x82);
                    fos.write(fromLong(0));
                    fos.write(0xff);
                    fos.write(fromInt(esds.limit() - esds.arrayOffset()));
                    fos.write(esds.array(), esds.position(), esds.limit() - esds.position());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        tryOpenPulisher();
    }
    public void setVideoForamt(MediaFormat mediaFormat) {
        builder.video(mediaFormat);
        if (fos != null) {
            synchronized (fos) {
                ByteBuffer sps = mediaFormat.getByteBuffer("csd-0");
                ByteBuffer pps = mediaFormat.getByteBuffer("csd-1");
                try {
                    fos.write(0x81);
                    fos.write(fromLong(0));
                    fos.write(0xff);
                    fos.write(fromInt(8 + (sps.limit() - sps.arrayOffset()) + (pps.limit() - pps.arrayOffset())));
                    fos.write(fromInt(sps.limit() - sps.arrayOffset()));
                    fos.write(sps.array(), sps.position(), sps.limit() - sps.position());
                    fos.write(fromInt(pps.limit() - pps.arrayOffset()));
                    fos.write(pps.array(), pps.position(), pps.limit() - pps.position());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        tryOpenPulisher();
    }

    private long beginVideoTimeStamp = -1;
    private long beginAudioTimeStamp = -1;
    private byte[] audioCache = null;
    private FileOutputStream fos = null;
    public void addAudioFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer byteBuffer) {
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            return;
        }
        synchronized (this) {
            if (this.rtmpPublish != null) {
                if (beginAudioTimeStamp == -1) {
                    beginAudioTimeStamp = bufferInfo.presentationTimeUs;
                    if (beginAudioTimeStamp == -1) beginAudioTimeStamp = 0;
                }
                int length = byteBuffer.limit() - byteBuffer.position();
                long timeStamp = (bufferInfo.presentationTimeUs - beginAudioTimeStamp) / 1000;
                if (audioCache == null || audioCache.length < length) {
                    audioCache = new byte[length];
                }
                byteBuffer.get(audioCache, 0, length);
                this.rtmpPublish.appendAudio(audioCache, length, timeStamp, false);
                if (fos != null) {
                    synchronized (fos) {
                        try {
                            fos.write(2);
                            fos.write(fromLong(timeStamp));
                            fos.write(0xff);
                            fos.write(fromInt(length));
                            fos.write(audioCache, 0, length);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    public void addVideoFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer byteBuffer) {
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            return;
        }
        synchronized (this) {
            if (this.rtmpPublish != null) {
                if (beginVideoTimeStamp == -1) {
                    beginVideoTimeStamp = bufferInfo.presentationTimeUs;
                    if (beginVideoTimeStamp == -1) beginVideoTimeStamp = 0;
                }
                int length = byteBuffer.limit() - byteBuffer.position();
                boolean isKey = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0;
                long timeStamp = (bufferInfo.presentationTimeUs - beginVideoTimeStamp) / 1000;

                ByteBuffer buffer = this.rtmpPublish.videoBuffer(length);
                buffer.clear();
                buffer.put(byteBuffer);
                if (fos != null) {
                    synchronized (fos) {
                        try {
                            fos.write(1);
                            fos.write(fromLong(timeStamp));
                            fos.write(isKey ? 0xff : 0x00);
                            fos.write(fromInt(length));
                            fos.write(buffer.array(), buffer.arrayOffset(), length);
                            Log.e("dylan", "ll=" + length);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                this.rtmpPublish.appendVideo(length, timeStamp, isKey, false);
            }
        }
    }
    private byte[] fromLong(long value) {
        byte[] result = new byte[8];
        result[0] = (byte)((value >>  0) & 0xff);
        result[1] = (byte)((value >>  8) & 0xff);
        result[2] = (byte)((value >> 16) & 0xff);
        result[3] = (byte)((value >> 24) & 0xff);
        result[4] = (byte)((value >> 32) & 0xff);
        result[5] = (byte)((value >> 40) & 0xff);
        result[6] = (byte)((value >> 48) & 0xff);
        result[7] = (byte)((value >> 56) & 0xff);
        return result;
    }
    private byte[] fromInt(int value) {
        byte[] result = new byte[4];
        result[0] = (byte)((value >>  0) & 0xff);
        result[1] = (byte)((value >>  8) & 0xff);
        result[2] = (byte)((value >> 16) & 0xff);
        result[3] = (byte)((value >> 24) & 0xff);
        return result;
    }
    public void close() {
        synchronized (this) {
            if (this.rtmpPublish != null) {
                MxRtmpPublish publish = this.rtmpPublish;
                this.rtmpPublish = null;
                publish.close();
            }
        }
    }

    public synchronized void tryOpenPulisher() {
        if (!builder.hasAudio() || !builder.hasVideo()) {
            return;
        }
        try {
            rtmpPublish = builder.build().setCallback(new MxRtmpPublish.Callback() {
                @Override
                public void onStatistics(long send, long lost, int bitRate) {
                    if (callback != null) callback.onStatistics(send, lost, bitRate);
                }
                @Override
                public void onError() {
                    if (callback != null) callback.onError(new Exception("Rtmp disconnected."));
                }
            });
            if (!rtmpPublish.start(this.url)) {
                if (callback != null) callback.onError(new Exception("Start publish failed."));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            if (callback != null) callback.onError(ex);
        }
    }
}
