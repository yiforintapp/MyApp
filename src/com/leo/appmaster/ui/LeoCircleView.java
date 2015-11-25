package com.leo.appmaster.ui;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class LeoCircleView extends View{
    
    private Paint paint;
    private int color ;
    private int radius ; // 圆环的半径
    private int centre ; // 获取圆心的x坐标
    
    public LeoCircleView(Context context) {
        this(context, null);
    }

    public LeoCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LeoCircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paint = new Paint();
        paint.setAntiAlias(true); // 消除锯齿
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(color); // 设置圆环的颜色
        radius = getWidth() / 2; 
        centre = getWidth() / 2; 
        canvas.drawCircle(centre, centre, radius, paint); 
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
    }
    
    public int getColor() {
        return color;
    }
    public void setColor(int color) {
        this.color = color;
        paint.setColor(color);
        invalidate();
    }
}
