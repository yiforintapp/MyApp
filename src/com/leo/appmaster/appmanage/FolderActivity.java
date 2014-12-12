package com.leo.appmaster.appmanage;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.leo.appmaster.R;
import com.leo.appmaster.appmanage.view.FolderView;
import com.leo.appmaster.appmanage.view.FolderView.ItemHolder;
import com.leo.appmaster.appmanage.view.LeoHomeGallery;
import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.backup.AppBackupRestoreManager.AppBackupDataListener;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppInfo;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.model.FolderItemInfo;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.ViewGroup.LayoutParams;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;

public class FolderActivity extends Activity implements AppBackupDataListener {

	private FolderView mFolderView;

	private GridView[] mFolderPagers;
	private SparseArray<Object> mFolderDatas;

	private int mFromType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_folder);
		handleIntent();
		initUI();
		loadData();
	}

	public void updateFolderData(int type, List<BaseInfo> list) {

	}

	private void loadData() {

		// mFolderPagers = new GridView[4];
		// mFolderDatas = new SparseArray<Object>();
		//
		// // load backup app
		// AppBackupRestoreManager abm = new AppBackupRestoreManager(this,
		// this);
		// ArrayList<AppItemInfo> restoreList = abm.getRestoreList();
		// mFolderDatas.put(FolderItemInfo.FOLDER_BACKUP_RESTORE, restoreList);
		//
		// AppLoadEngine laodEngine = AppLoadEngine.getInstance(this);
		// ArrayList<AppItemInfo> sourceList = laodEngine.getAllPkgInfo();
		// for (AppItemInfo appItemInfo : sourceList) {
		// laodEngine.loadAppDetailInfo(appItemInfo.packageName);
		// }
		// // sorted by flow
		// ArrayList<AppItemInfo> flowList = new ArrayList<AppItemInfo>();
		// Collections.copy(flowList, sourceList);
		// Collections.sort(flowList, new FlowComparator());
		//
		// // sorted by capacity
		// ArrayList<AppItemInfo> capacityList = new ArrayList<AppItemInfo>();
		// Collections.copy(capacityList, sourceList);
		// Collections.sort(capacityList, new CapacityComparator());
		//
		//
		// //load business data
		//
		//
		//
		// if (mFromType == FolderItemInfo.FOLDER_BACKUP_RESTORE) {
		//
		// } else if (mFromType == FolderItemInfo.FOLDER_FLOW_SORT) {
		//
		// } else if (mFromType == FolderItemInfo.FOLDER_CAPACITY_SORT) {
		//
		// } else if (mFromType == FolderItemInfo.FOLDER_BUSINESS_APP) {
		//
		// }

		List<ItemHolder> holder = new ArrayList<FolderView.ItemHolder>();
		ItemHolder item;
		ImageView iv;

		item = new ItemHolder();
		item.itmeTitle = getString(R.string.folder_sort_flow);
		iv = new ImageView(this);
		iv.setLayoutParams(new LeoHomeGallery.LayoutParams(
				LeoHomeGallery.LayoutParams.MATCH_PARENT,
				LeoHomeGallery.LayoutParams.MATCH_PARENT));
		iv.setImageResource(R.drawable.ic_launcher);
		item.pagerView = iv;
		holder.add(item);

		item = new ItemHolder();
		item.itmeTitle = getString(R.string.folder_sort_capacity);
		iv = new ImageView(this);
		iv.setLayoutParams(new LeoHomeGallery.LayoutParams(
				LeoHomeGallery.LayoutParams.MATCH_PARENT,
				LeoHomeGallery.LayoutParams.MATCH_PARENT));
		iv.setImageResource(R.drawable.ic_launcher);
		item.pagerView = iv;
		holder.add(item);

		item = new ItemHolder();
		item.itmeTitle = getString(R.string.folder_backup_restore);
		iv = new ImageView(this);
		iv.setLayoutParams(new LeoHomeGallery.LayoutParams(
				LeoHomeGallery.LayoutParams.MATCH_PARENT,
				LeoHomeGallery.LayoutParams.MATCH_PARENT));
		iv.setImageResource(R.drawable.ic_launcher);
		item.pagerView = iv;
		holder.add(item);

		item = new ItemHolder();
		item.itmeTitle = getString(R.string.folder_recommend);
		iv = new ImageView(this);
		iv.setLayoutParams(new LeoHomeGallery.LayoutParams(
				LeoHomeGallery.LayoutParams.MATCH_PARENT,
				LeoHomeGallery.LayoutParams.MATCH_PARENT));
		iv.setImageResource(R.drawable.ic_launcher);
		item.pagerView = iv;
		holder.add(item);

		mFolderView.fillTitle();
	}

	private void handleIntent() {
		Intent intent = this.getIntent();
		mFromType = intent.getIntExtra("from_type",
				FolderItemInfo.FOLDER_BACKUP_RESTORE);
	}

	private void initUI() {
		mFolderView = (FolderView) findViewById(R.id.folder_view);
		mFolderPagers = new GridView[4];
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

	@Override
	public void onDataReady() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDataUpdate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBackupProcessChanged(int doneNum, int totalNum,
			String currentApp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBackupFinish(boolean success, int successNum, int totalNum,
			String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onApkDeleted(boolean success) {
		// TODO Auto-generated method stub

	}

}
