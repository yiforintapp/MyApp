package com.leo.appmaster.battery;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;


public class BatteryShowViewActivity extends BaseActivity {
    private final String TAG = "AskAddToBlacklistActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏状态栏
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //获得当前窗体对象
        Window window = getWindow();
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);

        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_batter_show_view);

        handleIntent();
    }

    private void handleIntent() {
        Intent intent = getIntent();
//        switch (intExtra) {
//            default:
//                this.finish();
//                break;
//        }
    }

    @Override
    public void finish() {
        super.finish();
    }

}
