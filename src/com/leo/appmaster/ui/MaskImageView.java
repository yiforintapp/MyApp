package com.leo.appmaster.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class MaskImageView extends ImageView {
    public static final String TAG = "MaskImageView";
    private Handler hander;
    private Runnable refresh;
    private int a = 90;
    private int r = 0;
    private int g = 0;
    private int b = 0;
    private boolean enable = true;

    private boolean checked;

    public MaskImageView(Context context) {
        super(context);
        init();
    }

    public MaskImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public boolean updateState(final View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.setPressed(true);
                v.invalidate();
                hander.removeCallbacks(refresh);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                hander.post(refresh);
                break;
        }
        return !isClickable() && !isLongClickable();
    }

    private void init() {
        hander = new Handler();
        refresh = new Runnable() {
            @Override
            public void run() {
                setPressed(false);
                invalidate();
            }
        };

        super.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                if (!enable) {
                    return false;
                }
                return updateState(v, event);
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (this.isPressed() || checked) {
            canvas.drawARGB(a, r, g, b);
        }
    }

    public void setChecked(boolean checked) {
        if (this.checked != checked) {
            this.checked = checked;
            invalidate();
        }
    }
}
