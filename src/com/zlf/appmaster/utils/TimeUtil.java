package com.zlf.appmaster.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具类
 * 
 * @author way
 * 
 */
@SuppressLint("SimpleDateFormat")
public class TimeUtil {
//	private static final String TAG = "TimeUtil";
	private static final long INTERVAL_IN_MILLISECONDS = 30 * 1000;

	public static String getTime(long time) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(new Date(time));
	}

	public static String getTimeWithoutSec(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(new Date(time));
    }

	public static String getSimpleTime(long time) {
		SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
		return format.format(new Date(time));
	}
	
	public static String getYearAndDay(long time){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(new Date(time));
	}
	
	public static String getYearAndMonth(long time){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
		return format.format(new Date(time));
	}
	
	public static String getMonthAndDay(long time) {
		SimpleDateFormat format = new SimpleDateFormat("MM-dd");
		return format.format(new Date(time));
	}
	
	public static String getHourAndMin(long time) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		return format.format(new Date(time));
	}

	public static String getNewsSimpleTime(long timesamp){
		String result = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		Date today = new Date(System.currentTimeMillis());
		Date otherDay = new Date(timesamp);
		int temp = Integer.parseInt(sdf.format(today))
				- Integer.parseInt(sdf.format(otherDay));

		if(temp>0){	// 年份大于1 显示年份
			result = getYearAndDay(timesamp);
		}
		else
			result = getMonthAndDay(timesamp);

		return result;	
	}
	
	private final static long ONE_DAY = 24*3600*1000L;

	public static String getChatTime(long timesamp) {
		String ret = "";

		Calendar now = Calendar.getInstance();
		long ms = 1000 * (now.get(Calendar.HOUR_OF_DAY) * 3600
				+ now.get(Calendar.MINUTE) * 60 + now.get(Calendar.SECOND));// 毫秒数
		long ms_now = now.getTimeInMillis();
		if (ms_now - timesamp < ms) {
			//ret = "今天 " + getHourAndMin(timesamp);
            ret = getHourAndMin(timesamp);
		} else if (ms_now - timesamp < (ms + ONE_DAY)) {
			ret = "昨天 " + getHourAndMin(timesamp);
		} /*else if (ms_now - timesamp < (ms + ONE_DAY * 2)) {
			ret = "前天 " + getHourAndMin(timesamp);
		} */else if (ms_now - timesamp < (ms + ONE_DAY * 365)) {
			ret = getSimpleTime(timesamp);
		} else {
			// ret= "更早";
			ret = getTime(timesamp);
		}
		return ret;
	}
	
	/**
     * 返回股友圈格式描述的日期 (几分钟前、几天前、几月前...) 
     */
    public static String getFriendsZoneTime(long milliseconds) {
    	Date date = new Date(milliseconds);
    	final long minute = 60 * 1000;// 1分钟
        final long hour = 60 * minute;// 1小时
        final long day = 24 * hour;// 1天
        final long month = 31 * day;// 月
        final long year = 12 * month;// 年

        long diff = new Date().getTime() - date.getTime();
        long r = 0;
        if (diff > year) {
            r = (diff / year);
            return r + "年前";
        }
        if (diff > month) {
            r = (diff / month);
            return r + "个月前";
        }
        if (diff > day) {
            r = (diff / day);
            return r + "天前";
        }
        if (diff > hour) {
            r = (diff / hour);
            return r + "个小时前";
        }
        if (diff > minute) {
            r = (diff / minute);
            return r + "分钟前";
        }
        return "刚刚";
    }

	/**
	 * 更新时间<60分钟，显示"刚刚"
	 1小时<更新时间<24小时，显示"X小时前"
	 24小时<更新时间<30天，显示“X天前”
	 30天<更新时间<90天，显示X月前
	 更新时间>90天，显示20XX.XX.XX，显示更新日期
	 * @return
     */
	public static String getShowTime(long ctime){
		Date date = new Date(ctime);
		final long minute = 60 * 1000;// 1分钟
		final long hour = 60 * minute;// 1小时
		final long day = 24 * hour;// 1天
		final long month = 30 * day;// 月

		long diff = new Date().getTime() - date.getTime();
		long r = 0;
		if (diff > month*3) {
			return getYearAndDay(ctime);
		}
		if (diff > month) {
			r = (diff / month);
			return r + "个月前";
		}
		if (diff > day) {
			r = (diff / day);
			return r + "天前";
		}
		if (diff > hour) {
			r = (diff / hour);
			return r + "小时前";
		}
		return "刚刚";
	}
	/**
	 * 	timer_key:0表示没有设置时间；1：表示开播时间类型为每天；2表示按照周来计算；3：表示按照月来计算
	 *	timer_val:格式1|2|3|4|5 ；如果timer_key=0/1,这该值为空。如果timer_key=2:表示每周1，2，3，4，5；如果timer_key=3,则表示每月1，2，3，4，5号
	 * 	timer_hhmm:表示开播时间，从今天凌晨00：00：00开始经过的秒数
	 * 	描述：节目名称及开播时间，开播时间规范：
	 a.每周五、六 14:00开播
	 b.交易日 14:00开播
	 c.每月15日 14:00开播
	 */
	public static String getShowPeriodTime(int timer_key, String timer_val, long timer_hhmm){
		if(timer_key == 1){
			return "每天"+getOfZeroClockInterval(timer_hhmm)+"开播";
		}
		if(timer_key == 2){
			if(timer_val != null && !TextUtils.isEmpty(timer_val)){
				if(timer_val.equals("1|2|3|4|5")){
					return "交易日"+ getOfZeroClockInterval(timer_hhmm)+"开播";
				}else {
					return "每周"+timer_val.replace("|",",")+"日"+ getOfZeroClockInterval(timer_hhmm)+"开播";
				}
			}else{
				return "";
			}
		}
		if(timer_key == 3){
			if(timer_val != null && !TextUtils.isEmpty(timer_val)){
				return "每月"+timer_val.replace("|",",")+"日"+ getOfZeroClockInterval(timer_hhmm)+"开播";
			}else{
				return "";
			}
		}
		return "";
	}

	public static String getOfZeroClockInterval(long timesamp) {
		return new DecimalFormat("00").format((timesamp / 3600)) +":"+
				new DecimalFormat("00").format((timesamp % 3600) / 60) ;
	}

	/**
	 * 判断时间是否为今天
	 * @param timesamp
	 * @return
	 */
	public static boolean isToday(long timesamp) {
		Calendar now = Calendar.getInstance();
		long ms = 1000 * (now.get(Calendar.HOUR_OF_DAY) * 3600
				+ now.get(Calendar.MINUTE) * 60 + now.get(Calendar.SECOND));// 毫秒数
		long ms_now = now.getTimeInMillis();
		if (ms_now - timesamp < ms) {
			return true;
		}

		return false;

	}
	
	/**
     * 取得某天所在周周六 距离现在的时间
     * 
     */ 
    public static long getLastDayOfWeekInterval(long timeInMillis) { 
    	Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setTimeInMillis(timeInMillis);// 设置当前时间
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek() + 5); // 周一加5天
        
        c.clear(Calendar.HOUR);
        c.clear(Calendar.HOUR_OF_DAY);
        c.clear(Calendar.MINUTE);
        c.clear(Calendar.SECOND);
        //Log.i(TAG, getTime(c.getTimeInMillis()));
        return c.getTimeInMillis() - timeInMillis; 
   } 
    
    /**
     * 取得某天所在月的最后一天距离现在的时间
     * 
     */ 
    public static long getLastDayOfMonthInterval(long timeInMillis) { 
    	Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timeInMillis);// 设置当前时间
        c.set(Calendar.DAY_OF_MONTH, c.getMaximum(Calendar.DAY_OF_MONTH));
        
        c.clear(Calendar.HOUR);
        c.clear(Calendar.HOUR_OF_DAY);
        c.clear(Calendar.MINUTE);
        c.clear(Calendar.SECOND);
        
        return c.getTimeInMillis() - timeInMillis; 
   } 
    
    /**
     * 将格式化的日期类型转换为long
     * @return
     */
    public static long convertDateToTime(String str){
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	try {
			Date date = format.parse(str);
			return date.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return 0;
    }

    /**
     * 获取年与季度
     * 1-3 为1季度
     * 4-6 为2季度
     * 7-9 为3季度
     * 10-12 为4季度
     */
    public static String getQuarterTime(long time){
        Date date = new Date(time);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int qNo = c.get(Calendar.MONTH)/3 + 1;
        return year+"Q"+qNo;
    }

    public static int isSameDay(long time1,long time2){
        String day1 = getYearAndDay(time1);
        String day2 = getYearAndDay(time2);
        return day1.compareTo(day2);
    }

	/**
	 * 格式化为日
	 */
	public static long formatDayTime(long time){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		c.clear(Calendar.HOUR);
		c.clear(Calendar.HOUR_OF_DAY);
		c.clear(Calendar.MINUTE);
		c.clear(Calendar.SECOND);
		c.clear(Calendar.MILLISECOND);
		return c.getTimeInMillis();
	}


	public static boolean isCloseEnough(long time1, long time2) {
		long delta = time1 - time2;
		if (delta < 0) {
			delta = -delta;
		}
		return delta < INTERVAL_IN_MILLISECONDS;
	}

	/** 格式化时间 */
	public static String getWordChatFormatTime(long l) {
		String time;
		if (isToday(l)) {
			time = "今日 " + getHourAndMin(l);
		} else {
			time = getSimpleTime(l);
		}

		return time;
	}

	/** 聊天室记录发言时间格式化 */
	public static String getFormatChatTime(String time) {
		String defaultResult = time;
		int index = time.lastIndexOf(":");
		if (index > 0) {
			String newTime = time.substring(0, index);
			String[] pies = newTime.split(" ");
			if (pies != null && pies.length == 2) {
				String s1 = pies[0];
				String s2 = pies[1];
				String[] s11 = s1.split("-");
				if (s11 != null && s11.length == 3) {
					Calendar now = Calendar.getInstance();
					int year = now.get(Calendar.YEAR);
					int month = now.get(Calendar.MONTH) + 1;
					int day = now.get(Calendar.DAY_OF_MONTH);
					boolean isYearEqual = (year == Integer.parseInt(s11[0]));
					boolean isMonthEqual = (month == Integer.parseInt(s11[1]));
					boolean isDayEqual = (day == Integer.parseInt(s11[2]));
					if (isYearEqual && isMonthEqual && isDayEqual) {
						return s2;
					} else {
						return newTime;
					}
				}

			}
		}

		return defaultResult;
	}
}
