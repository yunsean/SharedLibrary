package com.dylan.uiparts.imageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.dylan.common.utils.AsyncImageLoader;
import com.dylan.common.utils.AsyncImageLoader.ImageCallback;
import com.dylan.common.utils.Utility;
import com.dylan.uiparts.R;

public class NetImageView extends ImageView {

	private boolean mRound = false;
	private Path mPath = null;
	private PaintFlagsDrawFilter mFilter = null;
	private Paint mPaint;
	private float mCircleWidth = 0;
	private int mCircleColor = 0xffaaaaaa;

	public NetImageView(Context context) {
		super(context);
		init(context, null, 0);
	}
	public NetImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}
	public NetImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	public void setRound(boolean round) {
		mRound = round;
		if (mRound) {
			if (Utility.isHoneycombOrLater()) {
				setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}
		}
	}
	public void setCircle(int color, int width) {
		mCircleColor = color;
		mCircleWidth = width;
		if (mCircleWidth > 0) {
			if (Utility.isHoneycombOrLater()) {
				setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}
		}
	}
	public void setAspect(int aspect) {
		mSizeAspect = aspect;
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		if (attrs != null) {
			final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NetImageView, defStyle, 0);
			mDefaultDrawable = a.getDrawable(R.styleable.NetImageView_niv_defalutImage);
			mLoadingDrawable = a.getDrawable(R.styleable.NetImageView_niv_loadingImage);
			if (mLoadingDrawable == null) {
				int src_resource = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "src", 0);
				if (src_resource != 0)
					mDefaultDrawable = getResources().getDrawable(src_resource);
			}
			if (mLoadingDrawable == null && mDefaultDrawable != null) {
				mLoadingDrawable = mDefaultDrawable;
			}
			mRound = a.getBoolean(R.styleable.NetImageView_niv_roundedImage, false);
			mCircleColor = a.getColor(R.styleable.NetImageView_niv_circleColor, 0xffaaaaaa);
			mCircleWidth = a.getDimension(R.styleable.NetImageView_niv_circleWidth, 0);
			mSizeAspect = a.getFloat(R.styleable.NetImageView_niv_imageAspect, .0f);
			a.recycle();
		}
		if (mRound || mCircleWidth > 0) {
			if (Utility.isHoneycombOrLater()) {
				setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}
		}
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.STROKE);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mRound || mCircleWidth > 0) {
			if (mPath == null) {
				mPath = new Path();
				int w = this.getWidth();
				int h = this.getHeight();
				int radius = w > h ? h : w;
				mPath.addCircle(w / 2, h / 2, radius / 2 - 1, Path.Direction.CW);
				mFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
			}
			canvas.clipPath(mPath);
			canvas.setDrawFilter(mFilter);
		}
		super.onDraw(canvas);

		if (mCircleWidth > 0.1) {
			int innerCircle = (int) (getWidth() - mCircleWidth) >> 1;
			mPaint.setColor(mCircleColor);
			mPaint.setStrokeWidth(mCircleWidth);
			canvas.drawCircle(getWidth() >> 1, getHeight() >> 1, innerCircle, mPaint);
		}
	}

	private Drawable mDefaultDrawable = null;
	private int mDefaultResId = 0;
	private Drawable mLoadingDrawable = null;
	private int mLoadingResId = 0;
	private OnLoadListener mListener = null;
	private boolean mFitHeight = false;
	private String mImageUrl = null;
	private float mSizeAspect = 0.f;

	public void setDefault(Drawable drawable) {
		mDefaultDrawable = drawable;
		mDefaultResId = 0;
	}
	public void setDefault(int resId) {
		mDefaultDrawable = null;
		mDefaultResId = resId;
	}
	public void setLoading(Drawable drawable) {
		mLoadingDrawable = drawable;
		mLoadingResId = 0;
	}
	public void setLoading(int resId) {
		mLoadingResId = resId;
		mLoadingDrawable = null;
	}

	public void setListener(OnLoadListener listener) {
		mListener = listener;
	}

	public void setFitHeight(boolean fit) {
		mFitHeight = fit;
	}

	public void setImage(String url) {
		setImage(url, true, null);
	}

	public void setImage(String url, AsyncImageLoader loader) {
		setImage(url, true, loader);
	}

	public void setImage(final String url, boolean useCache, AsyncImageLoader loader) {
		mImageUrl = url;
		if (loader == null) {
			loader = AsyncImageLoader.theDefault();
		}
		if (url == null || url.length() < 1 || loader == null) {
			if (mDefaultDrawable != null) {
				setImageDrawable(mDefaultDrawable);
			} else if (mDefaultResId != 0) {
				setImageResource(mDefaultResId);
			}
		} else {
			if (mLoadingDrawable != null) {
				setImageDrawable(mLoadingDrawable);
			} else if (mLoadingResId != 0) {
				setImageResource(mLoadingResId);
			}
			Bitmap image = loader.loadCache(getContext(), url, new ImageCallback() {
				@Override
				public void onError(Exception e, String url, Object userdata) {
					if (mListener != null)
						mListener.onFinish(NetImageView.this, true, null);
					if (mDefaultDrawable != null) {
						NetImageView.this.setImageDrawable(mDefaultDrawable);
					} else if (mDefaultResId != 0) {
						NetImageView.this.setImageResource(mDefaultResId);
					}
				}

				@Override
				public void onLoaded(Bitmap bitmap, String url, Object userdata) {
					if (bitmap == null)
						return;
					if (!url.equals(mImageUrl))
						return;
					NetImageView.this.setImageBitmap(bitmap);
					if (mListener != null)
						mListener.onFinish(NetImageView.this, true, bitmap);
				}
			});
			if (image != null) {
				NetImageView.this.setImageBitmap(image);
				if (mListener != null)
					mListener.onFinish(NetImageView.this, true, image);
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mSizeAspect > 0.01f) {
			if (mFitHeight) {
				int height = MeasureSpec.getSize(heightMeasureSpec);
				int width = (int) (height / mSizeAspect);
				this.setMeasuredDimension(width, height);
			} else {
				int width = MeasureSpec.getSize(widthMeasureSpec);
				int height = (int) (width * mSizeAspect);
				this.setMeasuredDimension(width, height);
			}
		} else if (getScaleType() != ImageView.ScaleType.FIT_XY) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		} else if (mFitHeight) {
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = MeasureSpec.getSize(heightMeasureSpec);
			if (getDrawable() != null) {
				Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
				if (bitmap != null) {
					width = height * bitmap.getWidth() / bitmap.getHeight();
				}
			}
			this.setMeasuredDimension(width, height);
		} else {
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = MeasureSpec.getSize(heightMeasureSpec);
			if (getDrawable() != null) {
				Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
				if (bitmap != null) {
					height = width * bitmap.getHeight() / bitmap.getWidth();
				}
			}
			this.setMeasuredDimension(width, height);
		}
	}

	public interface OnLoadListener {
		void onFinish(NetImageView view, boolean result, Bitmap bitmap);
	}
}
