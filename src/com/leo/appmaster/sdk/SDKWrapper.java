
package com.leo.appmaster.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources.NotFoundException;

import com.baidu.mobstat.StatService;
import com.leo.analytics.LeoAgent;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterConfig;
import com.leo.appmaster.R;
import com.leo.appmaster.sdk.push.PushInvoke;
import com.leo.appmaster.sdk.update.UIHelper;
import com.leo.appmaster.utils.LeoLog;
import com.leo.push.IPushStatHelper;
import com.leo.push.PushManager;
import com.tendcloud.tenddata.TCAgent;


public class SDKWrapper {

    private final static String TAG = "SDKWrapper";
    
    // useless with LeoAnaSDK_v3
    public static int P1  = 0;

    /**
     * initial leo analytics and flurry SDK, this will changed in the future.
     * this should be called in application's onCreate method.
     * 
     * @param ctx should be application context
     */
    public static void iniSDK(Context ctx) {
        try {
            Context context = ctx.getApplicationContext();
            iniLeoSdk(context);
            /* try initiate BaiduMTJ in AndroidManifest.xml */
            // iniBaidu(ctx);
            iniPushSDK(ctx);
            
            initNewSDK(context);
            
            // TalkingData
            TCAgent.LOG_ON = AppMasterConfig.LOGGABLE;
            TCAgent.init(ctx);
        } catch (Exception e) {
            
        }

    }
    
    public static String getBestServerDomain(){
        return LeoAgent.getBestServerDomain();
    }
    
    public static void checkUpdate(){
        LeoAgent.checkUpdate();
    }
    
    public static String getEncodedDeviceInfo(){
        return LeoAgent.getEncodedDeviceInfo();
    }
    
    public static boolean isUpdateAvailable(){
        return LeoAgent.isUpdateAvailable();
    }
    
    public static void checkForceUpdate(){
        LeoAgent.checkForceUpdate();
    }

    private static void iniPushSDK(final Context ctx) {
        /* TODO: change this from Log.DEBUG to Log.ERROR when release */
        PushManager.getInstance(ctx).setUiHelper(PushInvoke.getInstance(ctx));
        PushManager.getInstance(ctx).setDebugLevel(AppMasterConfig.SDK_LOG_LEVEL);
        try {
            int resId = ctx.getResources().getIdentifier("ic_launcher_notification_big", "drawable", ctx.getPackageName());
            PushManager.getInstance(ctx).setIcon(resId);
        } catch (NotFoundException e) {
            LeoLog.e(TAG, "failed to get ICON");
        }
        
        try {
            PushManager.getInstance(ctx).startPush(ctx.getString(R.string.channel_code),R.layout.custom_big_notification, R.id.img_big, R.id.tv_msg);
        } catch (NotFoundException e) {
            PushManager.getInstance(ctx).startPush("0000a", R.layout.custom_big_notification, R.id.img_big, R.id.tv_msg);
        }
        
        PushManager.getInstance(ctx).registerStatHelper(new IPushStatHelper(){
            @Override
            public void onPushSDKEvent(String eventID, String description) {
                SDKWrapper.addEvent(ctx, 0, eventID, description);
            }
            
        } );
   }
    
    private static void initNewSDK(Context ctx) {
        com.leo.stat.StatService.setAppId("privacyguard");
        try {
            com.leo.stat.StatService.setMarketId(ctx.getString(R.string.channel_code));
        } catch (Exception e) {
            com.leo.stat.StatService.setMarketId("0000a");
        }
        com.leo.stat.StatService.setsDebugLevel(AppMasterConfig.SDK_LOG_LEVEL);
        com.leo.stat.StatService.initialize(ctx);
    }

    /**
     * add an event that we will push to log service
     * 
     * @param ctx activity context
     * @param level log level for leoSDK, can be LeoStat.P0 ~ LeoStat.P4
     * @param eventID event type of this event
     * @param eventDescription detail of this event
     */
    public static void addEvent(Context context, int level, String id, String description) {
        // AM-727
//        LeoLog.d(TAG, "addEvent: id=" + id + ";   desc=" + description);
        // leo
//        LeoAgent.addEvent(id, description);
        // baidu
        Context ctx = AppMasterApplication.getInstance();
        StatService.onEvent(ctx, id, description);
        // leo
        com.leo.stat.StatService.onEvent(ctx, id, description);
        // TalkingData
        TCAgent.onEvent(ctx, id, description);
    }

    public static void endSession(Context ctx) {
        // LeoStat.endSession();
    }

    private static void iniLeoSdk(Context ctx) {
        try {
            LeoAgent.init(ctx, ctx.getString(R.string.channel_code),
                    "appmaster");
        } catch (NotFoundException e) {
            /* this happened rarely, but got a user feedback AM-593 */
            LeoAgent.init(ctx, "0000a", "appmaster");
        }
        // TODO: change log level to ERROR when release
        LeoAgent.setDebugLevel(AppMasterConfig.SDK_LOG_LEVEL);
        LeoAgent.initUpdateEngine(UIHelper.getInstance(ctx),
                true,false);
    }
    
    public static void onResume(Activity ctx){
        // 百度的SDK会引起内存泄露，但又不能使用context作为参数传进去。。。蛋疼~
        StatService.onResume(ctx);
//        LeoAgent.onResume();
        TCAgent.onResume(ctx);
        com.leo.stat.StatService.onResume(ctx);
    }
    
    public static void onPause(Activity ctx){
        StatService.onPause(ctx);
//        LeoAgent.onPause();
        TCAgent.onPause(ctx);
        com.leo.stat.StatService.onPause(ctx);
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
