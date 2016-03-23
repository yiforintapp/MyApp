package com.leo.appmaster.mgr.impl;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.receiver.DeviceReceiver;
import com.leo.appmaster.applocker.receiver.DeviceReceiverNewOne;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.LostSecurityManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.phoneSecurity.MTKSendMsmHandler;
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

import org.apache.http.client.utils.URLEncodedUtils;
import org.w3c.dom.Text;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LostSecurityManagerImpl extends LostSecurityManager {
    public static final Boolean DBG = true;
    public static final String TAG = "LostSecurityManagerImpl";
    private static final long WAIT_TIME_OUT = 5 * 1000;
    private static boolean mIsLocation;
    private static boolean mIsOnkey;
    private static boolean mIsFormate;
    private static boolean mIsLock;
    private static boolean mIsAlert;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LostSecurityManagerImpl mLostImpl;

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
                return PhoneSecurityConstants.ADD_SECUR_NUMBER_FAIL;
            }
            String selfNumber = getSelfPhoneNumnber();
            if (!Utilities.isEmpty(selfNumber)) {
                String formateSelfNumber = PrivacyContactUtils.formatePhoneNumber(selfNumber);
                /*查询加入的手机防盗号码是否为本机号码*/
                if (contact.getContactNumber().contains(formateSelfNumber)) {
                    return PhoneSecurityConstants.ADD_SECUR_NUMBER_SELT;
                }
            }
            String name = contact.getContactName();
            StringBuilder phone = new StringBuilder();
            phone.append(name);
            phone.append(":");
            phone.append(contact.getContactNumber());
            PreferenceTable.getInstance().putString(PrefConst.KEY_PHONE_SECURITY_TELPHONE_NUMBER, phone.toString());
            return PhoneSecurityConstants.ADD_SECUR_NUMBER_SUCESS;
        } catch (Exception e) {

        }
        return PhoneSecurityConstants.ADD_SECUR_NUMBER_FAIL;
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
    public synchronized Location getLocation(int fromId) {

        //TODO
        StringBuilder sb = new StringBuilder();
        if (DBG) {
            //测试需要：本地存储卡中查看当前获取位置信息的情况
            if (fromId == 0) {
                sb.append(System.currentTimeMillis() + ",本次请求为：sdkwrapper上报请求");
            } else if (fromId == 1) {
                sb.append(System.currentTimeMillis() + ",本次请求为：发送短信获取位置请求");
            } else if (fromId == 2) {
                sb.append(System.currentTimeMillis() + ",本次请求为：反向地理解析位置请求");
            } else if (fromId == 3) {
                sb.append(System.currentTimeMillis() + ",本次请求为：上报服务器位置请求");
            }
            sb.append("\n");
        }
        Location location = null;
        boolean isGoogleAva = false;
        LeoLog.d(TAG, "start get location");
        mLostImpl = this;
        int gpCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        //Google play service
        switch (gpCode) {
            case ConnectionResult.SUCCESS:
                LeoLog.d(TAG, "googley play service can connect,try use google play service ...");
                isGoogleAva = true;
                break;
            default:
                LeoLog.d(TAG, "googley play service no can connect,tyr use location manager ...");
                break;
        }
        if (isGoogleAva) {
            try {
                mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                LeoLog.d(TAG, "google play onConnected,GoogleApiClient is null:" + (mGoogleApiClient == null));
                                if (mGoogleApiClient != null) {
                                    mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                                    if (mLocation != null) {
                                        LeoLog.d(TAG, "google_api,latitude:" + mLocation.getLatitude() + ";google_api,longitude:" + mLocation.getLongitude());
                                        LeoLog.d(TAG, "本次位置信息获取使用Google play service");

                                    } else {
                                        LeoLog.d(TAG, "google_api get location null!");
                                    }
                                }
                                synchronized (mLostImpl) {
                                    mLostImpl.notify();
                                }
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                LeoLog.d(TAG, "disconnection ---onConnectionSuspended");
                            }
                        })
                        .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(ConnectionResult connectionResult) {
                                LeoLog.d(TAG, "googley play service location fail...");
                                synchronized (mLostImpl) {
                                    mLostImpl.notify();
                                }
                            }
                        })
                        .addApi(LocationServices.API)
                        .build();
                mGoogleApiClient.connect();

            } catch (Exception e) {
                e.printStackTrace();
            }

            synchronized (mLostImpl) {
                try {
                    mLostImpl.wait(WAIT_TIME_OUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            location = mLocation;
        }

        //TODO
        if (DBG) {
            if (mLocation != null) {
                sb.append(System.currentTimeMillis() + ",本次位置信息获取使用Google play service\n");
                sb.append(System.currentTimeMillis() + ",位置信息：google_api,latitude:" + mLocation.getLatitude() + ";google_api,longitude:" + mLocation.getLongitude() + "\n");
            }
        }


        if (mLocation == null) {
            /*可根据设备状况动态选择location provider*/
            PhoneSecurityManager psm = PhoneSecurityManager.getInstance(mContext);
            LocationManager locationManager = psm.getLocationManager();
            if (locationManager == null) {
                locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
                psm.setLocationManager(locationManager);
            }

            String provider = PhoneSecurityUtils.getLocateProvider(locationManager);
            LeoLog.i(TAG, "google play service no get location,use location manager provider:" + provider);
            if (!TextUtils.isEmpty(provider) && locationManager.isProviderEnabled(provider)) {
                for (int i = 0; i < 5; i++) {
                    location = locationManager.getLastKnownLocation(provider);
                    if (location != null) {
                        break;
                    }
                }
            }

            //TODO
            if (DBG) {
                sb.append(System.currentTimeMillis() + ",本次位置信息获取使用LocationManager,provider=" + provider + "\n");
            }
            if (location != null) {
                LeoLog.i(TAG, "location ,location.getLatitude():" + location.getLatitude() + ",location.getLongitude" + location.getLongitude());
                LeoLog.d(TAG, "本次位置信息获取使用LocationManager,provider=" + provider);

                //TODO
                if (DBG) {
                    sb.append(System.currentTimeMillis() + ",location ,location.getLatitude():" + location.getLatitude() + ",location.getLongitude" + location.getLongitude() + "\n");
                }
            }
        }
        if (mGoogleApiClient != null) {
            LeoLog.i(TAG, "mGoogleApiClient.isConnected():" + mGoogleApiClient.isConnected());
            //初始化GoogleApiClient
            if (mGoogleApiClient.isConnected()) {
                LeoLog.i(TAG, "GoogleApiClient connected,excute disconnect... ");
                mGoogleApiClient.disconnect();
                try {
                    mGoogleApiClient.unregisterConnectionCallbacks(null);
                    mGoogleApiClient.unregisterConnectionFailedListener(null);
                } catch (Exception e) {
                } catch (Error e1) {
                }
                mGoogleApiClient = null;
            }
        }
        mLocation = null;
//        mLostImpl = null;
        if (location == null) {
            LeoLog.d(TAG, "本次位置信息获取没有结果！");
            LeoLog.i(TAG, "location is null ! ");

            //TODO
            if (DBG) {
                sb.append(System.currentTimeMillis() + ",本次位置信息获取没有结果！\n");
            }
        } else {
            LeoLog.i(TAG, "location  no is null !");

            //TODO
            if (DBG) {
                sb.append(System.currentTimeMillis() + ",http://www.google.com/maps?mrt=loc&q=" + location.getLatitude() + "%2C" + location.getLongitude());
            }
        }
        if (DBG) {
            final StringBuilder s = sb;
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    PhoneSecurityUtils.writeToFile(s.toString());
                }
            });
        }

        return location;
    }

    @Override
    public boolean isOpenAdvanceProtect() {
        DevicePolicyManager manager = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName mAdminName = new ComponentName(mContext, DeviceReceiver.class);
        ComponentName mAdminName2 = new ComponentName(mContext, DeviceReceiverNewOne.class);
        if (manager.isAdminActive(mAdminName) || manager.isAdminActive(mAdminName2)) {
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
    public boolean executeLockLocateposition(String number, boolean isExecuNoMsm, boolean otherFlag) {

        long cu1 = System.currentTimeMillis();
        LeoLog.d("getTime", "executeLockLocateposition:" + mIsLocation);
        if (!mIsLocation || otherFlag) {
            try {
                LeoLog.i(TAG, "执行位置追踪指令");
                mIsLocation = true;
                String sendNumber = null;
                if (Utilities.isEmpty(number)) {
                /*如果不指定手机号，默认给防盗号码发送位置信息*/
                    String nameAndnumber = getPhoneSecurityNumber();
                    if (!Utilities.isEmpty(nameAndnumber)) {
                        String[] numbers = nameAndnumber.split(":");
                        if (numbers.length >= 2) {
                            sendNumber = numbers[1];
                        }
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
                if (!isExecuNoMsm) {
                    googleMapUri = PhoneSecurityManager.getInstance(mContext).securLocateHandler();
                }

                long cu2 = System.currentTimeMillis();
                LeoLog.d("getTime", "获取经纬度耗时：" + (cu2 - cu1));

                long cu3 = System.currentTimeMillis();

                if (!Utilities.isEmpty(googleMapUri)) {
                    locatePositionMsm = mContext.getResources().getString(R.string.secur_location_msm, googleMapUri);
                    LeoLog.i(TAG, "执行位置URL=" + locatePositionMsm);
                    try {
                        PhoneSecurityManager.getInstance(mContext).setLocalMsm(new StringBuilder(locatePositionMsm));
                        PrivacyContactManagerImpl mgr = (PrivacyContactManagerImpl) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
                        mgr.sendMessage(sendNumber, locatePositionMsm, MTKSendMsmHandler.SEND_LOCAL_MSM_ID);
                        LeoLog.d("getTime", "msm content：" + locatePositionMsm);
                        mIsLocation = false;
                        return true;
                    } catch (Exception e) {
                    }
                } else {
                    if (isExecuNoMsm) {
                        String noLocation = mContext.getResources().getString(R.string.secur_send_msm_no_location);
                        locatePositionMsm = noLocation;
                        try {
                            PhoneSecurityManager.getInstance(mContext).setLocalMsm(new StringBuilder(noLocation));
                            PrivacyContactManagerImpl mgr = (PrivacyContactManagerImpl) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
                            mgr.sendMessage(sendNumber, locatePositionMsm, MTKSendMsmHandler.SEND_LOCAL_MSM_ID);
                            LeoLog.d("getTime", "msm content：" + locatePositionMsm);
                            mIsLocation = false;
                            return true;
                        } catch (Exception e) {
                        }
                    }
                }
                LeoLog.d("getTime", "获取经纬度后发送短信时间：" + (cu3 - cu2));
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            } finally {
                mIsLocation = false;
            }
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
                            lockManager.removePkgFromMode(addLockList, mode, false);
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
                if (mAudioManager != null) {
                    try {
                        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
                        LeoLog.i(TAG, "最大音量：" + maxVolume + ",当前音量：" + currentVolume);
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
                    } catch (IllegalArgumentException e) {
                        // AM-3785
                        e.printStackTrace();
                    }
                }
                /*获取系统铃声*/
                Uri sysSingUri = RingtoneManager.getActualDefaultRingtoneUri(mContext,
                        RingtoneManager.TYPE_RINGTONE);
                PhoneSecurityManager pm = PhoneSecurityManager.getInstance(mContext);
                if (pm.getMediaPlayer() == null) {
                    pm.setMediaPlayer(MediaPlayer.create(mContext, sysSingUri));
                }
                MediaPlayer media = pm.getMediaPlayer();
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
                if (PhoneSecurityManager.getInstance(mContext).isStopAlert()) {
                    alertMode = false;
                    PhoneSecurityManager.getInstance(mContext).setStopAlert(false);
                }
            }
            /*一直响铃*/
            while (alertMode) {
                LeoLog.i(TAG, "执行警报指令");
                AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                //系统音乐音量
                try {
                    int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
                    LeoLog.i(TAG, "最大音量：" + maxVolume + ",当前音量：" + currentVolume);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
                } catch (IllegalArgumentException e) {
                    // AM-3785
                    e.printStackTrace();
                }
                /*获取系统铃声*/
                Uri sysSingUri = RingtoneManager.getActualDefaultRingtoneUri(mContext,
                        RingtoneManager.TYPE_RINGTONE);
                PhoneSecurityManager pm = PhoneSecurityManager.getInstance(mContext);
                if (pm.getMediaPlayer() == null) {
                    pm.setMediaPlayer(MediaPlayer.create(mContext, sysSingUri));
                }
                MediaPlayer media = pm.getMediaPlayer();
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
                if (PhoneSecurityManager.getInstance(mContext).isStopAlert()) {
                    alertMode = false;
                    PhoneSecurityManager.getInstance(mContext).setStopAlert(false);
                    executeStopAlert(true);
                    break;
                }
            }
        }

        return false;
    }

    @Override
    public boolean executeStopAlert(boolean isStopAlert) {
        PhoneSecurityManager psm = PhoneSecurityManager.getInstance(mContext);
        MediaPlayer media = psm.getMediaPlayer();
        if (isStopAlert && media != null) {
            media.stop();
            psm.setMediaPlayer(null);
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
                boolean resultPostion = executeLockLocateposition(null, false, false);
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
        return PhoneSecurityConstants.MAX_SCORE;
    }

    @Override
    public boolean getIsSimChange() {
        if (isUsePhoneSecurity()) {
            /*判断sim卡是否发生了变化*/
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
                    boolean result = mgr.sendMessage(sendNumber, body,MTKSendMsmHandler.SIM_CHANAGE_ID);
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
        return SimDetecter.isSimReady(mContext);
    }


}
