
package com.leo.appmaster.applocker;

import com.leo.appmaster.utils.DipPixelUtil;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

public class MyNumberPicker extends NumberPicker {

    public MyNumberPicker(Context context) {
        super(context);
    }

    public MyNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void addView(View child)
    {
        super.addView(child);
        updateView(child);
    }

    @Override
    public void addView(View child, int index,
            android.view.ViewGroup.LayoutParams params)
    {
        super.addView(child, index, params);
        updateView(child);
    }

    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params)
    {
        super.addView(child, params);
        updateView(child);
    }

    private void updateView(View child) {
        if (child instanceof EditText) {
            EditText et = (EditText) child;
            et.setEnabled(false);
            et.setTextColor(Color.rgb(0x42, 0x85, 0xf4));
            et.setTextSize(DipPixelUtil.dip2px(getContext(), 10));
        } else if (child instanceof Button) {
            Button bt = (Button) child;
            bt.setClickable(false);
            bt.setTextColor(Color.rgb(0xce, 0xce, 0xce));
            bt.setTextSize(DipPixelUtil.dip2px(getContext(), 10));
        }
    }

}
