
package com.leo.appmaster.fragment;

import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.Toast;

import com.leo.appmaster.PhoneInfo;
import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;

public class PretendAppUnknowCallFragment extends PretendFragment implements OnTouchListener {
    private float iv_guaduan_top, iv_guaduan_right, iv_guaduan_bottom, iv_duanxin_left,
            iv_duanxin_bottom, iv_duanxin_right, iv_jieting_top, iv_jieting_left,
            iv_jieting_bottom, iv_dianhua_hold_top, iv_dianhua_hold_right, iv_dianhua_hold_bottom,
            iv_dianhua_hold_left,iv_guaduan_left,iv_jieting_right,iv_duanxin_top;
    private ImageView iv_jieting, iv_guaduan, iv_duanxin, iv_dianhua_hold, iv_guaduan_big;
    private View bottom_view, activity_weizhuang_firstin;
    private boolean isFirstRound = false;
    private boolean isSecondRound = false;
    private boolean isThridRound = false;

    private boolean isControl = false;
    private float lc_top, lc_right, lc_bottom, lc_left;
    private int mVersion;
    private int startX;
    private int startY;

    @Override
    protected int layoutResourceId() {
        return R.layout.activity_unknowcall;
    }

    @Override
    protected void onInitUI() {
        activity_weizhuang_firstin = findViewById(R.id.activity_weizhuang_firstin);

        // make content match the screen
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        Window window = mActivity.getWindow();
        LayoutParams windowLayoutParams = window.getAttributes(); // 获取对话框当前的参数值
        windowLayoutParams.width = (int) (display.getWidth());
        windowLayoutParams.height = (int) (display.getHeight());
        activity_weizhuang_firstin.setLayoutParams(windowLayoutParams);

        bottom_view = findViewById(R.id.bottom_view);
        bottom_view.setOnTouchListener(this);

        iv_guaduan_big = (ImageView) findViewById(R.id.iv_guaduan_big);
        iv_jieting = (ImageView) findViewById(R.id.iv_jieting);
        iv_guaduan = (ImageView) findViewById(R.id.iv_guaduan);
        iv_duanxin = (ImageView) findViewById(R.id.iv_duanxin);

        iv_dianhua_hold = (ImageView) findViewById(R.id.iv_dianhua_hold);
        iv_dianhua_hold.setClickable(true);
        iv_dianhua_hold.setLongClickable(true);
        iv_dianhua_hold.setOnTouchListener(this);

        mVersion = PhoneInfo.getAndroidVersion();
        getThreeButton();
    }

    private void getThreeButton() {
        ViewTreeObserver guaduan = iv_guaduan.getViewTreeObserver();
        guaduan.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                iv_guaduan_top = iv_guaduan.getTop();
                iv_guaduan_right = iv_guaduan.getRight();
                iv_guaduan_bottom = iv_guaduan.getBottom();
                iv_guaduan_left = iv_guaduan.getLeft();
                // 成功调用一次后，移除 Hook 方法，防止被反复调用
                // removeGlobalOnLayoutListener() 方法在 API 16 后不再使用
                // 使用新方法 removeOnGlobalLayoutListener() 代替
                if (mVersion > 16) {
                    iv_guaduan.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    iv_guaduan.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

            }
        });

        ViewTreeObserver duanxin = iv_duanxin.getViewTreeObserver();
        duanxin.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                iv_duanxin_left = iv_duanxin.getLeft();
                iv_duanxin_bottom = iv_duanxin.getBottom();
                iv_duanxin_right = iv_duanxin.getRight();
                iv_duanxin_top = iv_duanxin.getTop();
                if (mVersion > 16) {
                    iv_duanxin.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    iv_duanxin.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        ViewTreeObserver jieting = iv_jieting.getViewTreeObserver();
        jieting.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                iv_jieting_top = iv_jieting.getTop();
                iv_jieting_left = iv_jieting.getLeft();
                iv_jieting_bottom = iv_jieting.getBottom();
                iv_jieting_right = iv_jieting.getRight();
                if (mVersion > 16) {
                    iv_jieting.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    iv_jieting.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        ViewTreeObserver hold_on = iv_dianhua_hold.getViewTreeObserver();
        hold_on.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                lc_top = iv_dianhua_hold.getTop();
                lc_left = iv_dianhua_hold.getLeft();
                lc_bottom = iv_dianhua_hold.getBottom();
                lc_right = iv_dianhua_hold.getRight();
                LeoLog.d("qazwsx", "lc_top is : " + lc_top
                        + "--lc_left is : " + lc_left + "--lc_bottom is : "
                        + lc_bottom + "----lc_right is : " + lc_right);
                if (mVersion > 16) {
                    iv_dianhua_hold.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    iv_dianhua_hold.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.bottom_view) {
            LeoLog.d("qazwsx", "bottom_view！");
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
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
            LeoLog.d("qazwsx", "iv_dianhua_hold！");
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

                    int top = (int) (iv_dianhua_hold_top + newY);
                    int left = (int) (iv_dianhua_hold_left + newX);
                    int bottom = (int) (iv_dianhua_hold_bottom + newY);
                    int right = (int) (iv_dianhua_hold_right + newX);

                    // 挂断状态，变大
                    if (left < iv_guaduan_right + 10 && top > iv_guaduan_top - iv_guaduan_top / 2
                            && bottom < iv_guaduan_bottom + iv_guaduan_bottom / 2) {
                        iv_guaduan_big.setVisibility(View.VISIBLE);
                        iv_guaduan.setVisibility(View.INVISIBLE);
                        isControl = false;
                    } else {
                        if (!isControl) {
                            iv_guaduan_big.setVisibility(View.INVISIBLE);
                            iv_guaduan.setVisibility(View.VISIBLE);
                        }
                        isControl = true;
                    }

                    if (left < iv_guaduan_left) {
                        left = (int) iv_guaduan_left;
                        right = left + v.getWidth();
                    }
                    if (right > iv_jieting_right) {
                        right = (int) iv_jieting_right;
                        left = right - v.getWidth();
                    }
                    if (top < iv_duanxin_top) {
                        top = (int) iv_duanxin_top;
                        bottom = top + v.getHeight();
                    }
                    if (bottom > iv_jieting_bottom + v.getHeight()) {
                        bottom = (int) (iv_jieting_bottom + v.getHeight());
                        top = (int) iv_jieting_bottom;
                    }

                    v.layout(left, top, right, bottom);

                    startX = (int) event.getRawX();
                    startY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:// 手指离开屏幕一瞬间
                    // // 记录控件距离屏幕左上角的坐标
                    if(iv_guaduan_big.getVisibility() == View.VISIBLE){
                        iv_guaduan_big.setVisibility(View.INVISIBLE);
                        iv_guaduan.setVisibility(View.VISIBLE);
                        onUnlockPretendFailed();
                    }else {
                        huiguiAnimation(v);
                    }
                    break;
            }
        }
        return false;
    }
    
    private void huiguiAnimation(View v) {
        v.layout((int) lc_left, (int) lc_top, (int) lc_right, (int) lc_bottom);
    }
    
    private void isShowOrder(float x, float y) {
        // 挂断?
        if (x < iv_guaduan_right && y > iv_guaduan_top && y < iv_guaduan_bottom) {
            // 进入挂断区域，先判断是否从别处进入
            if (!isFirstRound && !isSecondRound && !isThridRound) {
                // 首先进入挂断区域
                isFirstRound = true;
            } else {
                if (!isFirstRound) {
                    // 进入了别的区域
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
            } else {
                if (!isSecondRound) {
                    // 非正确进入第二
                    isFirstRound = false;
                    isThridRound = false;
                }
            }
        }
        // 接听?
        if (x > iv_jieting_left && y > iv_jieting_top && y < iv_jieting_bottom) {
            if (isFirstRound && isSecondRound && !isThridRound) {
                isThridRound = true;
                // 触发成功
                onUnlockPretendSuccessfully();
            } else {
                if (!isThridRound) {
                    // 非正确进入第三
                    isFirstRound = false;
                    isSecondRound = false;
                }
            }
        }
    }
}
