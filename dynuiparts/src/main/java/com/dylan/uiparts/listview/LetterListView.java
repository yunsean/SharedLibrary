package com.dylan.uiparts.listview;

import com.dylan.uiparts.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class LetterListView extends View {
	
	public void setOnTouchingLetterChangedListener(OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
		this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
	}
	
	private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
	private String[] LEETER = {"#","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
	private int mChoose = -1;
	private Paint mPaint = new Paint();
	private boolean mShowBg = false;
	private int mBackColor = Color.GRAY;
	private int mBackHightlightColor = Color.WHITE;
	private int mTextColor = Color.WHITE;
	private int mTextHighlightColor = Color.BLUE;

	public LetterListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LetterList, defStyle, 0);
		mBackColor = a.getColor(R.styleable.LetterList_ll_backColor, Color.GRAY);
		mBackHightlightColor = a.getColor(R.styleable.LetterList_ll_backHightlightColor, Color.WHITE);
		mTextColor = a.getColor(R.styleable.LetterList_ll_textColor, Color.WHITE);
		mTextHighlightColor = a.getColor(R.styleable.LetterList_ll_textHighlightColor, Color.BLUE);
		a.recycle();
	}
	public LetterListView(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.LetterListStyle);
	}
	public LetterListView(Context context) {
		this(context, null);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mShowBg){
		    canvas.drawColor(mBackHightlightColor);
		} else {
		    canvas.drawColor(mBackColor);
		}
	    int height = getHeight();
	    int width = getWidth();
	    int singleHeight = height / LEETER.length;
    	mPaint.setTextSize(singleHeight * 3 / 4);
	    for(int i = 0; i < LEETER.length; i++){
	    	mPaint.setTypeface(Typeface.DEFAULT_BOLD);
	    	mPaint.setAntiAlias(true);
	    	if(i == mChoose){
	    		mPaint.setColor(mTextHighlightColor);
	    		mPaint.setFakeBoldText(true);
	    	} else {
		    	mPaint.setColor(mTextColor);
	    	}
	    	float xPos = width / 2  - mPaint.measureText(LEETER[i]) / 2;
	    	float yPos = singleHeight * i + singleHeight;
	    	canvas.drawText(LEETER[i], xPos, yPos, mPaint);
	    }	
    	mPaint.reset();   
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final int action = event.getAction();
	    final float y = event.getY();
	    final int oldChoose = mChoose;
	    final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
	    final int c = (int) (y / getHeight() * LEETER.length);	    
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mShowBg = true;
				if(oldChoose != c && listener != null){
					if(c >= 0 && c < LEETER.length){
						listener.onTouchingLetterChanged(LEETER[c]);
						mChoose = c;
						invalidate();
					}
				}				
				break;
			case MotionEvent.ACTION_MOVE:
				if(oldChoose != c && listener != null){
					if(c >= 0 && c < LEETER.length){
						listener.onTouchingLetterChanged(LEETER[c]);
						mChoose = c;
						invalidate();
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				mShowBg = false;
				mChoose = -1;
				invalidate();
				break;
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}
	public interface OnTouchingLetterChangedListener{
		public void onTouchingLetterChanged(String s);
	}	
}
