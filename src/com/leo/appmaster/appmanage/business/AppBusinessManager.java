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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.ImageLoader;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.appmanage.view.BusinessAppFragment;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.model.BusinessItemInfo;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.BitmapUtils;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.LoadFailUtils;
import com.leoers.leoanalytics.LeoStat;

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
public class AppBusinessManager {

	private static final int DELAY_2_HOUR = 2 * 60 * 60 * 1000;
	public static final int DELAY_12_HOUR = 12 * 60 * 60 * 1000;

	// private static final int DELAY_2_HOUR = 5 * 1000;
	// public static final int DELAY_12_HOUR = 5 * 1000;

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
	private boolean mInitDataLoaded = false;

	private final ExecutorService mExecutorService = Executors
			.newSingleThreadExecutor();

	private static AppBusinessManager mInstance;

	private AppBusinessManager(Context ctx) {
		mContext = ctx.getApplicationContext();
		mBusinessListeners = new ArrayList<AppBusinessManager.BusinessListener>();
		mBusinessList = new Vector<BusinessItemInfo>();
		// init();
	}

	public void init() {
		loadInitData();
		syncServerAppListData();
		syncOtherRecommend(BusinessItemInfo.CONTAIN_FLOW_SORT);
		syncOtherRecommend(BusinessItemInfo.CONTAIN_CAPACITY_SORT);
	}

	private void loadInitData() {
		mLoadInitDataTask = new FutureTask<Vector<BusinessItemInfo>>(
				new Callable<Vector<BusinessItemInfo>>() {
					@Override
					public Vector<BusinessItemInfo> call() {
						final ContentResolver resolver = mContext
								.getContentResolver();
						String[] projection = { "lebal", "package_name",
								"app_size", "icon_status", "gp_priority",
								"gp_url", };
						Cursor c = resolver.query(
								Constants.APPLIST_BUSINESS_URI, null, null,
								null, Constants.ID);
						BusinessItemInfo info;
						
						int lebalIndex, pkgIndex, iconStatusIndex, iconUrlIndex, iconIndex;
						int downloadUrlIndex, appSizeIndex, gpPriorityIndex, gpUrlIndex, containerId;
						int ratingIndex, descIndex, downloadCountIndex;
						
						lebalIndex = c.getColumnIndex("lebal");
						pkgIndex = c.getColumnIndex("package_name");
						iconUrlIndex = c.getColumnIndex("icon_url");
						downloadUrlIndex = c.getColumnIndex("download_url");
						iconIndex = c.getColumnIndex("icon");
						containerId = c.getColumnIndex("container_id");
						gpPriorityIndex = c.getColumnIndex("gp_priority");
						gpUrlIndex = c.getColumnIndex("gp_url");
						appSizeIndex = c.getColumnIndex("app_size");
						iconStatusIndex = c.getColumnIndex("icon_status");
						ratingIndex = c.getColumnIndex("rating");
						downloadCountIndex = c.getColumnIndex("download_count");
						descIndex = c.getColumnIndex("desc");
						
						mBusinessList.clear();
						if (c != null && c.moveToFirst()) {
							do {
								info = new BusinessItemInfo();
								info.label = c.getString(lebalIndex);
								info.packageName = c.getString(pkgIndex);
								info.iconUrl = c.getString(iconUrlIndex);
								info.appDownloadUrl = c
										.getString(downloadUrlIndex);
								info.containType = c.getInt(containerId);
								info.gpPriority = c.getInt(gpPriorityIndex);
								info.gpUrl = c.getString(gpUrlIndex);
								info.appDownloadCount = c.getString(downloadCountIndex);
								info.rating = Float.parseFloat(c.getString(ratingIndex));
								info.desc = c.getString(descIndex);
								info.appSize = c.getInt(appSizeIndex);
								info.iconLoaded = c.getInt(iconStatusIndex) == 1 ? true
										: false;
								if (info.iconLoaded) {
									byte[] bytes = c.getBlob(iconIndex);
									info.icon = BitmapUtils
											.bitmapToDrawable(BitmapUtils
													.bytes2Bimap(bytes));
								}
								info.type = BaseInfo.ITEM_TYPE_BUSINESS_APP;
								mBusinessList.add(info);
							} while (c.moveToNext());
							c.close();
						}
						mInitDataLoaded = true;
						loadAppIcon();
						return mBusinessList;
					}
				});
		mExecutorService.execute(mLoadInitDataTask);
	}

	public static synchronized AppBusinessManager getInstance(Context ctx) {
		if (mInstance == null) {
			mInstance = new AppBusinessManager(ctx);
		}
		return mInstance;
	}

	public void registerBusinessListener(BusinessListener listener) {
		mBusinessListeners.add(listener);
	}

	public void unregisterBusinessListener(BusinessListener listener) {
		mBusinessListeners.remove(listener);
	}

	public Vector<BusinessItemInfo> getBusinessData() {
		if (mInitDataLoaded) {
			return mBusinessList;
		} else {
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
	}
	
    public boolean hasBusinessData(int type) {
        Vector<BusinessItemInfo> businessDatas = getBusinessData();
        for (BusinessItemInfo businessItemInfo : businessDatas) {
            if (businessItemInfo.installed)
                continue;
            if (businessItemInfo.containType == type) {
                return true;
            }
        }
        return false;
    }

	private void syncServerAppListData() {
		final AppMasterPreference pref = AppMasterPreference
				.getInstance(mContext);
		long curTime = System.currentTimeMillis();

		long lastSyncTime = pref.getLastSyncBusinessTime();
		if (lastSyncTime == 0
				|| (curTime - pref.getLastSyncBusinessTime()) > DELAY_12_HOUR) {

			HttpRequestAgent.getInstance(mContext).loadRecomApp(
					BusinessItemInfo.CONTAIN_APPLIST,
					new Listener<JSONObject>() {

						@Override
						public void onResponse(JSONObject response,
								boolean noModify) {
							if (response != null) {
								try {
									if (response != null) {
										pref.setLastSyncBusinessTime(System
												.currentTimeMillis());
										List<BusinessItemInfo> list = BusinessJsonParser
												.parserJsonObject(
														mContext,
														response,
														BusinessItemInfo.CONTAIN_APPLIST);
										if (!noModify) {
											// to paser data
											LeoLog.d("trySyncServerData",
													list.toString());
											syncApplistData(
													BusinessItemInfo.CONTAIN_APPLIST,
													list);
										} else {
											LeoLog.d("trySyncServerData",
													"noModify");
										}
									}
								} catch (Exception e) {
//									e.printStackTrace();
//									LeoLog.e("syncServerData", e.getMessage());
//									TimerTask recheckTask = new TimerTask() {
//										@Override
//										public void run() {
//											syncServerAppListData();
//										}
//									};
//									Timer timer = new Timer();
//									timer.schedule(recheckTask, DELAY_2_HOUR);
								} finally {
                                    TimerTask recheckTask = new TimerTask() {
                                        @Override
                                        public void run() {
                                            syncServerAppListData();
                                        }
                                    };
                                    Timer timer = new Timer();
                                    timer.schedule(recheckTask, DELAY_12_HOUR);
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
									syncServerAppListData();
								}
							};
							Timer timer = new Timer();
							timer.schedule(recheckTask, DELAY_2_HOUR);
							LoadFailUtils.sendLoadFail(mContext, "home_apps");
						}
					});
		}
	}

	private void syncOtherRecommend(final int type) {
		HttpRequestAgent.getInstance(mContext).loadRecomApp(type,
				new Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response, boolean noModify) {
						if (response != null) {
							try {
								if (response != null) {
									List<BusinessItemInfo> list = BusinessJsonParser
											.parserJsonObject(mContext,
													response, type);
									if (!noModify) {
										LeoLog.d("syncOtherRecommend",
												list.toString());
									} else {
										LeoLog.d("syncOtherRecommend",
												"noModify");
									}
									syncOtherRecommendData(type, list, noModify);
								}
							} catch (Exception e) {
//								e.printStackTrace();
//								LeoLog.e("syncServerData", e.getMessage());
//								TimerTask recheckTask = new TimerTask() {
//									@Override
//									public void run() {
//										syncOtherRecommend(type);
//									}
//								};
//								Timer timer = new Timer();
//								timer.schedule(recheckTask, DELAY_2_HOUR);
							} finally {
                                TimerTask recheckTask = new TimerTask() {
                                    @Override
                                    public void run() {
                                        syncOtherRecommend(type);
                                    }
                                };
                                Timer timer = new Timer();
                                timer.schedule(recheckTask, DELAY_12_HOUR);
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
								syncOtherRecommend(type);
							}
						};
						Timer timer = new Timer();
						timer.schedule(recheckTask, DELAY_2_HOUR);

						if (type == BusinessItemInfo.CONTAIN_FLOW_SORT) {
						    LoadFailUtils.sendLoadFail(mContext, "flow_apps");
						} else {
						    LoadFailUtils.sendLoadFail(mContext, "space_apps");
						}
					}
				});
	}

	private void syncOtherRecommendData(int type, List<BusinessItemInfo> list,
			boolean noModify) {
		List<BusinessItemInfo> removeList = new ArrayList<BusinessItemInfo>();
		for (BusinessItemInfo businessItemInfo : mBusinessList) {
			if (businessItemInfo.containType == type) {
				removeList.add(businessItemInfo);
			}
		}
		mBusinessList.removeAll(removeList);
		mBusinessList.addAll(list);
	}

	protected void syncApplistData(final int containerType,
			final List<BusinessItemInfo> list) {
		mExecutorService.execute(new Runnable() {
			@Override
			public void run() {
				final ContentResolver resolver = mContext.getContentResolver();
				resolver.delete(Constants.APPLIST_BUSINESS_URI, null, null);
				mBusinessList.clear();
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
					value.put("container_id", containerType);
					value.put("gp_priority", businessItemInfo.gpPriority);
					value.put("gp_url", businessItemInfo.gpUrl);
					value.put("rating", businessItemInfo.rating);
					value.put("download_count", businessItemInfo.appDownloadCount);
					value.put("desc", businessItemInfo.desc);
					values[i] = value;
					mBusinessList.add(businessItemInfo);
				}
				notifyBusinessChange();
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
									value, "icon_url=" + "\"" + info.iconUrl
											+ "\"", null);
							info.icon = BitmapUtils.bitmapToDrawable(response);
							info.iconLoaded = true;
							notifyBusinessChange();
						}
					}, new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							LeoLog.e("loadAppIcon", "false");
						}
					});
		}
	}
}
