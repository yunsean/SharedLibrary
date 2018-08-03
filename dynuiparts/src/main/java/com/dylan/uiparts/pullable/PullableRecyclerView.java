package com.dylan.uiparts.pullable;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.dylan.common.utils.Utility;

import java.lang.ref.WeakReference;

public class PullableRecyclerView extends RecyclerView implements Pullable {

    public PullableRecyclerView(Context context)	{
        super(context);
    }
    public PullableRecyclerView(Context context, AttributeSet attrs)	{
        super(context, attrs);
    }
    public PullableRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private WeakReference<PullToRefreshLayout> mWeakPullToRefreshLayout = null;
    @TargetApi(Build.VERSION_CODES.M)
    public void setupAutoLoad(PullToRefreshLayout layout) {
        mWeakPullToRefreshLayout = new WeakReference<PullToRefreshLayout>(layout);
        if (Utility.isMOrLater()) {
            setOnScrollChangeListener(new OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (getLastVisiblePosition() == (getChildCount() - 1)) {
                        if (mWeakPullToRefreshLayout != null && mWeakPullToRefreshLayout.get() != null) {
                            mWeakPullToRefreshLayout.get().autoLoad();
                        }
                    }
                }
            });
        } else {
            setOnScrollListener(new OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if (recyclerView == PullableRecyclerView.this && newState == SCROLL_STATE_IDLE && getLastVisiblePosition() == (getChildCount() - 1)) {
                        if (mWeakPullToRefreshLayout != null && mWeakPullToRefreshLayout.get() != null) {
                            mWeakPullToRefreshLayout.get().autoLoad();
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean canPullDown() {
        if (getChildCount() == 0) {
            return true;
        } else if (getFirstVisiblePosition() == 0 && getChildAt(0).getTop() >= 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canPullUp() {
        if (getChildCount() == 0) {
            return true;
        } else if (getLastVisiblePosition() == (getChildCount() - 1)) {
            if (getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()) != null && getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()).getBottom() <= getMeasuredHeight()) {
                return true;
            }
        }
        return false;
    }

    private int getFirstVisiblePosition() {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else if (layoutManager instanceof GridLayoutManager) {
            throw new IllegalAccessError("layoutManager is not LinearLayoutManager");
        }
        return 0;
    }
    private int getLastVisiblePosition() {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof GridLayoutManager) {
            throw new IllegalAccessError("layoutManager is not LinearLayoutManager");
        }
        return 0;
    }
}
