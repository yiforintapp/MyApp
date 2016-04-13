package com.leo.appmaster.airsig.airsigsdk;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.airsig.airsigengmulti.ASEngine;
import com.airsig.airsigengmulti.ASEngine.ASAction;
import com.airsig.airsigengmulti.ASEngine.ASAction.InvalidActionIndexException;
import com.airsig.airsigengmulti.ASEngine.ASError;
import com.airsig.airsigengmulti.ASEngine.ASStrength;
import com.airsig.airsigengmulti.ASEngine.OnGetActionResultListener;
import com.airsig.airsigengmulti.ASEngine.OnResetSignatureResultListener;
import com.leo.appmaster.R;
import com.leo.appmaster.airsig.AirSigActivity;
import com.leo.appmaster.airsig.airsigui.AnimatedGifImageView;
import com.leo.appmaster.airsig.airsigutils.EventLogger;
import com.leo.appmaster.airsig.airsigutils.Utils;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.feedback.FeedbackActivity;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.DeviceUtil;
import com.leo.appmaster.utils.LeoLog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class TrainingActivity extends BaseActivity implements OnClickListener {

    public static final String SETTING_FAILE = "isUpdateYet";
    private int mTryTimes = 0;
    private LEOAlarmDialog mConfirmCloseDialog;
    public static final int SHOWDIALOG = 1;
    public static final int BACK_DIALOG = 1;
    public static final int RESET_DIALOG = 2;
    private CommonToolbar mTitleBar;
    private View mReset;
    private View mTeach;

    private enum Step {
        TrainingCompleted(null),
        WriteAgain(TrainingCompleted),
        FirstTimeWriting(WriteAgain),
        HowToSetup(FirstTimeWriting),
        ChooseWord(HowToSetup);

        final Step next;

        Step(Step next) {
            this.next = next;
        }
    }

    // Keys for configure the activity
    public static final String kActionIndex = "The index of the action to be trained";
    public static final String kResultEventName = "The event name for receive training result from local broadcast";

    public static final String DEFAULT_RESULT_EVENT_NAME = "AirSig Training Result";

    // Keys for receive data from this activity
    public static final String kTrainingResult = "The training result";
    public static final String kIsRetrain = "isRetrain";

    // private data
    private ASAction mTrainingAction;
    private boolean mIsRetrain = false;
    private String mResultEventName = DEFAULT_RESULT_EVENT_NAME;
    private int mSignatureInputCount = 0;
    private int mProgress = 0;
    private Step mCurrentStep = null;

    // UI components for setting signature
    private RelativeLayout mTouchBox;
    private View mTouchBoxZone;
    private RelativeLayout mTouchareaBackground;
    private View mToucharea;
    private ImageView mImageThumb;

    // UI components for messages
    private RelativeLayout mMessageBox;
    private TextView mTouchareaMessage;
    private MessageBoxFragment mMessageBoxFragment;
    private View mProgressBox;
    private TextView mProgressText, mProgressMessage;

    // UI components for page containers
    private View mMainPage;
    private View mHelpPage;
    private Button mHelpPageButton;
    private boolean isFirstIn = false;

    // other UI components
    private ProgressBar mProgressBar;
    private ToneGenerator mToneGenerator = null;
    private ToneGenerator mToneGenerator2 = null;

    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOWDIALOG:
                    SDKWrapper.addEvent(TrainingActivity.this, SDKWrapper.P1, "airsig_set", "set_fail");

                    boolean isUpdateYet = LeoSettings.getBoolean(SETTING_FAILE, false);
                    if (!isUpdateYet) {
                        //update devices
                        String strings = DeviceUtil.getAirSigDeives(TrainingActivity.this);
                        SDKWrapper.addEvent(TrainingActivity.this, SDKWrapper.P1, "airsig_sdk", "set_fail_" + strings);

                        LeoSettings.setBoolean(SETTING_FAILE, true);
                    }


                    showFailDialog();
                    break;
            }
        }
    };


    @SuppressLint({"InflateParams", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.airsig_activity_training);

        isFirstIn = true;
        // customize action bar:
        // 1. no App icon
        // 2. back button
//        if (null != getActionBar()) {
//            getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
//            getActionBar().setDisplayHomeAsUpEnabled(true);
//            getActionBar().setDisplayShowTitleEnabled(false);
//            getActionBar().setDisplayShowCustomEnabled(true);
//            TextView title = new TextView(getApplicationContext());
//            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            title.setLayoutParams(lp);
//            title.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
//            title.setEllipsize(TruncateAt.END);
//            title.setTextColor(getResources().getColor(R.color.airsig_actionbar_title));
//            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.airsig_actionbar_title_textsize));
//            title.setText(getResources().getString(R.string.airsig_training_title));
//            getActionBar().setCustomView(title);
//        }

        mTitleBar = (CommonToolbar) findViewById(R.id.airsig_title_bar);
        mTitleBar.setToolbarTitle(R.string.airsig_settings_activity_title);
        mTitleBar.setOptionMenuVisible(false);
        mTitleBar.setNavigationClickListener(this);

        mReset = findViewById(R.id.airsig_reset_click);
        mReset.setOnClickListener(this);
        mTeach = findViewById(R.id.airsig_teach_click);
        mTeach.setOnClickListener(this);

        // initialize UI components:
        mTouchBox = (RelativeLayout) findViewById(R.id.viewTouchBox);
        mTouchBoxZone = findViewById(R.id.viewTouchBoxZone);
        mTouchBoxZone.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mTouchareaBackground = (RelativeLayout) findViewById(R.id.viewTouchAreaBackground);
        mToucharea = findViewById(R.id.viewTouchArea);
        mToucharea.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return onTouchPressArea(v, event);
            }
        });
        mTouchareaBackground.post(new Runnable() {
            @Override
            public void run() {
                Rect rect = new Rect();
                mToucharea.getHitRect(rect);
                int extraPadding = (int) Utils.dp2px(getResources(), 50);
                rect.top -= extraPadding;
                rect.left -= extraPadding;
                rect.right += extraPadding;
                rect.bottom += extraPadding;
                mTouchareaBackground.setTouchDelegate(new TouchDelegate(rect, mToucharea));
            }
        });
        mImageThumb = (ImageView) findViewById(R.id.imageThumb);


        // UI components for messages
        mMessageBox = (RelativeLayout) findViewById(R.id.viewMessageBox);
        mTouchareaMessage = (TextView) findViewById(R.id.textTouchAreaMessage);

        // UI components for page containers
        mMainPage = findViewById(R.id.contentContainer);
        mHelpPage = findViewById(R.id.helpPage);
        mHelpPageButton = (Button) findViewById(R.id.helpPageButton);
        mHelpPageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showAirSigTutorial();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        mMainPage.setVisibility(View.VISIBLE);
                        mHelpPage.setVisibility(View.INVISIBLE);
                    }
                }, 500);
            }
        });

        // other components
        mProgressBar = (ProgressBar) findViewById(R.id.progressBarWaiting);

        // initialize private data
        Bundle bundle = this.getIntent().getExtras();
        final int actionIndex = bundle.getInt(kActionIndex);
        if (bundle.containsKey(kResultEventName)) {
            mResultEventName = bundle.getString(kResultEventName);
        }

        try {
            mTrainingAction = new ASAction(actionIndex);
        } catch (InvalidActionIndexException e) {
            e.printStackTrace();
            showDialog(getResources().getString(R.string.airsig_training_err_invalid_action_index), null, null,
                    getResources().getString(android.R.string.ok),
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finishActivity(false);
                        }
                    },
                    null, null
            );
        }


        // get action data
        showWaiting(true);
        ASEngine.getSharedInstance().getAction(actionIndex, new OnGetActionResultListener() {
            @Override
            public void onResult(ASAction action, ASError error) {
                if (null != action) {
                    mTrainingAction = action;
                    if (!isTrainingNotCompleted()) {
                        mIsRetrain = true;
                    }
                }

                // virtual reset action
                ASEngine.getSharedInstance().virtualResetSignature(actionIndex, new OnResetSignatureResultListener() {
                    @Override
                    public void onResult(ASAction action, ASError error) {
                        showWaiting(false);

                        if (null != action) {
                            mTrainingAction = action;
                        }

                        // display initial view
//                        runOnUiThread(new Runnable() {
//                            public void run() {
//                                new Handler().postDelayed(new Runnable() {
//                                    public void run() {
//                                        LeoLog.d("testAirsig", "gotoStep ChooseWord");
//                                        gotoStep(Step.ChooseWord);
//                                    }
//                                }, 300);
//                            }
//                        });
                        runOnUiThread(new Runnable() {
                            public void run() {
                                new Handler().post(new Runnable() {
                                    public void run() {
                                        LeoLog.d("testAirsig", "gotoStep ChooseWord");
                                        gotoStep(Step.ChooseWord);
                                    }
                                });
                            }
                        });

                        // Log event
                        JSONObject eventProperties = new JSONObject();
                        try {
                            eventProperties.put(EventLogger.EVENT_PROP_TRAINING_SUCCESS_TIMES, EventLogger.getLocalIntProperty(EventLogger.EVENT_PROP_TRAINING_SUCCESS_TIMES, 0));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        EventLogger.startAction(EventLogger.EVENT_NAME_TRAINING, eventProperties, true);
                    }
                });
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != ASEngine.getSharedInstance()) {
            ASEngine.getSharedInstance().startSensors();
        }
    }

    @Override
    protected void onPause() {
        if (null != ASEngine.getSharedInstance()) {
            ASEngine.getSharedInstance().stopSensors();
        }
        if (null != mToneGenerator) {
            mToneGenerator.release();
            mToneGenerator = null;
        }
        if (null != mToneGenerator2) {
            mToneGenerator2.release();
            mToneGenerator2 = null;
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        // Log event
        EventLogger.logEvent(EventLogger.EVENT_NAME_TRAINING_CLICK_LEAVE, null);

        if (isTrainingNotCompleted()) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "airsig_sdk", "leave");
            showTipsDialog(getResources().getString(R.string.airsig_training_dialog_confirm_exit_detail),
                    getResources().getString(R.string.airsig_training_dialog_confirm_exit_postive_button),
                    getResources().getString(R.string.airsig_training_dialog_confirm_exit_negative_button),
                    BACK_DIALOG);
//            showDialog(getResources().getString(R.string.airsig_training_dialog_confirm_exit_title),
//                    getResources().getString(R.string.airsig_training_dialog_confirm_exit_detail),
//                    R.drawable.airsig_ic_dialog,
//                    getResources().getString(R.string.airsig_training_dialog_confirm_exit_postive_button),
//                    new OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            finishActivity(false);
//                        }
//                    },
//                    getResources().getString(R.string.airsig_training_dialog_confirm_exit_negative_button), null
//            );
        } else {
            finishActivity(true);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.airsig_actionbar_training_mode, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(final MenuItem item) {
//        super.onOptionsItemSelected(item);
//
//        if (R.id.reset == item.getItemId()) {
//            if (mHelpPage.getVisibility() == View.VISIBLE) {
//                return true;
//            }
//
//            showDialog(getResources().getString(R.string.airsig_training_dialog_confirm_reset_title),
//                    getResources().getString(R.string.airsig_training_dialog_confirm_reset_detail),
//                    R.drawable.airsig_ic_dialog,
//                    getResources().getString(R.string.airsig_training_dialog_confirm_reset_postive_button),
//                    new OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            // Log event
//                            EventLogger.logEvent(EventLogger.EVENT_NAME_TRAINING_RESET, null);
//
//                            reset();
//                        }
//                    },
//                    getResources().getString(R.string.airsig_training_dialog_confirm_reset_negative_button), null
//            );
//        } else if (android.R.id.home == item.getItemId()) {
//            // Log event
//            EventLogger.logEvent(EventLogger.EVENT_NAME_TRAINING_CLICK_LEAVE, null);
//
//            if (isTrainingNotCompleted()) {
//                showDialog(getResources().getString(R.string.airsig_training_dialog_confirm_exit_title),
//                        getResources().getString(R.string.airsig_training_dialog_confirm_exit_detail),
//                        R.drawable.airsig_ic_dialog,
//                        getResources().getString(R.string.airsig_training_dialog_confirm_exit_postive_button),
//                        new OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                finishActivity(false);
//                            }
//                        },
//                        getResources().getString(R.string.airsig_training_dialog_confirm_exit_negative_button), null
//                );
//            } else {
//                finishActivity(true);
//            }
//        } else if (R.id.help == item.getItemId()) {
//            if (mHelpPage.getVisibility() != View.VISIBLE) {
//                // Log event
//                EventLogger.logEvent(EventLogger.EVENT_NAME_TRAINING_CLICK_TUTORIAL, null);
//            }
//
//            if (mHelpPage.getVisibility() == View.VISIBLE) {
//                mMainPage.setVisibility(View.VISIBLE);
//
//                ScaleAnimation sa = new ScaleAnimation(1f, 0f, 1f, 0f, mHelpPage.getWidth() / 2, mHelpPage.getHeight() / 2);
//                sa.setDuration(300);
//                AlphaAnimation aa = new AlphaAnimation(1f, 0f);
//                aa.setDuration(300);
//                AnimationSet as = new AnimationSet(true);
//                as.setInterpolator(new AccelerateInterpolator());
//                as.addAnimation(sa);
//                as.addAnimation(aa);
//
//                item.setEnabled(false);
//                mHelpPage.startAnimation(sa);
//                new Handler().postDelayed(new Runnable() {
//                    public void run() {
//                        mHelpPage.setVisibility(View.INVISIBLE);
//                        item.setEnabled(true);
//                    }
//                }, sa.getDuration());
//            } else {
//                mMainPage.setVisibility(View.INVISIBLE);
//
//                mHelpPage.setVisibility(View.VISIBLE);
//                ScaleAnimation sa = new ScaleAnimation(0f, 1f, 0f, 1f, mHelpPage.getWidth() / 2, mHelpPage.getHeight() / 2);
//                sa.setDuration(300);
//                AlphaAnimation aa = new AlphaAnimation(0f, 1f);
//                aa.setDuration(300);
//                AnimationSet as = new AnimationSet(true);
//                as.setInterpolator(new AccelerateDecelerateInterpolator());
//                as.addAnimation(sa);
//                as.addAnimation(aa);
//
//                item.setEnabled(false);
//                mHelpPage.startAnimation(as);
//                new Handler().postDelayed(new Runnable() {
//                    public void run() {
//                        item.setEnabled(true);
//                    }
//                }, sa.getDuration());
//            }
//        }
//
//        return true;
//    }

    public static boolean isEnterTutorPage = false;

    private void showAirSigTutorial() {
        isEnterTutorPage = true;
        SDKWrapper.addEvent(this, SDKWrapper.P1, "airsig_set", "airsig_learn");
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), TutorialActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        intent.putExtras(bundle);

        startActivity(intent);
    }

    private void gotoStep(final Step step) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCurrentStep = step;
                switch (step) {
                    case ChooseWord: {

                        showTextMessage(getResources().getString(R.string.airsig_training_step_choose_word_title),
                                getResources().getString(R.string.airsig_training_step_choose_word_detail),
                                getResources().getString(R.string.airsig_training_step_choose_word_next),
                                new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        gotoStep(step.next);
                                    }
                                },
                                null, null
                        );


                        final String key = "first time show red dot";
                        if (!Utils.getSavedBoolean(getApplicationContext(), key, false)) {
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    if (mCurrentStep == Step.ChooseWord && mMessageBoxFragment != null) {
                                        mMessageBoxFragment.showHintIcon(true);
                                        Utils.saveBoolean(getApplicationContext(), key, true);
                                    }
                                }
                            }, 5000);
                        }

                        break;
                    }
                    case HowToSetup: {
                        showImageMessage(getResources().getString(R.string.airsig_training_step_how_to_setup_detail),
                                R.raw.airsig_ani_tutorial_total,
                                getResources().getString(R.string.airsig_training_step_how_to_setup_next),
                                new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        gotoStep(step.next);
                                    }
                                },
                                null, null
                        );
                        break;
                    }
                    case FirstTimeWriting: {
                        final View contentView = getLayoutInflater().inflate(R.layout.airsig_fragment_training_htw_small, null);
                        final AnimatedGifImageView gifView = (AnimatedGifImageView) contentView.findViewById(R.id.gifImageView);
                        gifView.setAnimatedGif(R.raw.airsig_ani_tutorial_total, AnimatedGifImageView.TYPE.FIT_CENTER);
                        collapseMessageBox(contentView);
                        break;
                    }
                    case WriteAgain: {
                        TextView textView = new TextView(getApplicationContext());
                        textView.setText(R.string.airsig_training_step_write_again);
                        textView.setGravity(Gravity.CENTER);
                        textView.setBackgroundColor(Color.TRANSPARENT);
                        textView.setTextColor(getResources().getColor(R.color.airsig_text_blue));
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.airsig_textsize_title));
                        textView.setLineSpacing(getResources().getDimensionPixelSize(R.dimen.airsig_text_lineSpacingExtra), 1);
                        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        textView.setLayoutParams(lp);
                        collapseMessageBox(textView);
                        break;
                    }
                    case TrainingCompleted: {
                        // Log event
                        JSONObject eventProperties = new JSONObject();
                        try {
                            eventProperties.put(EventLogger.EVENT_PROP_TRAINING_SIGN_COUNT, mSignatureInputCount);
                            eventProperties.put(EventLogger.EVENT_PROP_TRAINING_RESULT_STRENGTH, mTrainingAction.strength.name());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        EventLogger.logEvent(EventLogger.EVENT_NAME_TRAINING_COMPLETE, eventProperties);

                        SpannableString span100 = new SpannableString("100");
                        SpannableString spanPercent = new SpannableString("%");
                        SpannableString spanMsg = new SpannableString(getResources().getString(R.string.airsig_training_step_completed));

                        span100.setSpan(new AbsoluteSizeSpan((int) Utils.sp2px(getResources(), 72)), 0, span100.length(), 0);
                        spanPercent.setSpan(new AbsoluteSizeSpan((int) Utils.sp2px(getResources(), 24)), 0, spanPercent.length(), 0);
                        spanMsg.setSpan(new AbsoluteSizeSpan((int) getResources().getDimensionPixelSize(R.dimen.airsig_textsize_title)), 0, spanMsg.length(), 0);

                        TextView textView = new TextView(getApplicationContext());
                        textView.setText(TextUtils.concat(span100, spanPercent, "\n", spanMsg));
                        textView.setGravity(Gravity.CENTER);
                        textView.setBackgroundColor(Color.TRANSPARENT);
                        textView.setTextColor(getResources().getColor(R.color.airsig_text_white));
                        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                        expandMessageBox(textView,
                                getResources().getString(R.string.airsig_training_step_completed_postive_button),
                                new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        finishActivity(true);
                                    }
                                },
                                getResources().getString(R.string.airsig_training_step_completed_negative_button),
                                new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        reset();
                                    }
                                }
                        );
                        break;
                    }
                    default:
                        break;
                }
            }
        });
    }

    private void reset() {
        showWaiting(true);
        ASEngine.getSharedInstance().virtualResetSignature(mTrainingAction.actionIndex, new OnResetSignatureResultListener() {
            @Override
            public void onResult(ASAction action, ASError error) {
                showWaiting(false);
                if (null != action) {
                    mTrainingAction = action;
                    mSignatureInputCount = 0;
                    mProgress = 0;
                }
                gotoStep(Step.ChooseWord);
            }
        });
    }

    private boolean onTouchPressArea(final View v, final MotionEvent event) {
        if (v != mToucharea) {
            return false;
        }

        if (!isWaitingForInputSignature()) {
            return false;
        }

        final int[] vpos = new int[2];
        v.getLocationOnScreen(vpos);
        final float thumbPositionX = event.getX() + vpos[0];
        final float thumbPositionY = event.getY() + vpos[1];
        updateFingerPositionOnScreen(thumbPositionX, thumbPositionY);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pressTouchArea(true);
                ASEngine.getSharedInstance().startRecordingSensor(null);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                showWaiting(true);
                ASEngine.getSharedInstance().completeRecordSensorToTrainAction(mTrainingAction.actionIndex, new ASEngine.OnAddSignaturesResultListener() {
                    @SuppressLint("InflateParams")
                    @Override
                    public void onResult(ASAction action, ASError error, float progress, ASEngine.ASSignatureSecurityLevel securityLevel) {
                        showWaiting(false);
                        pressTouchArea(false);
                        if (null != error) {
                            // Log event
                            JSONObject eventProperties = new JSONObject();
                            try {
                                eventProperties.put(EventLogger.EVENT_PROP_ERROR_CODE, error.code);
                                eventProperties.put(EventLogger.EVENT_PROP_ERROR_MESSAGE, error.message);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            EventLogger.logEvent(EventLogger.EVENT_NAME_TRAINING_ERROR, eventProperties);

                            switch (error) {
                                case TRAINING_INVALID_LONG_SIGNATURE:
                                    LeoLog.d("testAirSig", "error 1");

                                    if (mTryTimes >= 2) {
                                        //failed time over 3 times
//                                        showFailDialog();
                                        mHandler.sendEmptyMessage(SHOWDIALOG);
                                    } else {
                                        mTryTimes++;
                                    }

                                    showTextMessage(getResources().getString(R.string.airsig_training_err_invalid_long_signature_title),
                                            getResources().getString(R.string.airsig_training_err_invalid_long_signature_detail), null,
                                            new OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    gotoStep(mCurrentStep);
                                                }
                                            },
                                            null, null
                                    );
                                    break;
                                case TRAINING_INVALID_SHORT_SIGNATURE:
                                    LeoLog.d("testAirSig", "error 2");
                                    gotoStep(mCurrentStep);
                                    return;
                                case TRAINING_INVALID_HOLDING_POSTURE:
                                    LeoLog.d("testAirSig", "error 3");
                                    if (mTryTimes >= 2) {
                                        //failed time over 3 times
//                                        showFailDialog();
                                        mHandler.sendEmptyMessage(SHOWDIALOG);
                                    } else {
                                        mTryTimes++;
                                    }
                                    showImageMessage(getResources().getString(R.string.airsig_training_err_invalid_holding_posture), R.raw.airsig_ani_tutorial_holding_posture, null,
                                            new OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    gotoStep(mCurrentStep);
                                                }
                                            },
                                            null, null
                                    );
                                    break;
                                case TRAINING_INVALID_FEW_WRIST:
                                    LeoLog.d("testAirSig", "error 4");
                                    if (mTryTimes >= 2) {
                                        //failed time over 3 times
//                                        showFailDialog();
                                        mHandler.sendEmptyMessage(SHOWDIALOG);
                                    } else {
                                        mTryTimes++;
                                    }
                                    SpannableString fw1 = new SpannableString(getResources().getString(R.string.airsig_training_err_few_wrist1));
                                    String fw2 = getResources().getString(R.string.airsig_training_err_few_wrist2);
                                    fw1.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.airsig_text_gray)), 0, fw1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    showImageMessage(TextUtils.concat(fw1, "\n", fw2), R.raw.airsig_ani_tutorial_holding_posture, null,
                                            new OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    gotoStep(mCurrentStep);
                                                }
                                            },
                                            null, null
                                    );
                                    break;
                                case TRAINING_INVALID_FEW_WORDS:
                                    LeoLog.d("testAirSig", "error 5");
                                    if (mTryTimes >= 2) {
                                        //failed time over 3 times
//                                        showFailDialog();
                                        mHandler.sendEmptyMessage(SHOWDIALOG);
                                    } else {
                                        mTryTimes++;
                                    }
                                    final ASEngine.ASSignatureSecurityLevel level = (ASEngine.ASSignatureSecurityLevel) error.userData.get(ASError.KEY_TRAINING_SECURITY_LEVEL);
                                    if (level != null) {
                                        displaySecurityLevel(level.level, false);
                                    }
                                    break;
                                case TRAINING_DIFFERENT_SIGNATURE:
                                    LeoLog.d("testAirSig", "error 6");
                                    if (mTryTimes >= 2) {
                                        //failed time over 3 times
//                                        showFailDialog();
                                        mHandler.sendEmptyMessage(SHOWDIALOG);
                                    } else {
                                        mTryTimes++;
                                    }
                                    showTextMessage(getResources().getString(R.string.airsig_training_err_different_signature_title),
                                            getResources().getString(R.string.airsig_training_err_different_signature_detail),
                                            getResources().getString(R.string.airsig_training_err_different_signature_postive_button),
                                            new OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    displayProgress(mProgress / 100f);
                                                }
                                            },
                                            getResources().getString(R.string.airsig_training_err_different_signature_negative_button),
                                            new OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    showAirSigTutorial();
                                                    new Handler().postDelayed(new Runnable() {
                                                        public void run() {
                                                            displayProgress(mProgress / 100f);
                                                        }
                                                    }, 300);
                                                }
                                            }
                                    );
                                    break;
                                case TRAINING_THE_SECOND_SIGNATURE_IS_TOO_DIFFERNT:
                                    LeoLog.d("testAirSig", "error 7");
                                    if (mTryTimes >= 2) {
                                        //failed time over 3 times
//                                        showFailDialog();
                                        mHandler.sendEmptyMessage(SHOWDIALOG);
                                    } else {
                                        mTryTimes++;
                                    }
                                    showTextMessage(getResources().getString(R.string.airsig_training_err_second_signature_too_different_title),
                                            getResources().getString(R.string.airsig_training_err_second_signature_too_different_detail),
                                            getResources().getString(R.string.airsig_training_err_second_signature_too_different_postive_button),
                                            new OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    reset();
                                                }
                                            },
                                            getResources().getString(R.string.airsig_training_err_second_signature_too_different_negative_button),
                                            new OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    showAirSigTutorial();
                                                    reset();
                                                }
                                            }
                                    );
                                    break;
                                case TRAINING_FAILED:
                                    LeoLog.d("testAirSig", "error 8");
                                    if (mTryTimes >= 2) {
                                        //failed time over 3 times
//                                        showFailDialog();
                                        mHandler.sendEmptyMessage(SHOWDIALOG);
                                    } else {
                                        mTryTimes++;
                                    }
                                    showTextMessage(getResources().getString(R.string.airsig_training_err_training_failed_title),
                                            getResources().getString(R.string.airsig_training_err_training_failed_detail),
                                            getResources().getString(R.string.airsig_training_err_training_failed_postive_button),
                                            new OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    showAirSigTutorial();
                                                    reset();
                                                }
                                            },
                                            getResources().getString(R.string.airsig_training_err_training_failed_negative_button),
                                            new OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    reset();
                                                }
                                            }
                                    );
                                    break;
                                default:
//                                    showToast(getResources().getString(R.string.airsig_training_err_others) + "\n" + error.message);
                                    break;
                            }
                            makeFailedSound();
                        } else if (null != action) {
                            LeoLog.d("testAirSig", "success");
                            makeSuccessSound();
                            mTrainingAction = action;
                            mSignatureInputCount++;

                            // update finger position
                            mTrainingAction.thumbPositionX = thumbPositionX;
                            mTrainingAction.thumbPositionY = thumbPositionY;
                            ArrayList<ASAction> actions = new ArrayList<ASAction>();
                            actions.add(action);
                            ASEngine.getSharedInstance().setActions(actions, null);

                            if (progress == 1) { // if training completed
                                LeoLog.d("testAirSig", "success 1");
                                if (mCurrentStep != null) {
                                    gotoStep(mCurrentStep.next);
                                }
                            } else if (securityLevel != null) { // display security level is enough
                                LeoLog.d("testAirSig", "success 2");
                                displaySecurityLevel(securityLevel.level, true);
                            } else if (mCurrentStep == Step.FirstTimeWriting) { // please repeat
                                LeoLog.d("testAirSig", "success 3");
                                gotoStep(mCurrentStep.next);
                            } else { // display progress message
                                LeoLog.d("testAirSig", "success 4");
                                displayProgress(progress);
                            }
                        }
                    }
                });
                break;
        }
        return true;
    }

    private void showFailDialog() {
        mTryTimes = 0;
        if (mConfirmCloseDialog == null) {
            mConfirmCloseDialog = new LEOAlarmDialog(this);
        }
        mConfirmCloseDialog.setContent(getString(R.string.air_sig_tips_content_two));
        mConfirmCloseDialog.setRightBtnStr(getString(R.string.secur_help_feedback_tip_button));
        mConfirmCloseDialog.setLeftBtnStr(getString(R.string.airsig_training_err_default_button));
        mConfirmCloseDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reset();

                Intent intent = new Intent(TrainingActivity.this,
                        FeedbackActivity.class);
                intent.putExtra("from", "airsig");
                startActivity(intent);

                mConfirmCloseDialog.dismiss();
            }
        });
        mConfirmCloseDialog.setLeftBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reset();
                mConfirmCloseDialog.dismiss();
            }
        });
        if (!isFinishing()) {
            mConfirmCloseDialog.show();
        }
    }

    private void showTipsDialog(String detail, String postiveButton, String negativeButton, final int type) {
        if (mConfirmCloseDialog == null) {
            mConfirmCloseDialog = new LEOAlarmDialog(TrainingActivity.this);
        }
        mConfirmCloseDialog.setContent(detail);
        mConfirmCloseDialog.setRightBtnStr(postiveButton);
        mConfirmCloseDialog.setLeftBtnStr(negativeButton);
        mConfirmCloseDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (type == BACK_DIALOG) {
                    finish();
                } else {
                    reset();
                }
                mConfirmCloseDialog.dismiss();
            }
        });
        if (!isFinishing()) {
            mConfirmCloseDialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConfirmCloseDialog != null && mConfirmCloseDialog.isShowing()) {
            mConfirmCloseDialog.dismiss();
            mConfirmCloseDialog = null;
        }
    }

    @Override
    public void finish() {
//        LockManager mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
//        mLockManager.filterPackage(this.getPackageName(), 1000);
        super.finish();
    }

    private void enableTouchArea(final boolean enable) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTouchBox.setEnabled(enable);
                mTouchareaBackground.setEnabled(enable);
                mToucharea.setEnabled(enable);
                if (enable) {
                    if (mSignatureInputCount > 0) {
                        mImageThumb.setVisibility(View.VISIBLE);
                        mTouchareaMessage.setVisibility(View.GONE);
                    } else {
                        mTouchareaMessage.setVisibility(View.VISIBLE);
                    }
                } else {
                    mImageThumb.setVisibility(View.GONE);
                    mTouchareaMessage.setVisibility(View.GONE);
                }
            }
        });
    }

    private void pressTouchArea(final boolean pressed) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (pressed) {
                    // Touch Area
                    mTouchareaBackground.setBackground(getResources().getDrawable(R.drawable.airsig_training_toucharea_pressed));
                    ViewGroup.MarginLayoutParams mpp = (ViewGroup.MarginLayoutParams) mTouchareaBackground.getLayoutParams();
                    mpp.setMargins((int) Utils.dp2px(getResources(), 9), (int) Utils.dp2px(getResources(), 9), (int) Utils.dp2px(getResources(), 9), (int) Utils.dp2px(getResources(), 11));
                    mTouchareaBackground.setLayoutParams(mpp);
                    mTouchareaMessage.setVisibility(View.GONE);

                    // Message Box
                    mMessageBox.removeAllViews();
                    TextView textView = new TextView(getApplicationContext());
                    textView.setText(R.string.airsig_training_toucharea_pressed);
                    textView.setGravity(Gravity.CENTER);
                    textView.setBackgroundColor(Color.TRANSPARENT);
                    textView.setTextColor(getResources().getColor(R.color.airsig_text_blue));
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.airsig_textsize_title));
                    textView.setLineSpacing(getResources().getDimensionPixelSize(R.dimen.airsig_text_lineSpacingExtra), 1);
                    ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    textView.setLayoutParams(lp);
                    mMessageBox.addView(textView);
                } else {
                    mTouchareaBackground.setBackground(getResources().getDrawable(R.drawable.airsig_training_toucharea));
                    ViewGroup.MarginLayoutParams mp = (ViewGroup.MarginLayoutParams) mTouchareaBackground.getLayoutParams();
                    mp.setMargins((int) Utils.dp2px(getResources(), 3), (int) Utils.dp2px(getResources(), 5), (int) Utils.dp2px(getResources(), 3), (int) Utils.dp2px(getResources(), 0));
                    mTouchareaBackground.setLayoutParams(mp);
                    mTouchareaMessage.setVisibility(mImageThumb.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                }
            }
        });
    }

    private void expandMessageBox(final View contentView,
                                  final String positiveButtonText, final OnClickListener positiveButtonClickListener,
                                  final String negativeButtonText, final OnClickListener negativeButtonClickListener) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFirstIn) {
                    isFirstIn = false;
                } else {
                    mTouchBox.setVisibility(View.INVISIBLE);
                }

                enableTouchArea(false);

                // Set content of message box
                if (mMessageBoxFragment == null) {
                    mMessageBox.removeAllViews();
                }
                final MessageBoxFragment newFragment = new MessageBoxFragment();
                mMessageBoxFragment = newFragment;
                if (positiveButtonText != null && positiveButtonText.length() > 0) {
                    newFragment.setPositiveButton(positiveButtonText, positiveButtonClickListener);
                }
                if (negativeButtonText != null && negativeButtonText.length() > 0) {
                    newFragment.setNegativeButton(negativeButtonText, negativeButtonClickListener);
                }
                if (contentView != null) {
                    newFragment.setContentView(contentView);
                }
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.viewMessageBox, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();

                // Animation
                int duration = getResources().getInteger(R.integer.airsig_message_box_animation_duration);

                mMessageBox.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
                if (mMessageBox.getMeasuredHeight() <= getResources().getDimensionPixelSize(R.dimen.airsig_small_message_box_height)) {
                    // Animation for touch area
                    AlphaAnimation aa = new AlphaAnimation(1f, 0f);
                    aa.setFillAfter(true);
                    aa.setInterpolator(new FastOutSlowInInterpolator());
                    aa.setDuration(duration);
                    mTouchBox.startAnimation(aa);

                    // Animation for message box
                    newFragment.onExpandStart();
                    Utils.animationChangeSize(mMessageBox, -1, getResources().getDimensionPixelSize(R.dimen.airsig_full_message_box_height), duration);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            newFragment.onExpandEnd();
                        }
                    }, duration);
                }

            }
        });
    }

    private void collapseMessageBox(final View contentView) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTouchBox.setVisibility(View.VISIBLE);
                // Animation
                int duration = getResources().getInteger(R.integer.airsig_message_box_animation_duration);

                if (mMessageBox.getMeasuredHeight() <= getResources().getDimensionPixelSize(R.dimen.airsig_small_message_box_height)) {
                    enableTouchArea(true);
                    mMessageBox.removeAllViews();
                    if (contentView != null) {
                        mMessageBox.addView(contentView);
                    }
                } else {
                    // Animation for touch area
                    AlphaAnimation aa = new AlphaAnimation(0f, 1f);
                    aa.setFillAfter(false);
                    aa.setInterpolator(new FastOutSlowInInterpolator());
                    aa.setDuration(duration);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            enableTouchArea(true);
                        }
                    }, aa.getDuration());
                    mTouchBox.startAnimation(aa);

                    // Animation for message box
                    Utils.animationChangeSize(mMessageBox, -1, getResources().getDimensionPixelSize(R.dimen.airsig_small_message_box_height), duration);

                    if (mMessageBox.getChildCount() > 0 && mMessageBoxFragment != null) {
                        mMessageBoxFragment.onCollapseStart();
                        AlphaAnimation aa2 = new AlphaAnimation(1f, 0f);
                        aa2.setFillAfter(false);
                        aa2.setInterpolator(new FastOutSlowInInterpolator());
                        aa2.setDuration(duration);
                        mMessageBoxFragment.getView().startAnimation(aa2);

                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                mMessageBoxFragment.onCollapseEnd();
                                mMessageBoxFragment = null;
                                mMessageBox.removeAllViews();
                                if (contentView != null) {
                                    mMessageBox.addView(contentView);
                                }
                            }
                        }, duration);
                    } else {
                        mMessageBox.removeAllViews();
                        if (contentView != null) {
                            mMessageBox.addView(contentView);
                        }
                    }
                }
            }
        });
    }

    private void updateFingerPositionOnScreen(final float x, final float y) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mImageThumb.setX(x - mImageThumb.getWidth() / 2);
                mImageThumb.setY(y - mImageThumb.getHeight() / 2 - getResources().getDimensionPixelSize(R.dimen.airsig_actionbar_height) - Utils.getStatusBarHeight(getApplicationContext()));
            }
        });
    }

    private void displaySecurityLevel(final int level, final boolean pass) {
        if (level < 0 || level > 5) {
            return;
        }
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = getLayoutInflater();
                ViewGroup contentView = (ViewGroup) inflater.inflate(R.layout.airsig_fragment_training_security_level, null);
                AutoFontSizeTextView titleView = (AutoFontSizeTextView) contentView.findViewById(R.id.textTitle);
                ImageView stars = (ImageView) contentView.findViewById(R.id.imageStars);
                AutoFontSizeTextView levelView = (AutoFontSizeTextView) contentView.findViewById(R.id.textLevel);

                switch (level) {
                    case 0:
                        levelView.setText(R.string.airsig_training_security_level_none);
                        stars.setImageResource(R.drawable.airsig_training_stars_0);
                        break;
                    case 1:
                        levelView.setText(R.string.airsig_training_security_level_insufficient);
                        stars.setImageResource(R.drawable.airsig_training_stars_1);
                        break;
                    case 2:
                        levelView.setText(R.string.airsig_training_security_level_insufficient);
                        stars.setImageResource(R.drawable.airsig_training_stars_2);
                        break;
                    case 3:
                        levelView.setText(R.string.airsig_training_security_level_insufficient);
                        stars.setImageResource(R.drawable.airsig_training_stars_3);
                        break;
                    case 4:
                        levelView.setText(R.string.airsig_training_security_level_insufficient);
                        stars.setImageResource(R.drawable.airsig_training_stars_4);
                        break;
                    case 5:
                        levelView.setText(R.string.airsig_training_security_level_insufficient);
                        stars.setImageResource(R.drawable.airsig_training_stars_5);
                        break;
                }

                // put into message box and display
                if (!pass) {
                    titleView.setText(R.string.airsig_training_security_level_fail_title);
                    expandMessageBox(contentView,
                            getResources().getString(R.string.airsig_training_security_level_fail_button),
                            new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    gotoStep(mCurrentStep);
                                }
                            }, null, null
                    );
                } else {
                    titleView.setText(R.string.airsig_training_security_level_pass_title);
                    levelView.setText(R.string.airsig_training_security_level_sufficient);
                    expandMessageBox(contentView,
                            getResources().getString(R.string.airsig_training_security_level_pass_postive_button),
                            new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mCurrentStep != null) {
                                        gotoStep(mCurrentStep.next);
                                    }
                                }
                            },
                            getResources().getString(R.string.airsig_training_security_level_pass_negative_button),
                            new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    reset();
                                }
                            }
                    );
                }
            }
        });
    }

    private void displayProgress(final float progress) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // initialize UI components
                if (mProgressBox == null) {
                    mProgressBox = getLayoutInflater().inflate(R.layout.airsig_fragment_training_progress, null);
                    mProgressText = (TextView) mProgressBox.findViewById(R.id.textProgress);
                    mProgressMessage = (TextView) mProgressBox.findViewById(R.id.textMessage);
                }
                collapseMessageBox(mProgressBox);

                // animation
                final Interpolator interpolator = new FastOutSlowInInterpolator();
                AnimationSet animationSet = new AnimationSet(false);
                int progressAnimationDuration = 400;
                int sizeAnimationDuration = 400;

                mProgressMessage.setVisibility(View.GONE);
                mProgressText.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                mProgressText.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                mProgressText.requestLayout();

                // progress animation
                final int originalProgress = mProgress;
                final int diff = (int) (progress * 100 - mProgress);
                Animation progressAnimation = new Animation() {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        mProgress = (int) (originalProgress + diff * interpolatedTime);
                        if (interpolatedTime >= 1) {
                            return;
                        }

                        SpannableString spanPercentage = new SpannableString(mProgress + "");
                        SpannableString spanPercent = new SpannableString("%");
                        SpannableString spanMsg = new SpannableString(getResources().getString(R.string.airsig_training_progress_hint));

                        spanPercentage.setSpan(new AbsoluteSizeSpan((int) Utils.sp2px(getResources(), 64)), 0, spanPercentage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spanPercent.setSpan(new AbsoluteSizeSpan((int) Utils.sp2px(getResources(), 24)), 0, spanPercent.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spanMsg.setSpan(new AbsoluteSizeSpan((int) getResources().getDimensionPixelSize(R.dimen.airsig_textsize_content)), 0, spanMsg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        mProgressText.setText(TextUtils.concat(spanPercentage, spanPercent, "\n", spanMsg));
                    }

                    @Override
                    public boolean willChangeBounds() {
                        return false;
                    }
                };
                progressAnimation.setDuration(progressAnimationDuration);
                animationSet.addAnimation(progressAnimation);

                // progress text size animation
                final int originWidth = mMessageBox.getMeasuredWidth();
                final int diffWidth = (int) Utils.dp2px(getResources(), 100) - originWidth;
                final int originTextSize = (int) Utils.sp2px(getResources(), 64);
                final int diffTextSize = (int) Utils.sp2px(getResources(), 48) - originTextSize;
                final int originTextSizePercent = (int) Utils.sp2px(getResources(), 24);
                final int diffTextSizePercent = (int) Utils.sp2px(getResources(), 14) - originTextSizePercent;
                Animation sizeAnimation = new Animation() {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        if (interpolatedTime == 0) {
                            return;
                        }
                        float it = interpolator.getInterpolation(interpolatedTime);

                        SpannableString spanPercentage = new SpannableString(mProgress + "");
                        SpannableString spanPercent = new SpannableString("%");

                        spanPercentage.setSpan(new AbsoluteSizeSpan((int) (originTextSize + diffTextSize * it)), 0, spanPercentage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spanPercent.setSpan(new AbsoluteSizeSpan((int) (originTextSizePercent + diffTextSizePercent * it)), 0, spanPercent.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mProgressText.setText(TextUtils.concat(spanPercentage, spanPercent));

                        mProgressText.getLayoutParams().width = (int) (originWidth + diffWidth * it);
                        mProgressText.requestLayout();

                        if (interpolatedTime == 1) {
                            mProgressMessage.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public boolean willChangeBounds() {
                        return true;
                    }
                };
                sizeAnimation.setStartOffset(progressAnimationDuration);
                sizeAnimation.setDuration(sizeAnimationDuration);
                animationSet.addAnimation(sizeAnimation);

                mProgressBox.startAnimation(animationSet);
            }
        });
    }

    private void showImageMessage(final CharSequence message, final Integer gifResourceId,
                                  final String customButtonString, final OnClickListener positiveButtonClickListener,
                                  final String negativeButton, final OnClickListener negativeButtonClickListener) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View contentView = (View) getLayoutInflater().inflate(R.layout.airsig_fragment_training_image_message, null);
                if (gifResourceId != null) {
                    AnimatedGifImageView imageView = (AnimatedGifImageView) contentView.findViewById(R.id.gifImageView);
                    imageView.setVisibility(View.VISIBLE);
                    if ("drawable".compareToIgnoreCase(getResources().getResourceTypeName(gifResourceId)) == 0) {
                        imageView.setImageResource(gifResourceId);
                    } else {
                        imageView.setAnimatedGif(gifResourceId, AnimatedGifImageView.TYPE.FIT_CENTER);
                    }
                }
                if (message != null) {
                    AutoFontSizeTextView textView = (AutoFontSizeTextView) contentView.findViewById(R.id.textView);
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(message);
                }

                // put into message box and display
                expandMessageBox(contentView,
                        (customButtonString == null ? getResources().getString(R.string.airsig_training_err_default_button) : customButtonString), positiveButtonClickListener,
                        negativeButton, negativeButtonClickListener
                );
            }
        });
    }

    private void showTextMessage(final String title, final String detail,
                                 final String customButtonString, final OnClickListener positiveButtonClickListener,
                                 final String negativeButton, final OnClickListener negativeButtonClickListener) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View contentView = (View) getLayoutInflater().inflate(R.layout.airsig_fragment_training_text_message, null);
                if (detail != null) {
                    TextView titleView = (TextView) contentView.findViewById(R.id.textTitle);
                    titleView.setVisibility(View.VISIBLE);
                    titleView.setText(title);
                }
                if (detail != null) {
                    TextView detailView = (TextView) contentView.findViewById(R.id.textContent);
                    detailView.setVisibility(View.VISIBLE);
                    detailView.setText(detail);
                }


                // put into message box and display
                expandMessageBox(contentView,
                        (customButtonString == null ? getResources().getString(R.string.airsig_training_err_default_button) : customButtonString), positiveButtonClickListener,
                        negativeButton, negativeButtonClickListener
                );
            }
        });
    }

    private void showDialog(final String title, final String detail, final Integer image,
                            final String postiveButton, final OnClickListener positiveButtonClickListener,
                            final String negativeButton, final OnClickListener negativeButtonClickListener) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertBuilder alert = new AlertBuilder(TrainingActivity.this);
                alert.setTitle(title);
                alert.setDetailedMessage(detail);
                if (image != null) {
                    if ("drawable".compareToIgnoreCase(getResources().getResourceTypeName(image)) == 0) {
                        alert.setImageResource(image);
                    } else {
                        alert.setGifResource(image);
                    }
                }
                alert.setPositiveButton(postiveButton, positiveButtonClickListener);
                alert.setNegativeButton(negativeButton, negativeButtonClickListener);
                alert.setCancelable(false);
                alert.show();
            }
        });
    }


    private void finishActivity(final boolean result) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Log event
                if (result) {
                    EventLogger.increaseLocalIntProperty(EventLogger.EVENT_PROP_TRAINING_SUCCESS_TIMES, 1);
                    JSONObject userProperties = new JSONObject();
                    try {
                        userProperties.put(EventLogger.USER_PROP_TRAINING_RESULT_STRENGTH, mTrainingAction.strength.name());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    EventLogger.setUserProperties(userProperties);
                    EventLogger.incrementUserProperty(EventLogger.USER_PROP_TRAINING_SUCCESS_TIMES, 1);
                }

                JSONObject eventProperties = new JSONObject();
                try {
                    eventProperties.put(EventLogger.EVENT_PROP_TRAINING_SUCCESS, result);
                    eventProperties.put(EventLogger.EVENT_PROP_TRAINING_SIGN_COUNT, mSignatureInputCount);
                    eventProperties.put(EventLogger.EVENT_PROP_TRAINING_RESULT_STRENGTH, mTrainingAction.strength.name());
                    eventProperties.put(EventLogger.EVENT_PROP_TRAINING_SUCCESS_TIMES, EventLogger.getLocalIntProperty(EventLogger.EVENT_PROP_TRAINING_SUCCESS_TIMES, 0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                EventLogger.endAction(EventLogger.EVENT_NAME_TRAINING, eventProperties);

                Intent intent = new Intent(mResultEventName);
                intent.putExtra(kTrainingResult, result);
                intent.putExtra(kIsRetrain, mIsRetrain);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                finish();
            }
        });
    }

    private boolean isTrainingNotCompleted() {
        return (mTrainingAction.strength == ASStrength.ASStrengthNoData || mTrainingAction.numberOfSignatureStillNeedBeforeVerify > 0);
    }

    private boolean isWaitingForInputSignature() {
        if (this.isWaiting()) {
            return false;
        }

        return true;
    }

    private boolean isWaiting() {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            return true;
        } else {
            return false;
        }
    }

    private void showWaiting(final boolean show) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    mProgressBar.setVisibility(View.VISIBLE);
                } else {
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void showToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, (msg.length() > 5) ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeSuccessSound() {
        makeABeepSound();
    }

    private void makeFailedSound() {
        makeABeepSound();
        makeABeepSound();
    }

    private void makeABeepSound() {
        // a sound generator
        if (null == mToneGenerator) {
            mToneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
        }
        // a no sound generator - for fixing bad first sound in some phone models
        if (null == mToneGenerator2) {
            mToneGenerator2 = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MIN_VOLUME);
        }

        mToneGenerator2.startTone(ToneGenerator.TONE_PROP_BEEP);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mToneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
    }

    public static class MessageBoxFragment extends Fragment {

        private FrameLayout mContentContainer;
        private TextView mPositiveButton, mNegativeButton;
        private ImageView mHintIcon;

        private View mContentView;
        private String mPositiveButtonText = null, mNegativeButtonText = null;
        private OnClickListener mPositiveButtonClickListener = null, mNegativeButtonClickListener = null;
        private int mButtonVisibility = View.VISIBLE;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.airsig_fragment_training_message_box, container, false);

            mContentContainer = (FrameLayout) rootView.findViewById(R.id.viewContent);
            setContentView(mContentView);
            mPositiveButton = (TextView) rootView.findViewById(R.id.button_positive);
            privateSetPositiveButton(mPositiveButtonText, mPositiveButtonClickListener);
            mNegativeButton = (TextView) rootView.findViewById(R.id.button_negative);
            privateSetNegativeButton(mNegativeButtonText, mNegativeButtonClickListener);

            mHintIcon = (ImageView) rootView.findViewById(R.id.imageHintIcon);

            return rootView;
        }

        public void onExpandStart() {
            if (mContentView != null) {
                mContentView.setVisibility(View.INVISIBLE);
            }
            mButtonVisibility = View.INVISIBLE;
            privateSetPositiveButton(mPositiveButtonText, mPositiveButtonClickListener);
            privateSetNegativeButton(mNegativeButtonText, mNegativeButtonClickListener);
        }

        public void onExpandEnd() {
            if (mContentView != null) {
                mContentView.setVisibility(View.VISIBLE);
            }
            mButtonVisibility = View.VISIBLE;
            privateSetPositiveButton(mPositiveButtonText, mPositiveButtonClickListener);
            privateSetNegativeButton(mNegativeButtonText, mNegativeButtonClickListener);
        }

        public void onCollapseStart() {
            if (mContentView != null) {
                mContentView.setVisibility(View.INVISIBLE);
            }
            mButtonVisibility = View.INVISIBLE;
            privateSetPositiveButton(mPositiveButtonText, mPositiveButtonClickListener);
            privateSetNegativeButton(mNegativeButtonText, mNegativeButtonClickListener);
        }

        public void onCollapseEnd() {
            // do nothing
        }

        public void setContentView(View view) {
            mContentView = view;
            if (mContentContainer != null) {
                mContentContainer.removeAllViews();
                if (mContentView != null) {
                    mContentContainer.addView(view);
                }
            }
        }

        public void setPositiveButton(String title, final OnClickListener clickListener) {
            mPositiveButtonText = title;
            mPositiveButtonClickListener = clickListener;
            privateSetPositiveButton(title, clickListener);
        }

        private void privateSetPositiveButton(String title, final OnClickListener clickListener) {
            if (mPositiveButton == null) {
                return;
            }

            if (title == null || title.length() == 0) {
                mPositiveButton.setVisibility(View.GONE);
                return;
            }
            mPositiveButton.setText(title);
            mPositiveButton.setVisibility(mButtonVisibility);
            mPositiveButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != clickListener) {
                        clickListener.onClick(mPositiveButton);
                    }
                }
            });
        }

        public void setNegativeButton(String title, final OnClickListener clickListener) {
            mNegativeButtonText = title;
            mNegativeButtonClickListener = clickListener;
            privateSetNegativeButton(title, clickListener);
        }

        private void privateSetNegativeButton(String title, final OnClickListener clickListener) {
            if (mNegativeButton == null) {
                return;
            }

            if (title == null || title.length() == 0) {
                mNegativeButton.setVisibility(View.GONE);
                return;
            }
            mNegativeButton.setText(title);
            mNegativeButton.setVisibility(mButtonVisibility);
            mNegativeButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != clickListener) {
                        clickListener.onClick(mNegativeButton);
                    }
                }
            });
        }

        public void showHintIcon(boolean show) {
            if (show) {
                mHintIcon.setVisibility(View.VISIBLE);
                Utils.blinkView(mHintIcon);
            } else {
                mHintIcon.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ct_back_rl:
                onBackPressed();
                break;
            case R.id.airsig_teach_click:
                if (mHelpPage.getVisibility() != View.VISIBLE) {
                    // Log event
                    EventLogger.logEvent(EventLogger.EVENT_NAME_TRAINING_CLICK_TUTORIAL, null);
                }

                if (mHelpPage.getVisibility() == View.VISIBLE) {
                    mMainPage.setVisibility(View.VISIBLE);

                    ScaleAnimation sa = new ScaleAnimation(1f, 0f, 1f, 0f, mHelpPage.getWidth() / 2, mHelpPage.getHeight() / 2);
                    sa.setDuration(300);
                    AlphaAnimation aa = new AlphaAnimation(1f, 0f);
                    aa.setDuration(300);
                    AnimationSet as = new AnimationSet(true);
                    as.setInterpolator(new AccelerateInterpolator());
                    as.addAnimation(sa);
                    as.addAnimation(aa);

//                    item.setEnabled(false);
                    mHelpPage.startAnimation(sa);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            mHelpPage.setVisibility(View.INVISIBLE);
//                            item.setEnabled(true);
                        }
                    }, sa.getDuration());
                } else {

                    mMainPage.setVisibility(View.INVISIBLE);

                    mHelpPage.setVisibility(View.VISIBLE);
                    ScaleAnimation sa = new ScaleAnimation(0f, 1f, 0f, 1f, mHelpPage.getWidth() / 2, mHelpPage.getHeight() / 2);
                    sa.setDuration(300);
                    AlphaAnimation aa = new AlphaAnimation(0f, 1f);
                    aa.setDuration(300);
                    AnimationSet as = new AnimationSet(true);
                    as.setInterpolator(new AccelerateDecelerateInterpolator());
                    as.addAnimation(sa);
                    as.addAnimation(aa);

//                    item.setEnabled(false);
                    mHelpPage.startAnimation(as);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
//                            item.setEnabled(true);
                        }
                    }, sa.getDuration());
                }
                break;

            case R.id.airsig_reset_click:
                if (mHelpPage.getVisibility() == View.VISIBLE) {
                    return;
                }

                showTipsDialog(getResources().getString(R.string.airsig_training_dialog_confirm_reset_detail),
                        getResources().getString(R.string.airsig_training_dialog_confirm_reset_postive_button),
                        getResources().getString(R.string.airsig_training_dialog_confirm_reset_negative_button),
                        RESET_DIALOG);

//                showDialog(getResources().getString(R.string.airsig_training_dialog_confirm_reset_title),
//                        getResources().getString(R.string.airsig_training_dialog_confirm_reset_detail),
//                        R.drawable.airsig_ic_dialog,
//                        getResources().getString(R.string.airsig_training_dialog_confirm_reset_postive_button),
//                        new OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                // Log event
//                                EventLogger.logEvent(EventLogger.EVENT_NAME_TRAINING_RESET, null);
//
//                                reset();
//                            }
//                        },
//                        getResources().getString(R.string.airsig_training_dialog_confirm_reset_negative_button), null
//                );
                break;
        }
    }
}
