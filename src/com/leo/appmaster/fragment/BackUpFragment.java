
package com.leo.appmaster.fragment;

import java.io.File;
import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.backup.AppBackupAdapter;
import com.leo.appmaster.backup.AppBackupItemView;
import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.backup.AppBackupRestoreManager.AppBackupDataListener;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.BackupEvent;
import com.leo.appmaster.home.BackUpActivity;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEOMessageDialog;
import com.leo.appmaster.ui.dialog.LEOProgressDialog;
import com.leo.appmaster.utils.LeoLog;

public class BackUpFragment extends BaseFragment implements AppBackupDataListener, OnClickListener,
        OnItemClickListener {
    public static final String MESSAGE_BACKUP_SUCCESS = "message_backup_success";
    private ListView list_backup_view;
    private View tv_button_backup;
    private View iv_check_backup;
    private ProgressBar pb_loading;

    private Handler mHandler = new Handler();
    private AppBackupRestoreManager mBackupManager;
    private AppBackupAdapter mBackupAdapter;
    private LEOProgressDialog mProgressDialog;
    private LEOAlarmDialog mAlarmDialog;
    private LEOMessageDialog mMessageDialog;
    private AppItemInfo mPendingDelApp;
    private boolean isAllCheck = false;

    @Override
    protected int layoutResourceId() {
        return R.layout.app_backup_list;
    }

    @Override
    protected void onInitUI() {
        list_backup_view = (ListView) findViewById(R.id.list_backup_view);
        list_backup_view.setOnItemClickListener(this);
        tv_button_backup = findViewById(R.id.tv_button_backup);
        tv_button_backup.setOnClickListener(this);
        iv_check_backup = findViewById(R.id.iv_check_backup);
        iv_check_backup.setOnClickListener(this);
        pb_loading = (ProgressBar) findViewById(R.id.pb_loading);

        mBackupManager = AppMasterApplication.getInstance().getBuckupManager();
        mBackupManager.registerBackupListener(this);

        mBackupAdapter = new AppBackupAdapter(mBackupManager);
        list_backup_view.setAdapter(mBackupAdapter);

        mBackupManager.prepareDate();
        renameFolder();
    }

    public void renameFolder() {
        String newName = getBackupPath();
        String path = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        path += "leo/appmaster/.backup/";
        File file = new File(path);
        if (file.exists()) {
            boolean ret;
            try {
                ret = file.renameTo(new File(newName));
            } catch (Exception e) {
                String newPath = path + AppBackupRestoreManager.BACKUP_PATH;
                File newFile = new File(newPath);
                newFile.mkdirs();
                ret = file.renameTo(new File(newPath));
                e.printStackTrace();
            }
            if (ret) {
                // Toast.makeText(this, "success",Toast.LENGTH_LONG).show();
                // LeoLog.i("AppBackupRestoreActivity",
                // "*******rename success");
            } else {
                // Toast.makeText(this, "fail",Toast.LENGTH_LONG).show();
                // LeoLog.i("AppBackupRestoreActivity", "*******rename fail");
            }
        }

    }

    public String getBackupPath() {
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            String path = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
            path += AppBackupRestoreManager.BACKUP_PATH;
            File backupDir = new File(path);
            if (!backupDir.exists()) {
                boolean success = backupDir.mkdirs();
                if (!success) {
                    return null;
                }
            }
            return path;
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    // useless code
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

    public void updateDataList() {
        mBackupAdapter.updateData();

        pb_loading.setVisibility(View.GONE);
        list_backup_view.setVisibility(View.VISIBLE);

        // refresh the RestoreFragment
        BackUpActivity app_activity = (BackUpActivity) mActivity;
        app_activity.refreshRestoreFragment();

        if (mBackupAdapter.getisAllCheck()) {
            iv_check_backup.setTag(true);
        } else {
            iv_check_backup.setTag(false);
        }

        if (mBackupAdapter.hasBackupApp()) {
            if (mBackupAdapter.getisAllCheck()) {
                iv_check_backup
                        .setBackgroundResource(R.drawable.app_select);
            } else {
                iv_check_backup
                        .setBackgroundResource(R.drawable.app_unselect);
            }
            iv_check_backup.setEnabled(true);
        } else {
            iv_check_backup
                    .setBackgroundResource(R.drawable.app_all_backuped);
            iv_check_backup.setEnabled(false);
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
    public void onResume() {
        super.onResume();

        mBackupManager.checkDataUpdate();
    }

    public AppBackupRestoreManager getBackupManager() {
        return mBackupManager;
    }

    @Override
    public void onBackupProcessChanged(final int doneNum, int totalNum, final String currentApp) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.setProgress(doneNum);
                    if (currentApp != null) {
                        String backup = getString(R.string.backuping);
                        mProgressDialog.setMessage(String.format(backup,
                                currentApp));
                    }
                }
            }
        });
    }

    @Override
    public void onBackupFinish(final boolean success, final int successNum, int totalNum,
            final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (success) {
                    showMessageDialog(mActivity.getString(R.string.backup_finish), String
                            .format(mActivity.getString(R.string.backuped_count),
                                    successNum, message));

                    // backup finish and success , now send eventBus to
                    // homeAppManagerFrament to refreash UI
                    LeoEventBus.getDefaultBus().post(
                            new BackupEvent(MESSAGE_BACKUP_SUCCESS));

                } else {
                    Toast.makeText(mActivity, message,
                            Toast.LENGTH_LONG).show();
                }
                updateDataList();
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    @Override
    public void onApkDeleted(final boolean success) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBackupManager.unregisterBackupListener(this);
        mBackupAdapter.checkAll(false);
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

    @Override
    public void onClick(View v) {
        if (v == tv_button_backup) {
            if (isAdded()) { // 判断fragment是否已经加入activity
                ArrayList<AppItemInfo> items = mBackupAdapter.getSelectedItems();
                int size = items.size();
                if (size > 0) {
                    showProgressDialog(getString(R.string.button_backup), "", size,
                            false, true);
                    mBackupManager.backupApps(items);
                    // track backup
                    // for (AppItemInfo info : items) {
                    // SDKWrapper.addEvent(getActivity(), SDKWrapper.P1,
                    // "backup",
                    // "backup: "
                    // + info.packageName);
                    // }
                } else {
                    Toast.makeText(getActivity(), R.string.no_application_selected,
                            Toast.LENGTH_SHORT).show();
                }
            }
        } else if (v == iv_check_backup) {
            Object tag = iv_check_backup.getTag();
            if (tag instanceof Boolean) {
                boolean checkAll = !(Boolean) tag;
                mBackupAdapter.checkAll(checkAll);
                // iv_check_backup
                // .setImageResource(checkAll ?
                // R.drawable.check_all_selected_selector
                // : R.drawable.check_all_selector);
                // iv_check_backup.setImageResource(checkAll ?
                // R.drawable.app_select
                // : R.drawable.app_unselect);
                iv_check_backup.setBackgroundResource(checkAll ? R.drawable.app_select
                        : R.drawable.app_unselect);
                isAllCheck = checkAll ? true : false;
                mBackupAdapter.setisAllCheck(checkAll ? true : false);
                iv_check_backup.setTag(checkAll);
            }
        }
    }

    private void showProgressDialog(String title, String message, int max,
            boolean indeterminate, boolean cancelable) {
        if (mProgressDialog == null) {
            mProgressDialog = new LEOProgressDialog(getActivity());
            mProgressDialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mBackupManager.cancelBackup();
                    iv_check_backup.setBackgroundResource(R.drawable.app_unselect);
                    mBackupAdapter.setisAllCheck(false);
                    iv_check_backup.setTag(false);
                    mBackupAdapter.checkAll(false);
                }
            });
        }
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (view instanceof AppBackupItemView) {
            AppBackupItemView item = (AppBackupItemView) view;
            AppItemInfo app = (AppItemInfo) item.getTag();

            Object tag = iv_check_backup.getTag();
            // if ((Boolean) tag) {
            // LeoLog.d("BackUpFragment", "onItemClick111");
            // } else {
            // LeoLog.d("BackUpFragment", "onItemClick222");
            // }

            if ((Boolean) tag) {
                // LeoLog.d("BackUpFragment", "is checkall!");
                if (app.isChecked) {
                    iv_check_backup.setTag(false);
                    // iv_check_backup
                    // .setBackgroundResource(R.drawable.check_all_selector);
                    iv_check_backup
                            .setBackgroundResource(R.drawable.app_unselect);
                    mBackupAdapter.setisAllCheck(false);
                } else {
                    // no such condition
                }
            } else {
                // LeoLog.d("BackUpFragment", "not checkall!");
                if (mBackupAdapter.checkAllIsFill(app.isChecked)) {
                    // LeoLog.d("BackUpFragment", "set checkallbutton check!");
                    iv_check_backup.setTag(true);
                    // iv_check_backup
                    // .setBackgroundResource(R.drawable.check_all_selected_selector);
                    iv_check_backup
                            .setBackgroundResource(R.drawable.app_select);
                    mBackupAdapter.setisAllCheck(true);
                }
            }

            if (app.isBackuped) {
                app.isChecked = false;
            } else {
                app.isChecked = !app.isChecked;
            }
            item.setState(app.isBackuped ? AppBackupItemView.STATE_BACKUPED
                    : app.isChecked ? AppBackupItemView.STATE_SELECTED
                            : AppBackupItemView.STATE_UNSELECTED);
        }

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
                                        mPendingDelApp.label), 0, true, false);
                        mBackupManager.deleteApp(mPendingDelApp);
                    }
                    mPendingDelApp = null;
                }
            });
        }
        mAlarmDialog.setTitle(title);
        mAlarmDialog.setContent(content);
        mAlarmDialog.show();
    }

    private void showMessageDialog(String title, String message) {
        if (mMessageDialog == null) {
            mMessageDialog = new LEOMessageDialog(mActivity);
        }

        iv_check_backup.setBackgroundResource(R.drawable.app_unselect);
        mBackupAdapter.setisAllCheck(false);
        iv_check_backup.setTag(false);
        mBackupAdapter.checkAll(false);

        mMessageDialog.setTitle(title);
        mMessageDialog.setContent(message);
        mMessageDialog.show();
    }

    public void updateDataFromApdater() {
        mBackupAdapter.updateData();

        if (mBackupAdapter.getisAllCheck()) {
            iv_check_backup.setTag(true);
        } else {
            iv_check_backup.setTag(false);
        }
        // iv_check_backup.setEnabled(mBackupAdapter.hasBackupApp());
        if (mBackupAdapter.hasBackupApp()) {
            if (mBackupAdapter.getisAllCheck()) {
                iv_check_backup
                        .setBackgroundResource(R.drawable.app_select);
            } else {
                iv_check_backup
                        .setBackgroundResource(R.drawable.app_unselect);
            }
            iv_check_backup.setEnabled(true);
        } else {

            iv_check_backup
                    .setBackgroundResource(R.drawable.app_all_backuped);

            iv_check_backup.setEnabled(false);
        }

    }
}
