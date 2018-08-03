package com.dylan.live.ingest;

import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Environment;
import android.os.StatFs;

import com.dylan.medias.codec.MxMediaMuxer;

import java.io.File;
import java.nio.ByteBuffer;

public class VodRecorder {

    public interface Callback {
        void onSegmentBegin(int segmentIndex, String file);
        void onSegmentEnd(int segmentIndex, long begin, long end, long audioOffset, long videoOffset, String filename);
        void onError(Exception ex);
        String removeOldestSegment();
    }

    private Callback callback = null;
    private long segmentDuration = 10 * 60;
    private MxMediaMuxer mediaMuxer = null;
    private MxMediaMuxer.Builder builder = null;
    private long beginVideoTimeStamp = -1;
    private long beginAudioTimeStamp = -1;
    private long latestVideoTimeStamp = 0;
    private long latestAudioTimeStamp = 0;
    private long segmentVideoOffset = 0;
    private long segmentAudioOffset = 0;
    private int segmentIndex = 0;
    private String storagePath = null;
    private int recycleThreshold = 500 * 1000 * 1000;

    public VodRecorder setStoragePath(String storagePath) {
        if (storagePath == null) return this;
        this.storagePath = storagePath;
        this.storagePath += "/vodfiles";
        File file = new File(this.storagePath);
        file.mkdirs();
        return this;
    }
    public VodRecorder setRecycleThreshold(int recycleThreshold) {
        this.recycleThreshold = recycleThreshold;
        return this;
    }
    public VodRecorder setSegmentDuration(int segmentDuration) {
        this.segmentDuration = segmentDuration;
        return this;
    }
    public VodRecorder setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    private VodRecorder() {
        if (new File("/storage/sdcard1").exists()) {
            this.storagePath = "/storage/sdcard1";
        } else if (Environment.isExternalStorageEmulated()) {
            this.storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            this.storagePath = Environment.getDataDirectory().getAbsolutePath();
        }
        this.storagePath += "/vodfiles";
        File file = new File(this.storagePath);
        file.mkdirs();
    }
    public static VodRecorder build() {
        return new VodRecorder();
    }
    public VodRecorder open(Activity activity) {
        this.builder = MxMediaMuxer.Builder.create()
                .mp4();
        this.beginVideoTimeStamp = -1;
        this.beginAudioTimeStamp = -1;
        return this;
    }
    public VodRecorder open(Activity activity, String cameraId) {
        this.builder = MxMediaMuxer.Builder.create()
                .orientationHint(activity, cameraId, activity)
                .mp4();
        this.beginVideoTimeStamp = -1;
        this.beginAudioTimeStamp = -1;
        return this;
    }
    public void setAudioFormat(MediaFormat mediaFormat) {
        builder.audio(mediaFormat);
        tryOpenMuxer();
    }
    public void setVideoForamt(MediaFormat mediaFormat) {
        builder.video(mediaFormat);
        tryOpenMuxer();
    }
    public void addAudioFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer byteBuffer) {
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            return;
        }
        if (this.mediaMuxer == null) {
            return;
        }
        synchronized (this) {
            if (beginAudioTimeStamp == -1) {
                beginAudioTimeStamp = bufferInfo.presentationTimeUs;
                if (beginAudioTimeStamp == -1)beginAudioTimeStamp = 0;
                if (segmentIndex == 1) segmentAudioOffset = 0;
                else segmentAudioOffset = bufferInfo.presentationTimeUs - latestAudioTimeStamp;
            }
            long timeStamp = bufferInfo.presentationTimeUs - beginAudioTimeStamp;
            latestAudioTimeStamp = bufferInfo.presentationTimeUs;
            bufferInfo.presentationTimeUs = timeStamp;
            if (this.mediaMuxer != null) {
                this.mediaMuxer.writeAudio(bufferInfo, byteBuffer);
            }
        }
    }
    public void addVideoFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer byteBuffer) {
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            return;
        }
        if (this.mediaMuxer == null) {
            return;
        }
        synchronized (this) {
            if (beginVideoTimeStamp == -1) {
                beginVideoTimeStamp = bufferInfo.presentationTimeUs;
                if (beginVideoTimeStamp == -1)beginVideoTimeStamp = 0;
                if (segmentIndex == 1) segmentVideoOffset = 0;
                else segmentVideoOffset = bufferInfo.presentationTimeUs - latestVideoTimeStamp;
            }
            long timeStamp = bufferInfo.presentationTimeUs - beginVideoTimeStamp;
            boolean isKey = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0;
            if (timeStamp > segmentDuration * 1000 * 1000 && isKey) {
                tryOpenMuxer();
                addVideoFrame(bufferInfo, byteBuffer);
            } else {
                latestVideoTimeStamp = bufferInfo.presentationTimeUs;
                bufferInfo.presentationTimeUs = timeStamp;
                if (this.mediaMuxer != null) {
                    this.mediaMuxer.writeVideo(bufferInfo, byteBuffer);
                }
            }
        }
    }
    public void close() {
        synchronized (this) {
            if (this.mediaMuxer != null) {
                this.mediaMuxer.stop();
                this.mediaMuxer = null;
                long endTime = System.currentTimeMillis();
                if (callback != null) {
                    callback.onSegmentEnd(segmentIndex, beginTime, endTime, segmentAudioOffset, segmentVideoOffset, this.filename);
                }
            }
        }
    }

    private long beginTime = 0;
    private String filename = null;
    private synchronized void tryOpenMuxer() {
        if (builder.videoFormat() != null && builder.audioFormat() != null) {
            try {
                synchronized (this) {
                    if (this.mediaMuxer != null) {
                        this.mediaMuxer.stop();
                        this.mediaMuxer = null;
                        long endTime = System.currentTimeMillis();
                        if (callback != null) {
                            callback.onSegmentEnd(segmentIndex, beginTime, endTime, segmentAudioOffset, segmentVideoOffset, filename);
                        }
                    }
                    this.segmentIndex++;
                    this.beginTime = System.currentTimeMillis();
                    this.filename = String.format("%s/vod_%d.mp4", this.storagePath , this.beginTime);
                    this.builder.file(this.filename);
                    this.mediaMuxer = this.builder
                            .build()
                            .start();
                    this.beginAudioTimeStamp = -1;
                    this.beginVideoTimeStamp = -1;
                    deleteOldFile();
                    if (this.callback != null) {
                        this.callback.onSegmentBegin(segmentIndex, filename);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                if (this.callback != null) {
                    this.callback.onError(ex);
                }
            }
        }
    }

    private long getFreeSize(){
        StatFs sf = new StatFs(storagePath);
        long blockSize = sf.getBlockSizeLong();
        long freeBlocks = sf.getAvailableBlocksLong();
        long freeSize = blockSize * freeBlocks;
        return freeSize;
    }
    private void deleteOldFile() {
        while (getFreeSize() < 500 * 1000 * 1000) {
            String file = callback.removeOldestSegment();
            if (file == null || file.length() < 1) break;
            deleteFile(file);
        }
    }
    private void deleteFile(String filename) {
        try {
            File file = new File(filename);
            file.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
