package com.zlf.appmaster.setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.zlf.appmaster.R;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.db.LeoSettings;
import com.zlf.appmaster.login.AboutusActivity;
import com.zlf.appmaster.login.ProtocolActivity;
import com.zlf.appmaster.ui.CommonSettingItem;
import com.zlf.appmaster.ui.CommonToolbar;
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

    private CommonSettingItem mAbout;
    private CommonSettingItem mRule;

    private ImageView mCleanIcon;
    private CommonToolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        init();
        setListener();

//        TestView view = new TestView(this);
//        setContentView(view);

//        ArrayList<TestItem> datas = new ArrayList<TestItem>();
//        TestItem pieData = new TestItem("sloop", 60);
//        TestItem pieData2 = new TestItem("sloop", 30);
//        TestItem pieData3 = new TestItem("sloop", 40);
//        TestItem pieData4 = new TestItem("sloop", 20);
//        TestItem pieData5 = new TestItem("sloop", 20);
//        datas.add(pieData);
//        datas.add(pieData2);
//        datas.add(pieData3);
//        datas.add(pieData4);
//        datas.add(pieData5);
//        view.setData(datas);
    }

    private void init() {
        mToolBar = (CommonToolbar) findViewById(R.id.fb_toolbar);
        mToolBar.setToolbarTitle(getResources().getString(R.string.personal_setting));
        mExitLogin = (RippleView) findViewById(R.id.exit_login);
        mHelp = (CommonSettingItem) findViewById(R.id.help);
        mClearCache = (CommonSettingItem) findViewById(R.id.clear_cache);
        mClearCache.setIcon(getResources().getDrawable(R.drawable.clean_prepare));
        mClearCache.setArrowVisable(false);
        mCleanIcon = mClearCache.getIcon();
        mAbout = (CommonSettingItem) findViewById(R.id.about);
        mAbout.setIcon(R.drawable.menu_about_icon);
        mRule = (CommonSettingItem) findViewById(R.id.rule);
        mRule.setIcon(R.drawable.ic_mine_xx);
        mAbout.setTitle(this.getString(R.string.personal_about));
        mRule.setTitle(this.getString(R.string.personal_use));

        mClearCache.setTitle(getResources().getString(R.string.clean_cache));
        mHelp.setTitle(getResources().getString(R.string.help));
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
        mAbout.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, AboutusActivity.class);
                startActivity(intent);
            }
        });
        mRule.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, ProtocolActivity.class);
                startActivity(intent);
            }
        });
    }

    private void startAnim() {
        mClearCache.setEnable(false);
        mCleanIcon.setImageResource(R.drawable.clean_in);
        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(mCleanIcon, "rotation", 0f, 3600f);
        rotateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        rotateAnim.setDuration(3000);

        AnimatorSet endCleanHide = getHideAnim(mCleanIcon);
        AnimatorSet endCleanShow = getShowAnim(mCleanIcon);
        endCleanShow.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mCleanIcon.setImageResource(R.drawable.clean_done);
                Toast.makeText(SettingActivity.this, getResources().getString(
                        R.string.clean_cache_complete), Toast.LENGTH_SHORT).show();
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
                LeoSettings.setString(PrefConst.USER_ROOM, "");
                LeoSettings.setLong(PrefConst.LAST_LOGIN_TIME, 0);
                finish();
                break;
        }
    }

}
