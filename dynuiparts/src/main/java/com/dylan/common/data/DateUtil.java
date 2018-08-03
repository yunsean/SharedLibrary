package com.dylan.common.data;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

	public static Date beginOfDay(Date date) {
		long millisecond = date.getTime();
		millisecond += Calendar.getInstance().get(Calendar.ZONE_OFFSET);
		millisecond -= millisecond % (3600 * 24 * 1000);
		millisecond -= Calendar.getInstance().get(Calendar.ZONE_OFFSET);
		return new Date(millisecond);
	}
	public static Date endOfDay(Date date) {
		long millisecond = date.getTime();
		millisecond += Calendar.getInstance().get(Calendar.ZONE_OFFSET);
		millisecond += (3600 * 24 * 1000 - 1) - (millisecond % (3600 * 24 * 1000));
		millisecond -= Calendar.getInstance().get(Calendar.ZONE_OFFSET);
		return new Date(millisecond);
	}
	public static Date dateOffset(Date date, float hour) {
		long millisecond = date.getTime();
		millisecond += (long)(hour * (3600 * 1000));
		return new Date(millisecond);
	}
	public static float offsetHour(Date end, Date begin) {
		long beginMs = begin.getTime();
		long endMs = end.getTime();
		return (endMs - beginMs) / (3600 * 1000);
	}
	public static Calendar beginOfDay(Calendar cal) {
		Calendar result = Calendar.getInstance();
		result.clear();
		result.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		return result;
	}
	public static Calendar endOfDay(Calendar cal) {
		Calendar result = Calendar.getInstance();
		result.clear();
		result.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
		return result;
	}
	public static Calendar adjustTime(Calendar date, int hour, int minute, int second) {
		if (date == null)return null;
		date.set(Calendar.HOUR_OF_DAY, hour);
		date.set(Calendar.MINUTE, minute);
		date.set(Calendar.SECOND, second);
		date.set(Calendar.MILLISECOND, 0);
		date.getTimeInMillis();
		return date;
	}
	
	public static Date fromUTC(long utc) {
		if (utc == 0) return new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(utc);
		return calendar.getTime();
	}
	public static String formatDate(long utc, String format) {
		if (utc == 0) return null;
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(utc);
		String dateString = formatter.format(calendar.getTime());
		return dateString;
	}
	public static String formatDateLong(long utc) {
		if (utc == 0) return null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(utc);
		String dateString = formatter.format(calendar.getTime());
		return dateString;
	}
	public static String formatDateShort(long utc) {
		if (utc == 0) return null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(utc);
		String dateString = dateFormat().format(calendar.getTime());
		return dateString;
	}
	public static String formatTimeShort(long utc) {
		if (utc == 0) return null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(utc);
		String dateString = timeFormat().format(calendar.getTime());
		return dateString;
	}

	public static String formatDate(Date date, String format) {
		if (date == null) return null;
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		String dateString = formatter.format(date);
		return dateString;
	}

	private static SimpleDateFormat dateTimeFormatter = null;
	private static SimpleDateFormat dateFormatter = null;
	private static SimpleDateFormat timeFormatter = null;
	private static SimpleDateFormat dateTimeFormat() {
		if (dateTimeFormatter == null) {
			synchronized (DateUtil.class) {
				dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			}
		}
		return dateTimeFormatter;
	}
	private static SimpleDateFormat dateFormat() {
		if (dateFormatter == null) {
			synchronized (DateUtil.class) {
				dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
			}
		}
		return dateFormatter;
	}
	private static SimpleDateFormat timeFormat() {
		if (timeFormatter == null) {
			synchronized (DateUtil.class) {
				timeFormatter = new SimpleDateFormat("HH:mm:ss");
			}
		}
		return timeFormatter;
	}

	public static String formatDateLong(Date date) {
		if (date == null) return null;
		String dateString = dateTimeFormat().format(date);
		return dateString;
	}
	public static String formatDateShort(Date date) {
		if (date == null) return null;
		String dateString = dateFormat().format(date);
		return dateString;
	}
	public static String formatTimeShort(Date date) {
		if (date == null) return null;
		String dateString = timeFormat().format(date);
		return dateString;
	}
	public static Date toDate(String date, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		try {
			Date strtodate = formatter.parse(date);
			return strtodate;			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new Date();
	}
	public static Date toDateLong(String date) {
		try {
			Date strtodate = dateTimeFormat().parse(date);
			return strtodate;			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new Date();
	}
	public static Date toDate(String date) {
		try {
			Date strtodate = dateFormat().parse(date);
			return strtodate;			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new Date();
	}
	public static Date toTime(String date) {
		try {
			Date strtodate = timeFormat().parse(date);
			return strtodate;			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new Date();
	}
	
	public static int daysBetween(Calendar begin, Calendar end)   {  
		begin.set(Calendar.HOUR_OF_DAY, 0);  
		begin.set(Calendar.MINUTE, 0);  
		begin.set(Calendar.SECOND, 0); 
		begin.set(Calendar.MILLISECOND, 0);
		end.set(Calendar.HOUR_OF_DAY, 0);  
		end.set(Calendar.MINUTE, 0);  
		end.set(Calendar.SECOND, 0); 
		end.set(Calendar.MILLISECOND, 0);
        long time2 = end.getTimeInMillis();         
        long time1 = begin.getTimeInMillis();          
        long between_days = (time2 - time1) / (1000 * 3600 * 24) + 1;
        return (int)between_days;            
    }
	
	@SuppressLint("SimpleDateFormat")
	public  String twoDateDistance(Date startDate, Date endDate){          
        if(startDate == null ||endDate == null){  
            return null;  
        }  
        long timeLong = endDate.getTime() - startDate.getTime();  
        if (timeLong < 60 * 1000) {  
            return timeLong / 1000 + "秒前";
        }
        else if (timeLong < 60 * 60 * 1000){  
            timeLong = timeLong / 1000 / 60;  
            return timeLong + "分钟前";  
        } else if (timeLong < 60 * 60 * 24 * 1000){  
            timeLong = timeLong / 60 / 60 / 1000;  
            return timeLong + "小时前";  
        } else if (timeLong < 60 * 60 * 24 * 1000 * 7){  
            timeLong = timeLong / 1000/ 60 / 60 / 24;  
            return timeLong + "天前";  
        } else if (timeLong < 60 * 60 * 24 * 1000 * 7 * 4){  
            timeLong = timeLong/1000/ 60 / 60 / 24 / 7;  
            return timeLong + "周前";  
        } else {
            return dateTimeFormat().format(startDate);
        }  
	}
	
	public static boolean isSameDay(Calendar left, Calendar right) {
		if (left.get(Calendar.YEAR) != right.get(Calendar.YEAR)) return false;
		else if (left.get(Calendar.DAY_OF_YEAR) != right.get(Calendar.DAY_OF_YEAR)) return false;
		else return true;
	}
	
	public static int getAgeByBirthday(Date birthday) {
		Calendar cal = Calendar.getInstance();
		if (cal.before(birthday)) {
			throw new IllegalArgumentException("The birthDay is before Now.It's unbelievable!");
		}
		int yearNow = cal.get(Calendar.YEAR);
		int monthNow = cal.get(Calendar.MONTH) + 1;
		int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
		cal.setTime(birthday);
		int yearBirth = cal.get(Calendar.YEAR);
		int monthBirth = cal.get(Calendar.MONTH) + 1;
		int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
		int age = yearNow - yearBirth;
		if (monthNow <= monthBirth) {
			if (monthNow == monthBirth) {
				if (dayOfMonthNow < dayOfMonthBirth) {
					age--;
				}
			} else { 
				age--;
			}
		}
		return age;
	}

	public static long timeZoneOffset() {
		return TimeZone.getDefault().getRawOffset();
	}
	public static long timeInMillis(Calendar calendar) {
		long timeInMillis = calendar.getTimeInMillis();
		return timeInMillis + calendar.get(Calendar.ZONE_OFFSET);
	}
	public static long timeInMillis(Date date) {
		long timeInMillis = date.getTime();
		return timeInMillis + date.getTimezoneOffset();
	}
}
