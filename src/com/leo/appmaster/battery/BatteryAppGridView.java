package com.leo.appmaster.battery;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by stone on 16/1/18.
 */
public class BatteryAppGridView extends GridView {
    public BatteryAppGridView(Context context) {
        super(context);
    }

    public BatteryAppGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BatteryAppGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int computeVerticalScrollOffset() {
        return super.computeVerticalScrollOffset();
    }
}
