
package com.leo.appmaster.sdk;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.util.Log;

import com.baidu.mobstat.StatService;
import com.leo.appmaster.AppMasterConfig;
import com.leo.appmaster.R;
import com.leo.appmaster.sdk.push.PushUIHelper;
import com.leo.appmaster.sdk.update.UIHelper;
import com.leo.appmaster.utils.LeoLog;
import com.leo.push.PushManager;
import com.leoers.leoanalytics.LeoStat;

public class SDKWrapper {

    private final static String TAG = "SDKWrapper";

    /**
     * initial leo analytics and flurry SDK, this will changed in the future.
     * this should be called in application's onCreate method.
     * 
     * @param ctx should be application context
     */
    public static void iniSDK(Context ctx) {
        iniLeoSdk(ctx.getApplicationContext());
        /* try initiate BaiduMTJ in AndroidManifest.xml */
        // iniBaidu(ctx);
        iniPushSDK(ctx);
    }
    
    public static String getBestServerDomain(){
        return LeoStat.getBestServerDomain();
    }

    private static void iniPushSDK(Context ctx) {
        /* TODO: change this from Log.DEBUG to Log.ERROR when release */
        PushManager.getInstance(ctx).setDebugLevel(AppMasterConfig.SDK_LOG_LEVEL);
        try {
            int resId = ctx.getResources().getIdentifier("ic_launcher_notification_big", "drawable", ctx.getPackageName());
            PushManager.getInstance(ctx).setIcon(resId);
        } catch (NotFoundException e) {
            LeoLog.e(TAG, "failed to get ICON");
        }
        
        try {
            PushManager.getInstance(ctx).startPush(ctx.getString(R.string.channel_code));
        } catch (NotFoundException e) {
            PushManager.getInstance(ctx).startPush("0000a");
        }
   }

    /**
     * add an event that we will push to log service
     * 
     * @param ctx activity context
     * @param level log level for leoSDK, can be LeoStat.P0 ~ LeoStat.P4
     * @param eventID event type of this event
     * @param eventDescription detail of this event
     */
    public static void addEvent(Context ctx, int level, String id, String description) {
        // AM-727
//        LeoLog.d(TAG, "addEvent: id=" + id + ";   desc=" + description);
        // leo
        LeoStat.addEvent(level, id, description);
        // baidu
        StatService.onEvent(ctx, id, description);
    }

    public static void endSession(Context ctx) {
        LeoStat.endSession();
    }

    private static void iniLeoSdk(Context ctx) {
        try {
            LeoStat.init(ctx, ctx.getString(R.string.channel_code),
                    "appmaster");
        } catch (NotFoundException e) {
            /* this happened rarely, but got a user feedback AM-593 */
            LeoStat.init(ctx, "0000a", "appmaster");
        }
        // TODO: change log level to ERROR when release
        LeoStat.setDebugLevel(AppMasterConfig.SDK_LOG_LEVEL);
        LeoStat.initUpdateEngine(UIHelper.getInstance(ctx),
                true);
        LeoStat.initPushEngine(PushUIHelper.getInstance(ctx));
    }

    // private static void iniBaidu(Context ctx) {
    // // TODO: use release Key when release
    // StatService.setAppKey("88ce739ea6"); // debug Key
    // // StatService.setAppKey("1004e462a2"); // release key
    // StatService.setAppChannel(ctx, ctx.getString(R.string.channel_code),
    // true);
    // StatService.setSessionTimeOut(30);
    // StatService.setOn(ctx, StatService.EXCEPTION_LOG);
    // StatService.setSendLogStrategy(ctx, SendStrategyEnum.APP_START, 1,
    // false);
    // StatService.setLogSenderDelayed(5);
    // // TODO: disable internal log when release
    // StatService.setDebugOn(true);
    // }

}
