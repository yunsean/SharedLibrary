package com.dylan.uiparts.image;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

public class AspectTransformation implements Transformation {
    private float aspect;

    public AspectTransformation(float aspect) {
        this.aspect = aspect;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int height = source.getHeight();
        int width = source.getWidth();
        int newWidth;
        int newHeight;
        if (width * aspect > height) {
            newHeight = height;
            newWidth = (int)(height / aspect);
        } else {
            newWidth = width;
            newHeight = (int)(width * aspect);
        }
        int fromX = (width - newWidth) / 2;
        int fromY = (height - newHeight) / 2;
        Bitmap target = Bitmap.createBitmap(source, fromX, fromY, newWidth, newHeight);
        source.recycle();
        return target;
    }

    @Override
    public String key() {
        return "AspectTransformation";
    }
}
