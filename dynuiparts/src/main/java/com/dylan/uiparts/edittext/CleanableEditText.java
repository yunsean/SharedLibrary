package com.dylan.uiparts.edittext;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.dylan.common.utils.Utility;
import com.dylan.uiparts.R;

public class CleanableEditText extends EditText {

	Drawable mCleanBtn = null;
	boolean mHasClean = false;
	boolean mEnableClean = true;
	boolean mShowAlways = false;
	boolean mShowOnlyEditing = true;
	boolean mHasFocus = false;
	private OnClearClickedListener mOnClearClickedListener = null;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public CleanableEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr);
	}
	public CleanableEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}
	public CleanableEditText(Context context) {
		super(context);
		init(context, null, 0);
	}
	public CleanableEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
		setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				mHasFocus = hasFocus;
				if (!mShowOnlyEditing)return;
				judgeShowClean();
			}
		});
		addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				judgeShowClean();
			}
		});
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		super.setText(text, type);
		if (!isInEditMode()) {
			judgeShowClean();
		}
	}

	private void judgeShowClean() {
		boolean canClean = getText().toString().length() > 0;
		Drawable[] icons = getCompoundDrawables();
		boolean hasFocus = !mShowOnlyEditing || mHasFocus; 
		if (hasFocus && isEnabled() && canClean && mEnableClean) {
			setCompoundDrawables(icons[0], icons[1], mCleanBtn, icons[3]);
			setCompoundDrawablePadding(Utility.dip2px(getContext(), 5));
		} else {
			setCompoundDrawables(icons[0], icons[1], null, icons[3]);
			setCompoundDrawablePadding(0);
		}
	}
	public void setEnableClean(boolean enable) {
		mEnableClean = enable;
		boolean canClean = getText().toString().length() > 0;
		Drawable[] icons = getCompoundDrawables();
		boolean hasFocus = !mShowOnlyEditing || mHasFocus;
		if (hasFocus && canClean && mEnableClean) {
			setCompoundDrawables(icons[0], icons[1], mCleanBtn, icons[3]);
			setCompoundDrawablePadding(Utility.dip2px(getContext(), 5));
		} else {
			setCompoundDrawables(icons[0], icons[1], null, icons[3]);
		}
	}
	public void setShowAlways(boolean always) {
		mShowAlways = always && !mShowOnlyEditing;
		judgeShowClean();
	}
	public void setClearClickedListener(OnClearClickedListener listener) {
		mOnClearClickedListener = listener;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		Drawable[] icons = getCompoundDrawables();
		if (!enabled) {
			setCompoundDrawables(icons[0], icons[1], null, icons[3]);
		} else {
			boolean canClean = getText().toString().length() > 0;
			if (mShowAlways)canClean = true;
			mHasClean = canClean;
			boolean hasFocus = !mShowOnlyEditing || mHasFocus;
			if (hasFocus && isEnabled() && canClean && mEnableClean) {
				setCompoundDrawables(icons[0], icons[1], mCleanBtn, icons[3]);
				setCompoundDrawablePadding(Utility.dip2px(getContext(), 5));
			} else {
				setCompoundDrawables(icons[0], icons[1], null, icons[3]);
			}
		}
	}
	public void setFocus(boolean hasFocus) {
		mHasFocus = hasFocus;
		if (mShowOnlyEditing) {
			judgeShowClean();
		}
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("ClickableViewAccessibility")
	void init(Context context, AttributeSet attrs, int defStyle) {
		if (attrs != null) {
			final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CleanableEditText, defStyle, 0);
			mCleanBtn = a.getDrawable(R.styleable.CleanableEditText_cet_button);
			mShowAlways = !a.getBoolean(R.styleable.CleanableEditText_cet_autoHide, true);
			mShowOnlyEditing = a.getBoolean(R.styleable.CleanableEditText_cet_onlyEditing, true);
			a.recycle();
		}
		if (mCleanBtn == null) {
			Resources res = context.getResources();
			mCleanBtn = res.getDrawable(R.drawable.edittext_clear_btn);
		}
		mCleanBtn.setBounds(0, 0, mCleanBtn.getMinimumWidth(), mCleanBtn.getMinimumHeight()); 
		addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				boolean canClean = getText().toString().length() > 0;
				if (mShowAlways)canClean = true;
				if (canClean == mHasClean)return;
				mHasClean = canClean;
				Drawable[] icons = getCompoundDrawables();
				boolean hasFocus = !mShowOnlyEditing || mHasFocus;
				if (hasFocus && isEnabled() && canClean && mEnableClean) {
					setCompoundDrawables(icons[0], icons[1], mCleanBtn, icons[3]);
					setCompoundDrawablePadding(Utility.dip2px(getContext(), 5));
				} else {
					setCompoundDrawables(icons[0], icons[1], null, icons[3]);
				}
			}
		});
		setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (!mEnableClean)return false;
				if (event.getAction() == MotionEvent.ACTION_UP && mHasClean) {
					Rect bound = mCleanBtn.getBounds();
					int x = (int)event.getX();
					int width = getWidth() - getPaddingRight();
					if(x >= width - bound.width() && x <= width) {
						setText("");
						if (mOnClearClickedListener != null) {
							mOnClearClickedListener.onClearClicked();
						}
					}
				} 
				return false;
			}
		});

		String text = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "text");
		if (text != null && text.length() > 0)setEnableClean(mEnableClean);
		super.setEnabled(true);
	}
	
	public interface OnClearClickedListener {
		public void onClearClicked();
	}
}
