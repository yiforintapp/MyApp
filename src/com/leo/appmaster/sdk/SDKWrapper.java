
package com.leo.appmaster.sdk;

import android.content.Context;
import android.util.Log;

import com.baidu.mobstat.StatService;
import com.leo.appmaster.R;
import com.leo.appmaster.sdk.push.PushUIHelper;
import com.leo.appmaster.sdk.update.UIHelper;
import com.leoers.leoanalytics.LeoStat;

public class SDKWrapper {

    /**
     * initial leo analytics and flurry SDK, this will changed in the future.
     * this should be called in application's onCreate method.
     * 
     * @param ctx should be application context
     */
    public static void iniSDK(Context ctx) {
        iniLeoSdk(ctx.getApplicationContext());
        /* try initiate BaiduMTJ in AndroidManifest.xml */
//        iniBaidu(ctx);
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
        // leo
        LeoStat.addEvent(level, id, description);
        // baidu
        StatService.onEvent(ctx, id, description);
    }

    public static void endSession(Context ctx) {
        LeoStat.endSession();
    }

    private static void iniLeoSdk(Context ctx) {
        LeoStat.init(ctx, ctx.getString(R.string.channel_code),
                "appmaster");
        // TODO: change log level to ERROR when release
        LeoStat.setDebugLevel(Log.VERBOSE);
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
