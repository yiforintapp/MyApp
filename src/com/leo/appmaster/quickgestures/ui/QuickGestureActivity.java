
package com.leo.appmaster.quickgestures.ui;

import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.PhoneInfo;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.quickgestures.QuickGestureProxyActivity;
import com.leo.appmaster.quickgestures.ui.QuickGestureRadioSeekBarDialog.OnDiaogClickListener;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.DipPixelUtil;

/**
 * QuickGestureActivity
 * 
 * @author run
 */
public class QuickGestureActivity extends BaseActivity implements OnTouchListener, OnClickListener {
    private CommonTitleBar mTitleBar;
    private AppMasterPreference mPre;
    private QuickGestureRadioSeekBarDialog mAlarmDialog;
    public static boolean mAlarmDialogFlag = false;
    private TextView mLeftTopView, mLeftBottomView, mRightTopView, mRightBottomView;
    private FrameLayout mGestureSwitchView;
    private RelativeLayout mActivityRootView, mSlideGuideView, mEditGuideView, mTipRL;
    private ImageView mHandImage, mArrowImage, mRotationImage, mRotationCloseBgImage,
            mRotationOpenBgImage, mSlideStopImage, mEditStopImage, mEditVideoBgImage
            , mSlideGuidehand, mSlideGuideArrow;
    private TextView mGestureSwitch;
    private Button mSlideAreaSetBtn;
    private VideoView mEditVideoView;
    private boolean mFlag, mOpenQuickFlag;
    public static final String FROME_STATUSBAR = "from_statusbar";
    private boolean mFromShortcut, isRoating, isTranslating;
    public static boolean isSureBt;
    private AnimatorSet mSlideGuideAnim;
    private boolean leftBottomTemp, leftCenterTemp, rightBottomTemp, RightCenterTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_gesture);
        mPre = AppMasterPreference.getInstance(this);
        initUi();
        Intent intent = getIntent();
        if (intent != null) {
            mFromShortcut = intent.getBooleanExtra(FROME_STATUSBAR, false);
        }
        LeoEventBus.getDefaultBus().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initQuickSwitch();
        if (!mPre.getSwitchOpenQuickGesture()) {
            // 初始化快捷手势数据
            AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {
                @Override
                public void run() {
                    QuickGestureManager.getInstance(getApplicationContext()).init();
                }
            });
        }
        if (!mPre.getFristSlidingTip()) {
            gestureTranslationAnim(mHandImage, mArrowImage);
            mTipRL.setVisibility(View.VISIBLE);
            mTipRL.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            quickTipAnim(mTipRL);
            mLeftTopView.setOnTouchListener(this);
            mLeftBottomView.setOnTouchListener(this);
            mRightTopView.setOnTouchListener(this);
            mRightBottomView.setOnTouchListener(this);
            // 初始化快捷手势数据
            AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {
                @Override
                public void run() {
                    QuickGestureManager.getInstance(getApplicationContext()).init();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mPre.getFristSlidingTip() || !mPre.getSwitchOpenQuickGesture()) {
            AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {
                @Override
                public void run() {
                    // 反初始化快捷手势数据
                    QuickGestureManager.getInstance(getApplicationContext()).unInit();
                }
            });
        }
        LeoEventBus.getDefaultBus().unregister(this);
    }
    public void onEventMainThread(PrivacyEditFloatEvent event) {
        if (QuickGestureManager.getInstance(this).QUICK_GESTURE_SETTING_EVENT
                .equals(event.editModel)) {
            FloatWindowHelper.initSlidingArea(AppMasterPreference.getInstance(this));
            setShowSlideAllArea();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    private void setShowSlideAllArea() {
        FloatWindowHelper.removeAllFloatWindow(this);
         FloatWindowHelper.createFloatWindow(getApplicationContext(),
         QuickGestureManager.getInstance(getApplicationContext()).mSlidAreaSize);
    }

    private void initUi() {
        mTitleBar = (CommonTitleBar) findViewById(R.id.layout_quick_gesture_title_bar);
        mTitleBar.openBackView();
        mTitleBar.setTitle(R.string.pg_appmanager_quick_gesture_name);
        mTitleBar.setOptionImageVisibility(View.VISIBLE);
        mTitleBar.setOptionImage(R.drawable.setup);
        mTitleBar.setOptionListener(this);

        mActivityRootView = (RelativeLayout) findViewById(R.id.quick_gesture_helping);
        mGestureSwitchView = (FrameLayout) findViewById(R.id.gesure_switch);
        mGestureSwitchView.requestFocus();
        mRotationImage = (ImageView) findViewById(R.id.gesture_rotation_iv);
        mRotationCloseBgImage = (ImageView) findViewById(R.id.rotaion_close_bg);
        mRotationOpenBgImage = (ImageView) findViewById(R.id.rotaion_open_bg);
        mGestureSwitch = (TextView) findViewById(R.id.gesture_switch_text);
        mGestureSwitch.setOnClickListener(this);
        mSlideAreaSetBtn = (Button) findViewById(R.id.slide_setting_button);

        mSlideGuideView = (RelativeLayout) findViewById(R.id.slide_guide_show);
        mSlideGuideView.setOnClickListener(this);
        mSlideGuidehand = (ImageView) mSlideGuideView.findViewById(R.id.gesture_arrow_hand);
        mSlideGuideArrow = (ImageView) mSlideGuideView.findViewById(R.id.gesture_arrow_iv);
        mSlideStopImage = (ImageView) mSlideGuideView.findViewById(R.id.slide_stop_iv);

        mEditGuideView = (RelativeLayout) findViewById(R.id.edit_guide_show);
        mEditGuideView.setOnClickListener(this);
        mEditVideoView = (VideoView) mEditGuideView.findViewById(R.id.edit_video);
        mEditStopImage = (ImageView) mEditGuideView.findViewById(R.id.edit_stop_iv);
        mEditVideoBgImage = (ImageView) mEditGuideView.findViewById(R.id.edit_video_bg_iv);

        mTipRL = (RelativeLayout) findViewById(R.id.quick_tipRL);
        mLeftTopView = (TextView) findViewById(R.id.gesture_left_tips_top_tv);
        mLeftBottomView = (TextView) findViewById(R.id.gesture_left_tips_bottom);
        mRightTopView = (TextView) findViewById(R.id.gesture_right_tips_top_tv);
        mRightBottomView = (TextView) findViewById(R.id.gesture_right_tips_bottom);
        mHandImage = (ImageView) findViewById(R.id.gesture_handIV);
        mArrowImage = (ImageView) findViewById(R.id.gesture_arrowIV);
    }

    private void initQuickSwitch() {
        // open quick gesture
        mOpenQuickFlag = mPre.getSwitchOpenQuickGesture();
        if (mOpenQuickFlag) {
            mGestureSwitchView.setBackgroundResource(R.drawable.gesture_open_bg);
            mRotationImage.setImageResource(R.drawable.gesture_rotation_open);
            mRotationCloseBgImage.setAlpha(0f);
            mRotationOpenBgImage.setAlpha(1.0f);
            mGestureSwitch.setBackgroundResource(R.drawable.gesture_open_selecter);
            mGestureSwitch.setText(R.string.quick_gesture_open_text);
            mGestureSwitch.setTextColor(getResources().getColor(R.color.quick_open_text_color));
            setOnClickListener();
        } else {
            mGestureSwitchView.setBackgroundResource(R.drawable.gesture_close_bg);
            mRotationImage.setImageResource(R.drawable.gesture_rotation_close);
            mRotationOpenBgImage.setAlpha(0f);
            mRotationCloseBgImage.setAlpha(1.0f);
            mGestureSwitch.setBackgroundResource(R.drawable.gesture_close_selecter);
            mGestureSwitch.setText(R.string.quick_gesture_close_text);
            mGestureSwitch.setTextColor(getResources().getColor(R.color.quick_close_text_color));
            unSetOnClickListener();
        }
    }

    private void setOnClickListener() {
        mSlideAreaSetBtn.setOnClickListener(this);
    }

    private void unSetOnClickListener() {
        mSlideAreaSetBtn.setOnClickListener(null);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mAlarmDialogFlag) {
            FloatWindowHelper.mEditQuickAreaFlag = true;
            updateFloatWindowBackGroudColor();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (FloatWindowHelper.mEditQuickAreaFlag == true) {
            FloatWindowHelper.mEditQuickAreaFlag = false;
            updateFloatWindowBackGroudColor();
        }
    }

    class DialogRadioBean {
        String name;
        boolean isCheck;
    }

    // sliding area setting dialog
    private void showSettingDialog(boolean flag) {
        if (mAlarmDialog == null) {
            mAlarmDialog = new QuickGestureRadioSeekBarDialog(this);
        }
        mAlarmDialog.setShowRadioListView(flag);
        mAlarmDialog
                .setTitle(R.string.pg_appmanager_quick_gesture_option_sliding_area_location_title);
        mAlarmDialog.setSeekBarTextVisibility(false);
        mAlarmDialog.setSeekbarTextProgressVisibility(false);
        mAlarmDialog.setSeekBarProgressValue(mPre.getQuickGestureDialogSeekBarValue());
        mAlarmDialog.setLeftBottomOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // left bottom
                boolean leftBottomStatus = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isLeftBottom;
                if (leftBottomStatus) {
                    mAlarmDialog.setLeftBottomBackgroud(QuickGestureActivity.this.getResources()
                            .getDrawable(R.drawable.unselect));
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isLeftBottom = false;
                } else {
                    mAlarmDialog.setLeftBottomBackgroud(QuickGestureActivity.this.getResources()
                            .getDrawable(R.drawable.select));
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isLeftBottom = true;
                }
                FloatWindowHelper.setShowSlideArea(QuickGestureActivity.this,
                        FloatWindowHelper.QUICK_GESTURE_LEFT_SLIDE_AREA);
            }
        });
        mAlarmDialog.setRightBottomOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // right bottom
                boolean rightBottomStatus = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isRightBottom;
                if (rightBottomStatus) {
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isRightBottom = false;
                    mAlarmDialog.setRightBottomBackgroud(QuickGestureActivity.this.getResources()
                            .getDrawable(R.drawable.unselect));
                } else {
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isRightBottom = true;
                    mAlarmDialog.setRightBottomBackgroud(QuickGestureActivity.this.getResources()
                            .getDrawable(R.drawable.select));

                }
                FloatWindowHelper.setShowSlideArea(QuickGestureActivity.this,
                        FloatWindowHelper.QUICK_GESTURE_RIGHT_SLIDE_AREA);
            }
        });
        mAlarmDialog.setLeftCenterOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // left center
                boolean leftCenterStatus = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isLeftCenter;
                if (leftCenterStatus) {
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isLeftCenter = false;
                    mAlarmDialog.setLeftCenterBackgroud(QuickGestureActivity.this.getResources()
                            .getDrawable(R.drawable.unselect));
                } else {
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isLeftCenter = true;
                    mAlarmDialog.setLeftCenterBackgroud(QuickGestureActivity.this.getResources()
                            .getDrawable(R.drawable.select));
                }
                FloatWindowHelper.setShowSlideArea(QuickGestureActivity.this,
                        FloatWindowHelper.QUICK_GESTURE_LEFT_SLIDE_AREA);
            }
        });
        mAlarmDialog.setRightCenterOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // right center
                boolean rightCenterStatus = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isRightCenter;
                if (rightCenterStatus) {
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isRightCenter = false;
                    mAlarmDialog.setRightCenterBackgroud(QuickGestureActivity.this.getResources()
                            .getDrawable(R.drawable.unselect));
                } else {
                    QuickGestureManager.getInstance(AppMasterApplication.getInstance()).isRightCenter = true;
                    mAlarmDialog.setRightCenterBackgroud(QuickGestureActivity.this.getResources()
                            .getDrawable(R.drawable.select));
                }
                FloatWindowHelper.setShowSlideArea(QuickGestureActivity.this,
                        FloatWindowHelper.QUICK_GESTURE_RIGHT_SLIDE_AREA);
            }
        });
        mAlarmDialog.setOnClickListener(new OnDiaogClickListener() {

            @Override
            public void onClick(int progress) {
                isSureBt = true;
                boolean mLeftBottom = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isLeftBottom;
                boolean mRightBottm = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isRightBottom;
                boolean mLeftCenter = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isLeftCenter;
                boolean mRightCenter = QuickGestureManager.getInstance(AppMasterApplication
                        .getInstance()).isRightCenter;
                if (mLeftBottom || mRightBottm || mLeftCenter || mRightCenter) {
                    // save progress value
                    mPre.setQuickGestureDialogSeekBarValue(QuickGestureManager
                            .getInstance(getApplicationContext()).mSlidAreaSize);
                    // save sliding area value
                    mPre.setDialogRadioLeftBottom(mLeftBottom);
                    mPre.setDialogRadioRightBottom(mRightBottm);
                    mPre.setDialogRadioLeftCenter(mLeftCenter);
                    mPre.setDialogRadioRightCenter(mRightCenter);
                    QuickGestureManager.getInstance(QuickGestureActivity.this).resetSlidAreaSize();
                    // update area background color
                    updateFloatWindowBackGroudColor();
                    if (mAlarmDialog != null) {
                        mAlarmDialog.dismiss();
                        // FloatWindowHelper.mEditQuickAreaFlag = false;
                        // mAlarmDialogFlag = false;
                        // updateFloatWindowBackGroudColor();
                    }
                } else {
                    Toast.makeText(
                            QuickGestureActivity.this,
                            QuickGestureActivity.this
                                    .getResources()
                                    .getString(
                                            R.string.pg_appmanager_quick_gesture_option_dialog_radio_toast_text),
                            Toast.LENGTH_SHORT).show();
                }
                unInitSlidingSetting();
            }

        });
        mAlarmDialog.setCancelable(true);
        mAlarmDialog.show();
        mAlarmDialogFlag = true;
        updateFloatWindowBackGroudColor();
    }

    private void unInitSlidingSetting() {
        leftCenterTemp = false;
        RightCenterTemp = false;
        leftBottomTemp = false;
        rightBottomTemp = false;
    }

    // update backgroud color
    private void updateFloatWindowBackGroudColor() {
        FloatWindowHelper
                .updateFloatWindowBackgroudColor(this, FloatWindowHelper.mEditQuickAreaFlag);
        // FloatWindowHelper.createFloatWindow(QuickGestureActivity.this,
        // AppMasterPreference
        // .getInstance(getApplicationContext()).getQuickGestureDialogSeekBarValue());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int viewId = view.getId();
        int width = view.getWidth();
        float downX = 0;
        float downY = 0;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = Math.abs(event.getX() - downX);
                float moveY = Math.abs(event.getY() - downY);
                if (moveX > width / 50 || moveY > width / 50) {
                    if (!mFlag) {
                        AppMasterPreference.getInstance(this).setRootViewAndWindowHeighSpace(
                                screenSpace());
                        QuickGestureManager.getInstance(QuickGestureActivity.this).screenSpace = screenSpace();
                        mTipRL.clearAnimation();
                        mHandImage.setVisibility(View.GONE);
                        mArrowImage.setVisibility(View.GONE);
                        // init the first time white float position
                        QuickGestureManager.getInstance(AppMasterApplication.getInstance()).onTuchGestureFlag = -2;
                        SDKWrapper.addEvent(QuickGestureActivity.this, SDKWrapper.P1,
                                "qssetting", "qs_open");
                        AppMasterPreference.getInstance(this).setFristSlidingTip(true);
                        Intent intent;
                        intent = new Intent(AppMasterApplication.getInstance(),
                                QuickGesturePopupActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        switch (viewId) {
                            case R.id.gesture_left_tips_top_tv:
                                intent.putExtra("show_orientation", 0);
                                break;
                            case R.id.gesture_left_tips_bottom:
                                intent.putExtra("show_orientation", 0);
                                break;
                            case R.id.gesture_right_tips_top_tv:
                                intent.putExtra("show_orientation", 2);
                                break;
                            case R.id.gesture_right_tips_bottom:
                                intent.putExtra("show_orientation", 2);
                                break;
                        }
                        try {
                            AppMasterApplication.getInstance().startActivity(intent);
                        } catch (Exception e) {
                        }
                        mPre.setSwitchOpenQuickGesture(true);
                        mOpenQuickFlag = true;
                        QuickGestureManager.getInstance(QuickGestureActivity.this)
                                .startFloatWindow();
                        initQuickSwitch();
                        mTipRL.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mTipRL.setVisibility(View.GONE);
                            }
                        }, 1000);
                        mFlag = true;
                        QuickGestureManager.getInstance(getApplicationContext()).createShortCut();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return true;
    }

    // 计算根布局与屏幕高的差值
    private int screenSpace() {
        @SuppressWarnings("deprecation")
        int height = ((WindowManager) QuickGestureActivity.this
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
        int rootHeight = mActivityRootView.getRootView().getHeight();
        if (mActivityRootView != null) {
            int temp = Math.abs(rootHeight - height);
            return temp;
        } else {
            return 0;
        }
    }

    private void quickTipAnim(View view) {
        AlphaAnimation alpha = new AlphaAnimation(0, 1);
        alpha.setDuration(1000);
        AnimationSet animation = new AnimationSet(true);
        animation.addAnimation(alpha);
        view.setAnimation(animation);
        animation.start();
    }

    private AnimatorSet gestureTranslationAnim(View view1, View view2) {
        view1.clearAnimation();
        view2.clearAnimation();
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator alphaArrow = ObjectAnimator.ofFloat(view2, "alpha", 0, 0, 1);
        alphaArrow.setDuration(2000);
        alphaArrow.setRepeatCount(-1);
        float translation = DipPixelUtil.dip2px(this, 100);
        PropertyValuesHolder arrowHolderX = PropertyValuesHolder
                .ofFloat("translationX", 0, 0, -translation);
        PropertyValuesHolder arrowHolderY = PropertyValuesHolder
                .ofFloat("translationY", 0, 0, -translation);
        ObjectAnimator translateArrow = (ObjectAnimator) ObjectAnimator.ofPropertyValuesHolder(
                view2, arrowHolderX, arrowHolderY);
        translateArrow.setDuration(2000);
        translateArrow.setRepeatCount(-1);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view1, "alpha", 0, 1, 1);
        alpha.setDuration(2000);
        alpha.setRepeatCount(-1);
        PropertyValuesHolder valuesHolderX = PropertyValuesHolder
                .ofFloat("translationX", 0, 270, 0);
        PropertyValuesHolder valuesHolderY = PropertyValuesHolder
                .ofFloat("translationY", 0, 300, 0);
        ObjectAnimator translate = (ObjectAnimator) ObjectAnimator.ofPropertyValuesHolder(view1,
                valuesHolderX, valuesHolderY);
        translate.setRepeatCount(-1);
        translate.setInterpolator(new AccelerateDecelerateInterpolator());
        translate.setDuration(2000);
        animatorSet.playTogether(translate, alpha, alphaArrow, translateArrow);
        animatorSet.start();
        return animatorSet;
    }

    @Override
    public void onClick(View arg0) {
        int flag = arg0.getId();
        switch (flag) {
            case R.id.tv_option_image:
                Log.i("null", "enter setting");
                Intent intent = new Intent(QuickGestureActivity.this,
                        QuickGestureSettingActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.gesture_switch_text:
                gestureSwitch();
                break;
            case R.id.slide_guide_show:
                onSlideGuideClick();
                break;
            case R.id.edit_guide_show:
                onEditVideoClick();
                break;
            case R.id.slide_setting_button:
                updateSlideView();
                break;
            default:
                break;
        }

    }


    private void gestureSwitch() {
        if (mOpenQuickFlag) {
            SDKWrapper.addEvent(QuickGestureActivity.this, SDKWrapper.P1, "qssetting",
                    "qs_close");
            if (!isRoating) {
                closeQuickGestureAnimation();
            }
            mPre.setSwitchOpenQuickGesture(false);
            mOpenQuickFlag = false;
            unSetOnClickListener();
            QuickGestureManager.getInstance(this).stopFloatWindow();
            QuickGestureManager.getInstance(QuickGestureActivity.this).screenSpace = 0;
            FloatWindowHelper.removeAllFloatWindow(QuickGestureActivity.this);
            if (AppMasterPreference.getInstance(QuickGestureActivity.this)
                    .getSwitchOpenStrengthenMode()) {
                FloatWindowHelper.removeWhiteFloatView(QuickGestureActivity.this);
                mPre.setWhiteFloatViewCoordinate(0, 0);
            }
        } else {
            SDKWrapper.addEvent(QuickGestureActivity.this, SDKWrapper.P1, "qssetting",
                    "qs_open");
            mPre.setSwitchOpenQuickGesture(true);
            mOpenQuickFlag = true;
            if (!isRoating) {
                openQuickGestureAnimation();
            }
            QuickGestureManager.getInstance(QuickGestureActivity.this).startFloatWindow();
            setOnClickListener();
            QuickGestureManager.getInstance(QuickGestureActivity.this).screenSpace = screenSpace();
            if (AppMasterPreference.getInstance(QuickGestureActivity.this)
                    .getSwitchOpenStrengthenMode()) {
                FloatWindowHelper.createWhiteFloatView(getApplicationContext());
            }
        }

    }

    private void updateSlideView() {
        SDKWrapper.addEvent(QuickGestureActivity.this, SDKWrapper.P1, "qssetting",
                "area_cli");
        FloatWindowHelper.mEditQuickAreaFlag = true;
        showSettingDialog(true);
        AppMasterPreference.getInstance(QuickGestureActivity.this)
                .setRootViewAndWindowHeighSpace(screenSpace());
        QuickGestureManager.getInstance(QuickGestureActivity.this).screenSpace = screenSpace();
        mActivityRootView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        AppMasterPreference.getInstance(QuickGestureActivity.this)
                                .setRootViewAndWindowHeighSpace(screenSpace());
                        QuickGestureManager.getInstance(QuickGestureActivity.this).screenSpace = screenSpace();
                        int value = QuickGestureManager
                                .getInstance(getApplicationContext()).mSlidAreaSize;
                        FloatWindowHelper.updateView(QuickGestureActivity.this, value);
                    }
                });
    }

    private void closeQuickGestureAnimation() {
        ValueAnimator mRotateClose = ValueAnimator.ofFloat(0, 90).setDuration(200);
        mRotateClose.setInterpolator(new LinearInterpolator());
        mRotateClose.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                float percent = value / 90;
                Log.i("tag", "value = " + value + "   percent = " + percent);
                mRotationImage.setRotation(value);
                mRotationOpenBgImage.setAlpha(1 - percent);
                mRotationCloseBgImage.setAlpha(percent);
            }
        });
        mRotateClose.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                isRoating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mRotationImage.setRotation(0);
                mGestureSwitchView.setBackgroundResource(R.drawable.gesture_close_bg);
                mRotationImage.setImageResource(R.drawable.gesture_rotation_close);
                mGestureSwitch.setBackgroundResource(R.drawable.gesture_close_selecter);
                mGestureSwitch.setText(R.string.quick_gesture_close_text);
                mGestureSwitch
                        .setTextColor(getResources().getColor(R.color.quick_close_text_color));
                unSetOnClickListener();
                isRoating = false;
            }
        });
        mRotateClose.start();
    }

    private void openQuickGestureAnimation() {
        ValueAnimator mRotateOpen = ValueAnimator.ofFloat(0, 90).setDuration(200);
        mRotateOpen.setInterpolator(new LinearInterpolator());
        mRotateOpen.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                float percent = value / 90;
                Log.i("tag", "value = " + value + "   percent = " + percent);
                mRotationImage.setRotation(-value);
                mRotationCloseBgImage.setAlpha(1 - percent);
                mRotationOpenBgImage.setAlpha(percent);
            }
        });
        mRotateOpen.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                isRoating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mRotationImage.setRotation(0);
                mGestureSwitchView.setBackgroundResource(R.drawable.gesture_open_bg);
                mRotationImage.setImageResource(R.drawable.gesture_rotation_open);
                mGestureSwitch.setBackgroundResource(R.drawable.gesture_open_selecter);
                mGestureSwitch.setText(R.string.quick_gesture_open_text);
                mGestureSwitch.setTextColor(getResources().getColor(R.color.quick_open_text_color));
                setOnClickListener();
                isRoating = false;
            }
        });
        mRotateOpen.start();
    }

    /**
     * play or stop the slide guide animation
     */
    private void onSlideGuideClick() {
        if (null == mSlideGuideAnim) {
            sildeGuideTransAnim();
        }
        if (!isTranslating) {
            mSlideStopImage.setVisibility(View.INVISIBLE);
            mSlideGuideAnim.start();
        } else {
            mSlideStopImage.setVisibility(View.VISIBLE);
            List<Animator> throbbers = mSlideGuideAnim.getChildAnimations();
            for (Animator animator : throbbers) {
                ((ObjectAnimator) animator).setRepeatCount(0);
                ((ObjectAnimator) animator).setRepeatMode(0);
            }
            mSlideGuideAnim.cancel();
            mSlideGuideAnim = null;
        }
    }

    private void sildeGuideTransAnim() {
        mSlideGuidehand.clearAnimation();
        mSlideGuideArrow.clearAnimation();
        mSlideGuideAnim = new AnimatorSet();

        ObjectAnimator handAlpha = ObjectAnimator.ofFloat(mSlideGuidehand, "alpha", 0, 1);
        handAlpha.setRepeatCount(-1);
        float translation = DipPixelUtil.dip2px(this, 60);
        PropertyValuesHolder handHolderX = PropertyValuesHolder
                .ofFloat("translationX", 0, -translation, 0);
        PropertyValuesHolder handHolderY = PropertyValuesHolder
                .ofFloat("translationY", 0, translation, 0);
        ObjectAnimator handTranslate = (ObjectAnimator) ObjectAnimator.ofPropertyValuesHolder(
                mSlideGuidehand, handHolderX, handHolderY);
        handTranslate.setRepeatCount(-1);

        ObjectAnimator arrowAlpha = ObjectAnimator.ofFloat(mSlideGuideArrow, "alpha", 0, 0, 1);
        arrowAlpha.setRepeatCount(-1);
        PropertyValuesHolder arrowHolderX = PropertyValuesHolder
                .ofFloat("translationX", 0, 0, 120);
        PropertyValuesHolder arrowHolderY = PropertyValuesHolder
                .ofFloat("translationY", 0, 0, -120);
        ObjectAnimator arrawTranslate = (ObjectAnimator) ObjectAnimator.ofPropertyValuesHolder(
                mSlideGuideArrow,
                arrowHolderX, arrowHolderY);
        arrawTranslate.setRepeatCount(-1);
        arrawTranslate.setInterpolator(new AccelerateDecelerateInterpolator());

        mSlideGuideAnim.setDuration(1500);
        mSlideGuideAnim.playTogether(handAlpha, handTranslate, arrowAlpha, arrawTranslate);

        mSlideGuideAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                isTranslating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isTranslating = false;
                mSlideGuidehand.setTranslationX(0);
                mSlideGuidehand.setTranslationY(0);
                mSlideGuidehand.setAlpha(1.0f);
                mSlideGuideArrow.setTranslationX(0);
                mSlideGuideArrow.setTranslationY(0);
                mSlideGuideArrow.setAlpha(1.0f);
            }
        });
    }

    private void onEditVideoClick() {
        mEditVideoView.setVisibility(View.VISIBLE);
        if (mEditVideoView.isPlaying()) {
            mEditVideoView.stopPlayback();
            mEditStopImage.setVisibility(View.VISIBLE);
            mEditVideoBgImage.setVisibility(View.VISIBLE);
        } else {
            mEditStopImage.setVisibility(View.INVISIBLE);
            mEditVideoBgImage.setVisibility(View.INVISIBLE);
            Uri uri = Uri.parse("android.resource://com.leo.appmaster/" + R.raw.eidt_gesture);
            mEditVideoView.setVideoURI(uri);
            mEditVideoView.start();
            mEditVideoView.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mEditVideoView.start();
                }
            });
        }
    }
}
