
package com.leo.appmaster.quickgestures.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mobstat.l;
import com.baidu.mobstat.o;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.quickgestures.QuickSwitchManager;
import com.leo.appmaster.quickgestures.QuickGestureManager.AppLauncherRecorder;
import com.leo.appmaster.quickgestures.model.QuickGsturebAppInfo;
import com.leo.appmaster.quickgestures.model.QuickSwitcherInfo;
import com.leo.appmaster.quickgestures.view.FreeDisturbImageView;
import com.leo.appmaster.quickgestures.view.FreeDisturbPagedGridView;
import com.leo.appmaster.ui.dialog.LEOBaseDialog;
import com.leo.appmaster.utils.LeoLog;

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
    private List<BaseInfo> mAddFreePackageName = null;
    private List<BaseInfo> mRemoveFreePackageName = null;
    private LinearLayout mCheckBoxLL;
    private TextView mCheckBoxTv;
    private CheckBox mCheckBox;
    private int mFlag;
    private static int mSwitchListSize = 0;
    private static final String TAG = "QuickGestureFreeDisturbAppDialog";
    private boolean isFirstClick = true;
    private boolean mFirstStatus = false;
    private String mLastName = "";
    private List<BaseInfo> quickSwitchSaveList;
    private List<QuickGsturebAppInfo> mCommonAppTemp;
    private static int mMostAppConunt = 0;

    public QuickGestureFreeDisturbAppDialog(Context context, int flag) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        mAddFreePackageName = new ArrayList<BaseInfo>();
        mRemoveFreePackageName = new ArrayList<BaseInfo>();
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

                if (mFlag == 2) {
                    // 快捷开关的方式
                    if (quickSwitchSaveList != null) {
                        mFirstStatus = getFirstStatusFromName(quickSwitchSaveList, selectInfl.label);
                    }
                }
                if (selectInfl.isFreeDisturb) {
                    selectInfl.isFreeDisturb = false;
                    mDisturbList.add(selectInfl);
                    mFreeDisturbApp.remove(selectInfl);
                    if (mAddFreePackageName != null && mAddFreePackageName.size() > 0) {
                        mAddFreePackageName.remove(selectInfl);
                    }
                    if (mFirstStatus) {
                        mRemoveFreePackageName.add(selectInfl);
                    }
                    ((FreeDisturbImageView) arg1.findViewById(R.id.iv_app_icon_free))
                            .setDefaultRecommendApp(false);
                    mSwitchListSize -= 1;
                } else {
                    if (mFlag == 2 || mFlag == 3) {

                        if (mFlag == 3) {
                            if (mMostAppConunt <= 12) {
                                selectInfl.gesturePosition = mMostAppConunt;
                                mMostAppConunt += 1;
                            }
                        }
                        if (mSwitchListSize <= 12) {
                            selectInfl.isFreeDisturb = true;
                            mFreeDisturbApp.add(selectInfl);
                            mDisturbList.remove(selectInfl);
                            if (!mFirstStatus) {
                                mAddFreePackageName.add(selectInfl);
                            }
                            if (mRemoveFreePackageName != null && mRemoveFreePackageName.size() > 0) {
                                mRemoveFreePackageName.remove(selectInfl);
                            }
                            ((FreeDisturbImageView) arg1.findViewById(R.id.iv_app_icon_free))
                                    .setDefaultRecommendApp(true);
                            mSwitchListSize += 1;
                        }
                    } else {
                        selectInfl.isFreeDisturb = true;
                        mFreeDisturbApp.add(selectInfl);
                        mDisturbList.remove(selectInfl);
                        if (!mFirstStatus) {
                            mAddFreePackageName.add(selectInfl);
                        }
                        if (mRemoveFreePackageName != null && mRemoveFreePackageName.size() > 0) {
                            mRemoveFreePackageName.remove(selectInfl);
                        }
                        ((FreeDisturbImageView) arg1.findViewById(R.id.iv_app_icon_free))
                                .setDefaultRecommendApp(true);
                    }

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
                loadMostUseData();
                break;
            default:
                break;
        }
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
    }

    protected boolean getFirstStatusFromName(List<? extends BaseInfo> mSaveList, String label) {
        boolean isCheck = false;
        for (int i = 0; i < mSaveList.size(); i++) {
            if (mSaveList.get(i).label.equals(label)) {
                isCheck = true;
            }
        }
        return isCheck;
    }

    public List<BaseInfo> getAddFreePackageName() {
        return mAddFreePackageName;
    }

    public List<BaseInfo> getRemoveFreePackageName() {
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

    // 加载免打扰应用数据
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
        if (!AppMasterPreference.PREF_QUICK_GESTURE_DEFAULT_COMMON_APP_INFO_PACKAGE_NAME
                .equals(packageName)) {
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

    // 加载常用应用
    private void loadMostUseData() {
        mSwitchListSize = 0;
        ArrayList<AppItemInfo> list = AppLoadEngine.getInstance(mContext)
                .getAllPkgInfo();
        mDisturbList = new ArrayList<QuickGsturebAppInfo>();
        mFreeDisturbApp = new ArrayList<QuickGsturebAppInfo>();
        String packageName = null;
        packageName = AppMasterPreference.getInstance(mContext)
                .getCommonAppPackageName();
        int i = 0;

        HashMap<String, Integer> packagePosition = new HashMap<String, Integer>();
        boolean isCheck = AppMasterPreference.getInstance(mContext)
                .getQuickGestureCommonAppDialogCheckboxValue();
        List<QuickGsturebAppInfo> resault = new ArrayList<QuickGsturebAppInfo>();
        QuickGsturebAppInfo qgInfo;

        if (isCheck) {
            ArrayList<AppLauncherRecorder> records = QuickGestureManager.getInstance(mContext).mAppLaunchRecorders;
            Iterator<AppLauncherRecorder> iterator = records.iterator();
            AppLauncherRecorder record;
            i = 0;
            while (iterator.hasNext()) {
                if (i > 13)
                    break;
                record = iterator.next();
                packagePosition.put(record.pkg, i);
                mSwitchListSize++;
                i++;
            }

            for (AppItemInfo info : list) {
                qgInfo = new QuickGsturebAppInfo();
                qgInfo.label = info.label;
                qgInfo.icon = info.icon;
                qgInfo.packageName = info.packageName;
                qgInfo.activityName = info.activityName;
                if (packagePosition.get(qgInfo.packageName) != null) {
                    qgInfo.gesturePosition = packagePosition.get(qgInfo.packageName);
                    qgInfo.isFreeDisturb = true;
                }
                resault.add(qgInfo);
            }
        } else {
            if (!AppMasterPreference.PREF_QUICK_GESTURE_DEFAULT_COMMON_APP_INFO_PACKAGE_NAME
                    .equals(packageName)) {
                String[] names = packageName.split(";");
                int sIndex = -1;
                for (String recoder : names) {
                    sIndex = recoder.indexOf(':');
                    if (sIndex != -1) {
                        packagePosition.put(recoder.substring(0, sIndex),
                                Integer.parseInt(recoder.substring(sIndex + 1)));
                    }
                }
            }
            for (AppItemInfo info : list) {
                qgInfo = new QuickGsturebAppInfo();
                qgInfo.label = info.label;
                qgInfo.icon = info.icon;
                qgInfo.packageName = info.packageName;
                qgInfo.activityName = info.activityName;
                if (packagePosition.get(qgInfo.packageName) != null && mSwitchListSize < 13) {
                    qgInfo.gesturePosition = packagePosition.get(qgInfo.packageName);
                    mSwitchListSize++;
                    qgInfo.isFreeDisturb = true;
                }

                resault.add(qgInfo);
            }
        }
        Collections.sort(resault, new PositionComparator());
        mFreeDisturbApp = resault;
        mGridView.setDatas(mFreeDisturbApp, 4, 4);
    }

    // 加载快捷开关数据
    private void loadQuickSwitchData() {
        // 未设置的快捷开关
        mDisturbList = new ArrayList<QuickGsturebAppInfo>();
        // 设置为快捷开关
        mFreeDisturbApp = new ArrayList<QuickGsturebAppInfo>();
        QuickSwitchManager qsm = QuickSwitchManager.getInstance(mContext);
        List<BaseInfo> allList = qsm.getAllList();
        // 设置为快捷手势的开关
        String packageNames = AppMasterPreference.getInstance(mContext)
                .getSwitchList();
        List<BaseInfo> quickSwitch = qsm.StringToList(packageNames);
        quickSwitchSaveList = quickSwitch;
        if (quickSwitch == null) {
            // 不可能的情况
            for (int i = 0; i < allList.size(); i++) {
                Object object = allList.get(i);
                QuickSwitcherInfo switchInfo = (QuickSwitcherInfo) object;
                mFreeDisturbApp.add(switchInfo);
            }
        } else {
            mSwitchListSize = quickSwitch.size();
            for (int i = 0; i < allList.size(); i++) {
                boolean hasSame = false;
                Object objectNormal = allList.get(i);
                QuickSwitcherInfo switchInfoN = (QuickSwitcherInfo) objectNormal;
                for (int j = 0; j < quickSwitch.size(); j++) {
                    Object objectSp = quickSwitch.get(j);
                    QuickSwitcherInfo switchInfoS = (QuickSwitcherInfo) objectSp;
                    if (switchInfoN.label.equals(switchInfoS.label)) {
                        hasSame = true;
                    }
                }
                if (hasSame) {
                    switchInfoN.isFreeDisturb = true;
                    mFreeDisturbApp.add(switchInfoN);
                } else {
                    switchInfoN.isFreeDisturb = false;
                    mDisturbList.add(switchInfoN);
                }
            }

            if (mFreeDisturbApp != null && mFreeDisturbApp.size() > 0) {
                mFreeDisturbApp.addAll(mDisturbList);
            } else {
                mFreeDisturbApp = mDisturbList;
            }
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

    public static class PositionComparator implements Comparator<QuickGsturebAppInfo> {

        @Override
        public int compare(QuickGsturebAppInfo lhs, QuickGsturebAppInfo rhs) {
            if (lhs.gesturePosition < rhs.gesturePosition) {
                return 1;
            } else if (lhs.gesturePosition > rhs.gesturePosition) {
                return -1;
            } else {
                return 0;
            }
        }
    }

}
