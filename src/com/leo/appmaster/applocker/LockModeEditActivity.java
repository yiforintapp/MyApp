package com.leo.appmaster.applocker;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.AppChangeListener;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.LockModeEvent;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.model.AppInfo;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LeoSingleLinesInputDialog;
import com.leo.appmaster.utils.LeoLog;

/**
 * Created by root on 15-10-11.
 */
public class LockModeEditActivity extends BaseActivity implements
        AppChangeListener, OnClickListener, OnItemClickListener {
    private static final String FROM_DEFAULT_RECOMMENT_ACTIVITY = "applocklist_activity";
    private static final String SHOW_NOW = "mode changed_show_now";
    private static final String START_FROM_ADD = "startFromadd";
    private LeoSingleLinesInputDialog mModeNameDiglog;
    private LEOAlarmDialog mMakeSureChange;
    private List<AppInfo> mLockedList;
    private List<AppInfo> mUnlockList;
    private List<AppInfo> mUnlockRecommendList;
    private List<AppInfo> mUnlockNormalList;

    private LockMode mEditMode;
    private String mModeName;
    private String eventMsg = "";
    private int mModeId;
    private List<String> mLockList;
    private boolean mNewMode;
    private boolean mEdited;
    private boolean mSaveDialogClick;
    private Toast toast = null;


    private ListAppLockAdapter mListAdapter;
    private ListView mLockListView;
    private ImageView mIvNameEdit;
    private View mIvBack, mIvDone;
    private TextView mTvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_lock_mode_edit);

        handleIntent();
        initUI();
        loadData();
    }

    private void initUI() {
        mIvBack = findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(this);
        mIvNameEdit = (ImageView) findViewById(R.id.iv_edit_mode_name);

        mIvDone = findViewById(R.id.iv_edit_finish);
        mTvName = (TextView) findViewById(R.id.mode_name_tv);
        mTvName.setText(mEditMode.modeName);

        if (mEditMode.defaultFlag != -1) {
            mIvNameEdit.setVisibility(View.INVISIBLE);
        }

        mIvNameEdit.setOnClickListener(this);
        mIvDone.setOnClickListener(this);

        mLockListView = (ListView) findViewById(R.id.edit_app_list);
        mLockListView.setOnItemClickListener(this);
        mListAdapter = new ListAppLockAdapter(this);
        mLockListView.setAdapter(mListAdapter);

        mLockedList = new ArrayList<AppInfo>();
        mUnlockList = new ArrayList<AppInfo>();
        mUnlockRecommendList = new ArrayList<AppInfo>();
        mUnlockNormalList = new ArrayList<AppInfo>();

        mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
    }

    private void loadData() {
        mUnlockRecommendList.clear();
        mUnlockNormalList.clear();
        mUnlockList.clear();
        mLockedList.clear();

        ArrayList<AppItemInfo> list = AppLoadEngine.getInstance(this)
                .getAllPkgInfo();

        if (mLockList != null && mLockList.size() != 0) {
            for (AppItemInfo appDetailInfo : list) {
                if (appDetailInfo.packageName.equals(this.getPackageName()))
                    continue;
                if (mLockList.contains(appDetailInfo.packageName)) {
                    appDetailInfo.isLocked = true;
                    mLockedList.add(appDetailInfo);
                } else {
                    appDetailInfo.isLocked = false;

                    appDetailInfo.topPos = fixPosEqules(appDetailInfo);
                    if (appDetailInfo.topPos > -1) {
                        mUnlockRecommendList.add(appDetailInfo);
                    } else {
                        mUnlockNormalList.add(appDetailInfo);
                    }
//                    mUnlockList.add(appDetailInfo);
                }
            }
        } else {
            for (AppItemInfo appDetailInfo : list) {
                appDetailInfo.isLocked = false;
                appDetailInfo.topPos = fixPosEqules(appDetailInfo);
                if (appDetailInfo.topPos > -1) {
                    mUnlockRecommendList.add(appDetailInfo);
                } else {
                    mUnlockNormalList.add(appDetailInfo);
                }
            }
//            mUnlockList.addAll(list);
        }

        Collections.sort(mLockedList, new LockedAppComparator(mLockList));
        Collections.sort(mUnlockRecommendList, new DefalutAppComparator());
        Collections.sort(mUnlockNormalList, new DefalutAppComparator());

        ArrayList<AppInfo> resaultUnlock = new ArrayList<AppInfo>(mUnlockRecommendList);
        resaultUnlock.addAll(mUnlockNormalList);
        mUnlockList = resaultUnlock;

//        Collections.sort(mUnlockList, new DefalutAppComparator());

        ArrayList<AppInfo> resault = new ArrayList<AppInfo>(mLockedList);
        resault.addAll(mUnlockList);

        mListAdapter.setFlag(FROM_DEFAULT_RECOMMENT_ACTIVITY);
        mListAdapter.setData(resault);
    }

    private int fixPosEqules(AppInfo info) {
        int topPosGet = info.topPos;
        String pckName = info.packageName;

        if (topPosGet != -1) {
            String[] strings = AppLoadEngine.sLocalLockArray;
            int k = 0;
            boolean isHavePckName = false;
            for (int i = 0; i < strings.length; i++) {
                String string = strings[i];
                if (string.equals(pckName)) {
                    k = i;
                    isHavePckName = true;
                    break;
                }
            }

            if (isHavePckName) {
                String[] nums = AppLoadEngine.sLocalLockNumArray;
                int num = Integer.parseInt(nums[k]);
                if (num > topPosGet) {
                    return num;
                } else {
                    return topPosGet;
                }
            } else {
                if (topPosGet <= 0) {
                    return 1000;
                } else {
                    return topPosGet;
                }
            }
        } else {
            return -1;
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        String Action = intent.getAction();
        LeoLog.d("testMultiModeView", "Action : " + Action);

        if (Action == null) {
            eventMsg = "mode changed";
        } else if (Action.equals(START_FROM_ADD)) {
            eventMsg = SHOW_NOW;
        }

        mModeName = intent.getStringExtra("mode_name");
        mModeId = intent.getIntExtra("mode_id", -1);
        mNewMode = intent.getBooleanExtra("new_mode", false);

        if (mNewMode) {
            mEditMode = new LockMode();
            mEditMode.defaultFlag = -1;
            mEditMode.isCurrentUsed = false;
            mEditMode.modeName = mModeName;
            mEditMode.lockList = Collections.synchronizedList(new LinkedList<String>());
        } else {
            List<LockMode> modeList = mLockManager.getLockMode();
            for (LockMode lockMode : modeList) {
                if (mModeId == lockMode.modeId) {
                    mEditMode = lockMode;
                }
            }
        }

        mLockList = mEditMode.lockList;
    }

    @Override
    public void onAppChanged(ArrayList<AppItemInfo> changes, int type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_edit_finish:
                saveMode();
                break;
            case R.id.iv_edit_mode_name:
                
                if (mEditMode != null && mEditMode.defaultFlag == 1) {
                    Toast.makeText(this, R.string.cont_edit_visitor_name, Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (mModeNameDiglog == null) {
                    mModeNameDiglog = new LeoSingleLinesInputDialog(this);
                }

                // mModeNameDiglog.getEditText().setText(mEditMode.modeName);
                mModeNameDiglog.getEditText().setText(mModeName);
                mModeNameDiglog.getEditText().selectAll();
                mModeNameDiglog.setRightBtnListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 1) {
                            String modeName = "";
                            EditText text = mModeNameDiglog.getEditText();
                            if (text != null) {
                                android.text.Editable ed = text.getText();
                                if (ed != null) {
                                    modeName = ed.toString();
                                }
                            }
                            mEdited = true;
                            if (TextUtils.isEmpty(modeName)) {
                                shakeView(mModeNameDiglog.getEditText());
                                Toast.makeText(LockModeEditActivity.this,
                                        R.string.lock_mode_name_cant_empty, Toast.LENGTH_SHORT)
                                        .show();
                                return;
                            } else {
                                mModeName = modeName;
                                mTvName.setText(mModeName);
                                dialog.dismiss();
                            }

                        }
                    }
                });
                mModeNameDiglog.setLeftBtnListener(new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            dialog.dismiss();
                        }
                    }
                });
                mModeNameDiglog.show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        try {
            long start = System.currentTimeMillis();
            mEdited = true;
            if (mEditMode == null || mEditMode.defaultFlag == 0) {
                Toast.makeText(this, R.string.unlock_all_mode_tip, Toast.LENGTH_SHORT).show();
                return;
            }

            if (view == null) {
                return;
            }
            ListLockItem lockImageView = (ListLockItem) view.findViewById(R.id.content_item_all);
            if(lockImageView == null) {
                return;
            }
            AppInfo mLastSelectApp = lockImageView.getInfo();
            if (mLastSelectApp == null) {
                return;
            }

            AppInfo info = null;
            if (mLastSelectApp != null && mLastSelectApp.isLocked) {
                mLastSelectApp.isLocked = false;
                for (AppInfo baseInfo : mLockedList) {
                    if (baseInfo.packageName != null && baseInfo.packageName.equals(mLastSelectApp.packageName)) {
                        info = baseInfo;
                        info.isLocked = false;
                        break;
                    }
                }
                mUnlockList.add(info);
                mLockedList.remove(info);
                if (lockImageView != null) {
                    lockImageView.setLockView(false);
                    lockImageView.setDescEx(info, false);
                }
                String toast = getString(R.string.unlock_app_action, info.label);
                showTextToast(toast);

                SDKWrapper.addEvent(this, SDKWrapper.P1, "app", "unlock: "
                        + mLastSelectApp.packageName);
            } else if (mLastSelectApp != null){
                mLastSelectApp.isLocked = true;
                for (AppInfo baseInfo : mUnlockList) {
                    if (baseInfo.packageName != null && baseInfo.packageName.equals(mLastSelectApp.packageName)) {
                        info = baseInfo;
                        info.isLocked = true;
                        break;
                    }
                }
                mLockedList.add(0, info);
                mUnlockList.remove(info);
                if (lockImageView != null) {
                    lockImageView.setLockView(true);
                    lockImageView.setDescEx(info, true);
                }
                String toast = getString(R.string.lock_app_action, info.label);
                showTextToast(toast);
                SDKWrapper.addEvent(this, SDKWrapper.P1, "app", " lock: "
                        + mLastSelectApp.packageName);
            }
            long end = System.currentTimeMillis();
            LeoLog.d("testItemClick", "time delay : " + (end - start));
        } catch (Exception e) {
        }
    }

    private void showTextToast(String msg) {
        if (toast == null) {
            toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (toast != null) {
            toast.cancel();
        }
        if(mMakeSureChange != null) {
            try {
                mMakeSureChange.dismiss();
            } catch (Exception e) {
            }
        }
        mMakeSureChange = null;
    }

    @Override
    public void onBackPressed() {
        if (mNewMode) {
            showSaveTip();
        } else {
            if (mEdited && mEditMode.defaultFlag != 0) {
                showSaveTip();
            } else {
                super.onBackPressed();
            }
        }

    }

    private void showSaveTip() {
        if (mMakeSureChange == null) {
            mMakeSureChange = new LEOAlarmDialog(this);
            mMakeSureChange.setTitle(getString(R.string.mode_save_hint));
            mMakeSureChange.setContent(getString(R.string.mode_save_ask,
                    getString(R.string.lock_mode)));

            mMakeSureChange.setOnClickListener(new LEOAlarmDialog.OnDiaogClickListener() {
                @Override
                public void onClick(int which) {
                    if (which == 0) {
                        // to cancel
                        finish();
                    } else if (which == 1) {
                        // to save
                        try {
                            saveMode();
                        } catch (Exception e) {
                        }
                        Toast.makeText(LockModeEditActivity.this, R.string.save_successful, Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            });
        }
        mMakeSureChange.show();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {

        }
    }

    private class LockedAppComparator implements Comparator<AppInfo> {
        List<String> sortBase;

        public LockedAppComparator(List<String> sortBase) {
            super();
            this.sortBase = sortBase;
        }

        @Override
        public int compare(AppInfo lhs, AppInfo rhs) {
            if (sortBase.indexOf(lhs.packageName) > sortBase
                    .indexOf(rhs.packageName)) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    public static class DefalutAppComparator implements Comparator<AppInfo> {
        @Override
        public int compare(AppInfo lhs, AppInfo rhs) {
//            if (lhs.topPos != -1 || rhs.topPos != -1) {
//                Integer a = lhs.topPos;
//                Integer b = rhs.topPos;
//                return b.compareTo(a);
//            }
//
//            if (lhs.systemApp && !rhs.systemApp) {
//                return -1;
//            } else if (!lhs.systemApp && rhs.systemApp) {
//                return 1;
//            }
//
//            return Collator.getInstance().compare(trimString(lhs.label),
//                    trimString(rhs.label));

            if (lhs.topPos > -1 && rhs.topPos < 0) {
                return 1;
            } else if (lhs.topPos < 0 && rhs.topPos > -1) {
                return -1;
            } else if (lhs.topPos > -1 && rhs.topPos > -1) {
//                return lhs.topPos - rhs.topPos;
                return rhs.topPos - lhs.topPos;
            }

//            if (lhs.topPos > -1 && rhs.topPos < 0) {
//                return -1;
//            } else if (lhs.topPos < 0 && rhs.topPos > -1) {
//                return 1;
//            } else if (lhs.topPos > -1 && rhs.topPos > -1) {
//                return lhs.topPos - rhs.topPos;
//            }

            if (lhs.systemApp && !rhs.systemApp) {
                return -1;
            } else if (!lhs.systemApp && rhs.systemApp) {
                return 1;
            }

            return Collator.getInstance().compare(trimString(lhs.label),
                    trimString(rhs.label));
        }

        private String trimString(String s) {
            return s.replaceAll("\u00A0", "").trim();
        }
    }

    private void saveMode() {
        String name = mTvName.getText().toString();
        if (TextUtils.equals(name, getString(R.string.new_mode))) {
            if (mModeNameDiglog == null) {
                mModeNameDiglog = new LeoSingleLinesInputDialog(this);
            }
            mModeNameDiglog.getEditText().setText(mEditMode.modeName);
            mModeNameDiglog.getEditText().selectAll();
            mModeNameDiglog.setRightBtnListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 1) {
                        EditText et = mModeNameDiglog.getEditText();
                        if (et != null) {
                            android.text.Editable ed = et.getText();
                            if (ed != null) {
                                mModeName = ed.toString();
                            }
                        }
                        if (TextUtils.isEmpty(mModeName)) {
                            shakeView(mModeNameDiglog.getEditText());
                            Toast.makeText(LockModeEditActivity.this,
                                    R.string.lock_mode_name_cant_empty, Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            if (mSaveDialogClick) {
                                return;
                            }
                            mSaveDialogClick = true;
                            mEditMode.modeName = mModeName;
                            List<String> changedList = Collections.synchronizedList(new LinkedList<String>());
                            for (AppInfo appInfo : mLockedList) {
                                changedList.add(new String(appInfo.packageName));
                            }
                            mEditMode.lockList = changedList;
                            if (mNewMode) {
                                // mEditMode.modeIconId = R.drawable.lock_mode_default;
                                // mEditMode.modeIcon = BitmapFactory.decodeResource(getResources(),
                                //        R.drawable.lock_mode_default);
                                mLockManager.addLockMode(mEditMode);
                            } else {
                                mLockManager.updateMode(mEditMode);
                            }
                            Toast.makeText(LockModeEditActivity.this, R.string.save_successful, Toast.LENGTH_SHORT)
                                    .show();
                            LeoEventBus.getDefaultBus().post(
                                    new LockModeEvent(EventId.EVENT_MODE_CHANGE, eventMsg));
                            hideIME();
                            mModeNameDiglog.getEditText().postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    finish();
                                }
                            }, 400);
                        }
                    }
                }
            });
            mModeNameDiglog.setLeftBtnListener(new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        dialog.dismiss();
                    }
                }
            });
            mModeNameDiglog.show();
            return;
        } else {
            mEditMode.modeName = mModeName;
            List<String> changedList = Collections.synchronizedList(new LinkedList<String>());
            for (AppInfo appInfo : mLockedList) {
                changedList.add(new String(appInfo.packageName));
            }
            mEditMode.lockList = changedList;
            if (mNewMode) {
//                mEditMode.modeIconId = R.drawable.lock_mode_default;
                // mEditMode.modeIcon = BitmapFactory.decodeResource(getResources(),
                //        R.drawable.lock_mode_default);
                mLockManager.addLockMode(mEditMode);
            } else {
                mLockManager.updateMode(mEditMode);
            }
            Toast.makeText(LockModeEditActivity.this, R.string.save_successful, Toast.LENGTH_SHORT).show();
            // LeoEventBus.getDefaultBus().post(
            // new LockModeEvent(EventId.EVENT_MODE_CHANGE, "mode changed"));
            LeoEventBus.getDefaultBus().post(
                    new LockModeEvent(EventId.EVENT_MODE_CHANGE, eventMsg));
            if (mModeNameDiglog != null) {
                hideIME();
            }

            // mModeNameDiglog.getEditText().postDelayed(new Runnable() {
            mIvNameEdit.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 400);
        }

    }

    private void shakeView(View v) {
        Animation shake = AnimationUtils.loadAnimation(this,
                R.anim.left_right_shake);
        v.startAnimation(shake);
    }

    private void hideIME() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = mModeNameDiglog.getEditText();
        imm.hideSoftInputFromWindow(v != null ? v.getWindowToken() : mIvBack.getWindowToken(), 0);
    }

}
