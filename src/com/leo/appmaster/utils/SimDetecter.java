package com.leo.appmaster.utils;

import java.lang.reflect.Method;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

/**
 * 手机卡判断
 */
@SuppressWarnings("ResourceType")
public class SimDetecter {
    private static final String PHONE1 = "phone1";
    private static final String PHONE2 = "phone2";

    public static boolean isSimReady(Context context) {

        try {

            /**singele SIM*/
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                return true;
            }
            /**个别山寨双卡手机*/
            try {
                TelephonyManager telephonyManager1 = (TelephonyManager) context.getSystemService(PHONE1);
                if (telephonyManager1 != null) {
                    if (telephonyManager1.getSimState() == TelephonyManager.SIM_STATE_READY) {
                        return true;
                    }
                }
            } catch (Exception e) {
            }

            try {
                TelephonyManager telephonyManager2 = (TelephonyManager) context.getSystemService(PHONE2);
                if (telephonyManager2 != null) {
                    if (telephonyManager2.getSimState() == TelephonyManager.SIM_STATE_READY) {
                        return true;
                    }
                }
            } catch (Exception e) {
            }

            if (readyForQualcommDoubleSim(context)) {
                return true;
            }
        } catch (Exception e) {
        }

        return false;
    }

    /**
     * 双卡手机
     */
    private static boolean readyForQualcommDoubleSim(Context context) {
        try {
            Class<?> cx = Class
                    .forName("android.telephony.MSimTelephonyManager");
            Object obj = context.getSystemService("phone_msim");
            Integer simId_1 = 0;
            Integer simId_2 = 1;
            Method simMd = cx.getMethod("getSimState", int.class);
            int state0 = (Integer) simMd.invoke(obj, simId_1);
            int state1 = (Integer) simMd.invoke(obj, simId_2);
            LeoLog.d("SimDetecter", "state0=" + state0 + ";   state1=" + state1);
            if (state0 == TelephonyManager.SIM_STATE_READY || state1 == TelephonyManager.SIM_STATE_READY) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 判断是否为飞行模式
     */
    public static boolean isAirPlaneModel(Context context) {
        ContentResolver cr = context.getContentResolver();
        String ariPlaneModelOn = Settings.System.AIRPLANE_MODE_ON;
        int arplaneMode = Settings.System.getInt(cr, ariPlaneModelOn, 0);
        boolean isAirplaneMode;
        if (arplaneMode == 1) {
            /*为飞行模式*/
            isAirplaneMode = true;
        } else {
            /*不为为飞行模式*/
            isAirplaneMode = false;
        }
        return isAirplaneMode;
    }

}
