
package com.leo.appmaster.home;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

public class HomeTopView extends View {

    
    
    
    
    public HomeTopView(Context context) {
        super(context);
    }

    public HomeTopView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        
        
        
        
        super.onDraw(canvas);
    }
    
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {          
        }
    }

}
