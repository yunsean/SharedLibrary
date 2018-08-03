package com.dylan.uiparts.button;

import com.dylan.uiparts.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.widget.CompoundButton;

public class Switch extends CompoundButton
{
	private CharSequence textLeft;
	private CharSequence textRight;
	private Drawable drawableBackground;
	private Drawable drawableSwitch;
	private Layout layoutLeft;
	private Layout layoutRight;
	private int switchMinWidth;
	private int width;
	private int height;
	private int innerPadding;
	private int switchPadding;
	private int textColorChecked;
	private int textColorUnChecked;
	private OnClickListener onClickListener;

	public Switch(Context context) {
		this(context, null);
	}
	public Switch(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.SwitchStyle);
	}
	public Switch(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.getPaint().setAntiAlias(true);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Switch, defStyle, 0);
		this.textLeft = a.getText(R.styleable.Switch_sw_textLeft);
		this.textRight = a.getText(R.styleable.Switch_sw_textRight);
		this.switchMinWidth = a.getDimensionPixelSize(R.styleable.Switch_sw_switchMinWidth, 250);
		this.textColorUnChecked = a.getColor(R.styleable.Switch_sw_colorUnChecked, 0xff808080);
		this.textColorChecked = a.getColor(R.styleable.Switch_sw_colorChecked, 0xffffffff);
		this.drawableBackground = a.getDrawable(R.styleable.Switch_sw_backgroundDrawable);
		this.drawableSwitch = a.getDrawable(R.styleable.Switch_sw_switchDrawable);
		this.switchPadding = a.getDimensionPixelSize(R.styleable.Switch_sw_switchPadding, 32);
		this.innerPadding = a.getDimensionPixelSize(R.styleable.Switch_sw_innerPadding, 20);
		this.setChecked(a.getBoolean(R.styleable.Switch_sw_isChecked, false));
		a.recycle();
		if (this.textLeft == null)this.textLeft = "Off";
		if (this.textRight == null)this.textRight = "On";
		if (this.drawableBackground == null)this.drawableBackground = getResources().getDrawable(R.drawable.switch_thumb_activated_holo_dark);
		if (this.drawableSwitch == null)this.drawableSwitch = getResources().getDrawable(R.drawable.switch_thumb_activated_holo_light);
		if (this.textLeft == null || this.textRight == null)
			throw new IllegalStateException("Either textLeft or textRight is null. Please them via the attributes with the same name in the layout");
	}
	
	public void setTextLeft(CharSequence textLeft) {
		if (textLeft == null)
			throw new IllegalArgumentException("The text for the left side must not be null!");
		this.textLeft = textLeft;
		this.requestLayout();
	}
	public CharSequence getTextLeft() {
		return this.textLeft;
	}
	public CharSequence getTextRight() {
		return this.textRight;
	}
	public void setTextRight(CharSequence textRight) {
		if (textRight == null)
			throw new IllegalArgumentException("The text for the right side must not be null!");
		this.textRight = textRight;
		this.requestLayout();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (this.layoutLeft == null)
			this.layoutLeft = this.makeLayout(this.textLeft);
		if (this.layoutRight == null)
			this.layoutRight = this.makeLayout(this.textRight);

		final int maxTextWidth = Math.max(this.layoutLeft.getWidth(), this.layoutRight.getWidth());
		int actualWidth = Math.max(this.switchMinWidth, maxTextWidth * 2 + this.getPaddingLeft() + this.getPaddingRight() + this.innerPadding * 4);
		final int switchHeight = Math.max(this.drawableBackground.getIntrinsicHeight(), this.drawableSwitch.getIntrinsicHeight());

		this.width = actualWidth;
		this.height = switchHeight;
		if (this.getText() != null)
			actualWidth += this.makeLayout(this.getText()).getWidth() + this.switchPadding;
		setMeasuredDimension(actualWidth, switchHeight);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int right = this.getWidth() - this.getPaddingRight();
		int left = right - this.width;
		this.drawableBackground.setBounds(left, 0, right, this.height);
		this.drawableBackground.draw(canvas);
		if (this.isChecked())
			this.drawableSwitch.setBounds(left, 0, left + (this.width / 2), this.height);
		else
			this.drawableSwitch.setBounds(left + this.width / 2, 0, right, this.height);
		this.drawableSwitch.draw(canvas);
		canvas.save();

		this.getPaint().setColor(this.isChecked() ? this.textColorChecked : this.textColorUnChecked);
		canvas.translate(left + (this.width / 2 - this.layoutLeft.getWidth()) / 2, (this.height - this.layoutLeft.getHeight()) / 2);
		this.layoutLeft.draw(canvas);
		canvas.restore();

		this.getPaint().setColor(!this.isChecked() ? this.textColorChecked : this.textColorUnChecked);
		canvas.translate(left + (this.width / 2 - this.layoutRight.getWidth()) / 2 + this.width / 2, (this.height - this.layoutRight.getHeight()) / 2);
		this.layoutRight.draw(canvas);
		canvas.restore();
	}

	@Override
	public int getCompoundPaddingRight() {
		int padding = super.getCompoundPaddingRight() + this.width;
		if (!TextUtils.isEmpty(getText()))
			padding += this.switchPadding;
		return padding;
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		super.setOnClickListener(l);
		this.onClickListener = l;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				this.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				this.setChecked(!this.isChecked());
				invalidate();
				if (this.onClickListener != null)
					this.onClickListener.onClick(this);
				return false;
		}
		return super.onTouchEvent(event);
	}
	@SuppressLint("FloatMath")
	private Layout makeLayout(CharSequence text) {
		return new StaticLayout(text, this.getPaint(), (int)Math.ceil(Layout.getDesiredWidth(text, this.getPaint())), Layout.Alignment.ALIGN_NORMAL, 1f, 0, true);
	}
	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		int[] myDrawableState = getDrawableState();
		if (this.drawableSwitch != null)
			this.drawableSwitch.setState(myDrawableState);
		invalidate();
	}
}