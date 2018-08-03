package com.dylan.uiparts.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dylan.common.utils.Utility;
import com.dylan.uiparts.R;
import com.dylan.uiparts.button.SmartButton;

public class SegmentBar extends LinearLayout implements OnClickListener {

	private int mLastIndex = 0;
	private Drawable mItemBackground = null;
	private Drawable mDivider = null;
    public void setOnSegmentBarChangedListener(OnSegmentBarChangedListener onSegmentBarChangedListener) {  
        this.onSegmentBarChangedListener = onSegmentBarChangedListener;  
    } 
    public void setValue(Context context, String[] stringArray) {  
        mStringArray = stringArray;  
        if(mStringArray.length < 1) {  
            throw new RuntimeException("the length of String array must bigger than 1");  
        }   
        resolveStringArray(context);  
    }
    public int getSelectedItemIndex() {
        return mLastIndex;
    }
    public void onClick(View v) {  
    	if (v.getTag() == null)return;
        int index = (Integer)v.getTag();  
        if (onSegmentBarChangedListener != null) {
        	onSegmentBarChangedListener.onBarItemChanged(index);            
        }
        TextView lastButton = (TextView)this.getChildAt(mDivider == null ? mLastIndex : (mLastIndex * 2));  
        lastButton.setSelected(false);
        lastButton.invalidate();
        TextView currButton = (TextView)this.getChildAt(mDivider == null ? index : (index * 2));  
        currButton.setSelected(true);  
        mLastIndex = index;  
    }  
    public void setDefaultBarItem(int index) {  
        if (index > mStringArray.length || index < 0) {  
            throw new RuntimeException("the value of default bar item can not bigger than string array's length");  
        }  
        mLastIndex = index;  
        TextView btn = (TextView)this.getChildAt(mDivider == null ? index : (index * 2));  
        btn.setSelected(true);   
        if (onSegmentBarChangedListener != null) {  
            onSegmentBarChangedListener.onBarItemChanged(index);  
        }  
    }     

    private int mPaddingLeft = 0;
    private int mPaddingTop = 0;
    private int mPaddingRight = 0;
    private int mPaddingBottom = 0;
    public void setSegmentPadding(int left, int top, int right, int bottom) {
    	mPaddingLeft = left;
    	mPaddingRight = right;
    	mPaddingTop = top;
    	mPaddingBottom = bottom;
    	int index = this.getChildCount();  
        for(int i = 0; i < index; i++) {  
            ((Button)this.getChildAt(i)).setPadding(left, top, right, bottom); 
        }
    }

    private Drawable mBeginSegmentBackground = null;
    private Drawable mMiddleSegmentBackground = null;
    private Drawable mEndSegmentBackground = null;
    public void setSegmentBackground(int beginResId, int middleResId, int endResId) {
    	if (beginResId != 0)mBeginSegmentBackground = getResources().getDrawable(beginResId);
    	if (middleResId != 0)mMiddleSegmentBackground = getResources().getDrawable(middleResId);
    	if (endResId != 0)mEndSegmentBackground = getResources().getDrawable(endResId);
    	int length = this.getChildCount();  
        for(int i = 0; i < length; i++) {  
            Button button = (Button)this.getChildAt(i); 
            if (i == 0) {  
                if (beginResId != 0)button.setBackgroundResource(beginResId); 
            } else if (i != 0 && i < (length - 1)) {  
            	if (middleResId != 0)button.setBackgroundResource(middleResId);  
            } else {  
            	if (endResId != 0)button.setBackgroundResource(endResId);  
            } 
        }
    }
    public void setSegmentBackground(Drawable begin, Drawable middle, Drawable end) {
    	mBeginSegmentBackground = begin;
    	mMiddleSegmentBackground = middle;
    	mEndSegmentBackground = end;
    	int length = this.getChildCount();  
        for(int i = 0; i < length; i++) {  
            Button button = (Button)this.getChildAt(i); 
            if (i == 0) {  
                if (begin != null)setButtonBackground(button, mBeginSegmentBackground); 
            } else if (i != 0 && i < (length - 1)) {  
                if (middle != null)setButtonBackground(button, mMiddleSegmentBackground);  
            } else {  
                if (end != null)setButtonBackground(button, mEndSegmentBackground);  
            } 
        }
    }
    @SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void setButtonBackground(Button button, Drawable background) {
    	if (Utility.isJellyBeanOrLater())
    		button.setBackground(background);
    	else 
    		button.setBackgroundDrawable(background);
    }
    
    private float mTextSize = 14.f;
    public void setTextSize(float sizeValue) {  
    	mTextSize = sizeValue;
        int index = this.getChildCount();  
        for(int i = 0; i < index; i++) {  
            ((Button)this.getChildAt(i)).setTextSize(mTextSize);  
        }  
    } 

    private int mNormalTextColor = 0xff333333;
    private int mSelectedTextColor = 0xff333333;
    private int mPressedTextColor = 0xff333333; 
    public void setTextColor(int normal, int pressed, int selected) { 
    	mNormalTextColor = normal;
    	mSelectedTextColor = selected;
    	mPressedTextColor = pressed;
        int index = this.getChildCount();   
        for(int i = 0; i < index; i++) {  
            ((SmartButton)this.getChildAt(i)).setTextColor(normal, pressed, normal, selected);
        }  
    } 
    public void setTextColor(int color) {  
    	mNormalTextColor = color;
    	mSelectedTextColor = color;
    	mPressedTextColor = color;
        int index = this.getChildCount();  
        for (int i = 0; i < index; i++) {  
            ((TextView)this.getChildAt(i)).setTextColor(color);  
        }  
    }  
	
	private String[] mStringArray = null; 
    public SegmentBar(Context context) {  
        this(context, null);    
    }       
    public SegmentBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.SegmentBarStyle); 
    }
    public SegmentBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SegmentBar, defStyle, 0);
		mItemBackground = a.getDrawable(R.styleable.SegmentBar_sb_itemBackground);
		mBeginSegmentBackground = a.getDrawable(R.styleable.SegmentBar_sb_backgroundBegin);
		mMiddleSegmentBackground = a.getDrawable(R.styleable.SegmentBar_sb_backgroundMiddle);
		mEndSegmentBackground = a.getDrawable(R.styleable.SegmentBar_sb_backgroundEnd);
		
		mNormalTextColor = a.getColor(R.styleable.SegmentBar_sb_textColor, Color.BLACK);
		mPressedTextColor = mNormalTextColor;
		mSelectedTextColor = mNormalTextColor;
		mNormalTextColor = a.getColor(R.styleable.SegmentBar_sb_textNormalColor, mNormalTextColor);
		mSelectedTextColor = a.getColor(R.styleable.SegmentBar_sb_textSelectedColor, mSelectedTextColor);
		mPressedTextColor = a.getColor(R.styleable.SegmentBar_sb_textPressedColor, mPressedTextColor);

		mPaddingLeft = (int)a.getDimension(R.styleable.SegmentBar_sb_paddingLeft, Utility.dip2px(context, 10.f));
		mPaddingRight = (int)a.getDimension(R.styleable.SegmentBar_sb_paddingRight, Utility.dip2px(context, 10.f));
		mPaddingTop = (int)a.getDimension(R.styleable.SegmentBar_sb_paddingTop, Utility.dip2px(context, 3.f));
		mPaddingBottom = (int)a.getDimension(R.styleable.SegmentBar_sb_paddingBottom, Utility.dip2px(context, 3.f));

		mTextSize = Utility.px2sp(context, a.getDimension(R.styleable.SegmentBar_sb_textSize, Utility.sp2px(context, 16.0f)));
		mDivider = a.getDrawable(R.styleable.SegmentBar_sb_divider);

        CharSequence[] options = null;
        if (a.hasValue(R.styleable.SegmentBar_sb_options)) {
            options = a.getTextArray(R.styleable.SegmentBar_sb_options);
        }
		a.recycle();
        if (options != null && options.length > 1) {
            mStringArray = new String[options.length];
            for (int i = 0; i < options.length; i++) {
                mStringArray[i] = options[i].toString();
            }
            resolveStringArray(context);
        }
	}
            
    private OnSegmentBarChangedListener onSegmentBarChangedListener = null;  
    public interface OnSegmentBarChangedListener {  
        public void onBarItemChanged(int segmentItemIndex);  
    } 

	private void resolveStringArray(Context context) {  
        int length = this.mStringArray.length;  
        for (int i = 0; i < length; i++) {   
            if (mDivider != null && i != 0) {
            	ImageView iv = new ImageView(context);
            	iv.setImageDrawable(mDivider);
            	iv.setTag(null);
                iv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));  
                iv.setPadding(5, 0, 5, 0);
            	addView(iv); 
            }
            SmartButton button = new SmartButton(context);  
            button.setText(mStringArray[i]);  
            button.setGravity(Gravity.CENTER);    
            button.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            button.setTag(i);
            button.setSingleLine();
            button.setOnClickListener(this);
            if (i == 0) {
                if (mBeginSegmentBackground != null) setButtonBackground(button, mBeginSegmentBackground);
                else if (mItemBackground != null)setButtonBackground(button, mItemBackground);
                else button.setBackgroundColor(0x00000000);
            } else if (i == length - 1) {
                if (mEndSegmentBackground != null) setButtonBackground(button, mEndSegmentBackground);
                else if (mItemBackground != null)setButtonBackground(button, mItemBackground);
                else button.setBackgroundColor(0x00000000);            	
            } else {
                if (mMiddleSegmentBackground != null) setButtonBackground(button, mMiddleSegmentBackground);
                else if (mItemBackground != null)setButtonBackground(button, mItemBackground);
                else button.setBackgroundColor(0x00000000);  
            }
            addView(button);
            button.setTextColor(mNormalTextColor, mPressedTextColor, mNormalTextColor, mSelectedTextColor);
            button.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom);
            button.setTextSize(mTextSize); 
        }  
    }  
}
