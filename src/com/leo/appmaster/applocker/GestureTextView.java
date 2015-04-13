
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
                break;
            case MotionEvent.ACTION_UP:
                mUpX = event.getX();
                float distance = Math.abs(mUpX - mDownX);
                LeoLog.e("onTouchEvent", "distance = " + distance);
                if (distance > 50) {
                    if (distance > 100) {
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
