
package com.leo.appmaster.quickgestures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity.DefalutAppComparator;
import com.leo.appmaster.applocker.AppLockListActivity.InstallTimeComparator;
import com.leo.appmaster.applocker.AppLockListActivity.NameComparator;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppInfo;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.ui.LockImageView;
import com.leo.appmaster.ui.PagedGridView;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;

/**
 * QuickGestureSlideTimeDialog
 * 
 * @author run
 */
public class QuickGestureFreeDisturbAppDialog extends LEOBaseDialog {
    private Context mContext;
    private FreeDisturbPagedGridView mGridView;
    private TextView mTitle;
    private List<AppItemInfo> mUnLockedList;

    public interface OnDiaogClickListener {
        public void onClick(int progress);
    }

    public QuickGestureFreeDisturbAppDialog(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        mUnLockedList = new ArrayList<AppItemInfo>();
        initUI();
    }

    @SuppressLint("CutPasteId")
    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_free_disturb_app, null);
        Resources resources = AppMasterApplication.getInstance().getResources();
        mGridView = (FreeDisturbPagedGridView) dlgView.findViewById(R.id.free_disturb_gridview);
        mGridView.setItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                animateItem(arg1);
                AppInfo selectInfl = (AppInfo) arg1.getTag();
                if (selectInfl.isLocked) {
                    selectInfl.isLocked = false;
                    ((LockImageView) arg1.findViewById(R.id.iv_app_icon_free))
                            .setDefaultRecommendApp(false);
                } else {
                    selectInfl.isLocked = true;
                    ((LockImageView) arg1.findViewById(R.id.iv_app_icon_free))
                            .setDefaultRecommendApp(true);
                }

            }
        });
        mTitle = (TextView) dlgView.findViewById(R.id.free_disturb_dialog_title);
        loadData();
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
    }

    public void setTitle(int id) {
        mTitle.setText(id);
    }

    private void loadData() {
        ArrayList<AppItemInfo> list = AppLoadEngine.getInstance(mContext)
                .getAllPkgInfo();
        List<String> lockList = LockManager.getInstatnce().getCurLockList();
        for (AppItemInfo appDetailInfo : list) {
            appDetailInfo.isLocked = false;
            mUnLockedList.add(appDetailInfo);
        }
        ArrayList<AppInfo> resault = new ArrayList<AppInfo>(mUnLockedList);
        int rowCount = mContext.getResources().getInteger(R.integer.gridview_row_count);
        mGridView.setDatas(resault, 4, rowCount);
    }

    private void animateItem(View view) {
        AnimatorSet animate = new AnimatorSet();
        animate.setDuration(300);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f,
                0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f,
                0.8f, 1f);
        animate.playTogether(scaleX, scaleY);
        animate.start();
    }
}
