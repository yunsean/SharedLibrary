package com.dylan.dyn3rdparts.pickerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.dylan.dyn3rdparts.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateTimePicker extends LinearLayout {

	private static int START_YEAR = 1950, END_YEAR = 2100;

	private PickerView mYearWheel = null;
	private PickerView mMonthWheel = null;
	private PickerView mDayWheel = null;
	private PickerView mHourWheel = null;
	private PickerView mMinuteWheel = null;
	private PickerView mSecondWheel = null;
	
	private int mSegmentCount = 0;
	
	public DateTimePicker(Context context) {
		this(context, null);
	}
	public DateTimePicker(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public DateTimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.pickerview_date_time, this);
		mYearWheel = (PickerView) findViewById(R.id.year);
		mMonthWheel = (PickerView) findViewById(R.id.month);
		mDayWheel = (PickerView) findViewById(R.id.day);
		mHourWheel = (PickerView) findViewById(R.id.hour);
		mMinuteWheel = (PickerView) findViewById(R.id.minute);
		mSecondWheel = (PickerView) findViewById(R.id.second);

		boolean hasYear = true;
		boolean hasMonth = true;
		boolean hasDay = true;
		boolean hasHour = false;
		boolean hasMinute = false;
		boolean hasSecond = false;
		Drawable background = null;
		Drawable centerPanel = null;
		boolean showShadow = true;

		int topBottomTextColor = 0xffafafaf;
		int centerTextColor = 0xff313131;
		int centerLineColor = 0xffc5c5c5;
		boolean canLoop = true;
		int drawItemsCount = 7;
		int textSize = 18;
		if (attrs != null) {		
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PickerView_DateTime, defStyleAttr, 0);
			if (a != null) {
				hasYear = a.getBoolean(R.styleable.PickerView_DateTime_pvdt_hasYear, true);
				hasMonth = a.getBoolean(R.styleable.PickerView_DateTime_pvdt_hasMonth, true);
				hasDay = a.getBoolean(R.styleable.PickerView_DateTime_pvdt_hasDay, true);
				hasHour = a.getBoolean(R.styleable.PickerView_DateTime_pvdt_hasHour, false);
				hasMinute = a.getBoolean(R.styleable.PickerView_DateTime_pvdt_hasMinute, false);
				hasSecond = a.getBoolean(R.styleable.PickerView_DateTime_pvdt_hasSecond, false);
				background = a.getDrawable(R.styleable.PickerView_DateTime_pvdt_background);
				centerPanel = a.getDrawable(R.styleable.PickerView_DateTime_pvdt_centerPanel);
				showShadow = a.getBoolean(R.styleable.PickerView_DateTime_pvdt_shadow, true);

				topBottomTextColor = a.getColor(R.styleable.PickerView_DateTime_pvdt_topBottomTextColor, 0xffafafaf);
				centerTextColor = a.getColor(R.styleable.PickerView_DateTime_pvdt_centerTextColor, 0xff313131);
				centerLineColor = a.getColor(R.styleable.PickerView_DateTime_pvdt_lineColor, 0xffc5c5c5);
				canLoop = a.getBoolean(R.styleable.PickerView_DateTime_pvdt_canLoop, true);
				textSize = a.getDimensionPixelSize(R.styleable.PickerView_DateTime_pvdt_textSize, 18);
				drawItemsCount = a.getInt(R.styleable.PickerView_DateTime_pvdt_drawItemCount, 7);
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
		
		mYearWheel.setDataList(numberList(START_YEAR, END_YEAR, 0));
		mYearWheel.setCyclic(false);
		mYearWheel.setPostfix("年");
		mYearWheel.setCurrentItem(year - START_YEAR);
		mYearWheel.setStyle(topBottomTextColor, centerTextColor, centerLineColor, canLoop, drawItemsCount, textSize);
		if (hasYear) {
			segmentCount++;
		} else {
			mYearWheel.setVisibility(View.GONE);
		}
		mMonthWheel.setDataList(numberList(1, 12, 0));
		mMonthWheel.setCyclic(false);
		mMonthWheel.setCurrentItem(month);
        mMonthWheel.setPostfix("月");
		mMonthWheel.setStyle(topBottomTextColor, centerTextColor, centerLineColor, canLoop, drawItemsCount, textSize);
		if (hasMonth) {
			segmentCount++;
		} else {
			mMonthWheel.setVisibility(View.GONE);
		}
		mDayWheel.setDataList(numberList(1, dayCountOfMonth(year, month), 0));
		mDayWheel.setCyclic(false);
		mDayWheel.setCurrentItem(day);
		mDayWheel.setPostfix("日");
		mDayWheel.setStyle(topBottomTextColor, centerTextColor, centerLineColor, canLoop, drawItemsCount, textSize);
		if (hasDay) {
			segmentCount++;
		} else {
			mDayWheel.setVisibility(View.GONE);
		}
		mHourWheel.setDataList(numberList(0, 23, 2));
		mHourWheel.setCyclic(false);
		mHourWheel.setCurrentItem(hour);
		mHourWheel.setPostfix("时");
		mHourWheel.setStyle(topBottomTextColor, centerTextColor, centerLineColor, canLoop, drawItemsCount, textSize);
		if (hasHour) {
			segmentCount++;
		} else {
			mHourWheel.setVisibility(View.GONE);
		}
		mMinuteWheel.setDataList(numberList(0, 59, 2));
		mMinuteWheel.setCyclic(false);
		mMinuteWheel.setCurrentItem(minute);
		mMinuteWheel.setPostfix("分");
		mMinuteWheel.setStyle(topBottomTextColor, centerTextColor, centerLineColor, canLoop, drawItemsCount, textSize);
		if (hasMinute) {
			segmentCount++;
		} else {
			mMinuteWheel.setVisibility(View.GONE);
		}
		mSecondWheel.setDataList(numberList(0, 59, 2));
		mSecondWheel.setCyclic(false);
		mSecondWheel.setCurrentItem(second);
		mSecondWheel.setPostfix("秒");
		mSecondWheel.setStyle(topBottomTextColor, centerTextColor, centerLineColor, canLoop, drawItemsCount, textSize);
		if (hasSecond) {
			segmentCount++;
		} else {
			mSecondWheel.setVisibility(View.GONE);
		}
		
		if (hasDay) {
			PickerView.LoopListener listener = new PickerView.LoopListener() {
				@Override
				public void onItemSelect(int item) {
					int year = mYearWheel.getCurrentItem() + START_YEAR;
					int month = mMonthWheel.getCurrentItem();
					int day = mDayWheel.getCurrentItem();
					int dayCount = dayCountOfMonth(year, month);
					if (day >= dayCount) day = dayCount;
					mDayWheel.setDataList(numberList(1, dayCount, 0));
					mDayWheel.setPostfix("日");
					mDayWheel.setCurrentItem(day);
				}
			};
			mYearWheel.setListener(listener);
			mMonthWheel.setListener(listener);
		}
		mSegmentCount = segmentCount;
	}
	
	private List<String> numberList(int begin, int end, int width) {
		List<String> result = new ArrayList<>();
		String fmt = width > 0 ? String.format("%%0%dd", width) : "%d";
		for (int index = begin; index <= end; index++) {
			result.add(String.format(fmt, index));
		}
		return result;
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
			mHourWheel.setCurrentItem(hour);
			mMinuteWheel.setCurrentItem(minute);
			mSecondWheel.setCurrentItem(second);
			mDayWheel.setDataList(numberList(1, dayCountOfMonth(year, month), 0));
			mDayWheel.setPostfix("日");
			mDayWheel.setCurrentItem(day);
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
		calendar.set(Calendar.SECOND, mSecondWheel.getCurrentItem());
		calendar.set(Calendar.MINUTE, mMinuteWheel.getCurrentItem());
		calendar.set(Calendar.HOUR_OF_DAY, mHourWheel.getCurrentItem());
		calendar.set(Calendar.DAY_OF_MONTH, mDayWheel.getCurrentItem() + 1);
		calendar.set(Calendar.MONTH, mMonthWheel.getCurrentItem());
		calendar.set(Calendar.YEAR, mYearWheel.getCurrentItem() + START_YEAR);
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
