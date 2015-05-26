
package com.leo.appmaster.quickgestures.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.quickgestures.model.QuickGsturebAppInfo;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
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
    private TextView mTitle, mSureBt, mLeftBt;
    private List<QuickGsturebAppInfo> mDisturbList = null;
    private List<QuickGsturebAppInfo> mFreeDisturbApp = null;
    private List<Object> mAddFreePackageName = null;
    private List<Object> mRemoveFreePackageName = null;
    private LinearLayout mCheckBoxLL;
    private TextView mCheckBoxTv;
    private CheckBox mCheckBox;
    private int mFlag;
    private static final String TAG = "QuickGestureFreeDisturbAppDialog";

    public QuickGestureFreeDisturbAppDialog(Context context, int flag) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        mAddFreePackageName = new ArrayList<Object>();
        mRemoveFreePackageName = new ArrayList<Object>();
        this.mFlag = flag;
        initUI();
    }

    @SuppressLint("CutPasteId")
    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_free_disturb_app, null);
        Resources resources = AppMasterApplication.getInstance().getResources();
        mGridView = (FreeDisturbPagedGridView) dlgView.findViewById(R.id.free_disturb_gridview);
        mSureBt = (TextView) dlgView.findViewById(R.id.quick_freed_disturb_dlg_right_btn);
        mLeftBt = (TextView) dlgView.findViewById(R.id.quick_freed_disturb_dlg_left_btn);
        mCheckBoxLL = (LinearLayout) dlgView.findViewById(R.id.checkboxLL);
        mCheckBoxTv = (TextView) dlgView.findViewById(R.id.dialog_all_app_itme_tv);
        mCheckBox = (CheckBox) dlgView.findViewById(R.id.dialog_all_app_itme_normalRB);
        mGridView.setItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                animateItem(arg1);
                QuickGsturebAppInfo selectInfl = (QuickGsturebAppInfo) arg1.getTag();
                if (selectInfl.isFreeDisturb) {
                    selectInfl.isFreeDisturb = false;
                    mDisturbList.add(selectInfl);
                    mFreeDisturbApp.remove(selectInfl);
                    mRemoveFreePackageName.add(selectInfl);
                    ((FreeDisturbImageView) arg1.findViewById(R.id.iv_app_icon_free))
                            .setDefaultRecommendApp(false);
                } else {
                    selectInfl.isFreeDisturb = true;
                    mFreeDisturbApp.add(selectInfl);
                    mDisturbList.remove(selectInfl);
                    mAddFreePackageName.add(selectInfl);
                    ((FreeDisturbImageView) arg1.findViewById(R.id.iv_app_icon_free))
                            .setDefaultRecommendApp(true);
                }

            }
        });
        mTitle = (TextView) dlgView.findViewById(R.id.free_disturb_dialog_title);
        switch (mFlag) {
            case 1:
                // 免打扰应用
                loadData(1);
                break;
            case 2:
                // 快捷开关
                loadQuickSwitchData();
                break;
            case 3:
                // 常用应用
                loadData(2);
                break;
            default:
                break;
        }
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
    }

    public List<Object> getAddFreePackageName() {
        return mAddFreePackageName;
    }

    public List<Object> getRemoveFreePackageName() {
        return mRemoveFreePackageName;
    }

    public void setRightBt(android.view.View.OnClickListener onClick) {
        mSureBt.setOnClickListener(onClick);
    }

    public void setLeftBt(android.view.View.OnClickListener onClick) {
        mLeftBt.setOnClickListener(onClick);
    }

    public void setIsShowCheckBox(boolean flag) {
        if (flag) {
            mCheckBoxLL.setVisibility(View.VISIBLE);
        } else {
            mCheckBoxLL.setVisibility(View.GONE);
        }
    }

    public void setCheckBoxText(int string) {
        mCheckBoxTv.setText(string);
    }

    public void setCheckValue(boolean flag) {
        mCheckBox.setChecked(flag);
    }

    public boolean getCheckValue() {
        return mCheckBox.isChecked();
    }

    public void setTitle(int id) {
        mTitle.setText(id);
    }

    // 加载系统应用数据
    private void loadData(int flag) {
        List<String> packageNames = null;
        // 所有应用
        ArrayList<AppItemInfo> list = AppLoadEngine.getInstance(mContext)
                .getAllPkgInfo();
        // 打扰的应用
        mDisturbList = new ArrayList<QuickGsturebAppInfo>();
        // 免打扰的应用
        mFreeDisturbApp = new ArrayList<QuickGsturebAppInfo>();
        String packageName = null;
        switch (flag) {
            case 1:
                packageName = AppMasterPreference.getInstance(mContext)
                        .getFreeDisturbAppPackageName();
                break;
            case 2:
                packageName = AppMasterPreference.getInstance(mContext)
                        .getCommonAppPackageName();
                break;
            default:
                break;
        }
        if (AppMasterPreference.PREF_QUICK_GESTURE_FREE_DISTURB_APP_PACKAGE_NAME
                .equals(packageName)) {
            // Log.e(TAG, "NoFreeApp!");
        } else {
            String[] names = packageName.split(";");
            packageNames = Arrays.asList(names);
        }
        for (AppItemInfo appDetailInfo : list) {
            QuickGsturebAppInfo appInfo = new QuickGsturebAppInfo();
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

    // 加载快捷开关数据
    private void loadQuickSwitchData() {
        // 为设置的快捷开关
        mDisturbList = new ArrayList<QuickGsturebAppInfo>();
        // 设置为快捷开关
        mFreeDisturbApp = new ArrayList<QuickGsturebAppInfo>();
        QuickSwitchManager qsm = QuickSwitchManager.getInstance(mContext);
        List<Object> allList = qsm.getSwitchList(AppMasterPreference.getInstance(mContext)
                .getSwitchListSize());
        // 设置为快捷手势的开关
        String quickGestureSwitchPackageNames = AppMasterPreference.getInstance(mContext)
                .getSwitchList();
        List<Object> quickSwitch = qsm.StringToList(quickGestureSwitchPackageNames);
        if (allList != null) {
            for (Object object : allList) {
                if (quickSwitch != null) {
                    if (quickSwitch.contains(object)) {
                        QuickSwitcherInfo switchInfo = (QuickSwitcherInfo) object;
                        switchInfo.isFreeDisturb = true;
                        mFreeDisturbApp.add(switchInfo);
                    } else {
                        QuickSwitcherInfo switchInfo = (QuickSwitcherInfo) object;
                        switchInfo.isFreeDisturb = false;
                        mDisturbList.add(switchInfo);
                    }
                } else {
                    QuickSwitcherInfo switchInfo = (QuickSwitcherInfo) object;
                    mDisturbList.add(switchInfo);
                }
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
