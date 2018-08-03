package com.dylan.uiparts.wheelview;

import java.util.Calendar;
import java.util.Date;

import com.dylan.common.utils.Utility;
import com.dylan.uiparts.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class DatePicker {

	private WheelMain mWheelMain = null;
	public DatePicker(Context context) {
		this(context, Calendar.getInstance());
	}
	@SuppressLint("InflateParams")
	public DatePicker(Context context, Calendar calendar) {
		this(context, calendar, false, false);
	}
	@SuppressLint("InflateParams")
	public DatePicker(Context context, Calendar calendar, boolean hasHour, boolean hasMin) {
		if (calendar == null)calendar = Calendar.getInstance();
		LayoutInflater inflater = LayoutInflater.from(context);
		View timepickerview = inflater.inflate(R.layout.wheelview_timepicker, null);
		mWheelMain = new WheelMain(timepickerview, hasHour, hasMin);
		mWheelMain.screenheight = Utility.getScreenHeight(context);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = hasHour ? calendar.get(Calendar.HOUR_OF_DAY) : 0;
		int min = hasMin ? calendar.get(Calendar.MINUTE) : 0;
		mWheelMain.initDateTimePicker(year, month, day, hour, min);
	}
	@SuppressLint("InflateParams")
	public DatePicker(Context context, Date date) {
		this(context, date, false, false);
	}
	@SuppressLint("InflateParams")
	public DatePicker(Context context, Date date, boolean hasHour, boolean hasMin) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		LayoutInflater inflater = LayoutInflater.from(context);
		View timepickerview = inflater.inflate(R.layout.wheelview_timepicker, null);
		mWheelMain = new WheelMain(timepickerview, hasHour, hasMin);
		mWheelMain.screenheight = Utility.getScreenHeight(context);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		mWheelMain.initDateTimePicker(year, month, day);
	}
	public View getView() {
		if (mWheelMain == null)return null;
		return mWheelMain.getView();
	}
	public String getTime() {
		if (mWheelMain == null)return null;
		return mWheelMain.getTime();
	}
}
