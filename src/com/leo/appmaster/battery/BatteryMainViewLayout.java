package com.leo.appmaster.battery;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.ObjectAnimator;

import org.w3c.dom.Text;


public class BatteryMainViewLayout extends RelativeLayout {
    private static final int API_LEVEL_19 = 19;
    private static int currSDK_INT = Build.VERSION.SDK_INT;
    private static View mMoveContent;
    private boolean mIsCharing = false;
    private int mBatteryLevel;
    private String mTime;
    private View mTimeContent;
    private TextView mTvLevel;
    private TextView mTvStatus;
    private TextView mTvBigTime;
    private TextView mTvSmallLeft;
    private TextView mTvSmallRight;
    private TextView mTvTime;
    private BatteryProtectSlideView mSlide;


    public BatteryMainViewLayout(Context context) {
        super(context);
    }

    public BatteryMainViewLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryMainViewLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMoveContent = findViewById(R.id.move_content);
        mTimeContent = findViewById(R.id.time_content);

        mTvLevel = (TextView) findViewById(R.id.battery_num);
        mTvStatus = (TextView) findViewById(R.id.battery_status);

        mTvBigTime = (TextView) findViewById(R.id.battery_num);
        mTvSmallLeft = (TextView) findViewById(R.id.battery_num);
        mTvSmallRight = (TextView) findViewById(R.id.battery_num);
        mTvTime = (TextView) findViewById(R.id.right_time);

        mSlide = (BatteryProtectSlideView) findViewById(R.id.slider);

        fillTime();
    }

    private void fillTime() {
        String string = "17";
        String string2 = "33";
        String text = mContext.getString(R.string.screen_protect_time_right, string, string2);
        mTvTime.setText(Html.fromHtml(text));
    }

    private int startX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (currSDK_INT >= API_LEVEL_19 && !isAttachedToWindow()) {
            return false;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:// 手指按下屏幕
                startX = (int) event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:// 手指在屏幕上移动
                int newX = (int) event.getRawX() - startX;

                int stayL = mMoveContent.getLeft();
                int stayR = mMoveContent.getRight();

                int left = stayL + newX;
                int right = stayR + newX;

                if (left <= 0) {
                    left = 0;
                    right = mMoveContent.getWidth();
                }

                LeoLog.d("testToastMove", "left : " + left);
                LeoLog.d("testToastMove", "right : " + right);

                mMoveContent.layout(left, mMoveContent.getTop(), right, mMoveContent.getBottom());
                startX = (int) event.getRawX();

                break;
            case MotionEvent.ACTION_UP:// 手指离开屏幕一瞬间

                if (mMoveContent.getLeft() < mMoveContent.getWidth() / 3) {
                    mMoveContent.layout(0, mMoveContent.getTop(), mMoveContent.getWidth(), mMoveContent.getBottom());
                } else {
                    unLockAnimation();
                }

                break;
        }

        return false;
    }

    private static void unLockAnimation() {
        ObjectAnimator animX = ObjectAnimator.ofFloat(mMoveContent,
                "x", mMoveContent.getLeft(), mMoveContent.getWidth());
        animX.setDuration(300);
        animX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                BatterProtectView.handleHide();
            }
        });
        animX.start();
    }

    public void notifyUI(boolean isCharing, int level, String time) {
        mIsCharing = isCharing;
        mBatteryLevel = level;
        mTime = time;

        if (isCharing) {
            mTvLevel.setVisibility(VISIBLE);
            mTvLevel.setText("电量 : " + level);
        } else {
            mTvLevel.setVisibility(GONE);
        }


    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        super.dispatchKeyEvent(event);
        LeoLog.d("testDisPatch", "dispatchKeyEvent");

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            BatterProtectView.handleHide();
        }
        return false;
    }

}
