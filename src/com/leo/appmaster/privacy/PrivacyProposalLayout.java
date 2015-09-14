
package com.leo.appmaster.privacy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.privacy.PrivacyHelper.Level;
import com.leo.appmaster.privacycontact.PrivacyContactActivity;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.videohide.VideoHideMainActivity;

public class PrivacyProposalLayout extends RelativeLayout implements OnClickListener {

    private final static int APPTRANSIONY = 60;
    private final static int OTHERTRANSIONY = 100;

    private View mStatusBar;
    private CommonTitleBar mTitleBar;
    private TextView mProposalTip;
    private PrivacyStatusView mProposalStatus;
    private PrivacyLevelSmallView mLevelView;
    private ScrollView mProposalList;
    private View mProposalApp;
    private View mProposalPic;
    private View mProposalVideo;
    private View mProposalContact;
    // private View mProposalAd;

    private TextView mAppLockSuggest;
    private TextView mAppLockDes;
    private TextView mHidePicSuggest;
    private TextView mHidePicDes;
    private TextView mHideVideoSuggest;
    private TextView mHideVideoDes;
    private TextView mPrivacyContactSuggest;
    private TextView mPrivacyContactDes;

    private int mColor;
    private int mBgColor;
    private int mWhiteColor;
    private int mTransColor;

    private boolean mFirstAnimating = false;
    private boolean mSecondAnimating = false;
    private Drawable mAnimDrawable;
    private Rect mAnimStartRect = new Rect();
    private Rect mAnimEndRect = new Rect();
    private Rect mAnimDrawRect = new Rect();

    private int mHomeTitleHeight;
    private int mStatusBarHeight;
    private int mTitleAlpha;
    private int mLayerAlpha;
    private Rect mOverlayStartRect = new Rect();
    private Rect mOverlayEndRect = new Rect();
    private Rect mOverlayDrawRect = new Rect();

    private ValueAnimator mFirstAnimator;
    private ValueAnimator mSecondAnimator;
    private ValueAnimator mCloseAnimator;
    private Paint mPaint;
    private Context mContext;

    public PrivacyProposalLayout(Context context) {
        this(context, null);
        this.mContext = context;
    }

    public PrivacyProposalLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.mContext = context;
    }

    public PrivacyProposalLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        mAnimDrawable = getResources().getDrawable(R.drawable.privacy_level_bg);
        mPaint = new Paint();
        mPaint.setStyle(Style.FILL);
        Resources res = getResources();
        mStatusBarHeight = res.getDimensionPixelSize(R.dimen.statusbar_height);
        mHomeTitleHeight = res.getDimensionPixelSize(R.dimen.home_shader_view_height);
        mBgColor = res.getColor(R.color.privacy_proposal_bg);
        mWhiteColor = res.getColor(R.color.white);
        mTransColor = res.getColor(R.color.transparent);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mStatusBar = findViewById(R.id.bg_statusbar);
        if (VERSION.SDK_INT < 19) {
            mStatusBar.setVisibility(View.GONE);
        }

        mTitleBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mTitleBar.setTitle(R.string.privacy_suggest_title);
        mTitleBar.openBackView();
        mTitleBar.setBackViewListener(this);
        mTitleBar.setBackArrowImg(R.drawable.arrow_down_icon);

        mProposalTip = (TextView) findViewById(R.id.proposal_tip);
        mProposalStatus = (PrivacyStatusView) findViewById(R.id.proposal_status);
        mLevelView = (PrivacyLevelSmallView) findViewById(R.id.proposal_level);

        mProposalList = (ScrollView) findViewById(R.id.proposal_list);
        mProposalApp = findViewById(R.id.privacy_suggest_applock);

        mAppLockSuggest = (TextView) mProposalApp.findViewById(R.id.suggest_applock_suggest);
        mAppLockDes = (TextView) mProposalApp.findViewById(R.id.suggest_applock_description);
        mProposalApp.setOnClickListener(this);

        mProposalPic = findViewById(R.id.privacy_suggest_hide_pic);
        mHidePicSuggest = (TextView) mProposalPic.findViewById(R.id.suggest_hide_pic_suggest);
        mHidePicDes = (TextView) mProposalPic.findViewById(R.id.suggest_hide_pic_description);
        mProposalPic.setOnClickListener(this);

        mProposalVideo = findViewById(R.id.privacy_suggest_hide_video);
        mHideVideoSuggest = (TextView) mProposalVideo.findViewById(R.id.suggest_hide_video_suggest);
        mHideVideoDes = (TextView) mProposalVideo.findViewById(R.id.suggest_hide_video_description);
        mProposalVideo.setOnClickListener(this);

        mProposalContact = findViewById(R.id.privacy_suggest_privacy_contact);
        mPrivacyContactSuggest = (TextView) mProposalContact
                .findViewById(R.id.suggest_privacy_contact_suggest);
        mPrivacyContactDes = (TextView) mProposalContact
                .findViewById(R.id.suggest_privacy_contact_description);
        mProposalContact.setOnClickListener(this);

        // mProposalAd = findViewById(R.id.privacy_ad_item);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mFirstAnimating) {
            mPaint.setAlpha(255);
            mPaint.setColor(mWhiteColor);
            canvas.drawRect(mOverlayDrawRect.left, mOverlayDrawRect.bottom, mOverlayDrawRect.right,
                    getHeight(), mPaint);

            mPaint.setColor(mColor);
            mPaint.setAlpha(mTitleAlpha);
            canvas.drawRect(mOverlayDrawRect, mPaint);

            mPaint.setAlpha(255);
            canvas.drawRect(mAnimDrawRect, mPaint);
            mAnimDrawable.setBounds(mAnimDrawRect);
            mAnimDrawable.draw(canvas);
        } else if (mSecondAnimating) {
            mPaint.setColor(mColor);
            mPaint.setAlpha(mLayerAlpha);
            canvas.drawRect(mOverlayDrawRect, mPaint);

            mPaint.setAlpha(mLayerAlpha);
            mPaint.setColor(Color.argb(mLayerAlpha, 255, 255, 255));
            int top = 0;
            if (VERSION.SDK_INT >= 19) {
                top = mStatusBarHeight;
            }
            canvas.drawRect(0, top + mAnimEndRect.bottom, getWidth(), getHeight(), mPaint);
        }
    }

    public void show(Rect animRect) {
        onLevelChange(-1, true);
        playAnim(animRect);
    }

    private void playAnim(Rect animRect) {
        if (mFirstAnimator != null) {
            mFirstAnimator.cancel();
        }
        int top = 0;
        if (VERSION.SDK_INT >= 19) {
            top = mStatusBarHeight;
        }
        mOverlayStartRect.set(0, top, getWidth(), top + mHomeTitleHeight);
        mOverlayEndRect.set(0, top, getWidth(), top + mTitleBar.getMeasuredHeight());

        int[] pos = new int[2];
        getLocationOnScreen(pos);
        mAnimStartRect.set(animRect);
        mAnimStartRect.offset(-pos[0], -pos[1]);
        mLevelView.getLevelRectOnScreen(mAnimEndRect);
        mAnimEndRect.offset(-pos[0], -pos[1]);

        mFirstAnimator = new ValueAnimator();
        mFirstAnimator.setDuration(500);
        mFirstAnimator.setFloatValues(0.0f, 1.0f);
        mFirstAnimator.removeAllUpdateListeners();
        mFirstAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                final float percent = (Float) animation.getAnimatedValue();
                int fromSize = mAnimStartRect.width();
                int toSize = mAnimEndRect.width();
                int left = mAnimStartRect.left
                        + (int) Math
                                .round(((mAnimEndRect.left - mAnimStartRect.left) * percent));
                int top = mAnimStartRect.top
                        + (int) Math
                                .round(((mAnimEndRect.top - mAnimStartRect.top) * percent));
                int size = fromSize + (int) Math.round((toSize - fromSize) * percent);
                mAnimDrawRect.set(left, top, left + size, top + size);

                mTitleAlpha = (int) (255 * percent);
                fromSize = mOverlayStartRect.height();
                toSize = mOverlayEndRect.height();
                size = fromSize + (int) Math.round((toSize - fromSize) * percent);
                left = mOverlayStartRect.left;
                top = mOverlayStartRect.top;
                mOverlayDrawRect.set(left, top, getWidth(), top + size);

                invalidate();

                if (percent > 0.95f) {
                    fadeOut();
                }
            }
        });
        mFirstAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mFirstAnimating = true;
                mTitleAlpha = 0;
                mAnimDrawRect.set(mAnimStartRect);
                mOverlayDrawRect.set(mOverlayStartRect);
                setBackgroundColor(mTransColor);
                setVisibility(View.VISIBLE);
                mTitleBar.setVisibility(View.INVISIBLE);
                mProposalList.setVisibility(View.INVISIBLE);
            }

            public void onAnimationEnd(Animator animation) {
                mFirstAnimating = false;
                mTitleAlpha = 255;
                setBackgroundColor(mBgColor);
                mTitleBar.setVisibility(View.VISIBLE);
                mProposalList.setVisibility(View.VISIBLE);

                // mProposalAd.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mFirstAnimating = false;
                mTitleAlpha = 255;
                setBackgroundColor(mBgColor);
                mTitleBar.setVisibility(View.VISIBLE);
                mProposalList.setVisibility(View.VISIBLE);
            }
        });
        mFirstAnimator.start();
    }

    private void fadeOut() {
        if (mSecondAnimating) {
            return;
        }
        if (mSecondAnimator != null) {
            mSecondAnimator.cancel();
        }
        mSecondAnimator = new ValueAnimator();
        mSecondAnimator.setDuration(500);
        mSecondAnimator.setFloatValues(1.0f, 0.0f);
        mSecondAnimator.removeAllUpdateListeners();
        mSecondAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                final float percent = (Float) animation.getAnimatedValue();
                mLayerAlpha = (int) (255 * percent);
                int appTrans = (int) (APPTRANSIONY * percent);
                int otherTrans = (int) (OTHERTRANSIONY * percent);
                setChildTransion(appTrans, otherTrans);
                invalidate();
                if (percent < 0.05f) {
                    mProposalStatus.playAnim();
                }
            }
        });
        mSecondAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mSecondAnimating = true;
            }

            public void onAnimationEnd(Animator animation) {
                mSecondAnimating = false;
                setChildTransion(0, 0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mSecondAnimating = false;
                setChildTransion(0, 0);
            }
        });
        mSecondAnimator.start();
    }

    private void setChildTransion(int appTrans, int otherTrans) {
        mProposalApp.setTranslationY(appTrans);
        mProposalPic.setTranslationY(otherTrans);
        mProposalVideo.setTranslationY(otherTrans);
        mProposalContact.setTranslationY(otherTrans);
    }

    public void close(boolean animation) {
        cancelAnimations();
        if (animation) {
            mCloseAnimator = new ValueAnimator();
            mCloseAnimator.setDuration(300);
            mCloseAnimator.setIntValues(0, getHeight());
            mCloseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    final int percent = (Integer) animation.getAnimatedValue();
                    setTranslationY(percent);
                }
            });
            mCloseAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    ((HomeActivity) mContext).shouldShowAd();
                    setVisibility(View.INVISIBLE);
                    setTranslationY(0);
                }
            });
            mCloseAnimator.start();
        } else {
            ((HomeActivity) mContext).shouldShowAd();
            setVisibility(View.INVISIBLE);
        }
    }

    private void cancelAnimations() {
        if (mFirstAnimator != null) {
            mFirstAnimator.cancel();
        }
        if (mSecondAnimator != null) {
            mSecondAnimator.cancel();
        }
        if (mProposalStatus != null) {
            mProposalStatus.cancelAnimation();
        }
        if (mCloseAnimator != null) {
            mCloseAnimator.cancel();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        Context context = getContext();
        switch (v.getId()) {
            case R.id.layout_title_back:
                close(true);
                break;
            case R.id.privacy_suggest_applock:
                LockManager lm = LockManager.getInstatnce();
                LockMode curMode = LockManager.getInstatnce().getCurLockMode();
                if (curMode != null && curMode.defaultFlag == 1 && !curMode.haveEverOpened) {
                    intent = new Intent(context, RecommentAppLockListActivity.class);
                    intent.putExtra("target", 0);
                    context.startActivity(intent);
                    curMode.haveEverOpened = true;
                    lm.updateMode(curMode);
                } else {
                    intent = new Intent(context, AppLockListActivity.class);
                    context.startActivity(intent);
                    SDKWrapper.addEvent(context, SDKWrapper.P1, "proposals", "applock");
                }
                break;
            case R.id.privacy_suggest_hide_pic:
                intent = new Intent(context, ImageHideMainActivity.class);
                context.startActivity(intent);
                SDKWrapper.addEvent(context, SDKWrapper.P1, "proposals", "hideimage");
                break;
            case R.id.privacy_suggest_hide_video:
                intent = new Intent(context, VideoHideMainActivity.class);
                context.startActivity(intent);
                SDKWrapper.addEvent(context, SDKWrapper.P1, "proposals", "hidevideo");
                break;
            case R.id.privacy_suggest_privacy_contact:
                intent = new Intent(context, PrivacyContactActivity.class);
                intent.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT,
                        PrivacyContactUtils.TO_PRIVACY_CONTACT_FLAG);
                context.startActivity(intent);
                SDKWrapper.addEvent(context, SDKWrapper.P1, "proposals", "contacts");
                break;
            default:
                break;
        }
    }

    public boolean isActive() {
        return getVisibility() == View.VISIBLE;
    }

    public void onLevelChange(int color, boolean init) {
        PrivacyHelper ph = PrivacyHelper.getInstance(getContext());
        Level level = ph.getPrivacyLevel();
        if (color == -1) {
            color = ph.getCurLevelColor().toIntColor();
        }
        mColor = color;
        mStatusBar.setBackgroundColor(color);
        mTitleBar.setBackgroundColor(color);
        mLevelView.invalidate(color);
        mProposalStatus.invalidate(level, init);
        if (init) {
            mProposalList.scrollTo(0, 0);
        }
        mProposalTip.setText(getResources().getString(R.string.privacy_proposal_tip,
                ph.getLevelDescription(level)));

        boolean appLockActive = ph.isVariableActived(PrivacyHelper.VARABLE_APP_LOCK);
        mAppLockSuggest.setText(appLockActive ? R.string.privacy_more_app_lock
                : R.string.privacy_no_app_lock);
        mAppLockDes.setText(appLockActive ? R.string.privacy_more_app_lock_des
                : R.string.privacy_no_app_lock_des);

        boolean hidePicActive = ph.isVariableActived(PrivacyHelper.VARABLE_HIDE_PIC);
        mHidePicSuggest.setText(hidePicActive ? R.string.privacy_more_pic_hide
                : R.string.privacy_no_pic_hide);
        mHidePicDes.setText(hidePicActive ? R.string.privacy_more_pic_hide_des
                : R.string.privacy_no_pic_hide_des);

        boolean hideVideoActive = ph.isVariableActived(PrivacyHelper.VARABLE_HIDE_VIDEO);
        mHideVideoSuggest.setText(hideVideoActive ? R.string.privacy_more_video_hide
                : R.string.privacy_no_video_hide);
        mHideVideoDes.setText(hideVideoActive ? R.string.privacy_more_video_hide_des
                : R.string.privacy_no_video_hide_des);

        boolean privacyContactActive = ph.isVariableActived(PrivacyHelper.VARABLE_PRIVACY_CONTACT);
        mPrivacyContactSuggest.setText(privacyContactActive ? R.string.privacy_more_privacy_contact
                : R.string.privacy_no_privacy_contact);
        mPrivacyContactDes.setText(privacyContactActive ? R.string.privacy_more_privacy_contact_des
                : R.string.privacy_no_privacy_contact_des);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

}
