package com.dylan.common.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dylan.common.data.JsonUtil;
import com.dylan.common.sketch.Dialogs;
import com.dylan.uiparts.R;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AutoUpdater {

	private static boolean mHasChecked = false;
	private static UploadThread mCheckThread = null;
	private static class UploadThread extends Thread {
		private boolean mCancelled = false;
		private String mCheckUrl = null;
		private Activity mActivity = null;
		public void cancel() {
			mCancelled = true;
			interrupt();
		}
		public UploadThread(Activity activity, String checkUrl) {
			mCheckUrl = checkUrl;
			mActivity = activity;
		}

		@Override
		public void run() {
			FileOutputStream fos = null;
			InputStream is = null;
			HttpURLConnection conn = null;
			try {
				URL url = new URL(mCheckUrl);
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3 * 1000);
				conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
				is = conn.getInputStream();
				if (is != null) {
					String result = getStringFromInputStream(is);
					JSONObject json = new JSONObject(result);
					int newCode = json.getInt("code");
					int oldCode = Utility.getVerCode(mActivity);
					if (newCode > oldCode) {
						final String notes = JsonUtil.textValue(json, "notes");
						final String version = JsonUtil.textValue(json, "version");
						final String file = json.getString("url");
						if (!isInterrupted()) {
							mActivity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									try {
										AutoUpdater updater = new AutoUpdater(mActivity, null);
										updater.notifyUpdate(version, notes, file);
										mHasChecked = true;
									} catch (Exception ex) {
										ex.printStackTrace();
										Toast.makeText(mActivity, "检查新版本失败！", Toast.LENGTH_SHORT).show();
									}
								}
							});
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(mActivity, "检查新版本失败！", Toast.LENGTH_SHORT).show();
					}
				});
			}
		}
	};
	public static void checkVersion(Activity context, String checkUrl) {
		try {
			abortUpdate();
			mCheckThread = new UploadThread(context, checkUrl);
			mCheckThread.start();
		} catch (Exception ex) {
			ex.printStackTrace();
			Toast.makeText(context, "检查新版本失败！", Toast.LENGTH_SHORT).show();
		}
	}
	public static void abortUpdate() {
		if (mCheckThread != null) {
			mCheckThread.cancel();
		}
		mCheckThread = null;
	}

	private static String getStringFromInputStream(InputStream is) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		while ((len = is.read(buffer)) != -1) {
			os.write(buffer, 0, len);
		}
		is.close();
		String state = os.toString();
		os.close();
		return state;
	}

	Context mContext = null;
	Dialog mDialog = null;
	OnFinishListener mListener = null;
	public AutoUpdater(Context context, OnFinishListener listener) {
		mContext = context;
		mListener = listener;
	}
	public void notifyUpdate(final String newVerName, String newNotes, final String url) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		final View view = inflater.inflate(R.layout.dialog_auto_update, null);
		TextView title = (TextView)view.findViewById(R.id.title);
		title.setText("发现新版本： v" + newVerName);
		TextView current = (TextView)view.findViewById(R.id.current);
		current.setText("当前版本： v" + Utility.getVerName(mContext));
		TextView notes = (TextView)view.findViewById(R.id.notes);
		if (newNotes.trim().length() > 0) {
			notes.setText("更新说明：\n" + newNotes);
			notes.setVisibility(View.VISIBLE);
		}
		Button update = (Button)view.findViewById(R.id.update);
		update.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				downloadApp(view, url, newVerName);
			}
		});
		Button dismiss = (Button)view.findViewById(R.id.dismiss);
		dismiss.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
				if (mListener != null)mListener.onNonUpdated();
			}
		});
		mDialog = new AlertDialog.Builder(mContext)
		 	.setView(view)
		 	.setCancelable(false)
			.create();
		mDialog.show();
	}
	
	private void downloadApp(final View view, String url, String verName) {
		final LinearLayout first = (LinearLayout)view.findViewById(R.id.first_panel);
		final LinearLayout second = (LinearLayout)view.findViewById(R.id.second_panel);
		int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);  
		int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);  
		first.measure(w, h);  
		int width = first.getMeasuredWidth();  
		TranslateAnimation ta1 = new TranslateAnimation(0, width + 50, 0, 0);
		TranslateAnimation ta2 = new TranslateAnimation(-1 * width - 50, 0, 0, 0);
		ta1.setDuration(700);
		ta1.setFillAfter(true);
		ta2.setDuration(700);
		ta2.setFillAfter(true);
		first.startAnimation(ta1);
		second.startAnimation(ta2);
		second.setVisibility(View.VISIBLE);
		
		final ProgressBar bar = (ProgressBar)view.findViewById(R.id.progressBar);
		TextView title = (TextView)view.findViewById(R.id.title);
		title.setText("正在升级到： v" + verName);
		TextView confirm = (TextView)view.findViewById(R.id.confirm);
		AlphaAnimation aa1 = new AlphaAnimation(1.0f, 0.0f);
		AlphaAnimation aa2 = new AlphaAnimation(0.0f, 1.0f);
		aa1.setDuration(700);
		aa2.setDuration(700);
		aa2.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				view.findViewById(R.id.dismiss).setVisibility(View.GONE);
			}
		});
		confirm.setAnimation(aa1);
		bar.setAnimation(aa2);
		confirm.setVisibility(View.INVISIBLE);
		bar.setVisibility(View.VISIBLE);
		
		final AsyncTask<String, Integer, String> task = new AsyncTask<String, Integer, String>() {
			private String mErrorMsg = "未知错误!";
			@Override
			protected String doInBackground(String... params) {
				FileOutputStream fos = null;
				InputStream is = null;
				HttpURLConnection conn = null;
	            try {
					URL url = new URL(params[0]);
					conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(3 * 1000);
					conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
					is = conn.getInputStream();
					String filePath = null;
	                if (is != null) {
						long length = conn.getContentLength();
	                	File path = Environment.getExternalStorageDirectory();
	                    File file = new File(path, getPackageName(mContext) + ".apk");
						fos = new FileOutputStream(file);
	                    byte[] buf = new byte[10240];
	                    int ch = -1;
						long count = 0;
	                    while ((ch = is.read(buf)) != -1) {
							fos.write(buf, 0, ch);
	                        count += ch;  
	                        if (length > 0) {  
	                        	publishProgress(new Integer[]{(int)(count * 100 / length)});
	                        }  
	    	            	if (isCancelled())return null;
	                    }  
	                    filePath = file.getPath();
	                }  
	                fos.flush();
	                return filePath;
	            } catch (Exception e) {  
	                e.printStackTrace();  
	            } finally {
					try {
						if (fos != null) {
							fos.close();
						}
						if (is != null) {
							is.close();
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				return null;
			}
			@Override
			protected void onProgressUpdate(Integer... progress) {
				bar.setProgress(progress[0]);
			}
			@Override
			protected void onPostExecute(String path) {
				if (isCancelled())return;
				mDialog.dismiss();
				if (path == null) {
					Dialogs.showMessage(mContext, "软件更新", mErrorMsg, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (mListener != null)mListener.onNonUpdated();
						}
					});
				} else {
					Intent intent = new Intent(Intent.ACTION_VIEW);  
	                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				    intent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");  
				    mContext.startActivity(intent);  
				    if (mListener != null)mListener.onDidUpdated();
				}
			}
		};
		Button cancel = (Button)view.findViewById(R.id.cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				task.cancel(true);
				mDialog.dismiss();
				if (mListener != null)mListener.onNonUpdated();
			}
		});
		task.execute(new String[]{url});
	}
	private static String getPackageName(Context context) {  
        try {  
        	return context.getPackageName();
        } catch (Exception e) {  
            e.printStackTrace();
        }  
        return "com.unknown.package";  
    }
	
	public interface OnFinishListener {
		void onDidUpdated();
		void onNonUpdated();
	}
}
