
package com.leo.appmaster.quickgestures.view;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.leo.appmaster.R;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;

import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.quickgestures.view.QuickGestureLayout;
import com.leo.appmaster.quickgestures.view.QuickGestureContainer.GType;
import com.leo.appmaster.quickgestures.view.QuickGestureLayout.LayoutParams;
import com.leo.appmaster.utils.DipPixelUtil;

import android.view.View;
import android.view.WindowManager;
import android.view.View.DragShadowBuilder;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;

public class QuickGesturePopup extends Activity {

    private static int switchNum;
    private QuickGestureContainer mContainer;
    private ImageView iv0;
    private ImageView iv1;
    private ImageView iv2;
    private ImageView iv3;
    private ImageView iv4;
    private ImageView iv5;
    private ImageView iv6;
    private AbstractList<AppItemInfo> list;
    private List<QuickSwitcherInfo> mSwitchList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_quick_gesture_left);
        mContainer = (QuickGestureContainer) findViewById(R.id.gesture_container);

        list = AppLoadEngine.getInstance(this).getAllPkgInfo();
        
        switchNum = 9;
        if (mSwitchList == null) {
            mSwitchList = QuickSwitchManager.getInstance(this).getSwitchList(switchNum);
        }
        
        fillQg1();
        fillQg2();
        fillQg3();
        
        mContainer.showOpenAnimation();  
    }

    private void fillQg1() {
        mContainer.fillGestureItem(GType.DymicLayout, list.subList(0, 7));
    }

    private void fillQg2() {
        mContainer.fillGestureItem(GType.MostUsedLayout, list.subList(7, 17));
    }

    private void fillQg3() {
//        mContainer.fillGestureItem(GType.SwitcherLayout, list.subList(17, 25));
        mContainer.fillGestureItem(GType.SwitcherLayout, mSwitchList);
    }

    @Override
    public void onBackPressed() {
        // mContainer.showCloseAnimation();
        super.onBackPressed();
    }
}
