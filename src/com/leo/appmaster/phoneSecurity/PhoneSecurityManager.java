package com.leo.appmaster.phoneSecurity;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import com.leo.analytics.LeoAgent;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.feedback.FeedbackActivity;
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
 * <p>
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
    private LocationManager mLocationManager;
    private SecurLocateListener mLocationListener;
    private boolean mIsFromScan;
    private boolean mIsAdvOpenTip;
    private boolean mQiKuSendFlag = false;
    private int mMtkFromSendId = -1;
    //MTK双卡是否尝试了重发
    private boolean mIsTryMtk;
    private StringBuilder mLocalMsm;
    private boolean mIsSonyMc = false;

    private PhoneSecurityManager(Context context) {
        mContext = context;
    }

    public LocationManager getLocationManager() {
        return mLocationManager;
    }

    public void setLocationManager(LocationManager locationManager) {
        this.mLocationManager = locationManager;
    }

    public static synchronized PhoneSecurityManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PhoneSecurityManager(context.getApplicationContext());
        }
        return mInstance;
    }

    public boolean isIsSonyMc() {
        return mIsSonyMc;
    }

    public void setIsSonyMc(boolean isSonyMc) {
        this.mIsSonyMc = isSonyMc;
    }

    public StringBuilder getLocalMsm() {
        return mLocalMsm;
    }

    public synchronized void setLocalMsm(StringBuilder localMsm) {
        this.mLocalMsm = localMsm;
    }

    public boolean isIsTryMtk() {
        return mIsTryMtk;
    }

    public void setIsTryMtk(boolean isTryMtk) {
        this.mIsTryMtk = isTryMtk;
    }

    public int getMtkFromSendId() {
        return mMtkFromSendId;
    }

    public synchronized void  setMtkFromSendId(int mMtkFromSendId) {
        this.mMtkFromSendId = mMtkFromSendId;
    }

    public boolean isQiKuSendFlag() {
        return mQiKuSendFlag;
    }

    public void setQiKuSendFlag(boolean qiKuSendFlag) {
        this.mQiKuSendFlag = qiKuSendFlag;
    }

    public synchronized MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public synchronized void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mMediaPlayer = mediaPlayer;
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

    public boolean isIsAdvOpenTip() {
        return mIsAdvOpenTip;
    }

    public void setIsAdvOpenTip(boolean isAdvOpenTip) {
        this.mIsAdvOpenTip = isAdvOpenTip;
    }

    /**
     * 手机防盗功能,短信广播中处理
     */
    public boolean securityPhoneReceiverHandler(SmsMessage message) {
        if (!isDbReceiverTimeBefor()) {
            setDbReceiverTimeBefor(true);
            LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
            String number = message.getOriginatingAddress();
            String body = message.getMessageBody();
            String formate = PrivacyContactUtils.formatePhoneNumber(number);
             /*手机防盗号码*/
            String phoneNumber = mgr.getPhoneSecurityNumber();
            if (phoneNumber.contains(formate)) {
                /*去除字符串中所有的空格*/
                body = body.replace(" ", "");
                /*获取激活的防盗指令集*/
                List<String> instructs = mgr.getActivateInstructs();
                 /*查询是否触发为防盗指令*/
                if (instructs.contains(body)) {
                    setDbReceiverTimeBefor(false);
                    return true;
                }
            }
        }
        setDbReceiverTimeBefor(false);
        return false;
    }

    /**
     * 手机防盗功能,短信内容观察处理
     */
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
                                    /*检查是否为防盗指令*/
                                    checkMsmIsInstructs(phoneNumber, messages);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        setDbChangeTimeBefor(false);
                    }
                });
            }
        }
    }

    /**
     * 检查是否为防盗指令
     */
    private void checkMsmIsInstructs(String phoneNumber, ArrayList<MessageBean> messages) {
        LeoLog.i(TAG, "checkMsmIsInstructs检查是否为防盗指令");
        long cu1 = System.currentTimeMillis();

        phoneNumber = PrivacyContactUtils.simpleFromateNumber(phoneNumber);
        final LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        for (MessageBean message : messages) {
            /*短信id*/
            long msmId = message.getMsmId();
            String formateNum = PrivacyContactUtils.simpleFromateNumber(message.getPhoneNumber());
            String formate = PrivacyContactUtils.formatePhoneNumber(formateNum);
            if (phoneNumber.contains(formate)) {
                String body = message.getMessageBody();
                /*去除字符串中所有的空格*/
                body = body.replace(" ", "");
                /*获取激活的防盗指令集*/
                List<String> instructs = mgr.getActivateInstructs();
                 /*查询是否触发为防盗指令*/
                if (instructs.contains(body)) {
                    try {
                        long beforeId = LeoPreference.getInstance().getLong(PrefConst.KEY_INSTRU_MSM_ID, -1);
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
                        e.printStackTrace();
                    }
                    String selection = "_id = ? ";
                    String[] selectionArgs = new String[]{String.valueOf(msmId)};
                    int resultCount = mContext.getContentResolver().delete(PrivacyContactUtils.SMS_INBOXS, selection, selectionArgs);
                    LeoLog.i(TAG, "删除本次执行过的未删除的指令：" + resultCount);
                    if (resultCount <= 0) {
                        /*存储执行指令短信id*/
                        LeoPreference.getInstance().putLong(PrefConst.KEY_INSTRU_MSM_ID, msmId);
                    }
                    LeoLog.i(TAG, "可以执行指令，信息内容：" + body);
                    new Thread(new Runnable() {
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
//                    ThreadManager.executeOnAsyncThread(new Runnable() {
//                        @Override
//                        public void run() {
//
//                        }
//                    });

                    LeoLog.d("getTime", "准备执行位置耗时：" + (System.currentTimeMillis() - cu1));

                   /* 选择性执行防盗操作*/
                    chanageExecuteInstructs(body);
                }
            } else {
                continue;
            }
        }
    }

    /**
     * 选择性执行防盗操作
     */
    private void chanageExecuteInstructs(String body) {
        final LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
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

            long cu1 = System.currentTimeMillis();

            /*执行位置指令前，先初始化各个信息*/
            if (mLocationManager != null) {
                if (mLocationListener != null) {
                    mLocationManager.removeUpdates(mLocationListener);
                }
                setLocationManager(null);
            }
            if (mLocationListener != null) {
                mLocationListener = null;
            }
            PhoneSecurityManager.getInstance(mContext).setIsExecuteLocate(false);

            new Thread(new Runnable() {
                @Override
                public void run() {
                                                             /*sdk event*/
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_gps");
                    LostSecurityManagerImpl securMgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                    int[] protectTime = securMgr.getPhoneProtectTime();
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_gps_$" + protectTime[0] + "d" + protectTime[1] + "h");
                }
            });

            LeoLog.d("getTime", "获取位置信息准备2：" + (System.currentTimeMillis() - cu1));

            mgr.executeLockLocateposition(null, false, false);
        } else if (SecurityInstructSet.ALERT.equals(body)) {
            /*防盗警报会进入死循环，因此该除需要先删除短信和上报数据在执行*/
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

    public String getUploadPhoneData() {
        StringBuilder sb = new StringBuilder();
        String model = PhoneSecurityUtils.getPhoneModel();
        Location loc = null;
        try {
            LostSecurityManagerImpl lm = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
            loc = lm.getLocation(PhoneSecurityConstants.LOCA_ID_UP_DATA);
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
            loc = lm.getLocation(PhoneSecurityConstants.LOCA_ID_SDKWRAP);
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
                boolean lockExecuStatue = LeoPreference.getInstance().getBoolean(PrefConst.KEY_LOCK_INSTUR_EXECU_STATUE, false);
                if (isUseSecur && lockExecuStatue) {
                    mgr.executeLockPhone(false, null, true);
                    LeoPreference.getInstance().putBoolean(PrefConst.KEY_LOCK_INSTUR_EXECU_STATUE, false);
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
                boolean lockExecuStatue = LeoPreference.getInstance().getBoolean(PrefConst.KEY_LOCK_INSTUR_EXECU_STATUE, false);
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
            if (cur != null) {
                int result = cur.getCount();
                LeoLog.i(TAG, "是否有短信权限：" + result);
                if (result > 0) {
                    return true;
                }
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
    public synchronized void executeLockLocateposition(String number, boolean isExecut, boolean otherFlag) {
        PhoneSecurityManager psm = PhoneSecurityManager.getInstance(mContext);
        boolean isExeLoca = psm.isIsExecuteLocate();
        if (!isExeLoca) {
            psm.setIsExecuteLocate(true);
            LostSecurityManagerImpl manager = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
            manager.executeLockLocateposition(number, isExecut, otherFlag);
        }
    }

    /**
     * 手机防盗获取位置处理
     */
    public String securLocateHandler() {

        long cur1 = System.currentTimeMillis();

        String locateUrl = PhoneSecurityUtils.getGoogleMapLocationUri();

        long cur2 = System.currentTimeMillis();
        LeoLog.d("getTime", "首次获取位置信息耗时：" + (cur2 - cur1));
        if (Utilities.isEmpty(locateUrl)) {
            LocationManager locationManager = getLocationManager();
            if (mLocationListener == null) {
                mLocationListener = new SecurLocateListener(mLocationManager);
            }
            if (locationManager == null) {
                locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
                setLocationManager(locationManager);
            }
            String provider = PhoneSecurityUtils.getLocateProvider(mLocationManager);
            LeoLog.d("LostSecurityManagerImpl", "requestLocationUpdates,provider:" + provider);
            if (!TextUtils.isEmpty(provider)) {
                locationManager.requestLocationUpdates(provider, PhoneSecurityConstants.LOCATION_MIN_TIME, PhoneSecurityConstants.LOCATION_MIN_DISTANCE, mLocationListener, mContext.getMainLooper());
                ThreadManager.executeOnAsyncThreadDelay(new Runnable() {
                    @Override
                    public void run() {
                        LocationManager locationManager = getLocationManager();
                        if (mLocationListener != null && locationManager != null) {
                            mLocationManager.removeUpdates(mLocationListener);
                            mLocationListener = null;
                            PhoneSecurityManager.getInstance(mContext).executeLockLocateposition(null, true, true);
                            setLocationManager(null);
                            LeoLog.i("LostSecurityManagerImpl", "移除手机防盗位置改变监听！");
                        }
                    }
                }, PhoneSecurityConstants.DELAY_REMOVE_LOCATION_TIME);
            } else {
                PhoneSecurityManager.getInstance(mContext).executeLockLocateposition(null, true, true);
            }
        }
        long cur3 = System.currentTimeMillis();
        LeoLog.d("getTime", "获取位置信息总耗时：" + (cur3 - cur2));
        return locateUrl;
    }

    /**
     * 位置改变监听
     */
    private class SecurLocateListener implements LocationListener {
        private LocationManager locationManager;

        public SecurLocateListener(LocationManager locationManager) {
            this.locationManager = locationManager;
        }

        @Override
        public void onLocationChanged(Location location) {
            updateToNewLocation(location, locationManager);
        }


        @Override
        public void onProviderDisabled(String provider) {
            updateToNewLocation(null, null);
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

    /**
     * 通过location获取当前设备的具体位置
     */
    private void updateToNewLocation(Location location, LocationManager locateManager) {
        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            LeoLog.i("LostSecurityManagerImpl", "requestLocationUpdates经度：" + lng + "纬度：" + lat);
            if (locateManager != null
                    && mLocationListener != null) {
                locateManager.removeUpdates(mLocationListener);
                LeoLog.i("LostSecurityManagerImpl", "updateToNewLocation remove location listener!");
                mLocationListener = null;
                setLocationManager(null);
            } else {
                mLocationListener = null;
                setLocationManager(null);
            }
            PhoneSecurityManager.getInstance(mContext).executeLockLocateposition(null, false, true);
        }
    }

    /**
     * 是否为防盗号码
     */
    public boolean isSecurNumber(String number) {
        String formateNumber = PrivacyContactUtils.formatePhoneNumber(number);
        LostSecurityManagerImpl mgr = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        String securNumber = mgr.getPhoneSecurityNumber();
        /*防盗号码为空，传入号码为防盗号码*/
        if (!Utilities.isEmpty(securNumber)
                && securNumber.contains(formateNumber)) {
            return true;
        }

        return false;
    }

}
