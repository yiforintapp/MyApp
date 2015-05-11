
package com.leo.appmaster.quickgestures;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.leo.appmaster.R;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.quickgestures.view.QuickGestureContainer;
import com.leo.appmaster.quickgestures.view.QuickGestureLayout;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.SoundEffectConstants;

public class QuickSwitchManager {

    private static QuickSwitchManager mInstance;
    public final static String BLUETOOTH = "bluetooth";
    public final static String FLASHLIGHT = "flashlight";
    public final static String WLAN = "wlan";
    public final static String CRAME = "carme";
    public final static String SOUND = "sound";
    public final static String LIGHT = "light";
    private Context mContext;
    private static BluetoothAdapter mBluetoothAdapter;
    private WifiManager mWifimanager;
    public Camera mCamera;
    private AudioManager mSoundManager;
    private PowerManager mPowerManager;
    private static boolean isBlueToothOpen = false;
    private static boolean isFlashLightOpen = false;
    private static boolean isWlantOpen = false;
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

    public static synchronized QuickSwitchManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new QuickSwitchManager(context);
        }
        return mInstance;
    }

    private QuickSwitchManager(Context context) {
        mContext = context.getApplicationContext();
        BlueTooth();
        Wlan();
        Sound();
        LightPower();
    }

    private void Sound() {
        mSoundManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (mSoundManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            mSoundStatus = 0;
        } else if (mSoundManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
            mSoundStatus = 1;
        } else if (mSoundManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            mSoundStatus = 2;
        }
    }

    private void Wlan() {
        mWifimanager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (mWifimanager.isWifiEnabled()) {
            isWlantOpen = true;
        } else {
            isWlantOpen = false;
        }
    }

    private void BlueTooth() {
        mBluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            isBlueToothOpen = true;
        } else {
            isBlueToothOpen = false;
        }
    }

    private void LightPower() {
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
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

    public List<QuickSwitcherInfo> getSwitchList(int switchNum) {
        List<QuickSwitcherInfo> mSwitchList = new ArrayList<QuickSwitcherInfo>();
        // 蓝牙开关
        QuickSwitcherInfo lanyaInfo = new QuickSwitcherInfo();
        lanyaInfo.label = mContext.getResources().getString(R.string.quick_guesture_bluetooth);
        lanyaInfo.switchIcon = new Drawable[2];
        lanyaInfo.switchIcon[0] = mContext.getResources().getDrawable(
                R.drawable.switch_bluetooth_pre);
        lanyaInfo.switchIcon[1] = mContext.getResources().getDrawable(R.drawable.switch_bluetooth);
        lanyaInfo.iDentiName = BLUETOOTH;
        mSwitchList.add(lanyaInfo);
        // 手电筒
        QuickSwitcherInfo flashlightInfo = new QuickSwitcherInfo();
        flashlightInfo.label = mContext.getResources()
                .getString(R.string.quick_guesture_flashlight);
        flashlightInfo.switchIcon = new Drawable[2];
        flashlightInfo.switchIcon[0] = mContext.getResources().getDrawable(
                R.drawable.switch_flashlight_pre);
        flashlightInfo.switchIcon[1] = mContext.getResources().getDrawable(
                R.drawable.switch_flashlight);
        flashlightInfo.iDentiName = FLASHLIGHT;
        mSwitchList.add(flashlightInfo);
        // WLAN
        QuickSwitcherInfo wlanInfo = new QuickSwitcherInfo();
        wlanInfo.label = mContext.getResources().getString(R.string.quick_guesture_wlan);
        wlanInfo.switchIcon = new Drawable[2];
        wlanInfo.switchIcon[0] = mContext.getResources().getDrawable(R.drawable.switch_wifi_pre);
        wlanInfo.switchIcon[1] = mContext.getResources().getDrawable(R.drawable.switch_wifi);
        wlanInfo.iDentiName = WLAN;
        mSwitchList.add(wlanInfo);
        // 相机
        QuickSwitcherInfo carmeInfo = new QuickSwitcherInfo();
        carmeInfo.label = mContext.getResources().getString(R.string.quick_guesture_carme);
        carmeInfo.switchIcon = new Drawable[1];
        carmeInfo.switchIcon[0] = mContext.getResources().getDrawable(R.drawable.switch_camera);
        carmeInfo.iDentiName = CRAME;
        mSwitchList.add(carmeInfo);
        // 声音
        QuickSwitcherInfo soundInfo = new QuickSwitcherInfo();
        soundInfo.label = mContext.getResources().getString(R.string.quick_guesture_sound);
        soundInfo.switchIcon = new Drawable[3];
        soundInfo.switchIcon[0] = mContext.getResources().getDrawable(R.drawable.switch_volume_min);
        soundInfo.switchIcon[1] = mContext.getResources()
                .getDrawable(R.drawable.switch_volume_mute);
        soundInfo.switchIcon[2] = mContext.getResources().getDrawable(
                R.drawable.switch_volume_vibration);
        soundInfo.iDentiName = SOUND;
        mSwitchList.add(soundInfo);
        // 亮度
        QuickSwitcherInfo lightInfo = new QuickSwitcherInfo();
        lightInfo.label = mContext.getResources().getString(R.string.quick_guesture_light);
        lightInfo.switchIcon = new Drawable[4];
        lightInfo.switchIcon[0] = mContext.getResources().getDrawable(
                R.drawable.switch_brightness_automatic);
        lightInfo.switchIcon[1] = mContext.getResources()
                .getDrawable(R.drawable.switch_brightness_min);
        lightInfo.switchIcon[2] = mContext.getResources().getDrawable(
                R.drawable.switch_brightness_half);
        lightInfo.switchIcon[3] = mContext.getResources().getDrawable(
                R.drawable.switch_brightness_max);
        lightInfo.iDentiName = LIGHT;
        mSwitchList.add(lightInfo);
        return mSwitchList;
    }

    public void toggleWlan(QuickGestureContainer mContainer, List<QuickSwitcherInfo> list,
            QuickGestureLayout quickGestureLayout) {
        if (!mWifimanager.isWifiEnabled()) {
            mWifimanager.setWifiEnabled(true);
            isWlantOpen = true;
        } else {
            mWifimanager.setWifiEnabled(false);
            isWlantOpen = false;
        }
        mContainer.fillSwitchItem(quickGestureLayout, list);
    }

    public void toggleBluetooth(QuickGestureContainer mContainer, List<QuickSwitcherInfo> list,
            QuickGestureLayout quickGestureLayout) {
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
        mContainer.fillSwitchItem(quickGestureLayout, list);
    }

    public void toggleSound(QuickGestureContainer mContainer, List<QuickSwitcherInfo> switchList,
            QuickGestureLayout quickGestureLayout) {
        if (mSoundManager == null) {
            mSoundManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        }
        if (mSoundManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            mSoundManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            mSoundStatus = mQuite;
        } else if (mSoundManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
            mSoundManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            mSoundStatus = mVibrate;
        } else if (mSoundManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            mSoundManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            mSoundStatus = mSound;
        }
        mContainer.fillSwitchItem(quickGestureLayout, switchList);
    }

    public void toggleFlashLight(QuickGestureContainer mContainer, List<QuickSwitcherInfo> list,
            QuickGestureLayout quickGestureLayout) {
        if (!isFlashLightOpen) {
            isFlashLightOpen = true;
            try {
                mCamera = Camera.open();
            } catch (Exception e) {
                if (mCamera != null) {
                    mCamera.release();
                    mCamera = null;
                }
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
            isFlashLightOpen = false;
            Parameters params = mCamera.getParameters();
            params.setFlashMode(Parameters.FLASH_MODE_OFF);
            mCamera.stopPreview();
            mCamera.release();
        }
        mContainer.fillSwitchItem(quickGestureLayout, list);
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
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    public void toggleLight(QuickGestureContainer mContainer, List<QuickSwitcherInfo> switchList,
            QuickGestureLayout quickGestureLayout) {
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
        mContainer.fillSwitchItem(quickGestureLayout, switchList);
        // refreshLightButton();
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

}
