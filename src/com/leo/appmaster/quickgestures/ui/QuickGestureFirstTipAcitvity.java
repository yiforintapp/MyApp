
package com.leo.appmaster.quickgestures.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;

public class QuickGestureFirstTipAcitvity extends BaseActivity implements OnTouchListener {
    private ImageView mLeftView, mRightView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_miui_open_float_window_tip);
        mLeftView = (ImageView) findViewById(R.id.quick_sliding_left_area);
        mRightView = (ImageView) findViewById(R.id.quick_sliding_right_area);
        mLeftView.setOnTouchListener(this);
        mRightView.setOnTouchListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int width = view.getWidth();
        int height = view.getHeight();
        float downX = 0;
        float downY = 0;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = Math.abs(event.getX() - downX);
                float moveY = Math.abs(event.getY() - downY);
                if (moveX > width / 50 || moveY > width / 50) {
                    finish();
                    Toast.makeText(QuickGestureFirstTipAcitvity.this, "开启快捷之旅", Toast.LENGTH_SHORT)
                            .show();
                    AppMasterPreference.getInstance(getApplicationContext())
                            .setFristSlidingTip(true);
                    Intent intent;
                    intent = new Intent(AppMasterApplication.getInstance(),
                            QuickGesturePopupActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    AppMasterApplication.getInstance().startActivity(intent);
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return true;
    }
}
