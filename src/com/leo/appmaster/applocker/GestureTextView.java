
package com.leo.appmaster.applocker;

import com.leo.appmaster.fragment.PretendFragment;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.MotionEvent;
import android.widget.TextView;

public class GestureTextView extends TextView implements OnClickListener {
    private int top, right, bottom, left;
    private float mDownX;
    private float mUpX;

    private float mDownY;
    private float mUpY;

    private PretendFragment mPf;
    private Context mContext;

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
    }

    public void setPretendFragment(PretendFragment pf) {
        mPf = pf;
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
        super.onLayout(arg0, arg1, arg2, arg3, arg4);
        left = arg1;
        top = arg2;
        right = arg3;
        bottom = arg4;
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
                            mPf.onUnlockPretendSuccessfully();
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
        }
    }

}
