package com.leo.appmaster.appmanage.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.ImageLoader;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.model.BusinessItemInfo;
import com.leo.appmaster.utils.BitmapUtils;
import com.leo.appmaster.utils.LeoLog;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

/**
 * app list business manager
 * 
 * @author zhangwenyang
 * 
 */
public class ApplistBusinessManager {

	private static final int DELAY_2_HOUR = 2 * 60 * 60 * 1000;
	public static final int DELAY_12_HOUR = 12 * 60 * 60 * 1000;

	/**
	 * applist business data change listener
	 * 
	 * @author zhangwenyang
	 * 
	 */
	public interface BusinessListener {

		void onBusinessDataChange(List<BusinessItemInfo> businessList);

	}

	private Context mContext;
	private List<BusinessListener> mBusinessListeners;
	private Vector<BusinessItemInfo> mBusinessList;
	private FutureTask<Vector<BusinessItemInfo>> mLoadInitDataTask;

	private final ExecutorService mExecutorService = Executors
			.newSingleThreadExecutor();

	private static ApplistBusinessManager mInstance;

	private ApplistBusinessManager(Context ctx) {
		mContext = ctx.getApplicationContext();
		mBusinessListeners = new ArrayList<ApplistBusinessManager.BusinessListener>();
		mBusinessList = new Vector<BusinessItemInfo>();
		init();
	}
	
	public void init() {
		loadInitData();
		trySyncServerData();
	}

	private void loadInitData() {
		mLoadInitDataTask = new FutureTask<Vector<BusinessItemInfo>>(
				new Callable<Vector<BusinessItemInfo>>() {
					@Override
					public Vector<BusinessItemInfo> call() throws Exception {
						final ContentResolver resolver = mContext
								.getContentResolver();
						String[] projection = { "lebal", "package_name",
								"app_size", "icon_status", "gp_priority",
								"gp_url", };
						Cursor c = resolver.query(
								Constants.APPLIST_BUSINESS_URI, projection,
								null, null, Constants.ID);
						BusinessItemInfo info;
						int lebalIndex, pkgIndex, iconStatusIndex, iconIndex, downloadUrlIndex, appSizeIndex, gpPriorityIndex, gpUrlIndex;
						if (c != null) {
							while (c.moveToNext()) {
								info = new BusinessItemInfo();

								lebalIndex = c.getColumnIndex("lebal");
								info.label = c.getString(lebalIndex);

								pkgIndex = c.getColumnIndex("lebal");
								info.packageName = c.getString(pkgIndex);

								downloadUrlIndex = c
										.getColumnIndex("download_url");
								info.iconUrl = c.getString(downloadUrlIndex);

								appSizeIndex = c.getColumnIndex("app_size");
								info.appDownloadUrl = c.getString(appSizeIndex);

								gpPriorityIndex = c
										.getColumnIndex("gp_priority");
								info.iconUrl = c.getString(gpPriorityIndex);

								gpUrlIndex = c.getColumnIndex("gp_url");
								info.appDownloadUrl = c.getString(gpUrlIndex);

								iconStatusIndex = c
										.getColumnIndex("icon_status");
								info.iconLoaded = c.getInt(iconStatusIndex) == 1 ? true
										: false;

								if (info.iconLoaded) {
									iconIndex = c.getColumnIndex("icon");
									byte[] bytes = c.getBlob(iconIndex);
									info.icon = BitmapUtils
											.bitmapToDrawable(BitmapUtils
													.bytes2Bimap(bytes));
								}

								mBusinessList.add(info);
							}
						}
						return mBusinessList;
					}
				});
		mExecutorService.execute(mLoadInitDataTask);
	}

	public static synchronized ApplistBusinessManager getInstance(Context ctx) {
		if (mInstance == null) {
			mInstance = new ApplistBusinessManager(ctx);
		}
		return mInstance;
	}

	public void registerBusinessListener(BusinessListener listener) {
		mBusinessListeners.add(listener);
	}

	public void unregisterBusinessListener(BusinessListener listener) {
		mBusinessListeners.remove(listener);
	}

	public Vector<BusinessItemInfo> getInitData() {
		try {
			return mLoadInitDataTask.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void trySyncServerData() {

		final AppMasterPreference pref = AppMasterPreference
				.getInstance(mContext);
		long curTime = System.currentTimeMillis();

		long lastSyncTime = pref.getLastSyncBusinessTime();
		if (lastSyncTime == 0
				|| (curTime - pref.getLastSyncBusinessTime()) > DELAY_12_HOUR) {

			HttpRequestAgent.getInstance(mContext).loadApplistRecomApp(
					new Listener<JSONObject>() {

						@Override
						public void onResponse(JSONObject response,
								boolean noModify) {
							if (response != null) {
								try {
									JSONObject jsonObject = response.getJSONObject("data");
									if (jsonObject != null) {
										pref.setLastSyncBusinessTime(System
												.currentTimeMillis());
										if (!noModify) {
											// to paser data
											List<BusinessItemInfo> list = BusinessJsonParser
													.parserJsonObject(mContext,
															jsonObject);
											syncLocalData(list);
										}
									}

									TimerTask recheckTask = new TimerTask() {
										@Override
										public void run() {
											trySyncServerData();
										}
									};
									Timer timer = new Timer();
									timer.schedule(recheckTask, DELAY_12_HOUR);

								} catch (JSONException e) {
									e.printStackTrace();
									LeoLog.e("syncServerData", e.getMessage());
									TimerTask recheckTask = new TimerTask() {
										@Override
										public void run() {
											trySyncServerData();
										}
									};
									Timer timer = new Timer();
									timer.schedule(recheckTask, DELAY_2_HOUR);
								}
							}
						}

					}, new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							LeoLog.e("syncServerData", error.getMessage());
							TimerTask recheckTask = new TimerTask() {
								@Override
								public void run() {
									trySyncServerData();
								}
							};
							Timer timer = new Timer();
							timer.schedule(recheckTask, DELAY_2_HOUR);
						}
					});
		}

	}

	protected void syncLocalData(final List<BusinessItemInfo> list) {  
		mExecutorService.execute(new Runnable() {
			@Override
			public void run() {
				final ContentResolver resolver = mContext.getContentResolver();
				resolver.delete(Constants.APPLIST_BUSINESS_URI, null, null);
				mBusinessList.clear();
				notifyBusinessChange();
				if (list == null || list.isEmpty())
					return;
				ContentValues[] values = new ContentValues[list.size()];
				BusinessItemInfo businessItemInfo;
				for (int i = 0; i < list.size(); i++) {
					ContentValues value = new ContentValues();
					businessItemInfo = list.get(i);
					value.put("lebal", businessItemInfo.label);
					value.put("package_name", businessItemInfo.packageName);
					value.put("icon_url", businessItemInfo.iconUrl);
					value.put("download_url", businessItemInfo.appDownloadUrl);
					value.put("app_size", businessItemInfo.appSize);
					value.put("icon_status", 0);
					value.put("gp_priority", businessItemInfo.gpPriority);
					value.put("gp_url", businessItemInfo.gpUrl);
					values[i] = value;
				}
				// write db
				resolver.bulkInsert(Constants.APPLIST_BUSINESS_URI, values);
				// goto laod app icon
				loadAppIcon();
			}
		});

	}

	private void notifyBusinessChange() {
		if (mBusinessListeners != null) {
			for (BusinessListener listner : mBusinessListeners) {
				listner.onBusinessDataChange(mBusinessList);
			}
		}
	}

	private void loadAppIcon() {
		final ContentResolver resolver = mContext.getContentResolver();
		for (final BusinessItemInfo info : mBusinessList) {
			if (info.iconLoaded)
				continue;
			HttpRequestAgent.getInstance(mContext).loadBusinessAppIcon(
					info.iconUrl, new Listener<Bitmap>() {
						@Override
						public void onResponse(Bitmap response, boolean noMidify) {
							ContentValues value = new ContentValues();
							value.put("icon",
									BitmapUtils.Bitmap2Bytes(response));
							value.put("icon_status", 1);
							resolver.update(Constants.APPLIST_BUSINESS_URI,
									value, "icon_url=" + info.iconUrl, null);

							info.icon = BitmapUtils.bitmapToDrawable(response);
							info.iconLoaded = true;
							notifyBusinessChange();
						}
					}, new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							LeoLog.e("syncLocalData", error.getMessage());
						}
					});
		}
	}
}
