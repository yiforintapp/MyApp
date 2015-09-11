
package com.leo.appmaster.home;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

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
    private static final int XIAOMI4 = 0;
    private static final int XIAOMIREAD = 1;
    private static final int HUAWEI = 2;
//    private static final int OPPO = 3;
    private static int[] LIST = {
            XIAOMI4, XIAOMIREAD, HUAWEI
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
                        LeoLog.i(TAG, "该手机机型不存在于白名单");
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
            case HUAWEI:
                list = new HuaWei();
                break;
//            case OPPO:
//                list = new Oppo();
//                break;
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

    /* oppo */
    private static class Oppo extends AutoStartGuideList {

        @Override
        protected boolean doHandler() {
            LeoLog.i(TAG, "加载Oppo的处理方法");

            Intent intent = new Intent();
            /* 进入纯净后台 */
            ComponentName cn = new
                    ComponentName("com.oppo.purebackground",
                            "com.oppo.purebackground.Purebackground_AddTrust_Activity");
            /* 进入自启动管理 */
            // ComponentName cn = new ComponentName("com.oppo.safe",
            // "com.oppo.safe.permission.startup.StartupAppListActivity");
             intent.setComponent(cn);
             intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                LockManager.getInstatnce().timeFilterSelf();
                mContext.startActivity(intent);
                LeoLog.e(TAG, "跳转oppo成功！");
            } catch (Exception e) {
                LeoLog.e(TAG, "跳转oppo失败！");
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
            return HUAWEI;
        }
//        boolean oppo = BuildProperties.isOppoOs();
//        if (oppo) {
//            return OPPO;
//        }
        return -1;
    }
}
