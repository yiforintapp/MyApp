
package com.leo.appmaster.appmanage;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.appmanage.business.AppBusinessManager;
import com.leo.appmaster.appmanage.view.BaseFolderFragment;
import com.leo.appmaster.appmanage.view.FolderLayer;
import com.leo.appmaster.appmanage.view.FolderView;
import com.leo.appmaster.appmanage.view.SlicingLayer;
import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.backup.AppBackupRestoreManager.AppBackupDataListener;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.AppChangeListener;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.model.AppInfo;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.model.BusinessItemInfo;
import com.leo.appmaster.model.FolderItemInfo;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoAppViewPager;
import com.leo.appmaster.ui.LeoAppViewPager.OnPageChangeListener;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.ui.LeoGridBaseAdapter;
import com.leo.appmaster.ui.PageIndicator;
import com.leo.appmaster.ui.dialog.LEOProgressDialog;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.PhoneInfoStateManager;
import com.leo.appmaster.utils.ProcessUtils;
import com.leo.appmaster.utils.TextFormater;
import com.leo.appmaster.utils.Utilities;
import com.leo.imageloader.ImageLoader;
import com.leoers.leoanalytics.LeoStat;

@SuppressLint("Override")
public class AppListActivity extends BaseFragmentActivity implements
        AppChangeListener, OnClickListener, AppBackupDataListener,
        OnPageChangeListener {

    private View mContainer;
    private View mLoadingView;
    private CommonTitleBar mTtileBar;
    private PageIndicator mPageIndicator;
    private LeoAppViewPager mViewPager;
    private View mPagerContain, mAllAppList;
    private SlicingLayer mSlicingLayer;
    private FolderLayer mFolderLayer;
    private View mSclingBgView;
    private View mCommenScilingContentView;
    private View mBackupSclingContentView;
    private View mBusinessContentView;
    private CommonSclingContentViewHolder mCommenScilingHolder;
    private BackupContentViewHolder mBackupScilingHolder;
    private BusinessAppContentViewHolder mBusinessScilingHolder;
    private LayoutInflater mInflater;
    private ViewGroup mPager1;

    private List<BaseInfo> mAllItems;
    private List<FolderItemInfo> mFolderItems;
    private List<BusinessItemInfo> mBusinessItems;
    private List<AppItemInfo> mAppDetails;
    private BaseInfo mLastSelectedInfo;
    private int mPageItemCount = 20;
    private int mCurrentPage = -1;
    private AppBackupRestoreManager mBackupManager;

    private OnItemClickListener mListItemClickListener;
    private Vector<AppItemInfo> mRestoreFolderData;
    private Vector<AppItemInfo> mCapacityFolderData;
    private Vector<AppItemInfo> mFlowFolderData;

    private static final int MSG_BACKUP_SUCCESSFUL = 1000;
    private static final int MSG_BACKUP_DELETE = 1001;

    private LEOProgressDialog mProgressDialog;
    public Handler mHandler = new Handler();
    private boolean mFromStatusbar;
    private boolean mOpenedBusinessFolder;

    // private View mBusinessFolderView;

    private class CommonSclingContentViewHolder {
        TextView installTime;
        TextView capacity;
        TextView cache;
        TextView power;
        TextView flow;
        TextView memory;
        TextView backup;
        TextView uninstall;
    }

    private class BackupContentViewHolder {
        TextView size;
        TextView version;
        TextView backupTime;
        TextView path;
        TextView restore;
        TextView delete;
    }

    private class BusinessAppContentViewHolder {
        RatingBar rating;
        TextView downloadCount;
        TextView appSize;
        TextView appDesc;
        View downloadLayout;
        ImageView googleIcon;
        View googleDownloadLayout;
        TextView highDownload;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);
        mFromStatusbar = getIntent().getBooleanExtra("from_statubar", false);
        mSlicingLayer = new SlicingLayer(this);
        mBackupManager = AppMasterApplication.getInstance().getBuckupManager();
        mBackupManager.registerBackupListener(this);
        AppLoadEngine.getInstance(this).registerAppChangeListener(this);
        initUI();
        // AM-771
        try {
            fillAppListData();
        } catch (Exception e) {

        }
    }

    @Override
    protected void onResume() {
        mBackupManager.checkDataUpdate();
        super.onResume();
        if (mFromStatusbar && !mOpenedBusinessFolder) {
            mContainer.post(new Runnable() {
                @Override
                public void run() {
                    handleItemClick(mPager1.getChildAt(0),
                            SlicingLayer.SLICING_FROM_APPLIST, false);
                    mOpenedBusinessFolder = true;
                }
            });

            SDKWrapper.addEvent(this, LeoStat.P1, "ub_newapp", "statusbar");
        } else if (AppBusinessManager.getInstance(this).hasBusinessData(
                BusinessItemInfo.CONTAIN_APPLIST)) {
            SDKWrapper.addEvent(this, LeoStat.P1, "app_rec", "home");
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {

        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState,
            PersistableBundle persistentState) {
        try {
            super.onRestoreInstanceState(savedInstanceState, persistentState);
        } catch (Exception e) {

        }
    }

    public void openSlicingLayer(View view, int from) {
        long startTime = System.currentTimeMillis();
        LeoLog.d("start slicing", "start time = " + startTime);

        if (mSlicingLayer.isAnimating() || mSlicingLayer.isSlicinged())
            return;
        mSclingBgView = mContainer;
        // mSclingBgView = mAllAppList;

        // if (!(view.getTag() instanceof AppItemInfo))
        // return;
        BaseInfo baseInfo = (BaseInfo) view.getTag();

        if (baseInfo instanceof BusinessItemInfo) {
            // TODO
            if (mBusinessContentView == null) {
                mBusinessContentView = getLayoutInflater().inflate(
                        R.layout.business_app_content_view, null);

                mBusinessScilingHolder = new BusinessAppContentViewHolder();
                mBusinessScilingHolder.rating = (RatingBar) mBusinessContentView
                        .findViewById(R.id.ratingBar1);
                mBusinessScilingHolder.downloadCount = (TextView) mBusinessContentView
                        .findViewById(R.id.app_download_hot);
                mBusinessScilingHolder.appSize = (TextView) mBusinessContentView
                        .findViewById(R.id.app_size);
                mBusinessScilingHolder.appDesc = (TextView) mBusinessContentView
                        .findViewById(R.id.app_desc);
                mBusinessScilingHolder.googleIcon = (ImageView) mBusinessContentView
                        .findViewById(R.id.google_icon);
                mBusinessScilingHolder.highDownload = (TextView) mBusinessContentView
                        .findViewById(R.id.tv_high_speed_download);
                mBusinessScilingHolder.googleDownloadLayout = mBusinessContentView
                        .findViewById(R.id.google_download);

                mBusinessScilingHolder.downloadLayout = mBusinessContentView
                        .findViewById(R.id.layout_download);
                mBusinessScilingHolder.downloadLayout.setOnClickListener(this);
            }

            BusinessItemInfo businessInfo = (BusinessItemInfo) baseInfo;
            mBusinessScilingHolder.rating.setRating(businessInfo.rating);
            mBusinessScilingHolder.downloadCount
                    .setText(businessInfo.appDownloadCount);
            mBusinessScilingHolder.appSize.setText(TextFormater
                    .getSizeFromKB(businessInfo.appSize));
            if (PhoneInfoStateManager.isGooglePlayPkg()) {
                mBusinessScilingHolder.googleIcon
                        .setImageResource(R.drawable.google_icon_selector);
                mBusinessScilingHolder.googleDownloadLayout
                        .setVisibility(View.VISIBLE);
                mBusinessScilingHolder.highDownload
                        .setVisibility(View.INVISIBLE);
            } else {
                if (businessInfo.gpPriority == 1 && AppUtil.appInstalled(this,
                                Constants.GP_PACKAGE)) {
                    mBusinessScilingHolder.googleIcon
                            .setImageResource(R.drawable.google_icon_selector);
                    mBusinessScilingHolder.googleDownloadLayout
                            .setVisibility(View.VISIBLE);
                    mBusinessScilingHolder.highDownload
                            .setVisibility(View.INVISIBLE);
                } else {
                    mBusinessScilingHolder.googleIcon
                            .setImageResource(R.drawable.highspeed_download_icon_selector);
                    mBusinessScilingHolder.googleDownloadLayout
                            .setVisibility(View.INVISIBLE);
                    mBusinessScilingHolder.highDownload.setVisibility(View.VISIBLE);
                }
            }

            mBusinessScilingHolder.appDesc.setText(businessInfo.desc);

            int contentheight = getResources().getDimensionPixelSize(
                    R.dimen.backup_scling_content_height);
            mSlicingLayer.startSlicing(view, mSclingBgView,
                    mBusinessContentView, contentheight);
//            
//            int line = mBusinessScilingHolder.appDesc.getLineCount();
//            if(line == 1) {
//                mBusinessScilingHolder.appDesc.setGravity(Gravity.RIGHT);
//            } else if(line == 2) {
//                mBusinessScilingHolder.appDesc.setGravity(Gravity.LEFT);
//            }
            
        } else {
            AppItemInfo appinfo = (AppItemInfo) baseInfo;
            if (from != BaseFolderFragment.FOLER_TYPE_BACKUP) {
                if (mCommenScilingContentView == null) {
                    mCommenScilingContentView = getLayoutInflater().inflate(
                            R.layout.comon_scling_content_view, null);

                    mCommenScilingHolder = new CommonSclingContentViewHolder();
                    mCommenScilingHolder.installTime = (TextView) mCommenScilingContentView
                            .findViewById(R.id.install_time);
                    mCommenScilingHolder.capacity = (TextView) mCommenScilingContentView
                            .findViewById(R.id.capacity);
                    mCommenScilingHolder.cache = (TextView) mCommenScilingContentView
                            .findViewById(R.id.cache);
                    mCommenScilingHolder.flow = (TextView) mCommenScilingContentView
                            .findViewById(R.id.flow);
                    mCommenScilingHolder.power = (TextView) mCommenScilingContentView
                            .findViewById(R.id.power);
                    mCommenScilingHolder.memory = (TextView) mCommenScilingContentView
                            .findViewById(R.id.memory);

                    mCommenScilingHolder.backup = (TextView) mCommenScilingContentView
                            .findViewById(R.id.backup);
                    mCommenScilingHolder.uninstall = (TextView) mCommenScilingContentView
                            .findViewById(R.id.uninstall);

                    mCommenScilingHolder.backup.setOnClickListener(this);
                    mCommenScilingHolder.uninstall.setOnClickListener(this);
                }
                appinfo = AppLoadEngine.getInstance(this).loadAppDetailInfo(
                        appinfo.packageName);

                int day = Math
                        .abs((int) ((System.currentTimeMillis() - appinfo.installTime) / (1000 * 60 * 60 * 24)));

                if (day > 10000) {
                    day /= 50;
                } else if (day > 1000) {
                    day /= 2;
                }
                mCommenScilingHolder.installTime.setText(getString(
                        R.string.install_time, day));

                mCommenScilingHolder.capacity.setText(TextFormater
                        .dataSizeFormat(appinfo.cacheInfo.total));

                mCommenScilingHolder.cache.setText(TextFormater
                        .dataSizeFormat(appinfo.cacheInfo.cacheSize));
                mCommenScilingHolder.flow.setText(TextFormater
                        .dataSizeFormat(appinfo.trafficInfo.total));
                mCommenScilingHolder.power.setText(appinfo.powerComsuPercent
                        * 100 + "%");
                mCommenScilingHolder.memory.setText(TextFormater
                        .dataSizeFormat(ProcessUtils.getAppUsedMem(this,
                                appinfo.packageName)));

                if (appinfo.isBackuped) {
                    mCommenScilingHolder.backup.setText(R.string.backuped);
                    mCommenScilingHolder.backup.setEnabled(false);
                    mCommenScilingHolder.backup
                            .setBackgroundResource(R.drawable.dlg_left_button_selector);
                } else {
                    mCommenScilingHolder.backup.setText(R.string.backup);
                    mCommenScilingHolder.backup.setEnabled(true);
                    mCommenScilingHolder.backup
                            .setBackgroundResource(R.drawable.folder_left_button_selector);
                }

                if (appinfo.systemApp) {
                    mCommenScilingHolder.uninstall.setEnabled(false);
                    mCommenScilingHolder.uninstall
                            .setBackgroundResource(R.drawable.folder_right_button_selector);
                } else {
                    mCommenScilingHolder.uninstall.setEnabled(true);
                    mCommenScilingHolder.uninstall
                            .setBackgroundResource(R.drawable.folder_right_button_selector);
                }

                int contentheight = getResources().getDimensionPixelSize(
                        R.dimen.common_scling_content_height);

                mSlicingLayer.startSlicing(view, mSclingBgView,
                        mCommenScilingContentView, contentheight);
            } else {
                if (mBackupSclingContentView == null) {
                    mBackupSclingContentView = getLayoutInflater().inflate(
                            R.layout.backup_scling_content_view, null);

                    mBackupScilingHolder = new BackupContentViewHolder();
                    mBackupScilingHolder.size = (TextView) mBackupSclingContentView
                            .findViewById(R.id.app_size);
                    mBackupScilingHolder.version = (TextView) mBackupSclingContentView
                            .findViewById(R.id.app_version);
                    mBackupScilingHolder.backupTime = (TextView) mBackupSclingContentView
                            .findViewById(R.id.backup_time);
                    mBackupScilingHolder.path = (TextView) mBackupSclingContentView
                            .findViewById(R.id.path);

                    mBackupScilingHolder.restore = (TextView) mBackupSclingContentView
                            .findViewById(R.id.restore);
                    mBackupScilingHolder.delete = (TextView) mBackupSclingContentView
                            .findViewById(R.id.delete);

                    mBackupScilingHolder.restore.setOnClickListener(this);
                    mBackupScilingHolder.delete.setOnClickListener(this);
                }

                mBackupScilingHolder.size.setText(mBackupManager
                        .getApkSize(appinfo));
                mBackupScilingHolder.version.setText(appinfo.versionName);
                SimpleDateFormat formatter = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss");
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(appinfo.backupTime);
                mBackupScilingHolder.backupTime.setText(formatter
                        .format(calendar.getTime()));
                mBackupScilingHolder.path.setText(mBackupManager
                        .getDisplayPath());

                int contentheight = getResources().getDimensionPixelSize(
                        R.dimen.backup_scling_content_height);
                mSlicingLayer.startSlicing(view, mSclingBgView,
                        mBackupSclingContentView, contentheight);
            }
        }
        LeoLog.d("end slicing", " end time = " + System.currentTimeMillis());
        LeoLog.d("end slicing", " spend = "
                + (System.currentTimeMillis() - startTime));
    }

    @Override
    public void onBackPressed() {

        if (mSlicingLayer.isSlicinged()) {
            mSlicingLayer.closeSlicing();
            return;
        }

        if (mFolderLayer.isFolderOpened()) {
            mFolderLayer.closeFloder();
            return;
        }
        if (mFromStatusbar) {
            Intent intent = new Intent(this, HomeActivity.class);
            this.startActivity(intent);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBackupManager.unregisterBackupListener(this);
        AppLoadEngine.getInstance(this).unregisterAppChangeListener(this);
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }

        // optimize imagelaoder,here we remove cache app icon in memory
        ImageLoader.getInstance().clearMemoryCache();

    }

    private void initUI() {
        mInflater = getLayoutInflater();
        mListItemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                    long arg3) {
                handleItemClick(view, SlicingLayer.SLICING_FROM_APPLIST, true);
            }
        };

        FolderView folderView = (FolderView) findViewById(R.id.folderView);
        mFolderLayer = new FolderLayer(this, folderView);
        mContainer = findViewById(R.id.container);
        mLoadingView = findViewById(R.id.rl_loading);

        mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mTtileBar.setTitle(R.string.app_backup_uninstall);
        // mTtileBar.openBackView();
        mTtileBar.setBackViewListener(this);
        mTtileBar.setOptionTextVisibility(View.INVISIBLE);

        mAllAppList = findViewById(R.id.applist);
        mPagerContain = findViewById(R.id.layout_pager_container);
        mPageIndicator = (PageIndicator) findViewById(R.id.indicator);
        mViewPager = (LeoAppViewPager) findViewById(R.id.pager);
    }

    public FolderLayer getFolderLayer() {
        return mFolderLayer;
    }

    public void fillAppListData() {
        mPageItemCount = getResources()
                .getInteger(R.integer.applist_cell_count);
        if (mSlicingLayer != null && mSlicingLayer.isSlicinged()) {
            mSlicingLayer.closeSlicing();
        }

        // load apps
        mAppDetails = new ArrayList<AppItemInfo>();
        List<AppItemInfo> list = AppLoadEngine.getInstance(this)
                .getAllPkgInfo();
        for (AppItemInfo appItemInfo : list) {
            if (appItemInfo.packageName.equals(this.getPackageName()))
                continue;

            if (!appItemInfo.systemApp)
                mAppDetails.add(appItemInfo);
        }
        // load all folder items
        getFolderData();

        mAllItems = new ArrayList<BaseInfo>();
        // first, add four folders
        mFolderItems = new ArrayList<FolderItemInfo>();
        loadFolderItems();
        mAllItems.addAll(mFolderItems);

        // filter loacal app
        checkInstalledFormBusinessApp();

        // second, add business items
        mBusinessItems = loadBusinessData();
        if (mBusinessItems != null) {
            mAllItems.addAll(mBusinessItems);
        }

        // third, add all local apps
        mAllItems.addAll(mAppDetails);

        // data load finished
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.INVISIBLE);
        }
        int pageCount = (int) Math.ceil(((double) mAllItems.size())
                / mPageItemCount);

        int i;
        ArrayList<View> viewList = new ArrayList<View>();
        for (i = 0; i < pageCount; i++) {
            GridView gridView = (GridView) mInflater.inflate(
                    R.layout.grid_page_item, mViewPager, false);
            if (i == pageCount - 1) {
                gridView.setAdapter(new DataAdapter(mAllItems, i
                        * mPageItemCount, mAllItems.size() - 1));
            } else {
                gridView.setAdapter(new DataAdapter(mAllItems, i
                        * mPageItemCount, (i + 1) * mPageItemCount - 1));
            }
            gridView.setOnItemClickListener(mListItemClickListener);
            viewList.add(gridView);
            if (i == 0) {
                mPager1 = gridView;
            }
        }
        if (mCurrentPage != -1) {
            if ((pageCount - 1) < mCurrentPage) {
                mCurrentPage = pageCount - 1;
                if (mCurrentPage < 0) {
                    mCurrentPage = 0;
                }
            }
        } else {
            mCurrentPage = 0;
        }
        if (mViewPager != null) {
            mViewPager.setAdapter(new DataPagerAdapter(viewList));
            mPageIndicator.setViewPager(mViewPager);
            mViewPager.setCurrentItem(mCurrentPage);
            mPageIndicator.setOnPageChangeListener(this);
            mPagerContain.setVisibility(View.VISIBLE);
        }
    }

    /**
     * we should judge load sync or not
     * 
     * @return
     */
    private List<BusinessItemInfo> loadBusinessData() {
        List<BusinessItemInfo> list = getRecommendData(BusinessItemInfo.CONTAIN_APPLIST);

        if (list == null || list.size() == 0)
            return null;
        List<BusinessItemInfo> resault;
        resault = list.subList(0, list.size() > 4 ? 4 : list.size());
        return resault;
    }

    private void loadFolderItems() {
        FolderItemInfo folder = null;
        // add business app folder
        folder = new FolderItemInfo();
        folder.type = BaseInfo.ITEM_TYPE_FOLDER;
        folder.folderType = FolderItemInfo.FOLDER_BUSINESS_APP;
        folder.icon = Utilities.getFolderScalePicture(this, null,
                FolderItemInfo.FOLDER_BUSINESS_APP);
        folder.label = getString(R.string.folder_recommend);
        mFolderItems.add(folder);
        // add flow sort folder
        folder = new FolderItemInfo();
        folder.type = BaseInfo.ITEM_TYPE_FOLDER;
        folder.folderType = FolderItemInfo.FOLDER_FLOW_SORT;
        folder.icon = Utilities.getFolderScalePicture(this, mFlowFolderData,
                FolderItemInfo.FOLDER_FLOW_SORT);
        folder.label = getString(R.string.folder_sort_flow);
        mFolderItems.add(folder);
        // add capacity folder
        folder = new FolderItemInfo();
        folder.type = BaseInfo.ITEM_TYPE_FOLDER;
        folder.folderType = FolderItemInfo.FOLDER_CAPACITY_SORT;
        folder.icon = Utilities.getFolderScalePicture(this,
                mCapacityFolderData, FolderItemInfo.FOLDER_CAPACITY_SORT);
        folder.label = getString(R.string.folder_sort_capacity);
        mFolderItems.add(folder);
        // add restore folder
        folder = new FolderItemInfo();
        folder.type = BaseInfo.ITEM_TYPE_FOLDER;
        folder.folderType = FolderItemInfo.FOLDER_BACKUP_RESTORE;
        folder.icon = Utilities.getFolderScalePicture(this, mRestoreFolderData,
                FolderItemInfo.FOLDER_BACKUP_RESTORE);
        folder.label = getString(R.string.folder_backup_restore);
        mFolderItems.add(folder);
    }

    private void animateItem(final View view, final int from,
            final boolean event) {
        AnimatorSet as = new AnimatorSet();
        as.setDuration(150);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f,
                0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f,
                0.8f, 1f);
        as.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator arg0) {
                switch (mLastSelectedInfo.type) {
                    case BaseInfo.ITEM_TYPE_NORMAL_APP:
                        SDKWrapper.addEvent(AppListActivity.this, LeoStat.P1,
                                "ub_listpress", "app");
                        openSlicingLayer(view, from);
                        break;
                    case BaseInfo.ITEM_TYPE_FOLDER:
                        if (!mFolderLayer.isFolderOpened()) {

                            FolderItemInfo folderInfo = (FolderItemInfo) mLastSelectedInfo;
                            fillFolder();
                            mFolderLayer.openFolderView(folderInfo.folderType,
                                    view, mAllAppList);
                            if (event) {
                                SDKWrapper.addEvent(AppListActivity.this,
                                        LeoStat.P1, "ub_listpress", "folder"
                                                + (folderInfo.folderType + 1));
                                if (folderInfo.folderType == FolderItemInfo.FOLDER_BACKUP_RESTORE) {
                                    SDKWrapper.addEvent(AppListActivity.this,
                                            LeoStat.P1, "ub_restore", "list");
                                } else if (folderInfo.folderType == FolderItemInfo.FOLDER_FLOW_SORT) {
                                    SDKWrapper.addEvent(AppListActivity.this,
                                            LeoStat.P1, "ub_liuliang", "list");
                                } else if (folderInfo.folderType == FolderItemInfo.FOLDER_CAPACITY_SORT) {
                                    SDKWrapper.addEvent(AppListActivity.this,
                                            LeoStat.P1, "ub_space", "list");
                                } else if (folderInfo.folderType == FolderItemInfo.FOLDER_BUSINESS_APP) {
                                    SDKWrapper.addEvent(AppListActivity.this,
                                            LeoStat.P1, "ub_newapp", "list");
                                }
                            }
                        }
                        break;
                    case BaseInfo.ITEM_TYPE_BUSINESS_APP:
                        openSlicingLayer(view, from);
                        break;

                    default:
                        break;
                }

            }
        });
        as.playTogether(scaleX, scaleY);
        as.start();
    }

    private class DataPagerAdapter extends PagerAdapter {
        List<View> pagerList;

        public DataPagerAdapter(ArrayList<View> viewList) {
            pagerList = viewList;
        }

        @Override
        public int getCount() {
            return pagerList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(pagerList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = pagerList.get(position);
            container.addView(view);
            return view;
        }
    }

    private class DataAdapter extends BaseAdapter implements LeoGridBaseAdapter {

        int startLoc;
        int endLoc;

        public DataAdapter(List<BaseInfo> appDetails, int start, int end) {
            super();
            startLoc = start;
            endLoc = end;
        }

        @Override
        public int getCount() {
            return endLoc - startLoc + 1;
        }

        @Override
        public Object getItem(int position) {
            return mAppDetails.get(startLoc + position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.app_item, null);
            }

            ImageView imageView = (ImageView) convertView
                    .findViewById(R.id.iv_app_icon);
            TextView textView = (TextView) convertView
                    .findViewById(R.id.tv_app_name);

            BaseInfo info = mAllItems.get(startLoc + position);
            imageView.setImageDrawable(info.icon);
            textView.setText(info.label);
            convertView.setTag(info);
            return convertView;
        }

        @Override
        public void reorderItems(int oldPosition, int newPosition) {
        }

        @Override
        public void setHideItem(int hidePosition) {
        }

        @Override
        public void removeItem(int position) {
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backup:
                if (mLastSelectedInfo instanceof AppItemInfo) {
                    AppItemInfo pending = (AppItemInfo) mLastSelectedInfo;
                    showProgressDialog(getString(R.string.button_backup),
                            String.format(getString(R.string.backuping),
                                    pending.label), 100, false, true, false);
                    mBackupManager.backupApp(pending);
                }

                SDKWrapper.addEvent(this, LeoStat.P1, "ub_backup", "list");
                break;
            case R.id.restore:
                if (mLastSelectedInfo instanceof AppItemInfo) {
                    AppItemInfo pending = (AppItemInfo) mLastSelectedInfo;
                    mBackupManager.restoreApp(this, pending);
                }

                break;
            case R.id.delete:
                if (mLastSelectedInfo instanceof AppItemInfo) {
                    AppItemInfo pending = (AppItemInfo) mLastSelectedInfo;
                    showProgressDialog(getString(R.string.delete), String.format(
                            getString(R.string.deleting_app), pending.label), 0,
                            true, false, false);
                    mBackupManager.deleteApp(pending);
                }
                break;
            case R.id.uninstall:
                if (mLastSelectedInfo instanceof AppItemInfo) {
                    AppItemInfo pending = (AppItemInfo) mLastSelectedInfo;
                    AppUtil.uninstallApp(this, pending.packageName);
                }
                SDKWrapper.addEvent(this, LeoStat.P1, "ub_uninstall", "list");
                break;
            case R.id.layout_title_back:
                if (!mSlicingLayer.isSlicinged() && !mFolderLayer.isFolderOpened()) {
                    finish();
                }

                if (mFromStatusbar) {
                    Intent intent = new Intent(this, HomeActivity.class);
                    this.startActivity(intent);
                }
                break;

            case R.id.layout_download:

                BusinessItemInfo bif = (BusinessItemInfo) mLastSelectedInfo;
                if (PhoneInfoStateManager.isGooglePlayPkg()) {
                    if (AppUtil.appInstalled(AppListActivity.this,
                            Constants.GP_PACKAGE)) {
                        AppUtil.downloadFromGp(AppListActivity.this,
                                bif.packageName);
                    } else {
                        AppUtil.downloadFromBrowser(AppListActivity.this,
                                bif.appDownloadUrl);
                    }
                } else {
                    if (bif.gpPriority == 1) {
                        if (AppUtil.appInstalled(AppListActivity.this,
                                Constants.GP_PACKAGE)) {
                            AppUtil.downloadFromGp(AppListActivity.this,
                                    bif.packageName);
                        } else {
                            AppUtil.downloadFromBrowser(AppListActivity.this,
                                    bif.appDownloadUrl);
                        }
                    } else {
                        AppUtil.downloadFromBrowser(AppListActivity.this,
                                bif.appDownloadUrl);
                    }
                }

                SDKWrapper.addEvent(AppListActivity.this, LeoStat.P1, "app_cli_pn",
                        bif.packageName);

                if (bif.containType == BusinessItemInfo.CONTAIN_APPLIST) {
                    SDKWrapper.addEvent(AppListActivity.this, LeoStat.P1,
                            "app_cli_ps", "home");
                } else if (bif.containType == BusinessItemInfo.CONTAIN_FLOW_SORT) {
                    SDKWrapper.addEvent(AppListActivity.this, LeoStat.P1,
                            "app_cli_ps", "flow");
                } else if (bif.containType == BusinessItemInfo.CONTAIN_CAPACITY_SORT) {
                    SDKWrapper.addEvent(AppListActivity.this, LeoStat.P1,
                            "app_cli_ps", "capacity");
                } else if (bif.containType == BusinessItemInfo.CONTAIN_BUSINESS_FOLDER) {
                    SDKWrapper.addEvent(AppListActivity.this, LeoStat.P1,
                            "app_cli_ps", "new");
                }
                break;

            default:
                break;
        }
    }

    public void handleItemClick(View view, int from, boolean event) {
        if (view == null) {
            return;
        }
        if ((mSlicingLayer != null && mSlicingLayer.isAnimating())
                || (mFolderLayer != null && mFolderLayer.isAnimating())) {
            return;
        }
        mLastSelectedInfo = (BaseInfo) view.getTag();
        animateItem(view, from, event);
    }

    private void getFolderData() {
        List<AppItemInfo> tempList = new ArrayList<AppItemInfo>(mAppDetails);

        // load folw sort data
        Collections.sort(tempList, new FlowComparator());
        mFlowFolderData = new Vector<AppItemInfo>(tempList.subList(0, tempList
                .size() < mPageItemCount ? tempList.size() : mPageItemCount));

        // load capacity sort data
        tempList = new ArrayList<AppItemInfo>(mAppDetails);
        Collections.sort(tempList, new CapacityComparator());
        mCapacityFolderData = new Vector<AppItemInfo>(tempList.subList(0,
                tempList.size() < mPageItemCount ? tempList.size()
                        : mPageItemCount));

        // load restore sort data
        ArrayList<AppItemInfo> temp = mBackupManager.getRestoreList();
        Collections.sort(temp, new BackupItemComparator());
        mRestoreFolderData = new Vector<AppItemInfo>(temp.subList(0,
                temp.size()));
    }

    private void fillFolder() {
        if (mFolderLayer != null) {
            // fill restore folder
            Collections.sort(mRestoreFolderData, new BackupItemComparator());
            mFolderLayer.updateFolderData(FolderItemInfo.FOLDER_BACKUP_RESTORE,
                    mRestoreFolderData, null);

            // fill flow folder
            int contentMaxCount = mPageItemCount;
            List<BusinessItemInfo> flowDataReccommendData = getRecommendData(BusinessItemInfo.CONTAIN_FLOW_SORT);
            int flowBusinessCount = flowDataReccommendData.size();
            contentMaxCount = flowBusinessCount > 0 ? contentMaxCount - 4
                    : contentMaxCount;
            mFolderLayer
                    .updateFolderData(
                            FolderItemInfo.FOLDER_FLOW_SORT,
                            mFlowFolderData.subList(
                                    0,
                                    mFlowFolderData.size() < contentMaxCount ? mFlowFolderData
                                            .size() : contentMaxCount),
                            flowDataReccommendData.subList(0,
                                    flowBusinessCount <= 4 ? flowBusinessCount
                                            : 4));

            // fill capacity folder
            contentMaxCount = mPageItemCount;
            List<BusinessItemInfo> capacityReccommendData = getRecommendData(BusinessItemInfo.CONTAIN_CAPACITY_SORT);
            int capacityBusinessCount = capacityReccommendData.size();
            contentMaxCount = capacityBusinessCount > 0 ? contentMaxCount - 4
                    : contentMaxCount;
            mFolderLayer
                    .updateFolderData(
                            FolderItemInfo.FOLDER_CAPACITY_SORT,
                            mCapacityFolderData.subList(
                                    0,
                                    mCapacityFolderData.size() <= contentMaxCount ? mCapacityFolderData
                                            .size()
                                            : contentMaxCount),
                            capacityReccommendData
                                    .subList(
                                            0,
                                            capacityBusinessCount <= 4 ? capacityBusinessCount
                                                    : 4));
        }
    }

    private void checkInstalledFormBusinessApp() {
        Vector<BusinessItemInfo> businessDatas = AppBusinessManager
                .getInstance(this).getBusinessData();
        if (businessDatas != null) {
            for (BusinessItemInfo businessItemInfo : businessDatas) {
                boolean installed = false;
                for (AppItemInfo info : mAppDetails) {
                    if (businessItemInfo.packageName.equals(info.packageName)) {
                        installed = true;
                        break;
                    }
                }
                businessItemInfo.installed = installed;
            }
        }

    }

    private List<BusinessItemInfo> getRecommendData(int containerId) {
        Vector<BusinessItemInfo> businessDatas = AppBusinessManager
                .getInstance(this).getBusinessData();
        List<BusinessItemInfo> list = new ArrayList<BusinessItemInfo>();
        for (BusinessItemInfo businessItemInfo : businessDatas) {
            if (businessItemInfo.installed)
                continue;
            if (businessItemInfo.containType == containerId) {
                if (containerId == BusinessItemInfo.CONTAIN_APPLIST) {
                    if (businessItemInfo.iconLoaded) {
                        list.add(businessItemInfo);
                    }
                } else {
                    list.add(businessItemInfo);
                }
            }
        }
        // if (containerId == BusinessItemInfo.CONTAIN_APPLIST) {
        // SDKWrapper.addEvent(this, LeoStat.P1, "app_rec", "home");
        // } else if (containerId == BusinessItemInfo.CONTAIN_FLOW_SORT) {
        // SDKWrapper.addEvent(this, LeoStat.P1, "app_rec", "flow");
        // } else if (containerId == BusinessItemInfo.CONTAIN_CAPACITY_SORT) {
        // SDKWrapper.addEvent(this, LeoStat.P1, "app_rec", "capacity");
        // } else if (containerId == BusinessItemInfo.CONTAIN_BUSINESS_FOLDER) {
        // SDKWrapper.addEvent(this, LeoStat.P1, "app_rec", "new");
        // }

        return list;
    }

    @Override
    public void onAppChanged(ArrayList<AppItemInfo> changes, int type) {
        for (AppItemInfo change : changes) {
            for (AppItemInfo saved : mBackupManager.getRestoreList()) {
                if (saved.packageName.equals(change.packageName)
                        && saved.versionCode == change.versionCode) {
                    change.isBackuped = true;
                    break;
                }
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    fillAppListData();
                    fillFolder();
                } catch (Exception e) {

                }
            }
        });

    }

    @Override
    public void onDataReady() {
    }

    @Override
    public void onDataUpdate() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateRestoreData();
            }
        });
    }

    @Override
    public void onBackupProcessChanged(final int doneNum, final int totalNum,
            final String currentApp) {
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
    public void onBackupFinish(final boolean success, int successNum,
            int totalNum, final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (success) {
                    Toast.makeText(AppListActivity.this,
                            R.string.backup_finish, 0).show();
                    if (mCommenScilingHolder != null
                            && mCommenScilingHolder.backup != null) {
                        mCommenScilingHolder.backup.setText(R.string.backuped);
                        mCommenScilingHolder.backup.setEnabled(false);
                        mCommenScilingHolder.backup
                                .setBackgroundResource(R.drawable.dlg_left_button_selector);
                    }
                    updateRestoreData();
                } else {
                    Toast.makeText(AppListActivity.this, message,
                            Toast.LENGTH_LONG).show();
                }

                if (mProgressDialog != null) {
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
                if (success) {
                    Toast.makeText(AppListActivity.this,
                            R.string.delete_successfully, 1).show();
                    if (mSlicingLayer != null && mSlicingLayer.isSlicinged()) {
                        mSlicingLayer.closeSlicing();
                    }
                    updateRestoreData();
                } else {
                    Toast.makeText(AppListActivity.this, R.string.delete_fail,
                            Toast.LENGTH_LONG).show();
                }
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    private void updateRestoreData() {
        // load restore sort data
        ArrayList<AppItemInfo> temp = mBackupManager.getRestoreList();
        // mRestoreFolderData = temp.subList(0, temp.size());
        mRestoreFolderData = new Vector<AppItemInfo>(temp.subList(0,
                temp.size()));
        Collections.sort(mRestoreFolderData, new BackupItemComparator());
        if (mFolderLayer != null) {
            mFolderLayer.updateFolderData(FolderItemInfo.FOLDER_BACKUP_RESTORE,
                    mRestoreFolderData, null);
        }
        // update folder icon
        for (FolderItemInfo restore : mFolderItems) {
            if (restore.folderType == FolderItemInfo.FOLDER_BACKUP_RESTORE) {
                restore.icon = Utilities.getFolderScalePicture(this,
                        mRestoreFolderData,
                        FolderItemInfo.FOLDER_BACKUP_RESTORE);
                if (mViewPager != null) {
                    View v = mViewPager.getChildAt(0);
                    if (v instanceof GridView) {
                        GridView grid = (GridView) mViewPager.getChildAt(0);
                        ListAdapter adapter = grid.getAdapter();
                        if (adapter instanceof DataAdapter) {
                            grid.setAdapter(adapter);
                        }
                    }
                }
                break;
            }
        }

    }

    private void showProgressDialog(String title, String message, int max,
            boolean indeterminate, boolean cancelable, boolean state) {
        if (mProgressDialog == null) {
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
        mProgressDialog.setStateTextVisiable(state);
        mProgressDialog.setMax(max);
        mProgressDialog.setProgress(0);
        mProgressDialog.setMessage(message);
        mProgressDialog.setTitle(title);
        mProgressDialog.show();
    }

    public static class BackupItemComparator implements Comparator<AppItemInfo> {

        @Override
        public int compare(AppItemInfo lhs, AppItemInfo rhs) {
            if (lhs.backupTime > rhs.backupTime) {
                return -1;
            } else if (lhs.backupTime < rhs.backupTime) {
                return 1;
            }
            return Collator.getInstance().compare(trimString(lhs.label),
                    trimString(rhs.label));
        }

        private String trimString(String s) {
            return s.replaceAll("\u00A0", "").trim();
        }
    }

    public static class FlowComparator implements Comparator<AppItemInfo> {
        @Override
        public int compare(AppItemInfo lhs, AppItemInfo rhs) {
            if (lhs.trafficInfo.total > rhs.trafficInfo.total) {
                return -1;
            } else if (lhs.trafficInfo.total < rhs.trafficInfo.total) {
                return 1;
            } else {
                return Collator.getInstance().compare(trimString(lhs.label),
                        trimString(rhs.label));
            }

        }

        private String trimString(String s) {
            return s.replaceAll("\u00A0", "").trim();
        }
    }

    public static class CapacityComparator implements Comparator<AppItemInfo> {
        @Override
        public int compare(AppItemInfo lhs, AppItemInfo rhs) {
            if (lhs.cacheInfo.total > rhs.cacheInfo.total) {
                return -1;
            } else if (lhs.cacheInfo.total < rhs.cacheInfo.total) {
                return 1;
            } else {
                return Collator.getInstance().compare(trimString(lhs.label),
                        trimString(rhs.label));
            }

        }

        private String trimString(String s) {
            return s.replaceAll("\u00A0", "").trim();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
            int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        mCurrentPage = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
