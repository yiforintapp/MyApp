
package com.leo.appmaster.quickgestures;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockModeActivity;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.ClickQuickItemEvent;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.quickgestures.ui.QuickGestureActivity;
import com.leo.appmaster.utils.LeoLog;

public class QuickSwitchManager {

    private static QuickSwitchManager mInstance;
    public final static String BLUETOOTH = "bluetooth";
    public final static String FLASHLIGHT = "flashlight";
    public final static String WLAN = "wlan";
    public final static String CRAME = "carme";
    public final static String SOUND = "sound";
    public final static String LIGHT = "light";
    public final static String MSM_PACKAGE_NAME = "com.android.contacts";
    public final static String SPEEDUP = "speedup";
    public final static String CHANGEMODE = "changemode";
    public final static String SWITCHSET = "switchset";
    public final static String SETTING = "setting";
    public final static String GPS = "gps";
    public final static String FLYMODE = "flymode";
    public final static String ROTATION = "rotation";
    public final static String MOBILEDATA = "mobiledata";
    public final static String HOME = "home";
    public final static String XUKUANG = "xukuang";
    public final static String SYS_NO_READ_MESSAGE_TIP = "sys_no_read_message_tip";
    public final static String SYS_NO_READ_CALL_LOG_TIP = "sys_no_read_call_log_tip";
    public final static String PRIVACY_NO_READ_CONTACT_TIP = "privacy_no_read_contact_tip";
    public final static String BLUETOOTH_EVENT = "bluetooth_event";
    private Context mContext;
    private static BluetoothAdapter mBluetoothAdapter;
    private WifiManager mWifimanager;
    public Camera mCamera;
    private AudioManager mSoundManager;
    private PowerManager mPowerManager;
    public static boolean isBlueToothOpen = false;
    public static boolean isFlashLightOpen = false;
    public static boolean isWlantOpen = false;
    public static boolean isGpsOpen = false;
    public static boolean isFlyModeOpen = false;
    public static boolean isRotationOpen = false;
    public static boolean isMobileDataOpen = false;
    private static int mSoundStatus;
    public final static int mSound = 0;
    public final static int mQuite = 1;
    public final static int mVibrate = 2;
    private static int mLightStatus;
    public static final int LIGHT_ERR = -1;
    public static final int LIGHT_AUTO = 0;
    public static final int LIGHT_NORMAL = 64;
    public static final int LIGHT_50_PERCENT = 127;
    public static final int LIGHT_100_PERCENT = 255;
    private RotationObserver mRotationObserver;
    private BrightObserver mBrightObserver;
    private GpsObserver mGpsObserver;
    private Vibrator vib;
    private LocationManager locationManager;
    private int isAirplaneMode;
    private int mRotateState;
    private ConnectivityManager mConnectivityManager;
    private Handler mHandler;
    private AppMasterPreference switchPreference;
    private boolean mSwitcherLoaded = false;
    private List<BaseInfo> mSaveList = null;
    private List<BaseInfo> mAllList;

    public static synchronized QuickSwitchManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new QuickSwitchManager(context);
        }
        return mInstance;
    }

    private QuickSwitchManager(Context context) {
        mContext = context.getApplicationContext();
        vib = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
        switchPreference = AppMasterPreference.getInstance(mContext);

        // 打开前判断每一个的状态
        blueTooth();
        wlan();
        sound();
        lightPower();
        gps();
        flyMode();
        rotation();
        mobileData();

        mHandler = new Handler();
        // 屏幕旋转观察者
        mRotationObserver = new RotationObserver(mHandler);
        mRotationObserver.startObserver();
        // 屏幕亮度观察者
        mBrightObserver = new BrightObserver(mHandler);
        mBrightObserver.startObserver();
        // GPS观察者
        mGpsObserver = new GpsObserver(mHandler);
        mGpsObserver.startObserver();
    }

    public void mobileData() {
        if (mConnectivityManager == null) {
            mConnectivityManager = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        try {
            boolean isMobileDataEnable = invokeMethod("getMobileDataEnabled",
                    null);
            if (isMobileDataEnable) {
                isMobileDataOpen = true;
            } else {
                isMobileDataOpen = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rotation() {
        try {
            mRotateState = android.provider.Settings.System.getInt(mContext.getContentResolver(),
                    android.provider.Settings.System.ACCELEROMETER_ROTATION);
            // 观察屏幕旋转开关
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mRotateState == 1) {
            isRotationOpen = true;
        } else {
            isRotationOpen = false;
        }
    }

    public void flyMode() {
        isAirplaneMode = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0);
        if (isAirplaneMode == 1) {
            isFlyModeOpen = true;
        } else {
            isFlyModeOpen = false;
        }
    }

    private void gps() {
        if (locationManager == null) {
            locationManager = (LocationManager) mContext
                    .getSystemService(Context.LOCATION_SERVICE);
        }
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            isGpsOpen = true;
        } else {
            isGpsOpen = false;
        }
    }

    public void sound() {
        if (mSoundManager == null) {
            mSoundManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        }
        if (mSoundManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            mSoundStatus = 0;
        } else if (mSoundManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
            mSoundStatus = 1;
        } else if (mSoundManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            mSoundStatus = 2;
        }
    }

    public void wlan() {
        if (mWifimanager == null) {
            mWifimanager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        }
        if (mWifimanager.isWifiEnabled()) {
            isWlantOpen = true;
        } else {
            isWlantOpen = false;
        }
    }

    public void blueTooth() {
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter
                    .getDefaultAdapter();
        }
        if (mBluetoothAdapter.isEnabled()) {
            isBlueToothOpen = true;
        } else {
            isBlueToothOpen = false;
        }
    }

    private void lightPower() {
        if (mPowerManager == null) {
            mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        }
        refreshLightButton();
    }

    // 更新按钮
    private void refreshLightButton()
    {
        switch (getBrightStatus())
        {
            case LIGHT_AUTO:
                mLightStatus = LIGHT_AUTO;
                break;
            case LIGHT_NORMAL:
                mLightStatus = LIGHT_NORMAL;
                break;
            case LIGHT_50_PERCENT:
                mLightStatus = LIGHT_50_PERCENT;
                break;
            case LIGHT_100_PERCENT:
                mLightStatus = LIGHT_100_PERCENT;
                break;
            case LIGHT_ERR:
                mLightStatus = LIGHT_ERR;
                break;
        }
    }

    // 得到当前亮度值状态
    private int getBrightStatus() {
        int light = 0;
        boolean auto = false;
        ContentResolver cr = mContext.getContentResolver();
        try {
            auto = Settings.System.getInt(cr,
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            if (!auto) {
                light = android.provider.Settings.System.getInt(cr,
                        Settings.System.SCREEN_BRIGHTNESS, -1);
                if (light > 0 && light <= LIGHT_NORMAL) {
                    return LIGHT_NORMAL;
                } else if (light > LIGHT_NORMAL && light <= LIGHT_50_PERCENT) {
                    return LIGHT_50_PERCENT;
                } else if (light > LIGHT_50_PERCENT && light <= LIGHT_100_PERCENT) {
                    return LIGHT_100_PERCENT;
                }
            } else {
                return LIGHT_AUTO;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return LIGHT_ERR;
    }

    public Drawable[] getIconFromName(String IdentiName) {
        Drawable[] iCons = null;
        if (IdentiName.equals(getLabelFromName(BLUETOOTH))) {
            iCons = new Drawable[2];
            iCons[0] = mContext.getResources().getDrawable(
                    R.drawable.switch_bluetooth_pre);
            iCons[1] = mContext.getResources().getDrawable(R.drawable.switch_bluetooth);
        } else if (IdentiName.equals(getLabelFromName(FLASHLIGHT))) {
            iCons = new Drawable[2];
            iCons[0] = mContext.getResources().getDrawable(
                    R.drawable.switch_flashlight_pre);
            iCons[1] = mContext.getResources().getDrawable(
                    R.drawable.switch_flashlight);
        } else if (IdentiName.equals(getLabelFromName(WLAN))) {
            iCons = new Drawable[2];
            iCons[0] = mContext.getResources().getDrawable(R.drawable.switch_wifi_pre);
            iCons[1] = mContext.getResources().getDrawable(R.drawable.switch_wifi);
        } else if (IdentiName.equals(getLabelFromName(CRAME))) {
            iCons = new Drawable[1];
            iCons[0] = mContext.getResources().getDrawable(R.drawable.switch_camera);
        } else if (IdentiName.equals(getLabelFromName(SOUND))) {
            iCons = new Drawable[3];
            iCons[0] = mContext.getResources().getDrawable(R.drawable.switch_volume_min);
            iCons[1] = mContext.getResources()
                    .getDrawable(R.drawable.switch_volume_mute);
            iCons[2] = mContext.getResources().getDrawable(
                    R.drawable.switch_volume_vibration);
        } else if (IdentiName.equals(getLabelFromName(LIGHT))) {
            iCons = new Drawable[4];
            iCons[0] = mContext.getResources().getDrawable(
                    R.drawable.switch_brightness_automatic);
            iCons[1] = mContext.getResources()
                    .getDrawable(R.drawable.switch_brightness_min);
            iCons[2] = mContext.getResources().getDrawable(
                    R.drawable.switch_brightness_half);
            iCons[3] = mContext.getResources().getDrawable(
                    R.drawable.switch_brightness_max);
        } else if (IdentiName.equals(getLabelFromName(SPEEDUP))) {
            iCons = new Drawable[2];
            iCons[0] = mContext.getResources().getDrawable(R.drawable.switch_speed_up);
            iCons[1] = mContext.getResources().getDrawable(
                    R.drawable.gesture_rocket_bg);
        } else if (IdentiName.equals(getLabelFromName(SWITCHSET))) {
            iCons = new Drawable[2];
            iCons[0] = mContext.getResources().getDrawable(
                    R.drawable.switch_set);
            iCons[1] = mContext.getResources().getDrawable(
                    R.drawable.switch_set_dis);
        } else if (IdentiName.equals(getLabelFromName(CHANGEMODE))) {
            iCons = new Drawable[1];
            iCons[0] = mContext.getResources().getDrawable(R.drawable.switch_mode);
        } else if (IdentiName.equals(getLabelFromName(MOBILEDATA))) {
            iCons = new Drawable[2];
            iCons[0] = mContext.getResources().getDrawable(
                    R.drawable.switch_data_pre);
            iCons[1] = mContext.getResources().getDrawable(
                    R.drawable.switch_data);
        } else if (IdentiName.equals(getLabelFromName(SETTING))) {
            iCons = new Drawable[1];
            iCons[0] = mContext.getResources().getDrawable(
                    R.drawable.switch_gestureset_pre);
        } else if (IdentiName.equals(getLabelFromName(GPS))) {
            iCons = new Drawable[2];
            iCons[0] = mContext.getResources().getDrawable(R.drawable.switch_gps_pre);
            iCons[1] = mContext.getResources().getDrawable(R.drawable.switch_gps);
        } else if (IdentiName.equals(getLabelFromName(ROTATION))) {
            iCons = new Drawable[2];
            iCons[0] = mContext.getResources().getDrawable(
                    R.drawable.switch_rotation_pre);
            iCons[1] = mContext.getResources().getDrawable(
                    R.drawable.switch_rotation);
        } else if (IdentiName.equals(getLabelFromName(HOME))) {
            iCons = new Drawable[1];
            iCons[0] = mContext.getResources().getDrawable(
                    R.drawable.switch_home);
        } else if (IdentiName.equals(getLabelFromName(FLYMODE))) {
            iCons = new Drawable[2];
            iCons[0] = mContext.getResources().getDrawable(
                    R.drawable.switch_flightmode_pre);
            iCons[1] = mContext.getResources().getDrawable(
                    R.drawable.switch_flightmode);
        } else if (IdentiName.equals(getLabelFromName(XUKUANG))) {
            iCons = new Drawable[1];
            iCons[0] = mContext.getResources().getDrawable(R.drawable.switch_add);
        }
        return iCons;
    }

    public String getLabelFromName(String IdentiName) {
        String mLabel = "";
        if (IdentiName.equals(BLUETOOTH)) {
            mLabel = mContext.getResources().getString(R.string.quick_guesture_bluetooth);
        } else if (IdentiName.equals(FLASHLIGHT)) {
            mLabel = mContext.getResources()
                    .getString(R.string.quick_guesture_flashlight);
        } else if (IdentiName.equals(WLAN)) {
            mLabel = mContext.getResources().getString(R.string.quick_guesture_wlan);
        } else if (IdentiName.equals(CRAME)) {
            mLabel = mContext.getResources().getString(R.string.quick_guesture_carme);
        } else if (IdentiName.equals(SOUND)) {
            mLabel = mContext.getResources().getString(R.string.quick_guesture_sound);
        } else if (IdentiName.equals(LIGHT)) {
            mLabel = mContext.getResources().getString(R.string.quick_guesture_light);
        } else if (IdentiName.equals(SPEEDUP)) {
            mLabel = mContext.getResources().getString(R.string.quick_guesture_speedup);
        } else if (IdentiName.equals(SWITCHSET)) {
            mLabel = mContext.getResources().getString(R.string.quick_guesture_switchset);
        } else if (IdentiName.equals(CHANGEMODE)) {
            mLabel = mContext.getResources()
                    .getString(R.string.quick_guesture_changemode);
        } else if (IdentiName.equals(MOBILEDATA)) {
            mLabel = mContext.getResources()
                    .getString(R.string.quick_guesture_mobliedata);
        } else if (IdentiName.equals(SETTING)) {
            mLabel = mContext.getResources().getString(R.string.quick_guesture_setting);
        } else if (IdentiName.equals(GPS)) {
            mLabel = mContext.getResources().getString(R.string.quick_guesture_gps);
        } else if (IdentiName.equals(ROTATION)) {
            mLabel = mContext.getResources().getString(R.string.quick_guesture_rotation);
        } else if (IdentiName.equals(HOME)) {
            mLabel = mContext.getResources()
                    .getString(R.string.quick_guesture_home);
        } else if (IdentiName.equals(FLYMODE)) {
            mLabel = mContext.getResources().getString(R.string.quick_guesture_flymode);
        }
        return mLabel;
    }

    public void onDataChange(String restoredData) {
        mSaveList = QuickSwitchManager.getInstance(mContext).StringToList(
                restoredData);
    }

    public synchronized List<BaseInfo> getSwitchList(int switchNum) {
        if (mSwitcherLoaded) {
            if (mSaveList.size() > 11) {
                return mSaveList.subList(0, 11);
            } else {
                return mSaveList;
            }
        } else {
            AppMasterPreference apf = AppMasterPreference.getInstance(mContext);
            String restoredData = apf.getSwitchList();
            if (restoredData.isEmpty() && !apf.getLoadedSwitchList()) {
                mSaveList = new ArrayList<BaseInfo>();
                // 蓝牙开关
                QuickSwitcherInfo lanyaInfo = new QuickSwitcherInfo();
                lanyaInfo.swtichIdentiName = BLUETOOTH;
                lanyaInfo.label = getLabelFromName(BLUETOOTH);
                lanyaInfo.icon = mContext.getResources().getDrawable(
                        R.drawable.switch_bluetooth);
                lanyaInfo.switchIcon = getIconFromName(lanyaInfo.label);
                lanyaInfo.gesturePosition = 0;
                lanyaInfo.isFreeDisturb = true;
                mSaveList.add(lanyaInfo);
                // 手电筒
                QuickSwitcherInfo flashlightInfo = new QuickSwitcherInfo();
                flashlightInfo.swtichIdentiName = FLASHLIGHT;
                flashlightInfo.label = getLabelFromName(FLASHLIGHT);
                flashlightInfo.switchIcon = getIconFromName(flashlightInfo.label);
                flashlightInfo.gesturePosition = 1;
                flashlightInfo.isFreeDisturb = true;
                flashlightInfo.icon = mContext.getResources().getDrawable(
                        R.drawable.switch_flashlight);
                mSaveList.add(flashlightInfo);
                // WLAN
                QuickSwitcherInfo wlanInfo = new QuickSwitcherInfo();
                wlanInfo.swtichIdentiName = WLAN;
                wlanInfo.label = getLabelFromName(WLAN);
                wlanInfo.switchIcon = getIconFromName(wlanInfo.label);
                wlanInfo.gesturePosition = 2;
                wlanInfo.isFreeDisturb = true;
                wlanInfo.icon = mContext.getResources().getDrawable(R.drawable.switch_wifi);
                mSaveList.add(wlanInfo);
                // 相机
                QuickSwitcherInfo carmeInfo = new QuickSwitcherInfo();
                carmeInfo.swtichIdentiName = CRAME;
                carmeInfo.label = getLabelFromName(CRAME);
                carmeInfo.switchIcon = getIconFromName(carmeInfo.label);
                carmeInfo.gesturePosition = 3;
                carmeInfo.isFreeDisturb = true;
                carmeInfo.icon = mContext.getResources().getDrawable(R.drawable.switch_camera);
                mSaveList.add(carmeInfo);
                // 声音
                QuickSwitcherInfo soundInfo = new QuickSwitcherInfo();
                soundInfo.swtichIdentiName = SOUND;
                soundInfo.label = getLabelFromName(SOUND);
                soundInfo.switchIcon = getIconFromName(soundInfo.label);
                soundInfo.gesturePosition = 4;
                soundInfo.isFreeDisturb = true;
                soundInfo.icon = mContext.getResources().getDrawable(
                        R.drawable.switch_volume_min);
                mSaveList.add(soundInfo);
                // 亮度
                QuickSwitcherInfo lightInfo = new QuickSwitcherInfo();
                lightInfo.swtichIdentiName = LIGHT;
                lightInfo.label = getLabelFromName(LIGHT);
                lightInfo.switchIcon = getIconFromName(lightInfo.label);
                lightInfo.gesturePosition = 5;
                lightInfo.isFreeDisturb = true;
                lightInfo.icon = mContext.getResources().getDrawable(
                        R.drawable.switch_brightness_automatic);
                mSaveList.add(lightInfo);
                // 加速
                QuickSwitcherInfo speedUpInfo = new QuickSwitcherInfo();
                speedUpInfo.swtichIdentiName = SPEEDUP;
                speedUpInfo.label = getLabelFromName(SPEEDUP);
                speedUpInfo.switchIcon = getIconFromName(speedUpInfo.label);
                speedUpInfo.gesturePosition = 6;
                speedUpInfo.isFreeDisturb = true;
                speedUpInfo.icon = mContext.getResources().getDrawable(
                        R.drawable.switch_speed_up);
                mSaveList.add(speedUpInfo);
                // 手势设置
                QuickSwitcherInfo switchSetInfo = new QuickSwitcherInfo();
                switchSetInfo.swtichIdentiName = SWITCHSET;
                switchSetInfo.label = getLabelFromName(SWITCHSET);
                switchSetInfo.switchIcon = getIconFromName(switchSetInfo.label);
                switchSetInfo.gesturePosition = 7;
                switchSetInfo.isFreeDisturb = true;
                switchSetInfo.icon = mContext.getResources().getDrawable(
                        R.drawable.switch_set);
                mSaveList.add(switchSetInfo);
                // 情景模式切换
                // QuickSwitcherInfo changeModeInfo = new QuickSwitcherInfo();
                // changeModeInfo.swtichIdentiName = CHANGEMODE;
                // changeModeInfo.label = getLabelFromName(CHANGEMODE);
                // changeModeInfo.switchIcon =
                // getIconFromName(changeModeInfo.label);
                // changeModeInfo.gesturePosition = 8;
                // changeModeInfo.isFreeDisturb = true;
                // changeModeInfo.icon = mContext.getResources().getDrawable(
                // R.drawable.switch_mode);
                // mSaveList.add(changeModeInfo);
                // 移动数据
                QuickSwitcherInfo mobileDataInfo = new QuickSwitcherInfo();
                mobileDataInfo.swtichIdentiName = MOBILEDATA;
                mobileDataInfo.label = getLabelFromName(MOBILEDATA);
                mobileDataInfo.switchIcon = getIconFromName(mobileDataInfo.label);
                mobileDataInfo.gesturePosition = 8;
                mobileDataInfo.isFreeDisturb = true;
                mobileDataInfo.icon = mContext.getResources().getDrawable(
                        R.drawable.switch_data);
                mSaveList.add(mobileDataInfo);
                // 系统设置
                QuickSwitcherInfo settingInfo = new QuickSwitcherInfo();
                settingInfo.swtichIdentiName = SETTING;
                settingInfo.label = getLabelFromName(SETTING);
                settingInfo.switchIcon = getIconFromName(settingInfo.label);
                settingInfo.gesturePosition = 9;
                settingInfo.isFreeDisturb = true;
                settingInfo.icon = mContext.getResources().getDrawable(
                        R.drawable.switch_gestureset_pre);
                mSaveList.add(settingInfo);
                // GPS
                QuickSwitcherInfo gpsInfo = new QuickSwitcherInfo();
                gpsInfo.swtichIdentiName = GPS;
                gpsInfo.label = getLabelFromName(GPS);
                gpsInfo.switchIcon = getIconFromName(gpsInfo.label);
                gpsInfo.gesturePosition = 10;
                gpsInfo.isFreeDisturb = true;
                gpsInfo.icon = mContext.getResources().getDrawable(R.drawable.switch_gps);
                mSaveList.add(gpsInfo);
                // 屏幕旋转
                QuickSwitcherInfo rotationInfo = new QuickSwitcherInfo();
                rotationInfo.swtichIdentiName = ROTATION;
                rotationInfo.label = getLabelFromName(ROTATION);
                rotationInfo.switchIcon = getIconFromName(rotationInfo.label);
                rotationInfo.gesturePosition = 11;
                rotationInfo.isFreeDisturb = false;
                rotationInfo.icon = mContext.getResources().getDrawable(
                        R.drawable.switch_rotation);
                mSaveList.add(rotationInfo);
                // Home
                QuickSwitcherInfo homeInfo = new QuickSwitcherInfo();
                homeInfo.swtichIdentiName = HOME;
                homeInfo.label = getLabelFromName(HOME);
                homeInfo.switchIcon = getIconFromName(homeInfo.label);
                homeInfo.gesturePosition = 12;
                homeInfo.isFreeDisturb = false;
                homeInfo.icon = mContext.getResources().getDrawable(
                        R.drawable.switch_home);
                mSaveList.add(homeInfo);
                // 飞行模式
                QuickSwitcherInfo flyModeInfo = new QuickSwitcherInfo();
                flyModeInfo.swtichIdentiName = FLYMODE;
                flyModeInfo.label = getLabelFromName(FLYMODE);
                flyModeInfo.switchIcon = getIconFromName(flyModeInfo.label);
                flyModeInfo.gesturePosition = 13;
                flyModeInfo.isFreeDisturb = false;
                flyModeInfo.icon = mContext.getResources().getDrawable(
                        R.drawable.switch_flightmode);
                mSaveList.add(flyModeInfo);
                String saveToSp = QuickSwitchManager.getInstance(mContext).listToString(
                        mSaveList.subList(0, 11),
                        switchNum, false);
                apf.setSwitchList(saveToSp);
                apf.setLoadedSwitchList(true);
                mSwitcherLoaded = true;
            } else {
                mSaveList = QuickSwitchManager.getInstance(mContext).StringToList(
                        restoredData);
            }

        }
        if (mSaveList.size() > 11) {
            return mSaveList.subList(0, 11);
        } else {
            return mSaveList;
        }
    }

    public List<BaseInfo> getAllList() {
        if (mAllList == null) {
            mAllList = new ArrayList<BaseInfo>();
        } else {
            return mAllList;
        }
        // 蓝牙开关
        QuickSwitcherInfo lanyaInfo = new QuickSwitcherInfo();
        lanyaInfo.swtichIdentiName = BLUETOOTH;
        lanyaInfo.label = getLabelFromName(BLUETOOTH);
        lanyaInfo.icon = mContext.getResources().getDrawable(
                R.drawable.switch_bluetooth);
        mAllList.add(lanyaInfo);
        // 手电筒
        QuickSwitcherInfo flashlightInfo = new QuickSwitcherInfo();
        flashlightInfo.swtichIdentiName = FLASHLIGHT;
        flashlightInfo.label = getLabelFromName(FLASHLIGHT);
        flashlightInfo.icon = mContext.getResources().getDrawable(
                R.drawable.switch_flashlight);
        mAllList.add(flashlightInfo);
        // WLAN
        QuickSwitcherInfo wlanInfo = new QuickSwitcherInfo();
        wlanInfo.swtichIdentiName = WLAN;
        wlanInfo.label = getLabelFromName(WLAN);
        wlanInfo.icon = mContext.getResources().getDrawable(R.drawable.switch_wifi);
        mAllList.add(wlanInfo);
        // 相机
        QuickSwitcherInfo carmeInfo = new QuickSwitcherInfo();
        carmeInfo.swtichIdentiName = CRAME;
        carmeInfo.label = getLabelFromName(CRAME);
        carmeInfo.icon = mContext.getResources().getDrawable(R.drawable.switch_camera);
        mAllList.add(carmeInfo);
        // 声音
        QuickSwitcherInfo soundInfo = new QuickSwitcherInfo();
        soundInfo.swtichIdentiName = SOUND;
        soundInfo.label = getLabelFromName(SOUND);
        soundInfo.icon = mContext.getResources().getDrawable(
                R.drawable.switch_volume_min);
        mAllList.add(soundInfo);
        // 亮度
        QuickSwitcherInfo lightInfo = new QuickSwitcherInfo();
        lightInfo.swtichIdentiName = LIGHT;
        lightInfo.label = getLabelFromName(LIGHT);
        lightInfo.icon = mContext.getResources().getDrawable(
                R.drawable.switch_brightness_automatic);
        mAllList.add(lightInfo);
        // 加速
        QuickSwitcherInfo speedUpInfo = new QuickSwitcherInfo();
        speedUpInfo.swtichIdentiName = SPEEDUP;
        speedUpInfo.label = getLabelFromName(SPEEDUP);
        speedUpInfo.icon = mContext.getResources().getDrawable(
                R.drawable.switch_speed_up);
        mAllList.add(speedUpInfo);
        // 手势设置
        QuickSwitcherInfo switchSetInfo = new QuickSwitcherInfo();
        switchSetInfo.swtichIdentiName = SWITCHSET;
        switchSetInfo.label = getLabelFromName(SWITCHSET);
        switchSetInfo.icon = mContext.getResources().getDrawable(
                R.drawable.switch_set);
        mAllList.add(switchSetInfo);
        // 移动数据
        QuickSwitcherInfo mobileDataInfo = new QuickSwitcherInfo();
        mobileDataInfo.swtichIdentiName = MOBILEDATA;
        mobileDataInfo.label = getLabelFromName(MOBILEDATA);
        mobileDataInfo.icon = mContext.getResources().getDrawable(
                R.drawable.switch_data);
        mAllList.add(mobileDataInfo);
        // 系统设置
        QuickSwitcherInfo settingInfo = new QuickSwitcherInfo();
        settingInfo.swtichIdentiName = SETTING;
        settingInfo.label = getLabelFromName(SETTING);
        settingInfo.icon = mContext.getResources().getDrawable(
                R.drawable.switch_gestureset_pre);
        mAllList.add(settingInfo);
        // GPS
        QuickSwitcherInfo gpsInfo = new QuickSwitcherInfo();
        gpsInfo.swtichIdentiName = GPS;
        gpsInfo.label = getLabelFromName(GPS);
        gpsInfo.icon = mContext.getResources().getDrawable(R.drawable.switch_gps);
        mAllList.add(gpsInfo);
        // 屏幕旋转
        QuickSwitcherInfo rotationInfo = new QuickSwitcherInfo();
        rotationInfo.swtichIdentiName = ROTATION;
        rotationInfo.label = getLabelFromName(ROTATION);
        rotationInfo.icon = mContext.getResources().getDrawable(
                R.drawable.switch_rotation);
        mAllList.add(rotationInfo);
        // Home
        QuickSwitcherInfo homeInfo = new QuickSwitcherInfo();
        homeInfo.swtichIdentiName = HOME;
        homeInfo.label = getLabelFromName(HOME);
        homeInfo.icon = mContext.getResources().getDrawable(
                R.drawable.switch_home);
        mAllList.add(homeInfo);
        // 飞行模式
        QuickSwitcherInfo flyModeInfo = new QuickSwitcherInfo();
        flyModeInfo.swtichIdentiName = FLYMODE;
        flyModeInfo.label = getLabelFromName(FLYMODE);
        flyModeInfo.icon = mContext.getResources().getDrawable(
                R.drawable.switch_flightmode);
        mAllList.add(flyModeInfo);
        return mAllList;
    }

    public void toggleWlan(QuickSwitcherInfo mInfo) {
        if (!mWifimanager.isWifiEnabled()) {
            mWifimanager.setWifiEnabled(true);
            isWlantOpen = true;
        } else {
            mWifimanager.setWifiEnabled(false);
            isWlantOpen = false;
        }
        LeoEventBus.getDefaultBus().post(
                new ClickQuickItemEvent(WLAN, mInfo));
    }

    public void toggleBluetooth(QuickSwitcherInfo mInfo) {
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter
                    .getDefaultAdapter();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            isBlueToothOpen = true;
        } else {
            mBluetoothAdapter.disable();
            isBlueToothOpen = false;
        }
        LeoEventBus.getDefaultBus().post(
                new ClickQuickItemEvent(BLUETOOTH_EVENT, mInfo));
    }

    public void toggleSound(QuickSwitcherInfo mInfo) {
        if (mSoundManager == null) {
            mSoundManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        }
        if (mSoundManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            mSoundManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            mSoundStatus = mQuite;
        } else if (mSoundManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
            mSoundManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            mSoundStatus = mVibrate;
            if (vib == null) {
                vib = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
            }
            vib.vibrate(150);
        } else if (mSoundManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            mSoundManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            mSoundStatus = mSound;
        }
        LeoEventBus.getDefaultBus().post(
                new ClickQuickItemEvent(SOUND, mInfo));
    }

    public void toggleFlashLight(QuickSwitcherInfo mInfo) {
        if (!isFlashLightOpen) {
            isFlashLightOpen = true;
            try {
                mCamera = Camera.open();
            } catch (Exception e) {
                if (mCamera != null) {
                    mCamera.release();
                    mCamera = null;
                }
                isFlashLightOpen = false;
                return;
            }
            try {
                Parameters params = mCamera.getParameters();
                params.setFlashMode(Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(params);
                mCamera.startPreview();
            } catch (Exception ee) {
                return;
            }
        } else {
            try {
                isFlashLightOpen = false;
                Parameters params = mCamera.getParameters();
                params.setFlashMode(Parameters.FLASH_MODE_OFF);
                mCamera.stopPreview();
                mCamera.release();
            } catch (Exception e) {
                isFlashLightOpen = false;
                return;
            }
        }
        LeoEventBus.getDefaultBus().post(
                new ClickQuickItemEvent(FLASHLIGHT, mInfo));
    }

    public static boolean checkBlueTooth() {
        if (isBlueToothOpen) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkFlashLight() {
        if (isFlashLightOpen) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkWlan() {
        if (isWlantOpen) {
            return true;
        } else {
            return false;
        }
    }

    public static int checkSound() {
        if (mSoundStatus == mSound) {
            return mSound;
        } else if (mSoundStatus == mQuite) {
            return mQuite;
        } else {
            return mVibrate;
        }
    }

    public static int checkLight() {
        if (mLightStatus == LIGHT_AUTO) {
            return LIGHT_AUTO;
        } else if (mLightStatus == LIGHT_NORMAL) {
            return LIGHT_NORMAL;
        } else if (mLightStatus == LIGHT_50_PERCENT) {
            return LIGHT_50_PERCENT;
        } else if (mLightStatus == LIGHT_100_PERCENT) {
            return LIGHT_100_PERCENT;
        } else {
            return LIGHT_ERR;
        }
    }

    public void openCrame() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setAction("android.media.action.STILL_IMAGE_CAMERA");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    public void toggleLight(QuickSwitcherInfo mInfo) {
        int light = 0;
        switch (getBrightStatus()) {
            case LIGHT_AUTO:
                light = LIGHT_NORMAL - 1;
                stopAutoBrightness(mContext.getContentResolver());
                mLightStatus = LIGHT_NORMAL;
                break;
            case LIGHT_NORMAL:
                light = LIGHT_50_PERCENT - 1;
                mLightStatus = LIGHT_50_PERCENT;
                break;
            case LIGHT_50_PERCENT:
                light = LIGHT_100_PERCENT - 1;
                mLightStatus = LIGHT_100_PERCENT;
                break;
            case LIGHT_100_PERCENT:
                startAutoBrightness(mContext.getContentResolver());
                mLightStatus = LIGHT_AUTO;
                break;
            case LIGHT_ERR:
                light = LIGHT_NORMAL - 1;
                break;
        }
        setLight(light);
        setScreenLightValue(mContext.getContentResolver(), light);
        LeoEventBus.getDefaultBus().post(
                new ClickQuickItemEvent(LIGHT, mInfo));
    }

    /*
     * 因为PowerManager提供的函数setBacklightBrightness接口是隐藏的，
     * 所以在基于第三方开发调用该函数时，只能通过反射实现在运行时调用
     */
    private void setLight(int light) {
        try {
            // 得到PowerManager类对应的Class对象
            Class<?> pmClass = Class.forName(mPowerManager.getClass().getName());
            // 得到PowerManager类中的成员mService（mService为PowerManagerService类型）
            Field field = pmClass.getDeclaredField("mService");
            field.setAccessible(true);
            // 实例化mService
            Object iPM = field.get(mPowerManager);
            // 得到PowerManagerService对应的Class对象
            Class<?> iPMClass = Class.forName(iPM.getClass().getName());
            /*
             * 得到PowerManagerService的函数setBacklightBrightness对应的Method对象，
             * PowerManager的函数setBacklightBrightness实现在PowerManagerService中
             */
            Method method = iPMClass.getDeclaredMethod("setBacklightBrightness", int.class);
            method.setAccessible(true);
            // 调用实现PowerManagerService的setBacklightBrightness
            method.invoke(iPM, light);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 启动自动调节亮度
    public void startAutoBrightness(ContentResolver cr)
    {
        Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }

    // 关闭自动调节亮度
    public void stopAutoBrightness(ContentResolver cr)
    {
        Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    // 设置改变亮度值
    public void setScreenLightValue(ContentResolver resolver, int value)
    {
        android.provider.Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS,
                value);
    }

    public void speedUp(QuickSwitcherInfo mInfo) {
        LeoEventBus.getDefaultBus().post(
                new ClickQuickItemEvent(ROTATION, mInfo));
    }

    public void toggleMode() {
        Intent intent = new Intent(mContext, LockModeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    public void switchSet() {
        Intent intent = new Intent(mContext, QuickGestureActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    public void goSetting() {
        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    public static boolean checkGps() {
        if (isGpsOpen) {
            return true;
        } else {
            return false;
        }
    }

    public void toggleGPS() {
        try {
            Intent callGPSSettingIntent = new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            callGPSSettingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(callGPSSettingIntent);
        } catch (Exception e) {
            Toast.makeText(mContext, "GPS is not available!", 0).show();
        }
    }

    public static boolean checkFlyMode() {
        if (isFlyModeOpen) {
            return true;
        } else {
            return false;
        }
    }

    public void toggleFlyMode() {
        try {
            Intent callFLYSettingIntent = new Intent(
                    android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS);
            callFLYSettingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(callFLYSettingIntent);
        } catch (Exception e) {
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            // Toast.makeText(mContext, "Flight mode is not available!",
            // 0).show();
        }
    }

    public static boolean checkRotation() {
        if (isRotationOpen) {
            return true;
        } else {
            return false;
        }
    }

    public void toggleRotation(QuickSwitcherInfo mInfo) {
        try {
            mRotateState = android.provider.Settings.System.getInt(mContext.getContentResolver(),
                    android.provider.Settings.System.ACCELEROMETER_ROTATION);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mRotateState == 1) {
            // 得到uri
            Uri uri = android.provider.Settings.System.getUriFor("accelerometer_rotation");
            // 沟通设置status的值改变屏幕旋转设置
            android.provider.Settings.System.putInt(mContext.getContentResolver(),
                    "accelerometer_rotation",
                    0);
            // 通知改变
            mContext.getContentResolver().notifyChange(uri, null);
            isRotationOpen = false;
        } else {
            // 得到uri
            Uri uri = android.provider.Settings.System.getUriFor("accelerometer_rotation");
            // 沟通设置status的值改变屏幕旋转设置
            android.provider.Settings.System.putInt(mContext.getContentResolver(),
                    "accelerometer_rotation",
                    1);
            // 通知改变
            mContext.getContentResolver().notifyChange(uri, null);
            isRotationOpen = true;
        }
        LeoEventBus.getDefaultBus().post(
                new ClickQuickItemEvent(ROTATION, mInfo));
    }

    // 观察屏幕旋转设置变化
    public class RotationObserver extends ContentObserver {
        ContentResolver mRotationResolver;

        public RotationObserver(Handler handler) {
            super(handler);
            mRotationResolver = mContext.getContentResolver();
        }

        // 屏幕旋转设置改变时调用
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            // 更新按钮状态
            LeoLog.d("QuickSwitchManager", " 屏幕旋转设置改变!!");
            rotation();
        }

        public void startObserver() {
            mRotationResolver.registerContentObserver(Settings.System
                    .getUriFor(Settings.System.ACCELEROMETER_ROTATION), false,
                    this);
        }

        public void stopObserver() {
            mRotationResolver.unregisterContentObserver(this);
        }
    }

    // 观察亮度设置变化
    private class BrightObserver extends ContentObserver {
        ContentResolver mBrightResolver;

        public BrightObserver(Handler handler) {
            super(handler);
            mBrightResolver = mContext.getContentResolver();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            lightPower();
        }

        // 注册观察
        public void startObserver() {
            mBrightResolver.registerContentObserver(Settings.System
                    .getUriFor(Settings.System.SCREEN_BRIGHTNESS), false,
                    this);
            mBrightResolver.registerContentObserver(Settings.System
                    .getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE), false,
                    this);
        }

        // 解除观察
        public void stopObserver() {
            mBrightResolver.unregisterContentObserver(this);
        }
    }

    // 观察GPS设置变化
    private class GpsObserver extends ContentObserver {
        ContentResolver mGpsResolver;

        public GpsObserver(Handler handler) {
            super(handler);
            mGpsResolver = mContext.getContentResolver();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            gps();
        }

        // 注册观察
        public void startObserver() {
            mGpsResolver.registerContentObserver(Settings.System
                    .getUriFor(Settings.System.LOCATION_PROVIDERS_ALLOWED),
                    false, this);
        }

        // 解除观察
        public void stopObserver() {
            mGpsResolver.unregisterContentObserver(this);
        }
    }

    public static boolean checkMoblieData() {
        if (isMobileDataOpen) {
            return true;
        } else {
            return false;
        }
    }

    public void toggleMobileData(QuickSwitcherInfo mInfo) {
        Object[] arg = null;
        try {
            boolean isMobileDataEnable = invokeMethod("getMobileDataEnabled",
                    arg);
            if (isMobileDataEnable) {
                invokeBooleanArgMethod("setMobileDataEnabled", false);
                isMobileDataOpen = false;
            } else {
                invokeBooleanArgMethod("setMobileDataEnabled", true);
                isMobileDataOpen = true;
            }
            LeoEventBus.getDefaultBus().post(
                    new ClickQuickItemEvent(MOBILEDATA, mInfo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean invokeMethod(String methodName, Object[] arg)
            throws Exception {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        Class ownerClass = mConnectivityManager.getClass();
        Class[] argsClass = null;
        if (arg != null) {
            argsClass = new Class[1];
            argsClass[0] = arg.getClass();
        }
        Method method = ownerClass.getMethod(methodName, argsClass);
        Boolean isOpen = (Boolean) method.invoke(mConnectivityManager, arg);
        return isOpen;
    }

    public Object invokeBooleanArgMethod(String methodName, boolean value)
            throws Exception {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        Class ownerClass = mConnectivityManager.getClass();
        Class[] argsClass = new Class[1];
        argsClass[0] = boolean.class;
        Method method = ownerClass.getMethod(methodName, argsClass);
        return method.invoke(mConnectivityManager, value);
    }

    public void goHome() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        mContext.startActivity(intent);
    }

    public String listToString(List<BaseInfo> mSwitchList, int mNum, boolean isFromDialog) {
        String ListString = "";
        for (int i = 0; i < mNum; i++) {
            BaseInfo switchInfo = (BaseInfo) mSwitchList.get(i);
            // String name = switchInfo.label;
            String name = switchInfo.swtichIdentiName;
            int position = switchInfo.gesturePosition;
            LeoLog.d("QuickSwitchManager", "name : " + name + "--position : " + position);
            if (i == 0) {
                ListString = name + ":" + position;
            } else {
                ListString = ListString + "," + name + ":" + position;
            }
        }

        if (isFromDialog) {
            this.mSaveList = StringToList(ListString);
        } else {
            // 每次调用listToString证明要存入sp，so，每次都更新 一下Manager里的list
            this.mSaveList = mSwitchList;
        }

        return ListString;
    }

    public List<BaseInfo> StringToList(String mSwitchListFromSp) {
        List<BaseInfo> mSwitcherList = new ArrayList<BaseInfo>();
        if (!mSwitchListFromSp.isEmpty()) {
            String[] mSwitchAllInfo = mSwitchListFromSp.split(",");
            LeoLog.d(
                    "QuickSwitchManager",
                    "listSize : " + mSwitchAllInfo.length);
            for (int i = 0; i < mSwitchAllInfo.length; i++) {
                QuickSwitcherInfo mInfo = new QuickSwitcherInfo();
                String[] mEachOneInfo = mSwitchAllInfo[i].split(":");
                mInfo.swtichIdentiName = mEachOneInfo[0];
                mInfo.gesturePosition = Integer.parseInt(mEachOneInfo[1]);
                mInfo.label = getLabelFromName(mEachOneInfo[0]);
                mInfo.switchIcon = getIconFromName(mInfo.label);
                mSwitcherList.add(mInfo);
            }
        }
        return mSwitcherList;
    }

    public String getListStringFromSp() {
        return switchPreference.getSwitchList();
    }

    public void init() {
        getSwitchList(11);
    }

    public void unInit() {
        mSwitcherLoaded = false;
        if (mSaveList != null) {
            mSaveList.clear();
            mSaveList = null;
        }
    }

}
