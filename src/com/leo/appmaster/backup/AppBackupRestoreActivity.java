
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
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.backup.AppBackupRestoreManager.AppBackupDataListener;
import com.leo.appmaster.model.AppDetailInfo;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEOMessageDialog;
import com.leo.appmaster.ui.dialog.LEOProgressDialog;

public class AppBackupRestoreActivity extends Activity implements View.OnClickListener, OnItemClickListener, AppBackupDataListener {
    
    private View mTabContainer;
    private  TextView  mViewBackup;
    private  TextView mViewRestore;
    private View mButtonBackup;
    private ViewPager mPager;
    private ListView mBackupList;
    private ListView mRestoreList;
    private TextView mInstallText;
    private TextView mStorageText;
    private ImageView mCheckAll;
    private AppBackupAdapter mBackupAdapter;
    private AppRestoreAdapter mRestoreAdapter;
    
    private AppBackupRestoreManager mBackupManager;
    
    private LEOProgressDialog mProgressDialog;
    private LEOAlarmDialog mAlarmDialog;
    private LEOMessageDialog mMessageDialog;
    
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
        mBackupManager.onDestory();
        if(mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if(mAlarmDialog != null) {
            mAlarmDialog.dismiss();
            mAlarmDialog = null;
        }
        if(mMessageDialog != null) {
            mMessageDialog.dismiss();
            mMessageDialog = null;
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mBackupManager.checkDataUpdate();
    }
    
    private void initUI() {
        CommonTitleBar title = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        title.setTitle(R.string.app_backup);
        title.openBackView();
        
        mTabContainer = findViewById(R.id.tab_container);
        mViewBackup = (TextView)findViewById(R.id.app_backup);
        mViewBackup.setOnClickListener(this);
                
        mViewRestore = (TextView)findViewById(R.id.app_restore);
        mViewRestore.setOnClickListener(this);
        
        mPager = (ViewPager) findViewById(R.id.pager);
        
        mBackupManager = new AppBackupRestoreManager(this, this);

        LayoutInflater inflater = LayoutInflater.from(this);
        final View backupList = inflater.inflate(R.layout.view_backup_list, null);
        mInstallText = (TextView) backupList.findViewById(R.id.text_backup_install);
        mStorageText = (TextView) backupList.findViewById(R.id.text_backup_storage);
        mCheckAll = (ImageView) backupList.findViewById(R.id.check_backup);
        mCheckAll.setTag(false);
        mCheckAll.setOnClickListener(this);
        mButtonBackup = backupList.findViewById(R.id.button_backup);
        mButtonBackup.setOnClickListener(this);
        mBackupList = (ListView)backupList.findViewById(R.id.list_backup);
        mBackupList.setOnItemClickListener(this);
        mBackupAdapter = new AppBackupAdapter(mBackupManager);
        mBackupList.setAdapter(mBackupAdapter);
        
        final View restoreList = inflater.inflate(R.layout.view_restore_list, null);
        mRestoreList = (ListView)restoreList.findViewById(R.id.list_restore);
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
                    view = restoreList;
                }
                container.addView(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
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
            mViewBackup.setTextColor(getResources().getColor(R.color.tab_select_text));
            mViewRestore.setTextColor(getResources().getColor(R.color.white));
            mTabContainer.setBackgroundResource(R.drawable.stacked_tabs_l);
        } else {
            mViewBackup.setTextColor(getResources().getColor(R.color.white));
            mViewRestore.setTextColor(getResources().getColor(R.color.tab_select_text));
            mTabContainer.setBackgroundResource(R.drawable.stacked_tabs_r);
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
            int size =  items.size();
            if(size > 0) {
                showProgressDialog(getString(R.string.backuping), size, false, true);
                mBackupManager.backupApps(items);
            } else {
                Toast.makeText(this, R.string.no_application_selected, Toast.LENGTH_LONG).show();
            }
        } else if(v == mCheckAll) {
            Object tag = mCheckAll.getTag();
            if(tag instanceof Boolean) {
                boolean checkAll = !(Boolean) tag;
                mBackupAdapter.checkAll(checkAll);
                mCheckAll.setImageResource(checkAll ? R.drawable.tick_all_sel  : R.drawable.tick_all_normal);
                mCheckAll.setTag(checkAll);
            }
        } else if(v.getId() == R.id.tv_option_image) {
            
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(view instanceof AppBackupItemView) {
            AppBackupItemView item = (AppBackupItemView) view;
            AppDetailInfo app = (AppDetailInfo) item.getTag();
            if(app.isBackuped) {
                app.isChecked = false;
            } else {
                app.isChecked = !app.isChecked;
            }
            item.setState(app.isBackuped ? AppBackupItemView.STATE_BACKUPED : app.isChecked ? AppBackupItemView.STATE_SELECTED : AppBackupItemView.STATE_UNSELECTED);
        }
    }
    

    public void tryDeleteApp(AppDetailInfo app) {
        mPendingDelApp = app;
        showAlarmDialog(getString(R.string.delete), String.format(getString(R.string.query_delete), app.getAppLabel()));
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
    public void onBackupFinish(final boolean success, final int successNum, final int totalNum, final String message) {
        mHandler.post(new Runnable() {       
            @Override
            public void run() {
                if(success) {
                    showMessageDialog(getString(R.string.backup_finish),  String.format(getString(R.string.backuped_count), totalNum));
                } else {
                    Toast.makeText(AppBackupRestoreActivity.this, message, Toast.LENGTH_LONG).show();
                }
                updateDataList();
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
                    updateDataList();
                } else {
                    Toast.makeText(AppBackupRestoreActivity.this, R.string.delete_fail, Toast.LENGTH_LONG).show();
                }
                if(mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }
        });
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
    
    private void updateDataList() {
        mRestoreAdapter.updateData();
        mBackupAdapter.updateData();
        mInstallText.setText(mBackupManager.getInstalledAppSize());
        mStorageText.setText(mBackupManager.getAvaiableSize());
        mCheckAll.setImageResource(R.drawable.tick_all_normal);
        mCheckAll.setTag(false);
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
        mProgressDialog.setButtonVisiable(cancelable);
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
                        showProgressDialog(String.format(getString(R.string.deleting_app), mPendingDelApp.getAppLabel()), 0, true, false);
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
        if(mMessageDialog == null) {
            mMessageDialog = new LEOMessageDialog(this);
        }
        mMessageDialog.setTitle(title);
        mMessageDialog.setContent(message);
        mMessageDialog.show();
    }


}
