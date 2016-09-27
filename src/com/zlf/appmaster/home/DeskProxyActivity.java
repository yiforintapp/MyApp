
package com.zlf.appmaster.home;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.utils.LeoLog;


public class DeskProxyActivity extends Activity {
    private static final String TAG = "DeskProxyActivity";
    public static final int IDX_APP_LOCK = 1;
    public static final int IDX_APP_COVER = 2;
    public static final int IDX_PIC_HIDE = 3;
    public static final int IDX_VID_HIDE = 4;
    public static final int IDX_PRIVACY_SMS = 5;
    public static final int IDX_FLOW = 6;
    public static final int IDX_ELEC = 7;
    public static final int IDX_BACKUP = 8;
    public static final int IDX_LOCK_THEME = 10;
    public static final int IDX_HOT_APP = 11;
    public static final int IDX_AD = 12;
    public static final int IDX_WIFI = 13;
    public static final int IDX_QUICK_HELPER = 14;
    public static final int IDX_FILTER_NOTI = 15;
    public static final int IDX_STRANGER_CALL_NOTI = 16;
    public static final int mMissCallNoti = 17;
    public static final int IDX_CALL_FILTER = 18;
    public static final int IDX_BATTERY_PROTECT = 19;

    private static final int IDX_HOME = 9999;

    private boolean mDelayFinish = false;
    private Handler mHandler;
    private String mCbPath;

    private boolean mHasRegistered = false;

    public static final String CALL_FILTER_PUSH = "from"; //是否从骚扰拦截push通知进入key


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LeoLog.i("proxy", "entered!");
        Intent intent = getIntent();
        Uri uri = intent.getData();

        String fromWhere = null;
        int type = 0;
        if (uri != null) {
            String schema = uri.getScheme();
            String host = uri.getHost();
            String path = uri.getPath();
            LeoLog.d(TAG, "onCreate, uri: " + uri);
            if(Constants.DP_APP_SCHEMA.equals(schema) && Constants.DP_APP_HOST.equals(host)){
                gotoHomeActivity();
                finish();
            }
            if (!Constants.DP_SCHEMA.equals(schema) || !Constants.DP_HOST.equals(host) || TextUtils.isEmpty(path)) {
                finish();
                return;
            }

            try {
                path = path.substring(1);
                type = Integer.parseInt(path);
            } catch (Exception e) {
                e.printStackTrace();
                finish();
                return;
            }
        } else {

        }

        handleAction(type, fromWhere);
    }

    private void handleAction(int type, String fromWhere) {

    }

    private void gotoBatteryClick() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        this.registerReceiver(mPresentReceiver, intentFilter);
        mHasRegistered = true;
    }

    private BroadcastReceiver mPresentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LeoLog.d("proxy", "action=" + intent.getAction());
            finish();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*if (wallAd != null) {
            wallAd.release();
            wallAd = null;
        }*/
        if (mHasRegistered) {
            try {
                unregisterReceiver(mPresentReceiver);
            } catch(Exception e) {
            }
            mHasRegistered = false;
        }
    }

    private void gotoAd(int type) {
        if (getIntent().getBooleanExtra("from_quickhelper", false)) {

        }
        // wallAd = MobvistaEngine.getInstance().createAdWallController(this);
//        wallAd = MobvistaEngine.getInstance(this).createAdWallController(this, Constants.UNIT_ID_61);

        /*if (wallAd != null) {
            wallAd.preloadWall();
            wallAd.clickWall();
        }*/
    }

    private void gotoHomeActivity(){
        Intent intent = new Intent(this, HomeMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void gotoHotApp(int type) {

    }

    private void gotoLockThem(int type) {

    }

    private void goToStrangerCall(int type) {

    }

    /**
     * 从骚扰拦截push通知进入骚扰拦截界面
     */
    private void gotoCallFilerActivity() {

    }

    private void goToBlackList(int type) {

    }

    private void goToBlackListTab1(int type) {

    }

    private void gotoBackUp(int type) {
        if (getIntent().getBooleanExtra("from_quickhelper", false)) {

        }

    }

    private void gotoWifi(int type) {
        if (getIntent().getBooleanExtra("from_quickhelper", false)) {

        }

    }

    private void gotoEle(int type) {

    }

    private void goToFlow(int type) {
        if (getIntent().getBooleanExtra("from_quickhelper", false)) {

        }

    }

    private void goToPrivateSms(int type) {

    }

    private void goToHideVio(int type) {

    }

    private void goToHidePic(int type) {

    }

    private void goToAppWeiZhuang(int type) {

    }

    private void goToAppLock(int type) {

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mDelayFinish = false;
        mHandler = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        // APP Indexing API 为了让Google 搜索发现和了解应用，以便在搜索结果中呈现深层链接
        // 也可以让应用允许 Googlebot 访问而不使用APP Indexing API

    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
