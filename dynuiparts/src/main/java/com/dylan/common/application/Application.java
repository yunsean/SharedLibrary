package com.dylan.common.application;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.widget.TextView;

import com.dylan.common.utils.LoadIndicator;
import com.dylan.common.utils.Utility;
import com.dylan.uiparts.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Application extends android.app.Application {
	private static Application mContext = null;
	public static Application context() {
		return mContext;
	}
	@Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
	}

	private Typeface mTypeface = null;
	public void setFontFamily(String font) {
		AssetManager mgr = getApplicationContext().getAssets();
		mTypeface = Typeface.createFromAsset(mgr, font);
	}
	public Typeface getTypeface() {
		return mTypeface;
	}

	private LoadIndicator.AnimationMode mLoadingAnimationMode = LoadIndicator.AnimationMode.Alpha;
	private int mLoadingResId = R.drawable.net_loading;
	private int mNetErrorResId = R.drawable.net_error;
	private int mLoadingBgResId = 0;
	public void setLoadAnimation(LoadIndicator.AnimationMode mode) {
		mLoadingAnimationMode = mode;
	}
	public LoadIndicator.AnimationMode getLoadAnimation() {
		return mLoadingAnimationMode;
	}
	public void setLoadingIcon(int resId) {
		mLoadingResId = resId;
	}
	public int getLoadingIcon() {
		return mLoadingResId;
	}
	public void setNetErrorIcon(int resId) {
		mNetErrorResId = resId;
	}
	public int getNetErrorIcon() {
		return mNetErrorResId;
	}
	public void setLoadingBackground(int resId) {
		mLoadingBgResId = resId;
	}
	public int getLoadingBackground() {
		return mLoadingBgResId;
	}


	private int mToastGravity = Gravity.BOTTOM;
	private int mToastOffsetX = 0;
	private int mToastOffsetY = -1;
	public void setToastGravity(int gravity) {
		mToastGravity = gravity;
	}
	public int getToastGravity() {
		return mToastGravity; 
	}
	public void setToastOffsetX(int offsetX) {
		mToastOffsetX = offsetX;
	}
	public int getToastOffsetX() {
		return mToastOffsetX; 
	}
	public void setToastOffsetY(int offsetY) {
		mToastOffsetY = offsetY;
	}
	public int getToastOffsetY() {
		if (mToastOffsetY == -1 && mToastGravity == Gravity.BOTTOM) {
			mToastOffsetY = Utility.dip2px(this, 50);
		}
		return mToastOffsetY; 
	}
	
	public void setFaceSize(int size) {
		mSmallFaceSize = size;
		mBigFaceSize = size * 2;
	}
	public void setFaceSize(int smallSize, int bigSize) {
		mSmallFaceSize = smallSize;
		mBigFaceSize = bigSize;
	}

	private class EmotionItem {
		public EmotionItem(String filename) {
			this.filename = filename;
		}
		public String filename = null;
		public Bitmap bigImage = null;
		public Bitmap smallImage = null;
	}
	private int mBigFaceSize = 128;
	private int mSmallFaceSize = 64;
	private HashMap<String, EmotionItem> mFaces = null;
	private JSONArray readFaceJson(InputStream is) throws Exception {
        int size = is.available();    
        byte[] buffer = new byte[size];  
        is.read(buffer);  
        is.close();    
        String text = new String(buffer, "UTF8");
        return new JSONArray(text);
	}
	public Bitmap readFaceIcon(String fileName, int size) {  
	      Bitmap image = null;  
	      AssetManager am = getResources().getAssets();  
	      try {  
	          InputStream is = am.open(fileName);  
	          image = BitmapFactory.decodeStream(is);  
	          is.close();
	          image = Bitmap.createScaledBitmap(image, size, size, true);
	      } catch (IOException e) {  
	          e.printStackTrace();  
	      }  
	      return image;
	} 
	public void loadFace(String assetFile) {
		if (mFaces != null)return;
		try {
			InputStream is = getAssets().open(assetFile); 
			loadFace(is);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public void loadFaces() {
		if (mFaces != null)return;
		try {
			InputStream is = getAssets().open("face.json"); 
			loadFace(is);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public void loadFace(InputStream is) {
		if (mFaces != null)return;
		mFaces = new HashMap<String, EmotionItem>();
		try { 
			JSONArray faces = readFaceJson(is);
			for (int i = 0; i < faces.length(); i++) {
				JSONObject face = faces.getJSONObject(i);
				String phrase = face.getString("phrase");
				String url = face.getString("url");
				EmotionItem item = new EmotionItem("face/" + url);
				mFaces.put(phrase, item);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public String[] getFaceKeys(boolean autoLoad) {
		if (mFaces != null)return mFaces.keySet().toArray(new String[mFaces.size()]);
		else if (!autoLoad)return null;
		loadFaces();
		return mFaces.keySet().toArray(new String[mFaces.size()]);
	}
	public String[] getFaceKeys() {
		if (mFaces != null)return mFaces.keySet().toArray(new String[mFaces.size()]);
		else return null;
	}
	public Bitmap getFace(String key, boolean bigImage) {
		try {
			if (mFaces == null) loadFaces();
			if (!mFaces.containsKey(key))return null;
			EmotionItem item = mFaces.get(key);
			if (bigImage) {
				if (item.bigImage == null) {
					item.bigImage = readFaceIcon(item.filename, mBigFaceSize);
				}
				return item.bigImage;
			} else {
				if (item.smallImage == null) {
					item.smallImage = readFaceIcon(item.filename, mSmallFaceSize);
				}
				return item.smallImage;
			}
		} catch (Exception ex) {
			return null;
		}
	}
	public Bitmap getFace(String key) {
		return getFace(key, false);
	}
	public void showRichText(TextView editText, String message) {		
		editText.setText("");
		int begin = 0;
		for (int i = 0; i < message.length(); i++) {
			char ch = message.charAt(i);
			if (ch != '[') continue;
			if (begin < i) {
				editText.append(message, begin, i);
				begin = i;
			}
			while (++i < message.length()) {
				ch = message.charAt(i);
				if (ch == '[') {
					editText.append(message, begin, i);
					begin = i;
				} else if (ch == ']') {
					String phrase = message.substring(begin, i + 1);
					Bitmap icon = getFace(phrase);
					if (icon == null) {
						editText.append(message, begin, i + 1);
					} else {
						ImageSpan imageSpan = new ImageSpan(this, icon);
						SpannableString spannableString = new SpannableString(phrase);
						spannableString.setSpan(imageSpan, 0, phrase.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						editText.append(spannableString);
					}
					begin = i + 1;
					break;
				}
			}
		}
		if (begin < message.length()) {
			editText.append(message, begin, message.length());			
		}
	}

	private Map<String, Object> sharedDatas = new HashMap<>();
	public void addSharedData(String key, Object value) {
		sharedDatas.put(key, value);
	}
	public Object getSharedData(String key) {
		return sharedDatas.get(key);
	}
}
