package com.leo.appmaster.utils;

import java.lang.reflect.Method;

import android.content.Context;
import android.telephony.TelephonyManager;

public class SimDetecter {
    Context context = null;

    public SimDetecter(Context _context) {
        context = _context;
    }

    public boolean isSimReady() {

        try {

            // singele SIM
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                return true;
            }
            /*个别山寨手机*/
            try {
                TelephonyManager telephonyManager1 = (TelephonyManager) context.getSystemService("phone1");
                if (telephonyManager1 != null) {
                    if (telephonyManager1.getSimState() == TelephonyManager.SIM_STATE_READY) {
                        return true;
                    }
                }
            } catch (Exception e) {
            }

            try {
                TelephonyManager telephonyManager2 = (TelephonyManager) context.getSystemService("phone2");
                if (telephonyManager2 != null) {
                    if (telephonyManager2.getSimState() == TelephonyManager.SIM_STATE_READY) {
                        return true;
                    }
                }
            } catch (Exception e) {
            }

            if (readyForQualcommDoubleSim()) {
                return true;
            }
        } catch (Exception e) {
        }

        return false;
    }

    /*双卡手机*/
    public boolean readyForQualcommDoubleSim() {
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

}
