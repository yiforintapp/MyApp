
package com.leo.appmaster.applocker.receiver;

import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.provider.Settings;
import android.view.WindowManager;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.callfilter.AskAddToBlacklistActivity;
import com.leo.appmaster.mgr.DeviceManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.showTrafficTip;
import com.leo.appmaster.ui.showTrafficTip.OnDiaogClickListener;
import com.leo.appmaster.utils.LeoLog;

public class showTrafficAlof extends BroadcastReceiver {
    private Context mContext;
    private AppMasterPreference sp_broadcast;
    ConnectivityManager mConnectivityManager;
    private long firstIn = 0;
    private long secondIn = 0;
    boolean isSwtich;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context.getApplicationContext();
        sp_broadcast = AppMasterPreference.getInstance(mContext);
        isSwtich = ((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)).
                getOverDataSwitch();
//        isSwtich = sp_broadcast.getFlowSetting();
        firstIn = sp_broadcast.getFirstTime();
        secondIn = System.currentTimeMillis();
        String action = intent.getAction();
        // 开机启动服务，开始获取，计算流量
         LeoLog.d("trafictest", "服务来通知咯，超额提示");
        if ("com.leo.appmaster.traffic.alot".equals(action)) {
            showAlarmDialog("com.leo.appmaster.traffic.alot");
        } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
            // LeoLog.d("ServiceTraffic",
            // "广播来了！android.net.conn.CONNECTIVITY_CHANGE");
            State wifiState = null;
            State mobileState = null;
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (ni != null) {
                wifiState = ni.getState();
            }
            ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (ni != null) {
                mobileState = ni.getState();
            }
            if (wifiState != null && mobileState != null
                    && State.CONNECTED != wifiState
                    && State.CONNECTED == mobileState) {
                // 手机网络连接成功

                // LeoLog.d("ServiceTraffic", "2G/3G/4G");
                boolean haveNotice = sp_broadcast.getAlotNotice();
                boolean mFinishNotice = sp_broadcast.getFinishNotice();

                if (haveNotice && isSwtich) {
                    if (mFinishNotice) {
                        if (secondIn - firstIn > 1200000) {
                            // LeoLog.d("ServiceTraffic", "广播！用完+满2小时！");
                            // LeoLog.d("testfucktime", "secondIn : " + secondIn
                            // + "---firstIn : " + firstIn);
                            showAlarmDialog(("com.leo.appmaster.traffic.finish"));
                            sp_broadcast.setFirstTime(secondIn);
                        }
                    } else {
                        if (secondIn - firstIn > 1200000) {
                            // LeoLog.d("testfucktime", "secondIn : " + secondIn
                            // + "---firstIn : " + firstIn);
                            // LeoLog.d("ServiceTraffic", "广播！超额+满2小时！");
                            showAlarmDialog("com.leo.appmaster.traffic.alot");
                            sp_broadcast.setFirstTime(secondIn);
                        }
                    }
                }
            } else if (wifiState != null && mobileState != null
                    && State.CONNECTED != wifiState
                    && State.CONNECTED != mobileState) {
                // 手机没有任何的网络
                // LeoLog.d("ServiceTraffic", "no network");
            } else if (wifiState != null && State.CONNECTED == wifiState) {
                // 无线网络连接成功
                // LeoLog.d("ServiceTraffic", "wifi");
            }
        } else if ("com.leo.appmaster.traffic.finish".equals(action)) {
            // LeoLog.d("ServiceTraffic", "服务来通知咯，用完提示");
            showAlarmDialog("com.leo.appmaster.traffic.finish");
        }
//        else if ("com.leo.appmaster.boost.notification".equals(action)) {
//            Intent realIntent = intent.getParcelableExtra("realIntent");  
//            context.startActivity(realIntent);
//            SDKWrapper.addEvent(mContext, SDKWrapper.P1,
//                    "boost", "statusbar");
//        }
    }

    private void showAlarmDialog(String action) {
        LeoLog.i("testtt", "to show");
        Intent intent = new Intent(mContext, AskAddToBlacklistActivity.class);
        intent.putExtra(AskAddToBlacklistActivity.EXTRA_WHAT_TO_SHOW, AskAddToBlacklistActivity.CASE_ALERT_FLOW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        LockManager mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        mLockManager.filterPackage(mContext.getPackageName(), 2000);
//        mContext.startActivity(intent);
        
//        final showTrafficTip dialog = new showTrafficTip(mContext);
        String tip = "";
        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "datapage", "data_notify");
        if (action.equals("com.leo.appmaster.traffic.alot")) {
            // LeoLog.d("ServiceTraffic", "流量超额通知！！");
            sp_broadcast.setAlotNotice(true);
            tip = mContext.getString(R.string.traffic_used_lot_text,((DeviceManager) MgrContext.getManager(MgrContext.MGR_DEVICE)). getOverDataInvokePercent());

        } else if (action.equals("com.leo.appmaster.traffic.finish")) {
            // LeoLog.d("ServiceTraffic", "流量用完通知！！");
            sp_broadcast.setFinishNotice(true);
            tip = mContext.getString(R.string.traffic_used_finish_text);
        }

        intent.putExtra(AskAddToBlacklistActivity.EXTRA_FLOW_TIPS, tip);
        // dialog.setTitle(R.string.traffic_used_lot);
//        dialog.setTitle(R.string.traffic_used_lot);
//        dialog.setContent(tip);
//        dialog.setOnClickListener(new OnDiaogClickListener() {
//            @Override
//            public void onClick(int which) {
//                if (which == 0) {
//                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "datapage", "data_cnts_notify");
//                    // 关闭网络
//                    setMobileNetUnable();
//                }
//                dialog.cancel();
//            }
//        });
//        dialog.getWindow().setType(
//                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//        dialog.show();
        mContext.startActivity(intent);
    }

    public final void setMobileNetUnable() {
        // LeoLog.d("ServiceTraffic", "关闭网络咯！！");
        if (android.os.Build.VERSION.SDK_INT > 19) {
            try {
                Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Object[] arg = null;
            try {
                boolean isMobileDataEnable = invokeMethod("getMobileDataEnabled", arg);
                if (isMobileDataEnable) {
                    invokeBooleanArgMethod("setMobileDataEnabled", false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean invokeMethod(String methodName, Object[] arg)
            throws Exception {

        ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        Class ownerClass = mConnectivityManager.getClass();

        Class[] argsClass = null;
        if (arg != null) {
            argsClass = new Class[1];
            argsClass[0] = arg.getClass();
        }

        Method method = ownerClass.getMethod(methodName, argsClass);

        Boolean isOpen = (Boolean) method.invoke(mConnectivityManager, arg);

        return isOpen;
    }

    public Object invokeBooleanArgMethod(String methodName, boolean value)
            throws Exception {

        ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        Class ownerClass = mConnectivityManager.getClass();

        Class[] argsClass = new Class[1];
        argsClass[0] = boolean.class;

        Method method = ownerClass.getMethod(methodName, argsClass);

        return method.invoke(mConnectivityManager, value);
    }

}
