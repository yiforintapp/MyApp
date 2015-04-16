
package com.leo.appmaster.applocker;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

public class UnKnowCallActivity5 extends Activity implements OnTouchListener {
    private TextView tv_use_tips, tv_use_tips_content;
    private ImageView iv_dianhua_hold, iv_guaduan;
    private GestureRelative mViewContent;

    private int hold_left, hold_top, hold_right, hold_bottom;
    private float mYuanX, mYuanY, mZhiJing, mBanJing;
    private int startX, startY;
    private float lc_left, lc_top, lc_right, lc_bottom;
    private float hold_width, hold_height;
    
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    iv_dianhua_hold.layout(hold_left, hold_top, hold_right, hold_bottom);
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
        iv_guaduan = (ImageView) findViewById(R.id.iv_guaduan);

        mViewContent = (GestureRelative) findViewById(R.id.mid_view);
        iv_dianhua_hold = (ImageView) findViewById(R.id.iv_dianhua_hold);
        iv_dianhua_hold.setOnTouchListener(this);
        // iv_dianhua_hold = new ImageView(this);
        // iv_dianhua_hold.setBackground(getResources().getDrawable(R.drawable.disguise_call));
        // iv_dianhua_hold.setLayoutParams(new LayoutParams(56,56));

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {

            // hold_left = iv_dianhua_hold.getLeft();
            // hold_top = iv_dianhua_hold.getTop();
            // hold_right = iv_dianhua_hold.getRight();
            // hold_bottom = iv_dianhua_hold.getBottom();
            // LeoLog.d("testnewcall", "hold_left is : " + hold_left
            // + "--hold_top is : " + hold_top + "--hold_right is : "
            // + hold_right + "hold_bottom is : " + hold_bottom);

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

            // 电话柄位于？
            setHold();
        }
        super.onWindowFocusChanged(hasFocus);
    }

    private void setHold() {
        new Thread() {
            public void run() {
                try {
                    sleep(70);
                    handler.sendEmptyMessage(1);
                } catch (InterruptedException e) {
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

                    v.layout(left, top, right, bottom);
                    startX = (int) event.getRawX();
                    startY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    v.layout(hold_left, hold_top, hold_right, hold_bottom);
                    break;
                default:
                    break;
            }
        }
        return false;
    }
}
