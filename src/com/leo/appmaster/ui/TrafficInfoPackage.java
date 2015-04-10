
package com.leo.appmaster.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.text.format.Time;
import android.util.Log;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.model.TrafficsInfo;
import com.leo.appmaster.utils.ManagerFlowUtils;

public class TrafficInfoPackage {

    private static final String Tag = "TrafficInfoPackage";
    private Context mContext;
    private PackageManager mPm;
    private List<PackageInfo> packAgeList;
    private AppMasterPreference sp_app_flow;
    private List<Integer> uidList;
    ConnectivityManager manager;

    public TrafficInfoPackage(Context context) {
        // 通包管理器，检索所有的应用程序（甚至卸载的）与数据目录
        this.mContext = context.getApplicationContext();
        mPm = mContext.getPackageManager();
        packAgeList = mPm.getInstalledPackages(0);
        sp_app_flow = AppMasterPreference.getInstance(context);
        uidList = new ArrayList<Integer>();
        manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public List<TrafficsInfo> getRunningProcess() {
        List<TrafficsInfo> trafficInfos = new ArrayList<TrafficsInfo>();
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        String nowDayTime = ManagerFlowUtils.getNowTime();
        int nowYear = getCurrentTime()[0];
        int nowMonth = getCurrentTime()[1];
        int saveMonth = sp_app_flow.getMonthAppTraf();

        for (PackageInfo packInfo : packAgeList) {
            //过滤掉pl
            if(packInfo.packageName.equals(mContext.getPackageName())){
                continue;
            }
            
            // 过滤掉没有联网功能的软件
            int result = mPm.checkPermission("android.permission.INTERNET",
                    packInfo.packageName);
            if (result != PackageManager.PERMISSION_GRANTED) {
                continue;
            }

            // 获取到应用的id，去重操作
            int uid = packInfo.applicationInfo.uid;
            if (!uidList.contains(uid)) {
                uidList.add(uid);
            } else {
                continue;
            }

            long sysSend = TrafficStats.getUidTxBytes(uid);
            long sysRev = TrafficStats.getUidRxBytes(uid);

            TrafficsInfo trafficInfo = new TrafficsInfo();
            // 获取应用的应用名、图标等信息
            String mPackageName = packInfo.packageName;
            Drawable mIconDrawable = packInfo.applicationInfo
                    .loadIcon(mPm);
            String mLabelName = packInfo.applicationInfo
                    .loadLabel(mPm).toString();
            
            trafficInfo.setPackName(mPackageName.isEmpty()?"":mLabelName);
            try {
                trafficInfo.setIcon(mIconDrawable);
            } catch (Exception e) {
                trafficInfo.setIcon(mContext.getResources().getDrawable(R.drawable.ic_launcher));
            }
            trafficInfo.setAppName(mLabelName.isEmpty()?"":mLabelName);

            long mAppBaseSend = sp_app_flow.getAppBaseSend(uid);
            long mAppBaseRev = sp_app_flow.getAppBaseRev(uid);
            long wifi_trffic_sned = sp_app_flow.getWifiSend(uid);
            long wifi_trffic_rev = sp_app_flow.getWifiRev(uid);
            long saveDbAppGprsSend = 0;
            long saveDbAppGprsRev = 0;

            Cursor mFindCursor = mContext.getContentResolver().query(Constants.APP_TRAFFIC_URI,
                    null,
                    "year=? and month=? and uid=?", new String[] {
                            String.valueOf(nowYear), String.valueOf(nowMonth), String.valueOf(uid)
                    }, null);
            if (mFindCursor != null) {
                // boolean moveToNext = false;
                if (mFindCursor.moveToNext()) {
                    saveDbAppGprsSend = (long) mFindCursor.getFloat(5);
                    saveDbAppGprsRev = (long) mFindCursor.getFloat(6);
                    // moveToNext = true;
                }
                mFindCursor.close();
                // if(moveToNext){
                // if (saveDbAppGprsRev < 1000 && saveDbAppGprsSend < 1000) {
                // LeoLog.d(Tag, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                // continue;
                // }
                // }
            }

            // 判断网络状态，记录网络数据
            if (mobile.getState() == NetworkInfo.State.CONNECTED
                    && wifi.getState() != NetworkInfo.State.CONNECTED) {// 非wifi状态
                // 重启后gprs进入，清零wifi数据
                if (wifi_trffic_sned > sysSend || wifi_trffic_rev > sysRev) {
                    sp_app_flow.setWifiSend(uid, 0);
                    sp_app_flow.setWifiRev(uid, 0);
                    wifi_trffic_sned = 0;
                    wifi_trffic_rev = 0;
                }

                if (wifi_trffic_sned > 0 || wifi_trffic_rev > 0) {// 系统数据存在wifi传输数据，减去
                    sysSend = sysSend - wifi_trffic_sned;
                    sysRev = sysRev - wifi_trffic_rev;
                    if (sysSend < 1000 && sysRev < 1000) {
                        continue;
                    }
                }

                // 同一天内如果没进行数据上传或下载，不进行IO操作
                if (mAppBaseSend == sysSend && mAppBaseRev == sysRev
                        && nowMonth == saveMonth && sysSend > 1000 && sysRev > 1000) {
                    trafficInfo.setTx(saveDbAppGprsSend);
                    trafficInfo.setRx(saveDbAppGprsRev);
                    trafficInfo.setApp_all_traffic(saveDbAppGprsSend + saveDbAppGprsRev);
                    trafficInfo
                            .setApp_all_traffic_for_show((int) (saveDbAppGprsSend + saveDbAppGprsRev));
                    trafficInfos.add(trafficInfo);
                    continue;
                }

                if (sysSend > mAppBaseSend || sysRev == mAppBaseSend)
                {
                    saveDbAppGprsSend = sysSend - mAppBaseSend + saveDbAppGprsSend;
                    saveDbAppGprsRev = sysRev - mAppBaseRev + saveDbAppGprsRev;
                }
                else {
                    saveDbAppGprsSend = sysSend + saveDbAppGprsSend;
                    saveDbAppGprsRev = sysRev + saveDbAppGprsRev;
                }
                sp_app_flow.setAppBaseSend(uid, sysSend);
                sp_app_flow.setAppBaseRev(uid, sysRev);

            } else {// wifi状态
                if (mAppBaseSend < sysSend || sysSend == mAppBaseSend) {// 系统数据大于基值，wifi数据传输
                    sp_app_flow.setWifiSend(uid, sysSend - mAppBaseSend);
                    sp_app_flow.setWifiRev(uid, sysRev - mAppBaseRev);
                } else {// 基值大于系统数据，重启手机的情况，清零
                    sp_app_flow.setWifiSend(uid, 0);
                    sp_app_flow.setWifiRev(uid, 0);
                }
            }

            // // wifi状态下传输数据，记录入SP后continue
            if (saveDbAppGprsRev < 1000 && saveDbAppGprsSend < 1000) {
                continue;
            }

            // 每个App获取一整个月的流量统计，只对比月份
            // 跨月清零
            Cursor mCursor = mContext.getContentResolver().query(Constants.APP_TRAFFIC_URI, null,
                    "year=? and month=? and uid=?", new String[] {
                            String.valueOf(nowYear), String.valueOf(nowMonth), String.valueOf(uid)
                    }, null);
            if (mCursor != null) {
                if (!mCursor.moveToNext()) {
                    Log.d(Tag, "cursor can not move to next , insert a new data .");
                    ContentValues values = new ContentValues();
                    values.put("daytime", nowDayTime);
                    values.put("year", nowYear);
                    values.put("month", nowMonth);
                    values.put("uid", uid);
                    values.put("monthsend", 0);
                    values.put("monthrev", 0);
                    values.put("monthall", 0);
                    mContext.getContentResolver().insert(Constants.APP_TRAFFIC_URI, values);
                } else {
                    // Log.d(Tag, "uid :" + uid +
                    // ",have old data . update it .");
                    ContentValues values = new ContentValues();
                    values.put("monthsend", saveDbAppGprsSend);
                    values.put("monthrev", saveDbAppGprsRev);
                    values.put("monthall", saveDbAppGprsSend + saveDbAppGprsRev);
                    mContext.getContentResolver().update(
                            Constants.APP_TRAFFIC_URI,
                            values,
                            "year=? and month=? and uid=?",
                            new String[] {
                                    String.valueOf(nowYear), String.valueOf(nowMonth),
                                    String.valueOf(uid)
                            });
                }
                mCursor.close();
            }

            long rx = 0;
            long tx = 0;
            // 重新获取，赋值
            Cursor mFillCursor = mContext.getContentResolver().query(Constants.APP_TRAFFIC_URI,
                    null,
                    "year=? and month=? and uid=?", new String[] {
                            String.valueOf(nowYear), String.valueOf(nowMonth), String.valueOf(uid)
                    }, null);
            if (mFillCursor != null) {
                if (mFillCursor.moveToNext()) {
                    rx = (long) mFillCursor.getFloat(6);
                    tx = (long) mFillCursor.getFloat(5);
                }
                mFillCursor.close();
            }

            if (rx < 1000 && tx < 1000) {
                continue;
            }

            trafficInfo.setRx(rx);
            trafficInfo.setTx(tx);
            trafficInfo.setApp_all_traffic(rx + tx);
            trafficInfo.setApp_all_traffic_for_show((int) (rx + tx));
            trafficInfos.add(trafficInfo);
        }
        // 每个App获取一整个月的流量统计，只对比月份
        if (nowMonth != saveMonth) {
            Log.d(Tag, "not the same month , so just reset the month . " + getCurrentTime()[1]);
            sp_app_flow.setYearAppTraf(getCurrentTime()[0]);
            sp_app_flow.setMonthAppTraf(getCurrentTime()[1]);
        }
        return trafficInfos;
    }

    // 获取系统时间。返回数组
    private int[] getCurrentTime() {
        int[] is = {
                0, 0, 0
        };
        Time time = new Time();
        time.setToNow();
        is[0] = time.year;
        is[1] = time.month + 1;
        is[2] = time.monthDay;
        return is;
    }
}
