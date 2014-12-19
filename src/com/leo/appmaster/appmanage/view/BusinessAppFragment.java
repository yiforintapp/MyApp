package com.leo.appmaster.appmanage.view;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.appmanage.AppListActivity;
import com.leo.appmaster.appmanage.business.AppBusinessManager;
import com.leo.appmaster.appmanage.business.BusinessJsonParser;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BusinessItemInfo;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.LockImageView;
import com.leo.appmaster.utils.LeoLog;
import com.leo.imageloader.DisplayImageOptions;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.ImageScaleType;
import com.leoers.leoanalytics.LeoStat;

public class BusinessAppFragment extends BaseFolderFragment implements
		OnItemClickListener, OnClickListener, OnRefreshListener2<GridView> {

	private PullToRefreshGridView mRecommendGrid;
	private View mRecommendHolder, mErrorView, mLayoutEmptyTip;
	private TextView mTvRetry;
	private List<BusinessItemInfo> mRecommendDatas;
	public LayoutInflater mInflater;
	private RecommendAdapter mRecommendAdapter;
	private ProgressBar mProgressBar;

	private int mCurrentPage = 1;
	private static final int MSG_LOAD_INIT_FAILED = 0;
	private static final int MSG_LOAD_INIT_SUCCESSED = 1;
	private static final int MSG_LOAD_PAGE_DATA_FAILED = 3;
	private static final int MSG_LOAD_PAGE_DATA_SUCCESS = 4;
	private EventHandler mHandler;
	private DisplayImageOptions commonOption;

	private boolean mInitDataLoaded, mInitLoading;

	private static class EventHandler extends Handler {
		WeakReference<BusinessAppFragment> fragmemtHolder;

		public EventHandler(BusinessAppFragment lockerTheme) {
			super();
			this.fragmemtHolder = new WeakReference<BusinessAppFragment>(
					lockerTheme);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LOAD_INIT_FAILED:
				if (fragmemtHolder.get() != null) {
					fragmemtHolder.get().onLoadInitAppFinish(false, null);
				}
				break;
			case MSG_LOAD_INIT_SUCCESSED:
				if (fragmemtHolder.get() != null) {
					fragmemtHolder.get().onLoadInitAppFinish(true, msg.obj);
				}
				break;
			case MSG_LOAD_PAGE_DATA_FAILED:
				if (fragmemtHolder.get() != null) {
					fragmemtHolder.get().mRecommendGrid.onRefreshComplete();
					fragmemtHolder.get().onLoadMoreBusinessDataFinish(false,
							null);
				}
				break;
			case MSG_LOAD_PAGE_DATA_SUCCESS:
				if (fragmemtHolder.get() != null) {
					fragmemtHolder.get().mRecommendGrid.onRefreshComplete();
					List<BusinessItemInfo> loadList = (List<BusinessItemInfo>) msg.obj;
					fragmemtHolder.get().onLoadMoreBusinessDataFinish(true,
							loadList);
				}
				break;
			default:
				break;
			}
		}
	}

	@Override
	protected int layoutResourceId() {
		return R.layout.fragment_folder_business;
	}

	@Override
	protected void onInitUI() {

		commonOption = new DisplayImageOptions.Builder()
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				.showImageOnLoading(R.drawable.recommend_loading_icon)
				.showImageOnFail(R.drawable.recommend_loading_icon)
				.cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
				.build();

		mHandler = new EventHandler(this);
		mInflater = LayoutInflater.from(mActivity);

		mRecommendHolder = findViewById(R.id.content_holder);
		mRecommendHolder.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				((AppListActivity) mActivity).getFolderLayer().closeFloder();
			}
		});

		mProgressBar = (ProgressBar) findViewById(R.id.progressbar_loading);
		mErrorView = findViewById(R.id.layout_load_error);
		mLayoutEmptyTip = findViewById(R.id.layout_empty);
		mTvRetry = (TextView) findViewById(R.id.tv_reload);
		mTvRetry.setOnClickListener(this);

		mRecommendGrid = (PullToRefreshGridView) findViewById(R.id.business_fragment);
		mRecommendGrid.setMode(Mode.PULL_FROM_END);
		mRecommendDatas = new ArrayList<BusinessItemInfo>();
		mRecommendAdapter = new RecommendAdapter();
		mRecommendGrid.setAdapter(mRecommendAdapter);
		mRecommendGrid.setOnRefreshListener(this);
		mRecommendGrid.setOnItemClickListener(this);
		mRecommendGrid.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				((AppListActivity) mActivity).getFolderLayer().closeFloder();
			}
		});
	}

	public void addMoreOnlineTheme(List<BusinessItemInfo> loadList) {
		boolean add;
		boolean newTheme = false;
		for (BusinessItemInfo businessAppInfo : loadList) {
			add = true;
			for (BusinessItemInfo appInfo : mRecommendDatas) {
				if (appInfo.packageName.equals(businessAppInfo.packageName)) {
					if (appInfo.packageName != null) {
						if (filterLocalApp(appInfo.packageName)) {
							add = false;
							break;
						}
					}
				}
			}
			if (add) {
				newTheme = true;
				mRecommendDatas.add(businessAppInfo);
			}
		}
		if (newTheme) {
			mLayoutEmptyTip.setVisibility(View.INVISIBLE);
			mRecommendAdapter.notifyDataSetChanged();
		} else {
			Toast.makeText(mActivity, R.string.no_more_theme, 0).show();
		}
	}

	public void onLoadMoreBusinessDataFinish(boolean succeed, Object object) {
		if (succeed) {
			List<BusinessItemInfo> list = (List<BusinessItemInfo>) object;
			if (list == null || list.isEmpty()) {
				Toast.makeText(mActivity, R.string.no_more_business_app, 0)
						.show();
			} else {
				addMoreOnlineTheme(list);
			}
		} else {
			Toast.makeText(mActivity, R.string.network_error_msg, 0).show();
		}
	}

	public void onLoadInitAppFinish(boolean succeed, Object object) {
		mRecommendHolder.setVisibility(View.VISIBLE);
		if (succeed) {
			mRecommendHolder.setVisibility(View.VISIBLE);
			mErrorView.setVisibility(View.INVISIBLE);
			mRecommendDatas.clear();
			if (object != null) {
				List<BusinessItemInfo> list = (List<BusinessItemInfo>) object;
				for (BusinessItemInfo businessItemInfo : list) {
					if (businessItemInfo.packageName != null) {
						if (!filterLocalApp(businessItemInfo.packageName)) {
							mRecommendDatas.add(businessItemInfo);
						}
					}
				}
			}
			// if filter local app
			mRecommendGrid.setVisibility(View.VISIBLE);
			mRecommendAdapter.notifyDataSetChanged();
			if (mRecommendDatas.isEmpty()) {
				mLayoutEmptyTip.setVisibility(View.VISIBLE);
			} else {
				mLayoutEmptyTip.setVisibility(View.INVISIBLE);
			}
			
			
			SDKWrapper.addEvent(mActivity, LeoStat.P1, "app_rec", "new");
		} else {
			mRecommendGrid.setVisibility(View.INVISIBLE);
			mErrorView.setVisibility(View.VISIBLE);
			mLayoutEmptyTip.setVisibility(View.INVISIBLE);
		}
		mProgressBar.setVisibility(View.INVISIBLE);
	}

	private boolean filterLocalApp(String pkg) {
		if (pkg == null)
			return false;
		ArrayList<AppItemInfo> appDetails = AppLoadEngine
				.getInstance(mActivity).getAllPkgInfo();

		for (AppItemInfo info : appDetails) {
			if (pkg.equals(info.packageName)) {
				return true;
			}
		}
		return false;

	}

	public void loadInitBusinessData() {
		if (mInitDataLoaded || mInitLoading)
			return;
		mRecommendDatas.clear();
		mInitLoading = true;
		HttpRequestAgent.getInstance(mActivity).loadBusinessRecomApp(1,
				new Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response, boolean noModify) {
						LeoLog.e("loadBusinessRecomApp", "response = "
								+ response);
						List<BusinessItemInfo> list = BusinessJsonParser
								.parserJsonObject(
										mActivity,
										response,
										BusinessItemInfo.CONTAIN_BUSINESS_FOLDER);
						Message msg = mHandler.obtainMessage(
								MSG_LOAD_INIT_SUCCESSED, list);
						mHandler.sendMessage(msg);
						mInitDataLoaded = true;
						mInitLoading = false;
					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						LeoLog.e("loadBusinessRecomApp", "onErrorResponse = "
								+ error.getMessage());
						mHandler.sendEmptyMessage(MSG_LOAD_INIT_FAILED);
						mInitLoading = false;
						SDKWrapper.addEvent(BusinessAppFragment.this.mActivity,
								LeoStat.P1, "load_failed", "new_apps");
					}
				});
	}

	private void loadMoreBusiness() {
		HttpRequestAgent.getInstance(mActivity).loadBusinessRecomApp(
				mCurrentPage + 1, new Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response, boolean noModify) {
						mCurrentPage++;
						List<BusinessItemInfo> list = BusinessJsonParser
								.parserJsonObject(
										mActivity,
										response,
										BusinessItemInfo.CONTAIN_BUSINESS_FOLDER);
						Message msg = mHandler.obtainMessage(
								MSG_LOAD_PAGE_DATA_SUCCESS, list);
						mHandler.sendMessage(msg);
					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						mHandler.sendEmptyMessage(MSG_LOAD_PAGE_DATA_FAILED);
						SDKWrapper.addEvent(BusinessAppFragment.this.mActivity,
								LeoStat.P1, "load_failed", "new_apps");
					}
				});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	private void doReload() {
		mRecommendHolder.setVisibility(View.INVISIBLE);
		mProgressBar.setVisibility(View.VISIBLE);
		loadInitBusinessData();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		((AppListActivity) mActivity).handleItemClick(view, mType);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.tv_reload:
			doReload();
			break;
		default:
			break;
		}
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
		loadMoreBusiness();
	}

	private class RecommendAdapter extends BaseAdapter {

		public RecommendAdapter() {
			super();
		}

		@Override
		public int getCount() {
			return mRecommendDatas.size();
		}

		@Override
		public Object getItem(int position) {
			return mRecommendDatas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// if (convertView == null) {
			convertView = mInflater.inflate(R.layout.app_item, null);
			// }

			LockImageView imageView = (LockImageView) convertView
					.findViewById(R.id.iv_app_icon);
			TextView textView = (TextView) convertView
					.findViewById(R.id.tv_app_name);
			BusinessItemInfo info = mRecommendDatas.get(position);
			ImageLoader.getInstance().displayImage(info.iconUrl, imageView,
					commonOption);
			textView.setText(info.label);
			convertView.setTag(info);
			return convertView;
		}
	}
}
