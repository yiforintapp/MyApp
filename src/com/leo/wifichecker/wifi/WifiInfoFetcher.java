
package com.leo.wifichecker.wifi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;

import com.leo.appmaster.utils.LeoLog;
import com.leo.wifichecker.Location.LocationMgr;
import com.leo.wifichecker.utils.LogEx;

/**
 * Created by luqingyuan on 15/9/7.
 */
public class WifiInfoFetcher {
    private static final String TAG = "WifiInfoFetcher";
    private WifiScanner mScanner;
    private WifiParser mParser;
    private LocationMgr mLocationMgr;
    private WifiFetcherListener mListener;
    private Context mContext;
    private Handler mMainHandler;
    private static final String SAVE_FILE_NAME = "wifi_info";
    private static final String PREF_NAME = "wifiInfo";
    private static final int MAX_INFO_NUM = 300;
    private static WifiInfoFetcher mInstance;

    private Hashtable<String, APInfo> mApInfos;
    private List<APInfo> mChangedInfos;

    /**
     * ap信息变化监听
     */
    public interface WifiFetcherListener {
        void onWifiChanged(List<APInfo> results);
    }

    /**
     * ap信息变化监听
     */
    interface InnerFetcherListener {
        /**
         * 从parser中返回的结果
         */
        public static final int INFO_FROM_WIFI_PARSER = 1;
        /**
         * 从scanner中返回的结果
         */
        public static final int INFO_FROM_WIFI_SCANNER = 2;

        void onWifiChanged(List<APInfo> results, int from);
    }

    /**
     * 获取实例
     * 
     * @return
     */
    public static WifiInfoFetcher getInstance() {
        if (mInstance == null) {
            synchronized (WifiInfoFetcher.class) {
                if (mInstance == null) {
                    mInstance = new WifiInfoFetcher();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化
     * 
     * @param context
     * @param lis WifiFetcherListener
     */
    public void init(Context context, WifiFetcherListener lis) {
        LogEx.enter();
        mListener = lis;
        mContext = context.getApplicationContext();
        mMainHandler = new Handler(mContext.getMainLooper());
        // 恢复数据
        mApInfos = restoreAPInfo(context, SAVE_FILE_NAME, PREF_NAME, Context.MODE_PRIVATE);
        // LogEx.d(TAG, "restoreAPInfo" + mApInfos);
        LogEx.leave();
    }

    /**
     * 是否打开log
     * 
     * @param enable 默认false
     */
    public void enableDebug(boolean enable) {
        LogEx.DEBUG_ENABLE = enable;
    }

    /**
     * 设置地理位置信息更新时间间隔
     * 
     * @param interval 时间间隔,默认1分钟
     */
    public void setMinDistanceUpdateInterval(long interval) {
        LocationMgr.MIN_UPDATE_TIME = interval;
    }

    private WifiInfoFetcher() {
    }

    /**
     * 获取ap信息
     * 
     * @return
     */
    public List<APInfo> getApInfoList() {
        if (mApInfos == null) {
            return null;
        }
        return new ArrayList<APInfo>(mApInfos.values());
    }

    /**
     * 获取数据有更新的APInfoList，即增量数据， 需要与afterUpload成对调用
     * 
     * @return List
     */
    public List<APInfo> prepareUploadData() {
        if (mChangedInfos == null) {
            mChangedInfos = new LinkedList<APInfo>();
            for (APInfo info : mApInfos.values()) {
                if (info.mIsChanged) {
                    LeoLog.d("testNewData", info.toString());
                    mChangedInfos.add(info);
                }
            }
        }
        return mChangedInfos;
    }

    /**
     * 增量数据上传完毕，改变数据状态， 需要与prepareUploadData成对调用
     * 
     * @param success 是否成功
     */
    public void afterUpload(boolean success) {
        if (success) {
            if (mChangedInfos != null && mChangedInfos.size() > 0) {
                for (APInfo info : mChangedInfos) {
                    info.mIsChanged = false;
                }
                saveAPInfo(mContext, SAVE_FILE_NAME, PREF_NAME, mApInfos, Context.MODE_PRIVATE);
                mChangedInfos.clear();
            }
        }
        mChangedInfos = null;
    }

    /**
     * 开始获取数据
     */
    public void start() {
        LogEx.enter();
        if (mLocationMgr == null) {
            mLocationMgr = new LocationMgr(mContext);
            mLocationMgr.start();
        }

        checkWifiConfiguration();
        if (mScanner == null) {
            mScanner = new WifiScanner(mContext, mInnerListener);
            mScanner.startScan(mContext);
        }
        if (mParser == null) {
            mParser = new WifiParser(mInnerListener);
            mParser.startParse();
        }
        LogEx.leave();
    }

    /**
     * 停止获取数据
     */
    public void stop() {
        LogEx.enter();
        if (mScanner != null) {
            mScanner.stopScan(mContext);
            mScanner = null;
        }

        if (mLocationMgr != null) {
            mLocationMgr.stop();
            mLocationMgr = null;
        }
        mParser = null;
        LogEx.leave();
    }

    public LocationMgr getLocationMgr() {
        return mLocationMgr;
    }

    InnerFetcherListener mInnerListener = new InnerFetcherListener() {
        @Override
        public void onWifiChanged(final List<APInfo> results, final int from) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (results != null) {
                        boolean canAdd = (from == InnerFetcherListener.INFO_FROM_WIFI_PARSER);
                        mergeApInfo(results, canAdd);
                    }
                    if (mListener != null) {
                        mListener.onWifiChanged(getApInfoList());
                    }
                }
            });
        }
    };

    private void mergeApInfo(final List<APInfo> newData, boolean canAdd) {
        if (newData == null) {
            return;
        }
        LogEx.enter();
        if (mApInfos == null) {
            mApInfos = new Hashtable<String, APInfo>();
        }
        Location currentLoc = mLocationMgr.getLastLocation();

        boolean hasChanged = false;
        for (APInfo info : newData) {
            if (info == null || info.mSSID == null) {
                continue;
            }
            if (currentLoc != null) {
                info.mLatitude = currentLoc.getLatitude();
                info.mLongitude = currentLoc.getLongitude();
                if (currentLoc.hasAccuracy()) {
                    info.mAccuracy = currentLoc.getAccuracy();
                }
            }

            String prefkey = info.mSSID;
            APInfo oldInfo = mApInfos.get(prefkey);

            if (oldInfo != null) {
                boolean ret = updateInfo(oldInfo, info);
                if (!hasChanged) {
                    hasChanged = ret;
                }
            } else if (mApInfos.size() < MAX_INFO_NUM) {
                // 需要上传的数据有三类：
                // 1、破解出密码的ap
                // 2、之前用户配置过的ap, 不管能否获取密码
                // 3、扫描到的开放的ap，不管用户有无配置过
                if (canAdd || info.mSecLevel == APInfo.SEC_OPEN) {
                    hasChanged = true;
                    mApInfos.put(prefkey, info);
                }
            }
        }
        if (hasChanged) {
            saveAPInfo(mContext, SAVE_FILE_NAME, PREF_NAME, mApInfos, Context.MODE_PRIVATE);
        }
        LogEx.leave();
    }

    private void checkWifiConfiguration() {
        LogEx.enter();
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> networks = wifiManager.getConfiguredNetworks();
        if (networks == null) {
            return;
        }
        List<APInfo> infoList = new ArrayList<APInfo>();
        Iterator<WifiConfiguration> iterator = networks.iterator();
        while (iterator.hasNext()) {
            WifiConfiguration localWifiConfiguration = iterator.next();
            APInfo info = new APInfo();
            info.mSSID = APInfo.stripLeadingAndTrailingQuotes(localWifiConfiguration.SSID);
            info.setmKeyMgmtByWifiConf(localWifiConfiguration);
            infoList.add(info);
        }
        mergeApInfo(infoList, true);
        LogEx.leave();
    }

    /**
     * 更新数据,放在这里，因为APInfo需要混淆
     * 
     * @param newInfo
     * @return
     */
    private boolean updateInfo(APInfo oldInfo, APInfo newInfo) {
        boolean changed = false;
        if (!TextUtils.isEmpty(newInfo.mSSID)
                && !newInfo.mSSID.equals(oldInfo.mSSID)) {
            oldInfo.mSSID = newInfo.mSSID;
            changed = true;
        }
        if (!TextUtils.isEmpty(newInfo.mKeyMgmt)
                && !newInfo.mKeyMgmt.equals(oldInfo.mKeyMgmt)) {
            oldInfo.mKeyMgmt = newInfo.mKeyMgmt;
        }
        if (newInfo.mSecLevel != oldInfo.mSecLevel) {
            oldInfo.mSecLevel = newInfo.mSecLevel;
            changed = true;
        }
        if (!TextUtils.isEmpty(newInfo.mPassword)
                && !newInfo.mPassword.equals(oldInfo.mPassword)) {
            oldInfo.mPassword = newInfo.mPassword;
            changed = true;
        }
        if (!TextUtils.isEmpty(newInfo.mEap)
                && !newInfo.mEap.equals(oldInfo.mEap)) {
            oldInfo.mEap = newInfo.mEap;
            changed = true;
        }
        if (!TextUtils.isEmpty(newInfo.mIdentity)
                && !newInfo.mIdentity.equals(oldInfo.mIdentity)) {
            oldInfo.mIdentity = newInfo.mIdentity;
            changed = true;
        }
        if (!TextUtils.isEmpty(newInfo.mOtherSettings)
                && !newInfo.mOtherSettings.equals(oldInfo.mOtherSettings)) {
            oldInfo.mOtherSettings = newInfo.mOtherSettings;
            changed = true;
        }
        if (TextUtils.isEmpty(oldInfo.mBSSID) &&
                !TextUtils.isEmpty(newInfo.mBSSID)) {
            oldInfo.mBSSID = newInfo.mBSSID;
            changed = true;
        }

        if (!TextUtils.isEmpty(newInfo.mCapabilities)
                && !newInfo.mCapabilities.equals(oldInfo.mCapabilities)) {
            oldInfo.mCapabilities = newInfo.mCapabilities;
            changed = true;
        }

        if (newInfo.mAccuracy != 0
                &&
                (oldInfo.mAccuracy == 0
                        || newInfo.mLevel > oldInfo.mLevel
                        || (newInfo.mLevel == oldInfo.mLevel && newInfo.mAccuracy < oldInfo.mAccuracy))) {
            // 检查定位精度
            oldInfo.mLevel = newInfo.mLevel;
            oldInfo.mLatitude = newInfo.mLatitude;
            oldInfo.mLongitude = newInfo.mLongitude;
            oldInfo.mAccuracy = newInfo.mAccuracy;
            changed = true;
        }
        if (!oldInfo.mIsChanged) {
            oldInfo.mIsChanged = changed;
        }
        return changed;
    }

    private void saveAPInfo(Context context, String prefFileName, String name,
            Hashtable<String, APInfo> value, int mode) {
        LogEx.enter();
        SharedPreferences preference = context.getSharedPreferences(prefFileName, mode);
        SharedPreferences.Editor editor = preference.edit();
        // 将map转换为byte[]
        ByteArrayOutputStream toByte = null;
        try {
            toByte = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(toByte);
            oos.writeObject(value);
            // 对byte[]进行Base64编码
            String mapBase64 = new String(Base64.encode(toByte.toByteArray(), Base64.DEFAULT));
            // 存储
            editor.putString(name, mapBase64);
            editor.commit();
        } catch (IOException e) {
        }
    }

    private Hashtable<String, APInfo> restoreAPInfo(Context context, String prefFileName,
            String name, int mode) {
        LogEx.enter();
        SharedPreferences preference = context.getSharedPreferences(prefFileName, mode);
        String value = preference.getString(name, null);
        if (value != null) {
            byte[] base64Bytes = Base64.decode(value, Base64.DEFAULT);
            ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
            Hashtable<String, APInfo> data = null;
            try {
                ObjectInputStream ois = new ObjectInputStream(bais);
                data = (Hashtable<String, APInfo>) ois.readObject();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return data;
        }
        return null;
    }
}
