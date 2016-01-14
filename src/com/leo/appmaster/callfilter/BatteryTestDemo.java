package com.leo.appmaster.callfilter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.battery.BatterProtectView;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

public class BatteryTestDemo extends BaseActivity implements OnClickListener {
    private static final String TAG = "BatteryTestDemo";
    private TextView mTvTitleName;
    private TextView mTvTitleNumber;
    private View mSlideView;
    private BatteryChangeReceiver mBatteryReceiver;
    private WindowManager mWM;
    private WindowManager.LayoutParams mParams;
    private int mScreenWidth;


    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_test);
        handleIntent();
        initUI();
        processUI();
        process();
    }

    private void process() {
        mBatteryReceiver = new BatteryChangeReceiver();
        registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        mWM = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        mParams = new WindowManager.LayoutParams();
    }

    private void handleIntent() {
        Intent intent = getIntent();
    }

    private void initUI() {
        mTvTitleName = (TextView) findViewById(R.id.battery_num);
        mTvTitleNumber = (TextView) findViewById(R.id.battery_status);
    }

    private void processUI() {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBatteryReceiver != null) {
            unregisterReceiver(mBatteryReceiver);
        }
    }

    private void loadSysContact() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }
    }


    private int BatteryN;       //目前电量
    private int BatteryV;       //电池电压
    private double BatteryT;        //电池温度
    private String BatteryStatusStr;   //电池状态
    private int BatteryStatusInt;   //电池状态

    public class BatteryChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                BatteryN = intent.getIntExtra("level", 0);    //目前电量
                LeoLog.d(TAG, "电量 : " + BatteryN);
                BatteryV = intent.getIntExtra("voltage", 0);  //电池电压
                LeoLog.d(TAG, "电压 : " + BatteryV);
                BatteryT = intent.getIntExtra("temperature", 0);  //电池温度
                LeoLog.d(TAG, "温度 : " + BatteryT);

                BatteryStatusInt = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                switch (BatteryStatusInt) {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        BatteryStatusStr = "充电状态";
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        BatteryStatusStr = "放电状态";
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        BatteryStatusStr = "未充电";
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        BatteryStatusStr = "充满电";
                        break;
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        BatteryStatusStr = "未知道状态";
                        break;
                }
                if (!Utilities.isEmpty(BatteryStatusStr)) {
                    LeoLog.d(TAG, "状态 : " + BatteryStatusStr);
                }

                mTvTitleName.setText("电量 : " + BatteryN);
                mTvTitleNumber.setText("状态 : " + BatteryStatusStr);

                if ("充电状态".equals(BatteryStatusStr)) {
                    BatterProtectView.makeText(BatteryTestDemo.this).show();
                }
            }
        }
    }


}
