
package com.leo.appmaster.battery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.activity.PrivacyOptionActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.PasswdProtectActivity;
import com.leo.appmaster.applocker.PasswdTipActivity;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.home.SimpleAnimatorListener;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.impl.BatteryManagerImpl;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.BatteryMenu;
import com.leo.appmaster.ui.LeoHomePopMenu;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PropertyInfoUtil;
import com.leo.appmaster.utils.Utilities;
import com.leo.appmaster.wifiSecurity.WifiSecurityActivity;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.AnimatorListenerAdapter;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.ValueAnimator;

import java.util.ArrayList;
import java.util.List;

public class BatteryViewFragment extends BaseFragment implements View.OnTouchListener, SelfScrollView.ScrollBottomListener, View.OnClickListener {
    private static final int MOVE_UP = 1;
    private static final int MOVE_DOWN = 2;
    private static final int GREEN_ARROW_MOVE = 3;

    private static final int CHARING_TYPE_SPEED = 1;
    private static final int CHARING_TYPE_CONTINUOUS = 2;
    private static final int CHARING_TYPE_TRICKLE = 3;

    private View mTimeContent;
    private View mBatteryIcon;
    private View mRemainTimeContent;
    private BatteryTestViewLayout mSlideView;
    ViewGroup.LayoutParams mSlideParams;

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
    private View mHideTextView;
    private TextView mTvHideTime;
    private TextView mTvHideText;

    private View mSettingView;

    public static boolean isExpand = false;
    public static String mBatteryText;
    private int moveDistance = 350;
    private BatteryManagerImpl.BatteryState newState;
    private String mChangeType = BatteryManagerImpl.SHOW_TYPE_IN;
    private int mRemainTime;
    private int moveUpCount = 0;
    private int adContentHeight = 0;
    private int adExpandContentHeight = 0;
    private BatteryMenu mLeoPopMenu;

    //开始首页动画
    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MOVE_UP:
                    showMoveUp();
                    timeContentMoveSmall();
                    batteryIconMoveSmall();
                    break;
                case MOVE_DOWN:
                    showMoveDown();
                    timeContentMoveBig();
                    batteryIconMoveBig();
                    break;
                case GREEN_ARROW_MOVE:
                    arrowMove();
                    break;
            }
        }
    };

    private void batteryIconMoveBig() {
        ObjectAnimator animMoveX = ObjectAnimator.ofFloat(mBatteryIcon,
                "x", mBatteryIcon.getLeft() + mBatteryIcon.getWidth() / 5, mBatteryIcon.getLeft());
        ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mBatteryIcon,
                "y", mBatteryIcon.getTop()
                        - mBatteryIcon.getHeight() - DipPixelUtil.dip2px(mActivity, 40), mBatteryIcon.getTop());

        ObjectAnimator animScaleX = ObjectAnimator.ofFloat(mBatteryIcon,
                "scaleX", 0.6f, 1f);
        ObjectAnimator animScaleY = ObjectAnimator.ofFloat(mBatteryIcon,
                "scaleY", 0.6f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.play(animMoveX).with(animMoveY);
        set.play(animMoveY).with(animScaleX);
        set.play(animScaleX).with(animScaleY);
        set.setDuration(1000);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mHideTextView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mThreeMoveView.setVisibility(View.VISIBLE);
                mRemainTimeContent.setVisibility(View.VISIBLE);
            }
        });
        set.start();
    }

    private void batteryIconMoveSmall() {
        ObjectAnimator animMoveX = ObjectAnimator.ofFloat(mBatteryIcon,
                "x", mBatteryIcon.getLeft(), mBatteryIcon.getLeft() + mBatteryIcon.getWidth() / 5);
        ObjectAnimator animMoveY = ObjectAnimator.ofFloat(mBatteryIcon,
                "y", mBatteryIcon.getTop(), mBatteryIcon.getTop()
                        - mBatteryIcon.getHeight() - DipPixelUtil.dip2px(mActivity, 40));


        ObjectAnimator animScaleX = ObjectAnimator.ofFloat(mBatteryIcon,
                "scaleX", 1f, 0.6f);
        ObjectAnimator animScaleY = ObjectAnimator.ofFloat(mBatteryIcon,
                "scaleY", 1f, 0.6f);

        AnimatorSet set = new AnimatorSet();
        set.play(animMoveX).with(animMoveY);
        set.play(animMoveY).with(animScaleX);
        set.play(animScaleX).with(animScaleY);
        set.setDuration(1000);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mThreeMoveView.setVisibility(View.INVISIBLE);
                mRemainTimeContent.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mHideTextView.setVisibility(View.VISIBLE);
                if (newState.level == 100) {
                    mTvHideText.setText(mActivity.getString(R.string.screen_protect_charing_text_four));
                    mTvHideTime.setVisibility(View.GONE);
                } else {
                    mTvHideTime.setVisibility(View.VISIBLE);
                    if (mBatteryText != null) {
                        mTvHideTime.setVisibility(View.VISIBLE);
                        mTvHideTime.setText(Html.fromHtml(mBatteryText));
                    } else {
                        mTvHideTime.setVisibility(View.GONE);
                        LeoLog.d("testBatteryView", "mBatteryText is Empty");
                    }
                }
            }
        });
        set.start();
    }

    private void arrowMove() {
        float stayY;
        if (place == CHARING_TYPE_SPEED) {
            stayY = mThreeMoveView.getHeight() - mIvSpeed.getHeight() / 2 - DipPixelUtil.dip2px(mActivity, 2);
        } else if (place == CHARING_TYPE_CONTINUOUS) {
            stayY = mThreeMoveView.getHeight() / 2 - DipPixelUtil.dip2px(mActivity, 2);
        } else {
            stayY = mIvSpeed.getHeight() / 2;
        }
        mGreenArrow.setY(stayY);
    }

    private void timeContentMoveBig() {
        ObjectAnimator animScaleX = ObjectAnimator.ofFloat(mTimeContent,
                "scaleX", 0.8f, 1f);
        ObjectAnimator animScaleY = ObjectAnimator.ofFloat(mTimeContent,
                "scaleY", 0.8f, 1f);
        ObjectAnimator animMoveX = ObjectAnimator.ofFloat(mTimeContent,
                "x", mTimeContent.getLeft() - mTimeContent.getWidth() * 3 / 4, mTimeContent.getLeft());

        AnimatorSet set = new AnimatorSet();
        set.play(animScaleX).with(animScaleY);
        set.play(animScaleY).with(animMoveX);
        set.setDuration(1000);
        set.start();
    }

    private void timeContentMoveSmall() {
        ObjectAnimator animScaleX = ObjectAnimator.ofFloat(mTimeContent,
                "scaleX", 1f, 0.8f);
        ObjectAnimator animScaleY = ObjectAnimator.ofFloat(mTimeContent,
                "scaleY", 1f, 0.8f);
        ObjectAnimator animMoveX = ObjectAnimator.ofFloat(mTimeContent,
                "x", mTimeContent.getLeft(), mTimeContent.getLeft() - mTimeContent.getWidth() * 3 / 4);

        AnimatorSet set = new AnimatorSet();
        set.play(animScaleX).with(animScaleY);
        set.play(animScaleY).with(animMoveX);
        set.setDuration(1000);
        set.start();
    }


    @Override
    protected int layoutResourceId() {
        return R.layout.activity_battery_view_new;
    }

    @Override
    protected void onInitUI() {
        LeoLog.d("testBatteryView", "INIT UI");
        mTimeContent = findViewById(R.id.time_move_content);
        mBatteryIcon = findViewById(R.id.infos_content);
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
                mSlideParams = mSlideView.getLayoutParams();
            }
        });
        moveDistance = DipPixelUtil.dip2px(mActivity, 180);

        mScrollView = (SelfScrollView) findViewById(R.id.slide_content_sv);
        mScrollView.setScrollBottomListener(this);


        mThreeMoveView = findViewById(R.id.battery_icon_flag);
        mGreenArrow = (ImageView) findViewById(R.id.little_arrow);
        mIvTrickle = (ImageView) findViewById(R.id.iv_trickle);
        mTvTrickle = (TextView) findViewById(R.id.tv_trickle);
        mIvContinuous = (ImageView) findViewById(R.id.iv_continuous);
        mTvContinuous = (TextView) findViewById(R.id.tv_continuous);
        mIvSpeed = (ImageView) findViewById(R.id.iv_speed);
        mTvSpeed = (TextView) findViewById(R.id.tv_speed);
        mHideTextView = findViewById(R.id.hide_tv_content);
        mTvHideTime = (TextView) findViewById(R.id.hide_tv_one);
        mTvHideText = (TextView) findViewById(R.id.hide_tv_two);

        mSettingView = findViewById(R.id.ct_option_2_rl);
        mSettingView.setOnClickListener(this);

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

    private int place = 0;

    private void setBatteryPercent() {
        mTvLevel.setText(newState.level + "%");

        if (newState.level < 70) {
            place = CHARING_TYPE_SPEED;
            mIvTrickle.setBackgroundResource(R.drawable.bay_trickle);
            mIvContinuous.setBackgroundResource(R.drawable.bay_continuous);
            mIvSpeed.setBackgroundResource(R.drawable.bay_speed2);
        } else if (newState.level < 85) {
            place = CHARING_TYPE_CONTINUOUS;
            mIvTrickle.setBackgroundResource(R.drawable.bay_trickle);
            mIvContinuous.setBackgroundResource(R.drawable.bay_continuous2);
            mIvSpeed.setBackgroundResource(R.drawable.bay_speed);
        } else {
            place = CHARING_TYPE_TRICKLE;
            mIvTrickle.setBackgroundResource(R.drawable.bay_trickle2);
            mIvContinuous.setBackgroundResource(R.drawable.bay_continuous);
            mIvSpeed.setBackgroundResource(R.drawable.bay_speed);
        }

        mHandler.sendEmptyMessage(GREEN_ARROW_MOVE);

    }

    private void noTime() {
        mTvLeftTime.setVisibility(View.GONE);
        mTvTime.setVisibility(View.GONE);
    }


    public void setTime(int second) {
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
                mBatteryText = text;
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
            mBatteryText = text;
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

        final ObjectAnimator anim = ObjectAnimator.ofFloat(mSlideView,
                "scaleX", 1f, 1f);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setDuration(1);
        anim.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                if (moveUpCount <= moveDistance) {
                    mSlideParams.height = adContentHeight + moveUpCount;
                    mSlideView.setLayoutParams(mSlideParams);

                    moveUpCount = moveUpCount + 35;
                } else {
                    isExpand = true;
                    mShowing = false;
                    mScrollView.setScrollY(0);
                    mSlideView.setScrollView(false);
                    anim.cancel();
                }
            }
        });
        anim.start();

    }

    private void showMoveDown() {

        final ObjectAnimator anim = ObjectAnimator.ofFloat(mSlideView,
                "scaleX", 1f, 1f);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setDuration(1);
        anim.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                if (moveUpCount <= moveDistance) {
                    mSlideParams.height = adExpandContentHeight - moveUpCount;
                    mSlideView.setLayoutParams(mSlideParams);
                    moveUpCount = moveUpCount + 35;
                } else {
                    isExpand = false;
                    mShowing = false;
                    mScrollView.setScrollY(0);
                    mSlideView.setScrollView(false);
                    anim.cancel();
                }
            }
        });
        anim.start();


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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ct_option_2_rl:
//                Intent dlIntent = new Intent(mActivity, BatterySettingActivity.class);
//                dlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                dlIntent.putExtra(Constants.BATTERY_FROM, "battery");
//                mLockManager.filterPackage(mActivity.getPackageName(), 1000);
//                startActivity(dlIntent);

                initSettingMenu();
                mLeoPopMenu.setPopMenuItems(mActivity, getRightMenuItems(), null);
                mLeoPopMenu.showPopMenu(mActivity, mGreenArrow, null, null);
                break;
        }
    }

    private List<String> getRightMenuItems() {
        List<String> listItems = new ArrayList<String>();
        Context ctx = mActivity;
        listItems.add(ctx.getString(R.string.screen_protect_type_pop_one));
        return listItems;
    }

    private void initSettingMenu() {
        if (mLeoPopMenu != null) return;
        mLeoPopMenu = new BatteryMenu();
        mLeoPopMenu.setAnimation(R.style.RightEnterAnim);
        mLeoPopMenu.setListViewDivider(null);
    }


}
