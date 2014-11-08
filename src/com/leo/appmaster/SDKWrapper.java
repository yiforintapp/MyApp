
package com.leo.appmaster;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.baidu.mobstat.SendStrategyEnum;
import com.baidu.mobstat.StatService;
import com.flurry.android.FlurryAgent;
import com.leo.appmaster.push.PushUIHelper;
import com.leo.appmaster.update.UIHelper;
import com.leoers.leoanalytics.LeoStat;

public class SDKWrapper {

    private final static String CHANNEL_CODE_FOR_91 = "0011a";

    private static boolean sChannelFor91 = false;

    /**
     * initial leo analytics and flurry SDK, this will changed in the future.
     * this should be called in application's onCreate method.
     * 
     * @param ctx should be application context
     */
    public static void iniSDK(Context ctx) {
        iniLeoSdk(ctx.getApplicationContext());
        iniFlurry(ctx.getApplicationContext());
        if (ctx.getString(R.string.channel_code).equalsIgnoreCase(CHANNEL_CODE_FOR_91)) {
            sChannelFor91 = true;
            iniBaidu(ctx);
        }
    }

    public static boolean isChannelFor91() {
        return sChannelFor91;
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
        // flurry
        Map<String, String> params = new HashMap<String, String>();
        params.put("description", description);
        FlurryAgent.logEvent(id, params);
        if (sChannelFor91) {
            // baidu
            StatService.onEvent(ctx, id, description);
        }
    }

    public static void endSession(Context ctx) {
        LeoStat.endSession();
    }

    private static void iniLeoSdk(Context ctx) {
        LeoStat.init(ctx, ctx.getString(R.string.channel_code),
                "appmaster");
        // TODO: change log level to ERROR when release
        LeoStat.setDebugLevel(Log.ERROR);
        LeoStat.initUpdateEngine(UIHelper.getInstance(ctx),
                true);
        LeoStat.initPushEngine(PushUIHelper.getInstance(ctx));
    }

    private static void iniFlurry(Context ctx) {
        FlurryAgent.setVersionName(ctx.getString(R.string.version_name) + "_"
                + ctx.getString(R.string.channel_code));
        FlurryAgent.setLogEvents(true);
        // TODO: disable internal log when release
        FlurryAgent.setLogEnabled(false);
    }

    private static void iniBaidu(Context ctx) {
        StatService.setAppKey("1004e462a2");
        StatService.setAppChannel(ctx, ctx.getString(R.string.channel_code), true);
        StatService.setSessionTimeOut(30);
        StatService.setOn(ctx, StatService.EXCEPTION_LOG);
        StatService.setSendLogStrategy(ctx, SendStrategyEnum.APP_START, 1, false);
        StatService.setLogSenderDelayed(5);
        // TODO: disable internal log when release
        // StatService.setDebugOn(true);
    }

}
