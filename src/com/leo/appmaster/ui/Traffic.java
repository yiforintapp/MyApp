
package com.leo.appmaster.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.TrafficStats;
import android.text.format.Time;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.mgr.DeviceManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.WifiSecurityManager;
import com.leo.appmaster.sdk.push.PushNotification;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.ManagerFlowUtils;

public class Traffic {
    private final static String TRAFFICNOTIFICATION = "traffic_notification";
    private final static int NON_SAVE = 0;
    private final static int DAY_OF_THREE = 1;
    private final static int FINISH_THREE_DAY_AVG = 2;
    private final static int MIN_SHOW_NOTI_TRAFFIC = 1024;
    private final static String FIRSTDAY = "first_day";
    private final static String FIRSTDAYTRAFFIC = "first_day_traffic";
    private final static String SECONDDAY = "second_day";
    private final static String SECONDDAYTRAFFIC = "second_day_traffic";
    private final static String THREEDAY = "three_day";
    private final static String THREEDAYTRAFFIC = "three_day_traffic";
    private final static String TRAFFIC_AVG = "traffic_avg";


    private static final String STATE_WIFI = "wifi";
    private static final String STATE_NO_NETWORK = "nonet";
    private Context mContext;
    private static Traffic mTraffic;
    private static AppMasterPreference s_preferences;
    private float[] gprs = {
            0, 0, 0
    };
    private DeviceManager mDeviceManager;

    private Traffic(Context context) {
        this.mContext = context.getApplicationContext();
        mDeviceManager = (DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE);
    }

    public static synchronized Traffic getInstance(Context context) {
        if (s_preferences == null) {
            s_preferences = AppMasterPreference.getInstance(context);
        }
        if (mTraffic == null) {
            mTraffic = new Traffic(context);
        }
        return mTraffic;
    }

    public void checkTraffic() {
        int type = PreferenceTable.getInstance().getInt(TRAFFICNOTIFICATION, 0);
        int nowMonth = getCurrentTime()[1];
        int nowDay = getCurrentTime()[2];

        if (type == NON_SAVE) {
            PreferenceTable.getInstance().putInt(TRAFFICNOTIFICATION, DAY_OF_THREE);
            LeoLog.d("testTrafficNoti", "第一次进入 准备计算前三天流量");
        } else if (type == DAY_OF_THREE) {
            //firstday
            int saveFirstDay = PreferenceTable.getInstance().getInt(FIRSTDAY, 0);
            if (saveFirstDay == 0) {
                PreferenceTable.getInstance().putInt(FIRSTDAY, nowDay);
                LeoLog.d("testTrafficNoti", "saveFirstDay==0 , today is:" + nowDay);
                return;
            } else {
                if (nowDay == saveFirstDay) {
                    float firstdayTraffic = mDeviceManager.getTodayUsed() / 1024;//KB
                    PreferenceTable.getInstance().putFloat(FIRSTDAYTRAFFIC, firstdayTraffic);
                    LeoLog.d("testTrafficNoti", "saveFirstDay!=0 , today tra is:" + firstdayTraffic);
                    return;
                }
            }

            //secondday
            int saveSecondDay = PreferenceTable.getInstance().getInt(SECONDDAY, 0);
            if (saveSecondDay == 0) {
                PreferenceTable.getInstance().putInt(SECONDDAY, nowDay);
                LeoLog.d("testTrafficNoti", "saveSecondDay==0 , today is:" + nowDay);
                return;
            } else {
                if (nowDay == saveSecondDay) {
                    float seconddayTraffic = mDeviceManager.getTodayUsed() / 1024;//KB
                    PreferenceTable.getInstance().putFloat(SECONDDAYTRAFFIC, seconddayTraffic);
                    LeoLog.d("testTrafficNoti", "saveSecondDay!=0 , today tra is:" + seconddayTraffic);
                    return;
                }
            }

            //thridday
            int saveThridDay = PreferenceTable.getInstance().getInt(THREEDAY, 0);
            if (saveThridDay == 0) {
                PreferenceTable.getInstance().putInt(THREEDAY, nowDay);
                LeoLog.d("testTrafficNoti", "saveThridDay==0 , today is:" + nowDay);
                return;
            } else {
                if (nowDay == saveThridDay) {
                    float thriddayTraffic = mDeviceManager.getTodayUsed() / 1024;//KB
                    PreferenceTable.getInstance().putFloat(THREEDAYTRAFFIC, thriddayTraffic);
                    LeoLog.d("testTrafficNoti", "saveThridDay!=0 , today tra is:" + thriddayTraffic);
                    return;
                }
            }

            //fourday
            if (nowDay != saveFirstDay && nowDay != saveSecondDay && nowDay != saveThridDay) {
                LeoLog.d("testTrafficNoti", "完成成就，准备计算平均值");
                PreferenceTable.getInstance().putInt(TRAFFICNOTIFICATION, FINISH_THREE_DAY_AVG);
            }

        } else if (type == FINISH_THREE_DAY_AVG) {
            float day1Traffic = PreferenceTable.getInstance().getFloat(FIRSTDAYTRAFFIC, 0);
            LeoLog.d("testTrafficNoti", "day1Traffic:" + day1Traffic);
            float day2Traffic = PreferenceTable.getInstance().getFloat(SECONDDAYTRAFFIC, 0);
            LeoLog.d("testTrafficNoti", "day2Traffic:" + day2Traffic);
            float day3Traffic = PreferenceTable.getInstance().getFloat(THREEDAYTRAFFIC, 0);
            LeoLog.d("testTrafficNoti", "day3Traffic:" + day3Traffic);

            float avg = (day1Traffic + day2Traffic + day3Traffic) / 3;
            LeoLog.d("testTrafficNoti", "avg : " + avg);
            PreferenceTable.getInstance().putFloat(TRAFFIC_AVG, avg);

            //morenzhi
            if (avg < 1024) {
                avg = MIN_SHOW_NOTI_TRAFFIC;
            }

            float dayTraffic = mDeviceManager.getTodayUsed() / 1024;//KB
            LeoLog.d("testTrafficNoti", "dayTraffic : " + dayTraffic);
            if (avg < dayTraffic) {
                showNotification(dayTraffic);
            }

        }

    }


    private void showNotification(float dayTraffic) {
        PushNotification pushNotification = new PushNotification(mContext);
        Intent intent = new Intent(mContext, StatusBarEventService.class);
        intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
                StatusBarEventService.EVENT_TEN_OVER_DAY_TRAFFIC);
        int KB2MB = (int) (dayTraffic / 1024);
        String title = mContext.getString(R.string.over_traffic_toast, KB2MB);
        String string = mContext.getString(R.string.over_traffic_toast_content);
        pushNotification.showNotification(intent, title, string,
                R.drawable.ic_launcher_notification, pushNotification.NOTI_TRAFFIC);
    }

    public float[] getAllgprs(int mVersion, String network_state) {

        long mBaseSend = s_preferences.getBaseSend();
        long mBaseRev = s_preferences.getBaseRev();
        long mGprsSend = s_preferences.getGprsSend();
        long mGprsRev = s_preferences.getGprsRev();

        // 5.0系统连接，断开wifi问题解决
        if (mVersion > 19) {
            if (network_state.equals(STATE_WIFI) || network_state.equals(STATE_NO_NETWORK)) {
                gprs[0] = mGprsSend;
                gprs[1] = mGprsRev;
                gprs[2] = gprs[0] + gprs[1];
                return gprs;
            }
        }

        // Log.d(Tag, "----頭炮！一開始檢測-----");
        // Log.d(Tag, "mBaseSend : " + mBaseSend / 1024 / 1024);
        // Log.d(Tag, "mGprsSend : " + mGprsSend / 1024 / 1024);
        // Log.d(Tag, "------------------------------------");

        int nowYear = getCurrentTime()[0];
        int nowMonth = getCurrentTime()[1];
        int nowDay = getCurrentTime()[2];
        String nowDayTime = ManagerFlowUtils.getNowTime();
        int lastSaveYear = 0;
        int lastSaveMonth = 0;
        int lastSaveDay = 0;
        String lastSaveDayTime = "";

        Cursor Testcursor = null;
        try {
            Testcursor = mContext.getContentResolver().query(Constants.MONTH_TRAFFIC_URI, null,
                    null, null, null);
            if (Testcursor != null) {
                if (Testcursor.moveToLast()) {
                    lastSaveDayTime = Testcursor.getString(1);
                    lastSaveYear = Testcursor.getInt(4);
                    lastSaveMonth = Testcursor.getInt(5);
                    lastSaveDay = Testcursor.getInt(6);
                }
            }
        } catch (Exception e) {
        } finally {
            if (Testcursor != null) {
                Testcursor.close();
            }
        }


        // 同一天内如果没进行数据上传或下载，不进行IO操作
        if (mBaseSend == TrafficStats.getMobileTxBytes()
                && mBaseRev == TrafficStats.getMobileRxBytes()
                && nowDayTime.equals(lastSaveDayTime)) {
            gprs[0] = mGprsSend;
            gprs[1] = mGprsRev;
            gprs[2] = gprs[0] + gprs[1];
            // Log.d(Tag, "----没上传下载----存储-----");
            // Log.d(Tag, "Same day , return gprs[2] : " + gprs[2] / 1024 /
            // 1024);
            // Log.d(Tag, "------------------------------------");
            return gprs;
        }

        if (TrafficStats.getMobileTxBytes() >= mBaseSend
                || TrafficStats.getMobileRxBytes() >= mBaseRev) {
            // Log.d(Tag, "--------正常状态接受-------！");
            // Log.d(Tag, "TrafficStats.getMobileTxBytes()  : " +
            // (TrafficStats.getMobileTxBytes())
            // / 1024 / 1024);
            // Log.d(Tag, "mBaseSend : " + (mBaseSend) / 1024 / 1024);
            // Log.d(Tag, "mGprsSend : " + (mGprsSend) / 1024 / 1024);
            // Log.d(Tag, "---------------------------------！");
            s_preferences.setGprsSend(TrafficStats.getMobileTxBytes() - mBaseSend + mGprsSend);
            s_preferences.setGprsRev(TrafficStats.getMobileRxBytes() - mBaseRev + mGprsRev);
        } else {
            s_preferences.setGprsSend(TrafficStats.getMobileTxBytes() + mGprsSend);
            s_preferences.setGprsRev(TrafficStats.getMobileRxBytes() + mGprsRev);
        }
        mBaseSend = TrafficStats.getMobileTxBytes();
        mBaseRev = TrafficStats.getMobileRxBytes();

        // Log.d(Tag, "-------!!基值!!-------");
        // LeoLog.d(Tag, "mBaseSend : " + mBaseSend / 1024 / 1024);
        // LeoLog.d(Tag, "mBaseRev: " + mBaseRev / 1024 / 1024);
        // Log.d(Tag, "-------------------------");

        s_preferences.setBaseSend(mBaseSend);
        s_preferences.setBaseRev(mBaseRev);

        gprs[0] = s_preferences.getGprsSend();
        gprs[1] = s_preferences.getGprsRev();
        gprs[2] = gprs[0] + gprs[1];
        // Log.d(Tag, "-------最终获取gprs[2]-------");
        // LeoLog.d(Tag, "gprs[2] : " + gprs[2] / 1024 / 1024);
        // Log.d(Tag, "--------------------------------------");
        // 每个月的天数
        int MonthOfDay = ManagerFlowUtils.getCurrentMonthDay();
        // 月结日
        int renewDay = ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                getDataCutDay();

        Cursor mCursor = null;
        try {
            // 比较日期
            mCursor = mContext.getContentResolver().query(Constants.MONTH_TRAFFIC_URI, null,
                    "daytime=?", new String[]{
                            nowDayTime
                    }, null);
            if (mCursor != null) {
                if (!mCursor.moveToNext()) {
                    // Log.d("testfuckflow", "新一天or月到来");
                    ContentValues values = new ContentValues();
                    values.put("daytime", nowDayTime);
                    values.put("daymemory", 0);
                    values.put("year", nowYear);
                    values.put("month", nowMonth);
                    values.put("day", nowDay);
                    mContext.getContentResolver().insert(Constants.MONTH_TRAFFIC_URI, values);

                    // Log.d("testfuckflow", "renewDay : " + renewDay);
                    // Log.d("testfuckflow", "MonthOfDay : " + MonthOfDay);
                    // Log.d("testfuckflow", "nowDay : " + nowDay);
                    // Log.d("testfuckflow", "lastSaveDay : " + lastSaveDay);

                    // 同年换月换日操作
                    if (nowYear == lastSaveYear) {
                        // 分析，月结日坑，如果月结日在31号，但2月只有28天的情况。
                        // 月结日大于这个月天数
                        if (renewDay > MonthOfDay) {
                            // Log.d("testfuckflow", "renewDay > MonthOfDay");
                            if (nowMonth > lastSaveMonth) {
                                if (lastSaveDay < renewDay || nowDay > renewDay || nowDay == MonthOfDay) {
                                    // LeoLog.d("testfuckflow", "1");
                                    ReSetMonthTraffic();
                                } else {
                                    s_preferences.setMonthGprsBase((long) (gprs[2] + s_preferences
                                            .getMonthGprsBase()));
                                }

                            } else {
                                if (nowDay == MonthOfDay) {
                                    ReSetMonthTraffic();
                                    // LeoLog.d("testfuckflow", "2");
                                } else {
                                    s_preferences.setMonthGprsBase((long) (gprs[2] + s_preferences
                                            .getMonthGprsBase()));
                                }
                            }
                        } else {
                            // Log.d("testfuckflow", "renewDay <= MonthOfDay");
                            // 月结日 重置月流量计算
                            if (nowMonth > lastSaveMonth) {
                                if (lastSaveDay < renewDay || nowDay >= renewDay
                                        || nowDay == MonthOfDay) {
                                    ReSetMonthTraffic();
                                    // LeoLog.d("testfuckflow", "3");
                                } else {
                                    s_preferences.setMonthGprsBase((long) (gprs[2] + s_preferences
                                            .getMonthGprsBase()));
                                }
                            } else {
                                if (nowDay >= renewDay && lastSaveDay < renewDay) {
                                    ReSetMonthTraffic();
                                    // LeoLog.d("testfuckflow", "4");
                                } else {
                                    s_preferences.setMonthGprsBase((long) (gprs[2] + s_preferences
                                            .getMonthGprsBase()));
                                }
                            }
                        }
                        s_preferences.setGprsSend(0);
                        s_preferences.setGprsRev(0);
                        gprs[2] = 0;
                        // s_preferences.setItSelfTodayBase(0);
                    } else if (nowYear > lastSaveYear) {
                        // LeoLog.d("testfuckflow", "5");
                        // Log.d(Tag, "换年咯,重置everything ! ");
                        s_preferences.setGprsSend(0);
                        s_preferences.setGprsRev(0);
                        ReSetMonthTraffic();
                        gprs[2] = 0;
                    }
                } else {
                    s_preferences
                            .setMonthGprsAll((long) (s_preferences.getMonthGprsBase() + gprs[2]));
                    ContentValues values = new ContentValues();
                    values.put("daymemory", gprs[2]);
                    mContext.getContentResolver().update(Constants.MONTH_TRAFFIC_URI, values,
                            "daytime=?",
                            new String[]{
                                    nowDayTime
                            });
                }
            }
        } catch (Exception e) {
        } finally {
            if (mCursor != null) {
                mCursor.close();
            }
        }


        long ItSelfBase = s_preferences.getItSelfTodayBase();
        // 如果设置了已用流量，那么会一直叠加，除非换月清零。
        if (s_preferences.getItselfMonthTraffic() > 0) {

            // 设置今日已用base
            if (ItSelfBase < 1 || ItSelfBase > gprs[2]) {
                s_preferences.setItSelfTodayBase((long) gprs[2]);
                ItSelfBase = s_preferences.getItSelfTodayBase();
            }

            long gprsKb = (long) (gprs[2] / 1024);
            ItSelfBase = ItSelfBase / 1024;

            s_preferences
                    .setItselfMonthTraffic((long) (gprsKb - ItSelfBase + s_preferences
                            .getItselfMonthTraffic()));
            // LeoLog.d("testTraffic", "叠加是： " + (gprsKb - ItSelfBase) +
            // s_preferences
            // .getItselfMonthTraffic());
            s_preferences.setItSelfTodayBase((long) gprs[2]);
        }

        return gprs;
    }

    private void ReSetMonthTraffic() {
        // Log.d(Tag, "月结日到了，重置月流量咯！！！！！！");
        // 换月，流量超额开关
        s_preferences.setAlotNotice(false);
        s_preferences.setFinishNotice(false);
        // 换月，已使用流量设置为0
        s_preferences.setItselfMonthTraffic(0);
        s_preferences.setMonthGprsBase(0);
        s_preferences.setMonthGprsAll(0);
    }

    // 获取系统时间。返回数组
    private int[] getCurrentTime() {
        int[] is = {
                0, 0, 0, 0, 0, 0
        };
        Time time = new Time();
        time.setToNow();
        is[0] = time.year;
        is[1] = time.month + 1;
        is[2] = time.monthDay;
        is[3] = time.hour;
        is[4] = time.minute;
        is[5] = time.second;
        return is;
    }

}
