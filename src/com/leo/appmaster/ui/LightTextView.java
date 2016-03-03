package com.leo.appmaster.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Jasper on 2016/3/3.
 */
public class LightTextView extends TextView {
    private static Typeface sLightFace;

    public LightTextView(Context context) {
        this(context, null);
    }

    public LightTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LightTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        if (sLightFace == null) {
            sLightFace = Typeface.createFromAsset(context.getAssets(), "Leo-Light.ttf");
        }
        setTypeface(sLightFace);
    }
}
