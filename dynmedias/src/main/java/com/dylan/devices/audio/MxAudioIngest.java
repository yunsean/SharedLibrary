package com.dylan.devices.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.dylan.exceptions.MxException;

import rx.Observable;
import rx.Subscriber;

public class MxAudioIngest {

    public class IngestException extends MxException {
        public IngestException(Throwable cause) {
            super(cause);
        }
        public IngestException(String message) {
            super(message);
        }
        @Override
        public String summary() {
            return "音频采集错误";
        }
    }

    public interface Callback {
        void onFrame(byte[] data, int[] amplitude, long presentationTimeUs);
        void onError(IngestException ex);
    }

    private int mAudioSource = MediaRecorder.AudioSource.DEFAULT;
    private int mSampleRateInHz = 8000;
    private int mChannelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int mFrameSize = 1280;

    private boolean mNeedAmplitude = false;
    private int mFrameDuration = 0;
    private int mFrameSample = 0;
    private int mChannelCount = 0;
    private int mBytePerSample = 0;

    public static MxAudioIngest with(Context context) {
        return new MxAudioIngest();
    }
    public MxAudioIngest audioSource(int audioSource) {
        mAudioSource = audioSource;
        return this;
    }
    public MxAudioIngest sampleRate(int sampleRate) {
        mSampleRateInHz = sampleRate;
        return this;
    }
    public MxAudioIngest mono() {
        mChannelConfig = AudioFormat.CHANNEL_IN_MONO;
        return this;
    }
    public MxAudioIngest stereo() {
        mChannelConfig = AudioFormat.CHANNEL_IN_STEREO;
        return this;
    }
    public MxAudioIngest bit8() {
        mAudioFormat = AudioFormat.ENCODING_PCM_8BIT;
        return this;
    }
    public MxAudioIngest bit16() {
        mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
        return this;
    }
    public MxAudioIngest frameSize(int framesize) {
        mFrameSize = framesize;
        return this;
    }
    public MxAudioIngest frameDuration(int durationMs) {
        mFrameDuration = durationMs;
        mFrameSize = 0;
        mFrameSample = 0;
        return this;
    }
    public MxAudioIngest frameSample(int sample) {
        mFrameSample = sample;
        mFrameDuration = 0;
        mFrameSize = 0;
        return this;
    }
    public MxAudioIngest amplitude() {
        mNeedAmplitude = true;
        return this;
    }
    public MxAudioIngest open() throws IngestException {
        openInternal();
        return this;
    }
    public Observable<byte[]> start() {
        return startInternal();
    }
    public MxAudioIngest start(Callback callback) {
        startInternal(callback);
        return this;
    }
    public int[] getDecibel(byte[] datas, int offset, int size) {
        if (mAudioFormat != AudioFormat.ENCODING_PCM_16BIT) {
            return new int[mChannelCount];
        }
        double[] sum = new double[mChannelCount];
        int sampleCount = 0;
        while (offset <= size - mBytePerSample * mChannelCount) {
            for (int i = 0; i < mChannelCount; i++) {
                int value = ((datas[offset + 1] & 0xff) << 8) | (datas[offset + 0] & 0xff);
                double val = value /  32768.0;
                sum[i] += val * val;
                offset += 2;
            }
            sampleCount++;
        }
        int[] decibel = new int[mChannelCount];
        for (int i = 0; i < mChannelCount; i++) {
            double rms = Math.sqrt(sum[i] / sampleCount);
            double db = 20 * Math.log10(rms);
            decibel[i] = (int)db;
        }
        return decibel;
    }
    public MxAudioIngest stop() {
        stopInternal();
        return this;
    }
    public void close() {
        closeInternal();
    }

    private AudioRecord mAudioRecorder = null;
    private Thread mAudioThread = null;
    private boolean mWillQuit = false;
    private Callback mCallback = null;
    public void openInternal() throws IngestException {
        mChannelCount = mChannelConfig == AudioFormat.CHANNEL_IN_STEREO ? 2 : 1;
        if (mAudioFormat == AudioFormat.ENCODING_PCM_FLOAT) mBytePerSample = 4;
        else if (mAudioFormat == AudioFormat.ENCODING_PCM_16BIT) mBytePerSample = 2;
        else mBytePerSample = 1;
        int bytePerSample = mBytePerSample * mChannelCount;

        if (mFrameSize == 0 && mFrameDuration == 0 && mFrameSample == 0)
            throw new IngestException("The frame size is not set yet.");
        else if (mFrameSize == 0 && mFrameDuration == 0)
            mFrameSize = mFrameSample * bytePerSample;
        else if (mFrameSize == 0)
            mFrameSize = mSampleRateInHz * mFrameDuration * bytePerSample / 1000;
        if (mFrameSize < bytePerSample || mFrameSize % bytePerSample != 0)
            throw new IngestException("Invalid frame size set. the frame size must be a multiple of " + bytePerSample);

        try {
            int bufferSize = AudioRecord.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat);
            if (bufferSize < mFrameSize)bufferSize = mFrameSize;
            bufferSize *= 5;
            mAudioRecorder = new AudioRecord(mAudioSource, mSampleRateInHz, mChannelConfig, mAudioFormat, bufferSize);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IngestException(ex);
        }
    }
    private Observable<byte[]> startInternal() {
        stopInternal();
        return Observable.create(new Observable.OnSubscribe<byte[]>() {
            @Override
            public void call(final Subscriber<? super byte[]> subscriber) {
                mWillQuit = false;
                mAudioThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            byte[] buffer = new byte[mFrameSize];
                            int read = 0;
                            mAudioRecorder.startRecording();
                            while (!mWillQuit) {
                                int length = mAudioRecorder.read(buffer, read, (mFrameSize - read));
                                read += length;
                                if (read == mFrameSize) {
                                    subscriber.onNext(buffer);
                                    buffer = new byte[mFrameSize];
                                    read = 0;
                                }
                            }
                            mAudioRecorder.stop();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            subscriber.onError(new IngestException(ex));
                        }
                    }
                };
                mAudioThread.setName("#PCMSource");
                mAudioThread.setPriority(Thread.MAX_PRIORITY);
                mAudioThread.start();
            }
        });
    }
    private void startInternal(Callback callback) {
        mWillQuit = false;
        mCallback = callback;
        mAudioThread = new Thread() {
            @Override
            public void run() {
                try {
                    byte[] buffer = new byte[mFrameSize];
                    int read = 0;
                    long totalBytes = 0;
                    int bytePerSecond = mSampleRateInHz * mChannelCount * mBytePerSample;
                    long beginTick = System.currentTimeMillis();
                    mAudioRecorder.startRecording();
                    while (!mWillQuit) {
                        int length = mAudioRecorder.read(buffer, read, (mFrameSize - read));
                        read += length;
                        if (read == mFrameSize) {
                            int[] amplitude = null;
                            if (mNeedAmplitude) amplitude = getDecibel(buffer, 0, read);
                            totalBytes += read;
                            long timeStamp = totalBytes * 1000 / bytePerSecond;
                            long current = System.currentTimeMillis();
                            if ((current - beginTick) - timeStamp > 500) {
                                long adjust = (current - beginTick);
                                Log.w("dylan", "auto adjust audio time stamp from [" + timeStamp + "] to [" + adjust + "]");
                                timeStamp = adjust;
                                totalBytes = timeStamp * bytePerSecond / 1000;
                            }
                            if (mCallback != null) mCallback.onFrame(buffer, amplitude, timeStamp * 1000);
                            read = 0;
                        }
                    }
                    mAudioRecorder.stop();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    if (mCallback != null) mCallback.onError(new IngestException(ex));
                }
            }
        };
        mAudioThread.setName("#PCMSource");
        mAudioThread.setPriority(Thread.MAX_PRIORITY);
        mAudioThread.start();
    }
    private void stopInternal() {
        try {
            mWillQuit = true;
            if (mAudioThread != null) {
                mAudioThread.join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mAudioThread = null;
        }
    }
    private void closeInternal() {
        try {
            stop();
            if (mAudioRecorder != null) {
                mAudioRecorder.release();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            mAudioRecorder = null;
        }
    }
}
