package com.leo.appmaster.mgr.impl;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telecom.Log;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.receiver.DeviceReceiver;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.LostSecurityManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.phoneSecurity.PhoneSecurityConstants;
import com.leo.appmaster.phoneSecurity.PhoneSecurityManager;
import com.leo.appmaster.phoneSecurity.PhoneSecurityUtils;
import com.leo.appmaster.phoneSecurity.SecurityInstructSet;
import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.SimDetecter;
import com.leo.appmaster.utils.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LostSecurityManagerImpl extends LostSecurityManager {
    public static final Boolean DBG = false;
    public static final String TAG = "LostSecurityManagerImpl";
    /*位置精度，1米检测*/
    public static final int LOCATION_MIN_DISTANCE = 1;
    /*时间精度,1秒检测*/
    public static final int LOCATION_MIN_TIME = 1000;
    private static final int MAX_SCORE = 6;
    private static boolean mIsLocation;
    private static boolean mIsOnkey;
    private static boolean mIsFormate;
    private static boolean mIsLock;
    private static boolean mIsAlert;
    private LocationManager mLocationManager;
    private SecurLocateListener mLocationListener;

    @Override
    public void onDestory() {

    }

    @Override
    public int getUsePhoneSecurityCount() {
        int count = 0;
        int number = PreferenceTable.getInstance().getInt(PrefConst.KEY_USE_SECUR_NUMBER, 0);
        if (number <= 0) {
            count = PhoneSecurityConstants.USE_SECUR_NUMBER;
        } else {
            count = number;
        }
        return count;
    }


    @Override
    public boolean setUsePhoneSecurityConut(int count) {

        try {
            int number = PreferenceTable.getInstance().getInt(PrefConst.KEY_USE_SECUR_NUMBER, 0);
            if (count > 0 && number != count) {
                PreferenceTable.getInstance().putInt(PrefConst.KEY_USE_SECUR_NUMBER, count);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isUsePhoneSecurity() {
        if (DBG) {
            return true;
        }
        return PreferenceTable.getInstance().getBoolean(PrefConst.KEY_PHONE_SECURITY_STATE, false);
    }

    @Override
    public boolean setUsePhoneSecurity(boolean securityState) {

        try {
            boolean flag = PreferenceTable.getInstance().getBoolean(PrefConst.KEY_PHONE_SECURITY_STATE, false);
            PreferenceTable.getInstance().putBoolean(PrefConst.KEY_PHONE_SECURITY_STATE, securityState);
            if (!flag && securityState) {
                notifySecurityChange();
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
        }
    }

    @Override
    public int addPhoneSecurityNumber(ContactBean contact) {
        try {
            if (contact == null) {
                PreferenceTable.getInstance().putString(PrefConst.KEY_PHONE_SECURITY_TELPHONE_NUMBER, "");
                return 0;
            }
            String selfNumber = getSelfPhoneNumnber();
            if (!Utilities.isEmpty(selfNumber)) {
                String formateSelfNumber = PrivacyContactUtils.formatePhoneNumber(selfNumber);
                /*查询加入的手机防盗号码是否为本机号码*/
                if (contact.getContactNumber().contains(formateSelfNumber)) {
                    return 1;
                }
            }
            String name = contact.getContactName();
            StringBuilder phone = new StringBuilder();
            phone.append(name);
            phone.append(":");
            phone.append(contact.getContactNumber());
            PreferenceTable.getInstance().putString(PrefConst.KEY_PHONE_SECURITY_TELPHONE_NUMBER, phone.toString());
            return 2;
        } catch (Exception e) {

        }
        return -1;
    }

    @Override
    public boolean modifyPhoneSecurityNumber(String phoneNumber) {
        try {
            if (Utilities.isEmpty(phoneNumber)) {
                return false;
            }
            String selfNumber = getSelfPhoneNumnber();
            if (!Utilities.isEmpty(selfNumber)) {
                String formateSelfNumber = PrivacyContactUtils.formatePhoneNumber(selfNumber);
                /*查询加入的手机防盗号码是否为本机号码*/
                if (phoneNumber.contains(formateSelfNumber)) {
                    return false;
                }
            }
            PreferenceTable.getInstance().putString(PrefConst.KEY_PHONE_SECURITY_TELPHONE_NUMBER, phoneNumber);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<String> getPhoneSecurityInstructionSets() {
        List<String> instructs = new ArrayList<String>();
        instructs.add(SecurityInstructSet.ONEKEY);
        instructs.add(SecurityInstructSet.LOCATEPOSITION);
        instructs.add(SecurityInstructSet.ALERT);
        instructs.add(SecurityInstructSet.FORMATDATA);
        instructs.add(SecurityInstructSet.LOCKMOBILE);
        instructs.add(SecurityInstructSet.OFFALERT);
        return instructs;
    }

    @Override
    public Location getLocation() {
        /*可根据设备状况动态选择location provider*/
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        Location location = null;
        Criteria criteria = new Criteria();
        /*设置为最大精度*/
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        /*不要求海拔信息*/
        criteria.setAltitudeRequired(false);
        /*不要求方位信息*/
        criteria.setBearingRequired(false);
        /*是否允许付费*/
        criteria.setCostAllowed(true);
        /*对电量的要求*/
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = mLocationManager.getBestProvider(criteria, true);
        if(provider==null){
            provider =LocationManager.NETWORK_PROVIDER;
        }
        LeoLog.i(TAG, "provider=" + provider);
        if (mLocationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if (location == null && mLocationManager
                .isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            location = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }

        if (location == null && mLocationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (location == null) {
            LeoLog.i(TAG, "location为空");
        } else {
            LeoLog.i(TAG, "location不为空");
        }
        if (location == null) {
            mLocationListener = new SecurLocateListener(mLocationManager);
            if (mLocationManager == null) {
                mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            }
            mLocationManager.requestLocationUpdates(provider, LOCATION_MIN_TIME, LOCATION_MIN_DISTANCE, mLocationListener, mContext.getMainLooper());
            ThreadManager.executeOnAsyncThreadDelay(new Runnable() {
                @Override
                public void run() {
                    if (mLocationListener != null) {
                        if (mLocationManager != null) {
                            mLocationManager.removeUpdates(mLocationListener);
                            mLocationListener = null;
                            PhoneSecurityManager.getInstance(mContext).executeLockLocateposition(null, true);
                            LeoLog.i(TAG, "Task移除位置监听");
                        }
                    }
                }
            }, PhoneSecurityConstants.DELAY_REMOVE_LOCATION_TIME);
        }
        return location;
    }

    @Override
    public boolean isSelectInstructionBackupFromMsm() {
        return false;
    }

    @Override
    public boolean isOpenAdvanceProtect() {
        DevicePolicyManager manager = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName mAdminName = new ComponentName(mContext, DeviceReceiver.class);
        if (manager.isAdminActive(mAdminName)) {
            return true;
        } else {
            return false;
        }
    }


    @Override
    public int[] getPhoneProtectTime() {
        /**
         * 保护时间以小时为单位
         */
        PreferenceTable preferTable = PreferenceTable.getInstance();
        int[] securityTime = new int[2];
        /*获取系统当前时间*/
        long currentTime = System.currentTimeMillis();
        /*获取开启手机防盗的时间*/
        float openSecurityTimeF = preferTable.getFloat(PrefConst.KEY_OPEN_PHONE_SECRITY_TIME, 0);
        long openTime = (long) openSecurityTimeF;
        /*手机保护时间*/
        long securityTimeMillis = currentTime - openTime;
        if (securityTimeMillis > 0) {
            /*天的余数*/
            long dayNumber = securityTimeMillis % PhoneSecurityConstants.DAY_TIME;
            /*天数*/
            int day = (int) (securityTimeMillis / PhoneSecurityConstants.DAY_TIME);
            if (day > 0) {
                securityTime[0] = day;
            } else {
                securityTime[0] = 0;
            }
            if (dayNumber > 0) {
                int hours = (int) (dayNumber / PhoneSecurityConstants.HOURS_TIME);
                securityTime[1] = hours;
            } else {
                /*0小时*/
                securityTime[1] = 0;
            }

        } else {
            /*0天0小时*/
            securityTime[0] = 0;
            securityTime[1] = 0;

        }
        return securityTime;
    }

    @Override
    public boolean isSendInstructionBackupMsm() {
        return false;
    }

    @Override
    public int getSecurityScore() {
        return isUsePhoneSecurity() ? PhoneSecurityConstants.PHONE_SECURITY_SCORE : 0;
    }

    @Override
    public void setOpenSecurityTime() {
        long currentTime = System.currentTimeMillis();
                /*保存开启手机防盗的当前时间*/
        PreferenceTable.getInstance().putFloat(PrefConst.KEY_OPEN_PHONE_SECRITY_TIME, currentTime);
    }

    @Override
    public boolean executeLockLocateposition(String number, boolean isExecute) {
        PhoneSecurityManager.getInstance(mContext).mIsExecuteLocate = false;
        if (!mIsLocation) {
            LeoLog.i(TAG, "执行位置追踪指令");
            mIsLocation = true;
            String sendNumber = null;
            if (Utilities.isEmpty(number)) {
            /*如果不指定手机号，默认给防盗号码发送位置信息*/
                String name_number = getPhoneSecurityNumber();
                if (!Utilities.isEmpty(name_number)) {
                    String[] numbers = name_number.split(":");
                    sendNumber = numbers[1];
                }
            } else {
                sendNumber = number;
            }
            if (Utilities.isEmpty(sendNumber)) {
                mIsLocation = false;
                return false;
            }
            String googleMapUri = null;
            String locatePositionMsm = null;
            if (!isExecute) {
                googleMapUri = PhoneSecurityUtils.getGoogleMapLocationUri();
            }
            if (!Utilities.isEmpty(googleMapUri)) {
                locatePositionMsm = mContext.getResources().getString(R.string.secur_location_msm, googleMapUri);
                LeoLog.i(TAG, "执行位置URL=" + locatePositionMsm);
                try {
                    PrivacyContactManagerImpl mgr = (PrivacyContactManagerImpl) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
                    mgr.sendMessage(sendNumber, locatePositionMsm);
                    mIsLocation = false;
                    return true;
                } catch (Exception e) {
                }
            } else {
                if (isExecute) {
                    String noLocation = mContext.getResources().getString(R.string.secur_send_msm_no_location);
                    locatePositionMsm = noLocation;
                    try {
                        PrivacyContactManagerImpl mgr = (PrivacyContactManagerImpl) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
                        mgr.sendMessage(sendNumber, locatePositionMsm);
                        mIsLocation = false;
                        return true;
                    } catch (Exception e) {
                    }
                }
            }
            mIsLocation = false;
        }
        return false;
    }

    @Override
    public boolean executePhoneMasterClear() {
        if (!mIsFormate) {
            LeoLog.i(TAG, "执行擦除指令");
            mIsFormate = true;
            try {
                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
                devicePolicyManager.wipeData(0);
                mIsFormate = false;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mIsFormate = false;
            }
        }
        return false;
    }

    @Override
    public boolean executeLockPhone(boolean isLockListener, String packageName, boolean isClearAllLockList) {
        if (!mIsLock) {
            LeoLog.i(TAG, "指令锁定程序指令");
            mIsLock = true;
            LockManagerImpl lockManager = (LockManagerImpl) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
            try {
                ArrayList<AppItemInfo> appInfoList = null;
                if (!isLockListener) {
                /*系统中所有应用*/
                    appInfoList = AppLoadEngine.getInstance(mContext).getAllPkgInfo();
                }
                 /*获取所有锁模式*/
                List<LockMode> modeList = lockManager.getLockMode();
                for (LockMode mode : modeList) {
                    LeoLog.i(TAG, "锁模式名字：" + mode.modeName);
                    List<String> addLockList = Collections.synchronizedList(new ArrayList<String>());
                    if (!isLockListener) {
                     /*获取每个模式的锁列表*/
                        List<String> lockList = mode.lockList;
                        if (!appInfoList.isEmpty()) {
                            for (AppItemInfo appInfo : appInfoList) {
                                if (!lockList.isEmpty() && !isClearAllLockList) {
                                    if (!lockList.contains(appInfo.packageName)) {
                                        addLockList.add(appInfo.packageName);
                                    }
                                } else {
                                    addLockList.add(appInfo.packageName);
                                }
                            }
                        }
                    } else {
                        /*监控系统安装加锁*/
                        if (!Utilities.isEmpty(packageName)) {
                            addLockList.add(packageName);
                        }
                    }
                    if (!isClearAllLockList) {
                         /*将所列表加入到锁模式中*/
                        lockManager.addPkg2Mode(addLockList, mode);
                        if (!isLockListener) {
                            boolean lockExecuStatue = PreferenceTable.getInstance().getBoolean(PrefConst.KEY_LOCK_INSTUR_EXECU_STATUE, false);
                            if (!lockExecuStatue) {
                                PreferenceTable.getInstance().putBoolean(PrefConst.KEY_LOCK_INSTUR_EXECU_STATUE, true);
                            }
                        }
                    } else {
                        if (appInfoList != null) {
                            lockManager.removePkgFromMode(addLockList, mode);
                        }
                        LeoLog.i(TAG, "清空所有模式的锁列表");
                    }

                }
                mIsLock = false;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mIsLock = false;
            }
        }
        return false;
    }

    @Override
    public boolean executeAlert(boolean alertMode) {
        if (!mIsAlert) {
            mIsAlert = true;
            /*响铃一次*/
            if (!alertMode) {
                LeoLog.i(TAG, "执行警报指令");
                AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                //系统音乐音量
                int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
                LeoLog.i(TAG, "最大音量：" + maxVolume + ",当前音量：" + currentVolume);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
                /*获取系统铃声*/
                Uri sysSingUri = RingtoneManager.getActualDefaultRingtoneUri(mContext,
                        RingtoneManager.TYPE_RINGTONE);
                PhoneSecurityManager pm = PhoneSecurityManager.getInstance(mContext);
                if (pm.getmMediaPlayer() == null) {
                    pm.setmMediaPlayer(MediaPlayer.create(mContext, sysSingUri));
                }
                MediaPlayer media = pm.getmMediaPlayer();
                media.setLooping(true);
                try {
                    media.start();
                    mIsAlert = false;
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mIsAlert = false;
                }
                 /*停止警报*/
                if (PhoneSecurityManager.getInstance(mContext).mStopAlert) {
                    alertMode = false;
                    PhoneSecurityManager.getInstance(mContext).mStopAlert = false;
                }
            }
            /*一直响铃*/
            while (alertMode) {
                LeoLog.i(TAG, "执行警报指令");
                AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                //系统音乐音量
                int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
                LeoLog.i(TAG, "最大音量：" + maxVolume + ",当前音量：" + currentVolume);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
                /*获取系统铃声*/
                Uri sysSingUri = RingtoneManager.getActualDefaultRingtoneUri(mContext,
                        RingtoneManager.TYPE_RINGTONE);
                PhoneSecurityManager pm = PhoneSecurityManager.getInstance(mContext);
                if (pm.getmMediaPlayer() == null) {
                    pm.setmMediaPlayer(MediaPlayer.create(mContext, sysSingUri));
                }
                MediaPlayer media = pm.getmMediaPlayer();
                media.setLooping(true);
                try {
                    media.start();
                    mIsAlert = false;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mIsAlert = false;
                }
                /*停止警报*/
                if (PhoneSecurityManager.getInstance(mContext).mStopAlert) {
                    alertMode = false;
                    PhoneSecurityManager.getInstance(mContext).mStopAlert = false;
                    executeStopAlert(true);
                    break;
                }
            }
        }

        return false;
    }

    @Override
    public boolean executeStopAlert(boolean isStopAlert) {
        MediaPlayer media = PhoneSecurityManager.getInstance(mContext).getmMediaPlayer();
        if (isStopAlert && media != null) {
            media.stop();
            PhoneSecurityManager.getInstance(mContext).setmMediaPlayer(null);
        }
        return false;
    }

    @Override
    public boolean executeOnekey() {
         /*一键防盗，执行顺序：锁定，追踪，警报，擦除*/
        if (!mIsOnkey) {
            LeoLog.i(TAG, "执行一键防盗");
            mIsOnkey = true;
            try {
                boolean result = executeLockPhone(false, null, false);
                boolean resultPostion = executeLockLocateposition(null, false);
                boolean resultAlert = executeAlert(false);
                ThreadManager.executeOnAsyncThreadDelay(new Runnable() {
                    @Override
                    public void run() {
                        boolean resultClear = executePhoneMasterClear();
                    }
                }, PhoneSecurityConstants.ONKEY_FORMATE_EXECU_DELAT_TIME);
                mIsOnkey = false;
                return true;
            } catch (Exception e) {
            } finally {
                mIsOnkey = false;
            }
        }
        return false;
    }

    @Override
    public String getSelfPhoneNumnber() {
        TelephonyManager phoneMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String number = phoneMgr.getLine1Number();
        return number;
    }

    @Override
    public boolean isSecurityNumber(String number) {
        return false;
    }

    @Override
    public String getPhoneSecurityNumber() {
        String number = PreferenceTable.getInstance().getString(PrefConst.KEY_PHONE_SECURITY_TELPHONE_NUMBER);
        return number;
    }

    @Override
    public List<String> getActivateInstructs() {
        boolean isOpenAdvancePro = isOpenAdvanceProtect();
        List<String> instructs = new ArrayList<String>();
        if (isOpenAdvancePro) {
            instructs = getPhoneSecurityInstructionSets();
        } else {
            instructs.add(SecurityInstructSet.LOCATEPOSITION);
            instructs.add(SecurityInstructSet.ALERT);
            instructs.add(SecurityInstructSet.LOCKMOBILE);
            instructs.add(SecurityInstructSet.OFFALERT);
        }
        return instructs;
    }

    @Override
    public int getMaxScore() {
        return MAX_SCORE;
    }

    @Override
    public boolean getIsSimChange() {
        if (isUsePhoneSecurity()) {
            //判断sim卡是否发生了变化
            TelephonyManager teleManger = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            String currentSimIMEI = teleManger.getSimSerialNumber();
            String imei = PreferenceTable.getInstance().getString(PrefConst.KEY_SIM_IMEI);
            if (!Utilities.isEmpty(imei)) {
                if (!imei.equals(currentSimIMEI)) {
                    //sim卡发生了变化
                    PrivacyContactManagerImpl mgr = (PrivacyContactManagerImpl) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
                    /*获取机型*/
                    String model = BuildProperties.getPoneModel();
                    String body = mContext.getResources().getString(R.string.sim_change_msm_tip, model);
                    String sendNumber = null;
                    String name_number = getPhoneSecurityNumber();
                    if (!Utilities.isEmpty(name_number)) {
                        String[] numbers = name_number.split(":");
                        sendNumber = numbers[1];
                    }
                    LeoLog.i(TAG, "SIM卡更换发送短信！");
                    LostSecurityManagerImpl manager = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
                    manager.setSimIMEI();
                    boolean result = mgr.sendMessage(sendNumber, body);
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft_use", "theft_use_SIM");
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean setSimIMEI() {
        try {
            TelephonyManager teleManger = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            String currentSimIMEI = teleManger.getSimSerialNumber();
            String imeiBefor = PreferenceTable.getInstance().getString(PrefConst.KEY_SIM_IMEI);
            if (!Utilities.isEmpty(imeiBefor)) {
                if (!imeiBefor.equals(currentSimIMEI)) {
                    LeoLog.i(TAG, "保存sim卡IMEI：" + currentSimIMEI);
                    PreferenceTable.getInstance().putString(PrefConst.KEY_SIM_IMEI, currentSimIMEI);
                }
            } else {
                LeoLog.i(TAG, "保存sim卡IMEI：" + currentSimIMEI);
                PreferenceTable.getInstance().putString(PrefConst.KEY_SIM_IMEI, currentSimIMEI);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean getIsExistSim() {
        /*判断是否为飞行模式*/
        int arplaneMode = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0);
        boolean isAirplaneMode = false;

        if (arplaneMode == 1) {
            /*为飞行模式*/
            isAirplaneMode = true;
        } else {
            /*不为为飞行模式*/
            isAirplaneMode = false;
        }
        return new SimDetecter(mContext).isSimReady();
    }

    private class SecurLocateListener implements LocationListener {
        private LocationManager locationManager;

        public SecurLocateListener(LocationManager locationManager) {
            this.locationManager = locationManager;
        }

        @Override
        public void onLocationChanged(Location location) {
//            Toast.makeText(mContext, "onLocationChanged", Toast.LENGTH_SHORT).show();
            updateToNewLocation(location, locationManager);
        }


        @Override
        public void onProviderDisabled(String provider) {
//            Toast.makeText(mContext, "onProviderDisabled", Toast.LENGTH_SHORT).show();
            updateToNewLocation(null, null);
        }

        @Override
        public void onProviderEnabled(String provider) {
//            Toast.makeText(mContext, "onProviderEnabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
//            Toast.makeText(mContext, "onStatusChanged", Toast.LENGTH_SHORT).show();
        }
    }

    /*通过location获取当前设备的具体位置*/
    private void updateToNewLocation(Location location, LocationManager locateManager) {
        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            LeoLog.i(TAG, "经度：" + lng + "纬度：" + lat);
            if (locateManager != null) {
                if (mLocationListener != null) {
                    locateManager.removeUpdates(mLocationListener);
                    LeoLog.i(TAG, "updateToNewLocation移除位置监听");
                }
            }
                PhoneSecurityManager.getInstance(mContext).executeLockLocateposition(null, false);
        }
    }

}
