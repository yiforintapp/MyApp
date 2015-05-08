
package com.leo.appmaster.quickgestures.view;

import com.leo.appmaster.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * QuickGesturesAreaView
 * 
 * @author run
 */
public class QuickGesturesAreaView extends View {
    public static int viewWidth;
    public static int viewHeight;

    public QuickGesturesAreaView(Context context) {
        super(context);
        initUI(context);
    }

    public QuickGesturesAreaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUI(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @SuppressLint("ResourceAsColor")
    private void initUI(Context mContext) {
        this.mContext = mContext;
        // setBackgroundResource(R.color.quick_gesture_switch_setting_hidden_color);
        setBackgroundResource(R.color.quick_gesture_switch_setting_show_color);
        setFocusable(true);
        setClickable(true);
    }
}
