
package com.leo.appmaster.quickgestures;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.leo.appmaster.R;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.quickgestures.view.QuickGestureContainer;
import com.leo.appmaster.quickgestures.view.QuickGestureLayout;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.net.wifi.WifiManager;
import android.provider.MediaStore;

public class QuickSwitchManager {

    private static QuickSwitchManager mInstance;
    public final static String BLUETOOTH = "bluetooth";
    public final static String FLASHLIGHT = "flashlight";
    public final static String WLAN = "wlan";
    public final static String CRAME = "carme";

    private Context mContext;
    private static BluetoothAdapter mBluetoothAdapter;
    private WifiManager mWifimanager;
    public Camera mCamera;
    private static boolean isBlueToothOpen = false;
    private static boolean isFlashLightOpen = false;
    private static boolean isWlantOpen = false;

    public static synchronized QuickSwitchManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new QuickSwitchManager(context);
        }
        return mInstance;
    }

    private QuickSwitchManager(Context context) {
        mContext = context.getApplicationContext();
        blueTooth();
        Wlan();
    }

    private void Wlan() {
        mWifimanager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (mWifimanager.isWifiEnabled()) {
            isWlantOpen = true;
        } else {
            isWlantOpen = false;
        }
    }

    private void blueTooth() {
        mBluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            isBlueToothOpen = true;
        } else {
            isBlueToothOpen = false;
        }
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
            return;
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

    public void openCrame() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
        mContext.startActivity(intent);
    }
}
