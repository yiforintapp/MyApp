
package com.leo.appmaster.applocker;

import android.app.Service;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.DipPixelUtil;

public class ZhiWenActivity extends BaseActivity implements OnClickListener {
    private final static int ZHIWENWEIZHUANG = 3;
    private final static int SHOWALPHA = 1;
    private final static int SHOWTRANS = 2;
    private TextView tv_zhiwen_title, tv_zhiwen_jieshao;
    private ImageView iv_zhiwen_tips, zhiwen_bang, show_slowly_iv, iv_zhiwen_click;
    private AppMasterPreference sp_zhiwen_weizhuang;
    private float iv_zhiwen_click_height;
    private Vibrator vib;
    private boolean isClick = false;
    private float lineTranslateY;
    // three click
    private LEOAlarmDialog mAlarmDialog;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            
            switch (msg.what) {
                case SHOWALPHA:
                    showDongHuaAlpha(1f, 0f);
                case SHOWTRANS:
                    showDongHuaTrans(lineTranslateY, 0);
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weizhuang_zhiwen);
        init();
    }

    private void init() {
        sp_zhiwen_weizhuang = AppMasterPreference.getInstance(this);
        tv_zhiwen_title = (TextView) findViewById(R.id.tv_zhiwen_title);
        tv_zhiwen_title.setVisibility(View.VISIBLE);
        tv_zhiwen_jieshao = (TextView) findViewById(R.id.tv_zhiwen_jieshao);
        tv_zhiwen_jieshao.setVisibility(View.VISIBLE);
        iv_zhiwen_tips = (ImageView) findViewById(R.id.iv_zhiwen_tips);
        iv_zhiwen_tips.setVisibility(View.VISIBLE);
        iv_zhiwen_click = (ImageView) findViewById(R.id.iv_zhiwen_click);
        iv_zhiwen_click.setOnClickListener(this);
        show_slowly_iv = (ImageView) findViewById(R.id.show_slowly_iv);
        zhiwen_bang = (ImageView) findViewById(R.id.zhiwen_bang);
        vib = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            iv_zhiwen_click_height = iv_zhiwen_click.getHeight();
            lineTranslateY = iv_zhiwen_click_height - DipPixelUtil.dip2px(this, 14);
            showDongHuaTrans(0, lineTranslateY);
            showDongHuaAlpha(0f, 1f);
        }
        super.onWindowFocusChanged(hasFocus);
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
                    Message ms = new Message();
                    ms.what = SHOWALPHA;
                    handler.sendMessage(ms);
//                    handler.sendEmptyMessage(1);
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
                    Message ms = new Message();
                    ms.what = SHOWTRANS;
                    handler.sendMessage(ms);
                } else {
                    showDongHuaTrans(0, lineTranslateY);
                }
            }
        });
        zhiwen_bang.setAnimation(trans);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_zhiwen_click:
                
                if(!isClick){
                    zhiwen_bang.setVisibility(View.VISIBLE);
                }
                isClick = true;
                break;
            default:
                break;
        }
    }

    private void showAlarmDialog(String title, String content, String sureText) {
        if (mAlarmDialog == null) {
            mAlarmDialog = new LEOAlarmDialog(this);
            mAlarmDialog.setOnClickListener(new OnDiaogClickListener() {
                @Override
                public void onClick(int which) {
                    // ok
                    if (which == 1) {
                        makeText();
                        sp_zhiwen_weizhuang.setPretendLock(ZHIWENWEIZHUANG);
                        vib.vibrate(150);
                        ZhiWenActivity.this.finish();
                    }

                }
            });
        }
        mAlarmDialog.setSureButtonText(sureText);
        mAlarmDialog.setTitle(title);
        mAlarmDialog.setContent(content);
        mAlarmDialog.show();
    }

    protected void makeText() {
        Toast.makeText(this, getString(R.string.zhiwen_mode_ok), 0).show();
    }

    @Override
    protected void onDestroy() {
        if (mAlarmDialog != null) {
            mAlarmDialog.dismiss();
            mAlarmDialog = null;
        }
        super.onDestroy();
    }

}