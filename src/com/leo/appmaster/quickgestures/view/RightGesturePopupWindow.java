
package com.leo.appmaster.quickgestures.view;

import java.util.ArrayList;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.quickgestures.view.QuickGestureContainer.GType;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

@SuppressLint("InflateParams")
public class RightGesturePopupWindow extends PopupWindow {

    private QuickGestureContainer mContainer;
    private ArrayList<AppItemInfo> list;

    public RightGesturePopupWindow(View contentView, int width, int height, boolean focusable) {
        super(contentView, width, height, focusable);
        mContainer = (QuickGestureContainer) contentView;
        mContainer.setPopWindow(this);
        list = AppLoadEngine.getInstance(AppMasterApplication.getInstance()).getAllPkgInfo();
        fillQg1();
        fillQg2();
        fillQg3();
        fillQg3();
    }

    private void fillQg1() {
        mContainer.fillGestureItem(GType.DymicLayout, list.subList(0, 7));
    }

    private void fillQg2() {
        mContainer.fillGestureItem(GType.MostUsedLayout, list.subList(7, 17));
    }

    private void fillQg3() {
        mContainer.fillGestureItem(GType.SwitcherLayout,
                QuickSwitchManager.getInstance(mContainer.getContext()).getSwitchList(9));
    }
}
