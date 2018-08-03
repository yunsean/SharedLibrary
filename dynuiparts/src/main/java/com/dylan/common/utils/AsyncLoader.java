package com.dylan.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dylan.common.digest.MD5;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class AsyncLoader {
	public static interface Callback {
		public void onLoaded(String url, Object userdata, String cacheFile);
		public void onError(String url, Object userdata, Exception e);
	}

	private static final AsyncLoader mDefault = new AsyncLoader();

	public static final AsyncLoader theDefault() {
		return mDefault;
	}

	public static String[] cachePaths(Context context) {
		String cache = Environment.getDownloadCacheDirectory().getAbsolutePath() + "/cache/";
		String data = null;
		try {
			String state = Environment.getExternalStorageState();
			if (state.equals(Environment.MEDIA_MOUNTED)) {
				String external = Environment.getExternalStorageDirectory().getAbsolutePath();
				PackageManager pm = context.getPackageManager();
				PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
				external += "/android/data/";
				external += info.packageName;
				external += "/cache/";
				File file = new File(external);
				if (!file.exists())
					file.mkdirs();
				if (file.exists())
					data = external;
			}
		} catch (NameNotFoundException e) {
		}
		if (data != null) {
			return new String[] { data, cache };
		} else {
			return new String[] { cache };
		}
	}

	private static final String TAG = "AsyncLoader";

	private class LoaderTask {
		public LoaderTask(Object userdata, Callback callback) {
			mUserdata = userdata;
			mCallback = callback;
		}

		Object mUserdata;
		Callback mCallback;
	}

	private ExecutorService mExecutor = null;
	private HashMap<String, String> mCachedFiles = new HashMap<String, String>();
	private HashMap<String, ArrayList<LoaderTask>> mTasks = new HashMap<String, ArrayList<LoaderTask>>();
	private Handler mHandler = null;

	public AsyncLoader() {
		mHandler = new Handler(Looper.getMainLooper());
		mExecutor = Executors.newSingleThreadScheduledExecutor();
	}

	public AsyncLoader(int threadCount) {
		mHandler = new Handler(Looper.getMainLooper());
		mExecutor = Executors.newFixedThreadPool(threadCount);
	}

	public AsyncLoader(int sampleSize, int threadCount) {
		mHandler = new Handler(Looper.getMainLooper());
		mExecutor = Executors.newFixedThreadPool(threadCount);
	}

	public String loadCache(Context context, String imageUrl, Callback callback) {
		return loadCache(context, imageUrl, null, callback);
	}

	public String loadCache(Context context, String imageUrl, Object userdata, Callback callback) {
		if (mCachedFiles.containsKey(imageUrl)) {
			return mCachedFiles.get(imageUrl);
		}
		if (callback == null)
			return null;
		if (mTasks.containsKey(imageUrl)) {
			ArrayList<LoaderTask> tasks = mTasks.get(imageUrl);
			tasks.add(new LoaderTask(userdata, callback));
		} else {
			ArrayList<LoaderTask> tasks = new ArrayList<LoaderTask>();
			tasks.add(new LoaderTask(userdata, callback));
			mTasks.put(imageUrl, tasks);
			mExecutor.execute(new LoadImageTask(imageUrl, context, this, mHandler));
		}
		return null;
	}

	public void loadFile(Context context, String url, Callback callback) {
		loadFile(context, url, true, null, callback);
	}
	public void loadFile(Context context, String url, Object userdata, Callback callback) {
		loadFile(context, url, true, userdata, callback);
	}
	public void loadFile(Context context, String url, boolean useCache, Object userdata, Callback callback) {
		if (callback == null)
			return;
		if (mCachedFiles.containsKey(url)) {
			String file = mCachedFiles.get(url);
			callback.onLoaded(url, userdata, file);
			return;
		}
		if (mTasks.containsKey(url)) {
			ArrayList<LoaderTask> tasks = mTasks.get(url);
			tasks.add(new LoaderTask(userdata, callback));
		} else {
			ArrayList<LoaderTask> tasks = new ArrayList<LoaderTask>();
			tasks.add(new LoaderTask(userdata, callback));
			mTasks.put(url, tasks);
			mExecutor.execute(new LoadImageTask(url, context, this, mHandler));
		}
	}

	public void shutdown() {
		if (mExecutor != null)
			mExecutor.shutdownNow();
		if (mCachedFiles != null)
			mCachedFiles.clear();
		if (mTasks != null)
			mTasks.clear();
	}

	private void onLoaded(String imageUrl, String cacheFile) {
		mCachedFiles.put(imageUrl, cacheFile);
		if (!mTasks.containsKey(imageUrl))
			return;
		ArrayList<LoaderTask> tasks = mTasks.get(imageUrl);
		for (LoaderTask loaderTask : tasks) {
			loaderTask.mCallback.onLoaded(imageUrl, loaderTask.mUserdata, cacheFile);
		}
		mTasks.remove(imageUrl);
	}

	private void onFailed(String imageUrl, Exception exception) {
		if (!mTasks.containsKey(imageUrl)) {
			return;
		}
		ArrayList<LoaderTask> tasks = mTasks.get(imageUrl);
		for (LoaderTask loaderTask : tasks) {
			loaderTask.mCallback.onError(imageUrl, loaderTask.mUserdata, exception);
		}
		mTasks.remove(imageUrl);
	}

	private static final class LoadImageTask implements Runnable {
		private Handler mHandler;
		private AsyncLoader mLoader;
		private String mPath;
		private Context mContext;

		public LoadImageTask(String imgPath, Context context, AsyncLoader loader, Handler handler) {
			Log.d(TAG, "start a task for load image:" + imgPath);
			this.mHandler = handler;
			this.mPath = imgPath;
			this.mLoader = loader;
			this.mContext = context;
		}

		@Override
		public void run() {
			try {
				String[] localFile = null;
				if (mContext != null) {
					localFile = localFileForUrl(mContext, mPath);
					if (localFile.length > 0) {
						final String cacheFile = loadLocalFile(localFile);
						if (cacheFile != null) {
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									mLoader.onLoaded(mPath, cacheFile);
								}
							});
							return;
						}
					}
				}

				URL url = new URL(mPath);
				URLConnection conn = url.openConnection();
				conn.connect();
				InputStream is = conn.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] cache = new byte[1024 * 10];
				int len = 0;
				while ((len = is.read(cache)) != -1) {
					baos.write(cache, 0, len);
				}
				final String cacheFile = tryWriteFile(baos, localFile);
				if (cacheFile != null) {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							Log.d(TAG, "load image success:" + mPath);
							mLoader.onLoaded(mPath, cacheFile);
						}
					});
					return;
				}
				throw new Exception("Write cache file failed.");
			} catch (final Exception e) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mLoader.onFailed(mPath, e);
					}
				});
			} catch (OutOfMemoryError e) {
				System.gc();
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mLoader.onFailed(mPath, new Exception("Memory insufficient."));
					}
				});
			}
		}

		private String loadLocalFile(String[] files) {
			for (String path : files) {
				try {
					File file = new File(path);
					if (!file.exists()) {
						continue;
					}
					return file.getAbsolutePath();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		private String[] localFileForUrl(Context context, String url) {
			String cache = Environment.getDownloadCacheDirectory().getAbsolutePath() + "/cache/";
			String data = null;
			try {
				String state = Environment.getExternalStorageState();
				if (state.equals(Environment.MEDIA_MOUNTED)) {
					String external = Environment.getExternalStorageDirectory().getAbsolutePath();
					PackageManager pm = context.getPackageManager();
					PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
					external += "/android/data/";
					external += info.packageName;
					external += "/cache/";
					File file = new File(external);
					if (!file.exists())
						file.mkdirs();
					if (file.exists())
						data = external;
				}
			} catch (NameNotFoundException e) {
			}
			File file = new File(cache);
			if (!file.exists()) {
				file.mkdirs();
			}
			String extName = null;
			int pos = url.lastIndexOf('.');
			if (pos >= 0) {
				extName = url.substring(pos);
			}
			String fileName = new MD5(url).asHex();
			if (data != null) {
				return new String[] { data + fileName + extName, cache + fileName + extName };
			} else {
				return new String[] { cache + fileName + extName };
			}
		}

		private String tryWriteFile(ByteArrayOutputStream baos, String[] localFile) {
			if (localFile == null || localFile.length < 1) {
				return null;
			}
			for (String path : localFile) {
				try {
					ByteArrayInputStream is = new ByteArrayInputStream(baos.toByteArray());
					File file = new File(path);
					file.createNewFile();
					FileOutputStream fos = new FileOutputStream(file);
					byte[] data = new byte[1024];
					int len = 0;
					while ((len = is.read(data)) != -1) {
						fos.write(data, 0, len);
					}
					fos.flush();
					fos.close();
					return path;
				} catch (Exception e) {
				}
			}
			return null;
		}
	}

}
