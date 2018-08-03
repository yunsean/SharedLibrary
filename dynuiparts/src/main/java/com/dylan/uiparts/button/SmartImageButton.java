package com.dylan.uiparts.button;

import com.dylan.uiparts.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;

public class SmartImageButton extends ImageButton {
	public SmartImageButton(Context context) {
		super(context);
		init(context, null, 0);
	}
	public SmartImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}
	public SmartImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}
	
	private int mFilterColorPressed = 0xaa000000;
	private int mFilterColorDisabled = 0x55000000;
	private void init(Context context, AttributeSet attrs, int defStyle) {
		if (attrs == null)return;
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SmartImageButton, defStyle, 0);
		if (a != null) {			
			mFilterColorPressed = a.getColor(R.styleable.SmartImageButton_sib_filterColor_pressed, 0x55000000);
			mFilterColorDisabled = a.getColor(R.styleable.SmartImageButton_sib_filterColor_disabled, 0x77ffffff);
			a.recycle();
		}
		boolean enabled = attrs.getAttributeBooleanValue("http://schemas.android.com/apk/res/android", "enabled", true);
		setEnabled(enabled);
	}

	public void setPressedFilterColor(int pressedFilterColor) {
		mFilterColorPressed = pressedFilterColor;
        refresh();
	}
	public void setPressedFilterColor(int pressedFilterColor, int disabledFilterColor) {
		mFilterColorPressed = pressedFilterColor;
		mFilterColorDisabled = disabledFilterColor;
        refresh();
	}
	
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        refresh();
    }
	
    private void refresh() {
        if (!isEnabled()) {
        	setColorFilter(mFilterColorDisabled);
        } else if (isPressed()) {
        	setColorFilter(mFilterColorPressed);
        } else {
        	setColorFilter(0x00000000);
        } 
    }
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (!isEnabled())return false;
			if (mFilterColorPressed != 0) {
				setColorFilter(mFilterColorPressed);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mFilterColorPressed != 0) {
				Rect r = new Rect();
				getLocalVisibleRect(r);
				if (!r.contains((int) event.getX(), (int) event.getY())) {
					setColorFilter(0x00000000);
				}
			}
			break;
		case MotionEvent.ACTION_OUTSIDE:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (mFilterColorPressed != 0) {
				setColorFilter(0x00000000);
			}
			break;
		}
		return super.onTouchEvent(event);
	}
}
