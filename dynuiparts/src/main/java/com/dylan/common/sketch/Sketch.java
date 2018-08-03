package com.dylan.common.sketch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dylan.common.data.JsonUtil;
import com.dylan.common.utils.AsyncImageLoader;
import com.dylan.common.utils.Utility;
import com.dylan.uiparts.imageview.CircleImageView;
import com.dylan.uiparts.imageview.NetImageView;
import com.dylan.uiparts.layout.TextLineInfo;

import org.json.JSONObject;

public final class Sketch {
	public static final void set_tag(View view, Object tag) {
		if (view != null) {
			view.setTag(tag);
		}
	}
	public static final void set_tag(View convertView, int resId, Object tag) {
		set_tag(convertView.findViewById(resId), tag);
	}
	public static final void set_tag(Activity activity, int resId, Object tag) {
		set_tag(activity.findViewById(resId), tag);
	}
	public static final void set_tag(Object tag, View...views) {
		for (int i = 0; i < views.length; i++) {
			set_tag(views[i], tag);			
		}
	}
	public static final void set_tag(View convertView, Object tag, int...resIds) {
		for (int i = 0; i < resIds.length; i++) {
			set_tag(convertView.findViewById(resIds[i]), tag);
		}
	}
	public static final void set_tag(Activity activity, Object tag, int...resIds) {
		for (int i = 0; i < resIds.length; i++) {
			set_tag(activity.findViewById(resIds[i]), tag);
		}
	}	
	
	public static final void set_value(View convertView, int resId, JSONObject json, String name, Object defaultValue) {
		try {
			if (json.has(name) && !json.isNull(name)) {
				View view = convertView.findViewById(resId);
				if (view instanceof TextView) ((TextView)view).setText(json.getString(name));
				else if (view instanceof NetImageView) ((NetImageView)view).setImage(json.getString(name));
				else if (view instanceof CircleImageView) ((CircleImageView)view).setImage(json.getString(name));
				else if (view instanceof Button) ((Button)view).setText(json.getString(name));
				else if (view instanceof CompoundButton) ((CompoundButton)view).setChecked(json.getBoolean(name));
				else if (view instanceof WebView) ((WebView)view).loadDataWithBaseURL(null, json.getString(name), "text/html", "utf-8", null);
			} else if (defaultValue != null){
				View view = convertView.findViewById(resId);
				if (view instanceof TextView) {
					((TextView)view).setText(defaultValue.toString());
				} else if (view instanceof NetImageView) {
					((NetImageView)view).setImage(defaultValue.toString());
				} else if (view instanceof CircleImageView) {
					((CircleImageView)view).setImage(defaultValue.toString());
				} else if (view instanceof Button) {
					((Button)view).setText(defaultValue.toString());
				} else if (view instanceof CompoundButton) {
					if (defaultValue instanceof String) {
						((CompoundButton)view).setText(defaultValue.toString());
					} else {
						((CompoundButton)view).setChecked(Boolean.valueOf(defaultValue.toString()));						
					}
				} else if (view instanceof WebView) {
					((WebView)view).loadDataWithBaseURL(null, defaultValue.toString(), "text/html", "utf-8", null);
				}
			}
		} catch (Exception e) {
		}
	}
	public static final void set_value(View convertView, int resId, Object value) {
		try {
			if (value != null){
				View view = convertView.findViewById(resId);
				if (view instanceof TextView) {
					((TextView)view).setText(value.toString());
				} else if (view instanceof NetImageView) {
					((NetImageView)view).setImage(value.toString());
				} else if (view instanceof CircleImageView) {
					((CircleImageView)view).setImage(value.toString());
				} else if (view instanceof Button) {
					((Button)view).setText(value.toString());
				} else if (view instanceof CompoundButton) {
					if (value instanceof String) {
						((CompoundButton)view).setText(value.toString());
					} else {
						((CompoundButton)view).setChecked(Boolean.valueOf(value.toString()));						
					}
				} else if (view instanceof WebView) {
					((WebView)view).loadDataWithBaseURL(null, value.toString(), "text/html", "utf-8", null);
				}
			}
		} catch (Exception e) {
		}
	}
	public static final void set_value(View convertView, int resId, JSONObject json, String name) {
		set_value(convertView, resId, json, name, null);
	}
	public static final void set_value(View parentView, JSONObject json, int[] resIds, String[] keys) {
		for (int i = 0; i < keys.length; i++) {
			set_value(parentView, resIds[i], json, keys[i]);
		}
	}

	public static final void set_tli(View tv, String text) {
		if (tv != null && tv instanceof TextLineInfo) {
			((TextLineInfo)tv).setText(text);
		}
	}
	public static final void set_tli(View textview, JSONObject json, String name) {
		set_tli(textview, JsonUtil.textValue(json, name));
	}
	public static final void set_tli(View textview, JSONObject json, String name, String def) {
		set_tli(textview, JsonUtil.textValue(json, name, def));
	}
	public static final void set_tli(View parent, int resId, String text) {
		set_tli(parent.findViewById(resId), text);
	}
	public static final void set_tli(Activity parent, int resId, String text) {
		set_tli(parent.findViewById(resId), text);
	}
	public static final void set_tli(View convertView, int resId, JSONObject json, String name) {
		set_tli(convertView.findViewById(resId), JsonUtil.textValue(json, name));
	}
	public static final void set_tli(Activity activity, int resId, JSONObject json, String name) {
		set_tli(activity.findViewById(resId), JsonUtil.textValue(json, name));
	}
	public static final void set_tli(View convertView, int resId, JSONObject json, String name, String defaultValue) {
		set_tli(convertView.findViewById(resId), JsonUtil.textValue(json, name, defaultValue));
	}
	public static final void set_tli(Activity activity, int resId, JSONObject json, String name, String defaultValue) {
		set_tli(activity.findViewById(resId), JsonUtil.textValue(json, name, defaultValue));
	}

	public static final String get_tli(View tv) {
		if (tv == null || !(tv instanceof TextLineInfo))return null;
		return ((TextLineInfo)tv).getText().toString();
	}
	public static final String get_tli(View tv, String def) {
		if (tv == null || !(tv instanceof TextLineInfo))return def;
		return ((TextLineInfo)tv).getText().toString();
	}
	public static final String get_tli(View parent, int resId) {
		return get_tv(parent.findViewById(resId));
	}
	public static final String get_tli(View parent, int resId, String def) {
		return get_tv(parent.findViewById(resId), def);
	}
	public static final String get_tli(Activity parent, int resId) {
		return get_tv(parent.findViewById(resId));
	}
	public static final String get_tli(Activity parent, int resId, String def) {
		return get_tv(parent.findViewById(resId), def);
	}

	public static final void set_tv(View tv, String text) {
		if (tv != null && tv instanceof TextView && text != null) {
			((TextView)tv).setText(text);
		}
	}
	public static final void set_tv(View parent, int resId, String text) {
		set_tv(parent.findViewById(resId), text);
	}
	public static final void set_tv(Activity parent, int resId, String text) {
		set_tv(parent.findViewById(resId), text);
	}
	public static final void set_tv(View textview, JSONObject json, String name) {
		set_tv(textview, JsonUtil.textValue(json, name));
	}
	public static final void set_tv(View textview, JSONObject json, String name, String def) {
		set_tv(textview, JsonUtil.textValue(json, name, def));
	}
	public static final void set_tv(View convertView, int resId, JSONObject json, String name) {
		set_tv(convertView.findViewById(resId), JsonUtil.textValue(json, name));
	}
	public static final void set_tv(Activity activity, int resId, JSONObject json, String name) {
		set_tv(activity.findViewById(resId), JsonUtil.textValue(json, name));
	}
	public static final void set_tv(View convertView, int resId, JSONObject json, String name, String defaultValue) {
		set_tv(convertView.findViewById(resId), JsonUtil.textValue(json, name, defaultValue));
	}
	public static final void set_tv(Activity activity, int resId, JSONObject json, String name, String defaultValue) {
		set_tv(activity.findViewById(resId), JsonUtil.textValue(json, name, defaultValue));
	}

	public static final void set_tvc(View tv, int color) {
		if (tv != null && tv instanceof TextView) {
			((TextView)tv).setTextColor(color);
		}
	}
	public static final void set_tvc(View parent, int resId, int color) {
		set_tvc(parent.findViewById(resId), color);
	}
	public static final void set_tvc(Activity parent, int resId, int color) {
		set_tvc(parent.findViewById(resId), color);
	}

	public static final void set_end(EditText et) {
		try {
			et.setSelection(et.getText().length());
		} catch (Exception ex) {
		}
	}
	public static final void set_end(View parent, int resId) {
		try {
			EditText et = (EditText)parent.findViewById(resId);
			et.setSelection(et.getText().length());
		} catch (Exception ex) {
		}
	}

	public static final void set_url(View web, String url) {
		if (web != null && web instanceof WebView) {
			((WebView)web).loadUrl(url);
		}
	}
	public static final void set_url(View parent, int resId, String url) {
		set_url(parent.findViewById(resId), url);
	}
	public static final void set_url(Activity parent, int resId, String url) {
		set_url(parent.findViewById(resId), url);
	}
	public static final void set_url(View convertView, int resId, JSONObject json, String name) {
		set_url(convertView.findViewById(resId), JsonUtil.textValue(json, name));
	}
	public static final void set_url(Activity activity, int resId, JSONObject json, String name) {
		set_url(activity.findViewById(resId), JsonUtil.textValue(json, name));
	}
	public static final void set_url(View convertView, int resId, JSONObject json, String name, String defaultUrl) {
		set_url(convertView.findViewById(resId), JsonUtil.textValue(json, name, defaultUrl));
	}
	public static final void set_url(Activity activity, int resId, JSONObject json, String name, String defaultUrl) {
		set_url(activity.findViewById(resId), JsonUtil.textValue(json, name, defaultUrl));
	}

	public static final void set_html(View web, String html) {
		if (web != null && web instanceof WebView) {
			((WebView)web).loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
		}
	}
	public static final void set_html(View parent, int resId, String html) {
		set_html(parent.findViewById(resId), html);
	}
	public static final void set_html(Activity parent, int resId, String html) {
		set_html(parent.findViewById(resId), html);
	}
	public static final void set_html(View convertView, int resId, JSONObject json, String name) {
		set_html(convertView.findViewById(resId), JsonUtil.textValue(json, name));
	}
	public static final void set_html(Activity activity, int resId, JSONObject json, String name) {
		set_html(activity.findViewById(resId), JsonUtil.textValue(json, name));
	}
	public static final void set_html(View convertView, int resId, JSONObject json, String name, String defaultHtml) {
		set_html(convertView.findViewById(resId), JsonUtil.textValue(json, name, defaultHtml));
	}
	public static final void set_html(Activity activity, int resId, JSONObject json, String name, String defaultHtml) {
		set_html(activity.findViewById(resId), JsonUtil.textValue(json, name, defaultHtml));
	}

	public static final String get_tv(View tv) {
		if (tv == null || !(tv instanceof TextView))return null;
		return ((TextView)tv).getText().toString();
	}
	public static final String get_tv(View tv, String def) {
		if (tv == null || !(tv instanceof TextView))return def;
		return ((TextView)tv).getText().toString();
	}
	public static final String get_tv(View parent, int resId) {
		return get_tv(parent.findViewById(resId));
	}
	public static final String get_tv(View parent, int resId, String def) {
		return get_tv(parent.findViewById(resId), def);
	}
	public static final String get_tv(Activity parent, int resId) {
		return get_tv(parent.findViewById(resId));
	}
	public static final String get_tv(Activity parent, int resId, String def) {
		return get_tv(parent.findViewById(resId), def);
	}

	public static final int get_tvi(View tv, int defaultValue) {
		if (tv == null || !(tv instanceof TextView))return defaultValue;
		try{
			return Integer.valueOf(((TextView)tv).getText().toString());
		} catch(Exception e) {			
			return defaultValue;
		}
	}
	public static final int get_tvi(View parent, int resId, int defaultValue) {
		return get_tvi(parent.findViewById(resId), defaultValue);
	}
	public static final int get_tvi(Activity parent, int resId, int defaultValue) {
		return get_tvi(parent.findViewById(resId), defaultValue);
	}

	public static final float get_tvf(View tv, float defaultValue) {
		if (tv == null || !(tv instanceof TextView))return defaultValue;
		try{
			return Float.valueOf(((TextView)tv).getText().toString());
		} catch(Exception e) {			
			return defaultValue;
		}
	}
	public static final float get_tvf(View parent, int resId, float defaultValue) {
		return get_tvf(parent.findViewById(resId), defaultValue);
	}
	public static final float get_tvf(Activity parent, int resId, float defaultValue) {
		return get_tvf(parent.findViewById(resId), defaultValue);
	}

	public static final void set_iv(View iv, int imageResId) {
		if (iv != null && iv instanceof ImageView) {
			((ImageView)iv).setImageResource(imageResId);
		}
	}
	public static final void set_iv(View iv, Drawable drawable) {
		if (iv != null && iv instanceof ImageView) {
			((ImageView)iv).setImageDrawable(drawable);
		}
	}
	public static final void set_iv(View iv, Bitmap bitmap) {
		if (iv != null && iv instanceof ImageView) {
			((ImageView)iv).setImageBitmap(bitmap);
		}
	}
	public static final void set_iv(View parent, int resId, int imageResId) {
		set_iv(parent.findViewById(resId), imageResId);
	}
	public static final void set_iv(Activity parent, int resId, int imageResId) {
		set_iv(parent.findViewById(resId), imageResId);
	}
	public static final void set_iv(View parent, int resId, Drawable drawable) {
		set_iv(parent.findViewById(resId), drawable);
	}
	public static final void set_iv(Activity parent, int resId, Drawable drawable) {
		set_iv(parent.findViewById(resId), drawable);
	}
	public static final void set_iv(View parent, int resId, Bitmap bitmap) {
		set_iv(parent.findViewById(resId), bitmap);
	}
	public static final void set_iv(Activity parent, int resId, Bitmap bitmap) {
		set_iv(parent.findViewById(resId), bitmap);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static final void set_bg(View iv, Drawable drawable) {
		if (iv == null)return;
		if (Utility.isJellyBeanOrLater()) {
			iv.setBackground(drawable);
		} else {
			iv.setBackgroundDrawable(drawable);
		}
	}
	public static final void set_bg(View iv, Bitmap bitmap) {
		BitmapDrawable bd = new BitmapDrawable(iv.getContext().getResources(), bitmap);
		set_bg(iv, bd);
	}
	public static final void set_bg(View iv, int imageResId) {
		if (iv != null) {
			iv.setBackgroundResource(imageResId);
		}
	}
	public static final void set_bgc(View iv, int color) {
		if (iv != null) {
			iv.setBackgroundColor(color);
		}
	}
	public static final void set_bg(View parent, int resId, int imageResId) {
		set_bg(parent.findViewById(resId), imageResId);
	}
	public static final void set_bg(Activity parent, int resId, int imageResId) {
		set_bg(parent.findViewById(resId), imageResId);
	}
	public static final void set_bgc(View parent, int resId, int color) {
		set_bgc(parent.findViewById(resId), color);
	}
	public static final void set_bgc(Activity parent, int resId, int color) {
		set_bgc(parent.findViewById(resId), color);
	}
	public static final void set_bg(View parent, int resId, Drawable drawable) {
		set_bg(parent.findViewById(resId), drawable);
	}
	public static final void set_bg(Activity parent, int resId, Drawable drawable) {
		set_bg(parent.findViewById(resId), drawable);
	}
	public static final void set_bg(View parent, int resId, Bitmap bitmap) {
		set_bg(parent.findViewById(resId), bitmap);
	}
	public static final void set_bg(Activity parent, int resId, Bitmap bitmap) {
		set_bg(parent.findViewById(resId), bitmap);
	}

	public static final void set_color(View view, int color) {
		if (view != null && view instanceof TextView) {
			((TextView)view).setTextColor(color);
		}
	}
	public static final void set_color(View parent, int resId, int color) {
		set_color(parent.findViewById(resId), color);
	}
	public static final void set_color(Activity parent, int resId, int color) {
		set_color(parent.findViewById(resId), color);
	}

	public static final void set_ib(View ib, int imageResId) {
		if (ib != null && ib instanceof ImageButton) {
			((ImageButton)ib).setImageResource(imageResId);
		}
	}
	public static final void set_ib(View ib, Drawable drawable) {
		if (ib != null && ib instanceof ImageButton) {
			((ImageButton)ib).setImageDrawable(drawable);
		}
	}
	public static final void set_ib(View ib, Bitmap bitmap) {
		if (ib != null && ib instanceof ImageButton) {
			((ImageButton)ib).setImageBitmap(bitmap);
		}
	}
	public static final void set_ib(View parent, int resId, int imageResId) {
		set_ib(parent.findViewById(resId), imageResId);
	}
	public static final void set_ib(Activity parent, int resId, int imageResId) {
		set_ib(parent.findViewById(resId), imageResId);
	}
	public static final void set_ib(View parent, int resId, Drawable drawable) {
		set_ib(parent.findViewById(resId), drawable);
	}
	public static final void set_ib(Activity parent, int resId, Drawable drawable) {
		set_ib(parent.findViewById(resId), drawable);
	}
	public static final void set_ib(View parent, int resId, Bitmap bitmap) {
		set_ib(parent.findViewById(resId), bitmap);
	}
	public static final void set_ib(Activity parent, int resId, Bitmap bitmap) {
		set_ib(parent.findViewById(resId), bitmap);
	}

	public static final void set_niv(View niv, String url, int defResId, AsyncImageLoader loader) {
		if (niv != null && niv instanceof NetImageView) {
			NetImageView iv = (NetImageView)niv;
			iv.setImageResource(defResId);
			iv.setImage(url, loader);
		}
	}
	public static final void set_niv(View niv, String url, Drawable def, AsyncImageLoader loader) {
		if (niv != null && niv instanceof NetImageView) {
			NetImageView iv = (NetImageView)niv;
			set_iv(iv, def);
			iv.setImage(url, loader);
		}
	}
	public static final void set_niv(View niv, String url, AsyncImageLoader loader) {
		if (niv != null && niv instanceof NetImageView) {
			((NetImageView)niv).setImage(url, loader);
		}
	}
	public static final void set_niv(View niv, String url) {
		set_niv(niv, url, null);
	}
	public static final void set_niv(View parent, int resId, String url, AsyncImageLoader loader) {
		set_niv(parent.findViewById(resId), url, loader);
	}
	public static final void set_niv(Activity parent, int resId, String url, AsyncImageLoader loader) {
		set_niv(parent.findViewById(resId), url, loader);
	}
	public static final void set_niv(View parent, int resId, String url) {
		set_niv(parent.findViewById(resId), url, null);
	}
	public static final void set_niv(Activity parent, int resId, String url) {
		set_niv(parent.findViewById(resId), url, null);
	}
	public static final void set_niv(View convertView, int resId, JSONObject json, String name, AsyncImageLoader loader) {
		set_niv(convertView.findViewById(resId), JsonUtil.textValue(json, name), loader);
	}
	public static final void set_niv(Activity activity, int resId, JSONObject json, String name, AsyncImageLoader loader) {
		set_niv(activity.findViewById(resId), JsonUtil.textValue(json, name), loader);
	}
	public static final void set_niv(View convertView, int resId, JSONObject json, String name) {
		set_niv(convertView.findViewById(resId), JsonUtil.textValue(json, name), null);
	}
	public static final void set_niv(View view, JSONObject json, String name) {
		set_niv(view, JsonUtil.textValue(json, name), null);
	}
	public static final void set_niv(View view, JSONObject json, String name, int defaultImageResId) {
		set_niv(view, JsonUtil.textValue(json, name), defaultImageResId, null);
	}
	public static final void set_niv(Activity activity, int resId, JSONObject json, String name) {
		set_niv(activity.findViewById(resId), JsonUtil.textValue(json, name), null);
	}
	public static final void set_niv(View convertView, int resId, int defaultImageResId, JSONObject json, String name) {
		set_niv(convertView.findViewById(resId), JsonUtil.textValue(json, name), defaultImageResId, null);
	}
	public static final void set_niv(Activity activity, int resId, int defaultImageResId, JSONObject json, String name) {
		set_niv(activity.findViewById(resId), JsonUtil.textValue(json, name), defaultImageResId, null);
	}
	public static final void set_niv(View convertView, int resId, int defaultImageResId, JSONObject json, String name, AsyncImageLoader loader) {
		set_niv(convertView.findViewById(resId), JsonUtil.textValue(json, name), defaultImageResId, loader);
	}
	public static final void set_niv(Activity activity, int resId, int defaultImageResId, JSONObject json, String name, AsyncImageLoader loader) {
		set_niv(activity.findViewById(resId), JsonUtil.textValue(json, name), defaultImageResId, loader);
	}
	public static final void set_niv(View parent, int resId, String url, int defaultImageResId) {
		set_niv(parent.findViewById(resId), url, defaultImageResId, null);
	}
	public static final void set_niv(Activity parent, int resId, String url, int defaultImageResId) {
		set_niv(parent.findViewById(resId), url, defaultImageResId, null);
	}

	public static final void set_civ(View civ, String url, int defResId, AsyncImageLoader loader) {
		if (civ != null && civ instanceof CircleImageView) {
			CircleImageView iv = (CircleImageView)civ;
			iv.setImageResource(defResId);
			iv.setImage(url, loader);
		}
	}
	public static final void set_civ(View civ, String url, Drawable def, AsyncImageLoader loader) {
		if (civ != null && civ instanceof CircleImageView) {
			CircleImageView iv = (CircleImageView)civ;
			set_iv(iv, def);
			iv.setImage(url, loader);
		}
	}
	public static final void set_civ(View civ, String url, AsyncImageLoader loader) {
		if (civ != null && civ instanceof CircleImageView) {
			((CircleImageView)civ).setImage(url, loader);
		}
	}
	public static final void set_civ(View civ, String url) {
		set_civ(civ, url, null);
	}
	public static final void set_civ(View parent, int resId, String url, AsyncImageLoader loader) {
		set_civ(parent.findViewById(resId), url, loader);
	}
	public static final void set_civ(Activity parent, int resId, String url, AsyncImageLoader loader) {
		set_civ(parent.findViewById(resId), url, loader);
	}
	public static final void set_civ(View parent, int resId, String url) {
		set_civ(parent.findViewById(resId), url, null);
	}
	public static final void set_civ(View parent, int resId, String url, int defResId) {
		set_civ(parent.findViewById(resId), url, defResId, null);
	}
	public static final void set_civ(Activity parent, int resId, String url) {
		set_civ(parent.findViewById(resId), url, null);
	}
	public static final void set_civ(Activity parent, int resId, String url, int defResId) {
		set_civ(parent.findViewById(resId), url, defResId, null);
	}
	public static final void set_civ(View convertView, int resId, JSONObject json, String name, AsyncImageLoader loader) {
		set_civ(convertView.findViewById(resId), JsonUtil.textValue(json, name), loader);
	}
	public static final void set_civ(Activity activity, int resId, JSONObject json, String name, AsyncImageLoader loader) {
		set_civ(activity.findViewById(resId), JsonUtil.textValue(json, name), loader);
	}
	public static final void set_civ(View convertView, int resId, JSONObject json, String name) {
		set_civ(convertView.findViewById(resId), JsonUtil.textValue(json, name), null);
	}
	public static final void set_civ(View view, JSONObject json, String name) {
		set_civ(view, JsonUtil.textValue(json, name), null);
	}
	public static final void set_civ(View convertView, int resId, JSONObject json, String name, int defResId) {
		set_civ(convertView.findViewById(resId), JsonUtil.textValue(json, name), defResId, null);
	}
	public static final void set_civ(View convertView, int resId, int defaultImageResId, JSONObject json, String name, AsyncImageLoader loader) {
		set_civ(convertView.findViewById(resId), JsonUtil.textValue(json, name), defaultImageResId, loader);
	}
	public static final void set_civ(Activity activity, int resId, int defaultImageResId, JSONObject json, String name, AsyncImageLoader loader) {
		set_civ(activity.findViewById(resId), JsonUtil.textValue(json, name), defaultImageResId, loader);
	}

	public static final void set_cb(View cb, boolean value) {
		if (cb != null && cb instanceof CompoundButton) {
			((CompoundButton)cb).setChecked(value);
		}
	}
	public static final void set_cb(View parent, int resId, boolean value) {
		set_cb(parent.findViewById(resId), value);
	}
	public static final void set_cb(Activity parent, int resId, boolean value) {
		set_cb(parent.findViewById(resId), value);
	}
	public static final void set_cb(View parent, int resId, JSONObject json, String name) {
		set_cb(parent.findViewById(resId), JsonUtil.boolValue(json, name));
	}
	public static final void set_cb(Activity parent, int resId, JSONObject json, String name) {
		set_cb(parent.findViewById(resId), JsonUtil.boolValue(json, name));
	}
	public static final boolean get_cb(View cb) {
		if (cb != null && cb instanceof CompoundButton) {
			return ((CompoundButton)cb).isChecked();
		} else {
			return false;
		}
	}
	public static final boolean get_cb(View parent, int resId) {
		return get_cb(parent.findViewById(resId));
	}
	public static final boolean get_cb(Activity parent, int resId) {
		return get_cb(parent.findViewById(resId));
	}

	public static final void set_sb(View sb, int value) {
		if (sb != null && sb instanceof SeekBar) {
			((SeekBar)sb).setProgress(value);
		}
	}
	public static final void set_sb(View parent, int resId, int value) {
		set_sb(parent.findViewById(resId), value);
	}
	public static final void set_sb(Activity parent, int resId, int value) {
		set_sb(parent.findViewById(resId), value);
	}
	public static final void set_sb(View parent, int resId, JSONObject json, String name) {
		set_sb(parent.findViewById(resId), JsonUtil.intValue(json, name));
	}
	public static final void set_sb(Activity parent, int resId, JSONObject json, String name) {
		set_sb(parent.findViewById(resId), JsonUtil.intValue(json, name));
	}
	public static final int get_sb(View sb) {
		if (sb != null && sb instanceof SeekBar) {
			return ((SeekBar)sb).getProgress();
		} else {
			return 0;
		}
	}
	public static final int get_sb(View parent, int resId) {
		return get_sb(parent.findViewById(resId));
	}
	public static final int get_sb(Activity parent, int resId) {
		return get_sb(parent.findViewById(resId));
	}
	
	public static final void set_rb(View sb, int value) {
		if (sb != null && sb instanceof RatingBar) {
			((RatingBar)sb).setProgress(value);
		}
	}
	public static final void set_rb(View parent, int resId, int value) {
		set_rb(parent.findViewById(resId), value);
	}
	public static final void set_rb(Activity parent, int resId, int value) {
		set_rb(parent.findViewById(resId), value);
	}
	public static final void set_rb(View parent, int resId, JSONObject json, String name) {
		set_rb(parent.findViewById(resId), JsonUtil.intValue(json, name));
	}
	public static final void set_rb(Activity parent, int resId, JSONObject json, String name) {
		set_rb(parent.findViewById(resId), JsonUtil.intValue(json, name));
	}
	public static final int get_rb(View sb) {
		if (sb != null && sb instanceof RatingBar) {
			return ((RatingBar)sb).getProgress();
		} else {
			return 0;
		}
	}
	public static final int get_rb(View parent, int resId) {
		return get_rb(parent.findViewById(resId));
	}
	public static final int get_rb(Activity parent, int resId) {
		return get_rb(parent.findViewById(resId));
	}

	public static final void set_radio(View radioGroup, int radioBoxId) {
		if (radioGroup != null && radioGroup instanceof RadioGroup) {
			((RadioGroup)radioGroup).check(radioBoxId);
		}
	}
	public static final void set_radio(View parentView, int radioGroupId, int radioBoxId) {
		if (parentView != null) set_radio(parentView.findViewById(radioGroupId), radioBoxId);
	}
	public static final void set_radio(Activity activity, int radioGroupId, int radioBoxId) {
		if (activity != null) set_radio(activity.findViewById(radioGroupId), radioBoxId);
	}
	
	public static final boolean get_bool(String text, boolean defaultValue) {
		if (text == null || text.length() < 1)return defaultValue;
		else if (text.equalsIgnoreCase("yes") || text.equalsIgnoreCase("true"))return true;
		else if (text.equalsIgnoreCase("no") || text.equalsIgnoreCase("false"))return false;		
		try {return Boolean.valueOf(text);} catch (Exception e) {return defaultValue;}
	}
	public static final int get_int(String text, int defaultValue) {
		try {return Integer.valueOf(text);} catch (Exception e) {return defaultValue;}
	}
	public static final float get_float(String text, float defaultValue) {
		try {return Float.valueOf(text);} catch (Exception e) {return defaultValue;}
	}
	public static final double get_double(String text, double defaultValue) {
		try {return Double.valueOf(text);} catch (Exception e) {return defaultValue;}
	}
	
	public static final void set_vh(View v, int height) {
		if (v == null)return;
		if (!(v.getLayoutParams() instanceof ViewGroup.LayoutParams))return;
		ViewGroup.LayoutParams lp = v.getLayoutParams();
		lp.height = height;
		v.requestLayout();
	}
	public static final void set_vw(View v, int width) {
		if (v == null)return;
		if (!(v.getLayoutParams() instanceof ViewGroup.LayoutParams))return;
		ViewGroup.LayoutParams lp = v.getLayoutParams();
		lp.width = width;
		v.requestLayout();		
	}
	public static final void set_vmLeft(View v, int leftMargin) {
		if (v == null)return;
		if (!(v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams))return;
		ViewGroup.MarginLayoutParams lp = (MarginLayoutParams) v.getLayoutParams();
		lp.leftMargin = leftMargin;
		v.requestLayout();			
	}
	public static final void set_vmTop(View v, int topMargin) {
		if (v == null)return;
		if (!(v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams))return;
		ViewGroup.MarginLayoutParams lp = (MarginLayoutParams) v.getLayoutParams();
		lp.topMargin = topMargin;
		v.requestLayout();			
	}
	public static final void set_vmRight(View v, int rightMargin) {
		if (v == null)return;
		if (!(v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams))return;
		ViewGroup.MarginLayoutParams lp = (MarginLayoutParams) v.getLayoutParams();
		lp.rightMargin = rightMargin;
		v.requestLayout();			
	}
	public static final void set_vmBottom(View v, int bottomMargin) {
		if (v == null)return;
		if (!(v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams))return;
		ViewGroup.MarginLayoutParams lp = (MarginLayoutParams) v.getLayoutParams();
		lp.bottomMargin = bottomMargin;
		v.requestLayout();			
	}
	
	public static final void set_enable(View v, boolean enabled) {
		if (v != null) {
			v.setEnabled(enabled);
		}
	}
	public static final void set_enable(View convertView, int resId, JSONObject json, String name) {
		set_enable(convertView.findViewById(resId), JsonUtil.boolValue(json, name));
	}
	public static final void set_enable(Activity activity, int resId, JSONObject json, String name) {
		set_enable(activity.findViewById(resId), JsonUtil.boolValue(json, name));
	}
	public static final void set_enable(View convertView, int resId, boolean enabled) {
		set_enable(convertView.findViewById(resId), enabled);
	}
	public static final void set_enable(Activity activity, int resId, boolean enabled) {
		set_enable(activity.findViewById(resId), enabled);
	}
	public static final void set_enable(View convertView, boolean enabled, int...resIds) {
		for (int resId : resIds) {
			set_enable(convertView.findViewById(resId), enabled);
		}
	}
	public static final void set_enable(boolean enabled, View...views) {
		for (View view : views) {
			set_enable(view, enabled);
		}
	}
	public static final void set_enable(Activity activity, boolean enabled, int...resIds) {
		for (int resId : resIds) {
			set_enable(activity.findViewById(resId), enabled);
		}
	}
	
	public static final void set_visible(View v, boolean visiable) {
		if (v != null)v.setVisibility(visiable ? View.VISIBLE : View.GONE);
	}
	public static final void set_visible(View toVisible, View toGone) {
		if (toVisible != null)toVisible.setVisibility(View.VISIBLE);
		if (toGone != null)toVisible.setVisibility(View.GONE);
	}
	public static final void set_visible(View[] toVisibles, View[] toGones) {
		if (toVisibles != null){
			for (View view : toVisibles)view.setVisibility(View.VISIBLE);
		}
		if (toGones != null) {
			for (View view : toGones)view.setVisibility(View.GONE);
		}
	}
	public static final void set_visible(boolean visible, View...views) {
		for (int i = 0; i < views.length; i++) {
			set_visible(views[i], visible);
		}
	}
	public static final void set_visible(View convertView, int resId, JSONObject json, String name) {
		set_visible(convertView.findViewById(resId), JsonUtil.boolValue(json, name));
	}
	public static final void set_visible(Activity activity, int resId, JSONObject json, String name) {
		set_visible(activity.findViewById(resId), JsonUtil.boolValue(json, name));
	}
	public static final void set_visible(View convertView, int resId, boolean visiable) {
		set_visible(convertView.findViewById(resId), visiable);
	}
	public static final void set_visible(Activity activity, int resId, boolean visiable) {
		set_visible(activity.findViewById(resId), visiable);
	}	
	public static final void set_visible(View v, int visiable) {
		if (v != null) {
			v.setVisibility(visiable);
		}
	}
	public static final void set_visible(int visible, View...views) {
		for (int i = 0; i < views.length; i++) {
			set_visible(views[i], visible);
		}
	}
	public static final void set_visible(View convertView, int resId, int visiable) {
		set_visible(convertView.findViewById(resId), visiable);
	}
	public static final void set_visible(Activity activity, int resId, int visiable) {
		set_visible(activity.findViewById(resId), visiable);
	}
	public static final void set_visible(View convertView, boolean visiable, int...resIds) {
		for (int i = 0; i < resIds.length; i++) {
			set_visible(convertView.findViewById(resIds[i]), visiable);
		}
	}
	public static final void set_visible(Activity activity, boolean visiable, int...resIds) {
		for (int i = 0; i < resIds.length; i++) {
			set_visible(activity.findViewById(resIds[i]), visiable);
		}
	}
	public static final void set_visible(View convertView, int visiable, int...resIds) {
		for (int i = 0; i < resIds.length; i++) {
			set_visible(convertView.findViewById(resIds[i]), visiable);
		}
	}
	public static final void set_visible(Activity activity, int visiable, int...resIds) {
		for (int i = 0; i < resIds.length; i++) {
			set_visible(activity.findViewById(resIds[i]), visiable);
		}
	}

	public static final boolean is_visible(View view) {
		if (view == null) return false;
		return view.getVisibility() == View.VISIBLE;
	}
	public static final boolean is_visible(View parentView, int resId) {
		if (parentView == null) return false;
		return is_visible(parentView.findViewById(resId));
	}
	public static final boolean is_visible(Activity activity, int resId) {
		if (activity == null) return false;
		return is_visible(activity.findViewById(resId));
	}
	
	public static final void set_check(View v, OnCheckedChangeListener listener) {
		if (v != null && v instanceof CompoundButton) {
			((CompoundButton)v).setOnCheckedChangeListener(listener);
		}
	}
	public static final void set_check(Activity activity, int resId, OnCheckedChangeListener listener) {
		set_check(activity.findViewById(resId), listener);
	}
	public static final void set_check(View convertView, int resId, OnCheckedChangeListener listener) {
		set_check(convertView.findViewById(resId), listener);
	}
	public static final void set_check(OnCheckedChangeListener listener, View...views) {
		for (int i = 0; i < views.length; i++) {
			set_check(views[i],  listener);
		}
	}
	public static final void set_check(Activity activity, OnCheckedChangeListener listener, int...resIds) {
		for (int i = 0; i < resIds.length; i++) {
			set_check(activity.findViewById(resIds[i]), listener);
		}
	}
	public static final void set_check(View convertView, OnCheckedChangeListener listener, int...resIds) {
		for (int i = 0; i < resIds.length; i++) {
			set_check(convertView.findViewById(resIds[i]), listener);
		}
	}

	public static final void set_click(View v, OnClickListener listener) {
		if (v != null) {
			v.setOnClickListener(listener);
		}
	}
	public static final void set_click(Activity activity, int resId, OnClickListener listener) {
		set_click(activity.findViewById(resId), listener);
	}
	public static final void set_click(View convertView, int resId, OnClickListener listener) {
		set_click(convertView.findViewById(resId), listener);
	}
	public static final void set_click(OnClickListener listener, View...views) {
		for (int i = 0; i < views.length; i++) {
			set_click(views[i],  listener);
		}
	}
	public static final void set_click(Activity activity, OnClickListener listener, int...resIds) {
		for (int i = 0; i < resIds.length; i++) {
			set_click(activity.findViewById(resIds[i]), listener);
		}
	}
	public static final void set_click(View convertView, OnClickListener listener, int...resIds) {
		for (int i = 0; i < resIds.length; i++) {
			set_click(convertView.findViewById(resIds[i]), listener);
		}
	}


	public static final void set_leftDrawable(View view, int imgResId) {
		if (!(view instanceof TextView))return;
		TextView textView = (TextView)view;
		Drawable[] drawables = textView.getCompoundDrawables();
		Drawable left = imgResId == 0 ? Drawables.getClear() : Drawables.getDrawable(view.getContext(), imgResId);
		textView.setCompoundDrawables(left, drawables[1], drawables[2], drawables[3]);
	}
	public static final void set_leftDrawable(View parent, int resId, int imgResId) {
		set_leftDrawable(parent.findViewById(resId), imgResId);
	}
	public static final void set_leftDrawable(Activity parent, int resId, int imgResId) {
		set_leftDrawable(parent.findViewById(resId), imgResId);
	}

	public static final void set_rightDrawable(View view, int imgResId) {
		if (!(view instanceof TextView))return;
		TextView textView = (TextView)view;
		Drawable[] drawables = textView.getCompoundDrawables();
		Drawable right = imgResId == 0 ? Drawables.getClear() : Drawables.getDrawable(view.getContext(), imgResId);
		textView.setCompoundDrawables(drawables[0], drawables[1], right, drawables[3]);
	}
	public static final void set_rightDrawable(View parent, int resId, int imgResId) {
		set_rightDrawable(parent.findViewById(resId), imgResId);
	}
	public static final void set_rightDrawable(Activity parent, int resId, int imgResId) {
		set_rightDrawable(parent.findViewById(resId), imgResId);
	}

	public static final void set_topDrawable(View view, int imgResId) {
		if (!(view instanceof TextView))return;
		TextView textView = (TextView)view;
		Drawable[] drawables = textView.getCompoundDrawables();
		Drawable top = imgResId == 0 ? Drawables.getClear() : Drawables.getDrawable(view.getContext(), imgResId);
		textView.setCompoundDrawables(drawables[0], top, drawables[2], drawables[3]);
	}
	public static final void set_topDrawable(View parent, int resId, int imgResId) {
		set_topDrawable(parent.findViewById(resId), imgResId);
	}
	public static final void set_topDrawable(Activity parent, int resId, int imgResId) {
		set_topDrawable(parent.findViewById(resId), imgResId);
	}

	public static final void set_bottomDrawable(View view, int imgResId) {
		if (!(view instanceof TextView))return;
		TextView textView = (TextView)view;
		Drawable[] drawables = textView.getCompoundDrawables();
		Drawable bottom = imgResId == 0 ? Drawables.getClear() : Drawables.getDrawable(view.getContext(), imgResId);
		textView.setCompoundDrawables(drawables[0], drawables[1], drawables[2], bottom);
	}
	public static final void set_bottomDrawable(View parent, int resId, int imgResId) {
		set_bottomDrawable(parent.findViewById(resId), imgResId);
	}
	public static final void set_bottomDrawable(Activity parent, int resId, int imgResId) {
		set_bottomDrawable(parent.findViewById(resId), imgResId);
	}

	public static final void set_h(View view, int height) {
		if (view != null) {
			LayoutParams layoutParams = view.getLayoutParams();
			layoutParams.height = height;
			view.requestLayout();
		}
	}
	public static final void set_h(View parent, int resId, int imgResId) {
		set_h(parent.findViewById(resId), imgResId);
	}
	public static final void set_h(Activity parent, int resId, int imgResId) {
		set_h(parent.findViewById(resId), imgResId);
	}
}
