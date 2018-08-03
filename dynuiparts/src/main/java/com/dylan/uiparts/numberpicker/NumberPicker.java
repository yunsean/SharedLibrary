package com.dylan.uiparts.numberpicker;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.dylan.uiparts.R;

import java.lang.reflect.Field;

public class NumberPicker extends android.widget.NumberPicker {

    private float mTextSize = 0.f;
    private int mTextColor = 0;
    private int mDividerColor = 0;
    public NumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }
    public NumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }
    public NumberPicker(Context context) {
        super(context);
        init(context, null, 0);
    }
    private Context init(Context context, AttributeSet attrs, int defStyle) {
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberPicker, defStyle, 0);
            mTextSize = a.getDimension(R.styleable.NumberPicker_np_textSize, mTextSize);
            mTextColor = a.getColor(R.styleable.NumberPicker_np_textColor, mTextColor);
            mDividerColor = a.getColor(R.styleable.NumberPicker_np_dividerColor, mDividerColor);
            applySetting();
            setDividerColor(mDividerColor);
        }
        return context;
    }

    @Override
    public void addView(View child) {
        this.addView(child, null);
    }
    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        this.addView(child, -1, params);
    }
    @Override
    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        setNumberPicker(child);
    }

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
    }
    public void setTextSize(int textSize) {
        this.mTextSize = textSize;
    }
    private void applySetting() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof EditText) {
                Field selectorWheelPaintField;
                try {
                    selectorWheelPaintField = android.widget.NumberPicker.class.getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    try {
                        if (mTextColor != 0) ((Paint) selectorWheelPaintField.get(this)).setColor(mTextColor);
                        if (mTextSize > 1.f) ((Paint) selectorWheelPaintField.get(this)).setTextSize(mTextSize);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    if (mTextColor != 0) ((TextView) child).setTextColor(mTextColor);
                    if (mTextSize > 1.f) ((TextView) child).setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
                    this.invalidate();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setNumberPicker(View view) {
        if (view instanceof EditText) {
            if (mTextColor != 0) ((EditText) view).setTextColor(mTextColor);
            if (mTextSize > 1.f) ((EditText) view).setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        }
    }
    @SuppressWarnings("unused")
    public void setDividerColor(int dividerColor) {
        mDividerColor = dividerColor;
        if (mDividerColor != 0) {
            Field[] pickerFields = NumberPicker.class.getDeclaredFields();
            for (Field pf : pickerFields) {
                if (pf.getName().equals("mSelectionDivider")) {
                    pf.setAccessible(true);
                    try {
                        pf.set(this, new ColorDrawable(mDividerColor));
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }
}
