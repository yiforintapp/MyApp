package com.leo.appmaster.download;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.SystemClock;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PhoneInfoStateManager;
import com.leo.appmaster.utils.StorageUtil;
import com.leo.appmaster.utils.Utilities;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class DownloadFileTask implements Runnable {
	private static final String TAG = "DownloadFileTask";

	public String mUrl = "";
	public String mDest = "";

	private String mTemp = "";
	private File mTempFile = null;

	public String mMimeType = "";
	public String mTitle = "";
	public String mDescription = "";

	public long old_file_size = 0;
	public long mTotalSize = 0;

	private DefaultHttpClient httpclient;
	private volatile boolean mCancel = false;

	private BufferedOutputStream file = null;
	public long id;
	public boolean bWifiOnly = false;
	public boolean bNeedNotify = false;
	public int notifyType = 0;

	private Context mContext;
	private long last_progress = 0;

	public WeakReference<Callback> callback = null;

	// public boolean bneedToast = false;
	public boolean bSilent = false;

	public DownloadFileTask(Context context, String url, String dst,
			String type, String title, String description, long oldSize,
			long id, Callback cb) {
		mUrl = url;
		mDest = dst;
		mMimeType = type;
		mTitle = title;
		mDescription = description;
		old_file_size = oldSize;
		this.id = id;

		mContext = context.getApplicationContext();
		mTemp = StorageUtil.getDownloadTempDir() + File.separator
				+ Utilities.md5(mUrl);
		mTempFile = new File(mTemp);

		if (cb != null) {
			callback = new WeakReference<Callback>(cb);
		}
		// LeoLog.i(TAG, "task " + id + ", this - " + this);
	}

	private void setup() {
		HttpParams httpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(httpParams, "utf-8");
		HttpProtocolParams.setUseExpectContinue(httpParams, false);
		HttpConnectionParams.setConnectionTimeout(httpParams, 6000);
		HttpConnectionParams.setSoTimeout(httpParams, 10000);
		httpclient = new DefaultHttpClient(httpParams);
		httpclient
				.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(
						3, false));
	}

	private void start() {

		if (!mCancel) {
			onStart();

			if (!StorageUtil.IsSdCardMounted()) {
				LeoLog.e(TAG, "sd card not mounted");
				onFinish(Constants.RESULT_FAILED_SDCARD);

			} else if (bWifiOnly
					&& !PhoneInfoStateManager.isWifiConnection(mContext)) {
				onFinish(Constants.RESULT_FAILED_NO_NETWORK);

			} else if (PhoneInfoStateManager.isNetworkConnectivity(mContext)) {

				long pos = 0;

				if (mTempFile.exists()) {

					if (old_file_size <= 0) {
						StorageUtil.deleteDir(mTempFile);
					} else {
						pos = mTempFile.length();
					}
				}

				if (mTempFile.exists() && old_file_size == mTempFile.length()) {
					// file download success
//					LeoLog.i(TAG, "old_file_size == " + mTempFile.length());

					onFinish(Constants.RESULT_SUCCESS);
				} else {
					httpGet(pos);
				}
			} else {
				onFinish(Constants.RESULT_FAILED_NO_NETWORK);
			}
		} else {
			onFinish(Constants.RESULT_CANCELLED);
		}
	}

	private void httpGet(long startpos) {
		setup();

		HttpUriRequest req;

		req = new HttpGet(mUrl);

		if (startpos > 0) {
			req.setHeader("RANGE", "bytes=" + startpos + "-");
		}

		HttpEntity resEntity = null;

		try {
			HttpResponse response = httpclient.execute(req);

			if (!mCancel) {

				resEntity = response.getEntity();

				long total = resEntity.getContentLength();

				mTotalSize = total + startpos;

				//
				File oldFile = new File(mDest);

				if (oldFile.exists() && oldFile.length() == mTotalSize) {
					try {
						req.abort();
					} catch (Exception e) {
						LeoLog.e(
								TAG,
								"len = total, req.abort() "
										+ e.getLocalizedMessage());
					}

					onFinish(Constants.RESULT_SUCCESS);
				} else {
					if (oldFile.exists()) {
						oldFile.delete();
					}

					if (startpos <= 0
							|| (old_file_size > 0 && startpos > 0 && mTotalSize == old_file_size)) {
						if (!StorageUtil.isExternalSpaceInsufficient(total)) {
							LeoLog.e(TAG, "sd card insufficient");
							try {
								req.abort();
							} catch (Exception e) {
								LeoLog.e(
										TAG,
										"2 len = total, req.abort() "
												+ e.getLocalizedMessage());
							}
							onFinish(Constants.RESULT_FAILED_SDCARD_INSUFFICIENT);
							return;
						}

						if (startpos <= 0) {
							file = new BufferedOutputStream(
									new FileOutputStream(mTempFile));
						} else {
							file = new BufferedOutputStream(
									new FileOutputStream(mTempFile, true));
						}

						onProgress(mTotalSize, startpos);

						int statusCode = response.getStatusLine()
								.getStatusCode();
						if (HttpStatus.SC_OK == statusCode
								|| HttpStatus.SC_PARTIAL_CONTENT == statusCode) {

							InputStream is = resEntity.getContent();

							byte[] buf = new byte[1024 * 8];
							int ch = -1;
							long len = startpos;

							while ((ch = is.read(buf)) != -1 && !mCancel) {
								file.write(buf, 0, ch);

								len += ch;

								onProgress(mTotalSize, len);

								if (mCancel)
								{
								    
								}
//									LeoLog.i(TAG, "aCancel = " + mCancel);
							}

							file.flush();
							file.close();

							if (mCancel) {
								onFinish(Constants.RESULT_CANCELLED);

							} else if (mTempFile.length() != mTotalSize) {
								LeoLog.e(TAG, "download error, length != total");
								onFinish(Constants.RESULT_FAILED);

							} else {

								onFinish(Constants.RESULT_SUCCESS);
							}
						}
					} else {
						try {
							req.abort();
						} catch (Exception e) {
							LeoLog.e(TAG,
									"req.abort " + e.getLocalizedMessage());
						}
						httpGet(0);
					}
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			LeoLog.e(TAG, "1 " + e.getLocalizedMessage());
			onFinish(Constants.RESULT_FAILED);
		} catch (IOException e) {
			e.printStackTrace();
			LeoLog.e(TAG, "2 " + e.getLocalizedMessage());
			onFinish(Constants.RESULT_FAILED);
		} catch (Exception e) {
			e.printStackTrace();
			LeoLog.e(TAG, "3 " + e.getLocalizedMessage());
			onFinish(Constants.RESULT_FAILED);
		} finally {
			req.abort();
		}
	}

	public void stop() {
		mCancel = true;
		// LeoLog.e(TAG, "stop " + id + ", " + mCancel + ", " + this);
	}

	@Override
	public void run() {
		start();
	}

	//
	public void onStart() {
		Intent intent = new Intent(Constants.ACTION_DOWNLOAD_START);
		intent.putExtra(Constants.EXTRA_ID, id);

		mContext.sendBroadcast(intent);

//		LeoLog.i(TAG, "onStart" + id);
	}

	public void onProgress(long total, long current) {

		long _now = SystemClock.elapsedRealtime();
		if (last_progress == 0
				|| _now - last_progress > Constants.PROGRESS_INTERVAL) {

			last_progress = _now;

			Intent intent = new Intent(Constants.ACTION_DOWNLOAD_PROGRESS);
			intent.putExtra(Constants.EXTRA_ID, id);
			intent.putExtra(Constants.EXTRA_TOTAL, total);
			intent.putExtra(Constants.EXTRA_CURRENT, current);
			intent.putExtra(Constants.EXTRA_URL, mUrl);
			intent.putExtra(Constants.EXTRA_MIMETYPE, mMimeType);

			mContext.sendBroadcast(intent);

			ContentValues values = new ContentValues();

			values.put(Constants.COLUMN_DOWNLOAD_CURRENT_SIZE, current);
			mContext.getContentResolver().update(Constants.DOWNLOAD_URI, values,
					Constants.ID + "=" + id, null);

			LeoLog.e(TAG, "onProgress:  current = " + current + "   total = "
					+ total);
		}
	}

	public void onFinish(int result) {
		// private static final int RESULT_SUCCESS = 0;
		// private static final int RESULT_FAILED = 1;
		// private static final int RESULT_CANCELLED = 2;

		if (result == Constants.RESULT_SUCCESS) {
			File dst = new File(mDest);
			if (dst.exists() && dst.length() == mTotalSize) {
				LeoLog.d(TAG, "dst file already exists");
			} else {
				StorageUtil.deleteDir(dst);
				dst.getParentFile().mkdirs();
				boolean ret = mTempFile.renameTo(dst);

				if (!ret) {
					LeoLog.e(TAG, "rename error");
					result = Constants.RESULT_FAILED;
				}
			}
		}

		if (callback != null) {
			Callback cb = callback.get();
			if (cb != null) {
				cb.onFinish(id);
			}
		}

		Intent intent = new Intent(Constants.ACTION_DOWNLOAD_COMPOLETED);
		intent.putExtra(Constants.EXTRA_ID, id);
		intent.putExtra(Constants.EXTRA_RESULT, result);
		intent.putExtra(Constants.EXTRA_DEST_PATH, mDest);

		LeoLog.i(TAG, "need nof " + bNeedNotify);

		if (bNeedNotify) {
			intent.putExtra(Constants.EXTRA_NOTIFY_TYPE, notifyType);
		}

		mContext.sendBroadcast(intent);

		ContentValues values = new ContentValues();

		values.put(Constants.COLUMN_DOWNLOAD_TOTAL_SIZE, mTotalSize);
		values.put(Constants.COLUMN_DOWNLOAD_STATUS, result);

		mContext.getContentResolver().update(Constants.DOWNLOAD_URI, values,
				Constants.ID + "=" + id, null);

		// LeoLog.e(TAG, "onFinish " + result);
		LeoLog.i(TAG, "onFinish " + result + ", " + mDest);
		LeoLog.i(TAG, "onFinish " + mUrl);

//		if (!bSilent) {
//			Message msg = new Message();
//			msg.what = Constants.MESSAGE_DOWNLOAD_FAILED;
//
//			if (result == Constants.RESULT_FAILED_SDCARD_INSUFFICIENT) {
//				msg.obj = mContext
//						.getString(R.string.toast_download_fail_storage);
//				((AppMasterApplication) mContext.getApplicationContext()).mHandler
//						.sendMessage(msg);
//			} else if (result == Constants.RESULT_FAILED_SDCARD) {
//				msg.obj = mContext
//						.getString(R.string.download_sdcard_status_error);
//				((AppMasterApplication) mContext.getApplicationContext()).mHandler
//						.sendMessage(msg);
//			} else if (result == Constants.RESULT_FAILED) {
//				if (PhoneInfoStateManager.isNetworkConnectivity(mContext)) {
//					msg.obj = mContext
//							.getString(R.string.download_failed_toast);
//					((AppMasterApplication) mContext.getApplicationContext()).mHandler
//							.sendMessage(msg);
//				}
//			} else if (result == Constants.RESULT_FAILED_NO_NETWORK) {
//				msg.obj = mContext
//						.getString(R.string.download_network_invalid_toast);
//				((AppMasterApplication) mContext.getApplicationContext()).mHandler
//						.sendMessage(msg);
//			}
//		}
	}

	public interface Callback {
		public void onFinish(long id);
	}
}
