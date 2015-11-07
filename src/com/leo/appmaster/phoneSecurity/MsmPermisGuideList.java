package com.leo.appmaster.phoneSecurity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.leo.appmaster.R;
import com.leo.appmaster.home.WhiteList;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;

/**
 * 需要手动开启权限的白名单列表
 * Created by runlee on 15-10-31.
 */
public class MsmPermisGuideList extends WhiteList {
    public static String TAG = "MsmPermisGuideList";
    public static final int MIUIV6PLUS = 0;
    //    public static final int MIUIV5 = 1;
    public static final int HUAWEIP7_PLUS = 1;
    public static final int HUAWEIP6 = 2;
    public static int[] LIST = {
            MIUIV6PLUS, /*MIUIV5,*/ HUAWEIP7_PLUS, HUAWEIP6
    };
    private static LockManager sLockManager;

    public MsmPermisGuideList() {
        mLists = LIST;
        sLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
    }

    @Override
    protected boolean doHandler() {
        try {
            if (mLists != null) {
                int flag = isMsmPermisListModel(mContext);
                for (int model : mLists) {
                    if (model != -1) {
                        if (model == flag) {
                            WhiteList wl = getWhiteList(model);
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
        }

        return false;
    }

    @Override
    protected WhiteList createWhiteListHandler(int flag) {
        WhiteList list = null;
        switch (flag) {
            case MIUIV6PLUS:
                list = new MiuiV6();
                break;
//            case MIUIV5:
//                list = new MiuiV5();
//                break;
            case HUAWEIP7_PLUS:
                list = new Huaweip7_plus();
                break;
            case HUAWEIP6:
                list = new Huaweip6();
                break;
        }

        return list;
    }


    /**
     * MiuiV6
     */
    private static class MiuiV6 extends WhiteList {

        @Override
        protected boolean doHandler() {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            ComponentName cn = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.MainAcitivty");
            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                mContext.startActivity(intent);
                LeoLog.i(TAG, "去miuiv6+获取系统权限");
            } catch (Exception e) {
                LeoLog.i(TAG, "去miuiv6+权限列表失败");
            }

            sLockManager.filterSelfOneMinites();
            return false;
        }
    }

    /**
     * MiuiV5
     */
    private static class MiuiV5 extends WhiteList {

        @Override
        protected boolean doHandler() {
            sLockManager.filterSelfOneMinites();
            return false;
        }
    }

    /**
     * HUAWEIP7_PLUS
     */

    private static class Huaweip7_plus extends WhiteList {

        @Override
        protected boolean doHandler() {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            ComponentName cn = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                mContext.startActivity(intent);
                LeoLog.i(TAG, "Huaweip7_plus获取系统权限");
            } catch (Exception e) {
                LeoLog.i(TAG, "Huaweip7_plus权限列表失败");
            }

            sLockManager.filterSelfOneMinites();
            return false;
        }
    }

    /**
     * HUAWEIP6
     */
    private static class Huaweip6 extends WhiteList {

        @Override
        protected boolean doHandler() {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            ComponentName cn = new ComponentName("com.huawei.permissionmanager", "com.huawei.permissionmanager.ui.MainActivity");
            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                mContext.startActivity(intent);
            } catch (Exception e) {

            }
            sLockManager.filterSelfOneMinites();
            return false;
        }
    }

    /* 判断是否为添加自启动的白名单机型 */
    public int isMsmPermisListModel(Context context) {
        boolean miuiV5 = BuildProperties.isMiuiV5();
//        if (miuiV5) {
//            return MIUIV5;
//        }
        boolean miuiv6Plus = BuildProperties.isMIUI();
        if (miuiv6Plus && !miuiV5) {
            return MIUIV6PLUS;
        }
        boolean huawei = BuildProperties.isHuaWeiTipPhone(context);
        if (huawei) {
            return HUAWEIP7_PLUS;
        }
        boolean huaweiP6 = (BuildProperties.isHuaWeiModel()
                && !BuildProperties.isHuaWeiTipPhone(context));
        if (huaweiP6) {
            return HUAWEIP6;
        }
        return -1;
    }

    /*根据机型产生对应文案*/
    public String getModelString() {
        Resources mRes = mContext.getResources();
        boolean miuiV5 = BuildProperties.isMiuiV5();
        boolean miuiv6Plus = BuildProperties.isMIUI();
        if (miuiv6Plus && !miuiV5) {
            return mRes.getString(R.string.mi_op_tip);
        }
        boolean huawei = BuildProperties.isHuaWeiTipPhone(mContext);
        if (huawei) {
            return mRes.getString(R.string.huawei7pl_op_tip);
        }
        boolean huaweiP6 = (BuildProperties.isHuaWeiModel()
                && !BuildProperties.isHuaWeiTipPhone(mContext));
        if (huaweiP6) {
            return mRes.getString(R.string.huawei6_op_tip);
        }
        return mRes.getString(R.string.secur_no_kno_model_msm_tip);
    }
}
