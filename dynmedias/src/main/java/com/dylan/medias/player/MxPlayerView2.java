package com.dylan.medias.player;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.dylan.devices.audio.MxAudioPlayout;
import com.dylan.dynmedias.R;
import com.dylan.medias.codec.MxAudioDecoder;
import com.dylan.medias.codec.MxVideoDecoder;

import java.nio.ByteBuffer;

@Deprecated
public class MxPlayerView2 extends RelativeLayout implements Callback, OnClickListener, MxVideoDecoder.Callback, MxAudioDecoder.Callback {

	public interface Callback {
		void onPlay(int width, int height);
		void onError(String reason);
	}

	private MxVideoDecoder mVideoDecoder = null;
	private MxAudioDecoder mAudioDecoder = null;
	private MxAudioPlayout mAudioPlayout = null;
	private MediaFormat mVideoFormat = null;
	private MediaFormat mAudioFormat = null;

	private SurfaceView mSurfaceView = null;
	private ProgressBar mProgressBar = null;
	private boolean mAutoPlay = true;
	private boolean mNoDelay = false;
	private Callback mCallback = null;
	private boolean mQuited = true;
	private boolean mStarted = false;
	private boolean mInited = false;

	long beginTimeStamp = -1;
	long beginTickCount = -1;
	long latestTimeStamp = -1;

	public MxPlayerView2(Context context) {
		super(context);
	}
	public MxPlayerView2(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public MxPlayerView2(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public MxPlayerView2 setAutoPlay(boolean autoPlay) {
		this.mAutoPlay = autoPlay;
		return this;
	}
	public MxPlayerView2 setNoDelay(boolean noDelay) {
		this.mNoDelay = noDelay;
		return this;
	}
	public MxPlayerView2 setCallback(Callback callback) {
		this.mCallback = callback;
		return this;
	}

	public MxPlayerView2 open(MediaFormat videoFormat, MediaFormat audioFormat) {
		stop();
		mQuited = false;
		mStarted = false;
		mInited = false;
		setBackgroundColor(0xff000000);
		removeAllViews();

		mSurfaceView = new SurfaceView(getContext());
		mSurfaceView.getHolder().addCallback(this);
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

		mVideoFormat = videoFormat;
		mAudioFormat = audioFormat;
		if (mAutoPlay) {
			mStarted = true;
		}
		return this;
	}
	public boolean isPlaying() {
		return mInited && mStarted && !mQuited;
	}
	public void onVideoFrame(long timecode, byte[] data, int offset, int size, boolean key) {
		if (!isPlaying() || mVideoDecoder == null) return;
		if (beginTimeStamp == -1 || Math.abs(timecode - latestTimeStamp) > (1000 * 300)) {
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
	public void onAudioFrame(long timecode, byte[] data, int offset, int size) {
		if (!isPlaying() || mAudioDecoder == null) return;
		mAudioDecoder.decode(data, offset, size);
	}
	public MxPlayerView2 stop() {
		mQuited = true;
		if (mVideoDecoder != null) mVideoDecoder.close();
		mVideoDecoder = null;
		if (mAudioDecoder != null) mAudioDecoder.close();
		mAudioDecoder = null;
		if (mAudioPlayout != null) mAudioPlayout.close();
		mAudioPlayout = null;
		removeAllViews();
		return this;
	}
	private static void sleep(int ms) {
		try { Thread.sleep(ms); } catch (Exception e) { }
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        Log.e("dylan", "surfaceCreated");
		if (mVideoFormat != null) {
			mVideoDecoder = new MxVideoDecoder(this);
			if (!mVideoDecoder.open(mVideoFormat, mSurfaceView.getHolder().getSurface())) {
				mQuited = true;
				if (mCallback != null) mCallback.onError("Open video decoder failed.");
				return;
			}
		}
		if (mAudioFormat != null) {
			mAudioDecoder = new MxAudioDecoder(this);
			if (!mAudioDecoder.open(mAudioFormat)) {
				mQuited = true;
				if (mCallback != null) mCallback.onError("Open audio decoder failed.");
				return;
			}
		}
		mInited = true;
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}


	@Override
	public void onVideoFormat(final MediaFormat mediaFormat) {
		post(new Runnable() {
            @Override
            public void run() {
				int width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
				int height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
				if (mCallback != null) mCallback.onPlay(width, height);
            	int w = mSurfaceView.getWidth();
            	int h = mSurfaceView.getHeight();
                LayoutParams layoutParams = (LayoutParams) mSurfaceView.getLayoutParams();
                if (h > w * height / width) layoutParams.height = w * height / width;
                else layoutParams.width = h * width / height;
                mSurfaceView.requestLayout();
				if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
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
		if (mAudioPlayout != null) mAudioPlayout.playAudioBlock(datas, offset, length);
	}

	@Override
	public void onClick(View v) {
		if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
		v.setVisibility(View.GONE);
		mStarted = true;
	}
}
