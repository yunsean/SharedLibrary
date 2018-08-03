package com.dylan.uiparts.pullable;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.GridView;

public class PullableGridView extends GridView implements Pullable {

	public PullableGridView(Context context) {
		super(context);
	}
	public PullableGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PullableGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private WeakReference<PullToRefreshLayout> mWeakPullToRefreshLayout = null;
	public void setupAutoLoad(PullToRefreshLayout layout) {
		mWeakPullToRefreshLayout = new WeakReference<PullToRefreshLayout>(layout);
		setOnScrollListener(new OnScrollListener() {			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == SCROLL_STATE_IDLE && view.getLastVisiblePosition() == (view.getCount() - 1)) {
					if (mWeakPullToRefreshLayout != null && mWeakPullToRefreshLayout.get() != null) {
						mWeakPullToRefreshLayout.get().autoLoad();
					}
				}
			}			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
	}
	
	@Override
	public boolean canPullDown() {
		if (getCount() == 0) {
			return true;
		} else if (getFirstVisiblePosition() == 0 && getChildAt(0).getTop() >= 0) {
			return true;
		} else {
			return false;
		}
	}
	@Override
	public boolean canPullUp() {
		if (getCount() == 0) {
			return true;
		} else if (getLastVisiblePosition() == (getCount() - 1)) {
			if (getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()) != null && getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()).getBottom() <= getMeasuredHeight()) {
				return true;
			}
		}
		return false;
	}

}
