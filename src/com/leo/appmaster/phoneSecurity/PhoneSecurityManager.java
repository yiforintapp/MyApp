package com.leo.appmaster.phoneSecurity;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.media.MediaPlayer;
import android.telephony.SmsMessage;

import com.leo.analytics.LeoAgent;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.feedback.FeedbackActivity;
import com.leo.appmaster.mgr.LostSecurityManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.privacycontact.MessageBean;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.Utilities;

/**
 * Created by runlee on 15-9-29.
 * <p/>
 * 手机防盗
 */
public class PhoneSecurityManager {
    public static String TAG = "PhoneSecurityManager";
    private Context mContext;
    private static PhoneSecurityManager mInstance;
    /*短信的Observer去除一条短信两次触发的重复标志*/
    private volatile boolean mDbChangeTimeBefor;
    /*短信的Receiver去除一条短信两次触发的重复标志*/
    private volatile boolean mDbReceiverTimeBefor;
    /*是否执行了手机防盗*/
    private volatile boolean mIsExecuteSecur;
    /*停止警报*/
    private volatile boolean mStopAlert = false;
    private MediaPlayer mMediaPlayer;
    /*执行位置监听时，是否执行位置指令的标志*/
    private volatile boolean mIsExecuteLocate = false;

    private PhoneSecurityManager(Context context) {
        mContext = context;
    }

    public static synchronized PhoneSecurityManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PhoneSecurityManager(context.getApplicationContext());
        }
        return mInstance;
    }

    public synchronized MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public synchronized void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mMediaPlayer = mMediaPlayer;
    }

    public boolean isDbChangeTimeBefor() {
        return mDbChangeTimeBefor;
    }

    public void setDbChangeTimeBefor(boolean mDbChangeTimeBefor) {
        this.mDbChangeTimeBefor = mDbChangeTimeBefor;
    }

    public boolean isDbReceiverTimeBefor() {
        return mDbReceiverTimeBefor;
    }

    public void setDbReceiverTimeBefor(boolean mDbReceiverTimeBefor) {
        this.mDbReceiverTimeBefor = mDbReceiverTimeBefor;
    }

    public boolean isIsExecuteLocate() {
        return mIsExecuteLocate;
    }

    public void setIsExecuteLocate(boolean mIsExecuteLocate) {
        this.mIsExecuteLocate = mIsExecuteLocate;
    }

    public boolean isIsExecuteSecur() {
        return mIsExecuteSecur;
    }

    public void setIsExecuteSecur(boolean mIsExecuteSecur) {
        this.mIsExecuteSecur = mIsExecuteSecur;
    }

    public boolean isStopAlert() {
        return mStopAlert;
    }

    public void setStopAlert(boolean mStopAlert) {
        this.mStopAlert = mStopAlert;
    }

    /*手机防盗功能,短信广播处理*/
    public void securityPhoneReceiverHandler(final SmsMessage message) {
        if (!isDbReceiverTimeBefor()) {
            setDbReceiverTimeBefor(true);
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        setIsExecuteSecur(true);
                        final LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                         /*手机防盗是否开启*/
                        boolean isOpenSecurit = mgr.isUsePhoneSecurity();
                        if (isOpenSecurit) {
                            String phoneNumberSms = message.getOriginatingAddress();// 电话号
                            String messageBody = message.getMessageBody();// 短信内容
                            if (!Utilities.isEmpty(messageBody)) {
                                /*手机防盗号码*/
                                String phoneNumber = mgr.getPhoneSecurityNumber();
                                if (Utilities.isEmpty(phoneNumber)) {
                                    setDbReceiverTimeBefor(false);
                                    return;
                                }
                                LeoLog.i(TAG, "securityPhoneReceiverHandler");
                                String formate = PrivacyContactUtils.formatePhoneNumber(phoneNumberSms);
                                if (phoneNumber.contains(formate)) {
                                    String body = messageBody;
                            /*去除字符串中所有的空格*/
                                    body = body.replace(" ", "");
                            /*获取激活的防盗指令集*/
                                    List<String> instructs = mgr.getActivateInstructs();
                            /*查询是否触发为防盗指令*/
                                    if (instructs.contains(body)) {
                                     /*是否开启高级保护*/
                                        boolean isOpenAdvan = mgr.isOpenAdvanceProtect();
                                /*选择性执行防盗操作*/
                                        if (SecurityInstructSet.ONEKEY.equals(body)) {
                                            if (isOpenAdvan) {
                                                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_onekey");
                                                boolean result = mgr.executeOnekey();
                                            }
                                        } else if (SecurityInstructSet.LOCATEPOSITION.equals(body)) {
                                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_gps");
                                            mgr.executeLockLocateposition(null, false);
                                        } else if (SecurityInstructSet.ALERT.equals(body)) {
                                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_alarm");
                                            boolean result = mgr.executeAlert(true);
                                        } else if (SecurityInstructSet.FORMATDATA.equals(body)) {
                                            if (isOpenAdvan) {
                                                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_format");
                                                boolean result = mgr.executePhoneMasterClear();
                                            }
                                        } else if (SecurityInstructSet.LOCKMOBILE.equals(body)) {
                                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_lock");
                                            boolean result = mgr.executeLockPhone(false, null, false);
                                        } else if (SecurityInstructSet.OFFALERT.equals(body)) {
                                            PhoneSecurityManager.getInstance(mContext).setStopAlert(true);
                                            mgr.executeStopAlert(true);
                                        }
                                        /*上传手机数据*/
                                        String uploadStr = getUploadPhoneData();
                                        if (uploadStr != null) {
                                            LeoAgent.addEvent(PhoneSecurityConstants.UPLOAD_PHONE_DATA_ID, uploadStr);
                                            LeoLog.i(TAG, "Receiver数据：" + uploadStr);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        setDbReceiverTimeBefor(false);

                    }
                }
            });
        }
    }


    /*手机防盗功能,短信内容观察处理*/
    public void securityPhoneOberserHandler() {
        if (!isDbChangeTimeBefor()) {
            boolean isExecuteSecu = isIsExecuteSecur();
            if (!isExecuteSecu) {
                setDbChangeTimeBefor(true);
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                            /*手机防盗是否开启*/
                            boolean isOpenSecurit = mgr.isUsePhoneSecurity();
                            if (isOpenSecurit) {
                                ArrayList<MessageBean> messages = (ArrayList<MessageBean>) PrivacyContactUtils
                                        .getSysMessage(mContext, "read=0 AND type=1", null, true, false);
                                if (!messages.isEmpty()) {
                                    /*手机防盗号码*/
                                    String phoneNumber = mgr.getPhoneSecurityNumber();
                                    if (Utilities.isEmpty(phoneNumber)) {
                                        setDbChangeTimeBefor(false);
                                        return;
                                    }
                                    LeoLog.i(TAG, "securityPhoneOberserHandler");
                                    LeoLog.i(TAG, "Obsever:短信数量：" + messages.size());
                                    for (MessageBean message : messages) {
                                        /*短信id*/
                                        long msmId = message.getMsmId();
                                        String formate = PrivacyContactUtils.formatePhoneNumber(message.getPhoneNumber());
                                        if (phoneNumber.contains(formate)) {
                                            String body = message.getMessageBody();
                                         /*去除字符串中所有的空格*/
                                            body = body.replace(" ", "");
                                            /*获取激活的防盗指令集*/
                                            List<String> instructs = mgr.getActivateInstructs();
                                            /*查询是否触发为防盗指令*/
                                            if (instructs.contains(body)) {
                                                try {
                                                    long beforeId = PreferenceTable.getInstance().getLong(PrefConst.KEY_INSTRU_MSM_ID, -1);
                                                    if (beforeId > 0) {
                                                        if (msmId <= beforeId) {
                                                            /*执行完毕删除该信息*/
                                                            String selection = "_id = ? ";
                                                            String[] selectionArgs = new String[]{String.valueOf(msmId)};
                                                            int resultCount = mContext.getContentResolver().delete(PrivacyContactUtils.SMS_INBOXS, selection, selectionArgs);
                                                            LeoLog.i(TAG, "删除上次执行过的未删除的指令：" + resultCount);
                                                            continue;
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                }
                                                String selection = "_id = ? ";
                                                String[] selectionArgs = new String[]{String.valueOf(msmId)};
                                                int resultCount = mContext.getContentResolver().delete(PrivacyContactUtils.SMS_INBOXS, selection, selectionArgs);
                                                LeoLog.i(TAG, "删除本次执行过的未删除的指令：" + resultCount);
                                                if (resultCount <= 0) {
                                                         /*存储执行指令短信id*/
                                                    PreferenceTable.getInstance().putLong(PrefConst.KEY_INSTRU_MSM_ID, msmId);
                                                }
                                                LeoLog.i(TAG, "可以执行指令，信息内容：" + body);
                                                ThreadManager.executeOnAsyncThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                           /*上传手机数据*/
                                                        String uploadStr = getUploadPhoneData();
                                                        if (uploadStr != null) {
                                                            LeoAgent.addEvent(PhoneSecurityConstants.UPLOAD_PHONE_DATA_ID, uploadStr);
                                                            LeoLog.i(TAG, "Obser数据：" + uploadStr);
                                                        }

                                                        String sekData = getSDKWrapperData();
                                                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_location_" + sekData);
                                                    }
                                                });
                                                    /*选择性执行防盗操作*/
                                                if (SecurityInstructSet.ONEKEY.equals(body)) {
                                                    ThreadManager.executeOnAsyncThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            boolean result = mgr.executeOnekey();
                                                        }
                                                    });
                                                     /*sdk event*/
                                                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_onekey");
                                                    LostSecurityManagerImpl securMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                                                    int[] protectTime = securMgr.getPhoneProtectTime();
                                                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_onekey_$" + protectTime[0] + "d" + protectTime[1] + "h");
                                                } else if (SecurityInstructSet.LOCATEPOSITION.equals(body)) {
                                                    ThreadManager.executeOnAsyncThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                             /*sdk event*/
                                                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_gps");
                                                            LostSecurityManagerImpl securMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                                                            int[] protectTime = securMgr.getPhoneProtectTime();
                                                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_gps_$" + protectTime[0] + "d" + protectTime[1] + "h");
                                                        }
                                                    });
                                                    mgr.executeLockLocateposition(null, false);
                                                } else if (SecurityInstructSet.ALERT.equals(body)) {
                                                    /*防盗警报灰进入死循环，因此该除需要先删除短信和上报数据在执行*/
                                                    ThreadManager.executeOnAsyncThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            boolean result = mgr.executeAlert(true);
                                                        }
                                                    });
                                                     /*sdk event*/
                                                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_alarm");
                                                    LostSecurityManagerImpl securMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                                                    int[] protectTime = securMgr.getPhoneProtectTime();
                                                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_alarm_$" + protectTime[0] + "d" + protectTime[1] + "h");
                                                } else if (SecurityInstructSet.FORMATDATA.equals(body)) {
                                                    ThreadManager.executeOnAsyncThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                             /*sdk event*/
                                                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_format");
                                                            LostSecurityManagerImpl securMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                                                            int[] protectTime = securMgr.getPhoneProtectTime();
                                                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_format_$" + protectTime[0] + "d" + protectTime[1] + "h");
                                                        }
                                                    });
                                                    boolean result = mgr.executePhoneMasterClear();
                                                } else if (SecurityInstructSet.LOCKMOBILE.equals(body)) {
                                                    ThreadManager.executeOnAsyncThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                             /*sdk event*/
                                                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_lock");
                                                            LostSecurityManagerImpl securMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                                                            int[] protectTime = securMgr.getPhoneProtectTime();
                                                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_lock_$" + protectTime[0] + "d" + protectTime[1] + "h");
                                                        }
                                                    });
                                                    boolean result = mgr.executeLockPhone(false, null, false);
                                                } else if (SecurityInstructSet.OFFALERT.equals(body)) {
                                                    ThreadManager.executeOnAsyncThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                             /*sdk event*/
                                                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_alarmoff");
                                                            LostSecurityManagerImpl securMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                                                            int[] protectTime = securMgr.getPhoneProtectTime();
                                                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_alarmoff_$" + protectTime[0] + "d" + protectTime[1] + "h");
                                                        }
                                                    });
                                                    PhoneSecurityManager.getInstance(mContext).setStopAlert(true);
                                                    mgr.executeStopAlert(true);
                                                }
                                            }
                                        } else {
                                            continue;
                                        }
                                    }
                                }

                            }
                        } catch (Exception e) {

                        }
                        setDbChangeTimeBefor(false);
                    }
                });
            }
        }
    }

    public String getUploadPhoneData() {
        StringBuilder sb = new StringBuilder();
        String model = PhoneSecurityUtils.getPhoneModel();
        Location loc = null;
        try {
            LostSecurityManagerImpl lm = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
            loc = lm.getLocation();
        } catch (Exception e) {

        }
        String cpuName = PhoneSecurityUtils.getCpuName();
        String ram = PhoneSecurityUtils.getTotalRam();
        String sd = String.valueOf(PhoneSecurityUtils.getSDCardMemory()[0]);
        String screPi = PhoneSecurityUtils.getScreenPix(mContext);
        String physicalSize = "" + PhoneSecurityUtils.getScreenPhysicalSize(mContext);
        sb.append("PhoneModel：" + model + ",");
        if (loc != null) {
            sb.append("Latitude：" + loc.getLatitude() + ",");
            sb.append("Longitude:" + loc.getLongitude() + ",");
        } else {
            sb.append("Latitude：" + ",");
            sb.append("Longitude:" + ",");
        }
        sb.append("CPU_Name：" + cpuName + ",");
        sb.append("Ram_Memory：" + ram + ",");
        sb.append("SD_Size：" + sd + ",");
        sb.append("Screen_Resolution：" + screPi + ",");
        sb.append("Screen_Size：" + physicalSize);
        return sb.toString();
    }

    /*SDKWrapper上报*/
    public String getSDKWrapperData() {
        StringBuilder sb = new StringBuilder();
        String model = PhoneSecurityUtils.getPhoneModel();
        Location loc = null;
        try {
            LostSecurityManagerImpl lm = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
            loc = lm.getLocation();
        } catch (Exception e) {

        }
        String screPi = PhoneSecurityUtils.getScreenPix(mContext);
        double physicalSize = PhoneSecurityUtils.getScreenPhysicalSize(mContext);
        sb.append(model + ",");
        if (loc != null) {
            sb.append("Latitude：" + loc.getLatitude() + ";");
            sb.append("Longitude:" + loc.getLongitude() + ",");
        } else {
            sb.append("Latitude：" + ";");
            sb.append("Longitude:" + ",");
        }
        sb.append(screPi + ",");
        sb.append(physicalSize);
        return sb.toString();
    }

    /*使用锁定手机后如果解锁成功去除所有锁定手机加的锁*/
    public void removeAllModeLockList() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                boolean isUseSecur = mgr.isUsePhoneSecurity();
                boolean lockExecuStatue = PreferenceTable.getInstance().getBoolean(PrefConst.KEY_LOCK_INSTUR_EXECU_STATUE, false);
                if (isUseSecur && lockExecuStatue) {
                    mgr.executeLockPhone(false, null, true);
                    PreferenceTable.getInstance().putBoolean(PrefConst.KEY_LOCK_INSTUR_EXECU_STATUE, false);
                    LeoLog.i(TAG, "解锁成功去除所有模式加锁！");
                }
            }
        });

    }

    /*启动锁定手机指令后，监控系统新安装应用加锁*/
    public void executeLockInstruInstallListener(final String packageName) {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                boolean isUseSecur = mgr.isUsePhoneSecurity();
                boolean lockExecuStatue = PreferenceTable.getInstance().getBoolean(PrefConst.KEY_LOCK_INSTUR_EXECU_STATUE, false);
                if (isUseSecur && lockExecuStatue) {
                    mgr.executeLockPhone(true, packageName, false);
                }
            }
        });
    }

    /**
     * 手机防盗对已知需手动打开权限的手机特别引导
     */
    public void openMsmPermissionGuide() {
        new MsmPermisGuideList().executeGuide();
    }

    /**
     * 从防盗帮助页面到用户反馈，提交防盗反馈
     */
    public void securHelpToFeedBack() {
        Intent intent = new Intent(mContext, FeedbackActivity.class);
        intent.putExtra(PhoneSecurityConstants.SECUR_HELP_TO_FEEDBACK, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    /**
     * 尝试到系统中去读取短信
     * 用于判断是否有权限去读取短信
     * （如果系统短信为空，该判断不能信任）
     *
     * @return
     */
    public boolean tryReadSysMsm() {
        ContentResolver cr = mContext.getContentResolver();
        Cursor cur = null;
        try {
            cur = cr.query(PrivacyContactUtils.SMS_INBOXS, null, null, null, "_id asc LIMIT " + PhoneSecurityConstants.TYY_READ_MSM_COUNT);
            int result = cur.getCount();
            LeoLog.i(TAG, "是否有短信权限：" + result);
            if (result > 0) {
                return true;
            }
        } catch (Exception e) {
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
        return false;
    }

    /*执行位置指令*/
    public synchronized void executeLockLocateposition(String number, boolean isExecute) {
        PhoneSecurityManager psm=PhoneSecurityManager.getInstance(mContext);
        boolean isExeLoca=psm.isIsExecuteLocate();
        if (!isExeLoca) {
            psm.setIsExecuteLocate(true);
            LostSecurityManagerImpl manager = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
            manager.executeLockLocateposition(number, isExecute);
        }
    }

}
