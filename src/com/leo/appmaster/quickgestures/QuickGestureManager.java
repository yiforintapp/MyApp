
package com.leo.appmaster.quickgestures;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.provider.CallLog.Calls;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity.NameComparator;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.appmanage.business.AppBusinessManager;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.model.BusinessItemInfo;
import com.leo.appmaster.privacycontact.ContactCallLog;
import com.leo.appmaster.privacycontact.MessageBean;
import com.leo.appmaster.quickgestures.QuickGestureManager.AppLauncherRecorder;
import com.leo.appmaster.quickgestures.model.QuickGestureContactTipInfo;
import com.leo.appmaster.quickgestures.model.QuickGsturebAppInfo;
import com.leo.appmaster.quickgestures.tools.ColorMatcher;
import com.leo.appmaster.quickgestures.ui.QuickGestureActivity;
import com.leo.appmaster.quickgestures.ui.QuickGestureFilterAppDialog;
import com.leo.appmaster.quickgestures.ui.QuickGesturePopupActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NotificationUtil;

public class QuickGestureManager {
    public static final String TAG = "QuickGestureManager";

    protected static final String AppLauncherRecorder = null;
    public static final int APPITEMINFO = 1;
    public static final int NORMALINFO = 0;
    private static QuickGestureManager mInstance;
    private Context mContext;
    private boolean mInited = false;
    private AppMasterPreference mSpSwitch;
    public List<MessageBean> mMessages;
    public List<ContactCallLog> mCallLogs;
    public List<BaseInfo> mDynamicList;
    public List<BaseInfo> mMostUsedList;
    private Drawable[] mColorBgIcon;
    private Drawable mEmptyIcon;
    public int mSlidAreaSize;
    public boolean isShowPrivacyMsm = false;
    public boolean isShowPrivacyCallLog = false;
    public boolean isShowSysNoReadMessage = false;
    /*
     * -1:左侧底，-2：左侧中，1：右侧底，2：右侧中
     */
    public int onTuchGestureFlag = -1;
    public boolean isJustHome;
    public boolean isAppsAndHome;
    public boolean isLeftBottom, isRightBottom, isLeftCenter, isRightCenter;
    public int screenSpace;//根布局与屏幕高的差值
    private QuickGestureManager(Context ctx) {
        mContext = ctx.getApplicationContext();
        mSpSwitch = AppMasterPreference.getInstance(mContext);
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
            for (Drawable drawable : mColorBgIcon) {
                bmp = ((BitmapDrawable) drawable).getBitmap();
                LockManager.getInstatnce().mMatcher.addBitmapSample(bmp);
            }
            // TODO switcher init
            QuickSwitchManager.getInstance(mContext).init();
        }
    }

    public void unInit() {
        if (mInited) {
            mInited = false;
            mDynamicList.clear();
            mMostUsedList.clear();
            mDynamicList = null;
            mMostUsedList = null;
            LockManager.getInstatnce().mAppLaunchRecorders.clear();
            // LockManager.getInstatnce().mAppLaunchRecorders = null;
            mColorBgIcon = null;
            LockManager.getInstatnce().mMatcher.clearItem();
            // LockManager.getInstatnce().mMatcher = null;
            LockManager.getInstatnce().mDrawableColors.clear();
            // LockManager.getInstatnce().mDrawableColors = null;
            // TODO Switcher uninit
            QuickSwitchManager.getInstance(mContext).unInit();
        }
    }

    public Bitmap getMatchedColor(Drawable drawable) {
        Bitmap target = null;
        target = LockManager.getInstatnce().mDrawableColors.get(drawable);
        if (target == null) {
            target = LockManager.getInstatnce().mMatcher.getMatchedBitmap(drawable);
            if (target != null) {
                LockManager.getInstatnce().mDrawableColors.put(drawable, target);
            }
        }
        return target;
    }

    public List<BaseInfo> getDynamicList() {
        Vector<BusinessItemInfo> businessDatas = AppBusinessManager.getInstance(mContext)
                .getBusinessData();
        List<BaseInfo> dynamicList = new ArrayList<BaseInfo>();
        if (businessDatas != null && businessDatas.size() > 0) {
            int count = 0;
            for (BusinessItemInfo businessItem : businessDatas) {
                if (count == 4 || !businessItem.iconLoaded || businessItem.icon == null) {
                    break;
                }
                count++;
                dynamicList.add(businessItem);
            }
            // businessItem = businessDatas.get(0);
            // dynamicList.add(businessItem);
        }
        // no read sys_message
        boolean isShowMsmTip = AppMasterPreference.getInstance(mContext)
                .getSwitchOpenNoReadMessageTip();
        boolean isShowCallLogTip = AppMasterPreference.getInstance(mContext)
                .getSwitchOpenRecentlyContact();
        boolean isShowPrivacyContactTip = AppMasterPreference.getInstance(mContext)
                .getSwitchOpenPrivacyContactMessageTip();
        if (isShowMsmTip) {
            if (QuickGestureManager.getInstance(mContext).mMessages != null) {
                List<MessageBean> messages = QuickGestureManager.getInstance(mContext).mMessages;
                for (MessageBean message : messages) {
                    message.icon = mContext.getResources().getDrawable(
                            R.drawable.gesture_message);
                    if (message.getMessageName() != null
                            && !"".equals(message.getMessageName())) {
                        message.label = message.getMessageName();
                    } else {
                        message.label = message.getPhoneNumber();
                    }
                    message.isShowReadTip = true;
                    dynamicList.add(message);
                    if (dynamicList.size() >= 11)
                        break;
                }
            }
        }
        // // no read sys_call
        if (isShowCallLogTip) {
            if (QuickGestureManager.getInstance(mContext).mCallLogs != null) {
                List<ContactCallLog> baseInfos = QuickGestureManager.getInstance(mContext).mCallLogs;
                for (ContactCallLog baseInfo : baseInfos) {
                    baseInfo.icon = mContext.getResources().getDrawable(
                            R.drawable.gesture_call);
                    if (baseInfo.getCallLogName() != null
                            && !"".equals(baseInfo.getCallLogName())) {
                        baseInfo.label = baseInfo.getCallLogName();
                    } else {
                        baseInfo.label = baseInfo.getCallLogNumber();
                    }
                    baseInfo.isShowReadTip = true;
                    dynamicList.add(baseInfo);
                    if (dynamicList.size() >= 11)
                        break;
                }
            }
        }
        // privacy contact
        if (isShowPrivacyContactTip) {
            if (isShowPrivacyCallLog || isShowPrivacyMsm) {
                if (dynamicList.size() <= 11) {
                    QuickGestureContactTipInfo item = new QuickGestureContactTipInfo();
                    item.icon = mContext.getResources().getDrawable(
                            R.drawable.gesture_system);
                    item.label = mContext.getResources().getString(
                            R.string.pg_appmanager_quick_gesture_privacy_contact_tip_lable);
                    item.isShowReadTip = true;
                    dynamicList.add(item);
                }
            }
        }

        AppLoadEngine engine = AppLoadEngine.getInstance(mContext);
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

        return dynamicList;
    }

    private void preloadColorIcon() {
        Resources res = mContext.getResources();
        mColorBgIcon = new Drawable[11];
        mColorBgIcon[0] = res.getDrawable(R.drawable.switch_orange);
        mColorBgIcon[1] = res.getDrawable(R.drawable.switch_green);
        mColorBgIcon[2] = res.getDrawable(R.drawable.seitch_purple);
        mColorBgIcon[3] = res.getDrawable(R.drawable.switch_red);
        mColorBgIcon[4] = res.getDrawable(R.drawable.switch_blue);
        mColorBgIcon[5] = res.getDrawable(R.drawable.switch_blue_2);
        mColorBgIcon[6] = res.getDrawable(R.drawable.switch_blue_3);
        mColorBgIcon[7] = res.getDrawable(R.drawable.switch_green_2);
        mColorBgIcon[8] = res.getDrawable(R.drawable.switch_orange_2);
        mColorBgIcon[9] = res.getDrawable(R.drawable.switch_purple_2);
        mColorBgIcon[10] = res.getDrawable(R.drawable.switch_red_2);
    }

    public void stopFloatWindow() {
        LockManager.getInstatnce().stopFloatWindowService();
    }

    public void startFloatWindow() {
        LockManager.getInstatnce().startFloatWindowService();
    }

    // public void loadAppLaunchReorder() {
    // mAppLaunchRecorders = new
    // ArrayList<QuickGestureManager.AppLauncherRecorder>();
    // String recoders =
    // AppMasterPreference.getInstance(mContext).getAppLaunchRecoder();
    // AppLauncherRecorder temp = null;
    // int sIndex = -1;
    // if (!TextUtils.isEmpty(recoders)) {
    // recoders = recoders.substring(0, recoders.length() - 1);
    // String[] recoderList = recoders.split(";");
    // for (String recoder : recoderList) {
    // sIndex = recoder.indexOf(':');
    // if (sIndex != -1) {
    // temp = new AppLauncherRecorder();
    // temp.pkg = recoder.substring(0, sIndex);
    // temp.launchCount = Integer.parseInt(recoder.substring(sIndex + 1));
    // mAppLaunchRecorders.add(temp);
    // }
    // }
    // }
    // }

    // public void recordAppLaunch(String pkg) {
    // if (TextUtils.isEmpty(pkg)) {
    // return;
    // }
    // boolean hit = false;
    // for (AppLauncherRecorder recorder : mAppLaunchRecorders) {
    // if (recorder.pkg.equals(pkg)) {
    // recorder.launchCount++;
    // hit = true;
    // break;
    // }
    // }
    // if (!hit) {
    // AppLauncherRecorder recoder = new AppLauncherRecorder();
    // recoder.pkg = pkg;
    // recoder.launchCount = 1;
    // mAppLaunchRecorders.add(recoder);
    // }
    // saveAppLaunchRecoder();
    // }

    // public void saveAppLaunchRecoder() {
    // StringBuilder resault = new StringBuilder();
    // for (AppLauncherRecorder recorder : mAppLaunchRecorders) {
    // resault.append(recorder.pkg).append(':').append(recorder.launchCount).append(';');
    // }
    // AppMasterPreference.getInstance(mContext).setAppLaunchRecoder(resault.toString());
    // }

    // public void removeAppLaunchRecoder(String pkg) {
    // if (TextUtils.isEmpty(pkg)) {
    // return;
    // }
    // AppLauncherRecorder hitRecoder = null;
    // for (AppLauncherRecorder recorder : mAppLaunchRecorders) {
    // if (recorder.pkg.equals(pkg)) {
    // hitRecoder = recorder;
    // break;
    // }
    // }
    // if (hitRecoder != null) {
    // mAppLaunchRecorders.remove(hitRecoder);
    // StringBuilder resault = new StringBuilder();
    // for (AppLauncherRecorder recorder : mAppLaunchRecorders) {
    // resault.append(recorder.pkg).append(':').append(recorder.launchCount).append(';');
    // }
    // AppMasterPreference.getInstance(mContext).setAppLaunchRecoder(resault.toString());
    // }
    // }

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
        for (int i = 0; i < mHaveList.size(); i++) {
            QuickGsturebAppInfo mInfo = (QuickGsturebAppInfo) mHaveList.get(i);
            LeoLog.d("testSp", "mInfo.pk : " + mInfo.packageName +
                    " ; mInfo.posi : " + mInfo.gesturePosition);
        }

        List<BaseInfo> newresault = new ArrayList<BaseInfo>();
        List<BaseInfo> resault = new ArrayList<BaseInfo>();
        ArrayList<AppLauncherRecorder> recorderApp = LockManager.getInstatnce().mAppLaunchRecorders;

        // LaunchCount
        Collections.sort(recorderApp, new LaunchCount());

        AppLoadEngine engine = AppLoadEngine.getInstance(mContext);
        if (recorderApp != null && (recorderApp.size() > 1 || mHaveList.size() > 0)) {
            LeoLog.d("testSp", "recorderApp.size() : " + recorderApp.size());

            Iterator<AppLauncherRecorder> recorder = recorderApp.iterator();
            int i = 0;
            AppItemInfo info;
            QuickGsturebAppInfo temp = null;
            while (recorder.hasNext()) {
                AppLauncherRecorder recorderAppInfo = recorder.next();
                if (recorderAppInfo.launchCount > 0) {
                    info = engine.getAppInfo(recorderAppInfo.pkg);
                    LeoLog.d("testSp", "recorderApp.pk : " + recorderAppInfo.pkg +
                            " ; mInfo.launchCount : " + recorderAppInfo.launchCount);
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

        LeoLog.d("testSp", "resault.size() : " + resault.size());

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
            LeoLog.d("testSp", "mSize : " + mSize);
            if (resault.size() > 0) {
                for (int i = 0; i < 11; i++) {
                    LeoLog.d("testSp", "i  is : " + i);
                    QuickGsturebAppInfo mInfo = (QuickGsturebAppInfo) mHaveList.get(k);
                    int m = j;
                    if (resault.size() <= j) {
                        m = resault.size() - 1;
                    }
                    QuickGsturebAppInfo mInfoCount = (QuickGsturebAppInfo) resault.get(m);

                    boolean isGetPosition = isGetPosition(i, mPositions);
                    if (isGetPosition) {
                        LeoLog.d("testSp", "mInfo.gesturePosition : " + mInfo.gesturePosition);
                        newresault.add(mInfo);
                        if (k < mHaveList.size() - 1) {
                            k++;
                        }
                    } else {
                        if (resault.size() > j && i < mSize) {
                            LeoLog.d("testSp", "mInfoCount.gesturePosition : "
                                    + mInfoCount.gesturePosition);
                            mInfoCount.gesturePosition = i;
                            newresault.add(mInfoCount);
                            j++;
                        }
                    }
                }
                LeoLog.d("testSp", "newresault.size : " + newresault.size());
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
                        LeoLog.d("testSp", "mBackListRank.pk : " + mInfo.packageName);
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

    // private QuickGsturebAppInfo checkInfo(List<BaseInfo> resault, String
    // mPack) {
    // QuickGsturebAppInfo returnInfo = new QuickGsturebAppInfo();
    // for (int j = 0; j < resault.size(); j++) {
    // QuickGsturebAppInfo mInInfo = (QuickGsturebAppInfo) resault.get(j);
    // if (mPack.equals(mInInfo.packageName)) {
    // returnInfo = mInInfo;
    // }
    // }
    // return returnInfo;
    // }

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
        // QuickSwitchManager.getInstance(mContext).getAllList();
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
            if (mMessages != null && mMessages.size() > 0) {
                mMessages.remove(bean);
            }
        } else if (info instanceof ContactCallLog) {
            ContactCallLog callLog = (ContactCallLog) info;
            if (mCallLogs != null && mCallLogs.size() > 0) {
                mCallLogs.remove(callLog);
            }
        } else if (info instanceof QuickGestureContactTipInfo) {
            if (isShowPrivacyCallLog) {
                isShowPrivacyCallLog = false;
            }
            if (isShowPrivacyMsm) {
                isShowPrivacyMsm = false;
            }
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
        // Drawable icon = null;
        // int index = (int) (Math.random() * 10);
        // icon = mColorBgIcon[index];
        // return icon;
    }

    /**
     * Common App Dialog
     * 
     * @param context
     */
    public void showCommonAppDialog(final Context context) {
        final QuickGestureFilterAppDialog commonApp = new QuickGestureFilterAppDialog(
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
                // 确认按钮
                new Thread(new Runnable() {
                    @Override
                    public void run() {
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

                            // 判断要加入的是否已存在，已存在的不加入
                            // addCommonApp = hasSameName(addCommonApp, true);

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

                        // }
                        // else {
                        // List<AppLauncherRecorder> removeList = new
                        // ArrayList<QuickGestureManager.AppLauncherRecorder>();
                        // ArrayList<AppLauncherRecorder> record =
                        // LockManager.getInstatnce().mAppLaunchRecorders;
                        // for (AppLauncherRecorder appLauncherRecorder :
                        // record) {
                        // for (BaseInfo removeInfo : removeCommonApp) {
                        // QuickGsturebAppInfo info = (QuickGsturebAppInfo)
                        // removeInfo;
                        // if (appLauncherRecorder.pkg.equals(info.packageName))
                        // {
                        // removeList.add(appLauncherRecorder);
                        // break;
                        // }
                        // }
                        // }
                        // if (removeList.size() > 0) {
                        // record.removeAll(removeList);
                        // }
                        // AppLauncherRecorder addRecord;
                        //
                        // int maxCount = 0;
                        // for (AppLauncherRecorder appLauncherRecorder :
                        // record) {
                        // if (appLauncherRecorder.launchCount > maxCount) {
                        // maxCount = appLauncherRecorder.launchCount;
                        // }
                        // }
                        //
                        // if (addCommonApp != null && addCommonApp.size() > 0)
                        // {
                        // if (record != null && record.size() > 0) {
                        // addCommonApp = hasSameName(addCommonApp, true);
                        // }
                        // }
                        //
                        // for (BaseInfo removeInfo : addCommonApp) {
                        // addRecord = new AppLauncherRecorder();
                        // QuickGsturebAppInfo info = (QuickGsturebAppInfo)
                        // removeInfo;
                        // addRecord.pkg = info.packageName;
                        // LeoLog.e("QuickGestureManager", addRecord.pkg + ":"
                        // + info.gesturePosition);
                        // if (record.size() > 0) {
                        // addRecord.launchCount = maxCount + 1;
                        // } else {
                        // addRecord.launchCount = 1;
                        // }
                        // record.add(addRecord);
                        // }
                        //
                        // LeoLog.e("QuickGestureManager", "record size = " +
                        // record.size());
                        // for (AppLauncherRecorder appLauncherRecorder :
                        // record) {
                        // LeoLog.e("QuickGestureManager",
                        // appLauncherRecorder.pkg + ":"
                        // + appLauncherRecorder.launchCount);
                        // }
                        // LockManager.getInstatnce().saveAppLaunchRecoder();
                        // }

                        if (pref.getQuickGestureCommonAppDialogCheckboxValue() != flag) {
                            pref.setQuickGestureCommonAppDialogCheckboxValue(flag);
                            if (flag) {
                                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "qs_tab",
                                        "common_auto");
                            }
                        }
                    }

                    // private List<BaseInfo> hasSameName(List<BaseInfo>
                    // addCommonApp2, boolean isCheck) {
                    // List<BaseInfo> items;
                    // if (isCheck) {
                    // items = loadRecorderAppInfo();
                    // } else {
                    // items = loadCommonAppInfo();
                    // }
                    // List<BaseInfo> AddCommonApps = new ArrayList<BaseInfo>();
                    // for (BaseInfo addInfo : addCommonApp2) {
                    // boolean isHasSameName = false;
                    // for (BaseInfo item : items) {
                    // if (item.label.equals(addInfo.label)) {
                    // isHasSameName = true;
                    // }
                    // }
                    // if (!isHasSameName) {
                    // AddCommonApps.add(addInfo);
                    // }
                    // isHasSameName = false;
                    // }
                    // return AddCommonApps;
                    // }
                }).start();
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

        commonApp.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        commonApp.show();
    }

    public void deleteBusinessItem(BusinessItemInfo info) {
        AppBusinessManager abm = AppBusinessManager.getInstance(mContext);
        abm.removeBusinessData(info);
    }

    /**
     * Quick Switch Dialog
     * 
     * @param mSwitchList
     * @param context
     * @param activity
     */
    public void showQuickSwitchDialog(final Context context) {
        final QuickGestureFilterAppDialog quickSwitch = new QuickGestureFilterAppDialog(
                context.getApplicationContext(), 2);
        quickSwitch.setTitle(R.string.pg_appmanager_quick_switch_dialog_title);
        quickSwitch.setRightBt(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 确认按钮
                AppMasterApplication.getInstance().postInAppThreadPool(new
                        Runnable() {
                            @Override
                            public void run() {
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
                                LeoLog.d("QuickGestureManager", "mChangeList ： " + mChangeList);
                                mSpSwitch.setSwitchList(mChangeList);
                            }
                        });
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
        quickSwitch.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        quickSwitch.show();
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

    public void sendPermissionOpenNotification(final Context context) {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                NotificationManager notificationManager = (NotificationManager)
                        context
                                .getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notification = new Notification();
                // LockManager.getInstatnce().timeFilterSelf();
                LockManager.getInstatnce().addFilterLockPackage("com.leo.appmaster", false);
                Intent intentPending = new Intent(context,
                        QuickGestureActivity.class);
                intentPending.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intentPending,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                notification.icon = R.drawable.ic_launcher_notification;
                notification.tickerText = context
                        .getString(R.string.permission_open_tip_notification_title);
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                notification
                        .setLatestEventInfo(
                                context,
                                context.getString(R.string.permission_open_tip_notification_title),
                                context.getString(R.string.permission_open_tip_notification_content),
                                contentIntent);
                NotificationUtil.setBigIcon(notification,
                        R.drawable.ic_launcher_notification_big);
                notification.when = System.currentTimeMillis();
                notificationManager.notify(20150603, notification);
                AppMasterPreference.getInstance(context).setQuickPermissonOpenFirstNotificatioin(
                        true);
            }
        }, 5000);

    }

    // public void removeItemFromAppLaunchRecoder(String packageName) {
    // for (int i = 0; i < mAppLaunchRecorders.size(); i++) {
    // if (mAppLaunchRecorders.get(i).pkg.equals(packageName)) {
    // mAppLaunchRecorders.remove(i);
    // }
    // }
    // }

}
