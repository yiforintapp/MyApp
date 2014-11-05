
package com.leo.appmaster;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.leo.appmaster.update.UIHelper;
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
        iniFlurry(ctx.getApplicationContext());
    }

    /**
     * add an event that we will push to log service
     * 
     * @param level log level for leoSDK, can be LeoStat.P0 ~ LeoStat.P4
     * @param eventID event type of this event
     * @param eventDescription detail of this event
     */
    public static void addEvent(int level, String eventID, String eventDescription) {
        LeoStat.addEvent(level, eventID, eventDescription);
        Map<String, String> params = new HashMap<String, String>();
        params.put("description", eventDescription);
        FlurryAgent.logEvent(eventID, params);
    }

    public static void endSession(Context ctx) {
        LeoStat.endSession();
        FlurryAgent.onEndSession(ctx);
    }

    private static void iniLeoSdk(Context ctx) {
        LeoStat.init(ctx, ctx.getString(R.string.channel_code),
                "appmaster");
        LeoStat.setDebugLevel(Log.DEBUG);
        LeoStat.initUpdateEngine(UIHelper.getInstance(ctx),
                true);
    }

    private static void iniFlurry(Context ctx) {
        FlurryAgent.setVersionName(ctx.getString(R.string.version_name) + "_"
                + ctx.getString(R.string.channel_code));
        FlurryAgent.onStartSession(ctx, "F6PHG92TXG5QZ48H4YC8");
    }

}
