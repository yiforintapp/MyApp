
package com.leo.appmaster.fragment;

import android.annotation.SuppressLint;
import android.content.pm.VerifierInfo;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.leo.appmaster.PhoneInfo;
import com.leo.appmaster.R;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;

public class PretendAppZhiWenFragment extends PretendFragment implements OnClickListener {
    private final static int SHOWLOCK = 1;
    private final static int SHOWUNLOCK = 2;
    private final static int FINISHFRAGMENT = 3;
    private View zhiwen_content;
    private ImageView iv_zhiwen_tips, zhiwen_bang, show_slowly_iv, iv_zhiwen_click, finger_lock,
            finger_unlock;
    private float iv_zhiwen_click_height;
    private boolean isClick = false;
    private int mVersion;
    // three click
    long[] mHits = new long[3];

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOWLOCK:
                    finger_lock.setVisibility(View.VISIBLE);
                    finger_unlock.setVisibility(View.GONE);
                    break;
                case SHOWUNLOCK:
                    finger_lock.setVisibility(View.GONE);
                    finger_unlock.setVisibility(View.VISIBLE);
                    break;
                case FINISHFRAGMENT:
                    SDKWrapper
                            .addEvent(mActivity, SDKWrapper.P1, "appcover", "done_FingerPrint");
                    onUnlockPretendSuccessfully();
                    finger_lock.setVisibility(View.GONE);
                    finger_unlock.setVisibility(View.GONE);
                    break;
            }
        }

        ;
    };


    @Override
    protected int layoutResourceId() {
        return R.layout.activity_weizhuang_zhiwen;
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub

        super.onResume();
    }


    @Override
    protected void onInitUI() {
        zhiwen_content = findViewById(R.id.zhiwen_content);

        android.widget.LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        zhiwen_content.setLayoutParams(lp);

        SDKWrapper
                .addEvent(mActivity, SDKWrapper.P1, "appcover", "FingerPrint");

        iv_zhiwen_click = (ImageView) findViewById(R.id.iv_zhiwen_click);
        iv_zhiwen_click.setOnClickListener(this);
        show_slowly_iv = (ImageView) findViewById(R.id.show_slowly_iv);
        zhiwen_bang = (ImageView) findViewById(R.id.zhiwen_bang);
        finger_lock = (ImageView) findViewById(R.id.finger_lock);
        finger_unlock = (ImageView) findViewById(R.id.finger_unlock);

        mVersion = PhoneInfo.getAndroidVersion();
        getZhiWen();
    }

    @SuppressLint("NewApi")
    private void getZhiWen() {
        ViewTreeObserver guaduan = iv_zhiwen_click.getViewTreeObserver();
        guaduan.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                iv_zhiwen_click_height = iv_zhiwen_click.getHeight();
                // 成功调用一次后，移除 Hook 方法，防止被反复调用
                // removeGlobalOnLayoutListener() 方法在 API 16 后不再使用
                // 使用新方法 removeOnGlobalLayoutListener() 代替
                if (mVersion > 16) {
                    iv_zhiwen_click.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    iv_zhiwen_click.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                showDongHuaTrans(0, iv_zhiwen_click_height - 30);
                showDongHuaAlpha(0f, 1f);
            }
        });
    }

    @Override
    public boolean onBackPressed() {
        // TODO Auto-generated method stub
        SDKWrapper
                .addEvent(mActivity, SDKWrapper.P1, "appcover", "fail_FingerPrint");
        return super.onBackPressed();
    }


    private void showDongHuaAlpha(final float i, final float j) {
        // zhiwen
        AlphaAnimation alpha = new AlphaAnimation(i, j);
        alpha.setDuration(3000);
        alpha.setFillAfter(true);
        alpha.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (i < j) {
                    showDongHuaAlpha(1f, 0f);
                } else {
                    showDongHuaAlpha(0f, 1f);
                }
            }
        });
        show_slowly_iv.setAnimation(alpha);
    }

    private void showDongHuaTrans(final float k, final float z) {
        // zhiwen bang
        TranslateAnimation trans = new TranslateAnimation(0,
                0, k, z);
        trans.setDuration(3000);
        trans.setFillAfter(true);
        trans.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (k < z) {
                    showDongHuaTrans(iv_zhiwen_click_height - 30, 0);
                } else {
                    showDongHuaTrans(0, iv_zhiwen_click_height - 30);
                }
            }
        });
        zhiwen_bang.setAnimation(trans);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_zhiwen_click:

                if (!isClick) {
                    zhiwen_bang.setVisibility(View.VISIBLE);
                }
                isClick = true;

                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - 800)) {

                    showFeedBack();

                }
                break;
            default:
                break;
        }
    }

    private void showFeedBack() {
        LeoLog.i("PAZWF", "showFeedBack!");
        mHandler.sendEmptyMessage(SHOWLOCK);
        mHandler.sendEmptyMessageDelayed(SHOWUNLOCK, 300);
        mHandler.sendEmptyMessageDelayed(FINISHFRAGMENT, 600);
    }

}
