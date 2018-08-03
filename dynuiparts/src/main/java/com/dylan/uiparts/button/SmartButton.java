package com.dylan.uiparts.button;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.dylan.common.sketch.Colors;
import com.dylan.common.utils.Utility;
import com.dylan.uiparts.R;
import com.dylan.uiparts.fontable.FontableButton;

public class SmartButton extends FontableButton {

	public SmartButton(Context context) {
		super(context);
		init(context, null, 0);
	}
	public SmartButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}
	public SmartButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}
	
	private int mBgColorNormal = 0xffaaaaaa;
	private int mBgColorPressed = 0xff999999;
	private int mBgColorDisabled = 0xffeeeeee;
	private int mCornerRadius = 0;
	private boolean mShadowEnabled = false;
	private int mShadowColor = 0xffaaaaaa;
	private int mShadowHeight = 0;
	@SuppressLint({ "ClickableViewAccessibility", "InlinedApi" })
	@SuppressWarnings("ResourceType")
	private void init(Context context, AttributeSet attrs, int defStyle) {

		if (attrs == null)return;
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SmartView, defStyle, 0);
		if (a != null) {
			mBgColorNormal = a.getColor(R.styleable.SmartView_sv_bgcolor_normal, 0xffaaaaaa);
			if (a.hasValue(R.styleable.SmartView_sv_bgcolor_pressed)) {
				mBgColorPressed = a.getColor(R.styleable.SmartView_sv_bgcolor_pressed, 0xff999999);
			} else {
				mBgColorPressed = offsetColor(mBgColorNormal, 0.8f);
			}
			mBgColorDisabled = a.getColor(R.styleable.SmartView_sv_bgcolor_disabled, mBgColorNormal);

			int textColorNormal = 0;
			int textColorPressed = 0;
			int textColorDisabled = 0;
			if (a.hasValue(R.styleable.SmartView_sv_txcolor_normal)) {
				textColorNormal = a.getColor(R.styleable.SmartView_sv_txcolor_normal, 0xffffffff);
			} else {
				TypedArray ta = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.textColor});
		        if (ta != null) textColorNormal = ta.getColor(0, 0xffffffff);
		        ta.recycle();
			}
			textColorPressed = a.getColor(R.styleable.SmartView_sv_txcolor_pressed, textColorNormal);
			if (a.hasValue(R.styleable.SmartView_sv_txcolor_disabled)) {
				textColorDisabled = a.getColor(R.styleable.SmartView_sv_txcolor_disabled, 0xff777777);
			} else {
				textColorDisabled = offsetColor(textColorNormal, 0.5f);
			}
			ColorStateList textColor = Colors.createColorStateList(textColorNormal, textColorPressed, textColorPressed, textColorDisabled);
			super.setTextColor(textColor);

			mCornerRadius = (int) a.getDimension(R.styleable.SmartView_sv_cornerRadius, 0.0f);
			mShadowEnabled = a.getBoolean(R.styleable.SmartView_sv_shadowEnabled, false);
			mShadowColor = a.getColor(R.styleable.SmartView_sv_shadowColor, 0xffaaaaaa);
			mShadowHeight = (int)a.getDimension(R.styleable.SmartView_sv_shadowHeight, 0.0f);
			a.recycle();
		}

		try {
			if (true) {
				TypedArray ta = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.padding});
				if (ta != null) {
					mPaddingTop = mPaddingBottom = mPaddingLeft = mPaddingRight = ta.getDimensionPixelSize(0, 0);
					ta.recycle();
				}
			}
			if (Utility.isHoneycombOrLater()) {
				TypedArray ta = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.paddingStart, android.R.attr.paddingEnd});
		        if (ta != null) {
					if (ta.hasValue(0)) mPaddingLeft = ta.getDimensionPixelSize(0, mPaddingLeft);
					if (ta.hasValue(1)) mPaddingRight = ta.getDimensionPixelSize(1, mPaddingRight);
			        ta.recycle();
		        }
			}
			if (true) {
				TypedArray ta = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.paddingLeft, android.R.attr.paddingRight});
		        if (ta != null) {
		            mPaddingLeft = ta.getDimensionPixelSize(0, mPaddingLeft);
		            mPaddingRight = ta.getDimensionPixelSize(1, mPaddingRight);
			        ta.recycle();
		        }
			}
			if (true) {
				TypedArray ta = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.paddingTop, android.R.attr.paddingBottom});
		        if (ta != null) {
		        	mPaddingTop = ta.getDimensionPixelSize(0, mPaddingTop);
		            mPaddingBottom = ta.getDimensionPixelSize(1, mPaddingBottom);
			        ta.recycle();
		        }
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (true) {
        	boolean clickable = true;
			TypedArray ta = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.clickable});
	        if (ta != null) {
	        	clickable = ta.getBoolean(0, true);
	        }
	        setClickable(clickable);
		}
	}	
	
	@Override
	public void setBackgroundColor(int color) {
		setBackgroundColor(color, offsetColor(color, 0.8f), color);
	}
	public void setBackgroundColor(int normal, int pressed) {
		setBackgroundColor(normal, pressed, normal);
	}
	public void setBackgroundColor(int normal, int pressed, int disabled) {
		mBgColorNormal = normal;
		mBgColorPressed = pressed;
		mBgColorDisabled = disabled;
		refresh();
	}
	
	@Override
	public void setTextColor(int color) {
		setTextColor(color, color, offsetColor(color, 0.5f));
	}
	public void setTextColor(int normal, int disabled) {
		setTextColor(normal, normal, disabled);
	}
	public void setTextColor(int normal, int pressed, int disabled) {
		ColorStateList textColor = Colors.createColorStateList(normal, pressed, pressed, disabled);
		super.setTextColor(textColor);
	}
	public void setTextColor(int normal, int pressed, int disabled, int selected) {
		ColorStateList textColor = Colors.createColorStateList(normal, pressed, pressed, disabled, selected);
		super.setTextColor(textColor);
	}
	
	public void setCornetRadius(int radius) {
		mCornerRadius = radius;
		refresh();
	}

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        refresh();
    }	
	@Override
	public void setPadding(int left, int top, int right, int bottom) {
		mPaddingLeft = left;
		mPaddingTop = top;
		mPaddingRight = right;
		mPaddingBottom = bottom;
		super.setPadding(mPaddingLeft, mPaddingTop + mShadowHeight, mPaddingRight, mPaddingBottom);
	}
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        super.setClickable(enabled);
        refresh();
    }
	
	private Drawable mPressedDrawable = null;
	private Drawable mNormalDrawable = null;
	
	private int mPaddingLeft = 0;	
	private int mPaddingTop = 0;	
	private int mPaddingRight = 0;	
	private int mPaddingBottom = 0;
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (!isEnabled())super.onTouchEvent(event);
			updateBackground(mPressedDrawable);
			super.setPadding(mPaddingLeft, mPaddingTop + mShadowHeight, mPaddingRight, mPaddingBottom);
			break;
		case MotionEvent.ACTION_MOVE:
			Rect r = new Rect();
			getLocalVisibleRect(r);
			if (!r.contains((int)event.getX(), (int)event.getY() + 3 * mShadowHeight) && !r.contains((int)event.getX(), (int)event.getY() - 3 * mShadowHeight)) {
				updateBackground(mNormalDrawable);
				super.setPadding(mPaddingLeft, mPaddingTop + mShadowHeight, mPaddingRight, mPaddingBottom + mShadowHeight);
			}
			break;
		case MotionEvent.ACTION_OUTSIDE:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			updateBackground(mNormalDrawable);
			super.setPadding(mPaddingLeft, mPaddingTop + mShadowHeight, mPaddingRight, mPaddingBottom + mShadowHeight);
			break;
		}
		return super.onTouchEvent(event);
	}
	
	private int offsetColor(int color, float offset) {	//adjust brightness to offset [0~1]
		int alpha = Color.alpha(color);
		float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= offset;
        return Color.HSVToColor(alpha, hsv);
	}
    private LayerDrawable createDrawable(int radius, int topColor, int bottomColor) {
        float[] outerRadius = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        RoundRectShape topRoundRect = new RoundRectShape(outerRadius, null, null);
        ShapeDrawable topShapeDrawable = new ShapeDrawable(topRoundRect);
        topShapeDrawable.getPaint().setColor(topColor);
        RoundRectShape roundRectShape = new RoundRectShape(outerRadius, null, null);
        ShapeDrawable bottomShapeDrawable = new ShapeDrawable(roundRectShape);
        bottomShapeDrawable.getPaint().setColor(bottomColor);
        Drawable[] drawArray = {bottomShapeDrawable, topShapeDrawable};
        LayerDrawable layerDrawable = new LayerDrawable(drawArray);
        if (mShadowEnabled && topColor != Color.TRANSPARENT) {
            layerDrawable.setLayerInset(0, 0, 0, 0, 0);  /*index, left, top, right, bottom*/
        } else {
            layerDrawable.setLayerInset(0, 0, mShadowHeight, 0, 0);  /*index, left, top, right, bottom*/
        }
        layerDrawable.setLayerInset(1, 0, 0, 0, mShadowHeight);  /*index, left, top, right, bottom*/
        return layerDrawable;
    }
    private void refresh() {		
        if (isEnabled()) {
            if (mShadowEnabled) {
                mPressedDrawable = createDrawable(mCornerRadius, Color.TRANSPARENT, mBgColorPressed);
                mNormalDrawable = createDrawable(mCornerRadius, mBgColorNormal, mShadowColor);
            } else {
                mShadowHeight = 0;
                mPressedDrawable = createDrawable(mCornerRadius, mBgColorPressed, Color.TRANSPARENT);
                mNormalDrawable = createDrawable(mCornerRadius, mBgColorNormal, Color.TRANSPARENT);
            }
        } else {
        	mPressedDrawable = createDrawable(mCornerRadius, mBgColorDisabled, Color.TRANSPARENT);
        	mNormalDrawable = createDrawable(mCornerRadius, mBgColorDisabled, Color.TRANSPARENT);
        }
        updateBackground(mNormalDrawable);
        super.setPadding(mPaddingLeft, mPaddingTop + mShadowHeight, mPaddingRight, mPaddingBottom + mShadowHeight);
    }
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void updateBackground(Drawable background) {
		if (background == null) return;
        if (Utility.isJellyBeanOrLater()) {
            setBackground(background);
        } else {
            setBackgroundDrawable(background);
        }
	}
}
