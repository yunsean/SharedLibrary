package com.dylan.player;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dylan.devices.audio.MxAudioPlayout;
import com.dylan.dynmedias.R;
import com.dylan.medias.codec.MxAacDecoder;
import com.dylan.medias.codec.MxAvcDecoder;
import com.dylan.medias.codec.MxMp3Decoder;
import com.dylan.medias.stream.NativeMethod;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerView extends RelativeLayout implements Callback, Runnable, MxAvcDecoder.Callback, OnClickListener, MxAacDecoder.Callback, MxMp3Decoder.Callback {

	public interface Callback {
		void onPlay(int width, int height);
		void onStop();
		void onError(String reason);
	}
	
	private SurfaceView mSurfaceView = null;
	private ProgressBar mProgressBar = null;
	private String mRtspUrl = null;
	private boolean mTcpOnly = true;
	private boolean mAutoPlay = true;
	private boolean mNoDelay = false;
	private boolean mAutoRetry = true;
	private Thread mPlayThread = null;
	private boolean mWillQuit = false;
	private long mNativeHandle = 0;
	private MxAudioPlayout mAudioPlayout = null;
	private boolean mStarted = false;
	private Callback mCallback = null;
	private LinearLayout mVUMeterGroup = null;
	private VUMeterView[] mVUMeter = null;
	private boolean mMuted = false;
	private int mWidth = 0;
	private int mHeight = 0;

	public PlayerView(Context context) {
		super(context);
		mHandler = new VMMeterHandler(new WeakReference<PlayerView>(this));
	}
	public PlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mHandler = new VMMeterHandler(new WeakReference<PlayerView>(this));
	}
	public PlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mHandler = new VMMeterHandler(new WeakReference<PlayerView>(this));
	}

	public PlayerView setTcpOnly(boolean tcpOnly) {
		this.mTcpOnly = tcpOnly;
		return this;
	}
	public PlayerView setAutoPlay(boolean autoPlay) {
		this.mAutoPlay = autoPlay;
		return this;
	}
	public PlayerView setNoDelay(boolean noDelay) {
		this.mNoDelay = noDelay;
		return this;
	}
	public PlayerView setAutoRetry(boolean autoRetry) {
		this.mAutoRetry = autoRetry;
		return this;
	}
	public PlayerView setCallback(Callback callback) {
		this.mCallback = callback;
		return this;
	}
	public void setMuted(boolean muted) {
		this.mMuted = muted;
	}

	public PlayerView open(String url) {
		stop();
		mRtspUrl = url;
		mWillQuit = false;

		setBackgroundColor(0xff000000);
		removeAllViews();

		mVUMeterGroup = new LinearLayout(getContext());
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mVUMeterGroup.setId(1000);
		mVUMeterGroup.setOrientation(LinearLayout.VERTICAL);
		addView(mVUMeterGroup, layoutParams);

		mSurfaceView = new SurfaceView(getContext());
		layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		layoutParams.addRule(RelativeLayout.ALIGN_LEFT);
		layoutParams.addRule(RelativeLayout.ABOVE, 1000);
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
			mStarted = false;
		} else {
			mStarted = true;
		}
		if (mAutoRetry) {
			if (this.mTimer == null) {
				this.mTimer = new Timer(true);
				this.mTimer.schedule(new CheckTimerTask(this), 1000L, 2000L);
			}
		}

		mSurfaceView.getHolder().addCallback(this);
		mPlayThread = new Thread(this);
		mPlayThread.start();
		return this;
	}
	public boolean isPlaying() {
		return mPlayThread != null && mPlayThread.isAlive();
	}
	public PlayerView stop() {
		try {
			mWillQuit = true;
			if (mNativeHandle != 0) {
				NativeMethod.native_interrupt(mNativeHandle);
			}
			if (mPlayThread != null) {
				mPlayThread.interrupt();
				mPlayThread.join();
			}
			if (mNativeHandle != 0) {
				NativeMethod.native_close(mNativeHandle);
				mNativeHandle = 0;
			}
			if (mTimer != null) {
				mTimer.cancel();
				mTimer = null;
			}
			mProgressBar.setVisibility(View.GONE);
		} catch (Exception e) {
		}
		return this;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mWidth > 0 && mHeight > 0)
			resizeSurfaceView();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	private long mLatestFrame = 0;
	private Timer mTimer = null;
	private static class CheckTimerTask extends TimerTask {
		private WeakReference<PlayerView> playerViewWeakReference = null;
		public CheckTimerTask(PlayerView playerView) {
			playerViewWeakReference = new WeakReference<PlayerView>(playerView);
		}

		@Override
		public void run() {
			PlayerView recorder = this.playerViewWeakReference.get();
			if (recorder != null) {
				if (recorder.mLatestFrame > 0 && System.currentTimeMillis() - recorder.mLatestFrame > 1500) {
					recorder.mLatestFrame = 0;
					recorder.mHandler.sendEmptyMessage(MsgWhat_Reconnect);
				}
			}
		}
	}

	@Override
	public void run() {
        mAudioPlayout = null;
		final String error = runIn();
		if (error != null && !mWillQuit) {
			post(new Runnable() {
				@Override
				public void run() {
					if (mCallback != null) {
						mCallback.onError(error);
					} else {
						Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
					}
				}
			});
		} else if (!mWillQuit) {
			post(new Runnable() {
				@Override
				public void run() {
					if (mCallback != null) {
						mCallback.onStop();
					}
				}
			});
		}
		mNativeHandle = 0;
        mAudioPlayout = null;
	}

	private String runIn() {
        boolean result = true;
        boolean first = true;
    	do {
            mAudioPlayout = null;
    		if (mNativeHandle != 0) {
    			NativeMethod.native_interrupt(mNativeHandle);
    			NativeMethod.native_close(mNativeHandle);
    			mNativeHandle = 0;
    		}
        	mNativeHandle = NativeMethod.native_init();
            if (mNativeHandle == 0) {
            	if (first) {
					return "Open native handle failed.";
            	} else {
            		sleep(1000);
            		continue;
            	}
            }
            if (!NativeMethod.native_open(mNativeHandle, mRtspUrl, mTcpOnly)) {
                NativeMethod.native_close(mNativeHandle);
                mNativeHandle = 0;
            	if (first) {
	            	return "Open rtsp stream failed.";
            	} else {
            		sleep(1000);
            		continue;
            	}
            }
            while (mSurfaceView.getHolder().getSurface() == null && !mWillQuit) {
            	try {
    				Thread.sleep(20);
    			} catch (Exception e) {
    			}
    		}
            if (mWillQuit) {
                NativeMethod.native_close(mNativeHandle);
                mNativeHandle = 0;
            	break;
            }
            int videoIndex = NativeMethod.native_videoIndex(mNativeHandle);
            int audioIndex = NativeMethod.native_audioIndex(mNativeHandle);
            int videoCodec = NativeMethod.native_videoCodec(mNativeHandle);
            int audioCodec = NativeMethod.native_audioCodec(mNativeHandle);
            long beginTimeStamp = -1;
            long beginTickCount = -1;
            long latestTimeStamp = -1;
            if (videoIndex == -1) {
                NativeMethod.native_close(mNativeHandle);
                mNativeHandle = 0;
            	if (first) {
            		return "Not found video track in stream.";
            	} else {
            		sleep(1000);
            		continue;             		
            	}
            }
            MxAvcDecoder videoDecoder = new MxAvcDecoder(this);
			if (videoCodec == NativeMethod.Codec_Avc) {
				if (!videoDecoder.startup(mSurfaceView.getHolder().getSurface())) {
					NativeMethod.native_close(mNativeHandle);
					mNativeHandle = 0;
					if (first) {
						return "Startup avc decoder failed.";
					} else {
						sleep(1000);
						continue;
					}
				}
			}
            MxMp3Decoder mp3Decoder = null;
            MxAacDecoder aacDecoder = null;
            if (audioCodec == NativeMethod.Codec_Aac) {
            	aacDecoder = new MxAacDecoder(this);
            	byte[] data = NativeMethod.native_audioExtraData(mNativeHandle);
            	if (!aacDecoder.startup(data, 0, data.length)) {
            		NativeMethod.native_close(mNativeHandle);
                    videoDecoder.shutdown();
                    mNativeHandle = 0;
                	if (first) {
    	            	return "Startup aac decoder failed.";
                	} else {
                		sleep(1000);
                		continue;
                	}
            	}
            } else if (audioCodec == NativeMethod.Codec_Mp3) {
            	mp3Decoder = new MxMp3Decoder(this);
            	if (!mp3Decoder.startup()) {
            		NativeMethod.native_close(mNativeHandle);
                    videoDecoder.shutdown();
                    mNativeHandle = 0;
                	if (first) {
    	            	return "Startup mp3 decoder failed.";
                	} else {
                		sleep(1000);
                		continue;            		
                	}
            	}
            }
            if (first) {
                post(new Runnable() {
        			@Override
        			public void run() {
        				if (mProgressBar != null) {
    	    				mProgressBar.setVisibility(View.GONE);
    	    				mProgressBar = null;
        				}
        			}
        		});
            }
            first = false;
            while (!mWillQuit) {
                byte[] data = NativeMethod.native_read(mNativeHandle);
                if (data == null || data.length < 12) {
                	if (!mAutoRetry) result = false;
                	break;
                }
    			int header = lhInt(data, 0);
				int index = header & 0x00ffffff;
				boolean key = ((header >> 24) & 0xff) != 0;
    			long timeStamp = lhLong(data, 4);
    			if (index == videoIndex) {
	    			if (beginTimeStamp == -1 || Math.abs(timeStamp - latestTimeStamp) > (1000 * 300)) {
	    				Log.w("dylan", "adjust reference time, add: " + (timeStamp - latestTimeStamp));
	    				beginTimeStamp = timeStamp;
	    				beginTickCount = System.currentTimeMillis();
	    			}
	                videoDecoder.decodeNals(data, 12, data.length - 12, 0);
	    			latestTimeStamp = timeStamp;
	                if (mNoDelay) continue;
	                int delay = (int)((timeStamp - beginTimeStamp) - (System.currentTimeMillis() - beginTickCount));
	                if (delay > 0) {
	                	sleep(delay);
	                }
    			} else if (index == audioIndex) {
    				if (aacDecoder != null) {
    					aacDecoder.decodeAFrame(data, 12, data.length - 12);
    				} else if (mp3Decoder != null) {
    					mp3Decoder.decodeAFrame(data, 12, data.length - 12);
    				}
    			}
            }
            if (aacDecoder != null) aacDecoder.shutdown();
            if (mp3Decoder != null) mp3Decoder.shutdown();
            if (mAudioPlayout != null) mAudioPlayout.close();
            videoDecoder.shutdown();
            NativeMethod.native_close(mNativeHandle);
            mNativeHandle = 0;
            mAudioPlayout = null;
		} while (mAutoRetry && !mWillQuit);
        return null;
	}
    private static void sleep(int ms) {
    	try {
			Thread.sleep(ms);
		} catch (Exception e) {
		}
    }
    private static int lhInt(byte[] from, int offset) {
        return (int)((from[offset + 0] & 0xFF) | ((from[offset + 1] & 0xFF) << 8) | ((from[offset + 2] & 0xFF) << 16) | ((from[offset + 3] & 0xFF) << 24));
    }
    private static long lhLong(byte[] from, int offset) {
        return (long)((from[offset + 0] & 0xFFL) | ((from[offset + 1] & 0xFFL) << 8) | ((from[offset + 2] & 0xFFL) << 16) | ((from[offset + 3] & 0xFFL) << 24) | ((from[offset + 4] & 0xFFL) << 32) | ((from[offset + 5] & 0xFFL) << 40) | ((from[offset + 6] & 0xFFL) << 48) | ((from[offset + 7] & 0xFFL) << 56));
    }
    public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
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
	public void onVideoFormat(final int width, final int height, int colorFormat) {
		mWidth = width;
		mHeight = height;
		post(new Runnable() {
            @Override
            public void run() {
				Log.i("dylan", "w=" + width + ", h=" + height);
				if (mCallback != null) mCallback.onPlay(width, height);
        		resizeSurfaceView();
            }
        });
	}
	@Override
	public void onVideoFrame(byte[] datas, int offset, int length) {
	}
	
	@Override
	public void onAudioFormat(int channel, int sampleRate, int bitWidth) {
		mAudioPlayout = new MxAudioPlayout();
		mAudioPlayout.open(sampleRate, channel, bitWidth);
		mAudioChannel = channel;
		mAudioBitWidth = bitWidth;
		if (mHandler != null) {
			Message message = mHandler.obtainMessage(MsgWhat_AudioFormat);
			message.arg1 = channel;
			mHandler.sendMessage(message);
		}
	}
	private void setupVuMeter(int channel) {
		if (mVUMeterGroup != null) {
			mVUMeterGroup.removeAllViews();
			mVUMeter = new VUMeterView[channel];
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 10);
			for (int i = 0; i < channel; i++) {
				if (i > 0) layoutParams.topMargin = 2;
				mVUMeter[i] = new VUMeterView(getContext());
				mVUMeter[i].setVertical(false);
				mVUMeterGroup.addView(mVUMeter[i], layoutParams);
			}
		}
	}
	@Override
	public void onAudioBlock(byte[] datas, int offset, int length) {
		if (!mStarted) return;
		if (mAudioPlayout != null && !mMuted) mAudioPlayout.playAudioBlock(datas, offset, length);
		if (mHandler != null) {
			double[] decibels = calcDecibelLevel(datas, offset, length);
			Message message = mHandler.obtainMessage(MsgWhat_AudioBlock);
			message.obj = decibels;
			mHandler.sendMessage(message);
		}
	}
	private void updateVuMeter(double[] decibels) {
		if (decibels != null && mVUMeter != null) {
			for (int i = 0; i < Math.min(decibels.length, mVUMeter.length); i++) {
				mVUMeter[i].setLevel(decibels[i] / 100);
			}
		}
	}

	private int mAudioChannel = 0;
	private int mAudioBitWidth = 0;
	private double[] calcDecibelLevel(byte[] datas, int offset, int length) {
		if (mAudioChannel < 1) return null;
		if (mAudioBitWidth != 16) return null;
		double[] result = new double[mAudioChannel];
		int bytePerSample = mAudioChannel << 1;
		int sampleCount = length / bytePerSample;
		for (int i = 0; i < mAudioChannel; i++) {
			int position = offset + (i << 1);
			double sampleSum = 0;
			for (int j = 0; j < sampleCount; j++, position += bytePerSample) {
				long sample = byteToInt(datas, position);
				sampleSum += sample * sample;
			}
			double mean = sampleSum / sampleCount;
			double volume = 10 * Math.log10(mean);
			result[i] = volume;
		}
		return result;
	}
	private int byteToInt(byte[] datas, int offset) {
		return (datas[offset + 1] << 8) + (datas[offset] & 0x00ff);
	}

	@Override
	public void onClick(View v) {
		if (mProgressBar != null) {
			mProgressBar.setVisibility(View.VISIBLE);
		}
		v.setVisibility(View.GONE);
		mStarted = true;
	}

	private final static int MsgWhat_AudioFormat = 1;
	private final static int MsgWhat_AudioBlock = 2;
	private final static int MsgWhat_Reconnect = 3;
	private VMMeterHandler mHandler = null;
	private static class VMMeterHandler extends Handler {
		private WeakReference<PlayerView> levelViewWeakReference = null;
		public VMMeterHandler(WeakReference<PlayerView> view) {
			levelViewWeakReference = view;
		}
		@Override
		public void handleMessage(Message msg) {
			PlayerView view = levelViewWeakReference == null ? null : levelViewWeakReference.get();
			if (view == null) return;
			if (msg.what == MsgWhat_AudioFormat) {
				view.setupVuMeter(msg.arg1);
			} else if (msg.what == MsgWhat_AudioBlock) {
				view.updateVuMeter((double[])msg.obj);
			} else if (msg.what == MsgWhat_Reconnect) {
				view.mHandler.removeCallbacks(view.reopen);
				view.mHandler.postDelayed(view.reopen, 5000);
			}
		}
	}

	private boolean mReconnecting = false;
	private Runnable reopen = new Runnable() {
		@Override
		public void run() {
			try {
				if (mReconnecting) return;
				mReconnecting = true;
				mWillQuit = true;
				if (mNativeHandle != 0) {
					NativeMethod.native_interrupt(mNativeHandle);
				}
				if (mPlayThread != null) {
					mPlayThread.interrupt();
					mPlayThread.join();
				}
				if (mNativeHandle != 0) {
					NativeMethod.native_close(mNativeHandle);
					mNativeHandle = 0;
				}
			} catch (Exception e) {
			}
			mWillQuit = false;
			mPlayThread = new Thread(this);
			mPlayThread.start();
			mReconnecting = false;
		}
	};
}
