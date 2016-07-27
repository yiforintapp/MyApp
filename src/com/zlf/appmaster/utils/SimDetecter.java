package com.zlf.appmaster.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * 手机卡判断
 */
@SuppressWarnings("ResourceType")
public class SimDetecter {
    private static final String PHONE1 = "phone1";
    private static final String PHONE2 = "phone2";
    //SIM卡1
    public static final int SIM_TYPE_O = 0;
    //SIM卡2
    public static final int SIM_TYPE_1 = 1;
    private static final int SMS_COTENT_MAX = 70;

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
            com.zlf.appmaster.utils.LeoLog.d("SimDetecter", "state0=" + state0 + ";   state1=" + state1);
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

    //MTK双卡短信发送
    public static boolean sendMtkDoubleSim(String number, String content, int simType) {

        if (TextUtils.isEmpty(number) || TextUtils.isEmpty(content)) {
            return false;
        }

        Class<?> smsEx = null;
        ArrayList<String> contents = null;
        try {
            smsEx = Class.forName("com.mediatek.telephony.SmsManagerEx");
            Method df = smsEx.getMethod("getDefault", new Class[0]);
            Object sim = df.invoke(smsEx);

            Class[] types_send = new Class[6];
            types_send[0] = Class.forName("java.lang.String");
            types_send[1] = Class.forName("java.lang.String");
            types_send[2] = Class.forName("java.util.ArrayList");
            types_send[3] = Class.forName("java.util.ArrayList");
            types_send[4] = Class.forName("java.util.ArrayList");
            types_send[5] = int.class;
            Method send = null;
            SmsManager sm = SmsManager.getDefault();
//            if (content.length() > SMS_COTENT_MAX) {
            contents = sm.divideMessage(content);
            send = smsEx.getDeclaredMethod("sendMultipartTextMessage", types_send);
//            } else {
//                send = smsEx.getDeclaredMethod("sendDataMessage", types_send);
//            }
//          sendDataMessage(java.lang.String,java.lang.String,short,byte[],android.app.PendingIntent,android.app.PendingIntent,int)"

            Object[] params = new Object[6];
            params[0] = number;
            params[1] = null;
            params[2] = contents;
            params[3] = null;
            params[4] = null;
            params[5] = simType;

            send.invoke(sim, params);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //高通双卡发送短信
    public static boolean sendGTDoubleSim(String number, String content, int simType) {

        if (TextUtils.isEmpty(number) || TextUtils.isEmpty(content)) {
            return false;
        }

        Class<?> smsEx = null;
        ArrayList<String> contents = null;
        try {
            smsEx = Class.forName("android.telephony.MSimSmsManager");
            Method df = smsEx.getMethod("getDefault", new Class[0]);
            Object sim = df.invoke(smsEx);

            Class[] types_send = new Class[6];
            types_send[0] = Class.forName("java.lang.String");
            types_send[1] = Class.forName("java.lang.String");
            types_send[2] = Class.forName("java.util.ArrayList");
            types_send[3] = Class.forName("java.util.ArrayList");
            types_send[4] = Class.forName("java.util.ArrayList");
            types_send[5] = int.class;
            Method send = null;
            SmsManager sm = SmsManager.getDefault();
            contents = sm.divideMessage(content);
            send = smsEx.getDeclaredMethod("sendMultipartTextMessage", types_send);

            Object[] params = new Object[6];
            params[0] = number;
            params[1] = null;
            params[2] = contents;
            params[3] = null;
            params[4] = null;
            params[5] = simType;

            send.invoke(sim, params);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
