
package com.leo.appmaster.quickgestures.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

import com.leo.appmaster.R;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.ClickQuickItemEvent;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer;

public class QuickGesturePopupActivity extends Activity {

    private AppleWatchContainer mContainer;
    private ImageView iv_roket, iv_pingtai, iv_yun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_quick_gesture_apple_watch);
        LeoEventBus.getDefaultBus().register(this);
        mContainer = (AppleWatchContainer) findViewById(R.id.gesture_container);
        iv_roket = (ImageView) findViewById(R.id.iv_rocket);
        iv_pingtai = (ImageView) findViewById(R.id.iv_pingtai);
        iv_yun = (ImageView) findViewById(R.id.iv_yun);
        mContainer.setRockey(iv_roket, iv_pingtai, iv_yun);

        int showOrientation = getIntent().getIntExtra("show_orientation", 0);
        mContainer.setShowOrientation(showOrientation == 0 ? AppleWatchContainer.Orientation.Left
                : AppleWatchContainer.Orientation.Right);
        fillDynamicLayout();
        overridePendingTransition(-1, -1);
    }

    public void onEventMainThread(ClickQuickItemEvent event) {
        mContainer.checkStatus(event.info);
    }

    @Override
    protected void onResume() {
        mContainer.post(new Runnable() {
            @Override
            public void run() {
                mContainer.showOpenAnimation();
            }
        });
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        finish();
        super.onStop();
        FloatWindowHelper.mGestureShowing = false;
    }

    private void fillDynamicLayout() {
        mContainer.fillDynamicLayout();
    }

    @Override
    protected void onDestroy() {
        FloatWindowHelper.mGestureShowing = false;
        LeoEventBus.getDefaultBus().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mContainer.isEditing()) {
            mContainer.leaveEditMode();
        } else {
            mContainer.showCloseAnimation();
        }
    }
}
