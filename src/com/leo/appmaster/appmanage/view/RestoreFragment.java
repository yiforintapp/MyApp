
package com.leo.appmaster.appmanage.view;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.appmanage.BackUpActivity;
import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.backup.AppBackupRestoreManager.AppBackupDataListener;
import com.leo.appmaster.backup.AppRestoreAdapter;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.BackupEvent;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.ThirdAppManager;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEOMessageDialog;
import com.leo.appmaster.ui.dialog.LEOProgressDialog;

public class RestoreFragment extends BaseFragment implements AppBackupDataListener {
    public static final String MESSAGE_BACKUP_SUCCESS = "message_backup_success";
    private View list_empty;
    private ListView list_restore;

    private AppRestoreAdapter mRestoreAdapter;

    private Handler mHandler = new Handler();
    private AppBackupRestoreManager mBackupManager_restore;
    private LEOProgressDialog mProgressDialog;
    private LEOAlarmDialog mAlarmDialog;
    private LEOMessageDialog mMessageDialog;

    private AppItemInfo mPendingDelApp;

    @Override
    protected int layoutResourceId() {
        return R.layout.app_restore_list;
    }

    @Override
    protected void onInitUI() {
        list_empty =  findViewById(R.id.list_empty);
        list_restore = (ListView) findViewById(R.id.list_restore);

        mBackupManager_restore = AppBackupRestoreManager.getInstance(mActivity);
        mBackupManager_restore.registerBackupListener(this);

        mRestoreAdapter = new AppRestoreAdapter(mBackupManager_restore);
        list_restore.setAdapter(mRestoreAdapter);

        mBackupManager_restore.prepareDate_restore();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    
    
    @Override
    public void onResume() {
        super.onResume();
        onDataUpdate();
    }

    public AppBackupRestoreManager getBackupManager() {
        return mBackupManager_restore;
    }

    public void tryDeleteApp(AppItemInfo app) {
        mPendingDelApp = app;
        showAlarmDialog(getString(R.string.delete),
                String.format(getString(R.string.query_delete), app.label));
    }

    private void showProgressDialog(String title, String message, int max,
            boolean indeterminate, boolean cancelable) {
        if (mProgressDialog == null) {
            mProgressDialog = new LEOProgressDialog(getActivity());
            mProgressDialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mBackupManager_restore.cancelBackup();
                }
            });
        }
//        mProgressDialog.getWindow().setLayout(DipPixelUtil.dip2px(mActivity, 280), DipPixelUtil.dip2px(mActivity, 250));
        mProgressDialog.setCancelable(cancelable);
        mProgressDialog.setButtonVisiable(cancelable);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(indeterminate);
        mProgressDialog.setMax(max);
        mProgressDialog.setProgress(0);
        mProgressDialog.setMessage(message);
        mProgressDialog.setTitle(title);
        mProgressDialog.show();
    }

    private void showAlarmDialog(String title, String content) {
        if (mAlarmDialog == null) {
            mAlarmDialog = new LEOAlarmDialog(getActivity());
            mAlarmDialog.setOnClickListener(new OnDiaogClickListener() {
                @Override
                public void onClick(int which) {
                    if (which == 1 && mPendingDelApp != null) {
                        showProgressDialog(getString(R.string.delete), String
                                .format(getString(R.string.deleting_app),
                                        mPendingDelApp.label), 0, false, true);
                        ((ThirdAppManager) MgrContext.getManager(MgrContext.MGR_THIRD_APP)).deleteRestoreApp(mPendingDelApp);
//                        mBackupManager_restore.deleteApp(mPendingDelApp);
                    }
                    mPendingDelApp = null;
                }
            });
        }
        mAlarmDialog.setTitle(title);
        mAlarmDialog.setContent(content);
        mAlarmDialog.show();
    }

    public void setContent(String content) {
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

    protected void updateDataList() {
        mRestoreAdapter.updateData();

        // refresh the RestoreFragment
        BackUpActivity app_activity = (BackUpActivity) mActivity;
        app_activity.refreshBackupFragment();

        if (mRestoreAdapter.isEmpty()) {
            list_empty.setVisibility(View.VISIBLE);
        } else {
            list_empty.setVisibility(View.GONE);
        }
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
    public void onApkDeleted(final boolean success) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (success) {
                    updateDataList();
                    //delete backup success , post the refresh the backupNum
                    LeoEventBus.getDefaultBus().post(
                            new BackupEvent(MESSAGE_BACKUP_SUCCESS));
                    
                } else {
                    Toast.makeText(getActivity(),
                            R.string.delete_fail, Toast.LENGTH_LONG).show();
                }
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBackupManager_restore.unregisterBackupListener(this);
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if (mAlarmDialog != null) {
            mAlarmDialog.dismiss();
            mAlarmDialog = null;
        }
        if (mMessageDialog != null) {
            mMessageDialog.dismiss();
            mMessageDialog = null;
        }
    }

    public void updateDataFromAdapter() {
        if(mRestoreAdapter != null) {
            mRestoreAdapter.updateData();

            if (mRestoreAdapter.isEmpty()) {
                list_empty.setVisibility(View.VISIBLE);
            } else {
                list_empty.setVisibility(View.GONE);
            }
        }
    }
}
