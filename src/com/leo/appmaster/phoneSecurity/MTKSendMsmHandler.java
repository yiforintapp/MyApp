package com.leo.appmaster.phoneSecurity;

import android.text.TextUtils;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.mgr.impl.PrivacyContactManagerImpl;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.SimDetecter;
import com.leo.appmaster.utils.Utilities;

/**
 * Created by Administrator on 2016/3/9.
 */
public class MTKSendMsmHandler {
    private static final String TAG = "MTKSendMsmHandler";
    public static final int DEF_FRO_ID = -1;
    public static final int BACKUP_SECUR_INSTRUCT_ID = 0;
    public static final int SEND_LOCAL_MSM_ID = 1;
    public static final int SIM_CHANAGE_ID = 2;
    public static final int CPU_TYPE_MTK = 0;
    public static final int CPU_TYPE_GT = 1;

    public MTKSendMsmHandler(int flag, int cpuType) {
        mtkAndGtSendMsmHandler(flag, cpuType);
    }

    private void mtkAndGtSendMsmHandler(int flag, final int typeCpu) {
        LeoLog.d(TAG, "send msm handler from:" + flag);
        if (BACKUP_SECUR_INSTRUCT_ID == flag) {
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    final LostSecurityManagerImpl lostMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                    PrivacyContactManagerImpl mgr = (PrivacyContactManagerImpl) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
                    String numberNmae = lostMgr.getPhoneSecurityNumber();
                    if (!Utilities.isEmpty(numberNmae)) {
                        String[] number = numberNmae.split(":");
                        if (number != null) {
                            String content = AppMasterApplication.getInstance().getResources().getString(R.string.secur_backup_msm);
                            if (!TextUtils.isEmpty(content) && !TextUtils.isEmpty(number[1])) {
                                sendMsmHandler(number[1], content, SimDetecter.SIM_TYPE_1, typeCpu);
                            }
                        }
                    }
                }
            });
        } else if (SEND_LOCAL_MSM_ID == flag) {
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    String sendNumber = null;
                    LostSecurityManagerImpl manager = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                    String name_number = manager.getPhoneSecurityNumber();
                    if (!Utilities.isEmpty(name_number)) {
                        String[] numbers = name_number.split(":");
                        sendNumber = numbers[1];
                    }
                    StringBuilder sb = PhoneSecurityManager.getInstance(AppMasterApplication.getInstance()).getLocalMsm();
                    if (sb == null || TextUtils.isEmpty(sb.toString()) || TextUtils.isEmpty(sendNumber)) {
                        return;
                    }
                    String body = sb.toString();
                    sendMsmHandler(sendNumber, body, SimDetecter.SIM_TYPE_1, typeCpu);
                    PhoneSecurityManager.getInstance(AppMasterApplication.getInstance()).setLocalMsm(null);
                    LeoLog.d(TAG, "MTKSendMsmHandler send body:" + body);
                }
            });
        } else if (SIM_CHANAGE_ID == flag) {
             /*检测SIM是否更换发送短信失败后重新发送*/
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    //sim卡发生了变化
                    PrivacyContactManagerImpl mgr = (PrivacyContactManagerImpl) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
                    LostSecurityManagerImpl manager = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                    /*获取机型*/
                    String model = BuildProperties.getPoneModel();
                    String body = AppMasterApplication.getInstance().getResources().getString(R.string.sim_change_msm_tip, model);
                    String sendNumber = null;
                    String name_number = manager.getPhoneSecurityNumber();
                    if (!Utilities.isEmpty(name_number)) {
                        String[] numbers = name_number.split(":");
                        sendNumber = numbers[1];
                    }
                    LeoLog.i(TAG, "SIM卡更换发送短信！");
                    if (TextUtils.isEmpty(body) || TextUtils.isEmpty(sendNumber)) {
                        return;
                    }
                    sendMsmHandler(sendNumber, body, SimDetecter.SIM_TYPE_1, typeCpu);
                }
            });
        }
    }

    //MTK和高通同双卡短信处理
    private void sendMsmHandler(String number, String body, int simType, int cpuType) {
        switch (cpuType) {
            case CPU_TYPE_MTK:
                SimDetecter.sendMtkDoubleSim(number, body, SimDetecter.SIM_TYPE_1);
                break;
            case CPU_TYPE_GT:
                SimDetecter.sendGTDoubleSim(number, body, SimDetecter.SIM_TYPE_1);
                break;
            default:
                break;
        }


    }
}
