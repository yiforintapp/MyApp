package com.leo.appmaster.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.lockswitch.BlueToothLockSwitch;
import com.leo.appmaster.applocker.lockswitch.SwitchGroup;
import com.leo.appmaster.applocker.lockswitch.WifiLockSwitch;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.engine.BatteryComsuption;
import com.leo.appmaster.engine.BatteryInfoProvider;
import com.leo.appmaster.imagehide.ImageGridActivity;
import com.leo.appmaster.mgr.DeviceManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.model.TrafficsInfo;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.TrafficInfoPackage;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.ManagerFlowUtils;

public class DeviceManagerImpl extends DeviceManager {
    private final static String TAG = "DeviceManagerImpl";
    private final static int RESTART_SHOW = 5000;
    private final static int WIFI_TURN_ON = 1;
    private final static int BLUETOOTH_TURN_ON = 2;

    private final static int WIFI_SHUT_DOWN = 1;
    private final static int BLUETOOTH_SHUT_DOWN = 2;
    private final static int WIFI_ENABLED = 3;
    private final static int BLUETOOTH_ENABLED = 4;

    private final static long DELAY_DISABL = 1000;
    private final static long SHOW_WIFI_ENABLE = 2500;

    private LockManager mLockManager;
    private WifiLockSwitch mWifiSwitch;
    private BlueToothLockSwitch mBlueToothSwitch;

    private boolean unlockOpenWifiDone = false;
    private boolean unlockOpenBlueToothDone = false;

    private WifiManager mWifimanager;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean isEnableIng = false;
    private long mInitTime;

    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case WIFI_SHUT_DOWN:
                    LeoLog.d(TAG, "shut down wifi");
                    mWifimanager.setWifiEnabled(false);
                    break;
                case BLUETOOTH_SHUT_DOWN:
                    LeoLog.d(TAG, "shut down bluetooth");
                    mBluetoothAdapter.disable();
                    break;
                case WIFI_ENABLED:
                    unlockOpenWifiDone = true;
                    mWifimanager.setWifiEnabled(true);
                    break;
                case BLUETOOTH_ENABLED:
                    unlockOpenBlueToothDone = true;
                    mBluetoothAdapter.enable();
                    break;
            }
        }
    };

    public DeviceManagerImpl() {
        mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);

        mWifiSwitch = new WifiLockSwitch();
        mBlueToothSwitch = new BlueToothLockSwitch();

        mWifimanager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    public void init() {
        LeoLog.d(TAG, "onCreate");

        mInitTime = System.currentTimeMillis();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        mContext.registerReceiver(mBlueToothChangeReceiver, filter);

//        IntentFilter sdFilter = new IntentFilter();
//        sdFilter.addAction(Intent.ACTION_MEDIA_SHARED);//如果SDCard未安装,并通过USB大容量存储共享返回
//        sdFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);//表明sd对象是存在并具有读/写权限
//        sdFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);//SDCard已卸掉,如果SDCard是存在但没有被安装
//        sdFilter.addAction(Intent.ACTION_MEDIA_CHECKING);  //表明对象正在磁盘检查
//        sdFilter.addAction(Intent.ACTION_MEDIA_EJECT);  //物理的拔出 SDCARD
//        sdFilter.addAction(Intent.ACTION_MEDIA_REMOVED);  //完全拔出
//        sdFilter.addDataScheme("file"); // 必须要有此行，否则无法收到广播
//        sdFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
//        mContext.registerReceiver(mSdcardReceiver, sdFilter);

    }

    private long lastEnableTime = 0;

    @Override
    public void wifiChangeReceiver(Intent intent) {
        LeoLog.d(TAG, "wifi onReceive");
        String action = intent.getAction();
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {// 这个监听wifi的打开与关闭，与wifi的连接无关
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_ENABLING:
                    LeoLog.d(TAG, "WIFI_STATE_ENABLING");

                    isEnableIng = true;
                    readyShowLock(WIFI_TURN_ON);
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    LeoLog.d(TAG, "WIFI_STATE_ENABLED");

                    //in case of enter ENABLED twice
                    long now = System.currentTimeMillis();
                    if (now - lastEnableTime < SHOW_WIFI_ENABLE) {
                        return;
                    }

                    //fix bug , some will not go ENABLING
                    if (!isEnableIng) {
                        readyShowLock(WIFI_TURN_ON);
                    }

                    isEnableIng = false;
                    unlockOpenWifiDone = false;
                    lastEnableTime = System.currentTimeMillis();
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    LeoLog.d(TAG, "WIFI_STATE_DISABLING");
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    LeoLog.d(TAG, "WIFI_STATE_DISABLED");
                    unlockOpenWifiDone = false;
                    break;
            }
        }
    }

    private void readyShowLock(int type) {
        mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        LockMode mode = mLockManager.getCurLockMode();
        switch (type) {
            case WIFI_TURN_ON:
                long show = System.currentTimeMillis();
                if (mWifiSwitch.isLockNow(mode) && !unlockOpenWifiDone
                        && (show - mInitTime > RESTART_SHOW)) {
                    //show LockScreen
                    shutDownWifi();
                    showLockScreen(type);
                }
                break;
            case BLUETOOTH_TURN_ON:
                if (mBlueToothSwitch.isLockNow(mode) && !unlockOpenBlueToothDone) {
                    shutDownBlueTooth();
                    showLockScreen(type);
                }
                break;
        }
    }

    private void shutDownWifi() {
        mHandler.sendEmptyMessageDelayed(WIFI_SHUT_DOWN, DELAY_DISABL);
    }

    private void showLockScreen(final int showType) {

        String pck;
        if (showType == WIFI_TURN_ON) {
            pck = SwitchGroup.WIFI_SWITCH;
        } else if (showType == BLUETOOTH_TURN_ON) {
            pck = SwitchGroup.BLUE_TOOTH_SWITCH;
        } else {
            pck = mContext.getPackageName();
        }

        mLockManager.applyLock(
                LockManager.LOCK_MODE_FULL, pck,
                false,
                new LockManager.OnUnlockedListener() {
                    @Override
                    public void onUnlocked() {
                        LeoLog.d(TAG, "onUnlocked");

                        if (showType == WIFI_TURN_ON) {
                            //delay 1s
                            mHandler.sendEmptyMessageDelayed(WIFI_ENABLED, DELAY_DISABL);
                        } else if (showType == BLUETOOTH_TURN_ON) {
                            mHandler.sendEmptyMessageDelayed(BLUETOOTH_ENABLED, DELAY_DISABL);
                        }
                    }

                    public void onUnlockCanceled() {
                        LeoLog.d(TAG, "onUnlockCanceled");
                    }

                    @Override
                    public void onUnlockOutcount() {

                    }
                });
    }

    public BroadcastReceiver mBlueToothChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LeoLog.d(TAG, "bluetooth onReceive");
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                        LeoLog.d(TAG, "BluetoothAdapter.STATE_TURNING_ON");
                        readyShowLock(BLUETOOTH_TURN_ON);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        unlockOpenBlueToothDone = false;
                        LeoLog.d(TAG, "BluetoothAdapter.STATE_ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        LeoLog.d(TAG, "BluetoothAdapter.STATE_TURNING_OFF");
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        unlockOpenBlueToothDone = false;
                        LeoLog.d(TAG, "BluetoothAdapter.STATE_OFF");
                        break;
                }
            }

        }
    };

    private void shutDownBlueTooth() {
        mHandler.sendEmptyMessageDelayed(BLUETOOTH_SHUT_DOWN, DELAY_DISABL);
    }

    @Override
    public boolean getIsLockNow(String name) {
        return false;
    }

    @Override
    public boolean getLockNum(String name) {
        return false;
    }

    @Override
    public void setSwitch(String name, boolean lock) {

    }


    @Override
    public void onDestory() {
        mContext.unregisterReceiver(mBlueToothChangeReceiver);
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

//    public BroadcastReceiver mSdcardReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            LeoLog.d(TAG, "mSdcardReceiver onReceive");
//            String action = intent.getAction();
//
//            if ("android.intent.action.MEDIA_MOUNTED".equals(action)) {
//                LeoLog.d(TAG, "action : " + action);
////                ImageGridActivity.mIsBackgoundRunning = false;
//            }
//
//            if ("action : android.intent.action.MEDIA_UNMOUNTED".equals(action)) {
//                LeoLog.d(TAG, "action : " + action);
////                ImageGridActivity.mIsBackgoundRunning = false;
//            }
//
//        }
//    };

}
