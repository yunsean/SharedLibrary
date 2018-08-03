package com.dylan.uiparts.seekbar;

import com.dylan.uiparts.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SeekArc extends View {

	public interface OnSeekArcChangeListener {
		void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser);
	}

	public SeekArc(Context context) {
		super(context);
		init(context, null, 0);
	}

	public SeekArc(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public SeekArc(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}
	
	public void getPointXY(int degree, int[] point) {
		int x = (int) (mTranslateX + mArcRadius * Math.cos(degree * Math.PI / 180));
		int y = (int) (mTranslateY + mArcRadius * Math.sin(degree * Math.PI / 180));
		point[0] = x;
		point[1] = y;
	}
	public void setBackgroundColor(int start, int end) {
		mArcStartColor = start;
		mArcEndColor = end;
		LinearGradient arcColor = new LinearGradient(mArcRect.left, mArcRect.top, mArcRect.right, mArcRect.bottom, mArcStartColor, mArcEndColor, Shader.TileMode.CLAMP);    
		mArcPaint.setShader(arcColor);
		postInvalidate();
	}

	@SuppressLint("InlinedApi")
	private void init(Context context, AttributeSet attrs, int defStyle) {
		final Resources res = getResources();
		float density = context.getResources().getDisplayMetrics().density;

		mArcStartColor = res.getColor(R.color.progress_gray);
		mArcEndColor = res.getColor(R.color.progress_gray);
		int progressColor = res.getColor(android.R.color.holo_blue_light);
		int thumbHalfheight = 0;
		int thumbHalfWidth = 0;
		mThumb = res.getDrawable(R.drawable.arcseekbar__control_selector);
		mProgressWidth = (int) (mProgressWidth * density);
		if (attrs != null) {
			final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArcSeekbar, defStyle, 0);
			if (a.hasValue(R.styleable.ArcSeekbar_asb_thumb)) {
				mThumb = a.getDrawable(R.styleable.ArcSeekbar_asb_thumb);
			}
			mShowOnly = a.getBoolean(R.styleable.ArcSeekbar_asb_showOnly, false);
			thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 2;
			thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
			mThumb.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth, thumbHalfheight);

			mMax = a.getInteger(R.styleable.ArcSeekbar_asb_max, mMax);
			mProgress = a.getInteger(R.styleable.ArcSeekbar_asb_progress, mProgress);
			mProgressWidth = (int) a.getDimension(R.styleable.ArcSeekbar_asb_progressWidth, mProgressWidth);
			mArcWidth = (int) a.getDimension(R.styleable.ArcSeekbar_asb_arcWidth, mArcWidth);
			mStartAngle = a.getInt(R.styleable.ArcSeekbar_asb_startAngle, mStartAngle);
			mSweepAngle = a.getInt(R.styleable.ArcSeekbar_asb_sweepAngle, mSweepAngle);
			mRotation = a.getInt(R.styleable.ArcSeekbar_asb_rotation, mRotation);
			mRoundedEdges = a.getBoolean(R.styleable.ArcSeekbar_asb_roundEdges, mRoundedEdges);
			mTouchInside = a.getBoolean(R.styleable.ArcSeekbar_asb_touchInside, mTouchInside);
			mClockwise = a.getBoolean(R.styleable.ArcSeekbar_asb_clockwise, mClockwise);	
			if (a.hasValue(R.styleable.ArcSeekbar_asb_arcStartColor) && a.hasValue(R.styleable.ArcSeekbar_asb_arcEndColor)) {
				mArcStartColor = a.getColor(R.styleable.ArcSeekbar_asb_arcStartColor, mArcStartColor);
				mArcEndColor = a.getColor(R.styleable.ArcSeekbar_asb_arcEndColor, mArcEndColor);
			} else {
				mArcStartColor = a.getColor(R.styleable.ArcSeekbar_asb_arcColor, mArcStartColor);
				mArcEndColor = a.getColor(R.styleable.ArcSeekbar_asb_arcColor, mArcEndColor);
			}
			progressColor = a.getColor(R.styleable.ArcSeekbar_asb_progressColor, progressColor);

			a.recycle();
		}

		mProgress = (mProgress > mMax) ? mMax : mProgress;
		mProgress = (mProgress < 0) ? 0 : mProgress;

		mSweepAngle = (mSweepAngle > 360) ? 360 : mSweepAngle;
		mSweepAngle = (mSweepAngle < 0) ? 0 : mSweepAngle;

		mStartAngle = (mStartAngle > 360) ? 0 : mStartAngle;
		mStartAngle = (mStartAngle < 0) ? 0 : mStartAngle;

		mArcPaint = new Paint();
		mArcPaint.setAntiAlias(true);
		mArcPaint.setStyle(Paint.Style.STROKE);
		mArcPaint.setStrokeWidth(mArcWidth);

		mProgressPaint = new Paint();
		mProgressPaint.setAntiAlias(true);
		mProgressPaint.setStyle(Paint.Style.STROKE);
		mProgressPaint.setStrokeWidth(mProgressWidth);
		mProgressPaint.setColor(progressColor);

		if (mRoundedEdges) {
			mArcPaint.setStrokeCap(Paint.Cap.ROUND);
			mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
		}
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {		
		if(!mClockwise) {
			canvas.scale(-1, 1, mArcRect.centerX(), mArcRect.centerY() );
		}
		
		final int arcStart = mStartAngle + mAngleOffset + mRotation;
		final int arcSweep = mSweepAngle;

		if (mArcPaint.getShader() == null) {
			LinearGradient arcColor = new LinearGradient(mArcRect.left, mArcRect.top, mArcRect.right, mArcRect.bottom, mArcStartColor, mArcEndColor, Shader.TileMode.CLAMP);    
			mArcPaint.setShader(arcColor);
		}
		canvas.drawArc(mArcRect, arcStart, arcSweep, false, mArcPaint);
		canvas.drawArc(mArcRect, arcStart, mProgressSweep, false, mProgressPaint);

		if (!mShowOnly) {
			canvas.translate(mTranslateX -mThumbXPos, mTranslateY -mThumbYPos);
			mThumb.draw(canvas);
		}
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
		final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		final int min = Math.min(width, height);
		float top = 0;
		float left = 0;
		int arcDiameter = 0;

		mTranslateX = (int) (width * 0.5f);
		mTranslateY = (int) (height * 0.5f);
		
		arcDiameter = min - getPaddingLeft();
		mArcRadius = arcDiameter / 2;
		top = height / 2 - (arcDiameter / 2);
		left = width / 2 - (arcDiameter / 2);
		mArcRect.set(left, top, left + arcDiameter, top + arcDiameter);
	
		int arcStart = (int)mProgressSweep + mStartAngle  + mRotation + 90;
		mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(arcStart)));
		mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(arcStart)));
		
		setTouchInSide(mTouchInside);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = true;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			result = updateOnTouch(event, true);
			break;
		case MotionEvent.ACTION_MOVE:
			updateOnTouch(event, false);
			break;
		case MotionEvent.ACTION_UP:
			setPressed(false);
			break;
		case MotionEvent.ACTION_CANCEL:
			setPressed(false);
			break;
		}

		return result;
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (mThumb != null && mThumb.isStateful()) {
			int[] state = getDrawableState();
			mThumb.setState(state);
		}
		invalidate();
	}

	private boolean updateOnTouch(MotionEvent event, boolean moveDown) {
		boolean ignoreTouch = ignoreTouch(event.getX(), event.getY(), moveDown);
		if (ignoreTouch) {
			return false;
		}
		setPressed(true);
		mTouchAngle = getTouchDegrees(event.getX(), event.getY());
		int progress = getProgressForAngle(mTouchAngle);
		if (progress < 0) {
			return false;
		}
		onProgressRefresh(progress, true);
		return true;
	}

	private boolean ignoreTouch(float xPos, float yPos, boolean moveDown) {
		boolean ignore = false;
		float x = xPos - mTranslateX;
		float y = yPos - mTranslateY;

		float touchRadius = (float) Math.sqrt(((x * x) + (y * y)));
		if (moveDown) {
			if (touchRadius < mTouchIgnoreRadius) {
				ignore = true;
			}
		} else {
			if (touchRadius < mArcRadius >> 1) {
				ignore = true;
			}
		}
		return ignore;
	}

	private double getTouchDegrees(float xPos, float yPos) {
		float x = xPos - mTranslateX;
		float y = yPos - mTranslateY;
		x= (mClockwise) ? x : -x;
		double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2) - Math.toRadians(mRotation));
		if (angle < 0) {
			angle = 360 + angle;
		}
		angle -= mStartAngle;
		return angle;
	}

	private int getProgressForAngle(double angle) {
		int touchProgress = (int) Math.round(valuePerDegree() * angle);
		touchProgress = (touchProgress < 0) ? INVALID_PROGRESS_VALUE : touchProgress;
		touchProgress = (touchProgress > mMax) ? INVALID_PROGRESS_VALUE : touchProgress;
		return touchProgress;
	}

	private float valuePerDegree() {
		return (float) mMax / mSweepAngle;
	}

	private void onProgressRefresh(int progress, boolean fromUser) {
		updateProgress(progress, fromUser);
	}

	private void updateThumbPosition() {
		int thumbAngle = (int) (mStartAngle + mProgressSweep + mRotation + 90);
		mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(thumbAngle)));
		mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(thumbAngle)));
	}
	
	private void updateProgress(int progress, boolean fromUser) {
		if (progress == INVALID_PROGRESS_VALUE) {
			return;
		}
		if (mOnSeekArcChangeListener != null) {
			mOnSeekArcChangeListener.onProgressChanged(this, progress, fromUser);
		}

		progress = (progress > mMax) ? mMax : progress;
		progress = (mProgress < 0) ? 0 : progress;

		mProgress = progress;
		mProgressSweep = (float) progress / mMax * mSweepAngle;

		updateThumbPosition();

		invalidate();
	}

	public void setOnSeekArcChangeListener(OnSeekArcChangeListener l) {
		mOnSeekArcChangeListener = l;
	}

	public void setProgress(int progress) {
		updateProgress(progress, false);
	}

	public int getProgressWidth() {
		return mProgressWidth;
	}

	public void setProgressWidth(int mProgressWidth) {
		this.mProgressWidth = mProgressWidth;
		mProgressPaint.setStrokeWidth(mProgressWidth);
	}
	
	public int getArcWidth() {
		return mArcWidth;
	}

	public void setArcWidth(int mArcWidth) {
		this.mArcWidth = mArcWidth;
		mArcPaint.setStrokeWidth(mArcWidth);
	}
	public int getArcRotation() {
		return mRotation;
	}

	public void setArcRotation(int mRotation) {
		this.mRotation = mRotation;
		updateThumbPosition();
	}

	public int getStartAngle() {
		return mStartAngle;
	}

	public void setStartAngle(int mStartAngle) {
		this.mStartAngle = mStartAngle;
		updateThumbPosition();
	}

	public int getSweepAngle() {
		return mSweepAngle;
	}

	public void setSweepAngle(int mSweepAngle) {
		this.mSweepAngle = mSweepAngle;
		updateThumbPosition();
	}
	
	public void setRoundedEdges(boolean isEnabled) {
		mRoundedEdges = isEnabled;
		if (mRoundedEdges) {
			mArcPaint.setStrokeCap(Paint.Cap.ROUND);
			mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
		} else {
			mArcPaint.setStrokeCap(Paint.Cap.SQUARE);
			mProgressPaint.setStrokeCap(Paint.Cap.SQUARE);
		}
	}
	
	public void setTouchInSide(boolean isEnabled) {
		int thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 2;
		int thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
		mTouchInside = isEnabled;
		if (mTouchInside) {
			mTouchIgnoreRadius = (float) mArcRadius / 4;
		} else {
			mTouchIgnoreRadius = mArcRadius - Math.min(thumbHalfWidth, thumbHalfheight) * 2;
		}
	}
	
	public void setClockwise(boolean isClockwise) {
		mClockwise = isClockwise;
	}

	private static int INVALID_PROGRESS_VALUE = -1;
	private final int mAngleOffset = -90;
	private Drawable mThumb;
	private boolean mShowOnly = false;
	private int mMax = 100;
	private int mProgress = 0;
	private int mProgressWidth = 4;
	private int mArcWidth = 2;
	private int mStartAngle = 0;
	private int mSweepAngle = 360;
	private int mRotation = 0;
	private boolean mRoundedEdges = false;
	private boolean mTouchInside = true;
	private boolean mClockwise = true;

	private int mArcRadius = 0;
	private float mProgressSweep = 0;
	private RectF mArcRect = new RectF();
	private Paint mArcPaint;
	private Paint mProgressPaint;
	private int mArcStartColor;
	private int mArcEndColor;
	private int mTranslateX;
	private int mTranslateY;
	private int mThumbXPos;
	private int mThumbYPos;
	private double mTouchAngle;
	private float mTouchIgnoreRadius;
	private OnSeekArcChangeListener mOnSeekArcChangeListener;
}
