
package com.leo.appmaster.quickgestures.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.quickgestures.model.FreeDisturbAppInfo;
import com.leo.appmaster.quickgestures.view.FreeDisturbImageView;
import com.leo.appmaster.quickgestures.view.FreeDisturbPagedGridView;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;

/**
 * QuickGestureSlideTimeDialog
 * 
 * @author run
 */
public class QuickGestureFreeDisturbAppDialog extends LEOBaseDialog {
    private Context mContext;
    private FreeDisturbPagedGridView mGridView;
    private TextView mTitle, mSureBt;
    private List<FreeDisturbAppInfo> mDisturbList = null;
    private List<FreeDisturbAppInfo> mFreeDisturbApp = null;

    public QuickGestureFreeDisturbAppDialog(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        initUI();
    }

    @SuppressLint("CutPasteId")
    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_free_disturb_app, null);
        Resources resources = AppMasterApplication.getInstance().getResources();
        mGridView = (FreeDisturbPagedGridView) dlgView.findViewById(R.id.free_disturb_gridview);
        mSureBt = (TextView) dlgView.findViewById(R.id.quick_freed_disturb_dlg_right_btn);
        mGridView.setItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                animateItem(arg1);
                FreeDisturbAppInfo selectInfl = (FreeDisturbAppInfo) arg1.getTag();
                if (selectInfl.isFreeDisturb) {
                    selectInfl.isFreeDisturb = false;
                    mDisturbList.add(selectInfl);
                    mFreeDisturbApp.remove(selectInfl);
                    AppMasterPreference.getInstance(mContext).setFreeDisturbAppPackageNameRemove(
                            selectInfl.packageName);
                    ((FreeDisturbImageView) arg1.findViewById(R.id.iv_app_icon_free))
                            .setDefaultRecommendApp(false);
                } else {
                    selectInfl.isFreeDisturb = true;
                    mFreeDisturbApp.add(selectInfl);
                    mDisturbList.remove(selectInfl);
                    AppMasterPreference.getInstance(mContext).setFreeDisturbAppPackageNameAdd(
                            selectInfl.packageName);
                    ((FreeDisturbImageView) arg1.findViewById(R.id.iv_app_icon_free))
                            .setDefaultRecommendApp(true);
                }

            }
        });
        mTitle = (TextView) dlgView.findViewById(R.id.free_disturb_dialog_title);
        loadData();
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
    }

    public void setRightBt(android.view.View.OnClickListener onClick) {
        mSureBt.setOnClickListener(onClick);
    }

    public void setTitle(int id) {
        mTitle.setText(id);
    }

    private void loadData() {
        List<String> packageNames = null;
        // 所有应用
        ArrayList<AppItemInfo> list = AppLoadEngine.getInstance(mContext)
                .getAllPkgInfo();
        // 打扰的应用
        mDisturbList = new ArrayList<FreeDisturbAppInfo>();
        // 免打扰的应用
        mFreeDisturbApp = new ArrayList<FreeDisturbAppInfo>();
        String packageName = AppMasterPreference.getInstance(mContext)
                .getFreeDisturbAppPackageName();
        if (AppMasterPreference.PREF_QUICK_GESTURE_FREE_DISTURB_APP_PACKAGE_NAME
                .equals(packageName)) {
            // Log.e("######################", "没有免干扰应用");
        } else {
            String[] names = packageName.split(";");
            packageNames = Arrays.asList(names);
        }
        for (AppItemInfo appDetailInfo : list) {
            FreeDisturbAppInfo appInfo = new FreeDisturbAppInfo();
            appInfo.icon = appDetailInfo.icon;
            appInfo.packageName = appDetailInfo.packageName;
            appInfo.label = appDetailInfo.label;
            if (packageNames != null) {
                if (packageNames.contains(appDetailInfo.packageName)) {
                    appInfo.isFreeDisturb = true;
                    mFreeDisturbApp.add(appInfo);
                } else {
                    appInfo.isFreeDisturb = false;
                    mDisturbList.add(appInfo);
                }
            } else {
                appInfo.isFreeDisturb = false;
                mDisturbList.add(appInfo);
            }
        }
        if (mFreeDisturbApp != null && mFreeDisturbApp.size() > 0) {
            mFreeDisturbApp.addAll(mDisturbList);
        } else {
            mFreeDisturbApp = mDisturbList;
        }
        mGridView.setDatas(mFreeDisturbApp, 4, 4);
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
