
package com.leo.appmaster.applocker;

import com.leo.appmaster.R;
import com.leo.appmaster.fragment.PretendAppUnknowCallFragment5;
import com.leo.appmaster.fragment.PretendFragment;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class GestureRelative extends RelativeLayout {
    private Paint CirPanint;
    private int CirPointX, CirPointY;
    private int mZhiJing, mBanJing;
    private Context mContext;
    private boolean isFromActivity = true;
    private boolean isFirstRound = false;
    private boolean isSecondRound = false;
    private boolean isThridRound = false;
    private boolean isFlaseControl = false;
    public static boolean isInit = false;
    private int gua_left, gua_top, gua_right, gua_bottom;
    private int duan_left, duan_top, duan_right, duan_bottom;
    private int jie_left, jie_top, jie_right, jie_bottom;
    private UnKnowCallActivity5 mActivity;
    private PretendFragment mPf;
    private PretendAppUnknowCallFragment5 unknowFragment;
    private int screenW;
    private int screenH;

    public boolean mFilterLayout;

    public GestureRelative(Context context) {
        super(context);
        init(context);
    }

    public GestureRelative(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mFilterLayout) {
            mFilterLayout = false;
        } else {
            super.onLayout(changed, l, t, r, b);
        }
        if (screenH >= 1280) {
            CirPointY = (b - t) / 2 + 30;
        } else if (screenH == 800) {
            CirPointY = (b - t) / 2 + 20;
        } else {
            CirPointY = (b - t) / 2 + 10;
        }
        CirPointX = (r - l) / 2;
        // LeoLog.d("testlay", "l :"+l+"--t :"+t+"--r :"+r+"--b :"+b);
        mZhiJing = (b - t) * 7 / 8;
        mBanJing = mZhiJing / 2;
        if (unknowFragment != null) {
            unknowFragment.setPlace();
        }
        LeoLog.d("testGElayout", "喔操走onLayout了");
    }

    private void init(Context context) {
        this.mContext = context;
        CirPanint = new Paint();
        Display mDisplay = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        screenW = mDisplay.getWidth();
        screenH = mDisplay.getHeight();
        LeoLog.d("testGElayout", "喔操走init了");
    }

    public int getPointX() {
        return CirPointX;
    }

    public int getPointY() {
        return CirPointY;
    }

    public int getZhiJing() {
        return mZhiJing;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                isShowOrder(moveX, moveY);
                break;
            case MotionEvent.ACTION_UP:
                isFirstRound = false;
                isSecondRound = false;
                isThridRound = false;
                isFlaseControl = false;
                break;
        }
        return true;
    }

    private void isShowOrder(float x, float y) {
        // 挂断?
        if (x < gua_right && y > gua_top && y < gua_bottom && x > gua_left) {
            // 进入挂断区域，先判断是否从别处进入
            if (!isFirstRound && !isSecondRound && !isThridRound && !isFlaseControl) {
                // 首先进入挂断区域
                isFirstRound = true;
                // LeoLog.d("testfuck", "首先进入挂断区域");
            } else {
                if (!isFirstRound) {
                    // 进入了别的区域
                    // LeoLog.d("testfuck", "非首次进入挂断区域");
                    isFlaseControl = true;
                    isSecondRound = false;
                    isThridRound = false;
                } else if (isSecondRound) {
                    isSecondRound = false;
                    isFlaseControl = true;
                }
            }
        }

        // 短信?
        if (x > duan_left && x < duan_right && y < duan_bottom && y > duan_top) {
            if (isFirstRound && !isSecondRound && !isThridRound && !isFlaseControl) {
                isSecondRound = true;
                // 顺利进入第二
                // LeoLog.d("testfuck", "顺利进入第二");
            } else {
                if (!isSecondRound) {
                    // 非正确进入第二
                    // LeoLog.d("testfuck", "非正确进入第二");
                    isFlaseControl = true;
                    isFirstRound = false;
                    isThridRound = false;
                }
            }
        }

        // 接听?
        if (x > jie_left && y > jie_top && y < jie_bottom && x < jie_right) {
            if (isFirstRound && isSecondRound && !isThridRound && !isFlaseControl) {
                isThridRound = true;
                // 顺利进入第三
                // LeoLog.d("testfuck", "顺利进入第三");
                // 触发成功
                // LeoLog.d("testfuck", "触发成功");
                if (isFromActivity) {
                    mActivity.showAlarmDialog(
                            mContext.getString(R.string.open_weizhuang_dialog_title),
                            mContext.getString(R.string.open_weizhuang_dialog_content),
                            mContext.getString(R.string.open_weizhuang_dialog_sure));
                } else {
                    unknowFragment.setFinishView();
                    unknowFragment.setCanCel();
                    // mPf.onUnlockPretendSuccessfully();
                    SDKWrapper
                            .addEvent(mContext, SDKWrapper.P1, "appcover", "done_UnknowCall");
                }
            } else {
                if (!isThridRound) {
                    // 非正确进入第三
                    // LeoLog.d("testfuck", "非正确进入第三");
                    isFlaseControl = true;
                    isFirstRound = false;
                    isSecondRound = false;
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        CirPanint.setStyle(Paint.Style.STROKE);
        CirPanint.setColor(Color.WHITE);
        CirPanint.setAntiAlias(true);
        canvas.drawCircle(CirPointX, CirPointY, mBanJing, CirPanint);
        LeoLog.d("testGElayout", "喔操走onDraw了");
    }

    public void setGuaPosition(int left, int top, int right, int bottom) {
        gua_left = left;
        gua_top = top;
        gua_right = right;
        gua_bottom = bottom;
    }

    public void setDuanPosition(int left, int top, int right, int bottom) {
        duan_left = left;
        duan_top = top;
        duan_right = right;
        duan_bottom = bottom;
    }

    public void setJiePosition(int left, int top, int right, int bottom) {
        jie_left = left;
        jie_top = top;
        jie_right = right;
        jie_bottom = bottom;
    }

    public void setisFromActivity(boolean isActivity) {
        isFromActivity = isActivity;
    }

    public void setActivity(UnKnowCallActivity5 act) {
        mActivity = act;
    }

    public void setPretendFragment(PretendFragment pf) {
        mPf = pf;
    }

    public void setFragment(PretendAppUnknowCallFragment5 fragment) {
        unknowFragment = fragment;
    }

}
