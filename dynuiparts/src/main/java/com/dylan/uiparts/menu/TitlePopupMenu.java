package com.dylan.uiparts.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.dylan.uiparts.R;

public class TitlePopupMenu extends PopupWindow {
	private Context mContext;
	protected final int LIST_PADDING = 10;
	private Rect mRect = new Rect();
	private int mScreenWidth;
	private int popupGravity = Gravity.NO_GRAVITY;
	private ListView mListView = null;
	private OnItemClickedListener mListener = null;

	public TitlePopupMenu(Context context) {
		this(context, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}
	@SuppressLint("InflateParams")
	public TitlePopupMenu(Context context, int width, int height) {
		super(context);
		this.mContext = context;

		setFocusable(true);
		setTouchable(true);
		setOutsideTouchable(true);	
		mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;

		setWidth(width);
		setHeight(height);
		setBackgroundDrawable(new ColorDrawable(0x00000000));
		setContentView(LayoutInflater.from(mContext).inflate(R.layout.menu_titlepopup_layout, null));
		mListView = (ListView) getContentView().findViewById(R.id.title_list);
		mListView.setSelector(new BitmapDrawable());
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				dismiss();
				if (mListener != null) mListener.onItemClicked(position, id);
			}
		});
	}
	public void show(View view) {
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		mRect.set(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());
		showAtLocation(view, popupGravity, mScreenWidth - LIST_PADDING - (getWidth() / 2), mRect.bottom);
	}
	public void showAtTopLeft(View view) {
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		mRect.set(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());
		showAtLocation(view, popupGravity, mRect.left, mRect.bottom);		
	}

	public void setAdapter(BaseAdapter adapter) {
		mListView.setAdapter(adapter);
	}
	public void setOnItemClickedListener(OnItemClickedListener listener) {
		mListener = listener;
	}
	public void setBackground(int resId) {
		getContentView().findViewById(R.id.container).setBackgroundResource(resId);
	}
	public void setBackgroundColor(int color) {
		getContentView().findViewById(R.id.container).setBackgroundColor(color);
	}
	public ListView getListView() {
		return mListView;
	}
	
	public interface OnItemClickedListener {
		void onItemClicked(int position, long id);
	}
}
