package com.leo.appmaster.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CirCleDongHua extends View {
    private Paint CirPanint;
    private int mBanjing;
    private int mAlpha;
    private int Yuan_x,Yuan_y;
    
    public CirCleDongHua(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CirCleDongHua(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        CirPanint = new Paint();
        
        CirPanint.setStyle(Paint.Style.STROKE);
        CirPanint.setColor(Color.WHITE);
        CirPanint.setAntiAlias(true);
        CirPanint.setAlpha(mAlpha);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        
        canvas.drawCircle(Yuan_x, Yuan_y, mBanjing, CirPanint);
        
        super.onDraw(canvas);
    }

    public void setYuan(float mYuanX,float mYuanY){
        this.Yuan_x = (int) mYuanX;
        this.Yuan_y = (int) mYuanY;
    }
    
    /**
     * 获取半径.需要同步
     * 
     * @return
     */
    public synchronized int getProgress() {
        return mBanjing;
    }

    /**
     * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步 刷新界面调用postInvalidate()能在非UI线程刷新
     * 
     * @param progress
     */
    public synchronized void setProgress(int banjing,int alpha) {
        if (banjing < 0 ) {
            throw new IllegalArgumentException("progress not less than 0");
        }
        if (banjing >= 0) {
            this.mBanjing = banjing;
            this.mAlpha = alpha;
            postInvalidate();
        }
    }
}
