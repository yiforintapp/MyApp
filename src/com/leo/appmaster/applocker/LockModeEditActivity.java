
package com.leo.appmaster.applocker;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.AppChangeListener;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.LockModeEvent;
import com.leo.appmaster.model.AppInfo;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.LockImageView;
import com.leo.appmaster.ui.PagedGridView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LeoSingleLinesInputDialog;
import com.leo.appmaster.utils.LeoLog;

public class LockModeEditActivity extends BaseActivity implements
        AppChangeListener, OnItemClickListener, OnClickListener {

    private static final String SHOW_NOW = "mode changed_show_now";
    private static final String START_FROM_ADD = "startFromadd";
    private String eventMsg = "";
    public LayoutInflater mInflater;
    public ImageView mIvBack, mIvNameEdit, mIvDone;
    public TextView mTvName;
    private PagedGridView mAppPager;
    private LeoSingleLinesInputDialog mModeNameDiglog;
    private LEOAlarmDialog mMakeSureChange;

    private List<AppInfo> mLockedList;
    private List<AppInfo> mUnlockList;
    private LockMode mEditMode;
    private AppInfo mLastSelectApp;
    private String mModeName;
    private int mModeId;
    private List<String> mLockList;
    private boolean mNewMode;
    private boolean mEdited;
    private boolean mSaveDialogClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_mode_edit);
        handleIntent();
        initUI();
        loadData();

        AppLoadEngine.getInstance(this).registerAppChangeListener(this);
    }

    private void initUI() {
        mInflater = LayoutInflater.from(this);

        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mIvNameEdit = (ImageView) findViewById(R.id.iv_edit_mode_name);
        mIvDone = (ImageView) findViewById(R.id.iv_edit_finish);
        mTvName = (TextView) findViewById(R.id.mode_name_tv);
        mTvName.setText(mEditMode.modeName);

        if (mEditMode.defaultFlag != -1) {
            mIvNameEdit.setVisibility(View.INVISIBLE);
        }

        mIvBack.setOnClickListener(this);
        mIvNameEdit.setOnClickListener(this);
        mIvDone.setOnClickListener(this);

        mLockedList = new ArrayList<AppInfo>();
        mUnlockList = new ArrayList<AppInfo>();
        mAppPager = (PagedGridView) findViewById(R.id.pager_unlock);
        mAppPager.setItemClickListener(this);
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
            LockManager lm = LockManager.getInstatnce();
            List<LockMode> modeList = lm.getLockMode();
            for (LockMode lockMode : modeList) {
                if (mModeId == lockMode.modeId) {
                    mEditMode = lockMode;
                }
            }
        }

        mLockList = mEditMode.lockList;
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
        AppLoadEngine.getInstance(this).unregisterAppChangeListener(this);
        super.onDestroy();
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

            mMakeSureChange.setOnClickListener(new OnDiaogClickListener() {
                @Override
                public void onClick(int which) {
                    if (which == 0) {
                        // to cancel
                        finish();
                    } else if (which == 1) {
                        // to save
                        saveMode();
                        Toast.makeText(LockModeEditActivity.this, R.string.save_successful, 0)
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

    private void loadData() {
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
                    mUnlockList.add(appDetailInfo);
                }
            }
        } else {
            for (AppItemInfo appDetailInfo : list) {
                appDetailInfo.isLocked = false;
            }
            mUnlockList.addAll(list);
        }
        Collections.sort(mLockedList, new LockedAppComparator(mLockList));
        Collections.sort(mUnlockList, new DefalutAppComparator());

        ArrayList<AppInfo> resault = new ArrayList<AppInfo>(mLockedList);
        resault.addAll(mUnlockList);

        int rowCount = getResources().getInteger(R.integer.gridview_row_count);
        mAppPager.setDatas(resault, 4, rowCount);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        animateItem(view);
        mEdited = true;
        if (mEditMode.defaultFlag == 0) {
            Toast.makeText(this, R.string.unlock_all_mode_tip, Toast.LENGTH_SHORT).show();
            return;
        }

        mLastSelectApp = (AppInfo) view.getTag();
        AppInfo info = null;
        if (mLastSelectApp != null && mLastSelectApp.isLocked) {
            mLastSelectApp.isLocked = false;
            for (AppInfo baseInfo : mLockedList) {
                if (baseInfo.packageName.equals(mLastSelectApp.packageName)) {
                    info = baseInfo;
                    info.isLocked = false;
                    break;
                }
            }
            mUnlockList.add(info);
            mLockedList.remove(info);
            // List<String> list = new LinkedList<String>();
            // list.add(info.packageName);
            // lm.removePkgFromMode(list, lm.getCurLockMode());

            // to set view unlocked
            ((LockImageView) view.findViewById(R.id.iv_app_icon))
                    .setLocked(false);

            SDKWrapper.addEvent(this, SDKWrapper.P1, "app", "unlock: "
                    + mLastSelectApp.packageName);
        } else {
            mLastSelectApp.isLocked = true;
            for (AppInfo baseInfo : mUnlockList) {
                if (baseInfo.packageName.equals(mLastSelectApp.packageName)) {
                    info = baseInfo;
                    info.isLocked = true;
                    break;
                }
            }
            mLockedList.add(0, info);
            mUnlockList.remove(info);

            // List<String> list = new LinkedList<String>();
            // list.add(info.packageName);
            // lm.addPkg2Mode(list, mEditMode);

            // to set view lock
            ((LockImageView) view.findViewById(R.id.iv_app_icon))
                    .setLocked(true);

            SDKWrapper.addEvent(this, SDKWrapper.P1, "app", " lock: "
                    + mLastSelectApp.packageName);
        }
    }

    private void animateItem(View view) {

        AnimatorSet as = new AnimatorSet();
        as.setDuration(300);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f,
                0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f,
                0.8f, 1f);
        as.playTogether(scaleX, scaleY);
        as.start();
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                // TODO
                onBackPressed();
                break;
            case R.id.iv_edit_finish:
                saveMode();
                break;
            case R.id.iv_edit_mode_name:

                if (mEditMode.defaultFlag == 1) {
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
                            if(text != null) {
                                modeName = text.getText().toString();
                            }
                            mEdited = true;
                            if (TextUtils.isEmpty(modeName)) {
                                shakeView(mModeNameDiglog.getEditText());
                                Toast.makeText(LockModeEditActivity.this,
                                        R.string.lock_mode_name_cant_empty, 0)
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

    private void shakeView(View v) {
        Animation shake = AnimationUtils.loadAnimation(this,
                R.anim.left_right_shake);
        v.startAnimation(shake);
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
                        mModeName = mModeNameDiglog.getEditText().getText().toString();
                        if (TextUtils.isEmpty(mModeName)) {
                            shakeView(mModeNameDiglog.getEditText());
                            Toast.makeText(LockModeEditActivity.this,
                                    R.string.lock_mode_name_cant_empty, 0)
                                    .show();
                        } else {
                            if(mSaveDialogClick) {
                                return;
                            }
                            mSaveDialogClick = true;
                            LockManager lm = LockManager.getInstatnce();
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
                                lm.addLockMode(mEditMode);
                            } else {
                                lm.updateMode(mEditMode);
                            }
                            Toast.makeText(LockModeEditActivity.this, R.string.save_successful, 0)
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
            LockManager lm = LockManager.getInstatnce();
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
                lm.addLockMode(mEditMode);
            } else {
                lm.updateMode(mEditMode);
            }
            Toast.makeText(LockModeEditActivity.this, R.string.save_successful, 0).show();
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

    private void hideIME() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = mModeNameDiglog.getEditText();
        imm.hideSoftInputFromWindow(v != null ? v.getWindowToken() : mIvBack.getWindowToken(), 0);
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
            if (lhs.topPos > -1 && rhs.topPos < 0) {
                return -1;
            } else if (lhs.topPos < 0 && rhs.topPos > -1) {
                return 1;
            } else if (lhs.topPos > -1 && rhs.topPos > -1) {
                return lhs.topPos - rhs.topPos;
            }

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

}
