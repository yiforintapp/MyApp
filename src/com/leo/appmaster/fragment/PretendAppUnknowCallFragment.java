
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

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;

public class PretendAppUnknowCallFragment extends PretendFragment implements OnTouchListener {
    private float iv_guaduan_top, iv_guaduan_right, iv_guaduan_bottom, iv_duanxin_left,
            iv_duanxin_bottom, iv_duanxin_right, iv_jieting_top, iv_jieting_left,
            iv_jieting_bottom;
    private ImageView iv_jieting, iv_guaduan, iv_duanxin;
    private View bottom_view, activity_weizhuang_firstin;
    private boolean isFirstRound = false;
    private boolean isSecondRound = false;
    private boolean isThridRound = false;

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

        iv_jieting = (ImageView) findViewById(R.id.iv_jieting);
        iv_guaduan = (ImageView) findViewById(R.id.iv_guaduan);
        iv_duanxin = (ImageView) findViewById(R.id.iv_duanxin);

        getThreeButton();
    }

    private void getThreeButton() {
        ViewTreeObserver guaduan = iv_guaduan.getViewTreeObserver();
        guaduan.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                iv_guaduan_top = iv_guaduan.getTop();
                iv_guaduan_right = iv_guaduan.getRight();
                iv_guaduan_bottom = iv_guaduan.getBottom();
                LeoLog.d("PretendAppUnknowCallFragment", "iv_guaduan_top is : " + iv_guaduan_top
                        + "--iv_guaduan_bottom is : " + iv_guaduan_bottom
                        + "--iv_guaduan_right is : "
                        + iv_guaduan_right);
                // 成功调用一次后，移除 Hook 方法，防止被反复调用
                // removeGlobalOnLayoutListener() 方法在 API 16 后不再使用
                // 使用新方法 removeOnGlobalLayoutListener() 代替
                iv_guaduan.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        ViewTreeObserver duanxin = iv_duanxin.getViewTreeObserver();
        duanxin.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                iv_duanxin_left = iv_duanxin.getLeft();
                iv_duanxin_bottom = iv_duanxin.getBottom();
                iv_duanxin_right = iv_duanxin.getRight();
                LeoLog.d("PretendAppUnknowCallFragment", "iv_duanxin_left is : " + iv_duanxin_left
                        + "--iv_duanxin_bottom is : " + iv_duanxin_bottom
                        + "--iv_duanxin_right is : "
                        + iv_duanxin_right);
                iv_duanxin.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        ViewTreeObserver jieting = iv_jieting.getViewTreeObserver();
        jieting.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                iv_jieting_top = iv_jieting.getTop();
                iv_jieting_left = iv_jieting.getLeft();
                iv_jieting_bottom = iv_jieting.getBottom();
                LeoLog.d("PretendAppUnknowCallFragment", "iv_jieting_top is : " + iv_jieting_top
                        + "--iv_jieting_left is : " + iv_jieting_left + "--iv_jieting_bottom is : "
                        + iv_jieting_bottom);
                iv_jieting.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                LeoLog.d("PretendAppUnknowCallFragment", "X is : " + moveX + "Y is :" +
                        moveY);
                 isShowOrder(moveX, moveY);
                break;
            case MotionEvent.ACTION_UP:
                isFirstRound = false;
                isSecondRound = false;
                isThridRound = false;
                LeoLog.d("testfuck", "举手，重置所有顺序！");
                break;
        }
        return false;
    }

    private void isShowOrder(float x, float y) {
        //挂断?
        if(x < iv_guaduan_right && y >iv_guaduan_top && y<iv_guaduan_bottom){
            //进入挂断区域，先判断是否从别处进入
            if(!isFirstRound && !isSecondRound && !isThridRound){
                //首先进入挂断区域
                isFirstRound = true;
            }else {
                if(!isFirstRound){
                    //进入了别的区域
                    isSecondRound = false;
                    isThridRound = false;
                }else if(isSecondRound){
                    isSecondRound = false;
                }
            }
        }
        //短信?
        if(x>iv_duanxin_left && x<iv_duanxin_right && y<iv_duanxin_bottom){
            if(isFirstRound && !isSecondRound && !isThridRound){
                isSecondRound = true;
                //顺利进入第二
            }else {
                if(!isSecondRound){
                    //非正确进入第二
                    isFirstRound = false;
                    isThridRound = false;
                }
            }
        }
        //接听?
        if(x>iv_jieting_left && y>iv_jieting_top && y<iv_jieting_bottom){
            if(isFirstRound && isSecondRound && !isThridRound){
                isThridRound = true;
                //触发成功
                onUnlockPretendSuccessfully();
            }else {
                if(!isThridRound){
                    //非正确进入第三
                    isFirstRound = false;
                    isSecondRound = false;
                }
            }
        }
    }
}
