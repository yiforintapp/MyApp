
package com.leo.appmaster.applocker;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

public class UnKnowCallActivity5 extends Activity implements OnTouchListener {
    private TextView tv_use_tips, tv_use_tips_content;
    private ImageView iv_dianhua_hold, iv_guaduan, iv_duanxin, iv_jieting, iv_guaduan_big,
            iv_duanxin_big, iv_jieting_big,iv_tips_left,iv_tips_right;
    private GestureRelative mViewContent;

    private int hold_width, hold_height,hold_left, hold_top, hold_right, hold_bottom;
    private float mYuanX, mYuanY, mZhiJing, mBanJing;
    private int startX, startY;
    private float lc_left, lc_top, lc_right, lc_bottom;
    private int gua_yuan_x, gua_yuan_y, gua_left, gua_top, gua_right, gua_bottom;
    private int duan_yuan_x, duan_yuan_y, duan_left, duan_top, duan_right, duan_bottom;
    private int jie_yuan_x, jie_yuan_y, jie_left, jie_top, jie_right, jie_bottom;
    private int gua_left_big, gua_top_big, gua_right_big, gua_bottom_big;
    private int duan_left_big, duan_top_big, duan_right_big, duan_bottom_big;
    private int jie_left_big, jie_top_big, jie_right_big, jie_bottom_big;
    private int tip_left_x,tip_left_y,tip_left_left,tip_left_top,tip_left_right,tip_left_bottom;
    private int tip_right_x,tip_right_y,tip_right_left,tip_right_top,tip_right_right,tip_right_bottom;
    private boolean isControlGua = false;
    private boolean isControlDuan = false;
    private boolean isControlJie = false;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
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

                    iv_tips_left.layout(tip_left_left, tip_left_top, tip_left_right, tip_left_bottom);
                    iv_tips_right.layout(tip_right_left, tip_right_top, tip_right_right, tip_right_bottom);
                    
                    iv_tips_left.setVisibility(View.VISIBLE);
                    iv_tips_right.setVisibility(View.VISIBLE);
                    iv_dianhua_hold.setVisibility(View.VISIBLE);
                    iv_guaduan.setVisibility(View.VISIBLE);
                    iv_duanxin.setVisibility(View.VISIBLE);
                    iv_jieting.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unknowcall_five);
        init();
    }

    private void init() {
        tv_use_tips = (TextView) findViewById(R.id.tv_use_tips);
        tv_use_tips.setVisibility(View.VISIBLE);
        tv_use_tips_content = (TextView) findViewById(R.id.tv_use_tips_content);
        tv_use_tips_content.setVisibility(View.VISIBLE);

        mViewContent = (GestureRelative) findViewById(R.id.mid_view);
        mViewContent.setisFromActivity(true);
        mViewContent.setActivity(this);
        iv_dianhua_hold = (ImageView) findViewById(R.id.iv_dianhua_hold);
        iv_dianhua_hold.setOnTouchListener(this);

        iv_guaduan = (ImageView) findViewById(R.id.iv_guaduan);
        iv_duanxin = (ImageView) findViewById(R.id.iv_duanxin);
        iv_jieting = (ImageView) findViewById(R.id.iv_jieting);

        iv_guaduan_big = (ImageView) findViewById(R.id.iv_guaduan_big);
        iv_duanxin_big = (ImageView) findViewById(R.id.iv_duanxin_big);
        iv_jieting_big = (ImageView) findViewById(R.id.iv_jieting_big);

        iv_tips_left = (ImageView) findViewById(R.id.iv_tips_left);
        iv_tips_right = (ImageView) findViewById(R.id.iv_tips_right);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {

            hold_width = iv_dianhua_hold.getWidth();
            hold_height = iv_dianhua_hold.getHeight();

            // 圆心在？
            mYuanX = mViewContent.getPointX();
            mYuanY = mViewContent.getPointY();
            LeoLog.d("testnewcall", "mYuanX : " + mYuanX + "mYuanY : " + mYuanY);

            // 直径是？
            mZhiJing = mViewContent.getZhiJing();
            mBanJing = mZhiJing / 2;
            LeoLog.d("testnewcall", "mZhiJing : " + mZhiJing + "mBanJing " + mBanJing);

            hold_left = (int) (mYuanX - (hold_width / 2));
            hold_top = (int) (mYuanY - (hold_height / 2));
            hold_right = (int) (mYuanX + (hold_width / 2));
            hold_bottom = (int) (mYuanY + (hold_height / 2));
            LeoLog.d("testnewcall", "hold_left is : " + hold_left
                    + "--hold_top is : " + hold_top + "--hold_right is : "
                    + hold_right + "hold_bottom is : " + hold_bottom);

            // 挂断，短信，接听icon位置
            setPosition();
            // 电话柄位于？
            setHold();
        }
        super.onWindowFocusChanged(hasFocus);
    }

    private void setPosition() {
        // 挂断
        gua_yuan_x = (int) (mYuanX - mBanJing);
        gua_yuan_y = (int) mYuanY;
        int gua_width = iv_guaduan.getWidth();
        int gua_height = iv_guaduan.getHeight();
        gua_left = gua_yuan_x - (gua_width / 2);
        gua_top = gua_yuan_y - (gua_height / 2);
        gua_right = gua_yuan_x + (gua_width / 2);
        gua_bottom = gua_yuan_y + (gua_height / 2);
        mViewContent.setGuaPosition(gua_left, gua_top, gua_right, gua_bottom);
        // big
        int gua_big_width = iv_guaduan_big.getWidth();
        int gua_big_height = iv_guaduan_big.getHeight();
        gua_left_big = gua_yuan_x - (gua_big_width / 2);
        gua_top_big = gua_yuan_y - (gua_big_height / 2);
        gua_right_big = gua_yuan_x + (gua_big_width / 2);
        gua_bottom_big = gua_yuan_y + (gua_big_height / 2);

        // 短信
        duan_yuan_x = (int) mYuanX;
        duan_yuan_y = (int) (mYuanY - mBanJing);
        int duan_width = iv_duanxin.getWidth();
        int duan_height = iv_duanxin.getHeight();
        duan_left = duan_yuan_x - (duan_width / 2);
        duan_top = duan_yuan_y - (duan_height / 2);
        duan_right = duan_yuan_x + (duan_width / 2);
        duan_bottom = duan_yuan_y + (duan_height / 2);
        mViewContent.setDuanPosition(duan_left, duan_top, duan_right, duan_bottom);
        // big
        int duan_big_width = iv_duanxin_big.getWidth();
        int duan_big_height = iv_duanxin_big.getHeight();
        duan_left_big = duan_yuan_x - (duan_big_width / 2);
        duan_top_big = duan_yuan_y - (duan_big_height / 2);
        duan_right_big = duan_yuan_x + (duan_big_width / 2);
        duan_bottom_big = duan_yuan_y + (duan_big_height / 2);

        // 接听
        jie_yuan_x = (int) (mYuanX + mBanJing);
        jie_yuan_y = (int) mYuanY;
        int jie_width = iv_guaduan.getWidth();
        int jie_height = iv_guaduan.getHeight();
        jie_left = jie_yuan_x - (jie_width / 2);
        jie_top = jie_yuan_y - (jie_height / 2);
        jie_right = jie_yuan_x + (jie_width / 2);
        jie_bottom = jie_yuan_y + (jie_height / 2);
        mViewContent.setJiePosition(jie_left, jie_top, jie_right, jie_bottom);
        // big
        int jie_big_width = iv_guaduan_big.getWidth();
        int jie_big_height = iv_guaduan_big.getHeight();
        jie_left_big = jie_yuan_x - (jie_big_width / 2);
        jie_top_big = jie_yuan_y - (jie_big_height / 2);
        jie_right_big = jie_yuan_x + (jie_big_width / 2);
        jie_bottom_big = jie_yuan_y + (jie_big_height / 2);
        
        //left tip
        tip_left_x = (gua_yuan_x + duan_yuan_x)/2;
        tip_left_y = (gua_yuan_y + duan_yuan_y)/2;
        int tip_left_width = iv_tips_left.getWidth();
        int tip_left_height = iv_tips_left.getHeight();
        tip_left_left = tip_left_x - (tip_left_width/2);
        tip_left_top = tip_left_y - (tip_left_height/2);
        tip_left_right = tip_left_x + (tip_left_width/2);
        tip_left_bottom = tip_left_y + (tip_left_height/2);
        
        //right tip
        tip_right_x = (jie_yuan_x+ duan_yuan_x)/2;
        tip_right_y = (jie_yuan_y + duan_yuan_y)/2;
        int tip_right_width = iv_tips_right.getWidth();
        int tip_right_height = iv_tips_right.getHeight();
        tip_right_left = tip_right_x - (tip_right_width/2);
        tip_right_top = tip_right_y - (tip_right_height/2);
        tip_right_right = tip_right_x + (tip_right_width/2);
        tip_right_bottom = tip_right_y + (tip_right_height/2);
    }

    private void setHold() {
        new Thread() {
            public void run() {
                try {
                    sleep(150);
                    handler.sendEmptyMessage(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        }.start();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.iv_dianhua_hold) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:// 手指按下屏幕
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
                            && bottom < gua_bottom + gua_bottom / 2 && right > gua_left - 10) {
                        iv_guaduan_big.setVisibility(View.VISIBLE);
                        iv_guaduan.setVisibility(View.INVISIBLE);
                        isControlGua = false;
                    } else {
                        if (!isControlGua) {
                            iv_guaduan_big.setVisibility(View.INVISIBLE);
                            iv_guaduan.setVisibility(View.VISIBLE);
                        }
                        isControlGua = true;
                    }

                    if (top < duan_bottom + 10 && bottom > duan_top - 10
                            && left > duan_left - duan_left / 2
                            && right < duan_right + duan_right / 2) {
                        iv_duanxin.setVisibility(View.INVISIBLE);
                        iv_duanxin_big.setVisibility(View.VISIBLE);
                        isControlDuan = false;
                    } else {
                        if (!isControlDuan) {
                            iv_duanxin.setVisibility(View.VISIBLE);
                            iv_duanxin_big.setVisibility(View.INVISIBLE);
                        }
                        isControlDuan = true;
                    }

                    if (right > jie_left - 10 && top > jie_top - jie_top / 2
                            && bottom < jie_bottom + jie_bottom / 2 && left < jie_right + 10) {
                        iv_jieting_big.setVisibility(View.VISIBLE);
                        iv_jieting.setVisibility(View.INVISIBLE);
                        isControlJie = false;
                    } else {
                        if (!isControlJie) {
                            iv_jieting_big.setVisibility(View.INVISIBLE);
                            iv_jieting.setVisibility(View.VISIBLE);
                        }
                        isControlJie = true;
                    }

                    v.layout(left, top, right, bottom);
                    startX = (int) event.getRawX();
                    startY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    v.layout(hold_left, hold_top, hold_right, hold_bottom);

                    iv_guaduan_big.setVisibility(View.INVISIBLE);
                    iv_guaduan.setVisibility(View.VISIBLE);
                    iv_duanxin_big.setVisibility(View.INVISIBLE);
                    iv_duanxin.setVisibility(View.VISIBLE);
                    iv_jieting_big.setVisibility(View.INVISIBLE);
                    iv_jieting.setVisibility(View.VISIBLE);

                    break;
                default:
                    break;
            }
        }
        return false;
    }
}
