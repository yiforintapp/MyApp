
package com.leo.appmaster.backup;

import java.util.ArrayList;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.leo.appmaster.R;
import com.leo.appmaster.backup.AppBackupRestoreManager.AppBackupDataListener;
import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEOProgressDialog;

public class AppBackupRestoreActivity extends Activity implements View.OnClickListener, OnItemClickListener, AppBackupDataListener {
    
    
    private  View mViewBackup;
    private  View mViewRestore;
    private View mButtonBackup;
    private ViewPager mPager;
    private ListView mBackupList;
    private ListView mRestoreList;
    private AppBackupAdapter mBackupAdapter;
    private AppRestoreAdapter mRestoreAdapter;
    
    private AppBackupRestoreManager mBackupManager;
    
    private LEOProgressDialog mProgressDialog;
    private LEOAlarmDialog mAlarmDialog;
    
    private AppDetailInfo mPendingDelApp;
    
    private Handler mHandler = new Handler();
        
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_restore);
        initUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if(mAlarmDialog != null) {
            mAlarmDialog.dismiss();
            mAlarmDialog = null;
        }
    }
    
    private void initUI() {
        CommonTitleBar title = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        title.setTitle(R.string.app_backup);
        title.openBackView();
        
        mViewBackup = findViewById(R.id.app_backup);
        mViewBackup.setOnClickListener(this);
        
        mViewRestore = findViewById(R.id.app_restore);
        mViewRestore.setOnClickListener(this);
        
        mPager = (ViewPager) findViewById(R.id.pager);
        
        mBackupManager = new AppBackupRestoreManager(this, this);

        LayoutInflater inflater = LayoutInflater.from(this);
        final View backupList = inflater.inflate(R.layout.view_backup_list, null);
        mButtonBackup = backupList.findViewById(R.id.button_backup);
        mButtonBackup.setOnClickListener(this);
        mBackupList = (ListView)backupList.findViewById(R.id.list_backup);
        mBackupList.setOnItemClickListener(this);
        mBackupAdapter = new AppBackupAdapter(mBackupManager);
        mBackupList.setAdapter(mBackupAdapter);
        
        mRestoreList = new ListView(this);
        mRestoreAdapter = new AppRestoreAdapter(mBackupManager);
        mRestoreList.setAdapter(mRestoreAdapter);
        
        mPager.setAdapter(new PagerAdapter() {          
            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }
            
            @Override
            public int getCount() {
                return 2;
            }
            
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View view = null;
                if (position == 0) {
                    view = backupList;
                } else {
                    view = mRestoreList;
                }
                container.addView(view, 0);
                return view;
            }
        });
        
        mPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                onPageChange(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
        onPageChange(0);
        
        mBackupManager.prepareDate();
    }
    
    private void onPageChange(int page) {
        if (page == 0) {
            mBackupList.setBackgroundResource(R.color.tab_select);
            mRestoreList.setBackgroundResource(R.color.tab_unselect);
        } else {
            mBackupList.setBackgroundResource(R.color.tab_unselect);
            mRestoreList.setBackgroundResource(R.color.tab_select);
        }
    }
    
    public AppBackupRestoreManager getBackupManager() {
        return mBackupManager;
    }

    @Override
    public void onClick(View v) {
        if(v == mViewBackup) {
            mPager.setCurrentItem(0, true);
        } else if(v == mViewRestore) {
            mPager.setCurrentItem(1, true);
        } else if(v == mButtonBackup) {
            ArrayList<AppDetailInfo> items = mBackupAdapter.getSelectedItems();
            showProgressDialog("Backuping...", items.size(), false, true);
            mBackupManager.backupApps(items);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(view instanceof AppBackupItemView) {
            AppBackupItemView item = (AppBackupItemView) view;
            AppDetailInfo app = (AppDetailInfo) item.getTag();
            app.isChecked = !app.isChecked;
            item.setState(app.isBackuped ? AppBackupItemView.STATE_BACKUPED : app.isChecked ? AppBackupItemView.STATE_SELECTED : AppBackupItemView.STATE_UNSELECTED);
        }
    }
    

    public void tryDeleteApp(AppDetailInfo app) {
        mPendingDelApp = app;
        showAlarmDialog("Delete", "Are you sure to delete: " + app.getAppLabel());
    }

    @Override
    public void onDataReady() {
        mHandler.post(new Runnable() {       
            @Override
            public void run() {
               mBackupAdapter.updateData();
               mRestoreAdapter.updateData();
            }
        });
    }

    @Override
    public void onBackupProcessChanged(final int doneNum, final int totalNum) {
        mHandler.post(new Runnable() {       
            @Override
            public void run() {
                if(mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.setProgress(doneNum);
                }
            }
        });
    }

    @Override
    public void onBackupFinish(final boolean success, final int successNum, final int totalNum, String message) {
        mHandler.post(new Runnable() {       
            @Override
            public void run() {
               mBackupAdapter.updateData();
               mRestoreAdapter.updateData();
               if(mProgressDialog != null ) {
                   mProgressDialog.dismiss();
               }
            }
        });
    }

    @Override
    public void onApkDeleted(final boolean success) {
        mHandler.post(new Runnable() {       
            @Override
            public void run() {
                if(success) {
                    mRestoreAdapter.updateData();
                    mBackupAdapter.updateData();
                }
                if(mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }
        });
    }
    

    
    private void showProgressDialog(String message, int max, boolean indeterminate, boolean cancelable) {
        if(mProgressDialog == null) {
            mProgressDialog = new LEOProgressDialog(this);
            mProgressDialog.setOnCancelListener(new OnCancelListener() {            
                @Override
                public void onCancel(DialogInterface dialog) {
                    mBackupManager.cancelBackup();
                }
            });
        }
        mProgressDialog.setCancelable(cancelable);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(indeterminate);
        mProgressDialog.setMax(max);
        mProgressDialog.setProgress(0);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }
    

    private void showAlarmDialog(String title, String content) {
        if(mAlarmDialog == null) {
            mAlarmDialog = new LEOAlarmDialog(this);
            mAlarmDialog.setOnClickListener(new OnDiaogClickListener() {           
                @Override
                public void onClick(int which) {
                    if(which == 1 && mPendingDelApp != null) {
                        showProgressDialog("Deleting: " + mPendingDelApp.getAppLabel(), 0, true, false);
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


}
