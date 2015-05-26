
package com.leo.appmaster.quickgestures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.appmanage.business.AppBusinessManager;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.model.BusinessItemInfo;
import com.leo.appmaster.privacycontact.ContactCallLog;
import com.leo.appmaster.privacycontact.MessageBean;
import com.leo.appmaster.quickgestures.model.QuickGsturebAppInfo;
import com.leo.appmaster.quickgestures.model.QuickGestureContactTipInfo;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.quickgestures.ui.QuickGestureFreeDisturbAppDialog;
import com.leo.appmaster.utils.LeoLog;

public class QuickGestureManager {
    public static final String TAG = "QuickGestureManager";

    private Context mContext;
    private static QuickGestureManager mInstance;
    public TreeSet<AppLauncherRecorder> mAppLaunchRecorders;
    private AppMasterPreference mSpSwitch;
    public List<MessageBean> mMessages;
    public List<ContactCallLog> mCallLogs;

    public List<BaseInfo> mDynamicList;
    public List<BaseInfo> mMostUsedList;
    private Drawable[] mEmptyIcon;

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
        mDynamicList = new ArrayList<BaseInfo>();
        mMostUsedList = new ArrayList<BaseInfo>();
        loadAppLaunchReorder();
        preloadEmptyIcon();
    }

    public List<BaseInfo> getDynamicList() {
        Vector<BusinessItemInfo> businessDatas = AppBusinessManager.getInstance(mContext)
                .getBusinessData();
        List<BaseInfo> dynamicList = new ArrayList<BaseInfo>();
        BusinessItemInfo businessItem = null;
        if (businessDatas != null && businessDatas.size() > 0) {
            businessItem = businessDatas.get(0);
            dynamicList.add(businessItem);
        }
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RecentTaskInfo> recentTasks = am.getRecentTasks(50,
                ActivityManager.RECENT_WITH_EXCLUDED);
        String pkg;
        Drawable icon;
        BaseInfo baseInfo;
        AppLoadEngine engine = AppLoadEngine.getInstance(mContext);
        List<String> pkgs = new ArrayList<String>();
        for (RecentTaskInfo recentTaskInfo : recentTasks) {
            if (dynamicList.size() > 12)
                break;
            pkg = recentTaskInfo.baseIntent.getComponent().getPackageName();
            if (!pkgs.contains(pkg)) {
                pkgs.add(pkg);
                icon = engine.getAppIcon(pkg);
                if (icon != null) {
                    baseInfo = new BaseInfo();
                    baseInfo.icon = icon;
                    baseInfo.label = engine.getAppName(pkg);
                    dynamicList.add(baseInfo);
                }
            }
        }
        return dynamicList;
    }

    private void preloadEmptyIcon() {
        Resources res = mContext.getResources();
        mEmptyIcon = new Drawable[5];
        mEmptyIcon[0] = res.getDrawable(R.drawable.switch_orange);
        mEmptyIcon[1] = res.getDrawable(R.drawable.switch_green);
        mEmptyIcon[2] = res.getDrawable(R.drawable.seitch_purple);
        mEmptyIcon[3] = res.getDrawable(R.drawable.switch_red);
        mEmptyIcon[4] = res.getDrawable(R.drawable.switch_blue);
    }

    public void stopFloatWindow() {
        LockManager.getInstatnce().stopFloatWindowService();
    }

    public void startFloatWindow() {
        LockManager.getInstatnce().startFloatWindowService();
    }

    public void loadAppLaunchReorder() {
        mAppLaunchRecorders = new TreeSet<QuickGestureManager.AppLauncherRecorder>();
        String recoders = AppMasterPreference.getInstance(mContext).getAppLaunchRecoder();
        AppLauncherRecorder temp = null;
        int sIndex = -1;
        if (!TextUtils.isEmpty(recoders)) {
            recoders = recoders.substring(0, recoders.length() - 1);
            String[] recoderList = recoders.split(";");
            for (String recoder : recoderList) {
                sIndex = recoder.indexOf(':');
                if (sIndex != -1) {
                    temp = new AppLauncherRecorder();
                    temp.pkg = recoder.substring(0, sIndex);
                    temp.launchCount = Integer.parseInt(recoder.substring(sIndex + 1));
                }
            }
        }
    }

    public void recordAppLaunch(String pkg) {
        if (TextUtils.isEmpty(pkg)) {
            return;
        }
        boolean hit = false;
        for (AppLauncherRecorder recorder : mAppLaunchRecorders) {
            if (recorder.pkg.equals(pkg)) {
                recorder.launchCount++;
                hit = true;
                break;
            }
        }
        if (!hit) {
            AppLauncherRecorder recoder = new AppLauncherRecorder();
            recoder.pkg = pkg;
            recoder.launchCount = 1;
            mAppLaunchRecorders.add(recoder);
        }

        StringBuilder resault = new StringBuilder();
        for (AppLauncherRecorder recorder : mAppLaunchRecorders) {
            resault.append(recorder.pkg).append(':').append(recorder.launchCount).append(';');
        }
        AppMasterPreference.getInstance(mContext).setAppLaunchRecoder(resault.toString());
    }

    public void removeAppLaunchRecoder(String pkg) {
        if (TextUtils.isEmpty(pkg)) {
            return;
        }
        AppLauncherRecorder hitRecoder = null;
        for (AppLauncherRecorder recorder : mAppLaunchRecorders) {
            if (recorder.pkg.equals(pkg)) {
                hitRecoder = recorder;
                break;
            }
        }
        if (hitRecoder != null) {
            mAppLaunchRecorders.remove(hitRecoder);
            StringBuilder resault = new StringBuilder();
            for (AppLauncherRecorder recorder : mAppLaunchRecorders) {
                resault.append(recorder.pkg).append(':').append(recorder.launchCount).append(';');
            }
            AppMasterPreference.getInstance(mContext).setAppLaunchRecoder(resault.toString());
        }
    }

    public List<BaseInfo> getMostUsedList() {

        return null;
    }

    public List<QuickSwitcherInfo> getSwitcherList() {

        return null;
    }

    public void updateSwitcherData(List<BaseInfo> infos) {
        String saveToSp = QuickSwitchManager.getInstance(mContext)
                .listToString(infos, infos.size());
        LeoLog.d("updateSwitcherData", "saveToSp:" + saveToSp);
        mSpSwitch.setSwitchList(saveToSp);
        mSpSwitch.setSwitchListSize(infos.size());
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
            if (LockManager.getInstatnce().isShowPrivacyCallLog) {
                LockManager.getInstatnce().isShowPrivacyCallLog = false;
            }
            if (LockManager.getInstatnce().isShowPrivacyMsm) {
                LockManager.getInstatnce().isShowPrivacyMsm = false;
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
        Drawable icon = null;
        int index = (int) (Math.random() * 4);
        icon = mEmptyIcon[index];
        return icon;
    }

    /**
     * Common App Dialog
     * 
     * @param context
     */
    public void showCommontAppDialog(final Context activity) {
        final QuickGestureFreeDisturbAppDialog commonApp = new QuickGestureFreeDisturbAppDialog(
                activity, 3);
        final AppMasterPreference pref = AppMasterPreference.getInstance(activity);
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
                        // 是否选择使用习惯自动填充
                        boolean flag = commonApp.getCheckValue();
                        if (addCommonApp != null && addCommonApp.size() > 0) {
                            for (BaseInfo info : addCommonApp) {
                                QuickGsturebAppInfo string = (QuickGsturebAppInfo) info;
                                pref.setCommonAppPackageNameAdd(string.packageName);
                            }
                        }
                        if (removeCommonApp != null && removeCommonApp.size() > 0) {
                            for (BaseInfo info : removeCommonApp) {
                                QuickGsturebAppInfo string = (QuickGsturebAppInfo) info;
                                pref.setCommonAppPackageNameRemove(string.packageName);
                            }
                        }
                        if (pref.getQuickGestureCommonAppDialogCheckboxValue() != flag) {
                            pref.setQuickGestureCommonAppDialogCheckboxValue(flag);
                        }
                    }
                }).start();
                commonApp.dismiss();
            }
        });
        commonApp.setLeftBt(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // 取消按钮
                commonApp.dismiss();
            }
        });
        commonApp.show();
    }

    /**
     * Quick Switch Dialog
     * @param mSwitchList 
     * 
     * @param context
     */
    public static void showQuickSwitchDialog(final Context context) {
        final QuickGestureFreeDisturbAppDialog quickSwitch = new QuickGestureFreeDisturbAppDialog(
                context, 2);
        quickSwitch.setTitle(R.string.pg_appmanager_quick_switch_dialog_title);
        quickSwitch.setRightBt(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 确认按钮
                AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {

                    @Override
                    public void run() {
                        // 添加的应用包名
                        List<BaseInfo> addQuickSwitch = quickSwitch.getAddFreePackageName();
                        // 移除的应用包名
                        List<BaseInfo> removeQuickSwitch = quickSwitch.getRemoveFreePackageName();
                        if (addQuickSwitch != null && addQuickSwitch.size() > 0) {
//                            String saveToSp = QuickSwitchManager.getInstance(context).ListToString(
//                                    addQuickSwitch, addQuickSwitch.size());
//                            AppMasterPreference.getInstance(context).setSwitchList(saveToSp);
//                            AppMasterPreference.getInstance(context).setSwitchListSize(
//                                    AppMasterPreference.getInstance(context).getSwitchListSize()
//                                            + addQuickSwitch.size());
                        }
                        if (removeQuickSwitch != null && removeQuickSwitch.size() > 0) {
//                            String removeToSp = QuickSwitchManager.getInstance(context)
//                                    .ListToString(removeQuickSwitch, removeQuickSwitch.size());
//                            AppMasterPreference.getInstance(context)
//                                    .setQuickSwitchPackageNameRemove(removeToSp);
//                            AppMasterPreference.getInstance(context).setSwitchListSize(
//                                    AppMasterPreference.getInstance(context).getSwitchListSize()
//                                            - removeQuickSwitch.size());
                        }
                    }
                });
                quickSwitch.dismiss();
            }
        });
        quickSwitch.setLeftBt(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // 取消按钮
                quickSwitch.dismiss();
            }
        });
        quickSwitch.show();
    }

    class AppLauncherRecorder implements Comparable<AppLauncherRecorder> {
        String pkg;
        int launchCount;

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

}
