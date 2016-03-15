package com.leo.appmaster.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.battery.BatteryNotifyHelper;
import com.leo.appmaster.battery.RemainingTimeEstimator;
import com.leo.appmaster.engine.BatteryComsuption;
import com.leo.appmaster.engine.BatteryInfoProvider;
import com.leo.appmaster.mgr.DeviceManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.model.TrafficsInfo;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.TrafficInfoPackage;
import com.leo.appmaster.utils.ManagerFlowUtils;

public class DeviceManagerImpl extends DeviceManager {


    public DeviceManagerImpl() {

    }

    public void init() {

    }

    @Override
    public void onDestory() {

    }


    @Override
    public float getTodayUsed() {
        float today_flow = 0;
        String today_ymd = ManagerFlowUtils.getNowTime();
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(Constants.MONTH_TRAFFIC_URI, null,
                    "daytime=?",
                    new String[]{
                            today_ymd
                    }, null);
            if (cursor != null && cursor.moveToNext()) {
                today_flow = cursor.getFloat(2);
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return today_flow;
    }

    @Override
    public long getMonthUsed() {
        long mThisMonthTraffic = AppMasterPreference.getInstance(mContext).
                getMonthGprsAll();
        long mThisMonthItselfTraffi = AppMasterPreference.getInstance(mContext).
                getItselfMonthTraffic();
        if (mThisMonthItselfTraffi > 0) {
            return mThisMonthItselfTraffi * 1024;
        } else {
            return mThisMonthTraffic;
        }
    }

    @Override
    public void setMonthUsed(int mMonthUsed) {
        AppMasterPreference.getInstance(mContext).setItselfMonthTraffic(mMonthUsed * 1024);
    }

    @Override
    public void setMonthTotalTraffic(int mTotalTraffic) {
        AppMasterPreference.getInstance(mContext).setTotalTraffic(mTotalTraffic);
    }

    @Override
    public int getMonthTotalTraffic() {
        return AppMasterPreference.getInstance(mContext).getTotalTraffic();
    }

    @Override
    public ArrayList<Integer> getEveryDayTraffic() {
        // 每天的流量点
        ArrayList<Integer> dataList = new ArrayList<Integer>();
        int days = ManagerFlowUtils.getCurrentMonthDay();
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(Constants.MONTH_TRAFFIC_URI, null,
                    "year=? and month=?", new String[]{
                            String.valueOf(ManagerFlowUtils.getNowYear()),
                            String.valueOf(ManagerFlowUtils.getNowMonth())
                    }, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int day = cursor.getInt(6);
                    int gprs = (int) ManagerFlowUtils.BToKb(cursor.getFloat(2));
                    map.put(day, gprs);
                }
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        for (int i = 0; i < days; i++) {
            Integer value = map.get(i + 1);
            if (value == null) {
                dataList.add(0);
            } else {
                dataList.add(value);
            }
        }

        return dataList;
    }

    @Override
    public boolean getOverDataSwitch() {
        return AppMasterPreference.getInstance(mContext).getFlowSetting();
    }

    @Override
    public void setOverDataSwitch(boolean mFlag) {
        AppMasterPreference.getInstance(mContext).setFlowSetting(mFlag);
    }

    @Override
    public int getOverDataInvokePercent() {
        return AppMasterPreference.getInstance(mContext).getFlowSettingBar();
    }

    @Override
    public void setOverDataInvokePercent(int mPercent) {
        AppMasterPreference.getInstance(mContext).setFlowSettingBar(mPercent);
    }


    @Override
    public void setDataCutDay(int mDay) {
        AppMasterPreference.getInstance(mContext).setRenewDay(mDay);
    }

    @Override
    public int getDataCutDay() {
        return AppMasterPreference.getInstance(mContext).getRenewDay();
    }

    @Override
    public List<TrafficsInfo> getAppRange() {
        List<TrafficsInfo> readytosort;
        readytosort = new TrafficInfoPackage(mContext).getRunningProcess(true);
        for (int i = 0; i < readytosort.size(); i++) {
            if (readytosort.get(i).getApp_all_traffic().equals("0KB")
                    || (readytosort.get(i).getRx().equals("0KB") && readytosort.get(i).getTx()
                    .equals("0KB"))) {
                readytosort.remove(i);
            }
        }
        return ManagerFlowUtils.makeSort(readytosort);

    }

    @Override
    public List<BatteryComsuption> getBatteryRange() {
        BatteryInfoProvider info = new BatteryInfoProvider(mContext);
        info.setMinPercentOfTotal(0.01);
        List<BatteryComsuption> list = info.getBatteryStats();

        for (int i = list.size() - 1; i >= 0; i--) {

            String packageName = list.get(i).getDefaultPackageName();
            if (null != packageName) {
                if (packageName.equals(mContext.getPackageName())) {
                    list.remove(i);
                    continue;
                }
            }

            final BatteryComsuption sipper = list.get(i);
            String name = sipper.getName();
            if (name == null) {
                Drawable icon = sipper.getIcon();
                switch (sipper.getDrainType()) {
                    case CELL:
                        name = mContext.getString(R.string.power_cell);
                        icon = mContext.getResources().getDrawable
                                (R.drawable.ic_settings_cell_standby);
                        break;
                    case IDLE:
                        name = mContext.getString(R.string.power_idle);
                        icon = mContext.getResources().getDrawable(R.drawable.ic_settings_phone_idle);
                        break;
                    case BLUETOOTH:
                        name = mContext.getString(R.string.power_bluetooth);
                        icon = mContext.getResources().getDrawable(R.drawable.ic_settings_bluetooth);
                        break;
                    case WIFI:
                        name = mContext.getString(R.string.power_wifi);
                        icon = mContext.getResources().getDrawable(R.drawable.ic_settings_wifi);
                        break;
                    case SCREEN:
                        name = mContext.getString(R.string.power_screen);
                        icon = mContext.getResources().getDrawable(R.drawable.ic_settings_display);
                        break;
                    case PHONE:
                        name = mContext.getString(R.string.power_phone);
                        icon = mContext.getResources().getDrawable
                                (R.drawable.ic_settings_voice_calls);
                        break;
                    case KERNEL:
                        name = mContext.getString(R.string.process_kernel_label);
                        icon = mContext.getResources().getDrawable
                                (R.drawable.ic_power_system);
                        break;
                    case MEDIASERVER:
                        name = mContext.getString(R.string.process_mediaserver_label);
                        icon = mContext.getResources().getDrawable(R.drawable.ic_power_system);
                        break;
                    default:
                        break;
                }

                if (name != null) {
                    sipper.setName(name);
                    if (icon == null) {
                        PackageManager pm = mContext.getPackageManager();
                        icon = pm.getDefaultActivityIcon();
                    }
                    sipper.setIcon(icon);
                } else {
                    list.remove(i);
                }
            }
        }

        return list;
    }

    @Override
    public void setStopApp(String mPackageName) {
        try {
            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "batterypage", "batterystop");
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", mPackageName, null);
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
        } catch (Exception e) {
            try {
                Intent powerUsageIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
                ResolveInfo resolveInfo = mContext.getPackageManager().
                        resolveActivity(powerUsageIntent, 0);
                if (resolveInfo != null) {
                    mContext.startActivity(powerUsageIntent);
                }
            } catch (Exception e1) {
                Toast.makeText(mContext, R.string.battery_cannot_do, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
