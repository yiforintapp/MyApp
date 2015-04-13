
package com.leo.appmaster.applocker;

import com.leo.appmaster.fragment.PretendFragment;
import com.leo.appmaster.utils.LeoLog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
import android.widget.TextView;

public class GestureTextView extends TextView implements OnClickListener {

    private float mDownX;
    private float mUpX;

    private float mDownY;
    private float mUpY;

    private PretendFragment mPf;

    public GestureTextView(Context context) {
        super(context);
    }

    public GestureTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.setOnClickListener(this);
    }

    public void setPretendFragment(PretendFragment pf) {
        mPf = pf;
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
                float distanceX = Math.abs(mUpX - mDownX);
                float distanceY = Math.abs(mUpY - mDownY);
                LeoLog.d("onTouchEvent", "distanceX = " + distanceX + "     distanceY =  "
                        + distanceY);
                if (distanceX > 50) {
                    if (distanceY > 150)
                        return true;
                    if (distanceX > 100) {
                        if (mPf != null) {
                            mPf.onUnlockPretendSuccessfully();
                        }
                    }
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
