
package com.leo.appmaster.fragment;

import android.os.SystemClock;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

import com.leo.appmaster.PhoneInfo;
import com.leo.appmaster.R;
import com.leo.appmaster.sdk.SDKWrapper;

public class PretendAppZhiWenFragment extends PretendFragment implements OnClickListener {
    private View zhiwen_content;
    private ImageView iv_zhiwen_tips, zhiwen_bang, show_slowly_iv, iv_zhiwen_click;
    private float iv_zhiwen_click_height;
    private int mVersion;
    // three click
    long[] mHits = new long[3];

    @Override
    protected int layoutResourceId() {
        return R.layout.activity_weizhuang_zhiwen;
    }

    @Override
    protected void onInitUI() {
        zhiwen_content = findViewById(R.id.zhiwen_content);

        // make content match the screen
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        Window window = mActivity.getWindow();
        LayoutParams windowLayoutParams = window.getAttributes(); // 获取对话框当前的参数值
        windowLayoutParams.width = (int) (display.getWidth());
        windowLayoutParams.height = (int) (display.getHeight());
        zhiwen_content.setLayoutParams(windowLayoutParams);

        SDKWrapper
                .addEvent(mActivity, SDKWrapper.P1, "appcover", "FingerPrint");

        iv_zhiwen_click = (ImageView) findViewById(R.id.iv_zhiwen_click);
        iv_zhiwen_click.setOnClickListener(this);
        show_slowly_iv = (ImageView) findViewById(R.id.show_slowly_iv);
        zhiwen_bang = (ImageView) findViewById(R.id.zhiwen_bang);

        mVersion = PhoneInfo.getAndroidVersion();
        getZhiWen();
    }

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
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - 800)) {
                    onUnlockPretendSuccessfully();
                    SDKWrapper
                            .addEvent(mActivity, SDKWrapper.P1, "appcover", "done_FingerPrint");
                }
                break;
            default:
                break;
        }
    }

}
