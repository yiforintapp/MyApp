package com.leo.appmaster.appmanage;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;

import com.leo.appmaster.R;
import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.backup.AppDeleteAdapter;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.BackupEvent;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;

/**
 * Created by qili on 15-10-20.
 */
public class UninstallActivity extends BaseActivity implements View.OnClickListener,
        AppBackupRestoreManager.AppBackupDataListener, AppLoadEngine.AppChangeListener {
    public static final String MESSAGE_BACKUP_SUCCESS = "message_backup_success";
    public static final String MESSAGE_DELETE_APP = "message_delete_app";
    public static final String MESSAGE_ADD_APP = "message_add_app";
    private ListView mListview;

    private AppBackupRestoreManager mDeleteManager;
    private AppDeleteAdapter mDeleteAdapter;
    private CommonToolbar mTitleBar;
    private Handler mHandler = new Handler();
    private View mNothingToShowView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (AppMasterPreference.getInstance(this).getIsNeedCutBackupUninstallAndPrivacyContact()) {
//            finish();
//        }
        setContentView(R.layout.activity_uninstall);
        
        if (getIntent().getBooleanExtra("from_quickhelper", false)) {
            SDKWrapper.addEvent(this, SDKWrapper.P1,
                    "assistant", "uninstall_cnts");
        }

        initUI();
        loadData();
    }

    private void initUI() {
        LeoEventBus.getDefaultBus().register(this);
        mDeleteManager = AppBackupRestoreManager.getInstance(this);
        mDeleteManager.registerBackupListener(this);

        mTitleBar = (CommonToolbar) findViewById(R.id.uninstall_title_bar);
        mTitleBar.setToolbarTitle(R.string.home_app_manager_apps_delete);
        mTitleBar.setToolbarColorResource(R.color.cb);
        mTitleBar.setOptionMenuVisible(false);

        mListview = (ListView) findViewById(R.id.list_uninstall);
        mDeleteAdapter = new AppDeleteAdapter(mDeleteManager, this);
        mListview.setAdapter(mDeleteAdapter);

        mNothingToShowView = findViewById(R.id.content_show_nothing);
    }

    private void loadData() {
        onDataReady();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDeleteManager.unregisterBackupListener(this);
        LeoEventBus.getDefaultBus().unregister(this);
    }

    public void onEventMainThread(BackupEvent event) {
        if (MESSAGE_BACKUP_SUCCESS.equals(event.eventMsg)) {
        } else if (MESSAGE_DELETE_APP.equals(event.eventMsg)) {
            loadData();
        } else if (MESSAGE_ADD_APP.equals(event.eventMsg)) {
            loadData();
        }
    }

    @Override
    public void onDataReady() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateDataList();
            }
        });
    }

    private void updateDataList() {
        mDeleteAdapter.updateData();
    }

    @Override
    public void onDataUpdate() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateDataList();
            }
        });
    }

    @Override
    public void onBackupProcessChanged(int doneNum, int totalNum, String currentApp) {

    }

    @Override
    public void onBackupFinish(boolean success, int successNum, int totalNum, String message) {

    }

    @Override
    public void onApkDeleted(boolean success) {

    }

    @Override
    public void onAppChanged(ArrayList<AppItemInfo> changes, int type) {

    }

    @Override
    public void onClick(View view) {

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {

        }

    }

    public void showNothing() {
        mListview.setVisibility(View.GONE);
        mNothingToShowView.setVisibility(View.VISIBLE);
    }
}
