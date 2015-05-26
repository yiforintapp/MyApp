
package com.leo.appmaster.quickgestures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.text.TextUtils;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.privacycontact.ContactCallLog;
import com.leo.appmaster.privacycontact.MessageBean;
import com.leo.appmaster.quickgestures.model.QuickGestureContactTipInfo;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.utils.LeoLog;

public class QuickGestureManager {
    public static final String TAG = "QuickGestureManager";

    private Context mContext;
    private static QuickGestureManager mInstance;
    private TreeSet<AppLauncherRecorder> mAppLaunchRecorders;
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

    public List<BaseInfo> getDynamicList() {

        return null;
    }

    public List<BaseInfo> getMostUsedList() {

        return null;
    }

    public List<QuickSwitcherInfo> getSwitcherList() {

        return null;
    }

    public void updateSwitcherData(List<BaseInfo> infos) {
        String saveToSp = QuickSwitchManager.getInstance(mContext)
                .ListToString(infos, infos.size());
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
