
package com.leo.appmaster.applocker;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.fragment.PretendFragment;
import com.leo.appmaster.utils.LeoLog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class GestureRelative extends RelativeLayout {
    private final static int UnknowCallMode = 2;
    private Paint CirPanint;
    private int CirPointX, CirPointY;
    private int mZhiJing, mBanJing;
    private Context mContext;
    private boolean isFromActivity = true;
    private boolean isFirstRound = false;
    private boolean isSecondRound = false;
    private boolean isThridRound = false;
    private int gua_left, gua_top, gua_right, gua_bottom;
    private int duan_left, duan_top, duan_right, duan_bottom;
    private int jie_left, jie_top, jie_right, jie_bottom;
    private Activity mActivity;
    private PretendFragment mPf;
    private AppMasterPreference sp_unknowcall;

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
        CirPointX = (r - l) / 2;
        CirPointY = (b - t) / 2;
        // LeoLog.d("testlay", "l :"+l+"--t :"+t+"--r :"+r+"--b :"+b);
        mZhiJing = (b - t) * 7 / 8;
        mBanJing = mZhiJing / 2;

        super.onLayout(changed, l, t, r, b);
    }

    private void init(Context context) {
        this.mContext = context;
        CirPanint = new Paint();
        sp_unknowcall = AppMasterPreference.getInstance(mContext);
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
                break;
        }
        return true;
    }

    private void isShowOrder(float x, float y) {
        // 挂断?
        if (x < gua_right && y > gua_top && y < gua_bottom && x > gua_left) {
            // 进入挂断区域，先判断是否从别处进入
            if (!isFirstRound && !isSecondRound && !isThridRound) {
                // 首先进入挂断区域
                isFirstRound = true;
                LeoLog.d("testfuck", "首先进入挂断区域");
            } else {
                if (!isFirstRound) {
                    // 进入了别的区域
                    LeoLog.d("testfuck", "非首次进入挂断区域");
                    isSecondRound = false;
                    isThridRound = false;
                } else if (isSecondRound) {
                    isSecondRound = false;
                }
            }
        }
        // 短信?
        if (x > duan_left && x < duan_right && y < duan_bottom && y > duan_top) {
            if (isFirstRound && !isSecondRound && !isThridRound) {
                isSecondRound = true;
                // 顺利进入第二
                LeoLog.d("testfuck", "顺利进入第二");
            } else {
                if (!isSecondRound) {
                    // 非正确进入第二
                    LeoLog.d("testfuck", "非正确进入第二");
                    isFirstRound = false;
                    isThridRound = false;
                }
            }
        }
        // 接听?
        if (x > jie_left && y > jie_top && y < jie_bottom && x < jie_right) {
            if (isFirstRound && isSecondRound && !isThridRound) {
                isThridRound = true;
                // 顺利进入第三
                LeoLog.d("testfuck", "顺利进入第三");
                // 触发成功
                LeoLog.d("testfuck", "触发成功");
                if (isFromActivity) {
                    Toast.makeText(mContext, mContext.getString(R.string.weizhuang_setting_ok), 0)
                            .show();
                    sp_unknowcall.setPretendLock(UnknowCallMode);
                    mActivity.finish();
                }else {
                    mPf.onUnlockPretendSuccessfully();
                }
            } else {
                if (!isThridRound) {
                    // 非正确进入第三
                    LeoLog.d("testfuck", "非正确进入第三");
                    isFirstRound = false;
                    isSecondRound = false;
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        CirPanint.setStyle(Paint.Style.STROKE);
        CirPanint.setColor(Color.WHITE);
        CirPanint.setAntiAlias(true);
        canvas.drawCircle(CirPointX, CirPointY, mBanJing, CirPanint);
        super.onDraw(canvas);
    }

    public void setGuaPosition(int left, int top, int right, int bottom) {
        gua_left = left;
        gua_top = top;
        gua_right = right;
        gua_bottom = bottom;
        LeoLog.d("testlay",
        "gua_left :"+gua_left+"--gua_top :"+gua_top+"--gua_right :"+gua_right+"--gua_bottom :"+gua_bottom);
    }

    public void setDuanPosition(int left, int top, int right, int bottom) {
        duan_left = left;
        duan_top = top;
        duan_right = right;
        duan_bottom = bottom;
        LeoLog.d("testlay",
        "duan_left :"+duan_left+"--duan_top :"+duan_top+"--duan_right :"+duan_right+"--duan_bottom :"+duan_bottom);
    }

    public void setJiePosition(int left, int top, int right, int bottom) {
        jie_left = left;
        jie_top = top;
        jie_right = right;
        jie_bottom = bottom;
        LeoLog.d("testlay",
        "jie_left :"+jie_left+"--jie_top :"+jie_top+"--jie_right :"+jie_right+"--jie_bottom :"+jie_bottom);
    }

    public void setisFromActivity(boolean isActivity) {
        isFromActivity = isActivity;
    }

    public void setActivity(Activity act) {
        mActivity = act;
    }
    
    public void setPretendFragment(PretendFragment pf) {
        mPf = pf;
    }
}
