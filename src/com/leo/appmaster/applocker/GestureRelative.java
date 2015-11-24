
package com.leo.appmaster.applocker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.leo.appmaster.R;
import com.leo.appmaster.fragment.PretendAppUnknowCallFragment5;
import com.leo.appmaster.fragment.PretendFragment;
import com.leo.appmaster.sdk.SDKWrapper;

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
    private boolean isControlGua = false;
    private boolean isControlDuan = false;
    private boolean isControlJie = false;
    private int gua_left, gua_top, gua_right, gua_bottom;
    private int duan_left, duan_top, duan_right, duan_bottom;
    private int jie_left, jie_top, jie_right, jie_bottom;
    private UnKnowCallActivity5 mActivity;
    private PretendAppUnknowCallFragment5 unknowFragment;
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
//             mFilterLayout = false;
        } else {
            super.onLayout(changed, l, t, r, b);
        }
        if (screenH >= 1280) {
            CirPointY = (b - t) / 2 + 30;
        } else if (screenH == 800) {
            CirPointY = (b - t) / 2 + 20;
        } else if (screenH == 320) {
            CirPointY = (b - t) / 2 + 5;
        } else {
            CirPointY = (b - t) / 2 + 10;
        }
        CirPointX = (r - l) / 2;
        // LeoLog.d("testlay", "l :"+l+"--t :"+t+"--r :"+r+"--b :"+b);
        mZhiJing = (b - t) * 7 / 8;
        mBanJing = mZhiJing / 2;
        if (unknowFragment != null && !mFilterLayout) {
            unknowFragment.setPlace();
        }

    }

    private void init(Context context) {
        this.mContext = context;
        CirPanint = new Paint();
        Display mDisplay = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        screenH = mDisplay.getHeight();
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
                if (isFromActivity) {
                    mActivity.hideHands();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                isShowOrder(moveX, moveY);
                break;
            case MotionEvent.ACTION_UP:
                if (isFromActivity) {
                    mActivity.allTurnSmall();
                } else {
                    unknowFragment.allTurnSmall();
                }
                isFirstRound = false;
                isSecondRound = false;
                isThridRound = false;
                isFlaseControl = false;
                break;
        }
        return true;
    }

    private void isShowOrder(float x, float y) {
        int gua_width = gua_right - gua_left;
        int gua_height = gua_bottom - gua_top;
        // 挂断?
        if (x < gua_right + gua_width / 2 && y > gua_top - gua_height / 2
                && y < gua_bottom + gua_height / 2 && x > gua_left - gua_width / 2) {
            isControlGua = false;
            if (isFromActivity) {
                mActivity.guaTurnBig();
            } else {
                unknowFragment.guaTurnBig();
            }
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
        } else {
            if (!isControlGua) {
                if (isFromActivity) {
                    mActivity.guaTurnSmall();
                } else {
                    unknowFragment.guaTurnSmall();
                }
            }
            isControlGua = true;
        }

        int duan_width = duan_right - duan_left;
        int duan_height = duan_bottom - duan_top;
        // 短信?
        if (x > duan_left - duan_width / 2 && x < duan_right + duan_width / 2
                && y < duan_bottom + duan_height / 2 && y > duan_top - duan_height / 2) {
            isControlDuan = false;
            if (isFromActivity) {
                mActivity.duanTurnBig();
            } else {
                unknowFragment.duanTurnBig();
            }
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
        } else {
            if (!isControlDuan) {
                if (isFromActivity) {
                    mActivity.duanTurnSmall();
                } else {
                    unknowFragment.duanTurnSmall();
                }
            }
            isControlDuan = true;
        }

        int jie_width = jie_right - jie_left;
        int jie_height = jie_bottom - jie_top;
        // 接听?
        if (x > jie_left - jie_width / 2 && y > jie_top - jie_height / 2
                && y < jie_bottom + jie_height / 2 && x < jie_right + jie_width / 2) {
            isControlJie = false;
            if (isFromActivity) {
                mActivity.jieTurnBig();
            } else {
                unknowFragment.jieTurnBig();
            }
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
                    unknowFragment.allTurnSmall();
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
        } else {
            if (!isControlJie) {
                if (isFromActivity) {
                    mActivity.jieTurnSmall();
                } else {
                    unknowFragment.jieTurnSmall();
                }
            }
            isControlJie = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        CirPanint.setStyle(Paint.Style.STROKE);
        CirPanint.setStrokeWidth(0.5f);
        CirPanint.setColor(Color.WHITE);
        CirPanint.setAntiAlias(true);
        canvas.drawCircle(CirPointX, CirPointY, mBanJing, CirPanint);
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

    }

    public void setFragment(PretendAppUnknowCallFragment5 fragment) {
        unknowFragment = fragment;
    }

}
