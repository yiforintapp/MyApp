
package com.leo.appmaster.applocker;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;

public class ErrorWeiZhuang extends Activity implements OnTouchListener {
    private final static int ERRORWEIZHUANG = 1;
    private TextView tv_make_sure_error;
    private AppMasterPreference sp_error_weizhuang;
    private float mDownX;
    private float mUpX;
    private float mDownY;
    private float mUpY;
    private int button_top;
    private int button_right;
    private int button_bottom;
    private int button_left;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_error);
        init();
    }

    private void init() {
        tv_make_sure_error = (TextView) findViewById(R.id.tv_make_sure_error);
        tv_make_sure_error.setOnTouchListener(this);

        sp_error_weizhuang = AppMasterPreference.getInstance(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            button_top = tv_make_sure_error.getTop();
            button_right = tv_make_sure_error.getRight();
            button_bottom = tv_make_sure_error.getBottom();
            button_left = tv_make_sure_error.getLeft();
            LeoLog.d("testerror", "button_top is : " + button_top + "----button_right is : "
                    + button_right + "----button_bottom is :" + button_bottom
                    + "----button_left is : " + button_left);
        }
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                // LeoLog.d("testerror", "X is :" + event.getX() +
                // "----  Y is :" + event.getY());
                break;
            case MotionEvent.ACTION_UP:
                mUpX = event.getX();
                mUpY = event.getY();
                // float distanceX = Math.abs(mUpX - mDownX);
                // float distanceY = Math.abs(mUpY - mDownY);
                float distanceX = mUpX - mDownX;
                float distanceY = mUpY - mDownY;
                // LeoLog.d("onTouchEvent", "distanceX = " + distanceX +
                // "     distanceY =  "
                // + distanceY);
                float fitDistanceX = (button_right - button_left) * 3 / 4;
                float fitDistanceY = button_bottom - button_top + 20;
                if (distanceX > fitDistanceX) {
                    if (distanceY < fitDistanceY) {
                        // ok
                        Toast.makeText(this, getString(R.string.weizhuang_setting_ok), 0).show();
                        sp_error_weizhuang.setPretendLock(ERRORWEIZHUANG);
                        finish();
                    }
                } else {
                    if (distanceX > 0) {
                        Toast.makeText(this,
                                getString(R.string.weizhuang_error_notice_set_false), 0)
                                .show();
                    }
                }
                break;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }
}
