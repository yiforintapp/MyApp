
package com.leo.appmaster.applocker;

import com.leo.appmaster.utils.LeoLog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class GestureRelative extends RelativeLayout {
    private Paint CirPanint;
    private int CirPointX, CirPointY;
    private int mZhiJing, mBanJing;

    public GestureRelative(Context context) {
        super(context);
        init();
    }

    public GestureRelative(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        CirPointX = (r - l) / 2;
        CirPointY = (b - t) / 2;
        // LeoLog.d("testlay", "l :"+l+"--t :"+t+"--r :"+r+"--b :"+b);
        LeoLog.d("testlay", "CirPointX " + CirPointX + "---CirPointY" + CirPointY);
        mZhiJing = (b - t) * 7 / 8;
        mBanJing = mZhiJing / 2;

        super.onLayout(changed, l, t, r, b);
    }

    private void init() {
        CirPanint = new Paint();

    }

    public int getPointX() {
        return CirPointX;
    }

    public int getPointY() {
        return CirPointY;
    }

    public int getZhiJing() {
        return mZhiJing;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        CirPanint.setStyle(Paint.Style.STROKE);
        CirPanint.setColor(Color.WHITE);
        CirPanint.setAntiAlias(true);
        canvas.drawCircle(CirPointX, CirPointY, mBanJing, CirPanint);

        super.onDraw(canvas);
    }
}
