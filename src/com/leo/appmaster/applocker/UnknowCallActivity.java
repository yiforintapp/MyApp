
package com.leo.appmaster.applocker;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;

import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;

public class UnknowCallActivity extends Activity implements OnTouchListener {
    private final static int UnknowCallMode = 2;
    private View show_text_setting, bottom_view;
    private ImageView iv_jieting, iv_guaduan, iv_duanxin, iv_dianhua_hold;
    private float mDownX;
    private float iv_guaduan_top, iv_guaduan_right, iv_guaduan_bottom, iv_duanxin_left,
            iv_duanxin_bottom, iv_duanxin_right, iv_jieting_top, iv_jieting_left,
            iv_jieting_bottom, iv_dianhua_hold_top, iv_dianhua_hold_right, iv_dianhua_hold_bottom,
            iv_dianhua_hold_left, k_width, iv_dianhua_hold_height;
    private boolean isFirstRound = false;
    private boolean isSecondRound = false;
    private boolean isThridRound = false;
    private AppMasterPreference sp_unknowcall;
    private int startX;
    private int startY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unknowcall);
        init();
    }

    private void init() {

        sp_unknowcall = AppMasterPreference.getInstance(this);
        show_text_setting = findViewById(R.id.show_text_setting);
        show_text_setting.setVisibility(View.VISIBLE);
        bottom_view = findViewById(R.id.bottom_view);
        bottom_view.setOnTouchListener(this);

        iv_jieting = (ImageView) findViewById(R.id.iv_jieting);
        iv_guaduan = (ImageView) findViewById(R.id.iv_guaduan);
        iv_duanxin = (ImageView) findViewById(R.id.iv_duanxin);
        iv_dianhua_hold = (ImageView) findViewById(R.id.iv_dianhua_hold);
        iv_dianhua_hold.setOnTouchListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            iv_guaduan_top = iv_guaduan.getTop();
            iv_guaduan_right = iv_guaduan.getRight();
            iv_guaduan_bottom = iv_guaduan.getBottom();
            LeoLog.d("testfuck", "iv_guaduan_top is : " + iv_guaduan_top
                    + "--iv_guaduan_bottom is : " + iv_guaduan_bottom + "--iv_guaduan_right is : "
                    + iv_guaduan_right);

            iv_duanxin_left = iv_duanxin.getLeft();
            iv_duanxin_bottom = iv_duanxin.getBottom();
            iv_duanxin_right = iv_duanxin.getRight();
            LeoLog.d("testfuck", "iv_duanxin_left is : " + iv_duanxin_left
                    + "--iv_duanxin_bottom is : " + iv_duanxin_bottom + "--iv_duanxin_right is : "
                    + iv_duanxin_right);

            iv_jieting_top = iv_jieting.getTop();
            iv_jieting_left = iv_jieting.getLeft();
            iv_jieting_bottom = iv_jieting.getBottom();
            LeoLog.d("testfuck", "iv_jieting_top is : " + iv_jieting_top
                    + "--iv_jieting_left is : " + iv_jieting_left + "--iv_jieting_bottom is : "
                    + iv_jieting_bottom);

            LeoLog.d("testfuck", "iv_dianhua_hold_top is : " + iv_dianhua_hold_top
                    + "iv_dianhua_hold_left is :" + iv_dianhua_hold_left
                    + "iv_dianhua_hold_bottom is:" + iv_dianhua_hold_bottom
                    + "iv_dianhua_hold_right is :" + iv_dianhua_hold_right);
        }
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.bottom_view) {
            LeoLog.d("testfuck", "bottom_view！");
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = event.getX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    // LeoLog.d("testfuck", "X is : " + event.getX() + "Y is :"
                    // +
                    // event.getY());
                    float moveX = event.getX();
                    float moveY = event.getY();
                    isShowOrder(moveX, moveY);
                    break;
                case MotionEvent.ACTION_UP:
                    isFirstRound = false;
                    isSecondRound = false;
                    isThridRound = false;
                    break;
            }
        } else if (v.getId() == R.id.iv_dianhua_hold) {
            LeoLog.d("testfuck", "iv_dianhua_hold！");
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:// 手指按下屏幕
                    startX = (int) event.getRawX();
                    startY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:// 手指在屏幕上移动
                    int newX = (int) event.getRawX() - startX;
                    int newY = (int) event.getRawY() - startY;
                    
                    iv_dianhua_hold_top = v.getTop();
                    iv_dianhua_hold_left = v.getLeft();
                    iv_dianhua_hold_bottom = v.getBottom();
                    iv_dianhua_hold_right = v.getRight();
                    
                    LeoLog.d("testfuck", "event.getX() is : " + event.getX() + "----event.getY() is :" + event.getY());
//                    LeoLog.d("testfuck", "11startX is : " + startX+ "-------11startY is :" + startY);
//                    LeoLog.d("testfuck", "newX is : " + newX + "newY is :" + newY);

                    int top = (int) (iv_dianhua_hold_top + newY);
                    int left = (int) (iv_dianhua_hold_left + newX);
                    int bottom = (int) (iv_dianhua_hold_bottom + newY);
                    int right = (int) (iv_dianhua_hold_right + newX);
                    
//                    LeoLog.d("testfuck", "top is : " + top + "left is :" + left + "bottom is:"
//                            + bottom + "right is :" + right);

                    v.layout(left, top, right, bottom);
                    
                    startX = (int) event.getRawX();
                    startY = (int) event.getRawY();
//                    LeoLog.d("testfuck", "22startX is : " + startX+ "-----22startY is :" + startY);
                    break;
                case MotionEvent.ACTION_UP:// 手指离开屏幕一瞬间
                    // // 记录控件距离屏幕左上角的坐标
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    private void isShowOrder(float x, float y) {
        // 挂断?
        if (x < iv_guaduan_right && y > iv_guaduan_top && y < iv_guaduan_bottom) {
            // 进入挂断区域，先判断是否从别处进入
            if (!isFirstRound && !isSecondRound && !isThridRound) {
                // 首先进入挂断区域
                isFirstRound = true;
                LeoLog.d("testfuck", "首先进入挂断区域");
            } else {
                if (!isFirstRound) {
                    // 进入了别的区域
                    LeoLog.d("testfuck", "非首次进入挂断区域");
                    isSecondRound = false;
                    isThridRound = false;
                } else if (isSecondRound) {
                    isSecondRound = false;
                }
            }
        }
        // 短信?
        if (x > iv_duanxin_left && x < iv_duanxin_right && y < iv_duanxin_bottom) {
            if (isFirstRound && !isSecondRound && !isThridRound) {
                isSecondRound = true;
                // 顺利进入第二
                LeoLog.d("testfuck", "顺利进入第二");
            } else {
                if (!isSecondRound) {
                    // 非正确进入第二
                    LeoLog.d("testfuck", "非正确进入第二");
                    isFirstRound = false;
                    isThridRound = false;
                }
            }
        }
        // 接听?
        if (x > iv_jieting_left && y > iv_jieting_top && y < iv_jieting_bottom) {
            if (isFirstRound && isSecondRound && !isThridRound) {
                isThridRound = true;
                // 顺利进入第三
                LeoLog.d("testfuck", "顺利进入第三");
                // 触发成功
                LeoLog.d("testfuck", "触发成功");
                Toast.makeText(this, getString(R.string.weizhuang_setting_ok), 0).show();
                sp_unknowcall.setPretendLock(UnknowCallMode);
                finish();
            } else {
                if (!isThridRound) {
                    // 非正确进入第三
                    LeoLog.d("testfuck", "非正确进入第三");
                    isFirstRound = false;
                    isSecondRound = false;
                }
            }
        }
    }
}
