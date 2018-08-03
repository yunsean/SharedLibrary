package com.dylan.uiparts.edittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.dylan.common.application.Application;
import com.dylan.uiparts.R;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkedEditText extends EditText implements TextWatcher, OnClickListener {

	public interface OnShowFaceListener {
		public Bitmap getFace(LinkedEditText editText, String emoticon);
	}
	private OnShowFaceListener mOnShowFaceListener = null;
	public void setOnShowFaceListener(OnShowFaceListener listener) {
		mOnShowFaceListener = listener;
	}

	private int mLinkColor = 0;
	public LinkedEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}
	public LinkedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr);
	}
	private void init(Context context, AttributeSet attrs, int defStyleAttr) {
		mPatterns.add("\\$[^\\$]+\\$");
		mPatterns.add("@[^@]+\\：");
		mPatterns.add("@[^@]+\\:");
		mPatterns.add("@[^@]+\\s?");
		mPatterns.add("//[^//]+\\：");
		mPatterns.add("//[^//]+\\:");
		mPatterns.add("(http|ftp|https)://[^\\s]+\\s?");

		mLinkColor = 0xff2e518a;
		if (attrs != null) {
			final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LinkedTextView, defStyleAttr, 0);
			int resId = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "text", 0);
			String text = null;
			if (resId != 0) {
				text = context.getResources().getString(resId);
			} else {
				text = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "text");
			}
			mLinkColor = a.getColor(R.styleable.LinkedTextView_ltv_linkColor, 0xff2e518a);
			a.recycle();
			if (text != null && text.length() > 0) {
				updateText(text);
			}
		}
		addTextChangedListener(this);
		setOnClickListener(this);
	}

	private int mStoredSelection = -1;
	public void storeSelection() {
		mStoredSelection = getSelectionStart();
	}
	public void retrieveSelection(int offset) {
		int length = getText().toString().length();
		if (mStoredSelection == -1) {
			setSelection(length);
		} else {
			int selection = mStoredSelection + offset;
			if (selection > length) selection = length;
			for (SpanPair span : mSpanSection) {
				if (selection >= span.begin && selection < span.end) {
					selection = span.end;
					break;
				}
			}
			final int _selection = selection;
			postDelayed(new Runnable() {
				@Override
				public void run() {
					setSelection(_selection);
					requestFocus();
				}
			}, 100);
		}
	}
	private SpannableString mSpannableString = null;
	private ArrayList<String> mPatterns = new ArrayList<String>();
	public void setPatterns(String... patterns) {
		mPatterns.clear();
		for (int i = 0; i < patterns.length; i++) {
			mPatterns.add(patterns[i]);
		}
		updateText(getText());
	}

	private void updateText(CharSequence text) {
		mSpannableString = new SpannableString(text);
		mSpanSection.clear();
		Application app = null;
		if (true) {
			Pattern mode = Pattern.compile("\\[[^\\[]+\\]");
			Matcher matcher = mode.matcher(text);
			boolean result = matcher.find();
			if (result) {
				do {
					boolean found = false;
					int begin = matcher.start();
					int end = matcher.end();
					for (SpanPair span : mSpanSection) {
						if (begin >= span.begin && begin < span.end)found = true;
						else if (end > span.begin && end < span.end)found = true;
					}
					if (!found) {
						Bitmap face = null;
						if (mOnShowFaceListener != null) {
							face = mOnShowFaceListener.getFace(this, text.toString().substring(begin, end));
						} else {
							if (app == null) app = (Application)getContext().getApplicationContext();
							face = app.getFace(text.toString().substring(begin, end));
						}
						if (face != null) {
							mSpanSection.add(new SpanPair(begin, end, true));
							mSpannableString.setSpan(new ImageSpan(getContext(), face), begin, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
						}
					}
				}while (matcher.find());
			}
		}
		for (String pattern : mPatterns) {
			Pattern mode = Pattern.compile(pattern);
			Matcher matcher = mode.matcher(text);
			boolean result = matcher.find();
			if (result) {
				do {
					boolean found = false;
					int begin = matcher.start();
					int end = matcher.end();
					if (end > 0 && text.charAt(end - 1) == '：')end--;
					for (SpanPair span : mSpanSection) {
						if (begin >= span.begin && begin < span.end)found = true;
						else if (end > span.begin && end < span.end)found = true;
					}
					if (!found) {
						mSpanSection.add(new SpanPair(begin, end, false));
						mSpannableString.setSpan(new ForegroundColorSpan(mLinkColor), begin, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
					}
				}while (matcher.find());
			}
		}
		setText(mSpannableString);
	}

	private class SpanPair {
		public SpanPair(int b, int e, boolean is) {
			begin = b;
			end = e;
			emotion = is;
		}
		public int begin;
		public int end;
		private boolean emotion = false;
	}
	private ArrayList<SpanPair> mSpanSection = new ArrayList<SpanPair>();
	private Handler mSelectionHandler = new Handler();
	private class SelectionRunable implements Runnable {
		private int mBegin = 0;
		private int mEnd = 0;
		SelectionRunable(int begin, int end) {
			mBegin = begin;
			mEnd = end;
		}
		@Override
		public void run() {
			int len = length();
			if (mBegin > len) mBegin = len;
			if (mEnd > len) mEnd = len;
			setSelection(mBegin, mEnd);
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
			int selectionStart = getSelectionStart();
			int selectionEnd = getSelectionEnd();
			if (selectionStart == selectionEnd) {
				for (final SpanPair sp : mSpanSection) {
					if (sp.begin <= selectionStart && sp.end >= selectionEnd) {
						if (sp.emotion) {
							return super.onKeyDown(keyCode, event);
						} else {
							mSelectionHandler.postDelayed(new SelectionRunable(sp.begin, sp.end), 100);
							return false;
						}
					}
				}
			} else {
				boolean result = super.onKeyDown(keyCode, event);
				updateText(getText());
				if (selectionStart > length()) selectionStart = length();
				setSelection(selectionStart);
				return result;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private boolean mInTextChanged = false;
	@Override
	public void afterTextChanged(Editable arg0) {
		if (mInTextChanged)return;
		int selectionIndex = getSelectionStart();
		if (selectionIndex == length() & selectionIndex > 0) {
			char c = arg0.charAt(selectionIndex - 1);
			if (c != ' ' && c != '$' && c != ':' && c != '：' && c != ']') {
				return;
			}
		}
		mInTextChanged = true;
		updateText(arg0);
		mInTextChanged = false;
		setSelection(selectionIndex);
	}
	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
	}
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void onClick(View v) {
	}

	private int mLatestSelectionStart = 0;
	private boolean mSelectionEndChanging = false;
	@Override
	public void onSelectionChanged(int selStart, int selEnd) {
		if (mSpanSection == null)return;
		if (mLatestSelectionStart == selStart) {
			mSelectionEndChanging = true;
		} else {
			mSelectionEndChanging = false;
		}
		mLatestSelectionStart = selStart;
		int selectionStart = selStart;
		int selectionEnd = selEnd;
		SpanPair startSpan = null;
		SpanPair endSpan = null;
		boolean foundStart = false;
		boolean foundEnd = false;
		int index = 0;
		for (index = 0; index < mSpanSection.size(); index++) {
			SpanPair sp = mSpanSection.get(index);
			if (sp.begin <= selectionStart && sp.end > selectionStart) {
				int left = selectionStart - sp.begin;
				int right = sp.end - 1 - selectionStart;
				if (left > right) {
					selectionStart = sp.end;
				} else {
					selectionStart = sp.begin;
				}
				startSpan = sp;
				if (foundEnd)break;
			}
			if (sp.begin <= selectionEnd && sp.end > selectionEnd) {
				int left = selectionEnd - sp.begin;
				int right = sp.end - 1 - selectionEnd;
				if (left > right) {
					selectionEnd = sp.end;
				} else {
					selectionEnd = sp.begin;
				}
				endSpan = sp;
				if (foundStart)break;
			}
		}
		if (selectionStart != selStart || selectionEnd != selEnd) {
			if (selStart != selEnd && selectionStart == selectionEnd) {
				if (mSelectionEndChanging && startSpan != null) {
					selectionEnd = startSpan.end;
				} else if (!mSelectionEndChanging && endSpan != null) {
					selectionStart = endSpan.begin;
				}
			}
			String text = getText().toString();
			if (selectionStart >= text.length())selectionStart = text.length();
			if (selectionEnd >= text.length())selectionEnd = text.length();
			setSelection(selectionStart, selectionEnd);
		}
	}
}
