package com.dylan.live.ingest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.util.Size;
import android.view.SurfaceView;

import com.dylan.devices.audio.MxAudioIngest;
import com.dylan.devices.camera.MxCamera;
import com.dylan.devices.camera.MxCameraConfig;
import com.dylan.devices.video.MxFrameRenderer;
import com.dylan.live.overlay.MxOverlay;
import com.dylan.medias.codec.MxAudioEncoder;
import com.dylan.medias.codec.MxFormatConvert;
import com.dylan.medias.codec.MxVideoEncoder;
import com.dylan.rx.MxObserver;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class LiveJacker {

    public interface OnPreviewListener {
        void onPreviewFPS(float fps);
        void onPreviewDropped(float percent);
        void onError(Throwable ex);
    }

    public interface OnRecordListener {
        void onSegmentBegin(int segmentIndex, String file);
        void onSegmentEnd(int segmentIndex, long begin, long end, long audioOffset, long videoOffset, String filename);
        String removeOldestSegment();
        void onError(Exception ex);
    }

    public interface OnLiveListener {
        void onStatistics(long send, long lost, int bitRate);
        void onError(Throwable ex);
    }

    public interface OnPhotoListener {
        void onFinished(File file);
        void onError(Throwable ex);
    }

    private OnPreviewListener onPreviewListener;
    private OnRecordListener onRecordListener;
    private OnLiveListener onLiveListener;
    private OnPhotoListener onPhotoListener;

    public LiveJacker setOnPreviewListener(OnPreviewListener onPreviewListener) {
        this.onPreviewListener = onPreviewListener;
        return this;
    }
    public LiveJacker setOnRecordListener(OnRecordListener onRecordListener) {
        this.onRecordListener = onRecordListener;
        return this;
    }
    public LiveJacker setOnLiveListener(OnLiveListener onLiveListener) {
        this.onLiveListener = onLiveListener;
        return this;
    }
    public LiveJacker setOnPhotoListener(OnPhotoListener onPhotoListener) {
        this.onPhotoListener = onPhotoListener;
        return this;
    }

    private Handler handler = null;
    private WeakReference<Activity> activity = null;
    private Size videoSize = null;
    private Size photoSize = null;
    private SurfaceView cameraView = null;
    private MxFrameRenderer frameRenderer = null;

    private MxOverlay overlay = null;
    private MxCamera camera = null;
    private MxVideoEncoder vodVideoEncoder = null;
    private VodRecorder vodRecorder = null;
    private MxVideoEncoder liveVideoEncoder = null;
    private LivePublish livePublish = null;

    private boolean living = false;

    private MxAudioEncoder liveAudioEncoder = null;
    private MxAudioEncoder vodAudioEncoder = null;
    private MxAudioIngest audioIngest = null;
    private boolean audioMuted = false;

    private LiveJacker(Activity activity) {
        this.activity = new WeakReference<Activity>(activity);
        this.overlay = new MxOverlay();
        this.handler = new Handler();
    }

    public static LiveJacker with(Activity activity) {
        return new LiveJacker(activity);
    }
    public LiveJacker setCameraView(SurfaceView cameraView) {
        this.cameraView = cameraView;
        return this;
    }

    public LiveJacker setVideoSize(Size videoSize) {
        this.videoSize = videoSize;
        return this;
    }

    public LiveJacker setPhotoSize(Size photoSize) {
        this.photoSize = photoSize;
        return this;
    }

    public LiveJacker setFrameRenderer(MxFrameRenderer frameRenderer) {
        this.frameRenderer = frameRenderer;
        return this;
    }

    public LiveJacker colorMuted(boolean muted) {
        if (overlay != null) overlay.setColorMuted(muted);
        return this;
    }

    public LiveJacker audioMuted(boolean muted) {
        audioMuted = muted;
        return this;
    }

    public LiveJacker open(int cameraId, boolean addTimeStamp) throws LiveJackerException {
        try {
            this.overlay
                    .setAddStamp(addTimeStamp)
                    .asyncStart(5, new MxOverlay.Callback() {
                        private int mLatestFrameCount = 0;
                        private long mLatestUpdateFps = System.currentTimeMillis();
                        @Override
                        public void onDropped(final float percent) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (onPreviewListener != null) {
                                        onPreviewListener.onPreviewDropped(percent);
                                    }
                                }
                            });
                        }
                        @Override
                        public void onFrame(int width, int height, long timeStamp, byte[] yv12, byte[] aux, int size) {
                            mLatestFrameCount++;
                            long time = System.currentTimeMillis();
                            if (time - mLatestUpdateFps > 2000) {
                                float fps = mLatestFrameCount * 1000.f / (time - mLatestUpdateFps);
                                if (onPreviewListener != null) onPreviewListener.onPreviewFPS(fps);
                                mLatestUpdateFps = time;
                                mLatestFrameCount = 0;
                            }
                            if (frameRenderer != null) {
                                frameRenderer
                                        .setSize(width, height)
                                        .update(yv12);
                            }
                            if (liveVideoEncoder != null && living) {
                                liveVideoEncoder.enqueue(aux, 0, size, timeStamp);
                            }
                            if (vodVideoEncoder != null && recording) {
                                vodVideoEncoder.enqueue(aux, 0, size, timeStamp);
                            }
                        }
                    });
            this.audioIngest = MxAudioIngest.with(activity.get())
                    .bit16()
                    .stereo()
                    .sampleRate(44100)
                    .frameSample(1024)
                    .open()
                    .start(new MxAudioIngest.Callback() {
                        @Override
                        public void onFrame(byte[] data, int[] amplitude, long presentationTimeUs) {
                            if (liveAudioEncoder != null && living) {
                                if (audioMuted) {
                                    Arrays.fill(data, 0, data.length, (byte) 0x7f);
                                }
                                liveAudioEncoder.enqueue(data, presentationTimeUs);
                            }
                            if (vodAudioEncoder != null && recording) {
                                vodAudioEncoder.enqueue(data, presentationTimeUs);
                            }
                        }
                        @Override
                        public void onError(final MxAudioIngest.IngestException ex) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (onPreviewListener != null) onPreviewListener.onError(ex);
                                    ex.printStackTrace();
                                }
                            });
                        }
                    });
            MxCameraConfig config = new MxCameraConfig.Builder(activity.get())
                    .previewSize(videoSize)
                    .previewFps(5, 30)
                    .pictureSize(photoSize)
                    .previewFormat(ImageFormat.YV12)
                    .cacheCount(5)
                    .useCameraId(cameraId)
                    .build();
            MxCamera.open(config)
                    .subscribe(new MxObserver<MxCamera>(new MxObserver.OnNext<MxCamera>() {
                        @Override
                        public void onNext(MxCamera r) {
                            camera = r;
                            camera.read(new MxCamera.Callback() {
                                @Override
                                public boolean onFrame(MxCamera.Frame frame) {
                                    if (overlay != null) {
                                        return overlay.queue(frame);
                                    } else {
                                        return false;
                                    }
                                }
                            });
                            camera.preview(cameraView, activity.get());
                        }
                    }, new MxObserver.OnError() {
                        @Override
                        public void onError(final Throwable e) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (onPreviewListener != null) onPreviewListener.onError(e);
                                    e.printStackTrace();
                                }
                            });
                        }
                    }));
            return this;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new LiveJackerException(ex);
        }
    }
    public LiveJacker setLogo(int index, Bitmap bitmap, int left, int top, int flags) {
        this.overlay.setLogo(index, bitmap, left, top, flags);
        return this;
    }

    private boolean recording = false;
    public LiveJacker startRecord(final int segmentDuration, int bitRate, final String storagePath) throws Exception {
        try {
            stopRecord();
            this.recording = true;
            this.vodRecorder = VodRecorder.build()
                    .setSegmentDuration(segmentDuration)
                    .setStoragePath(storagePath)
                    .setCallback(new VodRecorder.Callback() {
                        @Override
                        public void onSegmentBegin(final int segmentIndex, final String file) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (onRecordListener != null) onRecordListener.onSegmentBegin(segmentDuration, file);
                                }
                            });
                        }
                        @Override
                        public void onSegmentEnd(final int segmentIndex, final long begin, final long end, final long audioOffset, final long videoOffset, final String filename) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (onRecordListener != null) onRecordListener.onSegmentEnd(segmentIndex, begin, end, audioOffset, videoOffset, filename);
                                }
                            });
                        }
                        @Override
                        public String removeOldestSegment() {
                            if (onRecordListener != null) return onRecordListener.removeOldestSegment();
                            else return null;
                        }
                        @Override
                        public void onError(final Exception ex) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    ex.printStackTrace();
                                    if (onRecordListener != null) onRecordListener.onError(ex);
                                    stopRecord();
                                }
                            });
                        }
                    })
                    .open(activity.get());
            this.vodAudioEncoder = MxAudioEncoder.Builder.create()
                    .aac()
                    .bitRate(64 * 1000)
                    .channel(2)
                    .sampleRate(44100)
                    .build()
                    .open(new MxAudioEncoder.Callback() {
                        @Override
                        public void onFormatChanged(MediaFormat mediaFormat) {
                            if (vodRecorder != null) {
                                vodRecorder.setAudioFormat(mediaFormat);
                            }
                        }

                        @Override
                        public void onEncodeFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer byteBuffer) {
                            if (vodRecorder != null && recording) {
                                vodRecorder.addAudioFrame(bufferInfo, byteBuffer);
                            }
                        }
                    });
            this.vodVideoEncoder = MxVideoEncoder.Builder.create()
                    .avc()
                    .size(this.videoSize)
                    .bitRate(bitRate)
                    .bitRateMode(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ)
                    .yv12()
                    .keyInterval(2)
                    .build()
                    .open(new MxVideoEncoder.Callback() {
                        @Override
                        public void onFormatChanged(MediaFormat mediaFormat) {
                            if (vodRecorder != null) {
                                vodRecorder.setVideoForamt(mediaFormat);
                            }
                        }

                        @Override
                        public void onEncodeFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer byteBuffer) {
                            if (vodRecorder != null && recording) {
                                vodRecorder.addVideoFrame(bufferInfo, byteBuffer);
                            }
                        }
                    });
            int inputFormat = MxFormatConvert.CodecCapabilities.toFOURCC(vodVideoEncoder.inputFormat());
            if (inputFormat == 0) throw new Exception("Unknown input format for video encoder");
            this.overlay.setAuxFormat(inputFormat);
            return this;
        } catch (Exception ex) {
            ex.printStackTrace();
            stopRecord();
            throw new LiveJackerException(ex);
        }
    }
    public LiveJacker stopRecord() {
        this.recording = false;
        if (this.vodRecorder != null) {
            this.vodRecorder.close();
            this.vodRecorder = null;
        }
        if (this.vodAudioEncoder != null) {
            this.vodAudioEncoder.close();
            this.vodAudioEncoder = null;
        }
        if (this.vodVideoEncoder != null) {
            this.vodVideoEncoder.close();
            this.vodVideoEncoder = null;
        }
        return this;
    }

    public LiveJacker startLive(String url, int bitRate) throws Exception {
        try {
            stopLive();
            this.living = true;
            this.livePublish = LivePublish.with(activity.get())
                    .setCallback(new LivePublish.Callback() {
                        @Override
                        public void onStatistics(final long send, final long lost, final int bitRate) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (onLiveListener != null) onLiveListener.onStatistics(send, lost, bitRate);
                                }
                            });
                        }

                        @Override
                        public void onError(final Exception ex) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    stopLive();
                                    ex.printStackTrace();
                                    if (onLiveListener != null) onLiveListener.onError(ex);
                                }
                            });
                        }
                    })
                    .open(url);
            this.liveVideoEncoder = MxVideoEncoder.Builder.create()
                    .avc()
                    .size(this.videoSize)
                    .bitRate(bitRate)
                    .bitRateMode(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)
                    .yv12()
                    .keyInterval(1)
                    .build()
                    .open(new MxVideoEncoder.Callback() {
                        @Override
                        public void onFormatChanged(MediaFormat mediaFormat) {
                            if (livePublish != null && living == true) {
                                livePublish.setVideoForamt(mediaFormat);
                            }
                        }

                        @Override
                        public void onEncodeFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer byteBuffer) {
                            if (livePublish != null && living) {
                                livePublish.addVideoFrame(bufferInfo, byteBuffer);
                            }
                        }
                    });
            this.liveAudioEncoder = MxAudioEncoder.Builder.create()
                    .aac()
                    .bitRate(32 * 1000)
                    .channel(2)
                    .sampleRate(44100)
                    .build()
                    .open(new MxAudioEncoder.Callback() {
                        @Override
                        public void onFormatChanged(MediaFormat mediaFormat) {
                            if (livePublish != null) {
                                livePublish.setAudioFormat(mediaFormat);
                            }
                        }

                        @Override
                        public void onEncodeFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer byteBuffer) {
                            if (livePublish != null && living) {
                                livePublish.addAudioFrame(bufferInfo, byteBuffer);
                            }
                        }
                    });
            int inputFormat = MxFormatConvert.CodecCapabilities.toFOURCC(liveVideoEncoder.inputFormat());
            if (inputFormat == 0) throw new Exception("Unknown input format for video encoder");
            this.overlay.setAuxFormat(inputFormat);
        } catch (Exception ex) {
            ex.printStackTrace();
            stopLive();
            throw new LiveJackerException(ex);
        }
        return this;
    }

    public LiveJacker stopLive() {
        this.living = false;
        if (this.livePublish != null) {
            this.livePublish.close();
            this.livePublish = null;
        }
        if (this.liveAudioEncoder != null) {
            this.liveAudioEncoder.close();
            this.liveAudioEncoder = null;
        }
        if (this.liveVideoEncoder != null) {
            this.liveVideoEncoder.close();
            this.liveVideoEncoder = null;
        }
        return this;
    }

    public LiveJacker takePhoto(String filepath) {
        if (this.camera != null) {
            this.camera.shutter(new File(filepath))
                    .subscribe(new MxObserver<File>(new MxObserver.OnNext<File>() {
                        @Override
                        public void onNext(File r) {
                            if (onPhotoListener != null) onPhotoListener.onFinished(r);
                        }
                    }, new MxObserver.OnError() {
                        @Override
                        public void onError(final Throwable ex) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    ex.printStackTrace();
                                    if (onPhotoListener != null) onPhotoListener.onError(ex);
                                }
                            });
                        }
                    }));
        }
        return this;
    }

    public void close() {
        this.living = false;
        this.recording = false;
        if (this.livePublish != null) {
            this.livePublish.close();
            this.livePublish = null;
        }
        if (this.vodRecorder != null) {
            this.vodRecorder.close();
            this.vodRecorder = null;
        }
        if (this.liveAudioEncoder != null) {
            this.liveAudioEncoder.close();
            this.liveAudioEncoder = null;
        }
        if (this.vodAudioEncoder != null) {
            this.vodAudioEncoder.close();
            this.vodAudioEncoder = null;
        }
        if (this.vodVideoEncoder != null) {
            this.vodVideoEncoder.close();
            this.vodVideoEncoder = null;
        }
        if (this.liveVideoEncoder != null) {
            this.liveVideoEncoder.close();
            this.liveVideoEncoder = null;
        }
        if (this.overlay != null) {
            this.overlay.stop();
            this.overlay = null;
        }
        if (this.camera != null) {
            this.camera.close();
            this.camera = null;
        }
        if (this.audioIngest != null) {
            this.audioIngest.close();
            this.audioIngest = null;
        }
    }
}
