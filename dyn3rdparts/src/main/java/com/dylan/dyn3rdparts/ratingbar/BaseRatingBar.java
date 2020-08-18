package com.dylan.dyn3rdparts.ratingbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dylan.dyn3rdparts.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class BaseRatingBar extends LinearLayout implements SimpleRatingBar {

    public interface OnRatingChangeListener {
        void onRatingChange(BaseRatingBar ratingBar, float rating);
    }

    public static final String TAG = "SimpleRatingBar";

    private static final int MAX_CLICK_DISTANCE = 5;
    private static final int MAX_CLICK_DURATION = 200;

    private final DecimalFormat mDecimalFormat;

    private int mNumStars;
    private int mPadding = 0;
    private int mStarWidth;
    private int mStarHeight;
    private float mRating = -1;
    private float mStepSize = 1f;
    private float mPreviousRating = 0;
    private boolean mIsTouchable = true;
    private boolean mClearRatingEnabled = true;

    private float mStartX;
    private float mStartY;

    private Drawable mEmptyDrawable;
    private Drawable mFilledDrawable;

    private OnRatingChangeListener mOnRatingChangeListener;

    protected List<PartialView> mPartialViews;

    public BaseRatingBar(Context context) {
        this(context, null);
    }
    public BaseRatingBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseRatingBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RatingBarAttributes);
        float rating = typedArray.getFloat(R.styleable.RatingBarAttributes_rb_rating, mRating);
        mNumStars = typedArray.getInt(R.styleable.RatingBarAttributes_rb_numStars, mNumStars);
        mPadding = typedArray.getDimensionPixelSize(R.styleable.RatingBarAttributes_rb_starPadding, mPadding);
        mStarWidth = typedArray.getDimensionPixelSize(R.styleable.RatingBarAttributes_rb_starWidth, 0);
        mStarHeight = typedArray.getDimensionPixelSize(R.styleable.RatingBarAttributes_rb_starHeight, 0);
        mStepSize = typedArray.getFloat(R.styleable.RatingBarAttributes_rb_stepSize, mStepSize);
        mEmptyDrawable = typedArray.getDrawable(R.styleable.RatingBarAttributes_rb_drawableEmpty);
        mFilledDrawable = typedArray.getDrawable(R.styleable.RatingBarAttributes_rb_drawableFilled);
        mIsTouchable = typedArray.getBoolean(R.styleable.RatingBarAttributes_rb_touchable, mIsTouchable);
        mClearRatingEnabled = typedArray.getBoolean(R.styleable.RatingBarAttributes_rb_clearRatingEnabled, mClearRatingEnabled);
        typedArray.recycle();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        mDecimalFormat = new DecimalFormat("#.##", symbols);
        verifyParamsValue();
        initRatingView();
        setRating(rating, false);
    }
    private void verifyParamsValue() {
        if (mNumStars <= 0) mNumStars = 5;
        if (mPadding < 0) mPadding = 0;
        if (mEmptyDrawable == null) mEmptyDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ratingbar_empty);
        if (mFilledDrawable == null) mFilledDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ratingbar_filled);
        if (mStepSize > 1.0f) mStepSize = 1.0f;
        else if (mStepSize < 0.1f) mStepSize = 0.1f;
    }

    private void initRatingView() {
        mPartialViews = new ArrayList<>();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(mStarWidth == 0 ? LayoutParams.WRAP_CONTENT : mStarWidth, mStarHeight == 0 ? ViewGroup.LayoutParams.WRAP_CONTENT : mStarHeight);
        for (int i = 1; i <= mNumStars; i++) {
            PartialView partialView = getPartialView(i, mFilledDrawable, mEmptyDrawable);
            mPartialViews.add(partialView);
            addView(partialView, params);
        }
    }

    private PartialView getPartialView(final int ratingViewId, Drawable filledDrawable, Drawable emptyDrawable) {
        PartialView partialView = new PartialView(getContext());
        partialView.setId(ratingViewId);
        partialView.setPadding(mPadding, mPadding, mPadding, mPadding);
        partialView.setFilledDrawable(filledDrawable);
        partialView.setEmptyDrawable(emptyDrawable);
        return partialView;
    }

    protected void emptyRatingBar() {
        fillRatingBar(0, true);
    }
    protected void fillRatingBar(final float rating, boolean animated) {
        for (PartialView partialView : mPartialViews) {
            int ratingViewId = partialView.getId();
            double maxIntOfRating = Math.ceil(rating);
            if (ratingViewId > maxIntOfRating) {
                partialView.setEmpty();
                continue;
            }
            if (ratingViewId == maxIntOfRating) partialView.setPartialFilled(rating);
            else partialView.setFilled();
        }
    }

    @Override
    public void setNumStars(int numStars) {
        if (numStars <= 0) return;
        mPartialViews.clear();
        removeAllViews();
        mNumStars = numStars;
        initRatingView();
    }

    @Override
    public int getNumStars() {
        return mNumStars;
    }

    @Override
    public void setRating(float rating) {
        setRating(rating, true);
    }
    public void setRating(float rating, boolean animated) {
        if (rating > mNumStars) rating = mNumStars;
        if (rating < 0) rating = 0;
        if (mRating == rating) return;
        mRating = rating;
        if (mOnRatingChangeListener != null) mOnRatingChangeListener.onRatingChange(this, mRating);
        fillRatingBar(rating, animated);
    }

    @Override
    public float getRating() {
        return mRating;
    }

    @Override
    public void setStarPadding(int ratingPadding) {
        if (ratingPadding < 0) return;
        mPadding = ratingPadding;
        for (PartialView partialView : mPartialViews) {
            partialView.setPadding(mPadding, mPadding, mPadding, mPadding);
        }
    }

    @Override
    public int getStarPadding() {
        return mPadding;
    }

    @Override
    public void setEmptyDrawableRes(@DrawableRes int res) {
        setEmptyDrawable(ContextCompat.getDrawable(getContext(), res));
    }

    @Override
    public void setFilledDrawableRes(@DrawableRes int res) {
        setFilledDrawable(ContextCompat.getDrawable(getContext(), res));
    }

    @Override
    public void setEmptyDrawable(Drawable drawable) {
        mEmptyDrawable = drawable;

        for (PartialView partialView : mPartialViews) {
            partialView.setEmptyDrawable(drawable);
        }
    }

    @Override
    public void setFilledDrawable(Drawable drawable) {
        mFilledDrawable = drawable;

        for (PartialView partialView : mPartialViews) {
            partialView.setFilledDrawable(drawable);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isTouchable()) return false;
        float eventX = event.getX();
        float eventY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = eventX;
                mStartY = eventY;
                mPreviousRating = mRating;
                break;
            case MotionEvent.ACTION_MOVE:
                handleMoveEvent(eventX);
                break;
            case MotionEvent.ACTION_UP:
                if (!isClickEvent(mStartX, mStartY, event)) return false;
                handleClickEvent(eventX);
        }
        return true;
    }

    private void handleMoveEvent(float eventX) {
        for (PartialView partialView : mPartialViews) {
            if (eventX < partialView.getWidth() / 10f) {
                setRating(0);
                return;
            }
            if (!isPositionInRatingView(eventX, partialView)) continue;
            float rating = calculateRating(eventX, partialView);
            if (mRating != rating) setRating(rating);
        }
    }

    private float calculateRating(float eventX, PartialView partialView) {
        float ratioOfView = Float.parseFloat(mDecimalFormat.format((eventX - partialView.getLeft()) / partialView.getWidth()));
        float steps = Math.round(ratioOfView / mStepSize) * mStepSize;
        return Float.parseFloat(mDecimalFormat.format(partialView.getId() - (1 - steps)));
    }

    private void handleClickEvent(float eventX) {
        for (PartialView partialView : mPartialViews) {
            if (!isPositionInRatingView(eventX, partialView)) continue;
            float rating = partialView.getId();
            if (mPreviousRating == rating && isClearRatingEnabled()) setRating(0);
            else setRating(rating);
            break;
        }
    }

    private boolean isPositionInRatingView(float eventX, View ratingView) {
        return eventX > ratingView.getLeft() && eventX < ratingView.getRight();
    }

    private boolean isClickEvent(float startX, float startY, MotionEvent event) {
        float duration = event.getEventTime() - event.getDownTime();
        if (duration > MAX_CLICK_DURATION) return false;
        float differenceX = Math.abs(startX - event.getX());
        float differenceY = Math.abs(startY - event.getY());
        return !(differenceX > MAX_CLICK_DISTANCE || differenceY > MAX_CLICK_DISTANCE);
    }

    public void setOnRatingChangeListener(OnRatingChangeListener onRatingChangeListener) {
        mOnRatingChangeListener = onRatingChangeListener;
    }

    public boolean isTouchable() {
        return mIsTouchable;
    }

    public void setTouchable(boolean touchable) {
        this.mIsTouchable = touchable;
    }

    public void setClearRatingEnabled(boolean enabled) {
        this.mClearRatingEnabled = enabled;
    }

    public boolean isClearRatingEnabled() {
        return mClearRatingEnabled;
    }

    public float getStepSize() {
        return mStepSize;
    }

    public void setStepSize(@FloatRange(from = 0.1, to = 1.0) float stepSize) {
        this.mStepSize = stepSize;
    }
}