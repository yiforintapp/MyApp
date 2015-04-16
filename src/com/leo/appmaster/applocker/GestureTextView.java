
package com.leo.appmaster.applocker;

import com.leo.appmaster.fragment.PretendFragment;
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

    public GestureTextView(Context context) {
        super(context);
//        getSize();
    }

    public GestureTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        getSize();
        this.setOnClickListener(this);
    }

    public void setPretendFragment(PretendFragment pf) {
        mPf = pf;
    }

//    private void getSize() {
//        ViewTreeObserver guaduan = getViewTreeObserver();
//        guaduan.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
//            public void onGlobalLayout() {
//                top = getTop();
//                right = getRight();
//                bottom = getBottom();
//                left = getLeft();
//                LeoLog.d("testerrorfragment", "11left is : " + left
//                        + "--11top is : " + top + "--11right is : "
//                        + right + "---11bottom is :" + bottom);
//                // 成功调用一次后，移除 Hook 方法，防止被反复调用
//                // removeGlobalOnLayoutListener() 方法在 API 16 后不再使用
//                // 使用新方法 removeOnGlobalLayoutListener() 代替
//                getViewTreeObserver().removeOnGlobalLayoutListener(this);
//            }
//        });
//    }
    
    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
        super.onLayout(arg0, arg1, arg2, arg3, arg4);
        left = arg1;
        top = arg2;
        right = arg3;
        bottom = arg4;
//        LeoLog.d("testerrorfragment", "left is : " + left
//                + "--top is : " + top + "--right is : "
//                + right + "---bottom is :" + bottom);
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
