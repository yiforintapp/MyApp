
package com.leo.appmaster.quickgestures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.locks.Lock;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.CallLog.Calls;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.appmanage.business.AppBusinessManager;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.model.BusinessItemInfo;
import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.privacycontact.ContactCallLog;
import com.leo.appmaster.privacycontact.MessageBean;
import com.leo.appmaster.privacycontact.PrivacyContactActivity;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.quickgestures.model.QuickGestureContactTipInfo;
import com.leo.appmaster.quickgestures.model.QuickGsturebAppInfo;
import com.leo.appmaster.quickgestures.ui.QuickGestureFilterAppDialog;
import com.leo.appmaster.quickgestures.ui.QuickGesturePopupActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NotificationUtil;
import com.leo.appmaster.utils.Utilities;

public class QuickGestureManager {
    public static final String TAG = "QuickGestureManager";
    public static final boolean DBG = true;
    /* 接受隐私联系人存在未读广播的权限 */
    private static final String SEND_RECEIVER_TO_SWIPE_PERMISSION = "com.leo.appmaster.RECEIVER_TO_ISWIPE";
    private static final String RECEIVER_TO_SWIPE_ACTION = "com.leo.appmaster.ACTION_PRIVACY_CONTACT";
    private static final String RECEIVER_TO_SWIPE_ACTION_CANCEL_PRIVACY_TIP = "com.leo.appmaster.ACTION_CANCEL_PRIVACY_TIP";

    private static final String ACTION_FIRST_USE_LOCK_MODE = "com.leo.appmaster.ACTION_FIRST_USE_LOCK_MODE";
    private static final String ACTION_LOCK_MODE_CHANGE = "com.leo.appmaster.ACTION_LOCK_MODE_CHANGE";

    public static final String PRIVACY_MSM = "privacy_msm";
    public static final String PRIVACY_CALL = "privacy_call";
    public static final String PRIVACYCONTACT_TO_IWIPE_KEY = "privacycontact_to_iswipe";
    protected static final String AppLauncherRecorder = null;
    public static boolean isFromDialog = false;
    public static boolean isClickSure = false;
    public static final int APPITEMINFO = 1;
    public static final int NORMALINFO = 0;
    public static final String RECORD_CALL = "call";
    public static final String RECORD_MSM = "msm";
    private static QuickGestureManager mInstance;
    private Context mContext;
    private boolean mInited = false;
    private List<String> mDeletedBusinessItems;
    private AppMasterPreference mSpSwitch;
    public List<MessageBean> mMessages;
    public List<ContactCallLog> mCallLogs;
    public List<BaseInfo> mDynamicList;
    public List<BaseInfo> mMostUsedList;
    // private Drawable[] mColorBgIcon;
    private int[] mColorBgIconIds;
    private Drawable mEmptyIcon;
    public int mSlidAreaSize;
    public boolean isShowPrivacyMsm = false;
    public boolean isShowPrivacyCallLog = false;
    public boolean isShowSysNoReadMessage = false;
    public boolean mToMsmFlag;
    public boolean mToCallFlag;
    public static String QUICK_GESTURE_SETTING_EVENT = "quick_gesture_setting_event";
    public boolean isDialogShowing = false;
    public int onTuchGestureFlag = -1;// -1:左侧底，-2：左侧中，1：右侧底，2：右侧中
    public boolean isJustHome;
    public boolean isAppsAndHome;
    public boolean isLeftBottom, isRightBottom, isLeftCenter, isRightCenter;
    public int screenSpace;// 根布局与屏幕高的差值
    public boolean isMessageReadRedTip;
    public String privacyLastRecord;// RECORD_CALL:最后记录为通话记录，RECORD_MSM：最后记录为短信
    public boolean isCallLogRead;
    private QuickGestureFilterAppDialog quickSwitch;

    private QuickGestureFilterAppDialog commonApp;

    private QuickGestureManager(Context ctx) {
        mContext = ctx.getApplicationContext();
        mSpSwitch = AppMasterPreference.getInstance(mContext);
        mDeletedBusinessItems = new ArrayList<String>();
        screenSpace = AppMasterPreference.getInstance(ctx).getRootViewAndWindowHeighSpace();
        loadDeletedBusinessItems();

        final Handler handler = new Handler(Looper.myLooper());
        mContext.getContentResolver().registerContentObserver(Constants.LOCK_MODE_URI, true,
                new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                sendLockModeChangeToISwipe();
            }
        });
    }

    public static synchronized QuickGestureManager getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new QuickGestureManager(ctx);
        }
        return mInstance;
    }

    public void init() {
        if (!mInited) {
            mInited = true;
            mDynamicList = new ArrayList<BaseInfo>();
            mMostUsedList = new ArrayList<BaseInfo>();
            preloadColorIcon();
            Bitmap bmp;
            LockManager lm = LockManager.getInstatnce();
            // for (Drawable drawable : mColorBgIcon) {
            // bmp = ((BitmapDrawable) drawable).getBitmap();
            // lm.mMatcher.addBitmapSample(bmp);
            // }
            long start = SystemClock.elapsedRealtime();
            for (int drawableId : mColorBgIconIds) {
                lm.mMatcher.addBitmapSample(drawableId);
            }
            // todo load app icon matcher color
            preloadIconMatcherColor();

            QuickSwitchManager.getInstance(mContext).init();
            
            AppMasterPreference pref = AppMasterPreference.getInstance(mContext);
            if (pref.getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
                sendFirstUseLockModeToISwipe();
            }
        }
    }

    private void preloadIconMatcherColor() {
        AppLoadEngine engine = AppLoadEngine.getInstance(mContext);
        ArrayList<AppItemInfo> allApps = engine.getAllPkgInfo();
        for (AppItemInfo appItemInfo : allApps) {
            getMatchedColor(appItemInfo.icon);
        }
    }

    public List<String> getDeletedBusinessList() {
        return mDeletedBusinessItems;
    }

    private void loadDeletedBusinessItems() {
        AppMasterPreference amp = AppMasterPreference.getInstance(mContext);
        String deleteds = amp.getDeletedBusinessItems();
        deleteds.subSequence(0, deleteds.length() - 1);
        String[] resault = deleteds.split(";");
        for (String string : resault) {
            mDeletedBusinessItems.add(string);
        }
    }

    private void addDeleteBusinessItem(String pkg) {
        mDeletedBusinessItems.add(pkg);
        AppMasterPreference amp = AppMasterPreference.getInstance(mContext);
        String deteleds = amp.getDeletedBusinessItems();
        deteleds = deteleds + pkg + ";";
        amp.setDeletedBusinessItems(deteleds);
    }

    public void unInit() {
        if (mInited) {
            mInited = false;
            mDynamicList.clear();
            mMostUsedList.clear();
            mDynamicList = null;
            mMostUsedList = null;
            LockManager.getInstatnce().mAppLaunchRecorders.clear();
            // mColorBgIcon = null;
            mColorBgIconIds = null;
            LockManager.getInstatnce().mMatcher.clearItem();
            LockManager.getInstatnce().clearDrawableColor();
            QuickSwitchManager.getInstance(mContext).unInit();
        }
    }

    public Drawable getMatchedColor(Drawable drawable) {
        LockManager lm = LockManager.getInstatnce();
        int drawableId = lm.getDrawableColorId(drawable);
        if (drawableId <= 0) {
            drawableId = lm.mMatcher.getMatchedDrawableId(drawable);
            if (drawableId > 0) {
                lm.putDrawableColorId(drawable, drawableId);
            }
        }

        if (drawableId > 0) {
            Context context = AppMasterApplication.getInstance();
            return context.getResources().getDrawable(drawableId);
        }

        return null;
    }

    public List<BaseInfo> getDynamicList() {
        long startTime = System.currentTimeMillis();
        AppLoadEngine engine = AppLoadEngine.getInstance(mContext);
        List<BaseInfo> dynamicList = new ArrayList<BaseInfo>();
        if (!AppMasterPreference.getInstance(mContext).getLastBusinessRedTipShow()) {
            Vector<BusinessItemInfo> businessDatas = AppBusinessManager.getInstance(mContext)
                    .getBusinessData();
            if (businessDatas != null && businessDatas.size() > 0) {
                int count = 0;
                for (BusinessItemInfo businessItem : businessDatas) {
                    businessItem.gesturePosition = -1000;
                    if (count == 1) {
                        break;
                    }
                    if (!businessItem.iconLoaded || businessItem.icon == null
                            || mDeletedBusinessItems.contains(businessItem.packageName)) {
                        continue;
                    }
                    if (engine.getAppInfo(businessItem.packageName) != null) {
                        continue;
                    }

                    count++;
                    dynamicList.add(businessItem);
                }
            }
        }
        // no read sys_message
        boolean isShowMsmTip = AppMasterPreference.getInstance(mContext)
                .getSwitchOpenNoReadMessageTip();
        boolean isShowCallLogTip = AppMasterPreference.getInstance(mContext)
                .getSwitchOpenRecentlyContact();
        boolean isShowPrivacyContactTip = AppMasterPreference.getInstance(mContext)
                .getSwitchOpenPrivacyContactMessageTip();
        if (isShowMsmTip) {
            if (getQuiQuickNoReadMessage() != null
                    && getQuiQuickNoReadMessage().size() > 0) {
                MessageBean message = getQuiQuickNoReadMessage().get(0);
                message.gesturePosition = -1000;
                message.icon = mContext.getResources().getDrawable(
                        R.drawable.gesture_message);
                message.label = mContext.getResources().getString(
                        R.string.privacy_contact_message);
                // } else {
                // if (message.getMessageName() != null
                // && !"".equals(message.getMessageName())) {
                // message.label = message.getMessageName();
                // } else {
                // message.label = message.getPhoneNumber();
                // }
                // }
                message.isShowReadTip = true;
                dynamicList.add(message);
                // if (dynamicList.size() >= 11)
                // break;
                // }
            }
        }

        // no read sys_call

        if (isShowCallLogTip) {
            if (QuickGestureManager.getInstance(mContext).getQuickNoReadCall() != null
                    && QuickGestureManager.getInstance(mContext).getQuickNoReadCall().size() > 0) {
                ContactCallLog baseInfo = QuickGestureManager.getInstance(mContext)
                        .getQuickNoReadCall()
                        .get(0);
                baseInfo.gesturePosition = -1000;
                baseInfo.icon = mContext.getResources().getDrawable(
                        R.drawable.gesture_call);
                baseInfo.label = mContext.getResources().getString(
                        R.string.privacy_contact_calllog);
                baseInfo.isShowReadTip = true;
                dynamicList.add(baseInfo);
            }
        }
        // privacy contact
        if (isShowPrivacyContactTip) {
            if (isShowPrivacyCallLog || isShowPrivacyMsm) {
                if (dynamicList.size() <= 11) {
                    QuickGestureContactTipInfo item = new QuickGestureContactTipInfo();
                    item.gesturePosition = -1000;
                    item.icon = mContext.getResources().getDrawable(
                            R.drawable.gesture_system);
                    item.label = mContext.getResources().getString(
                            R.string.pg_appmanager_quick_gesture_privacy_contact_tip_lable);
                    item.isShowReadTip = true;
                    dynamicList.add(item);
                }
            }
        }

        ArrayList<AppItemInfo> datas = engine.getLaunchTimeSortedApps();
        Drawable icon;
        AppItemInfo appInfo;
        String pkg;
        List<String> pkgs = new ArrayList<String>();
        if (Build.VERSION.SDK_INT <= 19 || datas.size() < 0) {
            ActivityManager am = (ActivityManager) mContext
                    .getSystemService(Context.ACTIVITY_SERVICE);
            List<RecentTaskInfo> recentTasks = am.getRecentTasks(20,
                    ActivityManager.RECENT_WITH_EXCLUDED);
            for (RecentTaskInfo recentTaskInfo : recentTasks) {
                if (dynamicList.size() > 11)
                    break;
                pkg = recentTaskInfo.baseIntent.getComponent().getPackageName();
                if (!pkgs.contains(pkg)) {
                    pkgs.add(pkg);
                    icon = engine.getAppIcon(pkg);
                    if (icon != null) {
                        appInfo = new AppItemInfo();
                        appInfo.packageName = pkg;
                        appInfo.activityName = engine.getActivityName(pkg);
                        appInfo.icon = icon;
                        appInfo.label = engine.getAppName(pkg);
                        dynamicList.add(appInfo);
                    }
                }
            }
        } else {
            for (AppItemInfo appItemInfo : datas) {
                if (dynamicList.size() > 11)
                    break;
                if (!pkgs.contains(appItemInfo.packageName)) {
                    pkgs.add(appItemInfo.packageName);
                    icon = appItemInfo.icon;
                    if (icon != null) {
                        appInfo = new AppItemInfo();
                        appInfo.packageName = appItemInfo.packageName;
                        appInfo.activityName = appItemInfo.activityName;
                        appInfo.icon = icon;
                        appInfo.label = appItemInfo.label;
                        dynamicList.add(appInfo);
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();
        return dynamicList;
    }

    private void preloadColorIcon() {
        mColorBgIconIds = new int[11];
        mColorBgIconIds[0] = R.drawable.switch_orange;
        mColorBgIconIds[1] = R.drawable.switch_green;
        mColorBgIconIds[2] = R.drawable.seitch_purple;
        mColorBgIconIds[3] = R.drawable.switch_red;
        mColorBgIconIds[4] = R.drawable.switch_blue;
        mColorBgIconIds[5] = R.drawable.switch_blue_2;
        mColorBgIconIds[6] = R.drawable.switch_blue_3;
        mColorBgIconIds[7] = R.drawable.switch_green_2;
        mColorBgIconIds[8] = R.drawable.switch_orange_2;
        mColorBgIconIds[9] = R.drawable.switch_purple_2;
        mColorBgIconIds[10] = R.drawable.switch_red_2;
    }

    public void stopFloatWindow() {
        LockManager.getInstatnce().stopFloatWindowService();
    }

    public void startFloatWindow() {
        LockManager.getInstatnce().startFloatWindowService();
    }

    public List<BaseInfo> getMostUsedList() {
        if (mSpSwitch.getQuickGestureCommonAppDialogCheckboxValue()) {
            return loadRecorderAppInfo();
        } else {
            return loadCommonAppInfo();
        }
    }

    // Recorder App
    public List<BaseInfo> loadRecorderAppInfo() {

        // load前看看已经选定的应用有啥，并且排列在最前面
        List<BaseInfo> mHaveList = loadCommonAppInfo();
        LeoLog.d("testSp", "mHaveList.size : " + mHaveList.size());

        List<BaseInfo> newresault = new ArrayList<BaseInfo>();
        List<BaseInfo> resault = new ArrayList<BaseInfo>();
        ArrayList<AppLauncherRecorder> recorderApp = LockManager.getInstatnce().mAppLaunchRecorders;

        // LaunchCount
        Collections.sort(recorderApp, new LaunchCount());

        AppLoadEngine engine = AppLoadEngine.getInstance(mContext);
        if (recorderApp != null && (recorderApp.size() > 1 || mHaveList.size() > 0)) {
            // LeoLog.d("testSp", "recorderApp.size() : " + recorderApp.size());
            Iterator<AppLauncherRecorder> recorder = recorderApp.iterator();
            int i = 0;
            AppItemInfo info;
            QuickGsturebAppInfo temp = null;
            while (recorder.hasNext()) {
                AppLauncherRecorder recorderAppInfo = recorder.next();
                if (recorderAppInfo.launchCount > 0) {
                    info = engine.getAppInfo(recorderAppInfo.pkg);
                    // LeoLog.d("testSp", "recorderApp.pk : " +
                    // recorderAppInfo.pkg +
                    // " ; mInfo.launchCount : " + recorderAppInfo.launchCount);
                    if (info == null)
                        continue;
                    if (i >= 11) {
                        break;
                    } else {
                        temp = new QuickGsturebAppInfo();
                        temp.packageName = info.packageName;
                        temp.activityName = info.activityName;
                        temp.label = info.label;
                        temp.icon = info.icon;
                        temp.gesturePosition = i;
                        resault.add(temp);
                        i++;
                    }
                }
            }
        } else {
            ArrayList<AppItemInfo> datas = engine.getLaunchTimeSortedApps();
            if (datas.size() > 0) {
                for (AppItemInfo appItemInfo : datas) {
                    if (resault.size() > 11)
                        break;
                    else
                        resault.add(appItemInfo);
                }
            } else {
                ActivityManager am = (ActivityManager) mContext
                        .getSystemService(Context.ACTIVITY_SERVICE);
                List<RecentTaskInfo> recentTasks = am.getRecentTasks(50,
                        ActivityManager.RECENT_WITH_EXCLUDED);
                String pkg;
                AppItemInfo appInfo;
                List<String> pkgs = new ArrayList<String>();
                for (RecentTaskInfo recentTaskInfo : recentTasks) {
                    if (resault.size() > 10)
                        break;
                    pkg = recentTaskInfo.baseIntent.getComponent().getPackageName();
                    if (!pkgs.contains(pkg)) {
                        pkgs.add(pkg);
                        appInfo = engine.getAppInfo(pkg);
                        if (appInfo != null) {
                            resault.add(appInfo);
                        }
                    }
                }
            }

            List<BaseInfo> mFirstList = changeList(resault);
            String mListString = QuickSwitchManager.getInstance(mContext).listToPackString(
                    mFirstList,
                    mFirstList.size(), NORMALINFO);
            mSpSwitch.setCommonAppPackageName(mListString);
            return mFirstList;
        }

        // LeoLog.d("testSp", "resault.size() : " + resault.size());
        // 删除相同应用
        if (resault.size() > 0 && mHaveList.size() > 0) {
            for (int i = 0; i < mHaveList.size(); i++) {
                QuickGsturebAppInfo mInfo = (QuickGsturebAppInfo) mHaveList.get(i);
                for (int j = 0; j < resault.size(); j++) {
                    QuickGsturebAppInfo mInfoCount = (QuickGsturebAppInfo) resault.get(j);
                    if (mInfo.packageName.equals(mInfoCount.packageName)) {
                        resault.remove(j);
                        break;
                    }
                }
            }
        }

        if (mHaveList.size() > 0) {
            int[] mPositions = new int[mHaveList.size()];
            for (int i = 0; i < mHaveList.size(); i++) {
                BaseInfo mInfo = mHaveList.get(i);
                mPositions[i] = mInfo.gesturePosition;
            }
            sortList(mPositions);

            int j = 0;
            int k = 0;
            int mSize = mHaveList.size() + resault.size() > 11 ? 11 : mHaveList.size()
                    + resault.size();
            // LeoLog.d("testSp", "mSize : " + mSize);
            if (resault.size() > 0) {
                for (int i = 0; i < 11; i++) {
                    // LeoLog.d("testSp", "i  is : " + i);
                    QuickGsturebAppInfo mInfo = (QuickGsturebAppInfo) mHaveList.get(k);
                    int m = j;
                    if (resault.size() <= j) {
                        m = resault.size() - 1;
                    }
                    QuickGsturebAppInfo mInfoCount = (QuickGsturebAppInfo) resault.get(m);
                    boolean isGetPosition = isGetPosition(i, mPositions);
                    if (isGetPosition) {
                        // LeoLog.d("testSp", "mInfo.gesturePosition : " +
                        // mInfo.gesturePosition);
                        newresault.add(mInfo);
                        if (k < mHaveList.size() - 1) {
                            k++;
                        }
                    } else {
                        if (resault.size() > j && i < mSize) {
                            // LeoLog.d("testSp",
                            // "mInfoCount.gesturePosition : "
                            // + mInfoCount.gesturePosition);
                            mInfoCount.gesturePosition = i;
                            newresault.add(mInfoCount);
                            j++;
                        }
                    }
                }
                // LeoLog.d("testSp", "newresault.size : " + newresault.size());
                resault.clear();
                resault = newresault;
            } else {
                resault = mHaveList;
            }
        }

        // 记录点击排行
        List<String> mBackListRank = new ArrayList<String>();
        if (recorderApp.size() > 0) {
            for (int i = 0; i < recorderApp.size(); i++) {
                AppLauncherRecorder recorderAppInfo = recorderApp.get(i);
                for (int j = 0; j < resault.size(); j++) {
                    QuickGsturebAppInfo mInfo = (QuickGsturebAppInfo) resault.get(j);
                    if (recorderAppInfo.pkg.equals(mInfo.packageName)) {
                        // LeoLog.d("testSp", "mBackListRank.pk : " +
                        // mInfo.packageName);
                        mBackListRank.add(mInfo.packageName);
                    }
                }
            }
        }

        // 换位
        makeResaultList(resault, mBackListRank);

        String mListString = QuickSwitchManager.getInstance(mContext).listToPackString(
                resault,
                resault.size(), NORMALINFO);
        mSpSwitch.setCommonAppPackageName(mListString);
        return resault;
    }

    private void makeResaultList(List<BaseInfo> resault, List<String> mBackListRank) {
        for (int i = 0; i < mBackListRank.size(); i++) {
            String mRankPck = mBackListRank.get(i);
            int mRankPosition = i;
            int AgoPosition = 0;
            for (int j = 0; j < resault.size(); j++) {
                QuickGsturebAppInfo mAgoInfo = (QuickGsturebAppInfo) resault.get(j);
                if (mAgoInfo.packageName.equals(mRankPck)) {
                    AgoPosition = mAgoInfo.gesturePosition;
                    QuickGsturebAppInfo aInfo = getInfoFromPosition(resault, mRankPosition);
                    if (mRankPosition != AgoPosition) {
                        mAgoInfo.gesturePosition = mRankPosition;
                        aInfo.gesturePosition = AgoPosition;
                    }
                }
            }
        }
    }

    private QuickGsturebAppInfo getInfoFromPosition(List<BaseInfo> resault, int mRankPosition) {
        QuickGsturebAppInfo returnInfo = new QuickGsturebAppInfo();
        for (int j = 0; j < resault.size(); j++) {
            QuickGsturebAppInfo mInInfo = (QuickGsturebAppInfo) resault.get(j);
            if (mRankPosition == mInInfo.gesturePosition) {
                returnInfo = mInInfo;
            }
        }
        return returnInfo;
    }

    public static class LaunchCount implements Comparator<AppLauncherRecorder> {
        @Override
        public int compare(AppLauncherRecorder lhs, AppLauncherRecorder rhs) {
            if (rhs.launchCount > lhs.launchCount) {
                return 1;
            } else if (rhs.launchCount == lhs.launchCount) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    private List<BaseInfo> changeList(List<BaseInfo> resault) {
        List<BaseInfo> mBaseList = new ArrayList<BaseInfo>();
        QuickGsturebAppInfo temp;
        for (int i = 0; i < resault.size(); i++) {
            AppItemInfo appInfo = (AppItemInfo) resault.get(i);
            temp = new QuickGsturebAppInfo();
            temp.packageName = appInfo.packageName;
            temp.activityName = appInfo.activityName;
            temp.label = appInfo.label;
            temp.icon = appInfo.icon;
            temp.gesturePosition = i;
            mBaseList.add(temp);
        }
        return mBaseList;
    }

    private boolean isGetPosition(int gesturePosition, int[] mPositions) {
        for (int i = 0; i < mPositions.length; i++) {
            if (gesturePosition == mPositions[i]) {
                return true;
            }
        }
        return false;
    }

    private void sortList(int[] mPositions) {
        for (int i = 0; i < mPositions.length; i++) {
            for (int j = i + 1; j < mPositions.length; j++) {
                if (mPositions[i] > mPositions[j]) {
                    int temp = mPositions[i];
                    mPositions[i] = mPositions[j];
                    mPositions[j] = temp;
                }
            }
        }
    }

    // Customize common app
    public List<BaseInfo> loadCommonAppInfo() {
        long startTime = System.currentTimeMillis();
        List<BaseInfo> resault = new ArrayList<BaseInfo>();
        List<QuickGsturebAppInfo> packageNames = new ArrayList<QuickGsturebAppInfo>();
        AppLoadEngine engin = AppLoadEngine.getInstance(mContext);
        String commonAppString = mSpSwitch.getCommonAppPackageName();
        if (!TextUtils.isEmpty(commonAppString)) {
            String[] names = commonAppString.split(";");
            QuickGsturebAppInfo temp = null;
            int sIndex = -1;
            for (String recoder : names) {
                sIndex = recoder.indexOf(':');
                if (sIndex != -1) {
                    temp = new QuickGsturebAppInfo();
                    temp.packageName = recoder.substring(0, sIndex);
                    temp.gesturePosition = Integer.parseInt(recoder.substring(sIndex + 1));
                    packageNames.add(temp);
                }
            }

            AppItemInfo info;
            for (QuickGsturebAppInfo appItemInfo : packageNames) {
                if (resault.size() >= 11) {
                    break;
                }
                info = engin.getAppInfo(appItemInfo.packageName);
                if (info != null) {
                    appItemInfo.packageName = info.packageName;
                    appItemInfo.activityName = info.activityName;
                    appItemInfo.label = info.label;
                    appItemInfo.icon = info.icon;
                    resault.add(appItemInfo);
                }
            }
        }
        return resault;
    }

    public List<BaseInfo> getSwitcherList() {
        return QuickSwitchManager.getInstance(mContext).getSwitchList(11);
    }

    public void updateSwitcherData(List<BaseInfo> infos) {
        String saveToSp = QuickSwitchManager.getInstance(mContext)
                .listToString(infos, infos.size(), false);
        LeoLog.d("updateSwitcherData", "saveToSp:" + saveToSp);
        mSpSwitch.setSwitchList(saveToSp);
        mSpSwitch.setSwitchListSize(infos.size());
        QuickSwitchManager.getInstance(mContext).onDataChange(saveToSp);
    }

    public void onRunningPkgChanged(String pkg) {

    }

    public void checkEventItemRemoved(BaseInfo info) {
        if (info instanceof MessageBean) {
            MessageBean bean = (MessageBean) info;
            if (getQuiQuickNoReadMessage() != null && getQuiQuickNoReadMessage().size() > 0) {
                removeQuickNoReadMessage(bean);
            }
        } else if (info instanceof ContactCallLog) {
            ContactCallLog callLog = (ContactCallLog) info;
            if (getQuickNoReadCall() != null && getQuickNoReadCall().size() > 0) {
                removeQuickNoReadCall(callLog);
            }
        } else if (info instanceof QuickGestureContactTipInfo) {
            if (isShowPrivacyCallLog) {
                isShowPrivacyCallLog = false;
                AppMasterPreference.getInstance(mContext).setQuickGestureCallLogTip(false);
            }
            if (isShowPrivacyMsm) {
                isShowPrivacyMsm = false;
                AppMasterPreference.getInstance(mContext).setQuickGestureMsmTip(false);
            }
        }
        if ((getQuiQuickNoReadMessage() == null || getQuiQuickNoReadMessage().size() <= 0)
                && (getQuickNoReadCall() == null || getQuickNoReadCall().size() <= 0)
                && !isShowPrivacyCallLog
                && !isShowPrivacyMsm) {
            QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage = false;
        }
    }

    public List<String> getFreeDisturbAppName() {
        List<String> packageNames = new ArrayList<String>();
        String packageName = AppMasterPreference.getInstance(mContext)
                .getFreeDisturbAppPackageName();
        if (!AppMasterPreference.PREF_QUICK_GESTURE_FREE_DISTURB_APP_PACKAGE_NAME
                .equals(packageName)) {
            String[] names = packageName.split(";");
            packageNames = Arrays.asList(names);
        }
        return packageNames;
    }

    // 获取未读短信数量
    public int getNoReadMsg() {
        int noReadMsgCount = 0;
        Cursor c = null;
        try {
            Uri uri = Uri.parse("content://sms");
            c = mContext.getContentResolver().query(uri, null,
                    "read=0 AND type=1", null, null);
            if (c != null) {
                noReadMsgCount = c.getCount();
                c.close();
                c = null;
            }
            uri = Uri.parse("content://mms");
            c = mContext.getContentResolver().query(uri, null,
                    "read=0 AND m_type=132", null, null);

            if (c != null) {
                noReadMsgCount += c.getCount();
                c.close();
                c = null;
            }
        } catch (Exception e) {
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return noReadMsgCount;
    }

    // 获取未读通话
    public int getMissedCallCount() {
        int missedCallCount = 0;
        Cursor c = null;
        try {
            String selection = Calls.TYPE + "=? and " + Calls.NEW + "=?";
            String[] selectionArgs = new String[] {
                    String.valueOf(Calls.MISSED_TYPE), String.valueOf(1)
            };

            c = mContext.getContentResolver().query(Calls.CONTENT_URI,
                    new String[] {
                            Calls.NUMBER, Calls.TYPE, Calls.NEW
                    },
                    selection, selectionArgs, Calls.DEFAULT_SORT_ORDER);

            if (c != null) {
                missedCallCount = c.getCount();
            }
        } catch (Exception e) {
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return missedCallCount;
    }

    public Drawable applyEmptyIcon() {
        if (mEmptyIcon == null) {
            mEmptyIcon = mContext.getResources().getDrawable(R.drawable.gesture_empty);
        }
        return mEmptyIcon;
    }

    /**
     * Common App Dialog
     * 
     * @param context
     */
    public void showCommonAppDialog(final Context context) {
        isFromDialog = true;
        commonApp = new QuickGestureFilterAppDialog(
                context.getApplicationContext(), 3);
        final AppMasterPreference pref = AppMasterPreference.getInstance(context);
        commonApp.setIsShowCheckBox(true);
        commonApp.setCheckBoxText(R.string.quick_gesture_change_common_app_dialog_checkbox_text);
        // 设置是否选择习惯
        commonApp.setCheckValue(pref.getQuickGestureCommonAppDialogCheckboxValue());
        commonApp.setTitle(R.string.quick_gesture_change_common_app_dialog_title);
        commonApp.setRightBt(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 避免线程内跑的比线程外快，避免数据不同步
                // 确认按钮
                // new Thread(new Runnable() {
                // @Override
                // public void run() {
                // 添加的应用包名
                List<BaseInfo> addCommonApp = commonApp.getAddFreePackageName();
                // 移除的应用包名
                List<BaseInfo> removeCommonApp = commonApp.getRemoveFreePackageName();
                List<BaseInfo> mDefineList = new ArrayList<BaseInfo>();

                // 是否选择使用习惯自动填充
                boolean flag = commonApp.getCheckValue();
                // if (!flag) {
                List<BaseInfo> comList = loadCommonAppInfo();
                List<BaseInfo> tempList = new ArrayList<BaseInfo>();

                if (removeCommonApp != null && removeCommonApp.size() > 0) {
                    boolean isHasSameName = false;
                    for (BaseInfo info : comList) {
                        for (BaseInfo baseInfo : removeCommonApp) {
                            if (baseInfo.label.equals(info.label)) {
                                isHasSameName = true;
                            }
                        }
                        if (!isHasSameName) {
                            mDefineList.add(info);
                        }
                        isHasSameName = false;
                    }
                }

                if (removeCommonApp != null && addCommonApp.size() > 0) {
                    if (removeCommonApp.size() == 0) {
                        mDefineList = comList;
                    }

                    // 记录现有的Icon位置
                    LeoLog.d("QuickGestureManager", "有货要加");
                    List<BaseInfo> mfixPostionList = new ArrayList<BaseInfo>();
                    List<Integer> sPosition = new ArrayList<Integer>();
                    for (int i = 0; i < mDefineList.size(); i++) {
                        sPosition.add(mDefineList.get(i).gesturePosition);
                        LeoLog.d("QuickGestureManager",
                                "已有货的位置 :" + mDefineList.get(i).gesturePosition);
                    }

                    int k = 0;
                    for (int i = 0; i < 11; i++) {
                        boolean isHasIcon = false;
                        for (int j = 0; j < mDefineList.size(); j++) {
                            if (i == mDefineList.get(j).gesturePosition) {
                                isHasIcon = true;
                            }
                        }
                        if (!isHasIcon && addCommonApp.size() > k) {
                            LeoLog.d("QuickGestureManager", i + "号位没人坐，收藏起来");
                            BaseInfo mInfo = addCommonApp.get(k);
                            mInfo.gesturePosition = i;
                            mfixPostionList.add(mInfo);
                            k++;
                        }
                    }

                    LeoLog.d("QuickGestureManager",
                            "收藏完毕，霸占了" + mfixPostionList.size() + "个位置");
                    for (int i = 0; i < mfixPostionList.size(); i++) {
                        BaseInfo mInfo = mfixPostionList.get(i);
                        mDefineList.add(mInfo);
                        LeoLog.d("QuickGestureManager", "霸占了 "
                                + mInfo.gesturePosition + "号位");
                    }
                }

                if (addCommonApp.size() == 0 && removeCommonApp.size() == 0) {
                    mDefineList = comList;
                }

                String mChangeList = QuickSwitchManager.getInstance(mContext)
                        .listToPackString(mDefineList, mDefineList.size(), NORMALINFO);
                LeoLog.d("QuickGestureManager", "mChangeList ： " + mChangeList);
                pref.setCommonAppPackageName(mChangeList);

                if (pref.getQuickGestureCommonAppDialogCheckboxValue() != flag) {
                    pref.setQuickGestureCommonAppDialogCheckboxValue(flag);
                    if (flag) {
                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_tab",
                                "common_auto");
                    }
                }
                // }

                // }).start();
                commonApp.dismiss();
                Intent intent = new Intent(AppMasterApplication.getInstance(),
                        QuickGesturePopupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (onTuchGestureFlag == -1 || onTuchGestureFlag == -2) {
                    intent.putExtra("show_orientation", 0);
                } else if (onTuchGestureFlag == 1 || onTuchGestureFlag == 2) {
                    intent.putExtra("show_orientation", 2);
                }
                isClickSure = true;
                AppMasterApplication.getInstance().startActivity(intent);
            }
        });
        commonApp.setLeftBt(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 取消按钮
                commonApp.dismiss();
                Intent intent = new Intent(AppMasterApplication.getInstance(),
                        QuickGesturePopupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (onTuchGestureFlag == -1 || onTuchGestureFlag == -2) {
                    intent.putExtra("show_orientation", 0);
                } else if (onTuchGestureFlag == 1 || onTuchGestureFlag == 2) {
                    intent.putExtra("show_orientation", 2);
                }
                AppMasterApplication.getInstance().startActivity(intent);
            }
        });

        // commonApp.getWindow().setType(
        // WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        // commonApp.getWindow().setType(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        commonApp.getWindow().setType(
                WindowManager.LayoutParams.TYPE_TOAST
                        | WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        commonApp.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isDialogShowing = false;
                Log.i("null", "commonApp dialog " + isDialogShowing);
            }
        });
        isDialogShowing = true;
        commonApp.show();
    }

    /**
     * Quick Switch Dialog
     * 
     * @param mSwitchList
     * @param context
     * @param activity
     */
    public void showQuickSwitchDialog(final Context context) {
        isFromDialog = true;
        quickSwitch = new QuickGestureFilterAppDialog(
                context.getApplicationContext(), 2);
        quickSwitch.setTitle(R.string.pg_appmanager_quick_switch_dialog_title);
        quickSwitch.setRightBt(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 线程外比线程内跑的快，数据不同步
                // 确认按钮
                // AppMasterApplication.getInstance().postInAppThreadPool(new
                // Runnable() {
                // @Override
                // public void run() {
                // 添加的应用包名
                boolean addSwitch = false;
                boolean removeSwitch = false;
                List<BaseInfo> addQuickSwitch = quickSwitch.getAddFreePackageName();

                for (int i = 0; i < addQuickSwitch.size(); i++) {
                    BaseInfo mInfo = addQuickSwitch.get(i);
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_switch",
                            "add_" + mInfo.swtichIdentiName);
                    LeoLog.d("testSp", "mInfo.swtichIdentiName : "
                            + mInfo.swtichIdentiName);
                }

                // 移除的应用包名
                List<BaseInfo> removeQuickSwitch = quickSwitch
                        .getRemoveFreePackageName();
                // 正确添加或删除的list
                String mListString = mSpSwitch.getSwitchList();
                List<BaseInfo> rightQuickSwitch =
                        QuickSwitchManager.getInstance(mContext).StringToList(
                                mListString);
                List<BaseInfo> mDefineList = new ArrayList<BaseInfo>();
                LeoLog.d("QuickGestureManager", "rightQuickSwitch.size is : "
                        + rightQuickSwitch.size());
                if (addQuickSwitch != null && addQuickSwitch.size() > 0) {
                    addSwitch = true;
                }
                if (removeQuickSwitch != null && removeQuickSwitch.size() > 0) {
                    for (int i = 0; i < rightQuickSwitch.size(); i++) {
                        boolean isHasSameName = false;
                        BaseInfo nInfo = rightQuickSwitch.get(i);
                        String rightName = nInfo.label;
                        for (int j = 0; j < removeQuickSwitch.size(); j++) {
                            BaseInfo mInfo = removeQuickSwitch.get(j);
                            String removeName = mInfo.label;
                            if (rightName.equals(removeName)) {
                                isHasSameName = true;
                            }
                        }
                        if (!isHasSameName) {
                            mDefineList.add(nInfo);
                        }
                    }
                    removeSwitch = true;
                }

                if (addSwitch) {
                    if (!removeSwitch) {
                        mDefineList = rightQuickSwitch;
                    }
                    // 记录现有的Icon位置
                    LeoLog.d("QuickGestureManager", "有货要加");
                    List<BaseInfo> mfixPostionList = new ArrayList<BaseInfo>();
                    List<Integer> sPosition = new ArrayList<Integer>();
                    for (int i = 0; i < mDefineList.size(); i++) {
                        sPosition.add(mDefineList.get(i).gesturePosition);
                        LeoLog.d("QuickGestureManager",
                                "已有货的位置 :" + mDefineList.get(i).gesturePosition);
                    }

                    int k = 0;
                    for (int i = 0; i < 11; i++) {
                        boolean isHasIcon = false;
                        for (int j = 0; j < mDefineList.size(); j++) {
                            if (i == mDefineList.get(j).gesturePosition) {
                                isHasIcon = true;
                            }
                        }

                        if (!isHasIcon && addQuickSwitch.size() > k) {
                            LeoLog.d("QuickGestureManager", i + "号位没人坐，收藏起来");
                            BaseInfo mInfo = addQuickSwitch.get(k);
                            mInfo.gesturePosition = i;
                            mfixPostionList.add(mInfo);
                            k++;
                        }
                    }

                    LeoLog.d("QuickGestureManager",
                            "收藏完毕，霸占了" + mfixPostionList.size() + "个位置");
                    for (int i = 0; i < mfixPostionList.size(); i++) {
                        BaseInfo mInfo = mfixPostionList.get(i);
                        mDefineList.add(mInfo);
                        LeoLog.d("QuickGestureManager", "霸占了 "
                                + mInfo.gesturePosition + "号位");
                    }
                }

                if (!addSwitch && !removeSwitch) {
                    mDefineList = rightQuickSwitch;
                }

                String mChangeList = QuickSwitchManager.getInstance(mContext)
                        .listToString(mDefineList, mDefineList.size(), true);
                mSpSwitch.setSwitchList(mChangeList);
                // }
                // });
                quickSwitch.dismiss();
                Intent intent = new Intent(AppMasterApplication.getInstance(),
                        QuickGesturePopupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (onTuchGestureFlag == -1 || onTuchGestureFlag == -2) {
                    intent.putExtra("show_orientation", 0);
                } else if (onTuchGestureFlag == 1 || onTuchGestureFlag == 2) {
                    intent.putExtra("show_orientation", 2);
                }
                isClickSure = true;
                AppMasterApplication.getInstance().startActivity(intent);
            }
        });
        quickSwitch.setLeftBt(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // cancel button
                quickSwitch.dismiss();
                Intent intent = new Intent(AppMasterApplication.getInstance(),
                        QuickGesturePopupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (onTuchGestureFlag == -1 || onTuchGestureFlag == -2) {
                    intent.putExtra("show_orientation", 0);
                } else if (onTuchGestureFlag == 1 || onTuchGestureFlag == 2) {
                    intent.putExtra("show_orientation", 2);
                }
                AppMasterApplication.getInstance().startActivity(intent);
            }
        });
        // quickSwitch.getWindow().setType(
        // WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        // quickSwitch.getWindow().setType(
        // WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        quickSwitch.getWindow().setType(
                WindowManager.LayoutParams.TYPE_TOAST
                        | WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        quickSwitch.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isDialogShowing = false;
                Log.i("null", "quickSwitch dialog " + isDialogShowing);
            }
        });
        isDialogShowing = true;
        quickSwitch.show();
    }

    public void makeDialogDimiss() {
        if (quickSwitch != null) {
            quickSwitch.dismiss();
            quickSwitch = null;
        }
        if (commonApp != null) {
            commonApp.dismiss();
            commonApp = null;
        }
    }

    public class AppLauncherRecorder implements Comparable<AppLauncherRecorder> {
        public String pkg;
        public int launchCount;

        @Override
        public int compareTo(AppLauncherRecorder another) {
            if (launchCount > another.launchCount) {
                return 1;
            } else if (launchCount == another.launchCount) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    public void resetSlidAreaSize() {
        mSlidAreaSize = mSpSwitch.getQuickGestureDialogSeekBarValue();
    }

    public void setSlidAreaSize(int value) {
        mSlidAreaSize = value;
    }

    public void onBusinessItemClicked(BusinessItemInfo info) {
        AppBusinessManager.getInstance(mContext).onItemClicked(info);
        addDeleteBusinessItem(info.packageName);
        AppMasterPreference.getInstance(mContext).setLastBusinessRedTipShow(true);
    }

    public void sendPermissionOpenNotification(final Context context) {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                NotificationManager notificationManager = (NotificationManager)
                        context
                                .getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notification = new Notification();
                // LockManager.getInstatnce().addFilterLockPackage("com.leo.appmaster",
                // false);
                Intent intentPending = new Intent(context,
                        QuickGestureProxyActivity.class);
                intentPending.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
                        StatusBarEventService.EVENT_BUSINESS_QUICK_GUESTURE);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intentPending,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                notification.icon = R.drawable.ic_launcher_notification;
                notification.tickerText = context
                        .getString(R.string.permission_open_tip_notification_title);
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                String title = context.getString(R.string.permission_open_tip_notification_title);
                String content = context
                        .getString(R.string.permission_open_tip_notification_content);
                notification.setLatestEventInfo(context, title, content, contentIntent);
                NotificationUtil.setBigIcon(notification,
                        R.drawable.ic_launcher_notification_big);
                notification.when = System.currentTimeMillis();
                notificationManager.notify(20150603, notification);
                AppMasterPreference.getInstance(context).setQuickPermissonOpenFirstNotificatioin(
                        true);
            }
        }, 5000);

    }

    public boolean checkBusinessRedTip() {
        if (AppMasterPreference.getInstance(mContext).getLastBusinessRedTipShow()) {
            return false;
        }
        Vector<BusinessItemInfo> mBusinessList = AppBusinessManager.getInstance(mContext)
                .getBusinessData();
        if (mBusinessList != null) {
            boolean show = false;
            AppLoadEngine engine = AppLoadEngine.getInstance(mContext);
            for (BusinessItemInfo info : mBusinessList) {
                if (engine.getAppInfo(info.packageName) != null
                        || mDeletedBusinessItems.contains(info.packageName)) {
                    continue;
                } else {
                    show = true;
                    break;
                }
            }
            return show;
        }
        return false;
    }

    /**
     * create shortcut of quick guesture
     */
    public void createShortCut() {
        SharedPreferences prefernece = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        boolean quickGestureFlag = prefernece.getBoolean("shortcut_quickGesture", false);
        if (!quickGestureFlag) {
            Intent quickGestureShortIntent = new Intent(mContext,
                    QuickGestureProxyActivity.class);
            quickGestureShortIntent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
                    StatusBarEventService.EVENT_BUSINESS_QUICK_GUESTURE);
            Intent quickGestureShortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            ShortcutIconResource quickGestureIconRes = Intent.ShortcutIconResource.fromContext(
                    mContext, R.drawable.gesture_desktopo_icon);
            quickGestureShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, mContext.getResources()
                    .getString(R.string.pg_appmanager_quick_gesture_name));
            quickGestureShortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, quickGestureIconRes);
            quickGestureShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, quickGestureShortIntent);
            quickGestureShortcut.putExtra("duplicate", false);
            quickGestureShortcut.putExtra("from_shortcut", true);
            mContext.sendBroadcast(quickGestureShortcut);
            prefernece.edit().putBoolean("shortcut_quickGesture", true).apply();
        }
    }

    public synchronized List<MessageBean> getQuiQuickNoReadMessage() {
        return mMessages;
    }

    public void addQuickNoReadMessage(List<MessageBean> messages) {
        getQuiQuickNoReadMessage();
        mMessages = messages;
    }

    public void removeQuickNoReadMessage(MessageBean messageBean) {
        getQuiQuickNoReadMessage();
        if (mMessages != null) {
            mMessages.remove(messageBean);
        }
    }

    public void clearQuickNoReadMessage() {
        getQuiQuickNoReadMessage();
        if (mMessages != null) {
            mMessages.clear();
        }
    }

    public synchronized List<ContactCallLog> getQuickNoReadCall() {
        return mCallLogs;
    }

    public void addQuickNoReadCall(List<ContactCallLog> call) {
        getQuickNoReadCall();
        mCallLogs = call;
    }

    public void removeQuickNoReadCall(ContactCallLog call) {
        getQuickNoReadCall();
        if (mCallLogs != null) {
            mCallLogs.remove(call);
        }
    }

    public void clearQuickNoReadCall() {
        getQuickNoReadCall();
        if (mCallLogs != null) {
            mCallLogs.clear();
        }
    }

    /* 隐私联系人有未读发送广播到iswipe */
    public void privacyContactSendReceiverToSwipe(final String flag, int actiontype) {
        Intent privacyIntent = null;
        if (!Utilities.isEmpty(flag)) {
            if (PRIVACY_MSM.equals(flag)) {
                LeoLog.i(TAG, "隐私联系人有未读短信");
                privacyIntent = getPrivacyMsmIntent();
            } else if (PRIVACY_CALL.equals(flag)) {
                LeoLog.i(TAG, "隐私联系人有未读通话");
                privacyIntent = getPrivacyCallIntent();
            }
        }
        Intent intent = new Intent(RECEIVER_TO_SWIPE_ACTION);
        if (actiontype == 0) {
            /* 通知有未读 */
            intent.setAction(RECEIVER_TO_SWIPE_ACTION);
            Bundle bundle = new Bundle();
            bundle.putParcelable(PRIVACYCONTACT_TO_IWIPE_KEY, privacyIntent);
            intent.putExtras(bundle);
        } else if (actiontype == 1) {
            /* 通知取消未读 */
            intent.setAction(RECEIVER_TO_SWIPE_ACTION_CANCEL_PRIVACY_TIP);
        }
        // intent.putExtra(PRIVACYCONTACT_TO_IWIPE_KEY, flag);
        try {
            mContext.sendBroadcast(intent, SEND_RECEIVER_TO_SWIPE_PERMISSION);
            // mContext.sendBroadcast(intent);
        } catch (Exception e) {
            LeoLog.i(TAG, "隐私联系人广播发送失败！！");
        }
    }

    private Intent getPrivacyMsmIntent() {
        Intent privacyMsmIntent = new Intent(mContext, PrivacyContactActivity.class);
        privacyMsmIntent.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT,
                PrivacyContactUtils.TO_PRIVACY_MESSAGE_FLAG);
        return privacyMsmIntent;
    }

    private Intent getPrivacyCallIntent() {
        Intent privacyCallIntent = new Intent(mContext,
                PrivacyContactActivity.class);
        privacyCallIntent.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT,
                PrivacyContactUtils.TO_PRIVACY_CALL_FLAG);
        return privacyCallIntent;
    }

    /* 对于iswipe没有隐私未读处理 */
    public void cancelPrivacyTipFromPrivacyCall() {
        AppMasterPreference amp = AppMasterPreference.getInstance(mContext);
        if (amp.getMessageNoReadCount() <= 0) {
            privacyContactSendReceiverToSwipe(null, 1);
        }
    }

    public void cancelPrivacyTipFromPrivacyMsm() {
        AppMasterPreference amp = AppMasterPreference.getInstance(mContext);
        if (amp.getCallLogNoReadCount() <= 0) {
            privacyContactSendReceiverToSwipe(null, 1);
        }
    }

    public void sendFirstUseLockModeToISwipe() {
        Intent intent = new Intent(ACTION_FIRST_USE_LOCK_MODE);

        AppMasterPreference pref = AppMasterPreference.getInstance(mContext);
        if (pref.getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
            List<LockMode> list = LockManager.getInstatnce().getLockMode();
            ArrayList<LockMode> arrayList = new ArrayList<LockMode>();
            arrayList.addAll(list);
            intent.putParcelableArrayListExtra("lock_mode_list", arrayList);
        }
        try {
            mContext.sendBroadcast(intent, SEND_RECEIVER_TO_SWIPE_PERMISSION);
        } catch (Exception e) {
            LeoLog.e(TAG, "send first use lock mode failed.", e);
        }
    }

    public void sendLockModeChangeToISwipe() {
        Intent intent = new Intent(ACTION_LOCK_MODE_CHANGE);
        
        AppMasterPreference pref = AppMasterPreference.getInstance(mContext);
        if (pref.getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
            List<LockMode> list = LockManager.getInstatnce().getLockMode();
            ArrayList<LockMode> arrayList = new ArrayList<LockMode>();
            arrayList.addAll(list);
            intent.putParcelableArrayListExtra("lock_mode_list", arrayList);
        }
        try {
            mContext.sendBroadcast(intent, SEND_RECEIVER_TO_SWIPE_PERMISSION);
        } catch (Exception e) {
            LeoLog.e(TAG, "send lock mode changed failed.", e);
        }
    }

}
