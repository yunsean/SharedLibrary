package com.dylan.uiparts.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.dylan.common.sketch.Observers;
import com.dylan.common.sketch.Observers.OnLayoutChangedListener;
import com.dylan.common.utils.Utility;
import com.dylan.uiparts.R;
import com.dylan.uiparts.wheelview.NumericWheelAdapter;
import com.dylan.uiparts.wheelview.OnWheelChangedListener;
import com.dylan.uiparts.wheelview.WheelAdapter;
import com.dylan.uiparts.wheelview.WheelView;

import java.util.Calendar;
import java.util.Date;

public class DateTimePicker extends LinearLayout {

	private static int START_YEAR = 1990, END_YEAR = 2100;

	private WheelView mYearWheel = null;
	private WheelView mMonthWheel = null;
	private WheelView mDayWheel = null;
	private WheelView mHourWheel = null;
	private WheelView mMinuteWheel = null;
	private WheelView mSecondWheel = null;
	
	private int mSegmentCount = 0;
	
	public DateTimePicker(Context context) {
		this(context, null);
	}
	public DateTimePicker(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public DateTimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.layout_date_time_picker, this);
		mYearWheel = (WheelView) findViewById(R.id.year);
		mMonthWheel = (WheelView) findViewById(R.id.month);
		mDayWheel = (WheelView) findViewById(R.id.day);
		mHourWheel = (WheelView) findViewById(R.id.hour);
		mMinuteWheel = (WheelView) findViewById(R.id.minute);
		mSecondWheel = (WheelView) findViewById(R.id.second);

		boolean hasYear = true;
		boolean hasMonth = true;
		boolean hasDay = true;
		boolean hasHour = false;
		boolean hasMinute = false;
		boolean hasSecond = false;
		Drawable background = null;
		Drawable centerPanel = null;
		boolean showShadow = true;
		if (attrs != null) {		
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DatePicker, defStyleAttr, 0);
			if (a != null) {
				hasYear = a.getBoolean(R.styleable.DatePicker_dp_hasYear, true);
				hasMonth = a.getBoolean(R.styleable.DatePicker_dp_hasMonth, true);
				hasDay = a.getBoolean(R.styleable.DatePicker_dp_hasDay, true);
				hasHour = a.getBoolean(R.styleable.DatePicker_dp_hasHour, false);
				hasMinute = a.getBoolean(R.styleable.DatePicker_dp_hasMinute, false);
				hasSecond = a.getBoolean(R.styleable.DatePicker_dp_hasSecond, false);
				background = a.getDrawable(R.styleable.DatePicker_db_background);
				centerPanel = a.getDrawable(R.styleable.DatePicker_db_centerPanel);
				showShadow = a.getBoolean(R.styleable.DatePicker_dp_shadow, true);
				a.recycle();
			}
		}
		
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH) - 1;
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		int segmentCount = 0;
		
		mYearWheel.setAdapter(new NumericWheelAdapter(START_YEAR, END_YEAR));
		mYearWheel.setCyclic(true);
		mYearWheel.setLabel("年");
		mYearWheel.setCurrentItem(year - START_YEAR);
		mYearWheel.setStyle(background, centerPanel, showShadow);
		if (hasYear) {
			segmentCount++;
		} else {
			mYearWheel.setVisibility(View.GONE);
		}
		mMonthWheel.setAdapter(new NumericWheelAdapter(1, 12));
		mMonthWheel.setCyclic(true);
		mMonthWheel.setLabel("月");
		mMonthWheel.setCurrentItem(month);
		mMonthWheel.setStyle(background, centerPanel, showShadow);
		if (hasMonth) {
			segmentCount++;
		} else {
			mMonthWheel.setVisibility(View.GONE);
		}
		mDayWheel.setAdapter(new NumericWheelAdapter(1, dayCountOfMonth(year, month)));
		mDayWheel.setCyclic(true);
		mDayWheel.setLabel("日");
		mDayWheel.setCurrentItem(day);
		mDayWheel.setStyle(background, centerPanel, showShadow);
		if (hasDay) {
			segmentCount++;
		} else {
			mDayWheel.setVisibility(View.GONE);
		}
		mHourWheel.setAdapter(new WheelAdapter() {
			@Override
			public int getMaximumLength() {
				return 2;
			}
			@Override
			public int getItemsCount() {
				return 24;
			}
			@Override
			public String getItem(int index) {
				return String.format("%02d", index);
			}
		});
		mHourWheel.setCyclic(true);
		mHourWheel.setLabel("时");
		mHourWheel.setCurrentItem(hour);
		mHourWheel.setStyle(background, centerPanel, showShadow);
		if (hasHour) {
			segmentCount++;
		} else {
			mHourWheel.setVisibility(View.GONE);
		}
		mMinuteWheel.setAdapter(new WheelAdapter() {
			@Override
			public int getMaximumLength() {
				return 2;
			}
			@Override
			public int getItemsCount() {
				return 60;
			}
			@Override
			public String getItem(int index) {
				return String.format("%02d", index);
			}
		});
		mMinuteWheel.setCyclic(true);
		mMinuteWheel.setLabel("分");
		mMinuteWheel.setCurrentItem(minute);
		mMinuteWheel.setStyle(background, centerPanel, showShadow);
		if (hasMinute) {
			segmentCount++;
		} else {
			mMinuteWheel.setVisibility(View.GONE);
		}
		mSecondWheel.setAdapter(new WheelAdapter() {
			@Override
			public int getMaximumLength() {
				return 2;
			}
			@Override
			public int getItemsCount() {
				return 60;
			}
			@Override
			public String getItem(int index) {
				return String.format("%02d", index);
			}
		});
		mSecondWheel.setCyclic(true);
		mSecondWheel.setLabel("秒");
		mSecondWheel.setCurrentItem(second);
		mSecondWheel.setStyle(background, centerPanel, showShadow);
		if (hasSecond) {
			segmentCount++;
		} else {
			mSecondWheel.setVisibility(View.GONE);
		}
		
		if (hasDay) {
			mYearWheel.addChangingListener(new OnWheelChangedListener() {
				public void onChanged(WheelView wheel, int oldValue, int newValue) {
					int year = newValue + START_YEAR;
					int month = mMonthWheel.getCurrentItem();
					mDayWheel.setAdapter(new NumericWheelAdapter(1, dayCountOfMonth(year, month)));
				}
			});
			mMonthWheel.addChangingListener(new OnWheelChangedListener() {
				public void onChanged(WheelView wheel, int oldValue, int newValue) {
					int year = mYearWheel.getCurrentItem() + START_YEAR;
					int month = newValue;
					mDayWheel.setAdapter(new NumericWheelAdapter(1, dayCountOfMonth(year, month)));
				}
			});
		}
		mSegmentCount = segmentCount;
		
		int fontSize = Utility.sp2px(context, 18);
		mYearWheel.textSize = fontSize;
		mMonthWheel.textSize = fontSize;
		mDayWheel.textSize = fontSize;
		mHourWheel.textSize = fontSize;
		mMinuteWheel.textSize = fontSize;
		mSecondWheel.textSize = fontSize;
		Observers.observeLayout(this, new OnLayoutChangedListener() {
			@Override
			public boolean onLayoutChanged(View v) {
				int fontSize = Utility.sp2px(v.getContext(), (int)(18 - (mSegmentCount - 3) * 0.5));
				mYearWheel.textSize = fontSize;
				mMonthWheel.textSize = fontSize;
				mDayWheel.textSize = fontSize;
				mHourWheel.textSize = fontSize;
				mMinuteWheel.textSize = fontSize;
				mSecondWheel.textSize = fontSize;
				return false;
			}
		});
	}
	
	public void setDate(Calendar calendar) {
		if (calendar != null) {
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH) - 1;
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			int second = calendar.get(Calendar.SECOND);
			mYearWheel.setCurrentItem(year - START_YEAR);
			mMonthWheel.setCurrentItem(month);
			mDayWheel.setCurrentItem(day);
			mHourWheel.setCurrentItem(hour);
			mMinuteWheel.setCurrentItem(minute);
			mSecondWheel.setCurrentItem(second);
			mDayWheel.setAdapter(new NumericWheelAdapter(1, dayCountOfMonth(year, month)));
		}
	}	
	public void setDate(Date date) {
		if (date == null)return;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		setDate(calendar);
	}
	public void setTime(long utc) {
		Date date = new Date(utc);
		setDate(date);
	}
	public Calendar getDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, mYearWheel.getCurrentItem() + START_YEAR);
		calendar.set(Calendar.MONTH, mMonthWheel.getCurrentItem());
		calendar.set(Calendar.DAY_OF_MONTH, mDayWheel.getCurrentItem() + 1);
		calendar.set(Calendar.HOUR_OF_DAY, mHourWheel.getCurrentItem());
		calendar.set(Calendar.MINUTE, mMinuteWheel.getCurrentItem());
		calendar.set(Calendar.SECOND, mSecondWheel.getCurrentItem());
		return calendar;
	}
	public long getTime() {
		Date date = getDate().getTime();
		return date.getTime();
	}
	
	private int dayCountOfMonth(int year, int month) {
		int[] months_little = { 4, 6, 9, 11 };
		month++;
		if (month == 2) {
			if (isLeapYear(year))return 29;
			else return 28;
		} else {
			for (int i : months_little) {
				if (i == month)return 30;
			}
		}
		return 31;
	}
	private boolean isLeapYear(int year) {
		if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
			return true;
		else
			return false;
	}
}
