package com.dylan.medias.player;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dylan.devices.audio.MxAudioPlayout;
import com.dylan.dynmedias.R;
import com.dylan.medias.codec.MxAudioDecoder;
import com.dylan.medias.codec.MxVideoDecoder;
import com.dylan.medias.stream.MxStreamReader;

import java.nio.ByteBuffer;

public class MxPlayerView extends RelativeLayout implements Callback, OnClickListener, MxStreamReader.Callback, MxVideoDecoder.Callback, MxAudioDecoder.Callback {

	public interface Callback {
		void onPlay(int width, int height);
		void onStop();
		void onError(String reason);
	}

	private enum WorkStage {Inited, Playing, Stopped};
	private MxStreamReader mStreamReader = null;
	private MxVideoDecoder mVideoDecoder = null;
	private MxAudioDecoder mAudioDecoder = null;
	private MxAudioPlayout mAudioPlayout = null;

	private SurfaceView mSurfaceView = null;
	private ProgressBar mProgressBar = null;
	private boolean mAutoPlay = true;
	private boolean mNoDelay = false;
	private WorkStage mWorkStage = WorkStage.Inited;
	private Callback mCallback = null;

	private boolean mMuted = false;
	private int mWidth = 0;
	private int mHeight = 0;

	public MxPlayerView(Context context) {
		super(context);
	}
	public MxPlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public MxPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mWidth > 0 && mHeight > 0) {
			resizeSurfaceView();
		}
	}

	public MxPlayerView setAutoPlay(boolean autoPlay) {
		this.mAutoPlay = autoPlay;
		return this;
	}
	public MxPlayerView setNoDelay(boolean noDelay) {
		this.mNoDelay = noDelay;
		return this;
	}
	public MxPlayerView setCallback(Callback callback) {
		this.mCallback = callback;
		return this;
	}
	public void setMuted(boolean muted) {
		this.mMuted = muted;
	}

	public MxPlayerView open(MxStreamReader reader) {
		stop();
		mStreamReader = reader;
		mWorkStage = WorkStage.Inited;

		setBackgroundColor(0xff000000);
		removeAllViews();

		mSurfaceView = new SurfaceView(getContext());
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		addView(mSurfaceView, layoutParams);

		mProgressBar = new ProgressBar(getContext());
		mProgressBar.setIndeterminate(true);
		layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		addView(mProgressBar, layoutParams);

		if (!mAutoPlay) {
			ImageButton play = new ImageButton(getContext());
			play.setImageResource(R.drawable.player_button_play);
			play.setBackgroundColor(0x00000000);
			play.setOnClickListener(this);
			addView(play, layoutParams);
			mProgressBar.setVisibility(View.GONE);
		}

		mSurfaceView.getHolder().addCallback(this);
		mStreamReader.setCallback(this);
		return this;
	}
	public boolean isPlaying() {
		if (mStreamReader == null) return false;
		else if (!mStreamReader.isWorking()) return false;
		else return mWorkStage == WorkStage.Playing;
	}
	public MxPlayerView stop() {
		if (mWorkStage == WorkStage.Playing) {
			mWorkStage = WorkStage.Stopped;
			if (mCallback != null) mCallback.onStop();
		}
		if (mStreamReader != null) mStreamReader.stop();
		mStreamReader = null;
		if (mVideoDecoder != null) mVideoDecoder.close();
		mVideoDecoder = null;
		if (mAudioDecoder != null) mAudioDecoder.close();
		mAudioDecoder = null;
		if (mAudioPlayout != null) mAudioPlayout.close();
		mAudioPlayout = null;
		removeAllViews();
		return this;
	}

	@Override
	public void onPrepered() {
		while (mSurfaceView.getHolder().getSurface() == null) {
			sleep(200);
		}

		MediaFormat videoFormat = mStreamReader.video();
		if (videoFormat != null) {
			mVideoDecoder = new MxVideoDecoder(this);
			if (!mVideoDecoder.open(videoFormat, mSurfaceView.getHolder().getSurface())) {
				mWorkStage = WorkStage.Stopped;
				if (mCallback != null) mCallback.onError("Open video decoder failed.");
				return;
			}
		}
		MediaFormat audioFormat = mStreamReader.audio();
		if (audioFormat != null) {
			mAudioDecoder = new MxAudioDecoder(this);
			if (!mAudioDecoder.open(audioFormat)) {
				mWorkStage = WorkStage.Stopped;
				if (mCallback != null) mCallback.onError("Open audio decoder failed.");
				return;
			}
		}
		post(new Runnable() {
			@Override
			public void run() {
				if (mProgressBar != null) {
					mProgressBar.setVisibility(View.GONE);
					mProgressBar = null;
				}
			}
		});

		if (mAutoPlay) {
			mWorkStage = WorkStage.Playing;
		}
	}
	private static void sleep(int ms) {
		try { Thread.sleep(ms); } catch (Exception e) { }
	}

	long beginTimeStamp = -1;
	long beginTickCount = -1;
	long latestTimeStamp = -1;
	@Override
	public void onVideoFrame(long timecode, byte[] data, int offset, int size, boolean key) {
		if (mWorkStage != WorkStage.Playing) return;
		if (beginTimeStamp == -1 || Math.abs(timecode - latestTimeStamp) > 300) {
			beginTimeStamp = timecode;
			beginTickCount = System.currentTimeMillis();
		}
		mVideoDecoder.decode(data, offset, size, key, 0);
		latestTimeStamp = timecode;
		if (!mNoDelay) {
			int delay = (int) ((timecode - beginTimeStamp) - (System.currentTimeMillis() - beginTickCount));
			if (delay > 0) sleep(delay);
		}
	}
	@Override
	public void onAudioFrame(long timecode, byte[] data, int offset, int size) {
		if (mWorkStage != WorkStage.Playing) return;
		mAudioDecoder.decode(data, offset, size);
	}
	@Override
	public void onReconnecting() {
		post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getContext(), "onReconnecting", Toast.LENGTH_SHORT).show();
			}
		});
	}
	@Override
	public void onReconnected() {
		post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getContext(), "onReconnected", Toast.LENGTH_SHORT).show();
			}
		});
	}
	@Override
	public void onStop() {
		mWorkStage = WorkStage.Stopped;
		if (mCallback != null) mCallback.onStop();
	}
	@Override
	public void onError(String reason) {
		mWorkStage = WorkStage.Stopped;
		if (mCallback != null) mCallback.onError(reason);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Canvas canvas = holder.lockCanvas();
		canvas.drawColor(Color.BLACK);
		holder.unlockCanvasAndPost(canvas);
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	private void resizeSurfaceView() {
		if (mSurfaceView == null) return;
		int w = getWidth();
		int h = getHeight();
		LayoutParams layoutParams = (LayoutParams) mSurfaceView.getLayoutParams();
		if (h > w * mHeight / mWidth) layoutParams.height = w * mHeight / mWidth;
		else layoutParams.width = h * mWidth / mHeight;
		mSurfaceView.requestLayout();
	}

	@Override
	public void onVideoFormat(final MediaFormat mediaFormat) {
		post(new Runnable() {
            @Override
            public void run() {
				mWidth = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
				mHeight = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
				if (mCallback != null) mCallback.onPlay(mWidth, mHeight);
				resizeSurfaceView();
            }
        });
	}
	@Override
	public void onVideoFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer byteBuffer) {

	}
	@Override
	public void onError() {

	}

	@Override
	public void onAudioFormat(int channel, int sampleRate, int bitWidth) {
		mAudioPlayout = new MxAudioPlayout();
		mAudioPlayout.open(sampleRate, channel, bitWidth);
	}
	@Override
	public void onAudioBlock(byte[] datas, int offset, int length) {
		if (mAudioPlayout != null && !mMuted) mAudioPlayout.playAudioBlock(datas, offset, length);
	}

	@Override
	public void onClick(View v) {
		if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
		v.setVisibility(View.GONE);
		mWorkStage = WorkStage.Playing;
	}
}
