package com.dylan.uiparts.image;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

public class RoundCornerTransformation implements Transformation {

    private int radius = 0;
    public RoundCornerTransformation(int radius) {
        this.radius = radius;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        RectF rectF = new RectF(0f, 0f, source.getWidth(), source.getHeight());
        canvas.drawRoundRect(rectF, radius, radius, paint);
        source.recycle();
        return result;
    }

    @Override
    public String key() {
        return "RoundCornerTransformation";
    }
}
