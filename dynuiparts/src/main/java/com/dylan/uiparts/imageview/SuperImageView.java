package com.dylan.uiparts.imageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.dylan.uiparts.R;

public class SuperImageView extends AppCompatImageView {

    public final static int FIXED_WIDTH = 1;
    public final static int FIXED_HEIGHT = 2;
    private int mFixedDirection = FIXED_WIDTH;
    private float mSizeAspect = 0.f;

    public void setFixedDirection(int fixedDirection) {
        this.mFixedDirection = fixedDirection;
        invalidate();
    }
    public void setSizeAspect(float sizeAspect) {
        this.mSizeAspect = sizeAspect;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public SuperImageView(Context context) {
        super(context);
        init(context, null, 0);
    }
    public SuperImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }
    public SuperImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SuperImageView, defStyleAttr, 0);
            mSizeAspect = a.getFloat(R.styleable.SuperImageView_siv_image_aspect, .0f);
            mFixedDirection = a.getInt(R.styleable.SuperImageView_siv_fixed_direction, 1);
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mSizeAspect > 0.01f) {
            if (mFixedDirection == FIXED_HEIGHT) {
                int height = View.MeasureSpec.getSize(heightMeasureSpec);
                int width = (int) (height / mSizeAspect);
                this.setMeasuredDimension(width, height);
            } else {
                int width = View.MeasureSpec.getSize(widthMeasureSpec);
                int height = (int) (width * mSizeAspect);
                this.setMeasuredDimension(width, height);
            }
        } else if (getScaleType() != ImageView.ScaleType.FIT_XY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else if (mFixedDirection == FIXED_HEIGHT) {
            int width = View.MeasureSpec.getSize(widthMeasureSpec);
            int height = View.MeasureSpec.getSize(heightMeasureSpec);
            if (getDrawable() != null) {
                Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
                if (bitmap != null) {
                    width = height * bitmap.getWidth() / bitmap.getHeight();
                }
            }
            this.setMeasuredDimension(width, height);
        } else {
            int width = View.MeasureSpec.getSize(widthMeasureSpec);
            int height = View.MeasureSpec.getSize(heightMeasureSpec);
            if (getDrawable() != null) {
                Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
                if (bitmap != null) {
                    height = width * bitmap.getHeight() / bitmap.getWidth();
                }
            }
            this.setMeasuredDimension(width, height);
        }
    }
}
