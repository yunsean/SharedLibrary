package com.dylan.live.overlay;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.media.Image;
import android.media.MediaCodec;
import android.os.Build;
import android.text.Layout;
import android.text.TextPaint;

import com.dylan.medias.codec.MxFormatConvert;
import com.dylan.medias.codec.MxFrame;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MxOverlay implements Runnable {
    public final static int OverlayMode_YUV     = 0;
    public final static int OverlayMode_RGB     = 1;
    public final static int OverlayFlag_Mono    = 0x02;
    public final static int OverlayFlag_Opacity = 0x04;

    public interface Callback {
        void onDropped(float percent);
        void onFrame(int width, int height, long timeStamp, byte[] yv12, byte[] aux, int size);
    }

    private BlockingQueue<Image> images = null;
    private BlockingQueue<MxFrame> frames = null;
    private Thread thread = null;
    private boolean willQuit = false;
    private Callback callback = null;
    private long latestStatistics = 0;
    private long totalFrame = 0;
    private long droppedFrame = 0;
    private boolean colorMuted = false;
    private int auxFourCC = 0;
    private Object auxSetting = 0;
    private int inputFourCC = MxFormatConvert.FOURCC_I420;

    private boolean addStamp = true;
    private boolean includeMs = false;

    private int imageWidth = 0;
    private int imageHeight = 0;
    private ByteBuffer cacheBuffer = null;
    private ByteBuffer auxBuffer = null;
    private ByteBuffer i420Buffer = null;
    private int dataSize = 0;
    private int sizeOfYPlane = 0;
    private NativeMethod nativeMethod = null;
    private long beginTimeTick = -1;
    private long beginTimeStamp = -1;
    private long latestTimeStamp = -1;

    public MxOverlay() {
    }
    public MxOverlay syncOpen(int width, int height, int format, Callback callback) {
        if (nativeMethod != null) nativeMethod.clean();
        this.nativeMethod = new NativeMethod();
        this.nativeMethod.init(format);
        if (format != MxFormatConvert.FOURCC_I420) {
            i420Buffer = ByteBuffer.allocateDirect(width * height * 3 / 2);
            i420Buffer.position(0);
            this.nativeMethod.setAux(MxFormatConvert.FOURCC_I420, i420Buffer);
        }
        this.inputFourCC = format;
        this.callback = callback;
        this.imageWidth = width;
        this.imageHeight = height;
        this.cacheBuffer = ByteBuffer.allocateDirect(width * height * 3 / 2);

        this.sizeOfYPlane = width * height;
        this.dataSize = sizeOfYPlane * 3 / 2;
        this.auxBuffer = ByteBuffer.allocateDirect(dataSize);
        this.auxBuffer.position(0);
        this.auxBuffer.limit(dataSize);

        this.beginTimeTick = -1;
        this.beginTimeStamp = -1;
        return this;
    }
    public MxOverlay asyncStart(int imageCount, Callback callback) {
        this.frames = new ArrayBlockingQueue<>(imageCount);
        if (nativeMethod != null) nativeMethod.clean();
        this.nativeMethod = new NativeMethod();
        this.nativeMethod.init(MxFormatConvert.FOURCC_I420);
        this.thread = new Thread(this);
        this.callback = callback;
        this.thread.start();
        return this;
    }
    public MxOverlay setLogo(int index, Bitmap bitmap, int left, int top, int flags) {
        this.nativeMethod.setLogo(index, bitmap, left, top, flags);
        return this;
    }
    public MxOverlay setAuxFormat(int format) {
        synchronized (auxSetting) {
            if (auxFourCC == MxFormatConvert.FOURCC_I420) {
                auxBuffer = null;
                auxFourCC = 0;
            } else if (format == 0) {
                auxBuffer = null;
                auxFourCC = 0;
            } else if (auxFourCC != format && dataSize != 0) {
                auxBuffer = ByteBuffer.allocateDirect(dataSize);
                auxBuffer.position(0);
                auxFourCC = format;
            } else if (auxFourCC != format) {
                auxBuffer = null;
                auxFourCC = format;
            }
            nativeMethod.setAux(format, auxBuffer);
        }
        return this;
    }
    public MxOverlay setAddStamp(boolean addStamp) {
        this.addStamp = addStamp;
        return this;
    }
    public MxOverlay setIncludeMs(boolean includeMs) {
        this.includeMs = includeMs;
        return this;
    }
    public void overlay(MediaCodec.BufferInfo bufferInfo, ByteBuffer byteBuffer) {
        statistics();
        totalFrame++;
        if ((Math.abs(latestTimeStamp - bufferInfo.presentationTimeUs) > 300 * 1000) || (beginTimeTick == -1 && beginTimeStamp == -1)) {
            beginTimeTick = System.currentTimeMillis();
            beginTimeStamp = bufferInfo.presentationTimeUs / 1000;
        }
        if ((bufferInfo.presentationTimeUs / 1000 - beginTimeStamp) < (System.currentTimeMillis() - beginTimeTick - 10)) {
            droppedFrame++;
            return;
        }
        ByteBuffer buffer;
        try {
            if (!byteBuffer.isDirect() || byteBuffer.isReadOnly()) {
                cacheBuffer.position(0);
                byteBuffer.position(bufferInfo.offset);
                byteBuffer.limit(bufferInfo.offset + bufferInfo.size);
                byteBuffer.get(cacheBuffer.array(), 0, Math.min(bufferInfo.size, cacheBuffer.capacity()));
                buffer = cacheBuffer;
            } else {
                buffer = byteBuffer;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        byte[] datas = buffer.array();
        if (colorMuted) Arrays.fill(datas, sizeOfYPlane, dataSize, (byte)0x7f);
        Bitmap bitmap = addStamp ? timecodeBitmap(imageWidth) : null;
        nativeMethod.render(buffer, imageWidth, imageHeight, bitmap);
        byte[] i420 = (inputFourCC != MxFormatConvert.FOURCC_I420) ? i420Buffer.array() : datas;
        byte[] aux = (auxFourCC == MxFormatConvert.FOURCC_I420) ? i420 : ((auxFourCC == 0) ? datas : auxBuffer.array());
        if (callback != null) callback.onFrame(imageWidth, imageHeight, bufferInfo.presentationTimeUs, i420, aux, dataSize);
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public boolean queue(Image image) {
        if (image.getFormat() != ImageFormat.YUV_420_888 && image.getFormat() != ImageFormat.YV12 && image.getFormat() != ImageFormat.NV21) return false;
        statistics();
        totalFrame++;
        if (!images.offer(image)) {
            droppedFrame++;
            return false;
        }
        return true;
    }
    public boolean queue(MxFrame frame) {
        statistics();
        totalFrame++;
        if (!frames.offer(frame)) {
            droppedFrame++;
            return false;
        }
        return true;
    }
    private void statistics() {
        if (latestStatistics == 0) {
            latestStatistics = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - latestStatistics > 5000) {
            if (droppedFrame > 0 && totalFrame > 0) {
                float percent = droppedFrame * 100.f / totalFrame;
                if (callback != null) callback.onDropped(percent);
            }
            totalFrame = 0;
            droppedFrame = 0;
            latestStatistics = System.currentTimeMillis();
        }
    }
    public void setColorMuted(boolean muted) {
        colorMuted = muted;
    }
    public void stop() {
        try {
            if (thread != null && thread.isAlive()) {
                willQuit = true;
                thread.interrupt();
                thread.join();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            thread = null;
        }
    }

    //Runnable
    @Override
    public void run() {
        runCamera();
    }
    private void runCamera() {
        while (!willQuit) {
            try {
                MxFrame frame = frames.take();
                synchronized (auxSetting) {
                    if (auxBuffer == null && auxFourCC != 0) {
                        sizeOfYPlane = frame.getWidth() * frame.getHeight();
                        dataSize = sizeOfYPlane * 3 / 2;
                        auxBuffer = ByteBuffer.allocateDirect(dataSize);
                        auxBuffer.position(0);
                        auxBuffer.limit(dataSize);
                        nativeMethod.setAux(auxFourCC, auxBuffer);
                    }
                }
                if (colorMuted) Arrays.fill(frame.getDirectBuffer().array(), sizeOfYPlane, dataSize, (byte)0x7f);
                Bitmap bitmap = addStamp ? timecodeBitmap(frame.getWidth()) : null;
                nativeMethod.render(frame.getDirectBuffer(), frame.getWidth(), frame.getHeight(), bitmap);
                if (callback != null) {
                    if (auxFourCC == 0) {
                        callback.onFrame(frame.getWidth(), frame.getHeight(), frame.getTimeStamp(), frame.getDirectBuffer().array(), frame.getDirectBuffer().array(), dataSize);
                    } else {
                        callback.onFrame(frame.getWidth(), frame.getHeight(), frame.getTimeStamp(), frame.getDirectBuffer().array(), auxBuffer.array(), dataSize);
                    }
                }
                frame.close();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                break;
            } catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
        }
        nativeMethod.clean();
        nativeMethod = null;
    }

    private SimpleDateFormat mFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private SimpleDateFormat mFormatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA);
    private long mLatestTimecode = 0;
    private Bitmap timecodeBitmap(int imageWidth) {
        long time = System.currentTimeMillis() / 1000;
        long timecode = includeMs ? System.currentTimeMillis() : time;
        if (timecode == mLatestTimecode) return null;
        String text = includeMs ? mFormatter1.format(new Date()) : mFormatter.format(new Date());
        TextPaint paint = new TextPaint();
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setStyle(Paint.Style.FILL);
        if (time % 2 == 0)paint.setColor(Color.DKGRAY);
        else paint.setColor(Color.GRAY);
        paint.setTextSize(15 * imageWidth / 640);
        paint.setTextAlign(Paint.Align.CENTER);
        int width = (int)Layout.getDesiredWidth(text, paint) + 10;
        Paint.FontMetrics fm = paint.getFontMetrics();
        int height = (int) Math.ceil(fm.descent - fm.top);
        width = width >> 1 << 1;
        height = height >> 1 << 1;
        float base = height - fm.bottom;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(0, 0, width, height, paint);
        if (time % 2 == 0) paint.setColor(Color.WHITE);
        else paint.setColor(Color.BLACK);
        canvas.drawText(text, width / 2, base, paint);
        mLatestTimecode = timecode;
        return bitmap;
    }
}
