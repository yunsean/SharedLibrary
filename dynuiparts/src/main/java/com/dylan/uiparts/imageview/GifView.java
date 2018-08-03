package com.dylan.uiparts.imageview;

import com.dylan.uiparts.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

public class GifView extends View {
    private static final int DEFAULT_MOVIE_VIEW_DURATION = 1000;

    private int mMovieResourceId;
    private Movie movie;
    private long mMovieStart;
    private int mCurrentAnimationTime;
    private float mLeft;
    private float mTop;
    private float mScale;
    private int mMeasuredMovieWidth;
    private int mMeasuredMovieHeight;
    private volatile boolean mPaused;
    private boolean mVisible = true;

    public GifView(Context context) {
        this(context, null);
    }
    public GifView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public GifView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setViewAttributes(context, attrs, defStyle);
    }

    @SuppressLint("NewApi")
    private void setViewAttributes(Context context, AttributeSet attrs, int defStyle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GifView, defStyle, defStyle);
        mMovieResourceId = array.getResourceId(R.styleable.GifView_gif, -1);
        mPaused = array.getBoolean(R.styleable.GifView_paused, false);
        array.recycle();
        if (mMovieResourceId != -1) {
            movie = Movie.decodeStream(getResources().openRawResource(mMovieResourceId));
        }
    }
    public void setGifResource(int movieResourceId) {
        this.mMovieResourceId = movieResourceId;
        movie = Movie.decodeStream(getResources().openRawResource(mMovieResourceId));
        requestLayout();
    }
    public int getGifResource() {
        return this.mMovieResourceId;
    }
    public void play() {
        if (this.mPaused) {
            this.mPaused = false;
            mMovieStart = android.os.SystemClock.uptimeMillis() - mCurrentAnimationTime;
            invalidate();
        }
    }

    public void pause() {
        if (!this.mPaused) {
            this.mPaused = true;
            invalidate();
        }
    }
    public boolean isPaused() {
        return this.mPaused;
    }
    public boolean isPlaying() {
        return !this.mPaused;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (movie != null) {
            int movieWidth = movie.width();
            int movieHeight = movie.height();
            float scaleH = 1f;
            int measureModeWidth = MeasureSpec.getMode(widthMeasureSpec);
            if (measureModeWidth != MeasureSpec.UNSPECIFIED) {
                int maximumWidth = MeasureSpec.getSize(widthMeasureSpec);
                if (movieWidth > maximumWidth) {
                    scaleH = (float) movieWidth / (float) maximumWidth;
                }
            }
            float scaleW = 1f;
            int measureModeHeight = MeasureSpec.getMode(heightMeasureSpec);
            if (measureModeHeight != MeasureSpec.UNSPECIFIED) {
                int maximumHeight = MeasureSpec.getSize(heightMeasureSpec);
                if (movieHeight > maximumHeight) {
                    scaleW = (float) movieHeight / (float) maximumHeight;
                }
            }
            mScale = 1f / Math.max(scaleH, scaleW);
            mMeasuredMovieWidth = (int) (movieWidth * mScale);
            mMeasuredMovieHeight = (int) (movieHeight * mScale);
            setMeasuredDimension(mMeasuredMovieWidth, mMeasuredMovieHeight);
        } else {
            setMeasuredDimension(getSuggestedMinimumWidth(), getSuggestedMinimumHeight());
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mLeft = (getWidth() - mMeasuredMovieWidth) / 2f;
        mTop = (getHeight() - mMeasuredMovieHeight) / 2f;
        mVisible = getVisibility() == View.VISIBLE;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (movie != null) {
            if (!mPaused) {
                updateAnimationTime();
                drawMovieFrame(canvas);
                invalidateView();
            } else {
                drawMovieFrame(canvas);
            }
        }
    }
    @SuppressLint("NewApi")
    private void invalidateView() {
        if (mVisible) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                postInvalidateOnAnimation();
            } else {
                invalidate();
            }
        }
    }
    private void updateAnimationTime() {
        long now = android.os.SystemClock.uptimeMillis();
        if (mMovieStart == 0) {
            mMovieStart = now;
        }
        int dur = movie.duration();
        if (dur == 0) {
            dur = DEFAULT_MOVIE_VIEW_DURATION;
        }
        mCurrentAnimationTime = (int) ((now - mMovieStart) % dur);
    }
    private void drawMovieFrame(Canvas canvas) {
        movie.setTime(mCurrentAnimationTime);
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(mScale, mScale);
        movie.draw(canvas, mLeft / mScale, mTop / mScale);
        canvas.restore();
    }
    @SuppressLint("NewApi")
    @Override
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        mVisible = screenState == SCREEN_STATE_ON;
        invalidateView();
    }
    @SuppressLint("NewApi")
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        mVisible = visibility == View.VISIBLE;
        invalidateView();
    }
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == View.VISIBLE;
        invalidateView();
    }
}