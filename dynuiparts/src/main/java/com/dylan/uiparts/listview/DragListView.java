package com.dylan.uiparts.listview;

import com.dylan.uiparts.R;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

public class DragListView extends ListView implements OnScrollListener {

	private float mLastY = -1;
	private Scroller mScroller;
	private OnScrollListener mScrollListener;

	private IDragListViewListener mListViewListener;

	private DragListViewHeader mHeaderView;
	private RelativeLayout mHeaderViewContent;
	private TextView mHeaderTimeView;
	private int mHeaderViewHeight; 
	private boolean mEnablePullRefresh = true;
	private boolean mPullRefreshing = false;

	private DragListViewFooter mFooterView;
	private boolean mEnablePullLoad = true;
	private boolean mPullLoading = true;
	private boolean mIsFooterReady = false;

	private int mTotalItemCount;
	private int mScrollBack;
	private final static int SCROLLBACK_HEADER = 0;
	private final static int SCROLLBACK_FOOTER = 1;
	private final static int SCROLL_DURATION = 400; 
	private final static int PULL_LOAD_MORE_DELTA = 1; 
	private final static int PULL_DRAG_MORE_PIXEL = 30;
	private final static float OFFSET_RADIO = 1.8f; 

	public DragListView(Context context) {
		super(context);
		initWithContext(context);
	}

	public DragListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initWithContext(context);
	}

	public DragListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initWithContext(context);
	}
	
	public void setForInner(boolean inner) {
		mForInner = inner;
	}
	public void setFooterBackgroundDrawable(Drawable background) {
		if (mFooterView != null)mFooterView.setBackgroundDrawable(background);
	}
	public void setFooterBackgroundResource(int resid) {
		if (mFooterView != null)mFooterView.setBackgroundResource(resid);
	}
	public void setFooterBackgroundColor(int color) {
		if (mFooterView != null)mFooterView.setBackgroundColor(color);
	}
	public void setHeaderDrawable(Drawable background, Drawable arrow) {
		if (mHeaderView != null)mHeaderView.setBackgroundDrawable(background, arrow);
	}
	public void setHeaderResource(int bgresid, int arrowid) {
		if (mHeaderView != null)mHeaderView.setBackgroundResource(bgresid, arrowid);
	}
	public void setHeaderColor(int color) {
		if (mHeaderView != null)mHeaderView.setBackgroundColor(color);
	}

	private void initWithContext(Context context) {
		mScroller = new Scroller(context, new DecelerateInterpolator());
		super.setOnScrollListener(this);
		mHeaderView = new DragListViewHeader(context);
		mHeaderViewContent = (RelativeLayout) mHeaderView.findViewById(R.id.draglistview_header_content);
		mHeaderTimeView = (TextView) mHeaderView.findViewById(R.id.draglistview_header_time);
		addHeaderView(mHeaderView);

		mFooterView = new DragListViewFooter(context);
		mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@SuppressWarnings("deprecation")
					@Override
					public void onGlobalLayout() {
						mHeaderViewHeight = mHeaderViewContent.getHeight();
						if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.JELLY_BEAN) {
							removeOnGlobalLayoutListener();
						} else {
							getViewTreeObserver().removeGlobalOnLayoutListener(this);
						}
					}
					@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
					private void removeOnGlobalLayoutListener() {
						getViewTreeObserver().removeOnGlobalLayoutListener(this);
					}
				});
	}

    private GestureDetector mGestureDetector = null; 
	public void setAsOuter() {
		mGestureDetector = new GestureDetector(getContext(), new YScrollDetector());  
        setFadingEdgeLength(0);  
	}
	
	@Override  
    public boolean onInterceptTouchEvent(MotionEvent ev) {  
        boolean result = super.onInterceptTouchEvent(ev);  
        if (mGestureDetector != null)return mGestureDetector.onTouchEvent(ev);
        else return result;
    } 
	
	class YScrollDetector extends SimpleOnGestureListener {  
        @Override  
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {  
            if (Math.abs(distanceY) >= Math.abs(distanceX)) {  
                return true;  
            } else { 
            	return false; 
            }
        }  
    } 

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (mIsFooterReady == false) {
			mIsFooterReady = true;
			addFooterView(mFooterView);
		}
		super.setAdapter(adapter);
	}

	public void setPullRefreshEnable(boolean enable) {
		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) { 
			mHeaderViewContent.setVisibility(View.INVISIBLE);
		} else {
			mHeaderViewContent.setVisibility(View.VISIBLE);
		}
	}

	public void setPullLoadEnable(boolean enable) {
		mEnablePullLoad = enable;
		if (!mEnablePullLoad) {
			mFooterView.hide();
			mFooterView.setOnClickListener(null);
		} else {
			mPullLoading = false;
			mFooterView.show();
			mFooterView.setState(DragListViewFooter.STATE_NORMAL);
			mFooterView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startLoadMore();
				}
			});
		}
	}

	public void triggerRefresh() {
		if (mPullRefreshing == false) {
			mPullRefreshing = true;
			mHeaderView.setState(DragListViewHeader.STATE_REFRESHING);
			if (mListViewListener != null) {
				mListViewListener.onRefresh();
			}
			int height = mHeaderView.getVisiableHeight();
			mScrollBack = SCROLLBACK_HEADER;
			mScroller.startScroll(0, height, 0, mHeaderViewHeight - height, SCROLL_DURATION);
			invalidate();
		}
	}
	public void triggerLoadMore() {
		startLoadMore();
	}
	public void stopRefresh() {
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			resetHeaderHeight();
		}
	}
	public void stopLoadMore() {
		if (mPullLoading == true) {
			mPullLoading = false;
			mFooterView.setState(DragListViewFooter.STATE_NORMAL);
		}
	}

	public void setRefreshTime(String time) {
		mHeaderTimeView.setText(time);
	}
	private void invokeOnScrolling() {
		if (mScrollListener instanceof OnXScrollListener) {
			OnXScrollListener l = (OnXScrollListener) mScrollListener;
			l.onXScrolling(this);
		}
	}

	private void updateHeaderHeight(float delta) {
		if (!mEnablePullRefresh || mHeaderView.getVisiableHeight() > mHeaderViewHeight + PULL_DRAG_MORE_PIXEL) {
			delta = 0;
		}
		mHeaderView.setVisiableHeight((int)delta + mHeaderView.getVisiableHeight());
		if (mEnablePullRefresh && !mPullRefreshing) { 
			if (mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
				mHeaderView.setState(DragListViewHeader.STATE_READY);
			} else {
				mHeaderView.setState(DragListViewHeader.STATE_NORMAL);
			}
		}
		setSelection(0); 
	}
	
	private void resetHeaderHeight() {
		int height = mHeaderView.getVisiableHeight();
		if (height == 0) {
			return;
		}
		if (mPullRefreshing && height <= mHeaderViewHeight) {
			return;
		}
		int finalHeight = 0; 
		if (mPullRefreshing && height > mHeaderViewHeight) {
			finalHeight = mHeaderViewHeight;
		}
		mScrollBack = SCROLLBACK_HEADER;
		mScroller.startScroll(0, height, 0, finalHeight - height, SCROLL_DURATION);
		invalidate();
	}

	private void updateFooterHeight(float delta) {
		if (!mEnablePullLoad || mFooterView.getBottomMargin() > PULL_DRAG_MORE_PIXEL) {
			delta = 0;
		}
		int height = mFooterView.getBottomMargin() + (int) delta;
		if (mEnablePullLoad && !mPullLoading) {
			if (height > PULL_LOAD_MORE_DELTA) {
				mFooterView.setState(DragListViewFooter.STATE_READY);
			} else {
				mFooterView.setState(DragListViewFooter.STATE_NORMAL);
			}
		}
		mFooterView.setBottomMargin(height);
	}

	private void resetFooterHeight() {
		int bottomMargin = mFooterView.getBottomMargin();
		if (bottomMargin > 0) {
			mScrollBack = SCROLLBACK_FOOTER;
			mScroller.startScroll(0, bottomMargin, 0, -bottomMargin, SCROLL_DURATION);
			invalidate();
		}
	}

	private void startLoadMore() {
		mPullLoading = true;
		mFooterView.setState(DragListViewFooter.STATE_LOADING);
		if (mListViewListener != null) {
			mListViewListener.onLoadMore();
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mLastY == -1) {
			mLastY = ev.getRawY();
		}

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastY = ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float deltaY = ev.getRawY() - mLastY;
			mLastY = ev.getRawY();
			if (getFirstVisiblePosition() == 0 && (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)) {
				updateHeaderHeight(deltaY / OFFSET_RADIO);
				invokeOnScrolling();
			} else if (getLastVisiblePosition() == mTotalItemCount - 1 && (mFooterView.getBottomMargin() > 0 || deltaY < 0)) {
				updateFooterHeight(-deltaY / OFFSET_RADIO);
			}
			break;
		default:
			mLastY = -1; 
			if (getFirstVisiblePosition() == 0) {
				if (mEnablePullRefresh && mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
					mPullRefreshing = true;
					mHeaderView.setState(DragListViewHeader.STATE_REFRESHING);
					if (mListViewListener != null) {
						mListViewListener.onRefresh();
					}
				}
				resetHeaderHeight();
			}
			if (getLastVisiblePosition() == mTotalItemCount - 1) {
				if (mEnablePullLoad && mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA) {
					startLoadMore();
				}
				resetFooterHeight();
			}
			break;
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			if (mScrollBack == SCROLLBACK_HEADER) {
				mHeaderView.setVisiableHeight(mScroller.getCurrY());
			} else {
				mFooterView.setBottomMargin(mScroller.getCurrY());
			}
			postInvalidate();
			invokeOnScrolling();
		}
		super.computeScroll();
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mScrollListener = l;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mScrollListener != null) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		mTotalItemCount = totalItemCount;
		if (mScrollListener != null) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	}

	public void setListener(IDragListViewListener l) {
		mListViewListener = l;
	}

	public interface OnXScrollListener extends OnScrollListener {
		public void onXScrolling(View view);
	}
	
	public interface IDragListViewListener {
		public void onRefresh();
		public void onLoadMore();
	}
	
	private boolean mForInner = false;
    @Override 
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { 
    	if (!mForInner) {
    		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	} else {
    		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST); 
    		super.onMeasure(widthMeasureSpec, expandSpec);
    	}
    }  
}
