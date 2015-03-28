package com.leo.appmaster.ui;

import com.leo.appmaster.utils.DipPixelUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

public class BatteryChartsView extends View{

    private float mPercent = 0.5f;
    
    private Context mContext;
    
    public BatteryChartsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        // TODO Auto-generated constructor stub
    }

    public void setPowerPercent(float percent) {
        mPercent = percent;
        postInvalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);  
        Paint paint=new Paint();  
        paint.setAntiAlias(true);
        
        Path path=new Path();
        paint.setColor(Color.rgb(238, 238, 238));
        path.moveTo(DipPixelUtil.dip2px(mContext, 20),DipPixelUtil.dip2px(mContext, 80));  
        path.lineTo(DipPixelUtil.dip2px(mContext, 300),DipPixelUtil.dip2px(mContext, 80));  
        path.lineTo(DipPixelUtil.dip2px(mContext, 300),DipPixelUtil.dip2px(mContext, 30));  
        path.close();
        canvas.drawPath(path, paint);
        canvas.save();
        
        paint.setAntiAlias(true);
        int poit1X = DipPixelUtil.dip2px(mContext, 20);
        int poit2X = DipPixelUtil.dip2px(mContext, 20 + 280 *mPercent);
        int poit1Y = DipPixelUtil.dip2px(mContext, 80);
        int poit2Y = DipPixelUtil.dip2px(mContext, 80 - 50 * mPercent);
        canvas.clipRect(poit1X,poit1Y ,poit2X ,poit2Y);
        Shader mShader=new LinearGradient(poit1X, poit1Y, poit2X, poit2Y,  
                Color.GREEN,Color.rgb((int)(255 * mPercent), (int)(255 * (1 - mPercent)), 0),  
                Shader.TileMode.REPEAT);  
        paint.setShader(mShader);  

        path.moveTo(poit1X, poit1Y);  
        path.lineTo(poit2X,poit1Y);  
        path.lineTo(poit2X,poit2Y);  
        path.close();
        canvas.drawPath(path, paint);
        canvas.restore();
        
        
        canvas.drawLine(poit2X, poit1Y, poit2X, DipPixelUtil.dip2px(mContext, 20), paint);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {          
        }
    }
    
}
