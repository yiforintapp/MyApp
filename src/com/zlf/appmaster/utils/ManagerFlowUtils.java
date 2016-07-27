
package com.zlf.appmaster.utils;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ManagerFlowUtils {

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 获取本年
     * @return
     */
    public static int getNowYear() {
        Calendar m = Calendar.getInstance();
        int year = m.get(Calendar.YEAR);
        return year;
    }
    
    /**
     * 获取当月
     * 
     * @return
     */
    public static int getNowMonth() {
        Calendar m = Calendar.getInstance();
        int month = m.get(Calendar.MONTH) + 1;
        return month;
    }

    /**
     * 获取当月的 天数
     */
    public static int getCurrentMonthDay() {

        Calendar a = Calendar.getInstance();
        a.set(Calendar.DATE, 1);
        a.roll(Calendar.DATE, -1);
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

    /**
     * 获取当前日
     * 
     * @return
     */
    public static int getDayOfMonth() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取当天年月日
     * 
     * @return
     */
    public static String getNowTime() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");// 可以方便地修改日期格式
        String hehe = dateFormat.format(now);
        return hehe;
    }

    // 单位换算
    // 转换单位
    public static String refreshTraffic(float lg) {
        String str = "0K";
        int a = 0, b = 1024, c = 1048576;

        if (lg < 1024) {
            str = "0K";
        }
        else if (lg >= b && lg < c) {
            int d = (int) (lg / b);
            str = d + "KB";
        }
        else {
            double e = (double) lg / c;
            java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
            str = df.format(e) + "MB";
        }
        return str;
    }
    
    public static String refreshTraffic_home_app(float lg) {
        String str = "0KB";
        int a = 0, b = 1024, c = 1048576;

        if (lg < 1024) {
            str = "0KB";
        }
        else if (lg >= b && lg < c) {
            int d = (int) (lg / b);
            str = d + "KB";
        }
        else {
            double e = (double) lg / c;
            java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
            str = df.format(e) + "MB";
        }
        return str;
    }


}
