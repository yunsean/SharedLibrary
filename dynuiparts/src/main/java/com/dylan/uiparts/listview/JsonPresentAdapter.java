package com.dylan.uiparts.listview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.dylan.common.sketch.Sketch;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class JsonPresentAdapter extends BaseAdapter {

	public JsonPresentAdapter(Context context, List<JSONObject> items, int layoutResId, int[] resIds, String[] jsonKeys, Object[] defaultValues) {
		mInflater = LayoutInflater.from(context);
		mLayoutResId = layoutResId;
		mItems = items;
		mResIds = resIds;
		mJsonKeys = jsonKeys;
		mDefaultValues = defaultValues;
	}
	public JsonPresentAdapter(Context context, List<JSONObject> items, int layoutResId, int[] resIds, String[] jsonKeys, Object[] defaultValues, String[] prefixs, String[] suffixs) {
		mInflater = LayoutInflater.from(context);
		mLayoutResId = layoutResId;
		mItems = items;
		mResIds = resIds;
		mJsonKeys = jsonKeys;
		mDefaultValues = defaultValues;
		mPrefixs = prefixs;
		mSuffixs = suffixs;
	}
	protected void initConvertView(View convertView) {
		
	}
	protected boolean fillConvertView(View convertView, JSONObject json) throws JSONException {
		return false;
	}
	protected void afterFillConvertView(View convertView, JSONObject json) throws JSONException {
		
	}
	
	private LayoutInflater mInflater = null;
	private Object[] mDefaultValues = null;
	private int[] mResIds = null;
	private String[] mJsonKeys = null;
	private String[] mPrefixs = null;
	private String[] mSuffixs = null;
	private int mLayoutResId = 0;
	private List<JSONObject> mItems = null;

	@Override
	public boolean isEmpty() {
		return mItems == null || mItems.size() < 1;
	}
	@Override
	public int getCount() {
		return mItems == null ? 0 : mItems.size();
	}
	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}
	@Override
	public long getItemId(int position) {
		return 0;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(mLayoutResId, parent, false);
			initConvertView(convertView);
		}
		try {
			JSONObject json = mItems.get(position);
			if (!fillConvertView(convertView, json)) {
				if (mPrefixs == null && mSuffixs == null) {
					for (int i = 0; i < mJsonKeys.length; i++) {
						Sketch.set_value(convertView, mResIds[i], json, mJsonKeys[i], mDefaultValues[i]);
					}
				} else {
					for (int i = 0; i < mJsonKeys.length; i++) {
						String value = "";
						if (mPrefixs != null && mPrefixs[i] != null)value = mPrefixs[i];
						if (json.has(mJsonKeys[i]) && !json.isNull(mJsonKeys[i]))value += json.getString(mJsonKeys[i]);
						else if (mDefaultValues[i] != null)value += mDefaultValues[i];
						if (mSuffixs != null && mSuffixs[i] != null)value += mSuffixs[i];
						Sketch.set_value(convertView, mResIds[i], value);
					}
				}
			}
			afterFillConvertView(convertView, json);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return convertView;
	}

}
