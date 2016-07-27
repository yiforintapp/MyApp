package com.zlf.appmaster.utils;

import java.util.Calendar;

/**
 * Created by Jasper on 2015/10/21.
 */
public class TimeUtil {

    /**
     * 是否在同一天
     * @param thiz
     * @param other
     * @return
     */
    public static boolean isSameDay(long thiz, long other) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(thiz);

        int thizDay = calendar.get(Calendar.DAY_OF_YEAR);

        calendar.setTimeInMillis(other);
        int otherDay = calendar.get(Calendar.DAY_OF_YEAR);

        return thizDay == otherDay;
    }

    /**
     * 获取当天中的小时数
     * @param ts
     * @return
     */
    public static int getHourOfDay(long ts) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(ts);

        return calendar.get(Calendar.HOUR_OF_DAY);
    }
}
