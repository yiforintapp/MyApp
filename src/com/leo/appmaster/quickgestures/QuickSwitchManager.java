
package com.leo.appmaster.quickgestures;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.leo.appmaster.R;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

public class QuickSwitchManager {

    private static QuickSwitchManager mInstance;
    public final static String BLUETOOTH = "bluetooth";
    private Context mContext;
    private PackageManager mPm;
    private CountDownLatch mLatch;
    private BluetoothAdapter mBluetoothAdapter;
    
    public static synchronized QuickSwitchManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new QuickSwitchManager(context);
        }
        return mInstance;
    }

    private QuickSwitchManager(Context context) {
        mContext = context.getApplicationContext();
        mPm = mContext.getPackageManager();
        mLatch = new CountDownLatch(1);
        
        mBluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
    }

    public List<QuickSwitcherInfo> getSwitchList(int switchNum) {
        List<QuickSwitcherInfo> mSwitchList =  new ArrayList<QuickSwitcherInfo>();
        // 蓝牙开关
        QuickSwitcherInfo lanyaInfo = new QuickSwitcherInfo();
        lanyaInfo.label = mContext.getResources().getString(R.string.quick_guesture_bluetooth);
        lanyaInfo.switchIcon =  new Drawable[2];
        lanyaInfo.switchIcon[0] = mContext.getResources().getDrawable(R.drawable.app_backup_icon);
        lanyaInfo.switchIcon[1] = mContext.getResources().getDrawable(R.drawable.app_battery_icon);
        lanyaInfo.iDentiName = BLUETOOTH;
        
        mSwitchList.add(lanyaInfo);
        return mSwitchList;
    }

    public void toggleBluetooth() {
        if (mBluetoothAdapter == null) {
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        } else {
            mBluetoothAdapter.disable();
        }
    }
}
