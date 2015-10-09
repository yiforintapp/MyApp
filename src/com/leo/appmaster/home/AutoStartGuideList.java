
package com.leo.appmaster.home;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;

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
    // private static final int OPPO = 3;
    public static int[] LIST = {
            XIAOMI4, XIAOMIREAD, HUAWEIP7_PLUS, LENOVO, LETV, HUAWEIP6, IUNI
    };
    public static int[] DOUBLE_OPEN_TIP_PHONE = {
            HUAWEIP7_PLUS
    };

    public AutoStartGuideList() {
        super();
        mLists = LIST;
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
                        LeoLog.i(TAG, "该手机机型不存在于白名单~~~");
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
                LockManager.getInstatnce().timeFilterSelf();
                mContext.startActivity(intent);
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
            // ComponentName cn = new ComponentName("com.android.settings",
            // "com.miui.securitycenter.power.SelectAutoRunApplicationActivity");
            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                LockManager.getInstatnce().timeFilterSelf();
                mContext.startActivity(intent);
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
                LockManager.getInstatnce().timeFilterSelf();
                mContext.startActivity(intent);
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
                LockManager.getInstatnce().timeFilterSelf();
                mContext.startActivity(intent);
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
                LockManager.getInstatnce().timeFilterSelf();
                mContext.startActivity(intent);
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
                LockManager.getInstatnce().timeFilterSelf();
                mContext.startActivity(intent);
                LeoLog.e(TAG, "跳转huaweiP6成功！");
            } catch (Exception e) {
                LeoLog.e(TAG, "跳转huaweiP6失败！");
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
                LockManager.getInstatnce().timeFilterSelf();
                mContext.startActivity(intent);
                LeoLog.e(TAG, "跳转inui成功！");
            } catch (Exception e) {
                LeoLog.e(TAG, "跳转inui失败！");
                e.printStackTrace();
            }
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
}
