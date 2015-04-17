
package com.leo.appmaster.fragment;

import android.database.ContentObservable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

import com.leo.appmaster.PhoneInfo;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.GestureRelative;
import com.leo.appmaster.utils.LeoLog;

public class PretendAppUnknowCallFragment5 extends PretendFragment implements OnTouchListener {
    private ImageView iv_dianhua_hold, iv_guaduan, iv_duanxin, iv_jieting, iv_guaduan_big,
            iv_duanxin_big, iv_jieting_big;
    private View activity_weizhuang_firstin;
    private float mYuanX, mYuanY, mZhiJing, mBanJing;
    private int hold_width, hold_height, hold_left, hold_top, hold_right, hold_bottom;
    private int gua_yuan_x, gua_yuan_y, gua_left, gua_top, gua_right, gua_bottom;
    private int duan_yuan_x, duan_yuan_y, duan_left, duan_top, duan_right, duan_bottom;
    private int jie_yuan_x, jie_yuan_y, jie_left, jie_top, jie_right, jie_bottom;
    private int gua_left_big, gua_top_big, gua_right_big, gua_bottom_big;
    private int duan_left_big, duan_top_big, duan_right_big, duan_bottom_big;
    private int jie_left_big, jie_top_big, jie_right_big, jie_bottom_big;
    private GestureRelative mViewContent;
    private int mVersion;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    iv_dianhua_hold.layout(hold_left, hold_top, hold_right, hold_bottom);

                    iv_guaduan.layout(gua_left, gua_top, gua_right, gua_bottom);
                    iv_guaduan_big.layout(gua_left_big, gua_top_big, gua_right_big, gua_bottom_big);

                    iv_duanxin.layout(duan_left, duan_top, duan_right, duan_bottom);
                    iv_duanxin_big.layout(duan_left_big, duan_top_big, duan_right_big,
                            duan_bottom_big);

                    iv_jieting.layout(jie_left, jie_top, jie_right, jie_bottom);
                    iv_jieting_big.layout(jie_left_big, jie_top_big, jie_right_big, jie_bottom_big);

                    iv_dianhua_hold.setVisibility(View.VISIBLE);
                    iv_guaduan.setVisibility(View.VISIBLE);
                    iv_duanxin.setVisibility(View.VISIBLE);
                    iv_jieting.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        };
    };

    @Override
    protected int layoutResourceId() {
        return R.layout.activity_unknowcall_five;
    }

    @Override
    protected void onInitUI() {
        activity_weizhuang_firstin = findViewById(R.id.activity_weizhuang_firstin);
        // make content match the screen
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        Window window = mActivity.getWindow();
        LayoutParams windowLayoutParams = window.getAttributes(); // 获取对话框当前的参数值
        windowLayoutParams.width = (int) (display.getWidth());
        windowLayoutParams.height = (int) (display.getHeight());
        activity_weizhuang_firstin.setLayoutParams(windowLayoutParams);

        mVersion = PhoneInfo.getAndroidVersion();
        init();
        GlobalLayoutListener();
    }

    private void init() {
        mViewContent = (GestureRelative) findViewById(R.id.mid_view);
        mViewContent.setisFromActivity(false);
        mViewContent.setPretendFragment(this);
        iv_dianhua_hold = (ImageView) findViewById(R.id.iv_dianhua_hold);
        iv_dianhua_hold.setOnTouchListener(this);

        iv_guaduan = (ImageView) findViewById(R.id.iv_guaduan);
        iv_duanxin = (ImageView) findViewById(R.id.iv_duanxin);
        iv_jieting = (ImageView) findViewById(R.id.iv_jieting);
        iv_guaduan_big = (ImageView) findViewById(R.id.iv_guaduan_big);
        iv_duanxin_big = (ImageView) findViewById(R.id.iv_duanxin_big);
        iv_jieting_big = (ImageView) findViewById(R.id.iv_jieting_big);
    }

    private void setPlace() {
        new Thread() {
            public void run() {
                try {
                    sleep(250);
                    mHandler.sendEmptyMessage(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        }.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LeoLog.d("testnewcall", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        return false;
    }

    private void GlobalLayoutListener() {
        // ViewContent
        ViewTreeObserver contentObserver = mViewContent.getViewTreeObserver();
        contentObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                LeoLog.d("testnewcall", "sesesesesesesese");
                // 圆心在？
                mYuanX = mViewContent.getPointX();
                mYuanY = mViewContent.getPointY();
                // LeoLog.d("testnewcall", "mYuanX : " + mYuanX + "mYuanY : " +
                // mYuanY);
                // 直径是？
                mZhiJing = mViewContent.getZhiJing();
                mBanJing = mZhiJing / 2;
                // LeoLog.d("testnewcall", "mZhiJing : " + mZhiJing +
                // "mBanJing " + mBanJing);

                // hold
                hold_width = iv_dianhua_hold.getWidth();
                hold_height = iv_dianhua_hold.getHeight();
                hold_left = (int) (mYuanX - (hold_width / 2));
                hold_top = (int) (mYuanY - (hold_height / 2));
                hold_right = (int) (mYuanX + (hold_width / 2));
                hold_bottom = (int) (mYuanY + (hold_height / 2));
                LeoLog.d("testnewcall", "hold_left is : " + hold_left
                        + "--hold_top is : " + hold_top + "--hold_right is : "
                        + hold_right + "hold_bottom is : " + hold_bottom);

                // gua
                gua_yuan_x = (int) (mYuanX - mBanJing);
                gua_yuan_y = (int) mYuanY;
                int gua_width = iv_guaduan.getWidth();
                int gua_height = iv_guaduan.getHeight();
                gua_left = gua_yuan_x - (gua_width / 2);
                gua_top = gua_yuan_y - (gua_height / 2);
                gua_right = gua_yuan_x + (gua_width / 2);
                gua_bottom = gua_yuan_y + (gua_height / 2);
                // LeoLog.d("testnewcall", "gua_left is : " + gua_left
                // + "--gua_top is : " + gua_top + "--gua_right is : "
                // + gua_right + "gua_bottom is : " + gua_bottom);
                mViewContent.setGuaPosition(gua_left, gua_top, gua_right, gua_bottom);

                // gua_big
                int gua_big_width = iv_guaduan_big.getWidth();
                int gua_big_height = iv_guaduan_big.getHeight();
                gua_left_big = gua_yuan_x - (gua_big_width / 2);
                gua_top_big = gua_yuan_y - (gua_big_height / 2);
                gua_right_big = gua_yuan_x + (gua_big_width / 2);
                gua_bottom_big = gua_yuan_y + (gua_big_height / 2);
                // LeoLog.d("testnewcall", "gua_left_big is : " + gua_left_big
                // + "--gua_top_big is : " + gua_top_big +
                // "--gua_right_big is : "
                // + gua_right_big + "gua_bottom_big is : " + gua_bottom_big);

                // duan
                duan_yuan_x = (int) mYuanX;
                duan_yuan_y = (int) (mYuanY - mBanJing);
                int duan_width = iv_duanxin.getWidth();
                int duan_height = iv_duanxin.getHeight();
                duan_left = duan_yuan_x - (duan_width / 2);
                duan_top = duan_yuan_y - (duan_height / 2);
                duan_right = duan_yuan_x + (duan_width / 2);
                duan_bottom = duan_yuan_y + (duan_height / 2);
                // LeoLog.d("testnewcall", "duan_left is : " + duan_left
                // + "--duan_top is : " + duan_top + "--duan_right is : "
                // + duan_right + "duan_bottom is : " + duan_bottom);
                mViewContent.setDuanPosition(duan_left, duan_top, duan_right, duan_bottom);

                // duan_big
                int duan_big_width = iv_duanxin_big.getWidth();
                int duan_big_height = iv_duanxin_big.getHeight();
                duan_left_big = duan_yuan_x - (duan_big_width / 2);
                duan_top_big = duan_yuan_y - (duan_big_height / 2);
                duan_right_big = duan_yuan_x + (duan_big_width / 2);
                duan_bottom_big = duan_yuan_y + (duan_big_height / 2);
                // LeoLog.d("testnewcall", "duan_left_big is : " + duan_left_big
                // + "--duan_top_big is : " + duan_top_big +
                // "--duan_right_big is : "
                // + duan_right_big + "duan_bottom_big is : " +
                // duan_bottom_big);

                // jie
                jie_yuan_x = (int) (mYuanX + mBanJing);
                jie_yuan_y = (int) mYuanY;
                int jie_width = iv_guaduan.getWidth();
                int jie_height = iv_guaduan.getHeight();
                jie_left = jie_yuan_x - (jie_width / 2);
                jie_top = jie_yuan_y - (jie_height / 2);
                jie_right = jie_yuan_x + (jie_width / 2);
                jie_bottom = jie_yuan_y + (jie_height / 2);
                mViewContent.setJiePosition(jie_left, jie_top, jie_right, jie_bottom);

                // jie_big
                int jie_big_width = iv_guaduan_big.getWidth();
                int jie_big_height = iv_guaduan_big.getHeight();
                jie_left_big = jie_yuan_x - (jie_big_width / 2);
                jie_top_big = jie_yuan_y - (jie_big_height / 2);
                jie_right_big = jie_yuan_x + (jie_big_width / 2);
                jie_bottom_big = jie_yuan_y + (jie_big_height / 2);

//                setPlace();
                if (mVersion > 16) {
                    mViewContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mViewContent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        // dianhua_hold
        ViewTreeObserver holdObserver = iv_dianhua_hold.getViewTreeObserver();
        holdObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
//                hold_width = iv_dianhua_hold.getWidth();
//                hold_height = iv_dianhua_hold.getHeight();
//                hold_left = (int) (mYuanX - (hold_width / 2));
//                hold_top = (int) (mYuanY - (hold_height / 2));
//                hold_right = (int) (mYuanX + (hold_width / 2));
//                hold_bottom = (int) (mYuanY + (hold_height / 2));
//                LeoLog.d("testnewcall", "hold_left is : " + hold_left
//                        + "--hold_top is : " + hold_top + "--hold_right is : "
//                        + hold_right + "hold_bottom is : " + hold_bottom);
                if (mVersion > 16) {
                    iv_dianhua_hold.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    iv_dianhua_hold.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

//        ViewTreeObserver ContentObservable = activity_weizhuang_firstin.getViewTreeObserver();
//        ContentObservable.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
//            public void onGlobalLayout() {
//                iv_guaduan.layout(gua_left, gua_top, gua_right, gua_bottom);
//                iv_guaduan_big.layout(gua_left_big, gua_top_big, gua_right_big, gua_bottom_big);
//                iv_guaduan.setVisibility(View.VISIBLE);
//                LeoLog.d("testnewcall", "bigbigbigbigbigbigbig");
//                if (mVersion > 16) {
//                    activity_weizhuang_firstin.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                } else {
//                    activity_weizhuang_firstin.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                }
//            }
//        });
        
        // guaduan
        ViewTreeObserver guaObserver = iv_guaduan.getViewTreeObserver();
        guaObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                LeoLog.d("testnewcall", "thththththththth");
//                gua_yuan_x = (int) (mYuanX - mBanJing);
//                gua_yuan_y = (int) mYuanY;
//                int gua_width = iv_guaduan.getWidth();
//                int gua_height = iv_guaduan.getHeight();
//                gua_left = gua_yuan_x - (gua_width / 2);
//                gua_top = gua_yuan_y - (gua_height / 2);
//                gua_right = gua_yuan_x + (gua_width / 2);
//                gua_bottom = gua_yuan_y + (gua_height / 2);
//                mViewContent.setGuaPosition(gua_left, gua_top, gua_right,
//                        gua_bottom);
                setPlace();
                if (mVersion > 16) {
                    iv_guaduan.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    iv_guaduan.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        // guaduan_big
        ViewTreeObserver guaBigObserver = iv_guaduan_big.getViewTreeObserver();
        guaBigObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener()
        {
            public void onGlobalLayout() {
//                int gua_big_width = iv_guaduan_big.getWidth();
//                int gua_big_height = iv_guaduan_big.getHeight();
//                gua_left_big = gua_yuan_x - (gua_big_width / 2);
//                gua_top_big = gua_yuan_y - (gua_big_height / 2);
//                gua_right_big = gua_yuan_x + (gua_big_width / 2);
//                gua_bottom_big = gua_yuan_y + (gua_big_height / 2);
                if (mVersion > 16) {
                    iv_guaduan_big.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    iv_guaduan_big.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        // duanxin
        ViewTreeObserver duanObserver = iv_duanxin.getViewTreeObserver();
        duanObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
//                duan_yuan_x = (int) mYuanX;
//                duan_yuan_y = (int) (mYuanY - mBanJing);
//                int duan_width = iv_duanxin.getWidth();
//                int duan_height = iv_duanxin.getHeight();
//                duan_left = duan_yuan_x - (duan_width / 2);
//                duan_top = duan_yuan_y - (duan_height / 2);
//                duan_right = duan_yuan_x + (duan_width / 2);
//                duan_bottom = duan_yuan_y + (duan_height / 2);
//                mViewContent.setDuanPosition(duan_left, duan_top, duan_right,
//                        duan_bottom);
                if (mVersion > 16) {
                    iv_duanxin.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    iv_duanxin.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        // duanxin_big
        ViewTreeObserver duanBigObserver = iv_duanxin_big.getViewTreeObserver();
        duanBigObserver.addOnGlobalLayoutListener(new
                OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
//                        int duan_big_width = iv_duanxin_big.getWidth();
//                        int duan_big_height = iv_duanxin_big.getHeight();
//                        duan_left_big = duan_yuan_x - (duan_big_width / 2);
//                        duan_top_big = duan_yuan_y - (duan_big_height / 2);
//                        duan_right_big = duan_yuan_x + (duan_big_width / 2);
//                        duan_bottom_big = duan_yuan_y + (duan_big_height / 2);
                        if (mVersion > 16) {
                            iv_duanxin_big.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            iv_duanxin_big.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                    }
                });

        // jieting
        ViewTreeObserver jieObserver = iv_jieting.getViewTreeObserver();
        jieObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
//                jie_yuan_x = (int) (mYuanX + mBanJing);
//                jie_yuan_y = (int) mYuanY;
//                int jie_width = iv_guaduan.getWidth();
//                int jie_height = iv_guaduan.getHeight();
//                jie_left = jie_yuan_x - (jie_width / 2);
//                jie_top = jie_yuan_y - (jie_height / 2);
//                jie_right = jie_yuan_x + (jie_width / 2);
//                jie_bottom = jie_yuan_y + (jie_height / 2);
//                mViewContent.setJiePosition(jie_left, jie_top, jie_right,
//                        jie_bottom);
                if (mVersion > 16) {
                    iv_jieting.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    iv_jieting.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        ViewTreeObserver jieBigObserver = iv_jieting_big.getViewTreeObserver();
        jieBigObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener()
        {
            public void onGlobalLayout() {
//                int jie_big_width = iv_guaduan_big.getWidth();
//                int jie_big_height = iv_guaduan_big.getHeight();
//                jie_left_big = jie_yuan_x - (jie_big_width / 2);
//                jie_top_big = jie_yuan_y - (jie_big_height / 2);
//                jie_right_big = jie_yuan_x + (jie_big_width / 2);
//                jie_bottom_big = jie_yuan_y + (jie_big_height / 2);
                if (mVersion > 16) {
                    iv_jieting_big.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    iv_jieting_big.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

    }
}
