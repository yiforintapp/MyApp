package com.leo.appmaster.backup;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.leo.appmaster.R;
import com.leo.appmaster.model.AppDetailInfo;

public class AppBackupAdapter extends BaseAdapter {

	private ArrayList<AppDetailInfo> mBackupList;

	private AppBackupRestoreManager mBackupManager;

	public AppBackupAdapter(AppBackupRestoreManager manager) {
		mBackupManager = manager;
		mBackupList = new ArrayList<AppDetailInfo>();
	}

	public ArrayList<AppDetailInfo> getSelectedItems() {
		ArrayList<AppDetailInfo> selectedItems = new ArrayList<AppDetailInfo>();
		for (AppDetailInfo app : mBackupList) {
			if (app.isChecked) {
				selectedItems.add(app);
			}
		}
		return selectedItems;
	}

	public boolean hasBackupApp() {
		for (AppDetailInfo app : mBackupList) {
			if (!app.isBackuped) {
				return true;
			}
		}
		return false;
	}

	public void updateData() {
		mBackupList.clear();
		ArrayList<AppDetailInfo> apps = mBackupManager.getBackupList();
		for (AppDetailInfo app : apps) {
			app.isChecked = false;
			mBackupList.add(app);
		}
		notifyDataSetChanged();
	}

	public void checkAll(boolean check) {
		for (AppDetailInfo app : mBackupList) {
			if (app.isBackuped)
				continue;
			app.isChecked = check;
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mBackupList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mBackupList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		AppBackupItemView itemView = null;
		if (arg1 instanceof AppBackupItemView) {
			itemView = (AppBackupItemView) arg1;
		} else {
			LayoutInflater inflater = LayoutInflater.from(arg2.getContext());
			itemView = (AppBackupItemView) inflater.inflate(
					R.layout.item_app_backup, null);
		}
		AppDetailInfo app = mBackupList.get(arg0);
		Context context = itemView.getContext();
		if (arg0 % 2 == 0) {
			itemView.setBackgroundDrawable(context.getResources().getDrawable(
					R.drawable.backup_list_item_one));
		} else {
			itemView.setBackgroundDrawable(context.getResources().getDrawable(
					R.drawable.backup_list_item_two));
		}
		itemView.setIcon(app.icon);
		itemView.setTitle(app.label);

		itemView.setVersion(String.format(
				context.getResources().getString(R.string.app_version),
				app.versionName));
		itemView.setSize(mBackupManager.getApkSize(app));
		itemView.setState(app.isBackuped ? AppBackupItemView.STATE_BACKUPED
				: app.isChecked ? AppBackupItemView.STATE_SELECTED
						: AppBackupItemView.STATE_UNSELECTED);
		itemView.setTag(app);
		return itemView;
	}

}
