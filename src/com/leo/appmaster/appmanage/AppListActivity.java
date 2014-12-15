package com.leo.appmaster.appmanage;

import java.lang.ref.WeakReference;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
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
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.model.BusinessItemInfo;
import com.leo.appmaster.model.FolderItemInfo;
import com.leo.appmaster.sdk.BaseFragmentActivity;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.LeoGridBaseAdapter;
import com.leo.appmaster.ui.LeoAppViewPager;
import com.leo.appmaster.ui.PageIndicator;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.AppwallHttpUtil;
import com.leo.appmaster.utils.ProcessUtils;
import com.leo.appmaster.utils.TextFormater;
import com.leo.appmaster.utils.Utilities;

public class AppListActivity extends BaseFragmentActivity implements
		AppChangeListener, OnClickListener, AppBackupDataListener {

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
	private CommonSclingContentViewHolder mCommenScilingHolder;
	private BackupContentViewHolder mBackupScilingHolder;
	private LayoutInflater mInflater;

	private List<BaseInfo> mAllItems;
	private List<BaseInfo> mFolderItems;
	private List<BusinessItemInfo> mBusinessItems;
	private List<AppItemInfo> mAppDetails;
	private BaseInfo mLastSelectedInfo;
	private int pageItemCount = 20;
	private AppBackupRestoreManager mBackupManager;
	private EventHandler mHandler;

	private OnItemClickListener mListItemClickListener;
	private List<AppItemInfo> mRestoreFolderData;
	private List<AppItemInfo> mCapacityFolderData;
	private List<AppItemInfo> mFlowFolderData;

	private static final int MSG_BACKUP_SUCCESSFUL = 1000;

	private static class CommonSclingContentViewHolder {
		TextView capacity;
		TextView cache;
		TextView power;
		TextView flow;
		TextView memory;
		TextView backup;
		TextView uninstall;
	}

	private static class BackupContentViewHolder {
		TextView size;
		TextView version;
		TextView backupTime;
		TextView path;
		TextView restore;
		TextView delete;
	}

	private static class EventHandler extends Handler {
		WeakReference<AppListActivity> appListActivityReference;

		public EventHandler(AppListActivity activity) {
			appListActivityReference = new WeakReference<AppListActivity>(
					activity);
		}

		@Override
		public void handleMessage(Message msg) {
			AppListActivity activity = appListActivityReference.get();
			switch (msg.what) {
			case MSG_BACKUP_SUCCESSFUL:
				if (activity != null) {
					Toast.makeText(activity, "备份成功", 0).show();
					activity.mCommenScilingHolder.backup
							.setText(R.string.backuped);
					activity.mCommenScilingHolder.backup.setEnabled(false);
					activity.mCommenScilingHolder.backup
							.setBackgroundResource(R.drawable.dlg_left_button_selector);
				}
				break;

			default:
				break;
			}

		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_manager);
		mSlicingLayer = new SlicingLayer(this);
		mBackupManager = AppMasterApplication.getInstance().getBuckupManager();
		mBackupManager.registerBackupListener(this);
		AppLoadEngine.getInstance(this).registerAppChangeListener(this);
		mHandler = new EventHandler(this);
		intiUI();
		fillData();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public void openSlicingLayer(View view, int from) {
		mSclingBgView = mContainer;
		AppItemInfo appinfo = (AppItemInfo) view.getTag();
		if (from != BaseFolderFragment.FOLER_TYPE_BACKUP) {
			if (mCommenScilingContentView == null) {
				mCommenScilingContentView = getLayoutInflater().inflate(
						R.layout.comon_scling_content_view, null);

				mCommenScilingHolder = new CommonSclingContentViewHolder();
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

			mCommenScilingHolder.capacity.setText(TextFormater
					.dataSizeFormat(appinfo.cacheInfo.codeSize
							+ appinfo.cacheInfo.codeSize));
			mCommenScilingHolder.cache.setText(TextFormater
					.dataSizeFormat(appinfo.cacheInfo.cacheSize));
			mCommenScilingHolder.flow.setText(TextFormater
					.dataSizeFormat(appinfo.trafficInfo.mTotal));
			mCommenScilingHolder.power.setText(appinfo.powerComsuPercent * 100
					+ "%");
			mCommenScilingHolder.memory.setText(TextFormater
					.dataSizeFormat(ProcessUtils.getAppUsedMem(this,
							appinfo.packageName)));

			if (appinfo.isBackuped) {
				mCommenScilingHolder.backup.setText(R.string.backuped);
				mCommenScilingHolder.backup.setEnabled(false);
				mCommenScilingHolder.backup
						.setBackgroundResource(R.drawable.folder_left_button_selector);
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
						.findViewById(R.id.uninstall);

				mBackupScilingHolder.restore.setOnClickListener(this);
				mBackupScilingHolder.delete.setOnClickListener(this);
			}

			mBackupScilingHolder.size.setText(mBackupManager
					.getApkSize(appinfo));
			mBackupScilingHolder.version.setText(String.format(getResources()
					.getString(R.string.app_version), appinfo.versionName));
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd hh:mm:ss");
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(appinfo.backupTime);
			mBackupScilingHolder.backupTime.setText(formatter.format(calendar
					.getTime()));
			mBackupScilingHolder.path.setText(mBackupManager.getBackupPath());

			int contentheight = getResources().getDimensionPixelSize(
					R.dimen.backup_scling_content_height);
			mSlicingLayer.startSlicing(view, mSclingBgView,
					mBackupSclingContentView, contentheight);
		}

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
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBackupManager.unregisterBackupListener(this);
		AppLoadEngine.getInstance(this).unregisterAppChangeListener(this);
	}

	private void intiUI() {
		mInflater = getLayoutInflater();
		mListItemClickListener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				handleItemClick(view, SlicingLayer.SLICING_FROM_APPLIST);
			}
		};

		FolderView folderView = (FolderView) findViewById(R.id.folderView);
		mFolderLayer = new FolderLayer(this, folderView);
		mContainer = findViewById(R.id.container);
		mLoadingView = findViewById(R.id.rl_loading);

		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.setTitle(R.string.uninstall_backup);
		mTtileBar.openBackView();
		mTtileBar.setOptionTextVisibility(View.INVISIBLE);

		mAllAppList = findViewById(R.id.applist);
		mPagerContain = findViewById(R.id.layout_pager_container);
		mPageIndicator = (PageIndicator) findViewById(R.id.indicator);
		mViewPager = (LeoAppViewPager) findViewById(R.id.pager);
	}

	public void fillData() {
		if (mSlicingLayer.isSlicinged()) {
			mSlicingLayer.closeSlicing();
		}

		// load apps
		mAppDetails = new ArrayList<AppItemInfo>();
		List<AppItemInfo> list = AppLoadEngine.getInstance(this)
				.getAllPkgInfo();
		for (AppItemInfo appItemInfo : list) {
			if (!appItemInfo.systemApp)
				mAppDetails.add(appItemInfo);
		}
		// load all folder items
		getFolderData();

		mAllItems = new ArrayList<BaseInfo>();
		// first, add four folders
		mFolderItems = new ArrayList<BaseInfo>();
		loadFolderItems();
		mAllItems.addAll(mFolderItems);

		// second, add business items
		mBusinessItems = new ArrayList<BusinessItemInfo>();
		mBusinessItems = loadBusinessData();
		mAllItems.addAll(mBusinessItems);

		// third, add all local apps
		mAllItems.addAll(mAppDetails);

		// data load finished
		mLoadingView.setVisibility(View.INVISIBLE);
		int pageCount = Math.round(((float) mAllItems.size()) / pageItemCount);
		int itemCounts[] = new int[pageCount];
		int i;
		for (i = 0; i < itemCounts.length; i++) {
			if (i == itemCounts.length - 1) {
				itemCounts[i] = mAllItems.size() % pageItemCount;
			} else {
				itemCounts[i] = pageItemCount;
			}
		}
		ArrayList<View> viewList = new ArrayList<View>();

		for (i = 0; i < pageCount; i++) {
			GridView gridView = (GridView) mInflater.inflate(
					R.layout.grid_page_item, mViewPager, false);
			if (i == pageCount - 1) {
				gridView.setAdapter(new DataAdapter(mAllItems, i
						* pageItemCount, mAppDetails.size() - 1));
			} else {
				gridView.setAdapter(new DataAdapter(mAllItems, i
						* pageItemCount, i * pageItemCount + pageItemCount - 1));
			}
			gridView.setOnItemClickListener(mListItemClickListener);
			viewList.add(gridView);
		}
		mViewPager.setAdapter(new DataPagerAdapter(viewList));
		mPageIndicator.setViewPager(mViewPager);
		mPagerContain.setVisibility(View.VISIBLE);
	}

	/**
	 * we should judge load sync or not
	 * 
	 * @return
	 */
	private List<BusinessItemInfo> loadBusinessData() {
		return getRecommendData(BusinessItemInfo.CONTAIN_APPLIST);
	}

	private void loadFolderItems() {
		FolderItemInfo folder = null;
		// add flow sort folder
		folder = new FolderItemInfo();
		folder.type = BaseInfo.ITEM_TYPE_FOLDER;
		folder.folderType = FolderItemInfo.FOLDER_FLOW_SORT;
		folder.icon = Utilities.getFolderScalePicture(this, mFlowFolderData,
				false);
		folder.label = getString(R.string.folder_sort_flow);
		mFolderItems.add(folder);
		// add capacity folder
		folder = new FolderItemInfo();
		folder.type = BaseInfo.ITEM_TYPE_FOLDER;
		folder.folderType = FolderItemInfo.FOLDER_CAPACITY_SORT;
		folder.icon = folder.icon = Utilities.getFolderScalePicture(this,
				mCapacityFolderData, false);
		folder.label = getString(R.string.folder_sort_capacity);
		mFolderItems.add(folder);
		// add restore folder
		folder = new FolderItemInfo();
		folder.type = BaseInfo.ITEM_TYPE_FOLDER;
		folder.folderType = FolderItemInfo.FOLDER_BACKUP_RESTORE;
		folder.icon = folder.icon = folder.icon = Utilities
				.getFolderScalePicture(this, mRestoreFolderData, true);
		folder.label = getString(R.string.folder_backup_restore);
		mFolderItems.add(folder);
		// add business app folder
		folder = new FolderItemInfo();
		folder.type = BaseInfo.ITEM_TYPE_FOLDER;
		folder.folderType = FolderItemInfo.FOLDER_BUSINESS_APP;
		folder.icon = getResources().getDrawable(
				R.drawable.folder_icon_recommend);
		folder.label = getString(R.string.folder_recommend);
		mFolderItems.add(folder);
	}

	private void animateItem(final View view, final int from) {
		AnimatorSet as = new AnimatorSet();
		as.setDuration(200);
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f,
				0.8f, 1f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f,
				0.8f, 1f);
		as.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator arg0) {
				openSlicingLayer(view, from);
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
			ArrayList<AppItemInfo> list = new ArrayList<AppItemInfo>();
			list.add((AppItemInfo) mLastSelectedInfo);
			mBackupManager.backupApps(list);
			break;
		case R.id.restore:
			mBackupManager.restoreApp(this, (AppItemInfo) mLastSelectedInfo);
			break;
		case R.id.uninstall:
			AppUtil.uninstallApp(this,
					((AppItemInfo) mLastSelectedInfo).packageName);
			break;

		default:
			break;
		}
	}

	public void handleItemClick(View view, int from) {
		if (mSlicingLayer.isAnimating() || mFolderLayer.isAnimating())
			return;
		Intent intent = null;
		mLastSelectedInfo = (BaseInfo) view.getTag();
		switch (mLastSelectedInfo.type) {
		case BaseInfo.ITEM_TYPE_NORMAL_APP:
			animateItem(view, from);
			break;
		case BaseInfo.ITEM_TYPE_FOLDER:
			FolderItemInfo folderInfo = (FolderItemInfo) mLastSelectedInfo;
			fillFolder();
			mFolderLayer.openFolderView(folderInfo.folderType, view,
					mAllAppList);
			break;
		case BaseInfo.ITEM_TYPE_BUSINESS_APP:
			BusinessItemInfo bif = (BusinessItemInfo) mLastSelectedInfo;
			if (AppUtil.appInstalled(this, Constants.GP_PACKAGE)) {
				AppUtil.downloadFromGp(this, bif.packageName);
			} else {
				AppUtil.downloadFromBrowser(this, bif.appDownloadUrl);
			}
			break;

		default:
			break;
		}
	}

	private void getFolderData() {
		int contentMaxCount = 20;
		List<AppItemInfo> tempList = new ArrayList<AppItemInfo>(mAppDetails);

		// load folw sort data
		Collections.sort(tempList, new FlowComparator());
		List<BusinessItemInfo> flowDataReccommendData = getRecommendData(BusinessItemInfo.CONTAIN_APPLIST);
		contentMaxCount = flowDataReccommendData.size() > 0 ? 16 : 20;
		mFlowFolderData = tempList.subList(0,
				tempList.size() < contentMaxCount ? tempList.size()
						: contentMaxCount);

		// load capacity sort data
		Collections.sort(tempList, new CapacityComparator());
		List<BusinessItemInfo> capacityReccommendData = getRecommendData(FolderItemInfo.FOLDER_CAPACITY_SORT);
		contentMaxCount = capacityReccommendData.size() > 0 ? 16 : 20;
		mCapacityFolderData = tempList.subList(0,
				tempList.size() < contentMaxCount ? tempList.size()
						: contentMaxCount);

		// load restore sort data
		List<BusinessItemInfo> restoreReccommendData = getRecommendData(FolderItemInfo.FOLDER_BACKUP_RESTORE);
		contentMaxCount = restoreReccommendData.size() > 0 ? 16 : 20;
		ArrayList<AppItemInfo> temp = mBackupManager.getRestoreList();
		mRestoreFolderData = temp.subList(0,
				temp.size() < contentMaxCount ? temp.size() : contentMaxCount);
	}

	private void fillFolder() {
		List<BusinessItemInfo> flowDataReccommendData = getRecommendData(BusinessItemInfo.CONTAIN_APPLIST);
		List<BusinessItemInfo> capacityReccommendData = getRecommendData(FolderItemInfo.FOLDER_CAPACITY_SORT);
		List<BusinessItemInfo> restoreReccommendData = getRecommendData(FolderItemInfo.FOLDER_BACKUP_RESTORE);

		mFolderLayer.updateFolderData(FolderItemInfo.FOLDER_FLOW_SORT,
				mFlowFolderData, flowDataReccommendData);

		mFolderLayer.updateFolderData(FolderItemInfo.FOLDER_CAPACITY_SORT,
				mCapacityFolderData, capacityReccommendData);

		mFolderLayer.updateFolderData(FolderItemInfo.FOLDER_BACKUP_RESTORE,
				mRestoreFolderData, restoreReccommendData);
	}

	private List<BusinessItemInfo> getRecommendData(int containerId) {
		Vector<BusinessItemInfo> businessDatas = AppBusinessManager
				.getInstance(this).getBusinessData();
		List<BusinessItemInfo> list = new ArrayList<BusinessItemInfo>();
		for (BusinessItemInfo businessItemInfo : businessDatas) {
			// if (businessItemInfo.containType == containerId) {
			if (containerId == BusinessItemInfo.CONTAIN_APPLIST) {
				if (businessItemInfo.iconLoaded) {
					list.add(businessItemInfo);
				}
			} else {
				list.add(businessItemInfo);
			}

			// }
		}
		return list;
	}

	@Override
	public void onAppChanged(ArrayList<AppItemInfo> changes, int type) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				fillData();
			}
		});

	}

	@Override
	public void onDataReady() {
	}

	@Override
	public void onDataUpdate() {
	}

	@Override
	public void onBackupProcessChanged(int doneNum, int totalNum,
			String currentApp) {
	}

	@Override
	public void onBackupFinish(boolean success, int successNum, int totalNum,
			String message) {
		mHandler.sendEmptyMessage(MSG_BACKUP_SUCCESSFUL);
	}

	@Override
	public void onApkDeleted(boolean success) {
	}

	public static class FlowComparator implements Comparator<AppItemInfo> {

		@Override
		public int compare(AppItemInfo lhs, AppItemInfo rhs) {
			if (lhs.trafficInfo.mTotal > rhs.trafficInfo.mTotal) {
				return -1;
			} else if (lhs.trafficInfo.mTotal < rhs.trafficInfo.mTotal) {
				return 1;
			}
			return Collator.getInstance().compare(trimString(lhs.label),
					trimString(rhs.label));
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
			}
			return Collator.getInstance().compare(trimString(lhs.label),
					trimString(rhs.label));
		}

		private String trimString(String s) {
			return s.replaceAll("\u00A0", "").trim();
		}
	}
}
