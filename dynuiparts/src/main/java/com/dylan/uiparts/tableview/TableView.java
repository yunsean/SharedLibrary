package com.dylan.uiparts.tableview;

import com.dylan.uiparts.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class TableView extends LinearLayout {
	
	public interface TableAdapter {
		int getCount();
	    View getView(int position);
	}
	public interface OnTableItemClickListener {
	    void onClick(int position, View v);
	}
	
	TableAdapter mAdapter;
	int mSize = 0;
	
	public TableView(Context context) {
	    super(context);
	    init();
	}
	public TableView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	void init() {
		this.setOrientation(LinearLayout.VERTICAL);
		this.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		this.setBackgroundResource(R.drawable.background_view_rounded_container);
	}

	public void setAdapter(TableAdapter mAdapter) {
		this.mAdapter = mAdapter;
		if (mAdapter != null) {
			mSize = mAdapter.getCount();
			for (int i = 0; i < mSize; i++) {
				View child = mAdapter.getView(i);
				this.addView(child);
			}
			commit();
		}
	}

	@Override
	public void addView(View child) {
		super.addView(child);
		child.setClickable(true);
	}
	public void commit() {
		int len = this.getChildCount();
		if (len > 1) {// 多项内容
			for (int i = 0; i < len; i++) {
				View child = this.getChildAt(i);
				if (i == 0) {// 顶部
					child.setBackgroundResource(R.drawable.background_view_rounded_top);
				} else if (i > 0 && i < len - 1) {// 中间
					child.setBackgroundResource(R.drawable.background_view_rounded_middle);
				} else if (i == len - 1) {// 底部
					child.setBackgroundResource(R.drawable.background_view_rounded_bottom);
				}
			}
		} else if (len == 1) {// 一项内容
			View child = this.getChildAt(0);
			child.setBackgroundResource(R.drawable.background_view_rounded_single);
		}
	}
	
	OnTableItemClickListener mItemClick;
	public void setOnTableItemClickListener(final OnTableItemClickListener mItemClick) {
		this.mItemClick = mItemClick;
		for (int i = 0; i < mSize; i++) {
			final int index = i;
			View childView = this.getChildAt(i);
			childView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mItemClick != null) {
						mItemClick.onClick(index, v);
					}
				}
			});
		}
	} 
}
