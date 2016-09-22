package com.zlf.appmaster.setting;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.zlf.appmaster.R;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.db.LeoSettings;
import com.zlf.appmaster.ui.CommonSettingItem;
import com.zlf.appmaster.ui.RippleView;
import com.zlf.appmaster.utils.AppUtil;
import com.zlf.appmaster.utils.PrefConst;
import com.zlf.tools.animator.Animator;
import com.zlf.tools.animator.AnimatorListenerAdapter;
import com.zlf.tools.animator.AnimatorSet;
import com.zlf.tools.animator.ObjectAnimator;

public class SettingActivity extends Activity implements View.OnClickListener {

    private RippleView mExitLogin;
    private CommonSettingItem mClearCache;
    private CommonSettingItem mHelp;

    private ImageView mCleanIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        init();
        setListener();
    }

    private void init() {
        mExitLogin = (RippleView) findViewById(R.id.exit_login);
        mHelp = (CommonSettingItem) findViewById(R.id.help);
        mClearCache = (CommonSettingItem) findViewById(R.id.clear_cache);
        mClearCache.setIcon(getResources().getDrawable(R.drawable.clean_prepare));
        mClearCache.setArrowVisable(false);
        mCleanIcon = mClearCache.getIcon();

        mClearCache.setTitle("清除缓存");
        mHelp.setTitle("帮助");
        View line_two = mHelp.findViewById(R.id.line_2);
        line_two.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AppUtil.isLogin()) {
            mExitLogin.setVisibility(View.VISIBLE);
        } else {
            mExitLogin.setVisibility(View.GONE);
        }
    }

    private void setListener() {
        mExitLogin.setOnClickListener(this);
        mClearCache.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnim();
            }
        });
        mHelp.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void startAnim() {
        mClearCache.setEnable(false);
        mCleanIcon.setImageResource(R.drawable.clean_in);
        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(mCleanIcon, "rotation", 0f, 1080f);
        rotateAnim.setDuration(1500);

        AnimatorSet endCleanHide = getHideAnim(mCleanIcon);
        AnimatorSet endCleanShow = getShowAnim(mCleanIcon);
        endCleanShow.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mCleanIcon.setImageResource(R.drawable.clean_done);
                Toast.makeText(SettingActivity.this, "清理完成", Toast.LENGTH_SHORT).show();
                ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCleanIcon.setImageResource(R.drawable.clean_prepare);
                        mClearCache.setEnable(true);
                    }
                }, 1500);
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(rotateAnim, endCleanHide, endCleanShow);
        animatorSet.start();
    }

    private AnimatorSet getHideAnim(View view) {
        AnimatorSet animator = new AnimatorSet();
        ObjectAnimator dissAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.5f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.5f);

        animator.playTogether(dissAnimator, scaleXAnimator, scaleYAnimator);
        animator.setDuration(500);

        return  animator;
    }

    private AnimatorSet getShowAnim(View view) {
        AnimatorSet animator = new AnimatorSet();
        ObjectAnimator dissAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1f);

        animator.playTogether(dissAnimator, scaleXAnimator, scaleYAnimator);
        animator.setDuration(500);

        return  animator;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.exit_login:
                LeoSettings.setString(PrefConst.USER_NAME, "");
                LeoSettings.setString(PrefConst.USER_PHONE, "");
                LeoSettings.setString(PrefConst.USER_PWD, "");
                LeoSettings.setLong(PrefConst.LAST_LOGIN_TIME, 0);
                finish();
                break;
        }
    }

}
