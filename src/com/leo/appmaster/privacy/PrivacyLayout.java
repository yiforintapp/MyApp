
package com.leo.appmaster.privacy;

import com.leo.appmaster.R;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class PrivacyLayout extends RelativeLayout {
    
    private PrivacyLevelView mPrivacyLevel;
    
    public PrivacyLayout(Context context) {
        super(context);
    }

    public PrivacyLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PrivacyLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected void onFinishInflate() {
        mPrivacyLevel = (PrivacyLevelView) findViewById(R.id.privacy_level);
        super.onFinishInflate();
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(mPrivacyLevel != null) {
            mPrivacyLevel.cancelAnim();
        }    
        return super.onInterceptTouchEvent(ev);
    }
    
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {
            
        }
    }

}
