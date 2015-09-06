
package com.leo.appmaster.applocker;

import java.util.Timer;
import java.util.TimerTask;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Service;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CirCleDongHua;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;

public class UnKnowCallActivity5 extends BaseActivity implements OnTouchListener {
    private final static int UnknowCallMode = 2;
    private final static int SHOWGUABIG = 3;
    private final static int SHOWDUANBIG = 4;
    private final static int SHOWJIEBIG = 5;
    private final static int SHOWNORMAL = 6;
    private TextView tv_use_tips, tv_use_tips_content;
    private ImageView iv_dianhua_hold, iv_guaduan, iv_duanxin, iv_jieting, iv_guaduan_big,
            iv_duanxin_big, iv_jieting_big, iv_hands, iv_topguide;
    private GestureRelative mViewContent;
    private CirCleDongHua myself_circle;
    private boolean isShowing = false;
    private boolean isLunXun = false;
    private int hold_width, hold_height, hold_left, hold_top, hold_right, hold_bottom;
    private float mYuanX, mYuanY, mZhiJing, mBanJing;
    private int startX, startY;
    private float lc_left, lc_top, lc_right, lc_bottom;
    private int gua_yuan_x, gua_yuan_y, gua_left, gua_top, gua_right, gua_bottom;
    private int duan_yuan_x, duan_yuan_y, duan_left, duan_top, duan_right, duan_bottom;
    private int top_guide_left, top_guide_top, top_guide_right, top_guide_bottom;
    private int jie_yuan_x, jie_yuan_y, jie_left, jie_top, jie_right, jie_bottom;
    private int gua_left_big, gua_top_big, gua_right_big, gua_bottom_big;
    private int duan_left_big, duan_top_big, duan_right_big, duan_bottom_big;
    private int jie_left_big, jie_top_big, jie_right_big, jie_bottom_big;
    private int hand_x, hand_y, hand_left, hand_top, hand_right, hand_bottom;
    private LEOAlarmDialog mAlarmDialog;
    private AppMasterPreference sp_unknowcall;
    private Vibrator vib;
    private Timer mTimer;

    private ObjectAnimator mMoveX;
    private ObjectAnimator mMoveY;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    iv_dianhua_hold.layout(hold_left, hold_top, hold_right, hold_bottom);
                    LeoLog.d("testGElayout", "电话柄左：" + hold_left + "右：" + hold_right);

                    iv_guaduan.layout(gua_left, gua_top, gua_right, gua_bottom);
                    iv_guaduan_big.layout(gua_left_big, gua_top_big, gua_right_big, gua_bottom_big);

                    iv_duanxin.layout(duan_left, duan_top, duan_right, duan_bottom);
                    iv_duanxin_big.layout(duan_left_big, duan_top_big, duan_right_big,
                            duan_bottom_big);

                    iv_jieting.layout(jie_left, jie_top, jie_right, jie_bottom);
                    iv_jieting_big.layout(jie_left_big, jie_top_big, jie_right_big, jie_bottom_big);

                    iv_topguide.layout(top_guide_left, top_guide_top, top_guide_right,
                            top_guide_bottom);

                    iv_dianhua_hold.setVisibility(View.VISIBLE);
                    iv_guaduan.setVisibility(View.VISIBLE);
                    iv_duanxin.setVisibility(View.VISIBLE);
                    iv_jieting.setVisibility(View.VISIBLE);
                    iv_topguide.setVisibility(View.VISIBLE);

                    mViewContent.mFilterLayout = true;

                    if (!isShowing) {
                        iv_hands.setVisibility(View.VISIBLE);
                        iv_hands.layout(hand_left, hand_top, hand_right, hand_bottom);
                        showTransDonghua();
                    }
                    break;
                case SHOWGUABIG:
                    iv_guaduan_big.setVisibility(View.VISIBLE);
                    iv_guaduan.setVisibility(View.INVISIBLE);
                    iv_duanxin_big.setVisibility(View.INVISIBLE);
                    iv_duanxin.setVisibility(View.VISIBLE);
                    iv_jieting_big.setVisibility(View.INVISIBLE);
                    iv_jieting.setVisibility(View.VISIBLE);
                    break;
                case SHOWDUANBIG:
                    iv_guaduan_big.setVisibility(View.INVISIBLE);
                    iv_guaduan.setVisibility(View.VISIBLE);
                    iv_duanxin_big.setVisibility(View.VISIBLE);
                    iv_duanxin.setVisibility(View.INVISIBLE);
                    iv_jieting_big.setVisibility(View.INVISIBLE);
                    iv_jieting.setVisibility(View.VISIBLE);
                    break;
                case SHOWJIEBIG:
                    iv_guaduan_big.setVisibility(View.INVISIBLE);
                    iv_guaduan.setVisibility(View.VISIBLE);
                    iv_duanxin_big.setVisibility(View.INVISIBLE);
                    iv_duanxin.setVisibility(View.VISIBLE);
                    iv_jieting_big.setVisibility(View.VISIBLE);
                    iv_jieting.setVisibility(View.INVISIBLE);
                    break;
                case SHOWNORMAL:
                    iv_guaduan_big.setVisibility(View.INVISIBLE);
                    iv_guaduan.setVisibility(View.VISIBLE);
                    iv_duanxin_big.setVisibility(View.INVISIBLE);
                    iv_duanxin.setVisibility(View.VISIBLE);
                    iv_jieting_big.setVisibility(View.INVISIBLE);
                    iv_jieting.setVisibility(View.VISIBLE);
                    break;
            }
        };
    };

    @Override
    protected void onStop() {
        iv_dianhua_hold.setVisibility(View.INVISIBLE);
        iv_guaduan.setVisibility(View.INVISIBLE);
        iv_duanxin.setVisibility(View.INVISIBLE);
        iv_jieting.setVisibility(View.INVISIBLE);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unknowcall_five);
        init();
    }

    protected void showTransDonghua() {
        isShowing = true;
        LeoLog.d("testshowwick", "showTransDonghua");
        if (!isLunXun) {
            setTimerTask();
        }
        if (mMoveX == null) {
            mMoveX = ObjectAnimator.ofFloat(iv_hands, "translationX", 0, mBanJing, mBanJing,
                    mZhiJing);
            mMoveX.setDuration(2500);
        }

        if (mMoveY == null) {
            mMoveY = ObjectAnimator.ofFloat(iv_hands, "translationY", 0, -mBanJing, -mBanJing, 0);
            mMoveY.setDuration(2500);
        }

        mMoveX.setRepeatCount(ObjectAnimator.INFINITE);
        mMoveY.setRepeatCount(ObjectAnimator.INFINITE);
        mMoveX.start();
        // mMoveY.addListener(new AnimatorListenerAdapter() {
        // @Override
        // public void onAnimationEnd(Animator animation) {
        // showTransDonghua();
        // }
        // });
        mMoveY.start();
    }

    private void setTimerTask() {
        try {
            if (mTimer != null) {
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        isLunXun = true;
                        LeoLog.d("testunknow",
                                "hand_x : " + iv_hands.getX() + " ; hand_y : " + iv_hands.getY());
                        int handX = (int) iv_hands.getX();
                        int handY = (int) iv_hands.getY();
                        int handWidth = iv_hands.getWidth();
                        int handHeight = iv_hands.getHeight();
                        int handLeft = handX - handWidth / 2;
                        int handTop = handY - handHeight / 2;
                        if (handLeft < gua_right + 10 && handTop < gua_top - 10) {
                            Message message = new Message();
                            message.what = SHOWGUABIG;
                            handler.sendMessage(message);
                        } else if (handTop < duan_bottom + 10 && handLeft > duan_left - 10) {
                            Message message = new Message();
                            message.what = SHOWDUANBIG;
                            handler.sendMessage(message);
                        } else if (handLeft > jie_left - 20) {
                            Message message = new Message();
                            message.what = SHOWJIEBIG;
                            handler.sendMessage(message);
                        } else {
                            Message message = new Message();
                            message.what = SHOWNORMAL;
                            handler.sendMessage(message);
                        }
                    }
                }, 100, 100);
            }
        } catch (Exception e) {

        }
    }

    private void init() {
        sp_unknowcall = AppMasterPreference.getInstance(this);
        vib = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);

        tv_use_tips = (TextView) findViewById(R.id.tv_use_tips);
        tv_use_tips.setVisibility(View.VISIBLE);
        tv_use_tips_content = (TextView) findViewById(R.id.tv_use_tips_content);
        tv_use_tips_content.setVisibility(View.VISIBLE);

        mViewContent = (GestureRelative) findViewById(R.id.mid_view);
        mViewContent.setisFromActivity(true);
        mViewContent.setActivity(this);
        iv_dianhua_hold = (ImageView) findViewById(R.id.iv_dianhua_hold);
        iv_dianhua_hold.setOnTouchListener(this);

        iv_guaduan = (ImageView) findViewById(R.id.iv_guaduan);
        iv_duanxin = (ImageView) findViewById(R.id.iv_duanxin);
        iv_jieting = (ImageView) findViewById(R.id.iv_jieting);

        iv_guaduan_big = (ImageView) findViewById(R.id.iv_guaduan_big);
        iv_duanxin_big = (ImageView) findViewById(R.id.iv_duanxin_big);
        iv_jieting_big = (ImageView) findViewById(R.id.iv_jieting_big);

        iv_hands = (ImageView) findViewById(R.id.iv_hands);
        iv_topguide = (ImageView) findViewById(R.id.iv_topguide);
        mTimer = new Timer();

        iv_hands.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });
        myself_circle = (CirCleDongHua) findViewById(R.id.myself_circle);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {

            hold_width = iv_dianhua_hold.getWidth();
            hold_height = iv_dianhua_hold.getHeight();

            // 圆心在？
            mYuanX = mViewContent.getPointX();
            mYuanY = mViewContent.getPointY();
            myself_circle.setYuan(mYuanX, mYuanY);

            // 直径是？
            mZhiJing = mViewContent.getZhiJing();
            mBanJing = mZhiJing / 2;

            hold_left = (int) (mYuanX - (hold_width / 2));
            hold_top = (int) (mYuanY - (hold_height / 2));
            hold_right = (int) (mYuanX + (hold_width / 2));
            hold_bottom = (int) (mYuanY + (hold_height / 2));

            // 挂断，短信，接听icon位置
            setPosition();
            // 电话柄位于？
            setHold();
        }
        super.onWindowFocusChanged(hasFocus);
    }

    private void setPosition() {
        // 挂断
        gua_yuan_x = (int) (mYuanX - mBanJing);
        gua_yuan_y = (int) mYuanY;
        int gua_width = iv_guaduan.getWidth();
        int gua_height = iv_guaduan.getHeight();
        gua_left = gua_yuan_x - (gua_width / 2);
        gua_top = gua_yuan_y - (gua_height / 2);
        gua_right = gua_yuan_x + (gua_width / 2);
        gua_bottom = gua_yuan_y + (gua_height / 2);
        mViewContent.setGuaPosition(gua_left, gua_top, gua_right, gua_bottom);
        // big
        int gua_big_width = iv_guaduan_big.getWidth();
        int gua_big_height = iv_guaduan_big.getHeight();
        gua_left_big = gua_yuan_x - (gua_big_width / 2);
        gua_top_big = gua_yuan_y - (gua_big_height / 2);
        gua_right_big = gua_yuan_x + (gua_big_width / 2);
        gua_bottom_big = gua_yuan_y + (gua_big_height / 2);

        // 短信
        duan_yuan_x = (int) mYuanX;
        duan_yuan_y = (int) (mYuanY - mBanJing);
        int duan_width = iv_duanxin.getWidth();
        int duan_height = iv_duanxin.getHeight();
        duan_left = duan_yuan_x - (duan_width / 2);
        duan_top = duan_yuan_y - (duan_height / 2);
        duan_right = duan_yuan_x + (duan_width / 2);
        duan_bottom = duan_yuan_y + (duan_height / 2);
        mViewContent.setDuanPosition(duan_left, duan_top, duan_right, duan_bottom);
        // big
        int duan_big_width = iv_duanxin_big.getWidth();
        int duan_big_height = iv_duanxin_big.getHeight();
        duan_left_big = duan_yuan_x - (duan_big_width / 2);
        duan_top_big = duan_yuan_y - (duan_big_height / 2);
        duan_right_big = duan_yuan_x + (duan_big_width / 2);
        duan_bottom_big = duan_yuan_y + (duan_big_height / 2);

        // 接听
        jie_yuan_x = (int) (mYuanX + mBanJing);
        jie_yuan_y = (int) mYuanY;
        int jie_width = iv_guaduan.getWidth();
        int jie_height = iv_guaduan.getHeight();
        jie_left = jie_yuan_x - (jie_width / 2);
        jie_top = jie_yuan_y - (jie_height / 2);
        jie_right = jie_yuan_x + (jie_width / 2);
        jie_bottom = jie_yuan_y + (jie_height / 2);
        mViewContent.setJiePosition(jie_left, jie_top, jie_right, jie_bottom);
        // big
        int jie_big_width = iv_guaduan_big.getWidth();
        int jie_big_height = iv_guaduan_big.getHeight();
        jie_left_big = jie_yuan_x - (jie_big_width / 2);
        jie_top_big = jie_yuan_y - (jie_big_height / 2);
        jie_right_big = jie_yuan_x + (jie_big_width / 2);
        jie_bottom_big = jie_yuan_y + (jie_big_height / 2);

        int hand_width = iv_hands.getWidth();
        int hand_height = iv_hands.getHeight();
        hand_x = gua_yuan_x + hand_width / 2;
        hand_y = gua_yuan_y + hand_height / 2;
        hand_left = hand_x - (hand_width / 2);
        hand_top = hand_y - (hand_height / 2);
        hand_right = hand_x + (hand_width / 2);
        hand_bottom = hand_y + (hand_height / 2);

        // top guide
        top_guide_left = (int) (mYuanX - mBanJing);
        top_guide_top = (int) (mYuanY - mBanJing) - 4;
        top_guide_right = (int) (mYuanX + mBanJing) + DipPixelUtil.dip2px(this, 5);
        top_guide_bottom = (int) (mYuanY);
    }

    public void setHold() {
        handler.sendEmptyMessageDelayed(1, 150);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.iv_dianhua_hold) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    myself_circle.setVisibility(View.INVISIBLE);
                    startX = (int) event.getRawX();
                    startY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int newX = (int) event.getRawX() - startX;
                    int newY = (int) event.getRawY() - startY;

                    lc_left = iv_dianhua_hold.getLeft();
                    lc_top = iv_dianhua_hold.getTop();
                    lc_right = iv_dianhua_hold.getRight();
                    lc_bottom = iv_dianhua_hold.getBottom();

                    int top = (int) (lc_top + newY);
                    int left = (int) (lc_left + newX);
                    int bottom = (int) (lc_bottom + newY);
                    int right = (int) (lc_right + newX);

                    if (left < gua_left) {
                        left = gua_left;
                        right = left + iv_dianhua_hold.getWidth();
                    }

                    if (top < duan_top) {
                        top = duan_top;
                        bottom = top + iv_dianhua_hold.getHeight();
                    }

                    if (right > jie_right) {
                        right = jie_right;
                        left = right - iv_dianhua_hold.getWidth();
                    }

                    if (bottom > duan_yuan_y + mZhiJing) {
                        bottom = (int) (duan_yuan_y + mZhiJing);
                        top = bottom - iv_dianhua_hold.getHeight();
                    }

                    v.layout(left, top, right, bottom);
                    startX = (int) event.getRawX();
                    startY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    v.layout(hold_left, hold_top, hold_right, hold_bottom);

                    myself_circle.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    protected void makeText() {
        Toast.makeText(this, getString(R.string.weizhuang_setting_ok), Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    protected void onDestroy() {
        if (mAlarmDialog != null) {
            mAlarmDialog.dismiss();
            mAlarmDialog = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }

        // 解决内存泄露
        if (mMoveX != null) {
            mMoveX.end();
        }
        if (mMoveY != null) {
            mMoveY.end();
        }
        mViewContent.mFilterLayout = false;
        super.onDestroy();
    }

    public void showAlarmDialog(String title, String content, String sureText) {
        if (mAlarmDialog == null) {
            mAlarmDialog = new LEOAlarmDialog(this);
            mAlarmDialog.setOnClickListener(new OnDiaogClickListener() {
                @Override
                public void onClick(int which) {
                    // ok
                    if (which == 1) {
                        makeText();
                        sp_unknowcall.setPretendLock(UnknowCallMode);
                        vib.vibrate(150);
                        UnKnowCallActivity5.this.finish();
                    }
                }
            });
        }
        mAlarmDialog.setSureButtonText(sureText);
        mAlarmDialog.setTitle(title);
        mAlarmDialog.setContent(content);
        mAlarmDialog.show();
    }

    protected void allTurnSmall() {
        iv_guaduan_big.setVisibility(View.INVISIBLE);
        iv_guaduan.setVisibility(View.VISIBLE);
        iv_duanxin_big.setVisibility(View.INVISIBLE);
        iv_duanxin.setVisibility(View.VISIBLE);
        iv_jieting_big.setVisibility(View.INVISIBLE);
        iv_jieting.setVisibility(View.VISIBLE);
    }

    public void guaTurnBig() {
        iv_guaduan_big.setVisibility(View.VISIBLE);
        iv_guaduan.setVisibility(View.INVISIBLE);
    }

    public void duanTurnBig() {
        iv_duanxin.setVisibility(View.INVISIBLE);
        iv_duanxin_big.setVisibility(View.VISIBLE);
    }

    public void jieTurnBig() {
        iv_jieting_big.setVisibility(View.VISIBLE);
        iv_jieting.setVisibility(View.INVISIBLE);
    }

    public void guaTurnSmall() {
        iv_guaduan_big.setVisibility(View.INVISIBLE);
        iv_guaduan.setVisibility(View.VISIBLE);
    }

    public void duanTurnSmall() {
        iv_duanxin_big.setVisibility(View.INVISIBLE);
        iv_duanxin.setVisibility(View.VISIBLE);
    }

    public void jieTurnSmall() {
        iv_jieting_big.setVisibility(View.INVISIBLE);
        iv_jieting.setVisibility(View.VISIBLE);
    }

    public void hideHands() {
        iv_hands.setVisibility(View.INVISIBLE);
        mTimer.cancel();
    }

}
