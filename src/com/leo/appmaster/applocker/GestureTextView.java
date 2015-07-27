
package com.leo.appmaster.applocker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.leo.appmaster.fragment.PretendAppErrorFragment;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;

public class GestureTextView extends TextView implements OnClickListener {
    private int top, right, bottom, left;
    private float mDownX;
    private float mUpX;

    private float mDownY;
    private float mUpY;

    private PretendAppErrorFragment mPf;
    private Context mContext;
    private Paint mPaint;
    private boolean isSetSelector = false;

    public GestureTextView(Context context) {
        super(context);
        this.mContext = context;
        // getSize();
    }

    public GestureTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // getSize();
        this.setOnClickListener(this);
        this.mContext = context;
        mPaint = new Paint();
    }

    public void setPretendFragment(PretendAppErrorFragment pf) {
        mPf = pf;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        mPaint.setColor(Color.BLUE);// 设置灰色
        mPaint.setStyle(Paint.Style.FILL);// 设置填满
        canvas.drawRect(0, 0, 0, 0, mPaint);// 长方形

        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
        super.onLayout(arg0, arg1, arg2, arg3, arg4);
        left = arg1;
        top = arg2;
        right = arg3;
        bottom = arg4;

        LeoLog.d("testTextview", "left :" + left + "--top :" + top + "--right :" + right
                + "--bottom :" + bottom);

        if (mPf != null && !isSetSelector) {
            mPf.setSelector(left,top,right,bottom);
            isSetSelector = true;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                mUpX = event.getX();
                mUpY = event.getY();
                float distanceX = mUpX - mDownX;
                float distanceY = Math.abs(mUpY - mDownY);
                float fitDistanceX = (right - left) / 3;
                float fitDistanceY = bottom - top + 20;
                if (distanceY > fitDistanceY) {
                    setPressed(false);
                    return true;
                } else if (distanceX > 50) {
                    if (distanceX > fitDistanceX) {
                        if (mPf != null) {
                            mPf.startMove();
//                            mPf.onUnlockPretendSuccessfully();
                            SDKWrapper
                                    .addEvent(mContext, SDKWrapper.P1, "appcover", "done_AppError");
                        }
                    }
                    setPressed(false);
                    return true;
                }
                break;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        if (mPf != null) {
            mPf.onUnlockPretendFailed();
            
            SDKWrapper
            .addEvent(mContext, SDKWrapper.P1, "appcover", "fail_AppError");
            
            
        }
    }

}
