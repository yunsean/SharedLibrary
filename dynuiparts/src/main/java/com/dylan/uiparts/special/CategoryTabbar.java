package com.dylan.uiparts.special;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dylan.common.utils.Utility;
import com.dylan.uiparts.R;
import com.dylan.uiparts.drawable.TextDrawable;

public class CategoryTabbar extends HorizontalScrollView {
	private LayoutInflater mLayoutInflater = null;
	private final PageListener mPageListener = new PageListener();
	private ViewPager mViewPager = null;
	private LinearLayout mTabsContainer = null;
	private int mTabCount = 0;
	
	private int mCurrentPosition = 0;
	private float mCurrentPositionOffset = 0f;
	
	private LinearLayout.LayoutParams mDefaultTabLayoutParams;
	private int mScrollOffset = 0;
	private int mLastScrollX = 0;

	private float mTextSize = 16.0f;
	private int mNormalTextColor = 0;
	private int mSelectedTextColor = 0;
	private Drawable mLeftEdge = null;
    private Drawable mRightEdge = null;
	private Drawable mIndicator = null;
	private Rect mIndicatorRect = null;
	private int mPaddingLeft = 0;
	private int mPaddingRight = 0;
	private TextDrawable[] mTextDrawables = null;
	
	public CategoryTabbar(Context context) {
		this(context, null);
	}
	public CategoryTabbar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public CategoryTabbar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mLayoutInflater = LayoutInflater.from(context);
		mTextDrawables = new TextDrawable[3];
		int i = 0;
		while (i < mTextDrawables.length) {
			mTextDrawables[i] = new TextDrawable(getContext());
            i++;
        }		
		mIndicatorRect = new Rect();		
		setFillViewport(true);
		setWillNotDraw(false);
		mTabsContainer = new LinearLayout(context);
		mTabsContainer.setOrientation(LinearLayout.HORIZONTAL);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mTabsContainer.setLayoutParams(lp);
		mTabsContainer.setGravity(Gravity.CENTER);
		addView(mTabsContainer);		
		DisplayMetrics dm = getResources().getDisplayMetrics();
		mScrollOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mScrollOffset, dm);		
		mDefaultTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);

		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CategoryTabbar, defStyle, 0);
		if (a.hasValue(R.styleable.CategoryTabbar_ctb_background_selected)) {
			mIndicator = a.getDrawable(R.styleable.CategoryTabbar_ctb_background_selected);
		} else {
			mIndicator = getResources().getDrawable(R.drawable.category_tabbar_indicator);
		}
		mPaddingLeft = (int) a.getDimension(R.styleable.CategoryTabbar_ctb_padding_left, 0);
		mPaddingRight = (int) a.getDimension(R.styleable.CategoryTabbar_ctb_padding_right, 0);
		if (a.hasValue(R.styleable.CategoryTabbar_ctb_left_edge)) {
			mLeftEdge = a.getDrawable(R.styleable.CategoryTabbar_ctb_left_edge);
		} else {
			mLeftEdge = getResources().getDrawable(R.drawable.category_tabbar_left_edge);
		}
		if (a.hasValue(R.styleable.CategoryTabbar_ctb_right_edge)) {
			mRightEdge = a.getDrawable(R.styleable.CategoryTabbar_ctb_right_edge);
		} else {
			mRightEdge = getResources().getDrawable(R.drawable.category_tabbar_right_edge);
		}
		mNormalTextColor = a.getColor(R.styleable.CategoryTabbar_ctb_textcolor_normal, 0xff333333);
		mSelectedTextColor = a.getColor(R.styleable.CategoryTabbar_ctb_textcolor_selected, 0xffffffff);
		mTextSize = a.getDimension(R.styleable.CategoryTabbar_ctb_textsize, Utility.sp2px(context, 14.0f));
		a.recycle();
	}

	public void setViewPager(ViewPager mViewPager) {
		this.mViewPager = mViewPager;
		if (mViewPager.getAdapter() == null) {
			throw new IllegalStateException("ViewPager does not have adapter instance.");
		}
		mViewPager.addOnPageChangeListener(mPageListener);
		notifyDataSetChanged();
	}
	public void notifyDataSetChanged() {
		mTabsContainer.removeAllViews();
		mDefaultTabLayoutParams.width = LayoutParams.WRAP_CONTENT;
		mTabCount = mViewPager.getAdapter().getCount();
		for (int i = 0; i < mTabCount; i++) {
			addTab(i, mViewPager.getAdapter().getPageTitle(i).toString());
		}
		adjustWidth();
		mTabsContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				if (mTabsContainer.getWidth() == 0) return;
				if (Utility.isJellyBeanOrLater()) mTabsContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				else mTabsContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				adjustWidth();
			}
		});
	}

	private void adjustWidth() {
		int totalWidth = 0;
		int tabCount = mTabsContainer.getChildCount();
		if (tabCount < 1) return;
		for (int i = 0; i < tabCount; i++) {
			totalWidth += mTabsContainer.getChildAt(i).getWidth();
		}
		int containerWidth = getWidth();
		if (mLeftEdge != null) containerWidth -= mLeftEdge.getIntrinsicWidth();
		if (mRightEdge != null) containerWidth -= mRightEdge.getIntrinsicWidth();
		containerWidth -= getPaddingLeft() + getPaddingRight();
		if (totalWidth < containerWidth) {
			mDefaultTabLayoutParams.width = containerWidth / tabCount;
			for (int i = 0; i < tabCount; i++) {
				mTabsContainer.getChildAt(i).setLayoutParams(mDefaultTabLayoutParams);
				mTabsContainer.getChildAt(i).requestLayout();
			}
		}
	}

	public void addTab(final int position, String title) {
		ViewGroup tab = (ViewGroup)mLayoutInflater.inflate(R.layout.category_tabbar_item, this, false);
		TextView text = (TextView)tab.findViewById(R.id.category_tabbar_text);
		text.setText(title);
		text.setGravity(Gravity.CENTER);
		text.setSingleLine();
		text.setFocusable(true);
		text.setPadding(mPaddingLeft, 0, mPaddingRight, 0);
		text.setTextColor(mNormalTextColor);
		text.setTextSize(0, mTextSize);
		tab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(position);
			}
		});
		mTabsContainer.addView(tab, position, mDefaultTabLayoutParams);
	}
	public void setSelectItem(int index) {
		mCurrentPosition = index;
		scrollToChild(index, 0);
		invalidate();
		mViewPager.setCurrentItem(index);
	}

	private void calculateIndicatorRect(Rect rect) {
		ViewGroup currentTab = (ViewGroup)mTabsContainer.getChildAt(mCurrentPosition);
		if (currentTab == null)return;
		TextView text = (TextView) currentTab.findViewById(R.id.category_tabbar_text);		
		float left = currentTab.getLeft();
        float right = currentTab.getWidth() + left;
        if (mCurrentPositionOffset > 0f && mCurrentPosition < mTabCount - 1) {
        	ViewGroup nextTab = (ViewGroup)mTabsContainer.getChildAt(mCurrentPosition + 1);
			TextView next_text = (TextView) nextTab.findViewById(R.id.category_tabbar_text);			
			float next_left = (float) (nextTab.getLeft() + next_text.getLeft());
			left = left * (1.0f - mCurrentPositionOffset) + next_left * mCurrentPositionOffset;
			right = right * (1.0f - mCurrentPositionOffset) + mCurrentPositionOffset * (((float) next_text.getWidth()) + next_left);
        }
        rect.set(((int) left) + getPaddingLeft(), getPaddingTop() + currentTab.getTop(), ((int) right) + getPaddingLeft(), currentTab.getTop() + getPaddingTop() + currentTab.getHeight());
	}
	
	private int getScrollRange() {
        return getChildCount() > 0 ? Math.max(0, getChildAt(0).getWidth() - getWidth() + getPaddingLeft() + getPaddingRight()) : 0;
    }	
	private void scrollToChild(int position, int offset) {
		if (mTabCount == 0) {
			return;
		}		
        calculateIndicatorRect(mIndicatorRect);
		int newScrollX = mLastScrollX;
		if (mIndicatorRect.left < getScrollX() + mScrollOffset) {
            newScrollX = mIndicatorRect.left - mScrollOffset;
        } else if (mIndicatorRect.right > getScrollX() + getWidth() - mScrollOffset) {
            newScrollX = mIndicatorRect.right - getWidth() + mScrollOffset;
        }
		if (newScrollX != mLastScrollX) {
			mLastScrollX = newScrollX;
			scrollTo(newScrollX, 0);
		}
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);		
        calculateIndicatorRect(mIndicatorRect);        
		if(mIndicator != null) {
			mIndicator.setBounds(mIndicatorRect);
			mIndicator.draw(canvas);
		}
		
        int i = 0;
        while (i < mTabsContainer.getChildCount()) {
            if (i < mCurrentPosition - 1 || i > mCurrentPosition + 1) {
                i++;
            } else {
            	ViewGroup tab = (ViewGroup)mTabsContainer.getChildAt(i);
            	TextView text = (TextView) tab.findViewById(R.id.category_tabbar_text);
                if (text != null) {
                    TextDrawable textDrawable = mTextDrawables[i - mCurrentPosition + 1];
                    int save = canvas.save();
                    calculateIndicatorRect(mIndicatorRect);
                    canvas.clipRect(mIndicatorRect);
                    textDrawable.setText(text.getText());
                    textDrawable.setTextSize(0, text.getTextSize());
                    textDrawable.setTextColor(mSelectedTextColor);
                    textDrawable.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    int left = tab.getLeft() + text.getLeft() + (text.getWidth() - textDrawable.getIntrinsicWidth()) / 2 + getPaddingLeft();
                    int top = tab.getTop() + text.getTop() + (text.getHeight() - textDrawable.getIntrinsicHeight()) / 2 + getPaddingTop();
                    textDrawable.setBounds(left, top, textDrawable.getIntrinsicWidth() + left, textDrawable.getIntrinsicHeight() + top);
                    textDrawable.draw(canvas);
                    canvas.restoreToCount(save);
                }
                i++;
            }
        }
        
        i = canvas.save();
        int top = getScrollX();
        int height = getHeight();
        int width = getWidth();
        canvas.translate((float) top, 0.0f);
        if (mLeftEdge == null || top <= 0) {
            if (mRightEdge == null || top >= getScrollRange()) {
                canvas.restoreToCount(i);
            }
            mRightEdge.setBounds(width - mRightEdge.getIntrinsicWidth(), 0, width, height);
            mRightEdge.draw(canvas);
            canvas.restoreToCount(i);
        }
        mLeftEdge.setBounds(0, 0, mLeftEdge.getIntrinsicWidth(), height);
        mLeftEdge.draw(canvas);
        if (mRightEdge != null) {
            mRightEdge.setBounds(width - mRightEdge.getIntrinsicWidth(), 0, width, height);
            mRightEdge.draw(canvas);
        }
        canvas.restoreToCount(i);
	}

	private class PageListener implements OnPageChangeListener {
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			mCurrentPosition = position;
			mCurrentPositionOffset = positionOffset;			
			scrollToChild(position, (int) (positionOffset * mTabsContainer.getChildAt(position).getWidth()));
			invalidate();
		}
		@Override
		public void onPageScrollStateChanged(int state) {
			if (state == ViewPager.SCROLL_STATE_IDLE) {
				if(mViewPager.getCurrentItem() == 0) {
					scrollTo(0, 0);
				} else if (mViewPager.getCurrentItem() == mTabCount - 1) {
					scrollTo(getScrollRange(), 0);
				} else {
					scrollToChild(mViewPager.getCurrentItem(), 0);
				}
			}
		}
		@Override
		public void onPageSelected(int position) {
		}
	}
}
