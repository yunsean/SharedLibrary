package com.dylan.player;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.dylan.devices.audio.MxAudioPlayout;
import com.dylan.dynmedias.R;
import com.dylan.medias.codec.MxAacDecoder;
import com.dylan.medias.codec.MxAvcDecoder;
import com.dylan.medias.codec.MxMp3Decoder;
import com.dylan.medias.stream.NativeMethod;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class PlayerActivity extends Activity implements Callback, Runnable, OnClickListener {
	
	private SurfaceView mSurfaceView = null;
	private ProgressBar mProgressBar = null;
	private String mRtspUrl = null;
	private boolean mTcpOnly = true;
	private boolean mAutoPlay = true;
	private boolean mNoDelay = false;
	private boolean mAutoRetry = false;
	private Thread mPlayThread = null;
	private boolean mWillQuit = false;
	private long mNativeHandle = 0;
	private View mBlackView = null;
	private boolean mStarted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		
		mRtspUrl = getIntent().getStringExtra("url");
		mTcpOnly = getIntent().getBooleanExtra("tcpOnly", true);
		mAutoPlay = getIntent().getBooleanExtra("autoPlay", true);
		mNoDelay = getIntent().getBooleanExtra("noDelay", false);
		mAutoRetry = getIntent().getBooleanExtra("autoRetry", false);
		int playImageResId = getIntent().getIntExtra("playImageResId", R.drawable.player_button_play);
		if (mRtspUrl == null || mRtspUrl.isEmpty()) {
			finish();
			return;
		}		
		
		RelativeLayout rootLayout = new RelativeLayout(this);
		rootLayout.setBackgroundColor(0xff000000);
		mSurfaceView = new SurfaceView(this);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		rootLayout.addView(mSurfaceView, layoutParams);
		
		mBlackView = new View(this);
		mBlackView.setBackgroundColor(0xff000000);
		rootLayout.addView(mBlackView);

		mProgressBar = new ProgressBar(this);
		mProgressBar.setIndeterminate(true);
		layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		rootLayout.addView(mProgressBar, layoutParams);	
		
		if (!mAutoPlay) {
			ImageButton play = new ImageButton(this);
			play.setImageResource(playImageResId);
			play.setBackgroundColor(0x00000000);
			play.setOnClickListener(this);
			rootLayout.addView(play, layoutParams);
			mProgressBar.setVisibility(View.GONE);
			mStarted = false;
		} else {
			mBlackView.setVisibility(View.GONE);
			mStarted = true;
		}

		TextView back = new TextView(this);
		back.setText("关闭");
		back.setTextColor(0xffffffff);
		back.setTextSize(16.f);
		back.setClickable(true);
		back.setOnClickListener(this);
		int padding = dip2px(this, 10);
		back.setPadding(padding, padding, padding, padding);
		layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rootLayout.addView(back, layoutParams);
		
		layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		setContentView(rootLayout, layoutParams);		
		
		mSurfaceView.getHolder().addCallback(this);
		mPlayThread = new Thread(this);
		mPlayThread.setPriority(Thread.MAX_PRIORITY);
		mPlayThread.start();
	}
	@Override
	protected void onPause() {
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
		} catch (Exception e) {
		}		
		setResult(RESULT_OK);
		finish();
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		if (mNativeHandle != 0) {
			NativeMethod.native_close(mNativeHandle);
			mNativeHandle = 0;
		}
		super.onDestroy();
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

	@Override
	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
		if (!runIn() && !mWillQuit) {
			runOnUiThread(new Runnable() {				
				@Override
				public void run() {
					Toast.makeText(PlayerActivity.this, "播放失败！", Toast.LENGTH_SHORT).show();
					Intent data = new Intent();
					data.putExtra("reason", "播放失败！");
					setResult(RESULT_CANCELED, data);
					finish();
				}
			});
		}
		mNativeHandle = 0;
	}

    @SuppressWarnings("unused")
	private boolean runIn() {
        boolean result = true;
        boolean first = true;
    	do {
    		if (mNativeHandle != 0) {
    			NativeMethod.native_interrupt(mNativeHandle);
    			NativeMethod.native_close(mNativeHandle);
    			mNativeHandle = 0;
    		}
        	mNativeHandle = NativeMethod.native_init();
            if (mNativeHandle == 0) {
            	if (first) {
	            	Log.e("dylan", "Open native handle failed.");
	            	return false;
            	} else {
            		sleep(1000);
            		continue;
            	}
            }
            if (!NativeMethod.native_open(mNativeHandle, mRtspUrl, mTcpOnly)) {
                NativeMethod.native_close(mNativeHandle);
                mNativeHandle = 0;
            	if (first) {
	            	Log.e("dylan", "Open rtsp stream failed.");
	            	return false;
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
			VideoThread videoThread = new VideoThread();
			AudioThread audioThread = new AudioThread();
            if (!videoThread.init()) {
            	videoThread.reset();
				if (first) {
					Log.e("dylan", "Not found video track in stream.");
					return false;
				} else {
					sleep(1000);
					continue;
				}
			}
			if (!audioThread.init()) {
				audioThread.reset();
			}
            if (first) {
                runOnUiThread(new Runnable() {
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
            videoThread.start();
            audioThread.start();
            while (!mWillQuit) {
                byte[] data = NativeMethod.native_read(mNativeHandle);
                if (data == null || data.length < 12) {
                	if (!mAutoRetry) result = false;
                	break;
                }
				int header = lhInt(data, 0);
				int index = header & 0x00ffffff;
                videoThread.append(data);
				audioThread.append(data);
				Log.i("dylan", "buffer status: v=" + videoThread.bufferSize() + ", a=" + audioThread.bufferSize());
            }
            videoThread.reset();
            audioThread.reset();
            NativeMethod.native_close(mNativeHandle);
            mNativeHandle = 0;
		} while (mAutoRetry && !mWillQuit);
        return result;
	}
	private class VideoThread extends Thread implements MxAvcDecoder.Callback {
		private MxAvcDecoder videoDecoder = null;
		private int videoIndex = 0;
		private long beginTimeStamp = -1;
		private long beginTickCount = -1;
		private long latestTimeStamp = -1;
		private LinkedBlockingQueue<byte[]> allFrames = new LinkedBlockingQueue<>();
		private boolean willQuit = false;
		public boolean init() {
			videoIndex = NativeMethod.native_videoIndex(mNativeHandle);
			int audioCodec = NativeMethod.native_audioCodec(mNativeHandle);
			if (videoIndex == -1) return false;
			videoDecoder = new MxAvcDecoder(this);
			if (!videoDecoder.startup(mSurfaceView.getHolder().getSurface())) return false;
			return true;
		}
		public void append(byte[] data) {
			int header = lhInt(data, 0);
			int index = header & 0x00ffffff;
			try {
				if (index == videoIndex && !allFrames.offer(data)) Log.e("dylan", "video buffer is full");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		public int bufferSize() {
			return allFrames.size();
		}
		public void reset() {
			willQuit = true;
			try {
				interrupt();
				join();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (videoDecoder != null) videoDecoder.shutdown();
			videoDecoder = null;
		}
		@Override
		public void run() {
			while (!willQuit) {
				byte[] data;
				try {
					data = allFrames.poll(10, TimeUnit.MILLISECONDS);
					if (data == null) continue;
					videoDecoder.decodeNals(data, 12, data.length - 12, 0);
					if (mNoDelay) continue;
					long timeStamp = lhLong(data, 4);
					latestTimeStamp = timeStamp;
					if (beginTimeStamp == -1 || Math.abs(timeStamp - latestTimeStamp) > (1000 * 300)) {
						Log.w("dylan", "adjust reference time, add: " + (timeStamp - latestTimeStamp));
						beginTimeStamp = timeStamp;
						beginTickCount = System.currentTimeMillis();
					}
					int delay = (int) ((timeStamp - beginTimeStamp) - (System.currentTimeMillis() - beginTickCount));
					if (delay > 0) sleep(delay);
				} catch (Exception ex) {
					continue;
				}
			}
		}
		@Override
		public void onVideoFormat(final int width, final int height, int colorFormat) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.i("dylan", "w=" + width + ", h=" + height);
					int w = mSurfaceView.getWidth();
					int h = mSurfaceView.getHeight();
					LayoutParams layoutParams = (LayoutParams) mSurfaceView.getLayoutParams();
					if (h > w * height / width) {
						layoutParams.height = w * height / width;
					} else {
						layoutParams.width = h * width / height;
					}
					mSurfaceView.requestLayout();
				}
			});
		}
		@Override
		public void onVideoFrame(byte[] datas, int offset, int length) {
		}
	}
	private class AudioThread extends Thread implements MxAacDecoder.Callback, MxMp3Decoder.Callback {
		private MxMp3Decoder mp3Decoder = null;
		private MxAacDecoder aacDecoder = null;
		private int audioIndex = 0;
		private LinkedBlockingQueue<byte[]> allFrames = new LinkedBlockingQueue<>();
		private boolean willQuit = false;
		private boolean isAdts = false;

		public boolean init() {
			audioIndex = NativeMethod.native_audioIndex(mNativeHandle);
			int audioCodec = NativeMethod.native_audioCodec(mNativeHandle);
			if (audioCodec == NativeMethod.Codec_Aac) {
				aacDecoder = new MxAacDecoder(this);
				byte[] data = NativeMethod.native_audioExtraData(mNativeHandle);
				if (data == null || data.length < 2) isAdts = true;
				return (isAdts && aacDecoder.startup()) || (!isAdts && aacDecoder.startup(data, 0, data.length));
			} else if (audioCodec == NativeMethod.Codec_Mp3) {
				mp3Decoder = new MxMp3Decoder(this);
				return mp3Decoder.startup();
			}
			return false;
		}
		public void append(byte[] data) {
			if (aacDecoder == null && mp3Decoder == null) return;
			int header = lhInt(data, 0);
			int index = header & 0x00ffffff;
			try {
				if (index == audioIndex && !allFrames.offer(data)) Log.e("dylan", "audio buffer is full");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		public int bufferSize() {
			return allFrames.size();
		}
		public void reset() {
			willQuit = true;
			try {
				interrupt();
				join();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (aacDecoder != null) aacDecoder.shutdown();
			if (mp3Decoder != null) mp3Decoder.shutdown();
			if (audioPlayout != null) audioPlayout.close();
			mp3Decoder = null;
			aacDecoder = null;
			audioPlayout = null;
		}

		@Override
		public void run() {
			while (!willQuit) {
				byte[] data;
				try {
					data = allFrames.poll(10, TimeUnit.MILLISECONDS);
					if (data == null) continue;
					if ((mNoDelay && allFrames.size() > 20) || (mNoDelay && allFrames.size() > 100)) {
						while (allFrames.poll() != null);
						Log.w("dylan", "drop some audio frame.");
					}
					if (aacDecoder != null && isAdts) {
						aacDecoder.decodeAdtsFrame(data, 12, data.length - 12);
					} else if (aacDecoder != null && !isAdts) {
						aacDecoder.decodeAFrame(data, 12, data.length - 12);
					} else if (mp3Decoder != null) {
						mp3Decoder.decodeAFrame(data, 12, data.length - 12);
					}
				} catch (Exception ex) {
					continue;
				}
			}
		}

		private MxAudioPlayout audioPlayout = null;
		@Override
		public void onAudioFormat(int channel, int sampleRate, int bitWidth) {
			audioPlayout = new MxAudioPlayout();
			audioPlayout.open(sampleRate, channel, bitWidth);
		}
		@Override
		public void onAudioBlock(byte[] datas, int offset, int length) {
			if (!mStarted) return;
			if (audioPlayout != null) audioPlayout.playAudioBlock(datas, offset, length);
		}
	}
	public static void sleep(int ms) {
		try { Thread.sleep(ms); } catch (Exception e) { }
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
	
	@Override
	public void onClick(View v) {
		if (v instanceof ImageButton) {
			if (mProgressBar != null) {
				mProgressBar.setVisibility(View.VISIBLE);
			}
			mBlackView.setVisibility(View.GONE);
			v.setVisibility(View.GONE);
			mStarted = true;
		} else {
			finish();
		}
	}
}
