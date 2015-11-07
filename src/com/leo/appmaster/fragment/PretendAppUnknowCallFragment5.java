
package com.leo.appmaster.fragment;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.leo.appmaster.PhoneInfo;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.GestureRelative;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CirCleDongHua;
import com.leo.appmaster.utils.DipPixelUtil;

public class PretendAppUnknowCallFragment5 extends PretendFragment implements OnTouchListener {
    private ImageView iv_dianhua_hold, iv_guaduan, iv_duanxin, iv_jieting, iv_guaduan_big,
            iv_duanxin_big, iv_jieting_big, finish_lock, iv_test_icon;
    private RelativeLayout activity_weizhuang_firstin;
    private View text_content;
    private float mYuanX, mYuanY, mZhiJing, mBanJing;
    private int hold_width, hold_height, hold_left, hold_top, hold_right, hold_bottom;
    private int gua_yuan_x, gua_yuan_y, gua_left, gua_top, gua_right, gua_bottom;
    private int duan_yuan_x, duan_yuan_y, duan_left, duan_top, duan_right, duan_bottom;
    private int jie_yuan_x, jie_yuan_y, jie_left, jie_top, jie_right, jie_bottom;
    private int gua_left_big, gua_top_big, gua_right_big, gua_bottom_big;
    private int duan_left_big, duan_top_big, duan_right_big, duan_bottom_big;
    private int jie_left_big, jie_top_big, jie_right_big, jie_bottom_big;
    private int finish_left, finish_top, finish_right, finish_bottom;
    private int top_guide_left,top_guide_top,top_guide_right,top_guide_bottom;
    private int startX, startY;
    private float lc_left, lc_top, lc_right, lc_bottom;
    private boolean isControlGua = true;
    private boolean isControlDuan = true;
    private boolean isControlJie = true;
    private boolean isStartDong = false;
    private boolean isStop = false;
    private boolean isScreenOff = false;
    private GestureRelative mViewContent;
    private int mVersion;
    private Vibrator vib;
    private CirCleDongHua myself_circle;
    private ScreenBroadcastReceiver mScreenReceiver;
    private int mFinishLockWidth, mFinishLockHeight;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            // LeoLog.d("testfragment", "收到message!!!");
            switch (msg.what) {
                case 1:
                    iv_dianhua_hold.layout(hold_left, hold_top, hold_right, hold_bottom);

                    iv_guaduan.layout(gua_left, gua_top, gua_right, gua_bottom);
                    iv_guaduan_big.layout(gua_left_big, gua_top_big, gua_right_big, gua_bottom_big);

                    iv_duanxin.layout(duan_left, duan_top, duan_right, duan_bottom);
                    iv_duanxin_big.layout(duan_left_big, duan_top_big, duan_right_big,
                            duan_bottom_big);

                    iv_jieting.layout(jie_left, jie_top, jie_right, jie_bottom);
                    iv_jieting_big.layout(jie_left_big, jie_top_big, jie_right_big, jie_bottom_big);

                    if (!isStartDong) {
                        myself_circle.setVisibility(View.VISIBLE);
                        showDonghua();
                    }

                    iv_dianhua_hold.setVisibility(View.VISIBLE);
                    iv_guaduan.setVisibility(View.VISIBLE);
                    iv_duanxin.setVisibility(View.VISIBLE);
                    iv_jieting.setVisibility(View.VISIBLE);

                    mViewContent.mFilterLayout = true;

                    finish_lock.layout(finish_left, finish_top, finish_right, finish_bottom);

                    if (!isStop) {
                        vib.vibrate(new long[] {
                                1000, 1000, 1000, 1000
                        }, 1);
                        // LeoLog.d("testFragment", "start 震动 ! ");
                    }

                    mViewContent.isInit = true;
                    break;
                case 2:
                    iv_dianhua_hold.setVisibility(View.INVISIBLE);
                    iv_guaduan.setVisibility(View.INVISIBLE);
                    iv_duanxin.setVisibility(View.INVISIBLE);
                    iv_jieting.setVisibility(View.INVISIBLE);
                    break;
                case 3:
                    text_content.setVisibility(View.INVISIBLE);
                    finish_lock.setVisibility(View.VISIBLE);
                    break;
                case 4:
                    SDKWrapper
                            .addEvent(mActivity, SDKWrapper.P1, "appcover", "done_UnknowCall");
                    onUnlockPretendSuccessfully();
                    isStartDong = false;
                    text_content.setVisibility(View.VISIBLE);
                    finish_lock.setVisibility(View.INVISIBLE);
                    break;
                default:
                    break;
            }
        };
    };

    protected void showDonghua() {
        isStartDong = true;
        new Thread() {
            public void run() {
                int startInt = 0;
                int mAplha = 0;
                while (mBanJing > startInt) {
                    try {
                        Thread.sleep(15);
                        startInt += 2;
                        mAplha = 255 - (int) (startInt * 255 / mBanJing);
                        if (mAplha < 0) {
                            mAplha = 0;
                        }
                        myself_circle.setProgress(startInt, mAplha);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                showDonghua();
            };
        }.start();
    }

    @Override
    public void onDestroy() {
        mActivity.unregisterReceiver(mScreenReceiver);
        super.onDestroy();
    }

    @Override
    public void onStop() {
        // LeoLog.d("testFragment", "onStop");
        isStop = true;
        if (vib != null) {
            vib.cancel();
            // LeoLog.d("testFragment", "onStop , vib.cancel()");
        }
        super.onStop();
    }

    @Override
    public void onResume() {
        
//        SDKWrapper.addEvent(mActivity, SDKWrapper.P1, 
//                "appcover ", "unknowcall");
        
        if (!isScreenOff) {
            isStop = false;
        } else {
            isStop = true;
        }
        // LeoLog.d("testFragment", "onResume");
        super.onResume();
    }

    @Override
    protected int layoutResourceId() {
        return R.layout.activity_unknowcall_five;
    }

    @Override
    protected void onInitUI() {
        activity_weizhuang_firstin = (RelativeLayout) findViewById(R.id.activity_weizhuang_firstin);

        android.widget.LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        activity_weizhuang_firstin.setLayoutParams(lp);

        SDKWrapper
                .addEvent(mActivity, SDKWrapper.P1, "appcover", "UnknowCall");

        mScreenReceiver = new ScreenBroadcastReceiver();
        startScreenBroadcastReceiver();
        mVersion = PhoneInfo.getAndroidVersion();
        init();
        GlobalLayoutListener();
        vib = (Vibrator) getActivity().getSystemService(Service.VIBRATOR_SERVICE);
    }

    private void init() {
        mViewContent = (GestureRelative) findViewById(R.id.mid_view);
        mViewContent.setisFromActivity(false);
        mViewContent.setPretendFragment(this);
        mViewContent.setFragment(this);

        iv_dianhua_hold = (ImageView) findViewById(R.id.iv_dianhua_hold);
        iv_dianhua_hold.setOnTouchListener(this);

        text_content = findViewById(R.id.text_content);

        iv_guaduan = (ImageView) findViewById(R.id.iv_guaduan);
        iv_duanxin = (ImageView) findViewById(R.id.iv_duanxin);
        iv_jieting = (ImageView) findViewById(R.id.iv_jieting);
        iv_guaduan_big = (ImageView) findViewById(R.id.iv_guaduan_big);
        iv_duanxin_big = (ImageView) findViewById(R.id.iv_duanxin_big);
        iv_jieting_big = (ImageView) findViewById(R.id.iv_jieting_big);
        
        // iv_test_icon = new ImageView(mActivity);
        // iv_test_icon.setBackground(mActivity.getResources().getDrawable(R.drawable.app_seekbar_btn));
        // RelativeLayout.LayoutParams params = new
        // RelativeLayout.LayoutParams(25, 25);
        // iv_test_icon.setLayoutParams(params);

        myself_circle = (CirCleDongHua) findViewById(R.id.myself_circle);
        finish_lock = (ImageView) findViewById(R.id.finish_lock);
    }

    public void setPlace() {
        mHandler.sendEmptyMessage(2);
        mHandler.sendEmptyMessageDelayed(1, 50);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.iv_dianhua_hold) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:// 手指按下屏幕
                    myself_circle.setVisibility(View.INVISIBLE);
                    startX = (int) event.getRawX();
                    startY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int newX = (int) event.getRawX() - startX;
                    int newY = (int) event.getRawY() - startY;

                    lc_left = iv_dianhua_hold.getLeft();
                    lc_top = iv_dianhua_hold.getTop();
                    lc_right = iv_dianhua_hold.getRight();
                    lc_bottom = iv_dianhua_hold.getBottom();

                    int top = (int) (lc_top + newY);
                    int left = (int) (lc_left + newX);
                    int bottom = (int) (lc_bottom + newY);
                    int right = (int) (lc_right + newX);

                    if (left < gua_right + 10 && top > gua_top - gua_top / 2
                            && bottom < gua_bottom + gua_bottom / 2 && right >
                            gua_left - 10) {
                        // iv_guaduan_big.setVisibility(View.VISIBLE);
                        // iv_guaduan.setVisibility(View.INVISIBLE);
                        isControlGua = false;
                    } else {
                        if (!isControlGua) {
                            // iv_guaduan_big.setVisibility(View.INVISIBLE);
                            // iv_guaduan.setVisibility(View.VISIBLE);
                        }
                        isControlGua = true;
                    }

                    if (top < duan_bottom + 10 && bottom > duan_top - 10
                            && left > duan_left - duan_left / 2
                            && right < duan_right + duan_right / 2) {
                        // iv_duanxin.setVisibility(View.INVISIBLE);
                        // iv_duanxin_big.setVisibility(View.VISIBLE);
                        isControlDuan = false;
                    } else {
                        if (!isControlDuan) {
                            // iv_duanxin.setVisibility(View.VISIBLE);
                            // iv_duanxin_big.setVisibility(View.INVISIBLE);
                        }
                        isControlDuan = true;
                    }

                    if (right > jie_left - 10 && top > jie_top - jie_top / 2
                            && bottom < jie_bottom + jie_bottom / 2 && left <
                            jie_right + 10) {
                        // iv_jieting_big.setVisibility(View.VISIBLE);
                        // iv_jieting.setVisibility(View.INVISIBLE);
                        isControlJie = false;
                    } else {
                        if (!isControlJie) {
                            // iv_jieting_big.setVisibility(View.INVISIBLE);
                            // iv_jieting.setVisibility(View.VISIBLE);
                        }
                        isControlJie = true;
                    }

                    if (left < gua_left) {
                        left = gua_left;
                        right = left + iv_dianhua_hold.getWidth();
                    }

                    if (top < duan_top) {
                        top = duan_top;
                        bottom = top + iv_dianhua_hold.getHeight();
                    }

                    if (right > jie_right) {
                        right = jie_right;
                        left = right - iv_dianhua_hold.getWidth();
                    }

                    if (bottom > duan_yuan_y + mZhiJing) {
                        bottom = (int) (duan_yuan_y + mZhiJing);
                        top = bottom - iv_dianhua_hold.getHeight();
                    }

                    v.layout(left, top, right, bottom);
                    startX = (int) event.getRawX();
                    startY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    // if (iv_guaduan_big.getVisibility() == View.VISIBLE
                    // || iv_duanxin_big.getVisibility() == View.VISIBLE
                    // || iv_jieting_big.getVisibility() == View.VISIBLE) {
                    // onUnlockPretendFailed();
                    // vib.cancel();
                    if (!isControlDuan || !isControlGua || !isControlJie) {
                        SDKWrapper.addEvent(mActivity, SDKWrapper.P1, 
                                "appcover", "fail_UnknowCall");
                        onUnlockPretendFailed();
                        vib.cancel();
                    } else {
                        v.layout(hold_left, hold_top, hold_right, hold_bottom);
                        myself_circle.setVisibility(View.VISIBLE);
                    }

                    
                    
                    break;
            }
        }
        return false;
    }

    private void GlobalLayoutListener() {
        ViewTreeObserver contentObserver = mViewContent.getViewTreeObserver();
        contentObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
			public void onGlobalLayout() {
                // 圆心在？
                mYuanX = mViewContent.getPointX();
                mYuanY = mViewContent.getPointY();
                myself_circle.setYuan(mYuanX, mYuanY);

                // 直径是？
                mZhiJing = mViewContent.getZhiJing();
                mBanJing = mZhiJing / 2;

                // hold
                hold_width = iv_dianhua_hold.getWidth();
                hold_height = iv_dianhua_hold.getHeight();
                hold_left = (int) (mYuanX - (hold_width / 2));
                hold_top = (int) (mYuanY - (hold_height / 2));
                hold_right = (int) (mYuanX + (hold_width / 2));
                hold_bottom = (int) (mYuanY + (hold_height / 2));

                // gua
                gua_yuan_x = (int) (mYuanX - mBanJing);
                gua_yuan_y = (int) mYuanY;
                int gua_width = iv_guaduan.getWidth();
                int gua_height = iv_guaduan.getHeight();
                gua_left = gua_yuan_x - (gua_width / 2);
                gua_top = gua_yuan_y - (gua_height / 2);
                gua_right = gua_yuan_x + (gua_width / 2);
                gua_bottom = gua_yuan_y + (gua_height / 2);
                mViewContent.setGuaPosition(gua_left, gua_top, gua_right, gua_bottom);

                // gua_big
                int gua_big_width = iv_guaduan_big.getWidth();
                int gua_big_height = iv_guaduan_big.getHeight();
                gua_left_big = gua_yuan_x - (gua_big_width / 2);
                gua_top_big = gua_yuan_y - (gua_big_height / 2);
                gua_right_big = gua_yuan_x + (gua_big_width / 2);
                gua_bottom_big = gua_yuan_y + (gua_big_height / 2);

                // duan
                duan_yuan_x = (int) mYuanX;
                duan_yuan_y = (int) (mYuanY - mBanJing);
                int duan_width = iv_duanxin.getWidth();
                int duan_height = iv_duanxin.getHeight();
                duan_left = duan_yuan_x - (duan_width / 2);
                duan_top = duan_yuan_y - (duan_height / 2);
                duan_right = duan_yuan_x + (duan_width / 2);
                duan_bottom = duan_yuan_y + (duan_height / 2);
                mViewContent.setDuanPosition(duan_left, duan_top, duan_right, duan_bottom);

                // duan_big
                int duan_big_width = iv_duanxin_big.getWidth();
                int duan_big_height = iv_duanxin_big.getHeight();
                duan_left_big = duan_yuan_x - (duan_big_width / 2);
                duan_top_big = duan_yuan_y - (duan_big_height / 2);
                duan_right_big = duan_yuan_x + (duan_big_width / 2);
                duan_bottom_big = duan_yuan_y + (duan_big_height / 2);

                // jie
                jie_yuan_x = (int) (mYuanX + mBanJing);
                jie_yuan_y = (int) mYuanY;
                int jie_width = iv_guaduan.getWidth();
                int jie_height = iv_guaduan.getHeight();
                jie_left = jie_yuan_x - (jie_width / 2);
                jie_top = jie_yuan_y - (jie_height / 2);
                jie_right = jie_yuan_x + (jie_width / 2);
                jie_bottom = jie_yuan_y + (jie_height / 2);
                mViewContent.setJiePosition(jie_left, jie_top, jie_right, jie_bottom);

                // jie_big
                int jie_big_width = iv_guaduan_big.getWidth();
                int jie_big_height = iv_guaduan_big.getHeight();
                jie_left_big = jie_yuan_x - (jie_big_width / 2);
                jie_top_big = jie_yuan_y - (jie_big_height / 2);
                jie_right_big = jie_yuan_x + (jie_big_width / 2);
                jie_bottom_big = jie_yuan_y + (jie_big_height / 2);

                finish_left = gua_left - gua_width / 2 + 10;
                finish_top = duan_top - duan_height / 2;
                finish_right = jie_right + jie_width / 2 - 10;
                finish_bottom = gua_bottom + gua_height / 2;
                
              //top guide
                top_guide_left = (int) (mYuanX-mBanJing);
                top_guide_top = (int) (mYuanY-mBanJing)- 4;
                top_guide_right = (int) (mYuanX+mBanJing)+DipPixelUtil.dip2px(getActivity(), 5);
                top_guide_bottom = (int)(mYuanY);

                if (mVersion > 16) {
                    mViewContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mViewContent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    private void startScreenBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mActivity.registerReceiver(mScreenReceiver, filter);
    }

    /**
     * screen状态广播接收者
     * 
     * @author zhangyg
     */
    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        private String action = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                isScreenOff = true;
            }
        }
    }

    public void setCanCel() {
        isStop = true;
        vib.cancel();
    }

    public void setFinishView() {
        mHandler.sendEmptyMessage(3);
        mHandler.sendEmptyMessageDelayed(4, 1000);
    }

    public void guaTurnBig() {
        iv_guaduan_big.setVisibility(View.VISIBLE);
        iv_guaduan.setVisibility(View.INVISIBLE);
    }

    public void guaTurnSmall() {
        iv_guaduan_big.setVisibility(View.INVISIBLE);
        iv_guaduan.setVisibility(View.VISIBLE);
    }

    public void duanTurnBig() {
        iv_duanxin.setVisibility(View.INVISIBLE);
        iv_duanxin_big.setVisibility(View.VISIBLE);
    }

    public void duanTurnSmall() {
        iv_duanxin_big.setVisibility(View.INVISIBLE);
        iv_duanxin.setVisibility(View.VISIBLE);
    }

    public void jieTurnBig() {
        iv_jieting_big.setVisibility(View.VISIBLE);
        iv_jieting.setVisibility(View.INVISIBLE);
    }

    public void jieTurnSmall() {
        iv_jieting_big.setVisibility(View.INVISIBLE);
        iv_jieting.setVisibility(View.VISIBLE);
    }

    public void allTurnSmall() {
        iv_guaduan_big.setVisibility(View.INVISIBLE);
        iv_guaduan.setVisibility(View.VISIBLE);
        iv_duanxin_big.setVisibility(View.INVISIBLE);
        iv_duanxin.setVisibility(View.VISIBLE);
        iv_jieting_big.setVisibility(View.INVISIBLE);
        iv_jieting.setVisibility(View.VISIBLE);
    }
}
