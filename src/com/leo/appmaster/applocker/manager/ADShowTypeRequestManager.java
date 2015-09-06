
package com.leo.appmaster.applocker.manager;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import android.util.Log;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;


import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.http.HttpRequestAgent.RequestListener;
import com.leo.appmaster.sdk.SDKWrapper;
import com.mobvista.sdk.m.core.entity.Campaign;

public class ADShowTypeRequestManager {
    /* 是否升级 */

    private static final String AD_SHOW_TYPE = "a";// 广告的展示方式，如半屏广告，banner广告等等

    // private static final String ISWIP_BROWSER_DOWN_URL = "e";// 浏览器下载地址
    // private static final String ISWIP_DOWN_TYPE = "f";// 下载方式
    // private static final int NOTIFICATION_CHECKNEW_ISWIP = 20140903;
    // public static final String ISWIP_NOTIFICATION_TO_PG_HOME =
    // "iswip_to_home";

    private static ADShowTypeRequestManager mInstance;
    private Context mContext;
    private SimpleDateFormat mDateFormate;

    /* 是否为push吊起加载 */
    public boolean mIsPushRequestADShowType = false;
    
    public static Campaign mCampaign; 

    private ADShowTypeRequestManager(Context context) {
        mContext = context;
        mDateFormate = new SimpleDateFormat("yyyy-MM-dd");
    }

    public static synchronized ADShowTypeRequestManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ADShowTypeRequestManager(context);
        }
        return mInstance;
    }

    public void loadADCheckShowType() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                requestADShowType();
            }
        });
    }

    public void requestADShowType() {

        AppMasterPreference sp = AppMasterPreference.getInstance(mContext);
        // 当前时间（ms）
        long currentTime = System.currentTimeMillis();
        long lastRequestTime = sp.getADRequestShowTypeLastTime();
        String currentDate = mDateFormate.format(new Date(currentTime));
        String lastRequestDate = mDateFormate.format(new Date(lastRequestTime));

        Log.e("poha", "当前时间：" + currentTime + "，上次请求时间：" + lastRequestTime + ",间隔是（s）："
                + (currentTime - lastRequestTime) / 1000);
        Log.e("poha", "当前日期：" + currentDate + "，上次请求日期：" + lastRequestDate);
        Log.e("poha", "上次记录的到下次请求的间隔时间（12h/2h）：" + sp.getADRequestShowTypeNextTimeSpacing() / 1000
                / 60 / 60 + "，当天的请求失败次数："
                + sp.getADRequestShowtypeFailTimesCurrentDay());

        if (((currentTime - lastRequestTime) > sp.getADRequestShowTypeNextTimeSpacing()
                && sp.getADRequestShowtypeFailTimesCurrentDay() < 3) || mIsPushRequestADShowType)
        {
            mIsPushRequestADShowType = false;
                        
            Log.e("poha", "满足发起请求的条件，正在发起请求");
            UpdateADShowTypeRequestListener listenerr = new UpdateADShowTypeRequestListener(
                    AppMasterApplication.getInstance());
            HttpRequestAgent.getInstance(AppMasterApplication.getInstance()).loadADShowType(
                    listenerr,
                    listenerr);
        }


  

    }

    private class UpdateADShowTypeRequestListener extends RequestListener<AppMasterApplication> {

        public UpdateADShowTypeRequestListener(AppMasterApplication outerContext) {
            super(outerContext);
        }

        @Override
        public void onResponse(JSONObject response, boolean noMidify) {
            if (response != null)
            {
                AppMasterPreference sp = AppMasterPreference.getInstance(mContext);

                try {
                    int adtype = response.getInt(AD_SHOW_TYPE);               
                    
                    Log.e("poha", "请求成功，广告展示形式是：" + adtype);
                    if(adtype!=AppMasterPreference.getInstance(mContext).getADShowType())
                    {
                        if(adtype==1)
                        {
                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_banner");
                        }
                        if(adtype==2)
                        {
                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_bannerpop");
                        }
                        if(adtype==3)
                        {
                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_draw");
                        }
                        if(adtype==4)
                        {
                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "ad_pull", "ad_none");
                        }
                        
                    }
                    AppMasterPreference.getInstance(mContext).setADShowType(adtype);

                    long currentTime = System.currentTimeMillis();
                    String currentDate = mDateFormate.format(new Date(System.currentTimeMillis()));
                    String LastRequestDate = mDateFormate.format(new Date(sp
                            .getADRequestShowTypeLastTime()));

                    sp.setADRequestShowTypeLastTime(currentTime);
                    if (!currentDate.equals(LastRequestDate))
                    {
                        sp.setADRequestShowtypeFailTimesCurrentDay(0);
                    }
                    sp.setADRequestShowTypeNextTimeSpacing(1000 * 60 * 60 * 12);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block

                    Log.e("poha", "请求成功，JSON解析出错");

                    e.printStackTrace();
                }
            }
            else
            {
                Log.e("poha", "请求成功，JSON是null");
            }

        }

        @Override
        public void onErrorResponse(VolleyError error) {
            // TODO Auto-generated method stub
            Log.e("poha", "请求失败。。。");

            AppMasterPreference sp = AppMasterPreference.getInstance(mContext);
            long currentTime = System.currentTimeMillis();
            String currentDate = mDateFormate.format(new Date(System.currentTimeMillis()));
            String LastRequestDate = mDateFormate
                    .format(new Date(sp.getADRequestShowTypeLastTime()));

            sp.setADRequestShowTypeLastTime(currentTime);

            if (currentDate.equals(LastRequestDate))
            {
                sp.setADRequestShowtypeFailTimesCurrentDay(sp
                        .getADRequestShowtypeFailTimesCurrentDay() + 1);
                if (sp.getADRequestShowtypeFailTimesCurrentDay() == 3)
                {
                    sp.setADRequestShowTypeNextTimeSpacing(1000 * 60 * 60 * 12);
                }
                else
                {
                    sp.setADRequestShowTypeNextTimeSpacing(1000 * 60 * 60 * 2);
                }
            }
            else
            {
                sp.setADRequestShowtypeFailTimesCurrentDay(1);
                sp.setADRequestShowTypeNextTimeSpacing(1000 * 60 * 60 * 2);
            }
            // AppMasterPreference.getInstance(mContext).setADRequestShowTypeLastTime(System.currentTimeMillis());

        }

        // @Override
        // public void onResponse(JSONObject response, boolean noMidify) {
        // if (response != null) {
        // try {
        // /* 广告展示类型 */
        // int checkUpdate = response.getInt(AD_SHOW_TYPE);
        // // /* 提示频率 */
        // // int frequency = response.getInt(ISWIP_TIP_FREQUENCY);
        // // /* 提示次数 */
        // // int number = response.getInt(ISWIP_TIP_NUMBER);
        // // /* gp下载地址 */
        // // String gpUrl = response.getString(ISWIP_GP_DOWN_URL);
        // // /* 浏览器下载地址 */
        // // String browserUrl = response.getString(ISWIP_BROWSER_DOWN_URL);
        // // /* 下载方式 */
        // // int downType = response.getInt(ISWIP_DOWN_TYPE);
        // /* 拉取成功 */
        // Log.e("poha", "拉取成功,好开心");
        // //
        // AppMasterPreference.getInstance(mContext).setIswipUpdateLoadingStrategy(
        // // AppMasterConfig.TIME_24_HOUR);
        // //
        // AppMasterPreference.getInstance(mContext).setIswipUpateLastLoadingTime(
        // // System.currentTimeMillis());
        // /* 保存数据服务器加载出来的数据 */
        // // saveIswipUpdateDate(checkUpdate, frequency, number, gpUrl,
        // browserUrl, downType);
        // // // 测试日志
        // // testLog(checkUpdate, frequency, number, gpUrl, browserUrl,
        // downType);
        // } catch (JSONException e) {
        // e.printStackTrace();
        // }
        // }
        //
        // }
        //
        // private void saveIswipUpdateDate(int checkUpdate, int frequency, int
        // number, String gpUrl,
        // String browserUrl, int downType) {
        // AppMasterPreference preference =
        // AppMasterPreference.getInstance(AppMasterApplication
        // .getInstance());
        // preference.setIswipUpdateFlag(checkUpdate);
        // preference.setIswipUpdateFre(frequency);
        // preference.setIswipUpdateNumber(number);
        // if (!Utilities.isEmpty(gpUrl)) {
        // preference.setIswipUpdateGpUrl(gpUrl);
        // }
        // if (!Utilities.isEmpty(browserUrl)) {
        // preference.setIswipUpdateBrowserUrl(browserUrl);
        // }
        // preference.setIswipUpdateDownType(downType);
        //
        // }
        //
        // private void testLog(int checkUpdate, int frequency, int number,
        // String gpUrl,
        // String browserUrl, int downType) {
        // Log.d(Constants.RUN_TAG, "是否升级" + checkUpdate);
        // Log.d(Constants.RUN_TAG, " 提示频率" + frequency);
        // Log.d(Constants.RUN_TAG, "提示次数" + number);
        // Log.d(Constants.RUN_TAG, "gp下载地址" + gpUrl);
        // Log.d(Constants.RUN_TAG, "浏览器下载地址" + browserUrl);
        // Log.d(Constants.RUN_TAG, "下载方式" + downType);
        // }
        // @Override
        // public void onErrorResponse(VolleyError error) {
        // /* 拉取失败 */
        // Log.e("poha", "拉取失败le...e...");
        // //
        // AppMasterPreference.getInstance(mContext).setIswipUpdateLoadingStrategy(
        // // AppMasterConfig.TIME_2_HOUR);
        // // AppMasterPreference.getInstance(mContext).setIswipeLoadFailDate(
        // // mDateFormate.format(new Date(System.currentTimeMillis())));
        // //
        // AppMasterPreference.getInstance(mContext).setIswipUpateLastLoadingTime(
        // // System.currentTimeMillis());
        // // if
        // (AppMasterPreference.getInstance(mContext).getIswipUpdateLoadingNumber()
        // < 0) {
        // //
        // AppMasterPreference.getInstance(mContext).setIswipUpdateLoadingNumber(0);
        // // } else {
        // // AppMasterPreference.getInstance(mContext)
        // // .setIswipUpdateLoadingNumber(
        // // AppMasterPreference.getInstance(mContext)
        // // .getIswipUpdateLoadingNumber() + 1);
        // // // Log.e(Constants.RUN_TAG, "失败" +
        // // // (AppMasterPreference.getInstance(mContext)
        // // // .getIswipUpdateLoadingNumber()) + "次");
        // // }
        // }
    }

    // /* ISwip更新通知 */
    // public void showISwipCheckNewNotification() {
    // RemoteViews view_custom;
    // view_custom = new RemoteViews(mContext.getPackageName(),
    // R.layout.clean_mem_notify);
    // view_custom.setImageViewResource(R.id.appwallIV, R.drawable.iswip_icon);
    // view_custom.setTextViewText(R.id.appwallNameTV,
    // mContext.getString(R.string.clean_mem_notify_big));
    // view_custom.setTextViewText(R.id.appwallDescTV,
    // mContext.getString(R.string.clean_mem_notify_small));
    // view_custom.setTextViewText(R.id.app_precent, " %");
    // NotificationCompat.Builder mBuilder = new Builder(mContext);
    // mBuilder.setContent(view_custom)
    // .setWhen(System.currentTimeMillis())
    // .setTicker(mContext.getString(R.string.clean_mem_notify_big))
    // .setPriority(Notification.PRIORITY_DEFAULT)
    // .setOngoing(false)
    // .setSmallIcon(R.drawable.statusbaricon)
    // .setAutoCancel(true);
    // Intent intent = null;
    // intent = new Intent(Intent.ACTION_VIEW);
    // ComponentName cn = new ComponentName(mContext.getPackageName(),
    // "com.leo.appmaster.home.HomeActivity");
    // intent.setComponent(cn);
    // intent.putExtra(ISWIP_NOTIFICATION_TO_PG_HOME,
    // ISWIP_NOTIFICATION_TO_PG_HOME);
    // PendingIntent contentIntent = PendingIntent.getService(mContext, 1,
    // intent, PendingIntent.FLAG_UPDATE_CURRENT);
    // mBuilder.setContentIntent(contentIntent);
    // Notification notif = mBuilder.build();
    // NotificationManager nm = (NotificationManager) mContext
    // .getSystemService(Context.NOTIFICATION_SERVICE);
    // nm.notify(NOTIFICATION_CHECKNEW_ISWIP, notif);
    // }
    //
    // /* 通过频率次数定时发送ISwipe更新通知 */
    // public void alarmNotificationISwipeDownload(long triggerAtMillis, long
    // intervalMillis) {
    // AlarmManager am = (AlarmManager)
    // mContext.getSystemService(Context.ALARM_SERVICE);
    // Intent intent = new Intent("ISWIP_TIP");
    // PendingIntent pend = PendingIntent.getBroadcast(mContext, 0, intent, 0);
    // am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis,
    // pend);
    // }
}
