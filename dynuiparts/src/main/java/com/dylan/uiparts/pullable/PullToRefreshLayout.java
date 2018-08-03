package com.dylan.uiparts.pullable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dylan.uiparts.R;

import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("HandlerLeak")
public class PullToRefreshLayout extends RelativeLayout {
	public static final String TAG = "PullToRefreshLayout";
	public static final int INIT = 0;
	public static final int RELEASE_TO_REFRESH = 1;
	public static final int REFRESHING = 2;
	public static final int RELEASE_TO_LOAD = 3;
	public static final int LOADING = 4;
	public static final int DONE = 5;
	private int state = INIT;
	private OnRefreshListener mOnRefresh;
	private OnLoadmoreListener mOnLoadmore;
	public static final int SUCCEED = 0;
	public static final int FAIL = 1;
	private float downX, downY, lastY;
	private boolean isOutter = false;
	
	private CharSequence refreshSucceedTips = "";
	private CharSequence loadmoreSucceedTips = "";
	private CharSequence refreshFailedTips = "";
	private CharSequence loadmoreFailedTips = "";
	private CharSequence pullToRefreshTips = "";
	private CharSequence pullToLoadmoreTips = "";
	private CharSequence releaseToRefreshTips = "";
	private CharSequence releaseToLoadmoreTips = "";
	private CharSequence refreshingTips = "";
	private CharSequence loadmoreingTips = "";

	public float pullDownY = 0;
	private float pullUpY = 0;

	private float refreshDist = 200;
	private float loadmoreDist = 200;

	private MyTimer timer;
	public float MOVE_SPEED = 8;
	private boolean isLayout = false;
	private boolean isTouch = false;
	private float radio = 2;

	private RotateAnimation rotateAnimation;
	private RotateAnimation refreshingAnimation;

	private View refreshView;
	private View pullView;
	private View refreshingView;
	private View refreshStateImageView;
	private TextView refreshStateTextView;

	private View loadmoreView;
	private View pullUpView;
	private View loadingView;
	private View loadStateImageView;
	private TextView loadStateTextView;

	private View pullableView;
	private int mEvents;
	private boolean canPullDown = true;
	private boolean canPullUp = true;
	private boolean allowPullDown = true;
	private boolean allowPullUp = true;

	@SuppressLint("HandlerLeak")
	Handler updateHandler = new Handler() {
		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			MOVE_SPEED = (float) (8 + 5 * Math.tan(Math.PI / 2 / getMeasuredHeight() * (pullDownY + Math.abs(pullUpY))));
			if (!isTouch) {
				if (state == REFRESHING && pullDownY <= refreshDist) {
					pullDownY = refreshDist;
					timer.cancel();
				} else if (state == LOADING && -pullUpY <= loadmoreDist) {
					pullUpY = -loadmoreDist;
					timer.cancel();
				}
			}
			if (pullDownY > 0)
				pullDownY -= MOVE_SPEED;
			else if (pullUpY < 0)
				pullUpY += MOVE_SPEED;
			if (pullDownY < 0) {
				pullDownY = 0;
				pullView.clearAnimation();
				if (state != REFRESHING && state != LOADING)
					changeState(INIT);
				timer.cancel();
				requestLayout();
			}
			if (pullUpY > 0) {
				pullUpY = 0;
				pullUpView.clearAnimation();
				if (state != REFRESHING && state != LOADING)
					changeState(INIT);
				timer.cancel();
				requestLayout();
			}
			requestLayout();
			if (pullDownY + Math.abs(pullUpY) == 0)
				timer.cancel();
		}
	};
	
	public void setIsOutter(boolean isOutter) {
		this.isOutter = isOutter;
	}

	public void setListener(OnRefreshListener onRefresh, OnLoadmoreListener onLoadmore)	{
		mOnRefresh = onRefresh;
		mOnLoadmore = onLoadmore;
	}
	
	public PullToRefreshLayout(Context context)	{
		super(context);
		initView(context);
	}
	public PullToRefreshLayout(Context context, AttributeSet attrs)	{
		super(context, attrs);
		initView(context);
	}
	public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyle)	{
		super(context, attrs, defStyle);
		initView(context);
	}

	public void setAllowRefresh(boolean allow) {
		allowPullDown = allow;
	}
	public void setAllowLoadmore(boolean allow) {
		allowPullUp = allow;
	}
	private void initView(Context context)	{
		timer = new MyTimer(updateHandler);
		rotateAnimation = (RotateAnimation) AnimationUtils.loadAnimation(context, R.anim.reverse_anim);
		refreshingAnimation = (RotateAnimation) AnimationUtils.loadAnimation(context, R.anim.rotating);
		LinearInterpolator lir = new LinearInterpolator();
		rotateAnimation.setInterpolator(lir);
		refreshingAnimation.setInterpolator(lir);
		
		if (!isInEditMode()) {
			Resources res = getResources();
			refreshSucceedTips = res.getText(R.string.pullable_refresh_succeed);
			loadmoreSucceedTips = res.getText(R.string.pullable_load_succeed);
			refreshFailedTips = res.getText(R.string.pullable_refresh_fail);
			loadmoreFailedTips = res.getText(R.string.pullable_load_fail);
			pullToRefreshTips = res.getText(R.string.pullable_pull_to_refresh);
			pullToLoadmoreTips = res.getText(R.string.pullable_pullup_to_load);
			releaseToRefreshTips = res.getText(R.string.pullable_release_to_refresh);
			releaseToLoadmoreTips = res.getText(R.string.pullable_release_to_load);
			refreshingTips = res.getText(R.string.pullable_refreshing);
			loadmoreingTips = res.getText(R.string.pullable_loading);
		}
	}

	public void setRefreshTips(String pull, String release, String ing, String succeed, String failed) {
		if (succeed != null)refreshSucceedTips = succeed;
		if (failed != null)refreshFailedTips = failed;
		if (pull != null)pullToRefreshTips = pull;
		if (release != null)releaseToRefreshTips = release;
		if (ing != null)refreshingTips = ing;
	}
	public void setLoadmoreTips(String pull, String release, String ing, String succeed, String failed) {
		if (succeed != null)loadmoreSucceedTips = succeed;
		if (failed != null)loadmoreFailedTips = failed;
		if (pull != null)pullToLoadmoreTips = pull;
		if (release != null)releaseToLoadmoreTips = release;
		if (ing != null)loadmoreingTips = ing;
	}

	private void hide()	{
		timer.schedule(5);
	}

	public void refreshFinish() {
		refreshFinish(SUCCEED);
	}
	public void refreshFinish(int refreshResult)	{
		if (refreshingView == null)return;
		if (refreshingView.getVisibility() == View.GONE)return;
		refreshingView.clearAnimation();
		refreshingView.setVisibility(View.GONE);
		switch (refreshResult)		{
		case SUCCEED:
			refreshStateImageView.setVisibility(View.VISIBLE);
			refreshStateTextView.setText(refreshSucceedTips);
			refreshStateImageView.setBackgroundResource(R.drawable.pullable_refresh_succeed);
			break;
		case FAIL:
		default:
			refreshStateImageView.setVisibility(View.VISIBLE);
			refreshStateTextView.setText(refreshFailedTips);
			refreshStateImageView.setBackgroundResource(R.drawable.pullable_refresh_failed);
			break;
		}
		if (pullDownY > 0) {
			new Handler() {
				@Override
				public void handleMessage(Message msg) {
					changeState(DONE);
					hide();
				}
			}.sendEmptyMessageDelayed(0, 100);
		} else {
			changeState(DONE);
			hide();
		}
	}

	public void loadmoreFinish() {
		loadmoreFinish(SUCCEED);
	}
	public void loadmoreFinish(int refreshResult) {
		if (loadingView == null || loadingView.getVisibility() == View.GONE)return;
		loadingView.clearAnimation();
		loadingView.setVisibility(View.GONE);
		switch (refreshResult) {
		case SUCCEED:
			loadStateImageView.setVisibility(View.VISIBLE);
			loadStateTextView.setText(loadmoreSucceedTips);
			loadStateImageView.setBackgroundResource(R.drawable.pullable_load_succeed);
			break;
		case FAIL:
		default:
			loadStateImageView.setVisibility(View.VISIBLE);
			loadStateTextView.setText(loadmoreFailedTips);
			loadStateImageView.setBackgroundResource(R.drawable.pullable_load_failed);
			break;
		}
		if (pullUpY < 0) {
			final int height = getHeight() - pullableView.getBottom();
			new Handler() {
				@Override
				public void handleMessage(Message msg) {
					changeState(DONE);
					hide();
					if (mAutoScrollAfterLoading) {
						pullableView.scrollBy(0, height);
					}
				}
			}.sendEmptyMessageDelayed(0, 100);
		} else {
			changeState(DONE);
			hide();
		}
	}
	private boolean mAutoScrollAfterLoading = false;
	public void setAutoScrollAfterLoading(boolean auto) {
		mAutoScrollAfterLoading = auto;
	}

	private void changeState(int to) {
		state = to;
		if (refreshStateImageView == null)return;
		switch (state) {
		case INIT:
			refreshStateImageView.setVisibility(View.GONE);
			refreshStateTextView.setText(pullToRefreshTips);
			pullView.clearAnimation();
			pullView.setVisibility(View.VISIBLE);

			loadStateImageView.setVisibility(View.GONE);
			loadStateTextView.setText(pullToLoadmoreTips);
			pullUpView.clearAnimation();
			pullUpView.setVisibility(View.VISIBLE);
			break;
		case RELEASE_TO_REFRESH:
			refreshStateTextView.setText(releaseToRefreshTips);
			pullView.startAnimation(rotateAnimation);
			break;
		case REFRESHING:
			pullView.clearAnimation();
			refreshingView.setVisibility(View.VISIBLE);
			pullView.setVisibility(View.INVISIBLE);
			refreshingView.startAnimation(refreshingAnimation);
			refreshStateTextView.setText(refreshingTips);
			break;
		case RELEASE_TO_LOAD:
			loadStateTextView.setText(releaseToLoadmoreTips);
			pullUpView.startAnimation(rotateAnimation);
			break;
		case LOADING:
			pullUpView.clearAnimation();
			loadingView.setVisibility(View.VISIBLE);
			pullUpView.setVisibility(View.INVISIBLE);
			loadingView.startAnimation(refreshingAnimation);
			loadStateTextView.setText(loadmoreingTips);
			break;
		case DONE:
			break;
		}
	}

	private void releasePull() {
		canPullDown = true;
		canPullUp = true;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			downX = ev.getX();
			downY = ev.getY();
			lastY = downY;
			timer.cancel();
			mEvents = 0;
			releasePull();
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
		case MotionEvent.ACTION_POINTER_UP:
			mEvents = -1;
			break;
		case MotionEvent.ACTION_MOVE:
			if (isOutter && (Math.abs(ev.getX() - downX) > Math.abs(ev.getY() - downY))) {
				return super.dispatchTouchEvent(ev);
			}
			if (mEvents == 0) {
				if (pullDownY > 0 || (allowPullDown && ((Pullable) pullableView).canPullDown() && canPullDown && state != LOADING)) {
					pullDownY = pullDownY + (ev.getY() - lastY) / radio;
					if (pullDownY < 0) {
						pullDownY = 0;
						canPullDown = false;
						canPullUp = true;
					}
					if (pullDownY > getMeasuredHeight())
						pullDownY = getMeasuredHeight();
					if (state == REFRESHING) {
						isTouch = true;
					}
				} else if (pullUpY < 0 || (allowPullUp && ((Pullable) pullableView).canPullUp() && canPullUp && state != REFRESHING)) {
					pullUpY = pullUpY + (ev.getY() - lastY) / radio;
					if (pullUpY > 0) {
						pullUpY = 0;
						canPullDown = true;
						canPullUp = false;
					}
					if (pullUpY < -getMeasuredHeight())
						pullUpY = -getMeasuredHeight();
					if (state == LOADING) {
						isTouch = true;
					}
				} else
					releasePull();
			} else {
				mEvents = 0;
			}
			lastY = ev.getY();
			radio = (float) (2 + 2 * Math.tan(Math.PI / 2 / getMeasuredHeight() * (pullDownY + Math.abs(pullUpY))));
			if (pullDownY > 0 || pullUpY < 0)
				requestLayout();
			if (pullDownY > 0) {
				if (pullDownY <= refreshDist && (state == RELEASE_TO_REFRESH || state == DONE)) {
					changeState(INIT);
				}
				if (pullDownY >= refreshDist && state == INIT) {
					changeState(RELEASE_TO_REFRESH);
				}
			} else if (pullUpY < 0) {
				if (-pullUpY <= loadmoreDist && (state == RELEASE_TO_LOAD || state == DONE)) {
					changeState(INIT);
				}
				if (-pullUpY >= loadmoreDist && state == INIT) {
					changeState(RELEASE_TO_LOAD);
				}
			}
			if ((pullDownY + Math.abs(pullUpY)) > 8) {
				ev.setAction(MotionEvent.ACTION_CANCEL);
			}
			break;
		case MotionEvent.ACTION_UP:
			if (pullDownY > refreshDist || -pullUpY > loadmoreDist) {
				isTouch = false;
			}
			if (state == RELEASE_TO_REFRESH) {
				changeState(REFRESHING);
				if (mOnRefresh != null)
					mOnRefresh.onRefresh(this);
			} else if (state == RELEASE_TO_LOAD) {
				changeState(LOADING);
				if (mOnLoadmore != null)
					mOnLoadmore.onLoadMore(this);
			}
			hide();
		default:
			break;
		}
		super.dispatchTouchEvent(ev);
		return true;
	}

	private class AutoRefreshTask extends AsyncTask<Integer, Float, String> {
		@Override
		protected void onPreExecute() {
			pullDownY = 0;
			changeState(INIT);
		}
		@Override
		protected String doInBackground(Integer... params) {
			while (pullDownY < 4 / 3 * refreshDist) {
				pullDownY += MOVE_SPEED + MOVE_SPEED + MOVE_SPEED + MOVE_SPEED;
				publishProgress(pullDownY);
				try {
					Thread.sleep(params[0]);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			changeState(REFRESHING);
			if (mOnRefresh != null) mOnRefresh.onRefresh(PullToRefreshLayout.this);
			hide();
		}
		@Override
		protected void onProgressUpdate(Float... values) {
			if (pullDownY > refreshDist)changeState(RELEASE_TO_REFRESH);
			requestLayout();
		}
	}
	private class AutoLoadTask extends AsyncTask<Integer, Float, String> {
		@Override
		protected void onPreExecute() {
			pullUpY = 0;
			changeState(INIT);
		}
		@Override
		protected String doInBackground(Integer... params) {
			while (-pullUpY < 4 / 3 * loadmoreDist) {
				pullUpY -= MOVE_SPEED + MOVE_SPEED + MOVE_SPEED + MOVE_SPEED;
				publishProgress(pullUpY);
				try {
					Thread.sleep(params[0]);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			changeState(LOADING);
			if (mOnLoadmore != null) mOnLoadmore.onLoadMore(PullToRefreshLayout.this);
			hide();
		}
		@Override
		protected void onProgressUpdate(Float... values) {
			if (-pullUpY > loadmoreDist)changeState(RELEASE_TO_LOAD);
			requestLayout();
		}
	}

	public void autoRefresh(boolean animated) {
		if (animated) {
			AutoRefreshTask task = new AutoRefreshTask();
			task.execute(1);
		} else {
			pullDownY = refreshDist * 4 / 3;
			requestLayout();
			changeState(REFRESHING);
			if (mOnRefresh != null)mOnRefresh.onRefresh(this);
		}
	}
	public void autoRefresh() {
		autoRefresh(true);
	}
	public void autoLoad(boolean animated) {
		if (!allowPullUp)return;
		if (animated) {
			AutoLoadTask task = new AutoLoadTask();
			task.execute(1);
		} else {
			pullUpY = -loadmoreDist * 4 / 3;
			requestLayout();
			changeState(LOADING);
			if (mOnLoadmore != null)mOnLoadmore.onLoadMore(this);
		}
	}
	public void autoLoad() {
		autoLoad(true);
	}

	private void initView() {
		pullView = refreshView.findViewById(R.id.pull_icon);
		refreshStateTextView = (TextView) refreshView .findViewById(R.id.state_tv);
		refreshingView = refreshView.findViewById(R.id.refreshing_icon);
		refreshStateImageView = refreshView.findViewById(R.id.state_iv);

		pullUpView = loadmoreView.findViewById(R.id.pullup_icon);
		loadStateTextView = (TextView) loadmoreView.findViewById(R.id.loadstate_tv);
		loadingView = loadmoreView.findViewById(R.id.loading_icon);
		loadStateImageView = loadmoreView.findViewById(R.id.loadstate_iv);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (!isLayout) {
			refreshView = getChildAt(0);
			pullableView = getChildAt(1);
			loadmoreView = getChildAt(2);
			isLayout = true;
			initView();
		}
		if (refreshDist < 1 || loadmoreDist < 1) {
			refreshDist = ((ViewGroup) refreshView).getChildAt(0).getMeasuredHeight();
			loadmoreDist = ((ViewGroup) loadmoreView).getChildAt(0) .getMeasuredHeight();
		}

		refreshView.layout(0, (int) (pullDownY + pullUpY) - refreshView.getMeasuredHeight(), refreshView.getMeasuredWidth(), (int) (pullDownY + pullUpY));
		pullableView.layout(0, (int) (pullDownY + pullUpY), pullableView.getMeasuredWidth(), (int) (pullDownY + pullUpY) + pullableView.getMeasuredHeight());
		loadmoreView.layout(0, (int) (pullDownY + pullUpY) + pullableView.getMeasuredHeight(), loadmoreView.getMeasuredWidth(), (int) (pullDownY + pullUpY) + pullableView.getMeasuredHeight() + loadmoreView.getMeasuredHeight());
	}

	class MyTimer {
		private Handler handler;
		private Timer timer;
		private MyTask mTask;

		public MyTimer(Handler handler) {
			this.handler = handler;
			timer = new Timer();
		}
		public void schedule(long period) {
			if (mTask != null) {
				mTask.cancel();
				mTask = null;
			}
			mTask = new MyTask(handler);
			timer.schedule(mTask, 0, period);
		}
		public void cancel() {
			if (mTask != null) {
				mTask.cancel();
				mTask = null;
			}
		}
		class MyTask extends TimerTask {
			private Handler handler;
			public MyTask(Handler handler) {
				this.handler = handler;
			}
			@Override
			public void run() {
				handler.obtainMessage().sendToTarget();
			}
		}
	}
	
	public interface OnRefreshListener {
		void onRefresh(PullToRefreshLayout pullable);
	};
	public interface OnLoadmoreListener {
		void onLoadMore(PullToRefreshLayout pullable);
	}
}
