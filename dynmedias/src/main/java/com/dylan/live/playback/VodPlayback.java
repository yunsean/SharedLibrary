package com.dylan.live.playback;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;

import com.dylan.medias.codec.MxMediaMuxer;
import com.dylan.medias.publish.MxRtmpPublish;

import java.nio.ByteBuffer;
import java.util.List;

public class VodPlayback implements Runnable, MxRtmpPublish.Callback {

    private boolean canceled = false;

    @Override
    public void onStatistics(long send, long lost, int bitRate) {
        if (this.callback != null) {
            this.callback.onStatistics(send, lost, bitRate);
        }
    }
    @Override
    public void onError() {
        if (this.callback != null) {
            this.callback.onError(new Exception("Rtmp disconnected."));
        }
    }

    public interface Callback {
        void onStarted(long actualTime);
        void onFinished(String filepath, Bitmap thumb, long duration, int width, int height);
        void onStatistics(long send, long lost, int bitRate);
        void onEof();
        void onError(Exception ex);
    }

    private Context context = null;
    private SegmentManager segmentManager = null;
    private long beginUtc = -1;
    private long endUtc = -1;

    private String filename = null;
    private String rtmpUrl = null;
    private MediaExtractor mediaExtractor = null;
    private Thread readThread = null;
    private Callback callback = null;

    private VodPlayback(Context context) {
        this.context = context;
    }
    public static VodPlayback with(Context context) {
        return new VodPlayback(context);
    }

    public VodPlayback setSegmentManager(SegmentManager segmentManager) {
        this.segmentManager = segmentManager;
        return this;
    }
    public VodPlayback play(long utc, String url, Callback callback) {
        this.beginUtc = utc;
        this.rtmpUrl = url;
        this.callback = callback;
        this.readThread = new Thread(this);
        this.readThread.start();
        return this;
    }
    public VodPlayback export(long beginUtc, long endUtc, String filename, Callback callback) {
        this.beginUtc = beginUtc;
        this.endUtc = endUtc;
        this.filename = filename;
        this.callback = callback;
        this.readThread = new Thread(this);
        this.readThread.start();
        return this;
    }
    public void stop() {
        try {
            this.canceled = true;
            if (this.readThread != null) {
                this.readThread.join();
            }
            this.readThread = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        List<Segment> segments = this.segmentManager.querySegmentAfterTime(this.beginUtc);
        if (segments.size() < 1) {
            if (this.callback != null) {
                this.callback.onEof();
            }
            return;
        }
        if (this.filename != null) {
            exportThread(segments);
        } else {
            publishThread(segments);
        }
    }
    private void publishThread(List<Segment> segments) {
        boolean firstSegment = true;
        int segmentIndex = 0;
        MediaExtractor audioExtractor = null;
        MediaExtractor videoExtractor = null;
        MxRtmpPublish rtmpPublish = null;
        ByteBuffer videoBuffer = null;
        ByteBuffer audioBuffer = null;
        MediaCodec.BufferInfo videoInfo = new MediaCodec.BufferInfo();
        MediaCodec.BufferInfo audioInfo = new MediaCodec.BufferInfo();
        long videoBaseTimestampUs = 0;
        long audioBaseTimestampUs = 0;
        boolean reachEnd = false;
        videoInfo.offset = 0;
        audioInfo.offset = 0;
        long beginTimetick = 0;
        boolean isFullSpeed = true;
        try {
            while (!this.canceled && !reachEnd) {
                if (segmentIndex >= segments.size()) {
                    if (segments.size() < 1) break;
                    Segment last = segments.get(segments.size() - 1);
                    long id = last.getId();
                    segments = segmentManager.querySegmentAfterId(id);
                    if (segments.size() < 1) break;
                }
                Segment segment = segments.get(segmentIndex);
                if (!firstSegment) {
                    videoBaseTimestampUs += segment.getVideoOffset();
                    audioBaseTimestampUs += segment.getAudioOffset();
                }
                audioExtractor = new MediaExtractor();
                audioExtractor.setDataSource(segment.getFilename());
                videoExtractor = new MediaExtractor();
                videoExtractor.setDataSource(segment.getFilename());
                int audioTrackIndex = -1;
                int videoTrackIndex = -1;
                for (int i = 0; i < audioExtractor.getTrackCount(); i++) {
                    MediaFormat format = audioExtractor.getTrackFormat(i);
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    if (mime.equals(MediaFormat.MIMETYPE_AUDIO_AAC)) {
                        audioTrackIndex = i;
                        audioExtractor.selectTrack(i);
                    } else if (mime.equals(MediaFormat.MIMETYPE_VIDEO_AVC)) {
                        videoTrackIndex = i;
                        videoExtractor.selectTrack(i);
                    }
                }
                if (firstSegment) {
                    if (segment.getEndUtc() - 1000 <= this.beginUtc) {
                        segmentIndex++;
                        continue;
                    }
                    firstSegment = false;
                    long offset = (this.beginUtc - segment.getBeginUtc()) * 1000;
                    if (offset < 0)offset = 0;
                    videoExtractor.seekTo(offset, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
                    long videoTimeStampUs = videoExtractor.getSampleTime();
                    audioExtractor.seekTo(videoTimeStampUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                    long audioTimeStampUs = audioExtractor.getSampleTime();
                    long timeStampUs = Math.min(videoTimeStampUs, audioTimeStampUs);
                    long actualTime = segment.getBeginUtc() + timeStampUs / 1000;
                    MediaFormat videoFormat = videoExtractor.getTrackFormat(videoTrackIndex);
                    MediaFormat audioFormat = audioExtractor.getTrackFormat(audioTrackIndex);
                    rtmpPublish = MxRtmpPublish.Builder.with(this.context)
                            .video(videoFormat)
                            .audio(audioFormat)
                            .build()
                            .setCallback(this);
                    if (!rtmpPublish.start(this.rtmpUrl)) {
                        throw new Exception("连接直播服务器失败!");
                    }
                    int videoBufferSize = videoFormat.getInteger(MediaFormat.KEY_WIDTH) * videoFormat.getInteger(MediaFormat.KEY_HEIGHT) * 3 / 2;
                    videoBuffer = ByteBuffer.allocate(videoBufferSize);
                    int audioBuffserSize = 1024 * 4;
                    audioBuffer = ByteBuffer.allocate(audioBuffserSize);
                    if (this.callback != null) this.callback.onStarted(actualTime);
                    videoBaseTimestampUs = -timeStampUs;
                    audioBaseTimestampUs = -timeStampUs;
                    beginTimetick = System.currentTimeMillis() - 5000;  //前5s全速输入
                }
                long endTimestamp = (endUtc == -1) ? -1 : ((endUtc - segment.getEndUtc()) * 1000);
                boolean audioEof = false;
                boolean videoEof = false;
                long audioTimestampUs = -1;
                long videoTimestampUs = -1;
                do {
                    long sendTimeStamp = 0;
                    if (audioTimestampUs == -1 && !audioEof) {
                        audioInfo.size = audioExtractor.readSampleData(audioBuffer, 0);
                        if (audioInfo.size == -1) {
                            audioEof = true;
                            audioBaseTimestampUs = audioInfo.presentationTimeUs;
                        } else {
                            audioInfo.flags = audioExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC ? MediaCodec.BUFFER_FLAG_KEY_FRAME : 0;
                            audioTimestampUs = audioExtractor.getSampleTime();
                            audioExtractor.advance();
                        }
                    }
                    if (videoTimestampUs == -1 && !videoEof) {
                        videoInfo.size = videoExtractor.readSampleData(videoBuffer, 0);
                        if (videoInfo.size == -1) {
                            videoEof = true;
                            videoBaseTimestampUs = videoInfo.presentationTimeUs;
                        } else {
                            videoInfo.flags = audioExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC ? MediaCodec.BUFFER_FLAG_KEY_FRAME : 0;
                            videoTimestampUs = videoExtractor.getSampleTime();
                            videoExtractor.advance();
                        }
                    }
                    if (audioTimestampUs != -1 && videoTimestampUs != -1) {
                        if (endTimestamp != -1 && (videoTimestampUs > endTimestamp && audioTimestampUs > endTimestamp)) {
                            reachEnd = true;
                            break;
                        }
                        if (audioTimestampUs < videoTimestampUs) {
                            audioInfo.presentationTimeUs = audioBaseTimestampUs + audioTimestampUs;
                            sendTimeStamp = audioInfo.presentationTimeUs / 1000;
                            rtmpPublish.appendAudio(audioBuffer.array(), audioInfo.size, sendTimeStamp, !isFullSpeed);
                            audioTimestampUs = -1;
                        } else {
                            videoInfo.presentationTimeUs = videoBaseTimestampUs + videoTimestampUs;
                            sendTimeStamp = videoInfo.presentationTimeUs / 1000;
                            rtmpPublish.appendVideo(videoBuffer.array(), videoInfo.size, sendTimeStamp, (videoInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0, !isFullSpeed);
                            videoTimestampUs = -1;
                        }
                    } else if (audioTimestampUs != -1) {
                        audioInfo.presentationTimeUs = audioBaseTimestampUs + audioTimestampUs;
                        sendTimeStamp = audioInfo.presentationTimeUs / 1000;
                        rtmpPublish.appendAudio(audioBuffer.array(), audioInfo.size, sendTimeStamp, !isFullSpeed);
                        audioTimestampUs = -1;
                    } else if (videoTimestampUs != -1) {
                        videoInfo.presentationTimeUs = videoBaseTimestampUs + videoTimestampUs;
                        sendTimeStamp = videoInfo.presentationTimeUs / 1000;
                        rtmpPublish.appendVideo(videoBuffer.array(), videoInfo.size, sendTimeStamp, (videoInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0, !isFullSpeed);
                        videoTimestampUs = -1;
                    }
                    if (sendTimeStamp != 0) {
                        long waiting = sendTimeStamp - (System.currentTimeMillis() - beginTimetick);
                            if (waiting > 0) {
                            //isFullSpeed = false;
                            try {
                                Thread.sleep(waiting);
                            } catch (InterruptedException ex) {
                            }
                        }
                    }
                } while ((!audioEof || !videoEof) && !reachEnd && !canceled);
                segmentIndex++;
            }
            if (!this.canceled) {
                if (this.callback != null) {
                    this.callback.onEof();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            if (this.callback != null) {
                this.callback.onError(ex);
            }
        } finally {
            if (rtmpPublish != null) {
                rtmpPublish.stop();
            }
        }
    }
    private void exportThread(List<Segment> segments) {
        boolean firstSegment = true;
        int segmentIndex = 0;
        MediaExtractor audioExtractor = null;
        MediaExtractor videoExtractor = null;
        MxMediaMuxer mediaMuxer = null;
        ByteBuffer videoBuffer = null;
        ByteBuffer audioBuffer = null;
        MediaCodec.BufferInfo videoInfo = new MediaCodec.BufferInfo();
        MediaCodec.BufferInfo audioInfo = new MediaCodec.BufferInfo();
        long videoBaseTimestampUs = 0;
        long audioBaseTimestampUs = 0;
        boolean reachEnd = false;
        videoInfo.offset = 0;
        audioInfo.offset = 0;
        try {
            while (!this.canceled && !reachEnd) {
                if (segmentIndex >= segments.size()) {
                    if (segments.size() < 1) break;
                    Segment last = segments.get(segments.size() - 1);
                    long id = last.getId();
                    segments = segmentManager.querySegmentAfterId(id);
                    if (segments.size() < 1) break;
                }
                Segment segment = segments.get(segmentIndex);
                if (!firstSegment) {
                    videoBaseTimestampUs += segment.getVideoOffset();
                    audioBaseTimestampUs += segment.getAudioOffset();
                }
                audioExtractor = new MediaExtractor();
                audioExtractor.setDataSource(segment.getFilename());
                videoExtractor = new MediaExtractor();
                videoExtractor.setDataSource(segment.getFilename());
                int audioTrackIndex = -1;
                int videoTrackIndex = -1;
                for (int i = 0; i < audioExtractor.getTrackCount(); i++) {
                    MediaFormat format = audioExtractor.getTrackFormat(i);
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    if (mime.equals(MediaFormat.MIMETYPE_AUDIO_AAC)) {
                        audioTrackIndex = i;
                        audioExtractor.selectTrack(i);
                    } else if (mime.equals(MediaFormat.MIMETYPE_VIDEO_AVC)) {
                        videoTrackIndex = i;
                        videoExtractor.selectTrack(i);
                    }
                }
                if (firstSegment) {
                    if (segment.getEndUtc() - 1000 <= this.beginUtc) {
                        segmentIndex++;
                        continue;
                    }
                    firstSegment = false;
                    long offset = (segment.getBeginUtc() - this.beginUtc) * 1000;
                    if (offset <= 0) {
                        offset = 0;
                    } else {
                        videoExtractor.seekTo(offset, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
                    }
                    long videoTimeStampUs = videoExtractor.getSampleTime();
                    audioExtractor.seekTo(videoTimeStampUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                    long audioTimeStampUs = audioExtractor.getSampleTime();
                    long timeStampUs = Math.min(videoTimeStampUs, audioTimeStampUs);
                    long actualTime = segment.getBeginUtc() + timeStampUs / 1000;
                    MediaFormat videoFormat = videoExtractor.getTrackFormat(videoTrackIndex);
                    MediaFormat audioFormat = audioExtractor.getTrackFormat(audioTrackIndex);
                    mediaMuxer = MxMediaMuxer.Builder.create()
                            .video(videoFormat)
                            .audio(audioFormat)
                            .file(filename)
                            .build()
                            .start();
                    int videoBufferSize = videoFormat.getInteger(MediaFormat.KEY_WIDTH) * videoFormat.getInteger(MediaFormat.KEY_HEIGHT) * 3 / 2;
                    videoBuffer = ByteBuffer.allocate(videoBufferSize);
                    int audioBuffserSize = 1024 * 4;
                    audioBuffer = ByteBuffer.allocate(audioBuffserSize);
                    if (this.callback != null) this.callback.onStarted(actualTime);
                    videoBaseTimestampUs = -timeStampUs;
                    audioBaseTimestampUs = -timeStampUs;
                }
                long endTimestamp = (endUtc == -1) ? -1 : ((endUtc - segment.getEndUtc()) * 1000);
                boolean audioEof = false;
                boolean videoEof = false;
                long audioTimestampUs = -1;
                long videoTimestampUs = -1;
                do {
                    if (audioTimestampUs == -1 && !audioEof) {
                        audioInfo.size = audioExtractor.readSampleData(audioBuffer, 0);
                        if (audioInfo.size == -1) {
                            audioEof = true;
                            audioBaseTimestampUs = audioInfo.presentationTimeUs;
                        } else {
                            audioInfo.flags = audioExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC ? MediaCodec.BUFFER_FLAG_KEY_FRAME : 0;
                            audioTimestampUs = audioExtractor.getSampleTime();
                            audioExtractor.advance();
                        }
                    }
                    if (videoTimestampUs == -1 && !videoEof) {
                        videoInfo.size = videoExtractor.readSampleData(videoBuffer, 0);
                        if (videoInfo.size == -1) {
                            videoEof = true;
                            videoBaseTimestampUs = videoInfo.presentationTimeUs;
                        } else {
                            videoInfo.flags = audioExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC ? MediaCodec.BUFFER_FLAG_KEY_FRAME : 0;
                            videoTimestampUs = videoExtractor.getSampleTime();
                            videoExtractor.advance();
                        }
                    }
                    if (audioTimestampUs != -1 && videoTimestampUs != -1) {
                        if (endTimestamp != -1 && (videoTimestampUs > endTimestamp && audioTimestampUs > endTimestamp)) {
                            reachEnd = true;
                            break;
                        }
                        if (audioTimestampUs < videoTimestampUs) {
                            audioInfo.presentationTimeUs = audioBaseTimestampUs + audioTimestampUs;
                            mediaMuxer.writeAudio(audioInfo, audioBuffer);
                            audioTimestampUs = -1;
                        } else {
                            videoInfo.presentationTimeUs = videoBaseTimestampUs + videoTimestampUs;
                            mediaMuxer.writeVideo(videoInfo, videoBuffer);
                            videoTimestampUs = -1;
                        }
                    } else if (audioTimestampUs != -1) {
                        audioInfo.presentationTimeUs = audioBaseTimestampUs + audioTimestampUs;
                        mediaMuxer.writeAudio(audioInfo, audioBuffer);
                        audioTimestampUs = -1;
                    } else if (videoTimestampUs != -1) {
                        videoInfo.presentationTimeUs = videoBaseTimestampUs + videoTimestampUs;
                        mediaMuxer.writeVideo(videoInfo, videoBuffer);
                        videoTimestampUs = -1;
                    }
                } while ((!audioEof || !videoEof) && !reachEnd && !canceled);
                segmentIndex++;
            }
            boolean fileCreated = mediaMuxer != null;
            if (mediaMuxer != null) {
                mediaMuxer.stop();
                mediaMuxer = null;
            }
            if (!this.canceled) {
                if (!fileCreated) {
                    if (this.callback != null) this.callback.onError(new Exception("未创建任何文件!"));
                } else {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(filename);
                    long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    int width = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                    int height = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                    Bitmap thumb = retriever.getFrameAtTime();
                    thumb = ThumbnailUtils.extractThumbnail(thumb, 320, 240, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                    if (this.callback != null) {
                        this.callback.onFinished(filename, thumb, duration, width, height);
                    }
                }
            }
        } catch (Exception ex) {
            if (mediaMuxer != null) {
                mediaMuxer.stop();
            }
            ex.printStackTrace();
            if (this.callback != null) {
                this.callback.onError(ex);
            }
        }
    }
}
