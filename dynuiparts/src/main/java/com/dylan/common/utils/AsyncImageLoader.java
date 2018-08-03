package com.dylan.common.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.dylan.common.digest.MD5;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncImageLoader {
	public static interface ImageCallback {
		public void onLoaded(Bitmap bitmap, String url, Object userdata);

		public void onError(Exception e, String url, Object userdata);
	}

	private static final AsyncImageLoader mDefault = new AsyncImageLoader();

	public static final AsyncImageLoader theDefault() {
		return mDefault;
	}

	public static String[] cachePaths(Context context) {
		String cache = Environment.getDownloadCacheDirectory().getAbsolutePath() + "/image/";
		String data = null;
		try {
			String state = Environment.getExternalStorageState();
			if (state.equals(Environment.MEDIA_MOUNTED)) {
				String external = Environment.getExternalStorageDirectory().getAbsolutePath();
				PackageManager pm = context.getPackageManager();
				PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
				external += "/android/data/";
				external += info.packageName;
				external += "/image/";
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

	private static final String TAG = "AsyncImageLoader";

	private class LoaderTask {
		public LoaderTask(Object userdata, ImageCallback callback) {
			mUserdata = userdata;
			mCallback = callback;
		}

		Object mUserdata;
		ImageCallback mCallback;
	}

	private ExecutorService mExecutor = null;
	private HashMap<String, SoftReference<Bitmap>> mImages = new HashMap<String, SoftReference<Bitmap>>();
	private HashMap<String, ArrayList<LoaderTask>> mTasks = new HashMap<String, ArrayList<LoaderTask>>();
	private Handler mHandler = null;
	private int mSampleSize = 1;
	private int mLimitSize = 0;

	public AsyncImageLoader() {
		mHandler = new Handler(Looper.getMainLooper());
		mExecutor = Executors.newSingleThreadScheduledExecutor();
	}

	public AsyncImageLoader(int threadCount) {
		mHandler = new Handler(Looper.getMainLooper());
		mExecutor = Executors.newFixedThreadPool(threadCount);
	}

	public AsyncImageLoader(int sampleSize, int threadCount) {
		mHandler = new Handler(Looper.getMainLooper());
		mExecutor = Executors.newFixedThreadPool(threadCount);
		mSampleSize = sampleSize;
	}

	public AsyncImageLoader(int limitSize, int sampleSize, int threadCount) {
		mHandler = new Handler(Looper.getMainLooper());
		mExecutor = Executors.newFixedThreadPool(threadCount);
		mLimitSize = limitSize;
		mSampleSize = sampleSize;
	}

	public void setLimitWidth(int limitSize) {
		mLimitSize = limitSize;
	}

	public void setSampleSize(int sampleSize) {
		mSampleSize = sampleSize;
	}

	public Bitmap loadCache(Context context, String imageUrl, ImageCallback callback) {
		return loadCache(context, imageUrl, null, callback);
	}

	public Bitmap loadCache(Context context, String imageUrl, Object userdata, ImageCallback callback) {
		if (mImages.containsKey(imageUrl)) {
			SoftReference<Bitmap> softReference = mImages.get(imageUrl);
			Bitmap bitmap = softReference.get();
			if (bitmap != null)
				return bitmap;
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
			mExecutor.execute(new LoadImageTask(imageUrl, context, this, mHandler, mSampleSize, mLimitSize, true));
		}
		return null;
	}

	public void loadDrawable(Context context, String imageUrl, ImageCallback callback) {
		loadDrawable(context, imageUrl, true, null, callback);
	}

	public void loadDrawable(Context context, String imageUrl, Object userdata, ImageCallback callback) {
		loadDrawable(context, imageUrl, true, userdata, callback);
	}

	public void loadDrawable(Context context, String imageUrl, boolean useCache, Object userdata, ImageCallback callback) {
		if (callback == null)
			return;
		if (useCache && mImages.containsKey(imageUrl)) {
			SoftReference<Bitmap> softReference = mImages.get(imageUrl);
			Bitmap bitmap = softReference.get();
			if (bitmap != null) {
				callback.onLoaded(bitmap, imageUrl, userdata);
				return;
			}
		}
		if (mTasks.containsKey(imageUrl)) {
			ArrayList<LoaderTask> tasks = mTasks.get(imageUrl);
			tasks.add(new LoaderTask(userdata, callback));
		} else {
			ArrayList<LoaderTask> tasks = new ArrayList<LoaderTask>();
			tasks.add(new LoaderTask(userdata, callback));
			mTasks.put(imageUrl, tasks);
			mExecutor.execute(new LoadImageTask(imageUrl, context, this, mHandler, mSampleSize, mLimitSize, useCache));
		}
	}

	public void shutdown() {
		if (mExecutor != null)
			mExecutor.shutdownNow();
		if (mImages != null)
			mImages.clear();
		if (mTasks != null)
			mTasks.clear();
	}

	private void onLoaded(String imageUrl, Bitmap bitmap) {
		mImages.put(imageUrl, new SoftReference<Bitmap>(bitmap));
		if (!mTasks.containsKey(imageUrl))
			return;
		ArrayList<LoaderTask> tasks = mTasks.get(imageUrl);
		for (LoaderTask loaderTask : tasks) {
			loaderTask.mCallback.onLoaded(bitmap, imageUrl, loaderTask.mUserdata);
		}
		mTasks.remove(imageUrl);
	}

	private void onFailed(String imageUrl, Exception exception) {
		if (!mTasks.containsKey(imageUrl))
			return;
		ArrayList<LoaderTask> tasks = mTasks.get(imageUrl);
		for (LoaderTask loaderTask : tasks) {
			loaderTask.mCallback.onError(exception, imageUrl, loaderTask.mUserdata);
		}
		mTasks.remove(imageUrl);
	}

	private static final class LoadImageTask implements Runnable {
		private Handler mHandler;
		private AsyncImageLoader mLoader;
		private String mPath;
		private Context mContext;
		private int mSampleSize;
		private int mLimitSize = 0;
		private boolean mUseCache = true;

		public LoadImageTask(String imgPath, Context context, AsyncImageLoader loader, Handler handler, int sampleSize, int limitSize, boolean useCache) {
			Log.d(TAG, "start a task for load image:" + imgPath);
			this.mHandler = handler;
			this.mPath = imgPath;
			this.mLoader = loader;
			this.mContext = context;
			this.mSampleSize = sampleSize;
			this.mLimitSize = limitSize;
			this.mUseCache = useCache;
		}

		@Override
		public void run() {
			try {
				String[] localFile = null;
				if (mUseCache && mContext != null) {
					localFile = localFileForUrl(mContext, mPath);
					if (localFile.length > 0) {
						Bitmap drawable = loadLocalFile(localFile);
						if (drawable != null) {
							if (mLimitSize > 0) {
								int width = drawable.getWidth();
								int height = drawable.getHeight();
								if (width > mLimitSize || height > mLimitSize) {
									float scaleX = ((float) mLimitSize) / width;
									float scaleY = ((float) mLimitSize) / height;
									float scale = scaleX < scaleY ? scaleX : scaleY;
									Matrix matrix = new Matrix();
									matrix.postScale(scale, scale);
									drawable = Bitmap.createBitmap(drawable, 0, 0, width, height, matrix, true);
								}
							}
							final Bitmap image = drawable;
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									Log.d(TAG, "load image success from local:" + mPath);
									mLoader.onLoaded(mPath, image);
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
				ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inSampleSize = mSampleSize;
				Bitmap bmp = BitmapFactory.decodeStream(bais, null, opts);
				if (bmp != null) {
					if (mLimitSize > 0) {
						int width = bmp.getWidth();
						int height = bmp.getHeight();
						if (width > mLimitSize || height > mLimitSize) {
							float scaleX = ((float) mLimitSize) / width;
							float scaleY = ((float) mLimitSize) / height;
							float scale = scaleX < scaleY ? scaleX : scaleY;
							Matrix matrix = new Matrix();
							matrix.postScale(scale, scale);
							bmp = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
						}
					}
					final Bitmap image = bmp;
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							Log.d(TAG, "load image success:" + mPath);
							mLoader.onLoaded(mPath, image);
						}
					});
					if (mUseCache) {
						tryWriteFile(baos, localFile);
					}
				}
			} catch (final Exception e) {
				e.printStackTrace();
				Log.e(TAG, e.getMessage() + "", e);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						Log.d(TAG, "load image failed:" + mPath);
						mLoader.onFailed(mPath, e);
					}
				});
			} catch (OutOfMemoryError e) {
				Log.w("dylan", "mem: OutOfMemoryError");
				System.gc();
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mLoader.onFailed(mPath, new Exception("Memory insufficient."));
					}
				});
			}
		}

		private Bitmap loadLocalFile(String[] files) {
			for (String path : files) {
				FileInputStream fis = null;
				try {
					File file = new File(path);
					if (!file.exists())
						continue;
					fis = new FileInputStream(file);
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inSampleSize = mSampleSize;
					Bitmap res = BitmapFactory.decodeStream(fis, null, opts);
					if (res != null)
						return res;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						if (fis != null) fis.close();
					} catch (Exception ex) {
					}
				}
			}
			return null;
		}

		private String[] localFileForUrl(Context context, String url) {
			String cache = Environment.getDownloadCacheDirectory().getAbsolutePath() + "/image/";
			String data = null;
			try {
				String state = Environment.getExternalStorageState();
				if (state.equals(Environment.MEDIA_MOUNTED)) {
					String external = Environment.getExternalStorageDirectory().getAbsolutePath();
					PackageManager pm = context.getPackageManager();
					PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
					external += "/android/data/";
					external += info.packageName;
					external += "/image/";
					File file = new File(external);
					if (!file.exists())
						file.mkdirs();
					if (file.exists())
						data = external;
				}
			} catch (NameNotFoundException e) {
			}
			File file = new File(cache);
			if (!file.exists())
				file.mkdirs();
			String extName = null;
			int pos = url.lastIndexOf('.');
			if (pos >= 0)
				extName = url.substring(pos);
			String fileName = new MD5(url).asHex();
			if (data != null) {
				return new String[] { data + fileName + extName, cache + fileName + extName };
			} else {
				return new String[] { cache + fileName + extName };
			}
		}

		private void tryWriteFile(ByteArrayOutputStream baos, String[] localFile) {
			if (localFile == null || localFile.length < 1)
				return;
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
				} catch (Exception e) {
				}
			}
		}
	}

}
