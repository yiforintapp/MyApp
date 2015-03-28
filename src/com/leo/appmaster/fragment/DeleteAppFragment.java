
package com.leo.appmaster.fragment;

import android.os.Handler;
import android.widget.ListView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.backup.AppBackupRestoreManager.AppBackupDataListener;
import com.leo.appmaster.backup.AppDeleteAdapter;

public class DeleteAppFragment extends BaseFragment implements AppBackupDataListener {
    private Handler mHandler = new Handler();
    private TextView tv_delete_install;
    private TextView tv_delete_storage;
    private ListView lv_app_manager;
    private String mContent;

    private AppBackupRestoreManager mDeleteManager;
    private AppDeleteAdapter mDeleteAdapter;
    
    @Override
    protected int layoutResourceId() {
        return R.layout.app_delete_list;
    }

    @Override
    protected void onInitUI() {
        tv_delete_install = (TextView) findViewById(R.id.tv_delete_install);
        tv_delete_storage = (TextView) findViewById(R.id.tv_delete_storage);
        lv_app_manager = (ListView) findViewById(R.id.lv_app_manager);
        
        mDeleteManager = AppMasterApplication.getInstance().getBuckupManager();
        mDeleteManager.registerBackupListener(this);
        
        mDeleteAdapter = new AppDeleteAdapter(mDeleteManager,getActivity());
        lv_app_manager.setAdapter(mDeleteAdapter);
        
        
        mDeleteManager.prepareDate_delete();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    // useless method
    public void setContent(String content) {
        mContent = content;
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
        tv_delete_install.setText(mDeleteManager.getInstalledAppSize());
        tv_delete_storage.setText(mDeleteManager.getAvaiableSizeString());
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        mDeleteManager.unregisterBackupListener(this);
    }

    @Override
    public void onDataUpdate() {
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
}
