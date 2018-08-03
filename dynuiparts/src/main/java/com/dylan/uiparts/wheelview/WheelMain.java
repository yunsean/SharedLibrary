package com.dylan.uiparts.wheelview;

import java.util.Arrays;
import java.util.List;

import com.dylan.uiparts.R;

import android.view.View;

public class WheelMain {
	private View view;
	private WheelView wv_year;
	private WheelView wv_month;
	private WheelView wv_day;
	private WheelView wv_hours;
	private WheelView wv_mins;
	public int screenheight;
	private boolean hasSelectHour;
	private boolean hasSelectMin;
	private static int START_YEAR = 1990, END_YEAR = 2100;

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public static int getSTART_YEAR() {
		return START_YEAR;
	}

	public static void setSTART_YEAR(int sTART_YEAR) {
		START_YEAR = sTART_YEAR;
	}

	public static int getEND_YEAR() {
		return END_YEAR;
	}

	public static void setEND_YEAR(int eND_YEAR) {
		END_YEAR = eND_YEAR;
	}

	public WheelMain(View view) {
		super();
		this.view = view;
		hasSelectHour = false;
		hasSelectMin = false;
		setView(view);
	}
	public WheelMain(View view, boolean hasSelectHour, boolean hasSelectMin) {
		super();
		this.view = view;
		this.hasSelectHour = hasSelectHour;
		this.hasSelectMin = hasSelectMin;
		setView(view);
	}
	public void initDateTimePicker(int year ,int month,int day){
		this.initDateTimePicker(year, month, day, 0, 0);
	}

	public void initDateTimePicker(int year ,int month ,int day,int h,int m) {
		String[] months_big = { "1", "3", "5", "7", "8", "10", "12" };
		String[] months_little = { "4", "6", "9", "11" };

		final List<String> list_big = Arrays.asList(months_big);
		final List<String> list_little = Arrays.asList(months_little);

		wv_year = (WheelView) view.findViewById(R.id.year);
		wv_year.setAdapter(new NumericWheelAdapter(START_YEAR, END_YEAR));
		wv_year.setCyclic(true);
		wv_year.setLabel("年");
		wv_year.setCurrentItem(year - START_YEAR);

		wv_month = (WheelView) view.findViewById(R.id.month);
		wv_month.setAdapter(new NumericWheelAdapter(1, 12));
		wv_month.setCyclic(true);
		wv_month.setLabel("月");
		wv_month.setCurrentItem(month);

		wv_day = (WheelView) view.findViewById(R.id.day);
		wv_day.setCyclic(true);
		if (list_big.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 31));
		} else if (list_little.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 30));
		} else {
			if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
				wv_day.setAdapter(new NumericWheelAdapter(1, 29));
			else
				wv_day.setAdapter(new NumericWheelAdapter(1, 28));
		}
		wv_day.setLabel("日");
		wv_day.setCurrentItem(day - 1);

		wv_hours = (WheelView)view.findViewById(R.id.hour);
		wv_mins = (WheelView)view.findViewById(R.id.min);
		if(hasSelectHour){
			wv_hours.setVisibility(View.VISIBLE);			
			wv_hours.setAdapter(new NumericWheelAdapter(0, 23));
			wv_hours.setCyclic(true);
			wv_hours.setLabel("时");
			wv_hours.setCurrentItem(h);
		} else {
			wv_hours.setVisibility(View.GONE);
		}
		if(hasSelectMin){
			wv_mins.setVisibility(View.VISIBLE);			
			wv_mins.setAdapter(new NumericWheelAdapter(0, 59));
			wv_mins.setCyclic(true);
			wv_mins.setLabel("分");
			wv_mins.setCurrentItem(m);
		} else {
			wv_mins.setVisibility(View.GONE);
		}
		
		OnWheelChangedListener wheelListener_year = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int year_num = newValue + START_YEAR;
				if (list_big.contains(String.valueOf(wv_month.getCurrentItem() + 1))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 31));
				} else if (list_little.contains(String.valueOf(wv_month.getCurrentItem() + 1))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 30));
				} else {
					if ((year_num % 4 == 0 && year_num % 100 != 0) || year_num % 400 == 0)
						wv_day.setAdapter(new NumericWheelAdapter(1, 29));
					else
						wv_day.setAdapter(new NumericWheelAdapter(1, 28));
				}
			}
		};
		OnWheelChangedListener wheelListener_month = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int month_num = newValue + 1;
				if (list_big.contains(String.valueOf(month_num))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 31));
				} else if (list_little.contains(String.valueOf(month_num))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 30));
				} else {
					if (((wv_year.getCurrentItem() + START_YEAR) % 4 == 0 && (wv_year.getCurrentItem() + START_YEAR) % 100 != 0) || (wv_year.getCurrentItem() + START_YEAR) % 400 == 0)
						wv_day.setAdapter(new NumericWheelAdapter(1, 29));
					else
						wv_day.setAdapter(new NumericWheelAdapter(1, 28));
				}
			}
		};
		wv_year.addChangingListener(wheelListener_year);
		wv_month.addChangingListener(wheelListener_month);

		int textSize = 0;
		if(hasSelectHour)
			textSize = (screenheight / 100) * 3;
		else
			textSize = (screenheight / 100) * 4;
		wv_day.textSize = textSize;
		wv_month.textSize = textSize;
		wv_year.textSize = textSize;
		wv_hours.textSize = textSize;
		wv_mins.textSize = textSize;
	}

	public String getTime() {
		StringBuffer sb = new StringBuffer();
		sb.append((wv_year.getCurrentItem() + START_YEAR)).append("-").append((wv_month.getCurrentItem() + 1)).append("-").append((wv_day.getCurrentItem() + 1));
		if (hasSelectHour) {
			if (hasSelectMin)sb.append(" ").append(wv_hours.getCurrentItem()).append(":").append(wv_mins.getCurrentItem());
			else sb.append(" ").append(wv_hours.getCurrentItem()).append(":00");
		}
		return sb.toString();
	}
}
