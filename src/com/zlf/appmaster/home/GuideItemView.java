package com.zlf.appmaster.home;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

public class  GuideItemView extends View {
    
    private int mInitialColor;
    private int mCurrentColor;

    public GuideItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void initBackgroundColor(int backgroundColor){
        mInitialColor = backgroundColor;
        mCurrentColor = backgroundColor;
        this.setBackgroundColor(backgroundColor);
        invalidate();
    }
    
    public int getInitBackgroundColor(){
        return mInitialColor;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        canvas.drawColor(mCurrentColor);
        super.onDraw(canvas);
    }

    public void setCurrentColor(int currentColor){
        mCurrentColor = currentColor;
        invalidate();
    }
    
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {          
        }
    }
}
