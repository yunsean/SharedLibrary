package com.dylan.uiparts.image;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

public class RingCircleTransformation implements Transformation {
    private int ringRadius = 2;
    private int ringColor = 0xffffffff;

    public RingCircleTransformation() {
        super();
    }
    public RingCircleTransformation(int ringRadius, int ringColor) {
        super();
        this.ringRadius = ringRadius;
        this.ringColor = ringColor;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());
        int width = (source.getWidth() - size) / 2;
        int height = (source.getHeight() - size) / 2;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        if (width != 0 || height != 0) {
            Matrix matrix = new Matrix();
            matrix.setTranslate(-width, -height);
            shader.setLocalMatrix(matrix);
        }
        paint.setShader(shader);
        paint.setAntiAlias(true);
        float r = size / 2f;
        canvas.drawCircle(r + ringRadius, r + ringRadius, r - ringRadius - ringRadius, paint);
        paint = new Paint();
        paint.setStrokeWidth(ringRadius);
        paint.setColor(ringColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        canvas.drawCircle(r + ringRadius, r + ringRadius, r - ringRadius - ringRadius, paint);
        source.recycle();
        return bitmap;
    }
    @Override
    public String key() {
        return "RingCircleTransformation";
    }
}
