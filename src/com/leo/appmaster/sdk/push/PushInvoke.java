
package com.leo.appmaster.sdk.push;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.util.Log;

import com.leo.analytics.LeoAgent;
import com.leo.appmaster.AppMasterConfig;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.ADShowTypeRequestManager;
import com.leo.appmaster.bootstrap.CheckNewBootstrap;
import com.leo.appmaster.bootstrap.SplashBootstrap;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.schedule.MsgCenterFetchJob;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.sdk.update.UIHelper;
import com.leo.appmaster.utils.LeoLog;
import com.leo.push.data.PushInvokeHelper;

public class PushInvoke implements PushInvokeHelper {

    public static final String TYPE_BROAD = "event_type_broad";
    static PushInvoke mInvoke = null;
    private Context mContext = null;
    public static final String THEME = "1";
    public static final String HOTAPP = "2";
    public static final String UPDATE = "3";
    public static final String SPLASH = "4";
    public static final String GPGUIDE = "5";
    public static final String ADWALL = "6";
    public static final String ISWIPE = "7";
    public static final String MSG_CENTER = "8";
    
    public static final String ACTION_UPDATE_I = "com.leo.appmaster.invoke.update";
    public static final String ACTION_THEME_I = "com.leo.appmaster.invoke.theme";
    public static final String ACTION_SPLASH_I = "com.leo.appmaster.invoke.splash";
    public static final String ACTION_HOTAPP_I = "com.leo.appmaster.invoke.hotapp";
    public static final String ACTION_AD_I = "com.leo.appmaster.invoke.ad";
    public static final String ACTION_GP_I = "com.leo.appmaster.invoke.gp";
    public static final String ACTION_ISWIPE_I = "com.leo.appmaster.invoke.iswipe";

    public static final String PUSH_GOTO_PAGE_TRAFFIC = ".appmanage.FlowActivity";
    public static final String PUSH_GOTO_PAGE_ELEC = ".appmanage.EleActivity";
    public static final String PUSH_GOTO_PAGE_BACKUP = ".appmanage.BackUpActivity";
    public static final String PUSH_GOTO_PAGE_ISWIPE = ".quickgestures.ui.QuickGestureActivity";
    public static final String PUSH_GOTO_PAGE_THEME = ".lockertheme.LockerTheme";
    public static final String PUSH_GOTO_PAGE_HotApp = ".appmanage.HotAppActivity";
    public static final String PUSH_GOTO_GAME = ".appwall.AppWallActivity";
    public static final String PUSH_GOTO_MSGCENTER = ".msgcenter.MsgCenterActivity";

    public PushInvoke(Context ctx) {
        this.mContext = ctx;
    }

    public static PushInvoke getInstance(Context ctx) {
        if (mInvoke == null) {
            mInvoke = new PushInvoke(ctx);
        }
        return mInvoke;
    }

    @Override
    public void onInvoke(String type) {
        Log.d("PushInvoke", "type is : " + type);
        if (type.equals(THEME)) {
            invokeNewTheme();
            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "push_refresh", "theme");
        } else if (type.equals(HOTAPP)) {
            invokeHotApp();
            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "push_refresh", "hots");
        } else if (type.equals(UPDATE)) {
            invokeUpdate();
            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "push_refresh", "update");
        } else if (type.equals(SPLASH)) {
            invokeSplash();
            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "push_refresh", "screen");
        } else if (type.equals(GPGUIDE)) {
            invokeGp();
            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "push_refresh", "GP_guide");
        } else if (type.equals(ADWALL)) {
            invokeAd();
            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "push_refresh", "adunlocktop");
        } else if (type.equals(ISWIPE)) {
            invokeISwipe();
            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "push_refresh", "iSwipe");
        } else if (MSG_CENTER.equals(type)) {
            MsgCenterFetchJob.startImmediately();
            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "push_refresh", "push_Info_cnts");
        }
        // String mAction = getAction(type);
        // Log.d("PushInvoke", "mAction is : " + mAction);
        // Intent intent = new Intent(mContext, PushInvokeBroadcast.class);
        // intent.putExtra(TYPE_BROAD, type);
        // intent.setAction(mAction);
        // mContext.sendBroadcast(intent);
    }

    // private String getAction(String type) {
    // String action;
    // if (type.equals(THEME)) {
    // action = ACTION_THEME_I;
    // } else if (type.equals(HOTAPP)) {
    // action = ACTION_HOTAPP_I;
    // } else if (type.equals(UPDATE)) {
    // action = ACTION_UPDATE_I;
    // } else if (type.equals(SPLASH)) {
    // action = ACTION_SPLASH_I;
    // } else if (type.equals(GPGUIDE)) {
    // action = ACTION_GP_I;
    // } else if (type.equals(ADWALL)) {
    // action = ACTION_AD_I;
    // } else if (type.equals(ISWIPE)) {
    // action = ACTION_ISWIPE_I;
    // } else {
    // action = "";
    // }
    // return action;
    // }

    @Override
    public void addTimeFilter(String mType) {
        // String mType = getMidName(actionContent);
        LeoLog.d("testDEO", "addTimeFilter mType is : " + mType);
        if (mType.equals(PUSH_GOTO_PAGE_TRAFFIC)
                || mType.equals(PUSH_GOTO_PAGE_ELEC)
                || mType.equals(PUSH_GOTO_PAGE_BACKUP)
                || mType.equals(PUSH_GOTO_PAGE_ISWIPE)
                || mType.equals(PUSH_GOTO_PAGE_THEME)
                || mType.equals(PUSH_GOTO_PAGE_HotApp)
                || mType.equals(PUSH_GOTO_GAME)
                || mType.equals(PUSH_GOTO_MSGCENTER))
        {
            LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
            lm.filterPackage(mContext.getPackageName(), 1000);
        }

    }

//    private String getMidName(String actionContent) {
//        String mType = "d";
//        try {
//            String[] mContent = actionContent.split(";");
//            for (String mString : mContent) {
//                if (mString.startsWith("component=")) {
//                    String[] mStrings = mString.split("/");
//                    mType = mStrings[1];
//                }
//            }
//            Debug.d("testGetName", "mType is : " + mType);
//            // String[] mMid = mContent[1].split("/");
//            // mType = mMid[1];
//        } catch (Exception e) {
//            return mType;
//        }
//        return mType;
//    }

    private void invokeISwipe() {

    }

    private void invokeGp() {
        /* 解锁次数设置为初始状态 */
        AppMasterPreference.getInstance(mContext).setUnlockCount(0);
        /* googlepaly评分提示设置为初始状态 */
        AppMasterPreference.getInstance(mContext).setGoogleTipShowed(false);
    }

    private void invokeNewTheme() {
        CheckNewBootstrap.setFromPush(true);
        CheckNewBootstrap.checkNewTheme();
    }

    private void invokeHotApp() {
        CheckNewBootstrap.setFromPush(true);
        CheckNewBootstrap.checkNewAppBusiness();
    }

    private void invokeSplash() {
        SplashBootstrap.loadSplashDate(true);
    }

    // FengShi do it
    private void invokeAd() {
        ADShowTypeRequestManager.getInstance(mContext).mIsPushRequestADShowType=true;
        ADShowTypeRequestManager.getInstance(mContext).loadADCheckShowType(null);
    }

    private void invokeUpdate() {
        // PushNotification.isFromPush_Update = true;
        // planB convenient
        try {
            LeoAgent.init(mContext, mContext.getString(R.string.channel_code),
                    "appmaster");
        } catch (NotFoundException e) {
            /* this happened rarely, but got a user feedback AM-593 */
            LeoAgent.init(mContext, "0000a", "appmaster");
        }
        // TODO: change log level to ERROR when release
        LeoAgent.setDebugLevel(AppMasterConfig.SDK_LOG_LEVEL);
        LeoAgent.initUpdateEngine(UIHelper.getInstance(mContext),
                true, true);

        // planA -- can do
        // mTracker = LeoTracker.getInstance(mContext, "0000a", "appmaster");
        // mUpdateManager = mTracker.getUpdateManager();
        //
        // try {
        // if (mUpdateManager != null) {
        // mUpdateManager.startEngine(true);
        // } else {
        // throw new RuntimeException(
        // "mUpdateManager is null, did you call initUpdateManager()?");
        // }
        // } catch (Exception e) {
        //
        // }
    }
}
