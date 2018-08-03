package com.dylan.common.application;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.dylan.common.sketch.Dialogs;
import com.dylan.common.utils.Utility;
import com.dylan.uiparts.R;

public class SplashActivity extends AppCompatActivity {

	protected boolean needCheckNetwork() {
		return true;
	}
	
	protected Object doInBackgroundWhenShowSplash() {		
		return null;
	}
	protected void shouldGoHomeAfterSplash(Object resultOfInBackground) {		
	}
	protected void shouldGoGuideAfterSplash(Object resultOfInBackground) {		
	}	
	protected void setShowSplashTime(int ms) {
		mWaitTimeMs = ms;
	}
	protected void setSplashImage(int resId) {
		((ImageView)findViewById(R.id.app_splash_image)).setImageResource(resId);
	}
	protected void setSplashImage(int resId, ImageView.ScaleType scaleType) {
		((ImageView)findViewById(R.id.app_splash_image)).setImageResource(resId);
		((ImageView)findViewById(R.id.app_splash_image)).setScaleType(scaleType);
	}
	protected void setBackgroundColor(int color) {
		((ImageView)findViewById(R.id.app_splash_image)).setBackgroundColor(color);
	}
	public static void setIsFirstStartup(Context ctx, boolean isFirst) {
		SharedPreferences preferences = ctx.getSharedPreferences(SHAREDPREFERENCES_NAME, MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean("isFirstIn", isFirst);
		editor.putInt("lastGuideVersion", Utility.getVerCode(ctx));
		editor.commit();
	}
	
	private int mWaitTimeMs = 1000;
	private static final String SHAREDPREFERENCES_NAME = "first_pref";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_splash);
		if (needCheckNetwork() && !Utility.isNetworkAvailable(SplashActivity.this)) {
			new android.os.Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					Dialogs.showMessage(SplashActivity.this, "网络错误", "当前未检测到任何网络连接，是否立即进行网络设置？", "确定", "取消", new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent("android.settings.WIRELESS_SETTINGS");
							startActivityForResult(intent, 100);
							finish();
						}
					}, new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
				}
			}, 300);
		} else {
			showSplash();
		}
	}

	protected boolean shouleGoGuide() {
		SharedPreferences preferences = getSharedPreferences(SHAREDPREFERENCES_NAME, MODE_PRIVATE);
		int isFirstInVersion = preferences.getInt("lastGuideVersion", 0);
		return isFirstInVersion != Utility.getVerCode(this);
	}
    void showSplash() {
    	(new AsyncTask<Void, Void, Object>() {
    		@Override
    		protected void onPostExecute(Object result) {
				SharedPreferences preferences = getSharedPreferences(SHAREDPREFERENCES_NAME, MODE_PRIVATE);
				boolean isFirstIn = shouleGoGuide();
				if (isFirstIn)shouldGoGuideAfterSplash(result);
				else shouldGoHomeAfterSplash(result);
    		}
			@Override
			protected Object doInBackground(Void... params) {
				long start = SystemClock.uptimeMillis();
				Object mResult = null;
				mResult = doInBackgroundWhenShowSplash();
				long end = SystemClock.uptimeMillis();
				if (end - start < mWaitTimeMs) {
					try {
						Thread.sleep(end + mWaitTimeMs - start);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				return mResult;
			}
		}).execute();
    }
}
