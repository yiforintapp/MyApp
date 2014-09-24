package com.leo.appmaster.appmanage;

import java.util.Vector;

import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.IAppLoadListener;
import com.leo.appmaster.model.AppDetailInfo;

import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AppManagerActivity extends Activity implements IAppLoadListener {

	View mLoadingView;
	private GridView mAppContainGV;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_manager);
		intiUI();
		AppLoadEngine manager = AppLoadEngine.getInstance();
		manager.init(this.getApplicationContext());
		manager.setLoadListener(this);
		manager.loadAppDetailInfo();
	}

	private void intiUI() {
		mLoadingView = findViewById(R.id.rl_loading);
		mAppContainGV = (GridView) findViewById(R.id.gv_app_contain);
	}

	@Override
	public void onLoadFinsh(Vector<AppDetailInfo> appDetails) {
		mLoadingView.setVisibility(View.INVISIBLE);

		mAppContainGV.setAdapter(new DataAdapter(appDetails,
				getLayoutInflater()));
		mAppContainGV.setVisibility(View.VISIBLE);
	}

	private class DataAdapter extends BaseAdapter {

		Vector<AppDetailInfo> mAppDetails;
		LayoutInflater mInflater;

		public DataAdapter(Vector<AppDetailInfo> appDetails,
				LayoutInflater inflater) {
			super();
			this.mAppDetails = appDetails;
			this.mInflater = inflater;
		}

		@Override
		public int getCount() {
			return mAppDetails.size();
		}

		@Override
		public Object getItem(int position) {
			return mAppDetails.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ItemHolder holder;
			if (convertView == null) {
				holder = new ItemHolder();
				convertView = mInflater.inflate(R.layout.app_item, null);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.iv_app_icon);
				holder.textView = (TextView) convertView
						.findViewById(R.id.tv_app_name);
				convertView.setTag(holder);
			} else {
				holder = (ItemHolder) convertView.getTag();
			}
			holder.imageView.setImageDrawable(mAppDetails.get(position)
					.getAppIcon());
			holder.textView.setText(mAppDetails.get(position).getAppLabel());
			return convertView;
		}

	}

	private static class ItemHolder {
		public ImageView imageView;
		public TextView textView;
	}

}
