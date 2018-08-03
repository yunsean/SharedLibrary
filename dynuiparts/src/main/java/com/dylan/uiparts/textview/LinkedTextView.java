package com.dylan.uiparts.textview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.dylan.common.application.Application;
import com.dylan.uiparts.R;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressLint("ClickableViewAccessibility")
public class LinkedTextView extends TextView {

	public interface OnLinkClickedListener {
		public void onClicked(LinkedTextView textView, String content);
	}

	private int mLinkColor = 0;
	private OnLinkClickedListener mOnLinkClickedListener = null;
	public LinkedTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public LinkedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setMovementMethod(LinkMovementMethod.getInstance());

		mLinkColor = 0xff2e518a;
		if (attrs != null) {
			int resId = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "text", 0);
			String text = null;
			if (resId != 0) {
				text = context.getResources().getString(resId);
			} else {
				text = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "text");
			}
			final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LinkedTextView, defStyleAttr, 0);
			mShowOnly = a.getBoolean(R.styleable.LinkedTextView_ltv_showOnly, false);
			mLinkColor = a.getColor(R.styleable.LinkedTextView_ltv_linkColor, 0xff2e518a);
			if (a.hasValue(R.styleable.LinkedTextView_ltv_pattern)) {
				mPatterns.add(a.getString(R.styleable.LinkedTextView_ltv_pattern));
			}
			if (a.hasValue(R.styleable.LinkedTextView_ltv_patterns)) {
				CharSequence[] patterns = a.getTextArray(R.styleable.LinkedTextView_ltv_patterns);
				for (CharSequence charSequence : patterns) {
					mPatterns.add(charSequence.toString());
				}
			}
			a.recycle();
			if (mPatterns.size() < 1) {
				mPatterns.add("\\$[^\\$]+\\$");
				mPatterns.add("@[^@]+\\：");
				mPatterns.add("@[^@]+\\:");
				mPatterns.add("@[^@]+\\s?");
				mPatterns.add("//[^//]+\\：");
				mPatterns.add("//[^//]+\\:");
				mPatterns.add("(http|ftp|https)://[^\\s]+\\s?");
			}
			if (text != null && text.length() > 0) {
				updateText(text);
			}
		}
	}

	private boolean mShowOnly = false;
	public void setShowOnly(boolean showOnly) {
		mShowOnly = showOnly;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			boolean found = false;
			CharSequence text = getText();
			Spannable stext = Spannable.Factory.getInstance().newSpannable(text);
			int x = (int) event.getX();
			int y = (int) event.getY();
			x -= getTotalPaddingLeft();
			y -= getTotalPaddingTop();
			x += getScrollX();
			y += getScrollY();
			Layout layout = getLayout();
			int line = layout.getLineForVertical(y);
			int off = layout.getOffsetForHorizontal(line, x);
			ClickableSpan[] link = stext.getSpans(off, off, ClickableSpan.class);
			if (link.length == 0) {
				return false;
			}
		}
		return super.onTouchEvent(event);
	}

	public void setFormattedText(CharSequence text) {
		updateText(text);
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
	public void setOnLinkClickedListener(OnLinkClickedListener listener) {
		mOnLinkClickedListener = listener;
	}

	private class SpanSection {
		SpanSection(int b, int e) {
			begin = b;
			end = e;
		}
		int begin;
		int end;
	}
	private void updateText(CharSequence text) {
		setText(text);
		mSpannableString = new SpannableString(text);
		ArrayList<SpanSection> spans = new ArrayList<SpanSection>();
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
					for (SpanSection span : spans) {
						if (begin >= span.begin && begin < span.end)found = true;
						else if (end > span.begin && end < span.end)found = true;
					}
					if (!found) {
						if (app == null) app = (Application)getContext().getApplicationContext();
						Bitmap face = app.getFace(text.toString().substring(begin, end));
						if (face != null) {
							spans.add(new SpanSection(begin, end));
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
					MyClickSpan clickSpan = new MyClickSpan(matcher.group());
					boolean found = false;
					int begin = matcher.start();
					int end = matcher.end();
					if (end > 0 && text.charAt(end - 1) == '：')end--;
					for (SpanSection span : spans) {
						if (begin >= span.begin && begin < span.end)found = true;
						else if (end > span.begin && end < span.end)found = true;
					}
					if (!found) {
						mSpannableString.setSpan(clickSpan, begin, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
						spans.add(new SpanSection(begin, end));
					}
				}while (matcher.find());
			}
		}
		setText(mSpannableString);
	}
	private class MyClickSpan extends ClickableSpan {
		private String string;
		public MyClickSpan(String string) {
			super();
			this.string = string;
		}
		@Override
		public void updateDrawState(TextPaint ds) {
			super.updateDrawState(ds);
			ds.setColor(mLinkColor);
			ds.setUnderlineText(false);
		}
		@Override
		public void onClick(View widget) {
			if (mOnLinkClickedListener != null) {
				mOnLinkClickedListener.onClicked(LinkedTextView.this, string);
			}
		}
	}
}
