package com.dylan.uiparts.edittext;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.appcompat.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.dylan.uiparts.R;

public class PasswordEditText extends AppCompatEditText implements TextWatcher {
    private Paint mPaint;
    private int mPasswordItemWidth;
    private int mPasswordNumber = 6;
    private int mBgColor = Color.parseColor("#d1d2d6");
    private int mBgSize = 1;
    private int mBgCorner = 0;
    private int mDivisionLineColor = mBgColor;
    private int mDivisionLineSize = 1;
    private int mPasswordColor = mDivisionLineColor;
    private int mPasswordRadius = 4;

    public PasswordEditText(Context context) {
        this(context, null);
    }

    public PasswordEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
        initAttributeSet(context, attrs);
        setCursorVisible(false);
        addTextChangedListener(this);
    }

    private void initAttributeSet(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PasswordEditText);
        mDivisionLineSize = (int) array.getDimension(R.styleable.PasswordEditText_pet_divisionLineSize, dip2px(mDivisionLineSize));
        mPasswordRadius = (int) array.getDimension(R.styleable.PasswordEditText_pet_passwordRadius, dip2px(mPasswordRadius));
        mBgSize = (int) array.getDimension(R.styleable.PasswordEditText_pet_bgSize, dip2px(mBgSize));
        mBgCorner = (int) array.getDimension(R.styleable.PasswordEditText_pet_bgCorner, 0);
        mBgColor = array.getColor(R.styleable.PasswordEditText_pet_bgColor, mBgColor);
        mDivisionLineColor = array.getColor(R.styleable.PasswordEditText_pet_divisionLineColor, mDivisionLineColor);
        mPasswordColor = array.getColor(R.styleable.PasswordEditText_pet_passwordColor, mDivisionLineColor);
        array.recycle();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    private int dip2px(int dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getResources().getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int passwordWidth = getWidth() - (mPasswordNumber - 1) * mDivisionLineSize;
        mPasswordItemWidth = passwordWidth / mPasswordNumber;
        drawBg(canvas);
        drawDivisionLine(canvas);
        drawHidePassword(canvas);
    }

    private void drawBg(Canvas canvas) {
        mPaint.setColor(mBgColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mBgSize);
        RectF rectF = new RectF(mBgSize, mBgSize, getWidth() - mBgSize, getHeight() - mBgSize);
        if (mBgCorner == 0) {
            canvas.drawRect(rectF, mPaint);
        } else {
            canvas.drawRoundRect(rectF, mBgCorner, mBgCorner, mPaint);
        }
    }

    private void drawHidePassword(Canvas canvas) {
        int passwordLength = getText().length();
        mPaint.setColor(mPasswordColor);
        mPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < passwordLength; i++) {
            int cx = i * mDivisionLineSize + i * mPasswordItemWidth + mPasswordItemWidth / 2 + mBgSize;
            canvas.drawCircle(cx, getHeight() / 2, mPasswordRadius, mPaint);
        }
    }

    private void drawDivisionLine(Canvas canvas) {
        mPaint.setStrokeWidth(mDivisionLineSize);
        mPaint.setColor(mDivisionLineColor);
        for (int i = 0; i < mPasswordNumber - 1; i++) {
            int startX = (i + 1) * mDivisionLineSize + (i + 1) * mPasswordItemWidth + mBgSize;
            canvas.drawLine(startX, mBgSize, startX, getHeight() - mBgSize, mPaint);
        }
    }

    private OnEnoughListener onEnoughListener;
    public void setOnEnoughListener(OnEnoughListener onEnoughListener) {
        this.onEnoughListener = onEnoughListener;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String text = editable.toString();
        if (text.length() == mPasswordNumber && onEnoughListener != null) {
            onEnoughListener.onEnough(this);
        }
    }

    public interface OnEnoughListener {
        void onEnough(PasswordEditText thiz);
    }
}