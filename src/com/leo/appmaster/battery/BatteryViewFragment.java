
package com.leo.appmaster.battery;

import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.impl.BatteryManagerImpl;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PropertyInfoUtil;
import com.leo.appmaster.wifiSecurity.WifiSecurityActivity;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.ObjectAnimator;

public class BatteryViewFragment extends BaseFragment implements View.OnTouchListener, SelfScrollView.ScrollBottomListener {
    private static final int MOVE_UP = 1;
    private static final int MOVE_DOWN = 2;
    private View mTimeContent;
    private View mRemainTimeContent;
    private BatteryTestViewLayout mSlideView;

    private TextView mTvLevel;
    private TextView mTvBigTime;
    private TextView mTvSmallLeft;
    private TextView mTvSmallRight;
    private TextView mTvLeftTime;
    private TextView mTvTime;
    private SelfScrollView mScrollView;

    private View mThreeMoveView;
    private ImageView mGreenArrow;
    private ImageView mIvTrickle;
    private TextView mTvTrickle;
    private ImageView mIvContinuous;
    private TextView mTvContinuous;
    private ImageView mIvSpeed;
    private TextView mTvSpeed;


    public static boolean isExpand = false;
    private int moveDistance = 350;
    private BatteryManagerImpl.BatteryState newState;
    private String mChangeType = BatteryManagerImpl.SHOW_TYPE_IN;
    private int mRemainTime;

    private int moveUpCount = 0;
    private int adContentHeight = 0;
    private int adExpandContentHeight = 0;
    //开始首页动画
    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MOVE_UP:
                    showMoveUp();
                    break;
                case MOVE_DOWN:
                    showMoveDown();
                    break;
            }
        }
    };


    @Override
    protected int layoutResourceId() {
        return R.layout.activity_battery_view_new;
    }

    @Override
    protected void onInitUI() {

        LeoLog.d("testBatteryView", "INIT UI");
        mTimeContent = findViewById(R.id.time_content);
        mRemainTimeContent = findViewById(R.id.remain_time);

        mTvLevel = (TextView) findViewById(R.id.battery_num);
//        mTvBigTime = (TextView) findViewById(R.id.battery_num);
//        mTvSmallLeft = (TextView) findViewById(R.id.battery_num);
//        mTvSmallRight = (TextView) findViewById(R.id.battery_num);
        mTvLeftTime = (TextView) findViewById(R.id.left_time);
        mTvTime = (TextView) findViewById(R.id.right_time);

        mSlideView = (BatteryTestViewLayout) findViewById(R.id.slide_content);
        mSlideView.setOnTouchListener(this);
        mSlideView.post(new Runnable() {
            @Override
            public void run() {
                adContentHeight = mSlideView.getHeight();
            }
        });
        moveDistance = DipPixelUtil.dip2px(mActivity, 180);

        mScrollView = (SelfScrollView) findViewById(R.id.slide_content_sv);
        mScrollView.setScrollBottomListener(this);

        if (newState != null) {
            process(mChangeType, newState, mRemainTime);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isExpand = false;
        mShowing = false;
    }

    public void initCreate(String type, BatteryManager.BatteryState state, int remainTime) {
        mChangeType = type;
        newState = state;
        mRemainTime = remainTime;
    }

    public void process(String type, BatteryManager.BatteryState state, int remainTime) {
        mChangeType = type;
        newState = state;
        mRemainTime = remainTime;

        if (mChangeType.equals(BatteryManagerImpl.SHOW_TYPE_IN)) {
            notifyUI(mChangeType, true);
        } else if (mChangeType.equals(BatteryManagerImpl.SHOW_TYPE_OUT)) {
            notifyUI(mChangeType, false);
        } else if (mChangeType.equals(BatteryManagerImpl.UPDATE_UP)) {
            notifyUI(mChangeType, true);
        } else {
            notifyUI(mChangeType, false);
        }


    }

    public void notifyUI(String type, boolean isCharing) {

        if (mChangeType.equals(BatteryManagerImpl.SHOW_TYPE_IN) ||
                mChangeType.equals(BatteryManagerImpl.UPDATE_UP)) {
            setTime(mRemainTime);
        } else {
            noTime();
        }

        setBatteryPercent();

        if (isCharing) {

        } else {

        }

    }

    private void setBatteryPercent() {
        mTvLevel.setText(newState.level + "%");
    }

    private void noTime() {
        mTvLeftTime.setVisibility(View.GONE);
        mTvTime.setVisibility(View.GONE);
    }


    public void setTime(int second) {

        LeoLog.d("testBatteryView", "second : " + second);

        int h = 0;
        int d = 0;
        int s = 0;
        int temp = second % 3600;
        if (second > 3600) {
            h = second / 3600;
            if (temp != 0) {
                if (temp > 60) {
                    d = temp / 60;
                    if (temp % 60 != 0) {
                        s = temp % 60;
                    }
                } else {
                    s = temp;
                }
            }
        } else {
            d = second / 60;
            if (second % 60 != 0) {
                s = second % 60;
            }
        }


        String hString, dString;
        hString = String.valueOf(h);
        dString = String.valueOf(d);

        LeoLog.d("testBatteryView", "hString : " + hString + "dString : " + dString);

        if (hString.equals("0")) {
            if (!dString.equals("0")) {
                String text = mActivity.getString(R.string.screen_protect_time_right_two, dString);
                mTvLeftTime.setVisibility(View.VISIBLE);
                mTvTime.setVisibility(View.VISIBLE);
                mTvTime.setText(Html.fromHtml(text));
            } else {
                if (newState.level == 100) {
                    mTvLeftTime.setText(mActivity.getString(R.string.screen_protect_charing_text_four));
                    mTvTime.setVisibility(View.GONE);
                } else {
                    mTvLeftTime.setVisibility(View.GONE);
                    mTvTime.setVisibility(View.GONE);
                }
            }
        } else {
            String text = mActivity.getString(R.string.screen_protect_time_right, hString, dString);
            mTvLeftTime.setVisibility(View.VISIBLE);
            mTvTime.setVisibility(View.VISIBLE);
            mTvTime.setText(Html.fromHtml(text));
        }

    }

    public static boolean mShowing = false;
    private int staryY;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
//        if (view == mSlideView) {
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:// 手指按下屏幕
//                    LeoLog.d("testBatteryView", "ACTION_DOWN");
//                    staryY = (int) event.getRawY();
//                    break;
//                case MotionEvent.ACTION_MOVE:// 手指在屏幕上移动
//                    int newY = (int) event.getRawY();
//                    int moveY = newY - staryY;
//                    if (!isExpand) {
//                        if (moveY < -100 && !mShowing) {
//                            expandContent(true);
//                        }
//                    } else {
//                        if (moveY > 100 && !mShowing) {
//                            expandContent(false);
//                        }
//                    }
//
//                    break;
//                case MotionEvent.ACTION_UP:// 手指离开屏幕一瞬间
//
//                    break;
//            }
//
//        }
        return true;
    }

    private void showMoveUp() {
        if (moveUpCount <= moveDistance) {
            LeoLog.d("testBatteryView", "showMoveUp() : " + adContentHeight);

            ViewGroup.LayoutParams params = mSlideView.getLayoutParams();
            params.height = adContentHeight + moveUpCount;
            mSlideView.setLayoutParams(params);

            moveUpCount = moveUpCount + 30;
            mHandler.sendEmptyMessage(MOVE_UP);
        } else {
            isExpand = true;
            mShowing = false;
            mScrollView.setScrollY(0);
            mSlideView.setScrollView(false);
        }
    }

    private void showMoveDown() {
        if (moveUpCount <= moveDistance) {
            LeoLog.d("testBatteryView", "showMoveDown : " + adExpandContentHeight);

            ViewGroup.LayoutParams params = mSlideView.getLayoutParams();
            params.height = adExpandContentHeight - moveUpCount;
            mSlideView.setLayoutParams(params);

            moveUpCount = moveUpCount + 30;
            mHandler.sendEmptyMessage(MOVE_DOWN);
        } else {
            isExpand = false;
            mShowing = false;
            mScrollView.setScrollY(0);
            mSlideView.setScrollView(false);
        }
    }

    private void expandContent(boolean expand) {
        mShowing = true;

        if (expand) {
            moveUpCount = 0;
            mHandler.sendEmptyMessage(MOVE_UP);
        } else {
            moveUpCount = 0;
            adExpandContentHeight = mSlideView.getHeight();
            mHandler.sendEmptyMessage(MOVE_DOWN);
        }

    }

    @Override
    public void scrollBottom() {
        mSlideView.setScrollView(true);
        expandContent(false);
    }

    @Override
    public void scrollTop() {
        mSlideView.setScrollView(true);
        expandContent(true);
    }
}
