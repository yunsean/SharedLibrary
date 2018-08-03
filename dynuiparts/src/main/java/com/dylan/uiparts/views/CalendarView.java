package com.dylan.uiparts.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.dylan.common.utils.Utility;
import com.dylan.uiparts.R;

import java.util.Calendar;
import java.util.Date;

public class CalendarView extends View implements View.OnTouchListener{
	private enum MonthSelect {None, Previous, Next};
	private Date selectedDate = null;
	private Date curDate;
	private Date today; 
	private Date downDate; 
	private Date showFirstDate, showLastDate; 
	private int downIndex;
	private Calendar calendar;
	private Surface surface = new Surface();
	private int[] date = new int[42]; 
	private int[] month = new int[42];
	private int[] year = new int[42];
	private int curStartIndex, curEndIndex; 
	private MonthSelect selectMonth = MonthSelect.None;
	private FlipGesturer mGesturer = null;

	private ICalendarListener listener;
	public CalendarView(Context context) {
		super(context);
		init();
	}
	public CalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public void setDate(Date date) {
		if (date == null) return;
		if (selectedDate == null || (date.getTime() / (3600 * 1000 * 24) != selectedDate.getTime() / (3600 * 100 * 24))) {
			selectedDate = date;
			invalidate();
			if (listener != null) {
				listener.OnDateItemClick(this, selectedDate);
			}
		}
	}
	public Date getDate() {
		return selectedDate;
	}

	protected void init() {
		curDate = today = new Date();
		calendar = Calendar.getInstance();
		curDate = calendar.getTime();
		calendar.setTime(curDate);
		surface.density = getResources().getDisplayMetrics().density;
		setBackgroundColor(surface.bgColor);
		setOnTouchListener(this);
		calculateDate();
	}
	public void enableFlipping() {
		mGesturer = new FlipGesturer(this, new FlipGesturer.OnFilpListener() {
			@Override
			public void onFlipUp() {
			}
			@Override
			public void onFlipRight() {
				calendar.setTime(curDate);
				calendar.add(Calendar.MONTH, 1);
				curDate = calendar.getTime();
				calculateDate();
				invalidate();
			}
			@Override
			public void onFlipLeft() {
				calendar.setTime(curDate);
				calendar.add(Calendar.MONTH, -1);
				curDate = calendar.getTime();
				calculateDate();
				invalidate();
			}
			@Override
			public void onFlipDown() {
			}
		});
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		surface.width = MeasureSpec.getSize(widthMeasureSpec);
		surface.height = surface.width * 3 / 4;
		this.setMeasuredDimension(surface.width, surface.height);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (changed) {
			surface.init();
		}
		super.onLayout(changed, left, top, right, bottom);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		if (surface.backPaint != null) {
			canvas.drawRect(0, 0, surface.width, surface.height, surface.backPaint);
		}
		if (surface.headerBackPaint != null) {
			canvas.drawPath(surface.headerPath, surface.headerBackPaint);
		}
		if (surface.bodyBackPaint != null) {
			canvas.drawRect(0, surface.monthHeight + surface.weekHeight, surface.height - surface.width, surface.monthHeight + surface.weekHeight, surface.headerBackPaint);
		}
		canvas.drawPath(surface.headerLine, surface.borderPaint);
		canvas.drawPath(surface.boxPath, surface.borderPaint);
		if (surface.monthLeftImage != null) {
			canvas.drawBitmap(surface.monthLeftImage, new Rect(0, 0, surface.monthLeftImage.getWidth(), surface.monthLeftImage.getHeight()), surface.monthLeftRect, null);
		}
		if (surface.monthRightImage != null) {
			canvas.drawBitmap(surface.monthRightImage, surface.monthImageRect, surface.monthRightRect, null);
		}
		
		drawDownOrSelectedBg(canvas);
		int todayIndex = -1;
		calendar.setTime(curDate);
		String curYearAndMonth = calendar.get(Calendar.YEAR) + "" + calendar.get(Calendar.MONTH);
		calendar.setTime(today);
		String todayYearAndMonth = calendar.get(Calendar.YEAR) + "" + calendar.get(Calendar.MONTH);
		if (curYearAndMonth.equals(todayYearAndMonth)) {
			int todayNumber = calendar.get(Calendar.DAY_OF_MONTH);
			todayIndex = curStartIndex + todayNumber - 1;
		}
		for (int i = 0; i < 42; i++) {
			drawCellDecorate(canvas, i);
			int color = surface.dateColor;
			if (isLastMonth(i)) {
				color = surface.auxDateColor;
			} else if (isNextMonth(i)) {
				color = surface.auxDateColor;
			}
			if (todayIndex != -1 && i == todayIndex) {
				color = surface.todayNumberColor;
			}
			drawCellText(canvas, i, Integer.toString(getDayAtIndex(i)), color);
		}
		
		calendar.setTime(curDate);
		String text = calendar.get(Calendar.YEAR) + "年" + (calendar.get(Calendar.MONTH) + 1) + "月";
		float textTop = (surface.monthHeight - getTextHeight(surface.monthPaint)) / 2 + getFontLeading(surface.monthPaint);
		float textLeft = (surface.width - getTextWidth(surface.monthPaint, text)) / 2;
		canvas.drawText(text, textLeft, textTop, surface.monthPaint);
		float weekTextY = surface.monthHeight + surface.cellMargin / 2 + surface.weekHeight / 2;
		for (int i = 0; i < surface.weekText.length; i++) {
			float weekTextX = surface.cellMargin + i * surface.cellWidth + (surface.cellWidth - surface.weekPaint.measureText(surface.weekText[i])) / 2f;
			canvas.drawText(surface.weekText[i], weekTextX, weekTextY, surface.weekPaint);
		}
		super.onDraw(canvas);
	}

	private void calculateDate() {
		calendar.setTime(curDate);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		int monthInYear = calendar.get(Calendar.MONTH);
		int dayInWeek = calendar.get(Calendar.DAY_OF_WEEK);
		int yearInCal = calendar.get(Calendar.YEAR);
		int monthStart = dayInWeek;
		if (monthStart == 1) {
			monthStart = 8;
		}
		monthStart -= 1; 
		curStartIndex = monthStart;
		date[monthStart] = 1;
		month[monthStart] = monthInYear + 1;
		year[monthStart] = yearInCal;
		if (monthStart > 0) {
		    calendar.set(Calendar.DAY_OF_MONTH, 0);
		    int dayInmonth = calendar.get(Calendar.DAY_OF_MONTH);
		    monthInYear = calendar.get(Calendar.MONTH);
		    yearInCal = calendar.get(Calendar.YEAR);
		    for (int i = monthStart - 1; i >= 0; i--) {
		    	date[i] = dayInmonth;
				month[i] = monthInYear + 1;
				year[i] = yearInCal;
		    	dayInmonth--;
		    }
		    calendar.set(Calendar.DAY_OF_MONTH, date[0]);
		}
		showFirstDate = calendar.getTime();
		calendar.setTime(curDate);
		calendar.add(Calendar.MONTH, 1);
		calendar.set(Calendar.DAY_OF_MONTH, 0);
		int monthDay = calendar.get(Calendar.DAY_OF_MONTH);
		monthInYear = calendar.get(Calendar.MONTH);
	    yearInCal = calendar.get(Calendar.YEAR);
		for (int i = 1; i < monthDay; i++) {
			date[monthStart + i] = i + 1;
			month[monthStart + i] = monthInYear + 1;
			year[monthStart + i] = yearInCal;
		}
		calendar.setTime(curDate);
		curEndIndex = monthStart + monthDay;
		calendar.add(Calendar.MONTH, 1);
		monthInYear = calendar.get(Calendar.MONTH);
	    yearInCal = calendar.get(Calendar.YEAR);
		for (int i = monthStart + monthDay; i < 42; i++) {
			date[i] = i - (monthStart + monthDay) + 1;
			month[i] = monthInYear + 1;
			year[i] = yearInCal;
		}
		if (curEndIndex < 42) {
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		calendar.set(Calendar.DAY_OF_MONTH, date[41]);
		showLastDate = calendar.getTime();
		if (listener != null) {
			listener.OnMonthChanged(this, showFirstDate, showLastDate);
		}
	}
	
	protected Surface getSurface() {
		return surface;
	}
	public void setSurface(Surface surface) {
		this.surface = surface;
	}

	protected RectF getCellRect(int index) {
		int x = getXByIndex(index);
		int y = getYByIndex(index);
		float left = 1 + surface.cellMargin + surface.cellWidth * (x - 1) + surface.borderWidth;
		float top = surface.monthHeight + surface.weekHeight + surface.cellMargin + (y - 1) * surface.cellHeight + surface.borderWidth;
		return new RectF(left, top, left + surface.cellWidth - surface.borderWidth, top + surface.cellHeight - surface.borderWidth);
	}
	protected void drawCellText(Canvas canvas, int index, String text, int color) {
		int x = getXByIndex(index);
		int y = getYByIndex(index);
		surface.datePaint.setColor(color);
		float cellY = surface.monthHeight + surface.weekHeight + surface.cellMargin + (y - 1) * surface.cellHeight + surface.cellHeight * 3 / 4f;
		float cellX = surface.cellMargin + (surface.cellWidth * (x - 1)) + (surface.cellWidth - surface.datePaint.measureText(text)) / 2f;
		canvas.drawText(text, cellX, cellY, surface.datePaint);
	}
	protected void drawCellDecorate(Canvas canvas, int index) {
		
	}
	protected void drawCellBg(Canvas canvas, int index, int color) {
		surface.cellBgPaint.setColor(color);
		canvas.drawRect(getCellRect(index), surface.cellBgPaint);
	}
	protected void drawDownOrSelectedBg(Canvas canvas) {
		if (selectedDate != null && !selectedDate.before(showFirstDate) && !selectedDate.after(showLastDate)) {
			int index = findSelectedIndex();
			if (index >= 0 && index <= 41) {
				drawCellBg(canvas, index, surface.cellSelectedColor);
			}
		}
	    if (downDate != null) {
	    	drawCellBg(canvas, downIndex, surface.cellDownColor);
	    }
	}
	private int findSelectedIndex() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(selectedDate);
		int yearInCal = cal.get(Calendar.YEAR);
		int monthInYear = cal.get(Calendar.MONTH) + 1;
		int dayInMonth = cal.get(Calendar.DAY_OF_MONTH);
		for (int i = 0; i < 42; i++) {
			if (yearInCal == year[i] && monthInYear == month[i] && dayInMonth == date[i]) {
				return i;
			}
		}
		return -1;
	}
	protected static float getTextWidth(Paint paint, String str) {  
        return paint.measureText(str);  
    }   
	protected static float getTextHeight(Paint paint)  {    
        FontMetrics fm = paint.getFontMetrics();   
        return fm.descent - fm.ascent;    
    }  
    protected static float getFontLeading(Paint paint)  {    
        FontMetrics fm = paint.getFontMetrics();   
        return fm.leading- fm.ascent;    
    }

	public Date getShownFirstDate() {
		return showFirstDate;
	}
	public Date getShownLastDate() {
		return showLastDate;
	}
	public int getMonthAtIndex(int index) {
		return month[index];
	}
	public int getDayAtIndex(int index) {
		return date[index];
	}
	public Date getDateAtIndex(int index) {
		calendar.setTime(showFirstDate);
		calendar.add(Calendar.DAY_OF_YEAR, index);
		return calendar.getTime();
	}

	private boolean isLastMonth(int i) {
		if (i < curStartIndex) {
			return true;
		}
		return false;
	}
	private boolean isNextMonth(int i) {
		if (i >= curEndIndex) {
			return true;
		}
		return false;
	}
	private int getXByIndex(int i) {
		return i % 7 + 1; 
	}
	private int getYByIndex(int i) {
		return i / 7 + 1; 
	}
	
	public String getYearAndmonth() {
		calendar.setTime(curDate);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		return year + "-" + surface.monthText[month];
	}
	public String clickLeftMonth(){
		calendar.setTime(curDate);
		calendar.add(Calendar.MONTH, -1);
		curDate = calendar.getTime();
		invalidate();
		return getYearAndmonth();
	}
	public String clickRightMonth(){
		calendar.setTime(curDate);
		calendar.add(Calendar.MONTH, 1);
		curDate = calendar.getTime();
		invalidate();
		return getYearAndmonth();
	}
	private void setSelectedDateByCoor(float x, float y) {
		if (y > surface.monthHeight + surface.weekHeight) {
			int m = (int) (Math.floor(x / surface.cellWidth) + 1);
			int n = (int) (Math.floor((y - (surface.monthHeight + surface.weekHeight)) / Float.valueOf(surface.cellHeight)) + 1);
			downIndex = (n - 1) * 7 + m - 1;
			calendar.setTime(curDate);
			if (isLastMonth(downIndex)) {
				calendar.add(Calendar.MONTH, -1);
			} else if (isNextMonth(downIndex)) {
				calendar.add(Calendar.MONTH, 1);
			}
			calendar.set(Calendar.DAY_OF_MONTH, date[downIndex]);
			downDate = calendar.getTime();
		}
		invalidate();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			setSelectedDateByCoor(event.getX(), event.getY());
			testMonthSelect(event.getX(), event.getY());
			break;
		case MotionEvent.ACTION_UP:
			if (downDate != null) {
				selectedDate = downDate;
				if (listener != null) {
					listener.OnDateItemClick(this, selectedDate);
				}
				downDate = null;
				invalidate();
			} else if (selectMonth == MonthSelect.Previous) {
				calendar.setTime(curDate);
				calendar.add(Calendar.MONTH, -1);
				curDate = calendar.getTime();
				calculateDate();
				invalidate();
				selectMonth = MonthSelect.None;
			} else if (selectMonth == MonthSelect.Next) {
				calendar.setTime(curDate);
				calendar.add(Calendar.MONTH, 1);
				curDate = calendar.getTime();
				calculateDate();
				invalidate();
				selectMonth = MonthSelect.None;
			}
			break;
		}
		if (mGesturer != null) {
			mGesturer.onTouchEvent(event);
		}
		return true;
	}
	
	public void setListener(ICalendarListener onItemClickListener){
		this.listener =  onItemClickListener;
	}
	public interface ICalendarListener {
		void OnDateItemClick(CalendarView v, Date date);
		void OnMonthChanged(CalendarView v, Date shownFirstDate, Date shownLastDate);
	}
	
	private void testMonthSelect(float x, float y) {
		if (y < 0 || y > surface.monthHeight) {
			selectMonth = MonthSelect.None;
		} else if (x < (surface.monthLeftRect.right + 30)) {
			selectMonth = MonthSelect.Previous;
		} else if (x > (surface.monthRightRect.left - 30)) {
			selectMonth = MonthSelect.Next;
		} else {
			selectMonth = MonthSelect.None;
		}
	}
	
	protected class Surface {
		public float density;
		public int width; 
		public int height; 
		public float monthHeight; 
		public float weekHeight; 
		public float cellWidth; 
		public float cellHeight; 	
		public float borderWidth;
		public int bgColor = 0xffffffff;
		public int headerColor = 0xffefefef;
		public int weekColor = 0xff555555;
		public int dateColor = Color.BLACK;
		public int auxDateColor = Color.DKGRAY;
		public int btnColor = 0xff666666;
		public int borderColor = 0xffCCCCCC;
		public int todayNumberColor = Color.RED;
		public int cellDownColor = 0xffCCFFFF;
		public int cellSelectedColor = 0xff00a2e9;

		public float[] headerRadii = {15f, 15f, 15f, 15f, 0f, 0f, 0f, 0f};

		public Paint backPaint;
		public Paint headerBackPaint;
		public Paint bodyBackPaint;
		public Paint borderPaint;
		public Paint monthPaint;
		public Paint weekPaint;
		public Paint datePaint;
		public Paint monthChangeBtnPaint;
		public Paint cellBgPaint;
		public Path headerLine;
		public Path boxPath; 
		public Path headerPath;
		public RectF monthLeftRect;
		public RectF monthRightRect;
		public Bitmap monthLeftImage;
		public Bitmap monthRightImage;
		public Rect monthImageRect;
		public String[] weekText = { "周日", "周一", "周二", "周三", "周四", "周五", "周六"};
		public String[] monthText = {"Jan", "Feb", "Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

		public float cellTextSize = 16.f;
		public float weekTextSize = 13.f;
		public float monthTextSize = 16.f;

		public int leftArrowResId = R.drawable.calendar_leftarrow;
		public int rightArrowResId = R.drawable.calendar_rightarrow;

		public int cellMargin = 0;
	     
		public void init() {
		    float temp = height / 7f;
		    monthHeight = (float) ((temp + temp * 0.1f) * 0.8);
		    weekHeight = (float) ((temp + temp * 0.3f) * 0.5);
			cellMargin = Utility.dip2px(getContext(), 10);
		    cellHeight = (height - monthHeight - weekHeight - cellMargin - cellMargin - 1) / 6f;
		    cellWidth = (width - cellMargin - cellMargin - 2) / 7f;
		    
		    headerBackPaint = new Paint();
		    headerBackPaint.setStyle(Style.FILL);
		    headerBackPaint.setColor(headerColor);

			backPaint = new Paint();
			backPaint.setStyle(Style.FILL);
			backPaint.setColor(bgColor);
		    
		    headerPath = new Path();
		    RectF headerRect = new RectF(0, 0, surface.width, surface.monthHeight + surface.weekHeight);
		    headerPath.addRoundRect(headerRect, headerRadii, Path.Direction.CW);
		    
		    monthLeftImage = BitmapFactory.decodeResource(getResources(), leftArrowResId);
		    monthRightImage = BitmapFactory.decodeResource(getResources(), rightArrowResId);
		    float h = monthLeftImage.getHeight();
		    float w = monthLeftImage.getWidth();
		    monthImageRect = new Rect(0, 0, (int)w, (int)h);
			if (h > monthHeight - 4) {
				w = (monthHeight - 4) * w / h;
				h = monthHeight - 4;
			}
		    monthLeftRect = new RectF();
		    monthLeftRect.left = Utility.dip2px(getContext(), 15);
		    monthLeftRect.right = monthLeftRect.left + w;
		    monthLeftRect.top = (monthHeight - h) / 2;
		    monthLeftRect.bottom = monthLeftRect.top + h;
		    monthRightRect = new RectF();
		    monthRightRect.right = width - Utility.dip2px(getContext(), 15);
		    monthRightRect.left = monthRightRect.right - w;
		    monthRightRect.top = (monthHeight - h) / 2;
		    monthRightRect.bottom = monthRightRect.top + h;
		    borderPaint = new Paint();
		    borderPaint.setColor(borderColor);
		    borderPaint.setStyle(Paint.Style.STROKE);
		    borderWidth = (float) (0.5 * density);
		    borderWidth = borderWidth < 1 ? 1 : borderWidth;
		    borderPaint.setStrokeWidth(borderWidth);
		    
		    monthPaint = new Paint();
		    monthPaint.setColor(weekColor);
		    monthPaint.setAntiAlias(true);
			monthPaint.setTextSize(Utility.dip2px(getContext(), monthTextSize));
		    monthPaint.setTypeface(Typeface.DEFAULT);
		    
		    weekPaint = new Paint();
		    weekPaint.setColor(weekColor);
		    weekPaint.setAntiAlias(true);
		    weekPaint.setTextSize(Utility.dip2px(getContext(), weekTextSize));
		    weekPaint.setTypeface(Typeface.DEFAULT);
		    
		    datePaint = new Paint();
		    datePaint.setColor(dateColor);
		    datePaint.setAntiAlias(true);
		    datePaint.setTextSize(Utility.dip2px(getContext(), cellTextSize));
		    datePaint.setTypeface(Typeface.DEFAULT);

			headerLine = new Path();
			headerLine.moveTo(0, monthHeight);
			headerLine.lineTo(width, monthHeight);

		    boxPath = new Path();
		    for (int i = 0; i < 7; i++) {
		    	boxPath.moveTo(cellMargin, monthHeight + weekHeight + cellMargin + i * cellHeight);
		    	boxPath.rLineTo(width - cellMargin - cellMargin, 0);
		    	boxPath.moveTo(cellMargin + 1 + i * cellWidth, monthHeight + cellMargin + weekHeight);
		    	boxPath.rLineTo(0, height - cellMargin - cellMargin - monthHeight - weekHeight);
		    }
		    boxPath.moveTo(cellMargin + 7 * cellWidth, monthHeight + weekHeight + cellMargin);
		    boxPath.rLineTo(0, height - cellMargin - cellMargin - monthHeight - weekHeight);
		    
		    monthChangeBtnPaint = new Paint();
		    monthChangeBtnPaint.setAntiAlias(true);
		    monthChangeBtnPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		    monthChangeBtnPaint.setColor(btnColor);
		    
		    cellBgPaint = new Paint();
		    cellBgPaint.setAntiAlias(true);
		    cellBgPaint.setStyle(Paint.Style.FILL);
		    cellBgPaint.setColor(cellSelectedColor);
		}
	}
}
