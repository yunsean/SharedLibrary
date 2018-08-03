package com.dylan.uiparts.imageview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.dylan.common.data.StrUtil;
import com.dylan.common.utils.AsyncImageLoader;
import com.dylan.common.utils.Utility;
import com.dylan.uiparts.R;
import com.dylan.uiparts.imageview.NetImageView.OnLoadListener;
import com.dylan.uiparts.viewpager.VerticalViewPager;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressLint("ClickableViewAccessibility")
public class VerticalNetImageBanner extends LinearLayout implements OnLoadListener {
	
	private ScaleType mImageScaleType = ScaleType.CENTER_CROP;
	private int mFixedHeight = 0;
	private boolean mAutoScroll = true;
	private boolean mShowTitle = true;
	private boolean mShowDot = true;
	private boolean mDisableFlip = false;
	private Drawable mTitleBackground = null;
	private Integer mTitleBackgroundColor = null;
	private Integer mTitleTextColor = null;
	public VerticalNetImageBanner(Context context) {
		this(context, null);
	}
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public VerticalNetImageBanner(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.image_banner_vertical, this);
		mContainer = (RelativeLayout) findViewById(R.id.container);
		mViewPager = (VerticalViewPager) findViewById(R.id.viewPager);
		mTextDesc = (TextView) findViewById(R.id.textDesc);
		mViewGroup = (ViewGroup) findViewById(R.id.viewGroup);
		mViewHandler = new MyHandler(mViewPager);
		ViewTreeObserver vto2 = getViewTreeObserver();    
		vto2.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (getWidth() < 1)return;
				if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.JELLY_BEAN) {
					removeOnGlobalLayoutListener();
				} else {
					getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
				for (int i = 0; i < mItems.size(); i++) {
					ImageItem item = mItems.get(i);
					if (!(item.image instanceof NetImageView)) {
						Drawable drawable = item.image.getDrawable();
						Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
						onFinish(item.image, true, bitmap);
						break;
					}
				}
				if (mFixedHeight == 0 && !mSizeChanged && mImageHeight > 0 && mImageWidth > 0) {
					mSizeChanged = true;
					int height = getLayoutParams() != null ? getLayoutParams().height : 0;
					if (height == LayoutParams.MATCH_PARENT) {
						mImageHeight = getHeight();
					} else {
						mImageHeight = getWidth() * mImageHeight / mImageWidth;
					}
					if (mImageHeight > getLayoutParams().height) {
						ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) mContainer.getLayoutParams();
						lp.height = mImageHeight;
						mContainer.setLayoutParams(lp);
						Log.e("uu163", "set height 1: " + mImageHeight);
					}
				}
			}
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			private void removeOnGlobalLayoutListener() {
				getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});
		mViewPager.setOnPageChangeListener(new GuidePageChangeListener());
		mViewPager.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mDragged = false;
				case MotionEvent.ACTION_MOVE:
					mIsContinue = false;
					break;
				case MotionEvent.ACTION_UP:
					if (!mDragged && mListener != null) {
						int index = mViewPager.getCurrentItem();
						Object tag = mItems.get(index).tag;
						mListener.onClick(VerticalNetImageBanner.this, index, tag);
					}
				case MotionEvent.ACTION_CANCEL:
				default:
					mIsContinue = true;
					break;
				}
				return false;
			}
		});
		try {
			Field mField = VerticalViewPager.class.getDeclaredField("mScroller");
			mField.setAccessible(true);
			mScroller = new FixedSpeedScroller(mViewPager.getContext(), new AccelerateInterpolator());
			mField.set(mViewPager, mScroller);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mAdapter = new PagerAdapter() {
			@Override
			public boolean isViewFromObject(View view, Object object) {
				if (object instanceof ImageItem) {
					return ((ImageItem)object).image == view;
				}
				return false;
			}
			@Override
			public int getCount() {
				return mItems.size();
			}
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				container.addView(mItems.get(position).image, 0);
				return mItems.get(position);
			}
			@Override
			public void destroyItem(ViewGroup container, int position, Object object) {
				((VerticalViewPager)container).removeView(((ImageItem)object).image);
			}
			@Override  
			public int getItemPosition(Object object) {  
			    return POSITION_NONE;  
			}
		};
		mViewPager.setAdapter(mAdapter);
		if (!mShowDot)mViewGroup.setVisibility(View.GONE);
		if (!mShowTitle)mTextDesc.setVisibility(View.GONE);
		if (mDisableFlip)mViewPager.setCanScroll(false);
		if (mTitleBackground != null) {
			if (Utility.isJellyBeanOrLater()) {
				mTextDesc.setBackground(mTitleBackground);
			} else {
				mTextDesc.setBackgroundDrawable(mTitleBackground);
			}
		} else if (mTitleBackgroundColor != null) {
			mTextDesc.setBackgroundColor(mTitleBackgroundColor.intValue());
		}
		if (mTitleTextColor != null) {
			mTextDesc.setTextColor(mTitleTextColor.intValue());
		}
	}
	public void setShowDot(boolean show) {
		mViewGroup.setVisibility(show ? View.VISIBLE : View.GONE);
	}
	public void setShowTitle(boolean show) {
		mTextDesc.setVisibility(show ? View.VISIBLE : View.GONE);
	}
	public void setDisableFlip(boolean disable) {
		mViewPager.setCanScroll(!disable);
	}
	public void setImageScaleType(ScaleType scaleType) {
		mImageScaleType = scaleType;
		for (ImageItem item : mItems) {
			if (item != null && item.image != null) {
				item.image.setScaleType(mImageScaleType);
			}
		}
	}
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public void setTitleBackground(Drawable drawable) {
		if (Utility.isJellyBeanOrLater()) {
			mTextDesc.setBackground(drawable);
		} else {
			mTextDesc.setBackgroundDrawable(drawable);
		}
	}
	public void setTitleBackgroundColor(int color) {
		mTextDesc.setBackgroundColor(color);
	}
	public void setTitleTextColor(int color) {
		mTextDesc.setTextColor(color);
	}
	public void setAutoScroll(boolean auto) {
		mAutoScroll = auto;
		if (mThread == null && mAutoScroll) {
			mThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (!mWillExit && !Thread.currentThread().isInterrupted()) {
						if (mIsContinue) {
							mViewHandler.sendEmptyMessage(what.get());
							whatOption();
						} else {
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
							}
						}
					}
					Log.i("dylan", "NetImageBanner thread exit.");
				}

			});
			mThread.start();
		} else if (mThread != null) {
			mThread.interrupt();
			mThread = null;
		}
	}
	public void showNext(boolean autoRewind) {
		if (mImageViews == null)return;
		what.incrementAndGet();
		if (what.get() > mImageViews.length - 1) {
			if (autoRewind) {
				what.getAndAdd(-1 * mImageViews.length);
			} else {
				return;
			}
		}
		mViewHandler.sendEmptyMessage(what.get());
	}
	public void showNext() {
		showNext(false);
	}
	public void showPrevious() {
		if (mImageViews == null)return;
		what.decrementAndGet();
		if (what.get() < 0)return;
		mViewHandler.sendEmptyMessage(what.get());
	}
	
	public void setFixedHeight(int height) {
		mFixedHeight = height;
		for (int i = 0; i < mItems.size(); i++) {
			ImageItem item = mItems.get(i);
			item.image.setScaleType(ScaleType.CENTER_CROP);
		}
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) mContainer.getLayoutParams();
		lp.height = mFixedHeight;
	}
	
	private void init(Context context, AttributeSet attrs) {
		if (attrs != null) {
			final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NetImageBanner, 0, 0);
			mDefaultDrawable = a.getDrawable(R.styleable.NetImageBanner_nib_defalutItemImage);
			mLoadingDrawable = a.getDrawable(R.styleable.NetImageBanner_nib_loadingItemImage);
			mSizeAspect = a.getFloat(R.styleable.NetImageBanner_nib_bannerAspect, 0.f);
			mDisableFlip = a.getBoolean(R.styleable.NetImageBanner_nib_forbidFlip, false);
			mAutoScroll = !a.getBoolean(R.styleable.NetImageBanner_nib_forbidTimer, false);
			mShowDot = !a.getBoolean(R.styleable.NetImageBanner_nib_hideDot, false);
			mShowTitle = !a.getBoolean(R.styleable.NetImageBanner_nib_hideTitle, false);
			mTitleBackground = a.getDrawable(R.styleable.NetImageBanner_nib_titleBackground);
			if (a.hasValue(R.styleable.NetImageBanner_nib_titleBackgroundColor)) {
				mTitleBackgroundColor = a.getColor(R.styleable.NetImageBanner_nib_titleBackgroundColor, 0);
			} else {
				mTitleBackgroundColor = null;
			}
			if (a.hasValue(R.styleable.NetImageBanner_nib_titleTextColor)) {
				mTitleTextColor = a.getColor(R.styleable.NetImageBanner_nib_titleTextColor, 0);
			} else {
				mTitleTextColor = null;
			}
			a.recycle();
		}
		getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.JELLY_BEAN) {
					removeOnGlobalLayoutListener();
				} else {
					getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
				if (mDefaultDrawable != null) {
					Bitmap bitmap = ((BitmapDrawable)mDefaultDrawable).getBitmap();
					onImageChanged(bitmap);
				} else if (mLoadingDrawable != null) {
					Bitmap bitmap = ((BitmapDrawable)mDefaultDrawable).getBitmap();
					onImageChanged(bitmap);
				}
			}
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			private void removeOnGlobalLayoutListener() {
				getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});
	}
	
	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mWillExit = true;
		mThread = null;
	}
	
	public void addItem(String imageUrl, boolean useCache, AsyncImageLoader loader, String title, Object tag) {
		if (mItems == null)return;
		NetImageView image = new NetImageView(getContext());
		image.setListener(this);
		if (mLoadingDrawable != null) image.setLoading(mLoadingDrawable);
		if (mDefaultDrawable != null) image.setDefault(mDefaultDrawable);
		image.setImage(imageUrl, useCache, loader);
		image.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		image.setScaleType(mImageScaleType);
		mItems.add(new ImageItem(image, title, tag));
	}
	public void addItem(String imageUrl, boolean useCache, AsyncImageLoader loader, String title) {
		addItem(imageUrl, useCache, loader, title, null);
	}
	public void addItem(String imageUrl, String title, Object tag) {
		addItem(imageUrl, true, null, title, tag);
	}
	public void addItem(String imageUrl, String title) {
		addItem(imageUrl, true, null, title, null);
	}
	public void addItem(Drawable image, String title, Object tag) {
		if (image == null)return;
		ImageView iv = new ImageView(getContext());
		iv.setImageDrawable(image);
		iv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		iv.setScaleType(mImageScaleType);
		mItems.add(new ImageItem(iv, title, tag));
	}
	public void addItem(Drawable image, String title) {
		addItem(image, title, null);
	}
	public void addItem(int resId, String title, Object tag) {
		if (resId == 0)return;
		ImageView iv = new ImageView(getContext());
		iv.setImageResource(resId);
		iv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		iv.setScaleType(mImageScaleType);
		mItems.add(new ImageItem(iv, title, tag));
	}
	public void addItem(int resId, String title) {
		addItem(resId, title, null);
	}
	public void cleanAll() {
		mViewPager.removeAllViewsInLayout();
		mViewGroup.removeAllViews();
		mImageViews = null;
		mItems.clear();
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
		mSizeChanged = false;
	}
	public void setOnItemClickListener(OnItemClickListener listener) {
		mListener = listener;
	}
	public int getCount() {
		return mItems.size();
	}

	private Drawable mDefaultDrawable = null;
	private Drawable mLoadingDrawable = null;
	private float mSizeAspect = .0f;
	private RelativeLayout mContainer = null;
	private ImageView[] mImageViews = null;
	private VerticalViewPager mViewPager = null;
	private TextView mTextDesc = null;
	private ViewGroup mViewGroup = null;
	private boolean mIsContinue = true;
	private AtomicInteger what = new AtomicInteger(0);
	private FixedSpeedScroller mScroller = null;
	private boolean mDragged = false;
	private Boolean mSizeChanged = false;
	private boolean mWillExit = false;
	private class ImageItem {
		public ImageItem(ImageView i, String t, Object g) {
			image = i;
			title = t;
			tag = g;
		}
		public ImageView image;
		public String title;
		public Object tag;
	}
	private ArrayList<ImageItem> mItems = new ArrayList<ImageItem>();
	private OnItemClickListener mListener = null;
	@Override
	public void onFinish(NetImageView view, boolean result, Bitmap bitmap) {
		onFinish((ImageView)view, result, bitmap);
	}
	private int mImageWidth = 0;
	private int mImageHeight = 0;
	private void onFinish(ImageView view, boolean result, Bitmap bitmap) {
		if (!result)return;
		if (mFixedHeight > 0)return;
		if (mSizeChanged)return;
		if (bitmap == null)return;
		synchronized (mSizeChanged) {
			if (mSizeChanged)return;
			onImageChanged(bitmap);
		}
	}
	private void onImageChanged(Bitmap bitmap) {
		mImageHeight = bitmap.getHeight();
		mImageWidth = bitmap.getWidth();
		if (mImageWidth < 1 || mImageHeight < 1)return;
		if (getWidth() < 1)return;
		mSizeChanged = true;
		if (mSizeAspect > 0.01) {
			mImageHeight = (int) (getWidth() * mSizeAspect);
		} else {
			int height = getLayoutParams() != null ? getLayoutParams().height : 0;
			if (height == LayoutParams.MATCH_PARENT) {
				mImageHeight = getHeight();
			} else {
				mImageHeight = getWidth() * mImageHeight / mImageWidth;
			}
		}
		if (mImageHeight > getLayoutParams().height) {
			ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) mContainer.getLayoutParams();
			lp.height = mImageHeight;
			mContainer.setLayoutParams(lp);
		}
	}

	private PagerAdapter mAdapter = null;
	private Thread mThread = null;
	public void updateLayout() {
		if (mItems == null)return;
		mImageViews = new ImageView[mItems.size()];
		for (int i = 0; i < mItems.size(); i++) {
			ImageView iv = new ImageView(getContext());
			iv.setLayoutParams(new LayoutParams(Utility.dip2px(getContext(), 15), Utility.dip2px(getContext(), 15)));
			iv.setPadding(3, 3, 3, 3);
			mImageViews[i] = iv;
			if (i == 0) {
				mImageViews[i].setImageResource(R.drawable.banner_dot_focus);
			} else {
				mImageViews[i].setImageResource(R.drawable.banner_dot_normal);
			}
			mViewGroup.addView(mImageViews[i]);
		}
		mAdapter.notifyDataSetChanged();
		if (mItems.size() > 0) {
			if (mShowTitle && !StrUtil.isBlank(mItems.get(0).title)) {
				mTextDesc.setText(mItems.get(0).title);
				mTextDesc.setVisibility(View.VISIBLE);
			} else {
				mTextDesc.setVisibility(View.GONE);
			}
		}
		setAutoScroll(mAutoScroll);
	}
	private void whatOption() {
		what.incrementAndGet();
		if (mImageViews == null)return;
		if (what.get() > mImageViews.length - 1) {
			what.getAndAdd(-1 * mImageViews.length);
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
	}

	private Handler mViewHandler = null;
	private static class MyHandler extends Handler {
		WeakReference<VerticalViewPager> mViewPager = null;
        MyHandler(VerticalViewPager pager) {
	        	mViewPager = new WeakReference<VerticalViewPager>(pager);
        }
        @Override
		public void handleMessage(Message msg) {
	        	if (mViewPager.get() != null) {
	        		mViewPager.get().setCurrentItem(msg.what);
	        	}
			super.handleMessage(msg);
		}
	}
	private final class GuidePageChangeListener implements ViewPager.OnPageChangeListener {
		@Override
		public void onPageScrollStateChanged(int arg0) {
			mDragged = true;
		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
		@Override
		public void onPageSelected(int arg0) {
			what.getAndSet(arg0);
			for (int i = 0; i < mImageViews.length; i++) {
				mImageViews[arg0].setImageResource(R.drawable.banner_dot_focus);
				if (arg0 != i) {
					mImageViews[i].setImageResource(R.drawable.banner_dot_normal);
				}
			}
			if (mShowTitle && !StrUtil.isBlank(mItems.get(0).title)) {
				mTextDesc.setText(mItems.get(arg0).title);
				mTextDesc.setVisibility(View.VISIBLE);
			} else {
				mTextDesc.setVisibility(View.GONE);
			}
		}
	}

	public class FixedSpeedScroller extends Scroller {
		private int mDuration = 800;
		public FixedSpeedScroller(Context context) {
			super(context);
		}
		public FixedSpeedScroller(Context context, Interpolator interpolator) {
			super(context, interpolator);
		}
		@Override
		public void startScroll(int startX, int startY, int dx, int dy, int duration) {
			super.startScroll(startX, startY, dx, dy, mDuration);
		}
		@Override
		public void startScroll(int startX, int startY, int dx, int dy) {
			super.startScroll(startX, startY, dx, dy, mDuration);
		}
		public void setmDuration(int time) {
			mDuration = time;
		}
		public int getmDuration() {
			return mDuration;
		}
	}
	
	public interface OnItemClickListener {
		void onClick(VerticalNetImageBanner banner, int position, Object tag);
	}
}
