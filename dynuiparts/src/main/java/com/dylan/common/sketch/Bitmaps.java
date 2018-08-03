package com.dylan.common.sketch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class Bitmaps {

    public static Bitmap flipHorizontal(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newb);
        Matrix m = new Matrix();
        m.postScale(-1, 1);
        Bitmap new2 = Bitmap.createBitmap(bitmap, 0, 0, w, h, m, true);
        cv.drawBitmap(new2, new Rect(0, 0, new2.getWidth(), new2.getHeight()), new Rect(0, 0, w, h), null);
        return newb;
    }
    public static Bitmap flipVertical(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newb);
        Matrix m = new Matrix();
        m.postScale(1, -1);
        Bitmap new2 = Bitmap.createBitmap(bitmap, 0, 0, w, h, m, true);
        cv.drawBitmap(new2, new Rect(0, 0, new2.getWidth(), new2.getHeight()), new Rect(0, 0, w, h), null);
        return newb;
    }
    public static Bitmap rotateBitmap(Bitmap bitmap, int angle) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newb);
        Matrix m = new Matrix();
        m.postRotate(angle);
        Bitmap new2 = Bitmap.createBitmap(bitmap, 0, 0, w, h, m, true);
        cv.drawBitmap(new2, new Rect(0, 0, new2.getWidth(), new2.getHeight()), new Rect(0, 0, w, h), null);
        return newb;
    }
    public static Bitmap rotateAndFlip(Bitmap bitmap, int angle, boolean horizontal, boolean vertical) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newb);
        Matrix m = new Matrix();
        if (angle != 0)m.postRotate(angle);
        if (horizontal)m.postScale(-1, 1);
        if (vertical)m.postScale(1, -1);
        Bitmap new2 = Bitmap.createBitmap(bitmap, 0, 0, w, h, m, true);
        cv.drawBitmap(new2, new Rect(0, 0, new2.getWidth(), new2.getHeight()), new Rect(0, 0, w, h), null);
        return newb;
    }

    public static Bitmap getBitmapFromFilePath(Context context, String filePath) {
        if (filePath == null || filePath.length() < 1)return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        int bmpWidth = options.outWidth;
        int bmpHeght = options.outHeight;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        options.inSampleSize = 1;
        if (bmpWidth > bmpHeght) {
            if (bmpWidth > screenWidth)
                options.inSampleSize = bmpWidth / screenWidth;
        } else {
            if (bmpHeght > screenHeight)
                options.inSampleSize = bmpHeght / screenHeight;
        }
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }
    public static String bitmap2File(Bitmap bitmap) {
        File outDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        File outFile = new File(outDir, System.currentTimeMillis() + ".jpg");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outFile.getAbsolutePath();
    }
    public static byte[] bitmap2Bytes(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
