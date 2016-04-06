
package com.leo.appmaster.home;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.view.WindowManager;

import com.leo.appmaster.Constants;
import com.leo.appmaster.PhoneInfo;
import com.leo.appmaster.R;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

import java.util.List;

/**
 * 自启动引导
 *
 * @author run
 */
public class AutoStartGuideList extends WhiteList {
    public static final String TAG = "AutoStartGuideList";
    public static final int XIAOMI4 = 0;
    public static final int XIAOMIREAD = 1;
    public static final int HUAWEIP7_PLUS = 2;
    public static final int LENOVO = 3;
    public static final int LETV = 4;
    public static final int HUAWEIP6 = 5;
    public static final int IUNI = 6;
    public static final int SAMSUMG_SYS = 7;
    private static final int OPPO = 8;
    public static int[] LIST = {
            XIAOMI4, XIAOMIREAD, HUAWEIP7_PLUS, LENOVO, LETV, HUAWEIP6, IUNI, SAMSUMG_SYS, OPPO
    };


    /*双提示打开系统权限的机型 */
    public static int[] DOUBLE_OPEN_TIP_PHONE = {
            HUAWEIP7_PLUS
    };

    private static final int SAMSUNG_TIP_COUNT = 3;
    private static LockManager sLockManager;

    private static Boolean sIsSamSumg;

    public AutoStartGuideList() {
        super();
        mLists = LIST;
        sLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
    }

    @Override
    protected boolean doHandler() {
        try {
            if (mLists != null) {
                int flag = isAutoWhiteListModel(mContext);
                for (int i : mLists) {
                    if (flag != -1) {
                        if (flag == i) {
                            WhiteList wl = getWhiteList(i);
                            wl.executeGuide();
                            break;
                        }
                    } else {
                        LeoLog.i(TAG, "This phone is exist!");
                        break;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected WhiteList createWhiteListHandler(int flag) {
        WhiteList list = null;
        switch (flag) {
            case XIAOMI4:
                list = new XiaoMi4();
                break;
            case XIAOMIREAD:
                list = new ReadMi();
                break;
            case HUAWEIP7_PLUS:
                list = new HuaWei();
                break;
            case LENOVO:
                list = new Lenovo();
                break;
            case LETV:
                list = new Letv();
                break;
            case HUAWEIP6:
                list = new HuaWeiP6();
                break;
            case IUNI:
                list = new Iuini();
                break;
            case SAMSUMG_SYS:
                list = new SamSungOptimize();
                break;
            case OPPO:
                list = new Oppo();
                break;
            default:
                break;
        }
        return list;
    }

    /* 小米4 */
    private static class XiaoMi4 extends AutoStartGuideList {

        @Override
        protected boolean doHandler() {
            LeoLog.i(TAG, "加载小米4的处理方法");

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            ComponentName cn = new ComponentName("com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity");
            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                mContext.startActivity(intent);
                sLockManager.filterSelfOneMinites();
                sLockManager.filterPackage("com.miui.securitycenter", Constants.TIME_FILTER_TARGET);
                LeoLog.i(TAG, "跳转小米4成功！");
            } catch (Exception e) {
                LeoLog.i(TAG, "跳转小米4失败！");
                e.printStackTrace();
            }
            return false;
        }

    }

    /* 红米 */
    private static class ReadMi extends AutoStartGuideList {

        @Override
        protected boolean doHandler() {
            LeoLog.i(TAG, "加载红米的处理方法");

            Intent intent = new Intent();
            ComponentName cn = new ComponentName("com.android.settings",
                    "com.miui.securitycenter.permission.PermMainActivity");
            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                mContext.startActivity(intent);
                sLockManager.filterSelfOneMinites();
                sLockManager.filterPackage("com.android.settings", Constants.TIME_FILTER_TARGET);
                LeoLog.i(TAG, "跳转红米成功！");
            } catch (Exception e) {
                LeoLog.i(TAG, "跳转红米失败！");
                e.printStackTrace();
            }
            return false;
        }

    }

    /* 华为 */
    private static class HuaWei extends AutoStartGuideList {

        @Override
        protected boolean doHandler() {
            LeoLog.i(TAG, "加载 华为的处理方法");

            Intent intent = new Intent();
            ComponentName cn = new ComponentName("com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity");
            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                mContext.startActivity(intent);
                sLockManager.filterSelfOneMinites();
                sLockManager.filterPackage("com.huawei.systemmanager", Constants.TIME_FILTER_TARGET);
                LeoLog.e(TAG, "跳转huawei成功！");
            } catch (Exception e) {
                LeoLog.e(TAG, "跳转huawei失败！");
                e.printStackTrace();
            }
            return false;
        }

    }

    /* Lenovo */
    private static class Lenovo extends AutoStartGuideList {

        @Override
        protected boolean doHandler() {
            LeoLog.i(TAG, "加载Lenovo的处理方法");

            Intent intent = new Intent();
            ComponentName cn = new ComponentName("com.lenovo.security",
                    "com.lenovo.security.homepage.HomePageActivity");
            intent.setComponent(cn);
            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                mContext.startActivity(intent);
                sLockManager.filterSelfOneMinites();
                sLockManager.filterPackage("com.lenovo.security", Constants.TIME_FILTER_TARGET);
                LeoLog.e(TAG, "跳转Lenovo成功！");
            } catch (Exception e) {
                LeoLog.e(TAG, "跳转Lenovo失败！");
                e.printStackTrace();
            }
            return false;
        }
    }

    /* Letv */
    public static class Letv extends AutoStartGuideList {
        @Override
        protected boolean doHandler() {
            Intent intent = new Intent();
            ComponentName cn = new ComponentName("com.letv.android.letvsafe",
                    "com.letv.android.letvsafe.AutobootManageActivity");
            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                mContext.startActivity(intent);
                sLockManager.filterSelfOneMinites();
                sLockManager.filterPackage("com.letv.android.letvsafe", Constants.TIME_FILTER_TARGET);
                Log.e(TAG, "跳转Letv成功！");
            } catch (Exception e) {
                Log.e(TAG, "跳转Letv失败！");
                e.printStackTrace();
            }
            return false;
        }

    }

    /* huaweiP6 */
    public static class HuaWeiP6 extends AutoStartGuideList {
        @Override
        protected boolean doHandler() {
            Intent intent = new Intent();
            ComponentName cn = new ComponentName("com.huawei.android.hwpowermanager",
                    "com.huawei.android.hwpowermanager.BootApplicationActivity");
            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                mContext.startActivity(intent);
                sLockManager.filterSelfOneMinites();
                sLockManager.filterPackage("com.huawei.android.hwpowermanager", Constants.TIME_FILTER_TARGET);
                LeoLog.e(TAG, "跳转huaweiP6成功！");
            } catch (Exception e) {
                LeoLog.e(TAG, "跳转huaweiP6失败！");
                e.printStackTrace();
            }
            return false;
        }

    }

    /*oppo rom*/
    public static class Oppo extends AutoStartGuideList {
        @Override
        protected boolean doHandler() {

            Intent intent = new Intent();
            ComponentName cm = new ComponentName("com.oppo.purebackground", "com.oppo.purebackground.PurebackgroundTopActivity");
            intent.setComponent(cm);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                mContext.startActivity(intent);
                sLockManager.filterSelfOneMinites();
                sLockManager.filterPackage("com.oppo.purebackground", Constants.TIME_FILTER_TARGET);
                LeoLog.e(TAG, "skip oppo rom sucess！");
            } catch (Exception e) {
                LeoLog.e(TAG, "skip oppo rom fail！");
                e.printStackTrace();
            }
            return false;
        }
    }


    /* inui */
    public static class Iuini extends AutoStartGuideList {
        @Override
        protected boolean doHandler() {
            Intent intent = new Intent();
            ComponentName cn = new ComponentName("com.aurora.secure",
                    "com.secure.activity.MainActivity");
            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                mContext.startActivity(intent);
                sLockManager.filterSelfOneMinites();
                sLockManager.filterPackage("com.aurora.secure", Constants.TIME_FILTER_TARGET);
                LeoLog.e(TAG, "跳转inui成功！");
            } catch (Exception e) {
                LeoLog.e(TAG, "跳转inui失败！");
                e.printStackTrace();
            }
            return false;
        }
    }

    /*samsung 是否为存在“电池优化-应用程序优化”的系统*/
    public static class SamSungOptimize extends AutoStartGuideList {
        @Override
        protected boolean doHandler() {

            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "gd_wcnts", "gd_samsung_cover_use");

            startSamSungOpIntent(mContext);
            return false;
        }
    }


    /* 判断是否为添加自启动的白名单机型 */
    public static int isAutoWhiteListModel(Context context) {
        boolean miuiV5 = BuildProperties.isMiuiV5();
        if (miuiV5) {
            return XIAOMIREAD;
        }
        boolean miuiv6Plus = BuildProperties.isMIUI();
        if (miuiv6Plus && !miuiV5) {
            return XIAOMI4;
        }
        boolean huawei = BuildProperties.isHuaWeiTipPhone(context);
        if (huawei) {
            return HUAWEIP7_PLUS;
        }
        boolean lenovo = BuildProperties.isLenoveModel();
        if (lenovo) {
            return LENOVO;
        }
        boolean letv = BuildProperties.isLetvModel();
        if (letv) {
            return LETV;
        }
        boolean huaweiP6 = (BuildProperties.isHuaWeiModel()
                && !BuildProperties.isHuaWeiTipPhone(context));
        if (huaweiP6) {
            return HUAWEIP6;
        }
        boolean inui = BuildProperties.isIuniModel();
        if (inui) {
            return IUNI;
        }
         /*samsung 是否为存在“电池优化-应用程序优化”的系统*/
        boolean samSung = isSamSungGuideModel(context);
        if (samSung) {
            return SAMSUMG_SYS;
        }
        Boolean oppo = BuildProperties.isOppoOs();
        if (oppo) {
            return OPPO;
        }

        return -1;
    }

    /* 获取不同机型的引导文案 */
    public static int getAutoWhiteListTipText(Context context) {
        boolean miuiV5 = BuildProperties.isMiuiV5();
        if (miuiV5) {
            return R.string.auto_start_tip_redmi;
        }
        boolean miuiv6Plus = BuildProperties.isMIUI();
        if (miuiv6Plus && !miuiV5) {
            return R.string.auto_start_tip_xiaomi4_and_letv;
        }
        boolean huawei = BuildProperties.isHuaWeiTipPhone(context);
        if (huawei) {
            return R.string.auto_start_tip_huawei;
        }
        boolean lenovo = BuildProperties.isLenoveModel();
        if (lenovo) {
            return R.string.auto_start_tip_lenovo;
        }
        boolean letv = BuildProperties.isLetvModel();
        if (letv) {
            return R.string.auto_start_tip_xiaomi4_and_letv;
        }
        /*samsung 是否为存在“电池优化-应用程序优化”的系统*/
        boolean samSung = isSamSungGuideModel(context);
        if (samSung) {
            return R.string.samsung_tip_txt;
        }
        Boolean oppo = BuildProperties.isOppoOs();
        if (oppo) {
            return R.string.oppo_rom_tip_txt;
        }
        return R.string.auto_start_guide_tip_content;
    }

    /* 查询是否为双提示打开系统权限的机型 */
    public static boolean isDoubleTipOPenPhone(int phoneModel) {
        for (int i : DOUBLE_OPEN_TIP_PHONE) {
            if (i == phoneModel) {
                return true;
            }
        }
        return false;
    }

    /*Samsung 5.1.1 sys 电池优化权限提示*/
    public static synchronized boolean samSungSysTip(Context context, String key) {
        LeoPreference prefer = LeoPreference.getInstance();
        int countFlag = prefer.getInt(PrefConst.KEY_LOCK_SAMSUNG_TIP, 1);
        boolean isCountEnough = countFlag > SAMSUNG_TIP_COUNT;
        boolean samSung = isSamSungGuideModel(context);
        boolean appConsumed = prefer.getBoolean(PrefConst.KEY_APP_LOCK_HANDLER, false);
        if (!samSung || !appConsumed || isCountEnough) {
            return false;
        }
        if (PrefConst.KEY_HOME_SAMSUNG_TIP.equals(key)) {
            int count = prefer.getInt(PrefConst.KEY_LOCK_SAMSUNG_TIP, 1);
            if (count <= SAMSUNG_TIP_COUNT) {
                showSamSungDialog(context, key);
                prefer.putInt(PrefConst.KEY_LOCK_SAMSUNG_TIP, count + 1);
                return true;
            }
        } else if (PrefConst.KEY_LOCK_SAMSUNG_TIP.equals(key)) {
            int count = prefer.getInt(PrefConst.KEY_LOCK_SAMSUNG_TIP, 1);
            if (count <= SAMSUNG_TIP_COUNT) {
                showSamSungDialog(context, key);
                prefer.putInt(PrefConst.KEY_LOCK_SAMSUNG_TIP, count + 1);
                return true;
            }
        }
        return false;
    }

    private static void showSamSungDialog(final Context context, String key) {
        final Context contextApp = context.getApplicationContext();
        LeoPreference prefer = LeoPreference.getInstance();
        boolean appLockHandler = prefer.getBoolean(PrefConst.KEY_APP_LOCK_HANDLER, false);
        if (appLockHandler) {
            prefer.putBoolean(PrefConst.KEY_APP_LOCK_HANDLER, false);
        }

        final LEOAlarmDialog dialog = new LEOAlarmDialog(context);
        dialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SDKWrapper.addEvent(context, SDKWrapper.P1, "gd_wcnts", "gd_battery_samsung_use");

                startSamSungOpIntent(contextApp);
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        dialog.setLeftBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SDKWrapper.addEvent(context, SDKWrapper.P1, "gd_wcnts", "gd_battery_samsung_later");

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        String content = context.getResources().getString(R.string.samsung_tip_txt);
        dialog.setContent(content);
        String leftBt = context.getResources().getString(R.string.quick_first_tip_dialog_left_bt);
        dialog.setLeftBtnStr(leftBt);
        String rightBt = context.getResources().getString(R.string.setting);
        dialog.setRightBtnStr(rightBt);
        int type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        dialog.getWindow().setType(type);
        dialog.show();
        ((HomeActivity)context).setUninstallGuideShow();

        SDKWrapper.addEvent(context, SDKWrapper.P1, "gd_wcnts", "gd_battery_samsung");
    }

    /*判断是否该手机存在三星S的应用程序优化*/
    public static boolean isSamSungSActivity(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setPackage("com.samsung.android.sm");
        intent.setClassName("com.samsung.android.sm", "com.samsung.android.sm.ui.ram.AppLockingViewActivity");
        List<ResolveInfo> info = context.getPackageManager().queryIntentActivities(intent, 0);
        if (info != null && info.size() > 0) {
            return true;
        }
        return false;
    }

    /*判断是否该手机存在三星Note的应用程序优化*/
    public static boolean isSamSungNActivity(Context context) {
        // PGL-21
        if (PhoneInfo.getAndroidVersion() > 21) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setPackage("com.samsung.android.sm");
            intent.setClassName("com.samsung.android.sm", "com.samsung.android.sm.common.appmanager.AppLockingViewActivity");
            List<ResolveInfo> info = context.getPackageManager().queryIntentActivities(intent, 0);
            if (info != null && info.size() > 0) {
                return true;
            }
        }

        return false;
    }

    public static void startSamSungOpIntent(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setPackage("com.samsung.android.sm");
        intent.setClassName("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            LockManager manager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
            manager.filterSelfOneMinites();
            context.startActivity(intent);
            sLockManager.filterPackage("com.samsung.android.sm", Constants.TIME_FILTER_TARGET);
            LeoLog.e(TAG, "跳转samsung成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveSamSungAppLock() {
        LeoPreference leoPreference = LeoPreference.getInstance();
        int countFlag = leoPreference.getInt(PrefConst.KEY_LOCK_SAMSUNG_TIP, 1);
        boolean isCountEnough = countFlag > SAMSUNG_TIP_COUNT;
        if (isCountEnough) {
            return;
        }
        boolean appLockHandler = leoPreference.getBoolean(PrefConst.KEY_APP_LOCK_HANDLER, false);
        if (!appLockHandler) {
            leoPreference.putBoolean(PrefConst.KEY_APP_LOCK_HANDLER, true);
        }
    }


    public static boolean isSamSungGuideModel(Context context) {
        if (sIsSamSumg == null) {
            sIsSamSumg = BuildProperties.isSamSungModel()
                    && (isSamSungSActivity(context) || isSamSungNActivity(context));
        }
        return sIsSamSumg;
    }
}
