package com.leo.appmaster.feedback;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.Utilities;

/**
 * Feedback helper to save and commit feedbacks
 */
public class FeedbackHelper {

	private final static String FEEDBACK_URL = "/appmaster/feedback";
	// private final static String FEEDBACK_URL =
	// "http://test.leostat.com/appmaster/feedback";
	public final static String TABLE_NAME = "feedback";
	protected final static String KEY_EMAIL = "email";
	protected final static String KEY_CONTENT = "content";
	protected final static String KEY_CATEGORY = "category";
	protected final static String KEY_TIME = "submit_date";

	private final static int MAX_COMMIT_COUNT = 3;

	private int mCommitIndex = -1;

	private static FeedbackHelper sInstance;

	private Runnable mCommitTask = new Runnable() {
		@Override
		public void run() {
			mCommitIndex++;
			boolean success = true;
			Cursor c = AppMasterApplication.getInstance().getContentResolver()
					.query(Constants.FEEDBACK_URI, null, null, null, null);

			if (c != null && c.moveToFirst()) {
				try {
					final int idIndex = c.getColumnIndexOrThrow(Constants.ID);
					final int contentIndex = c
							.getColumnIndexOrThrow(KEY_CONTENT);
					final int emailIndex = c.getColumnIndexOrThrow(KEY_EMAIL);
					final int categoryIndex = c
							.getColumnIndexOrThrow(KEY_CATEGORY);
					final int timeIndex = c.getColumnIndexOrThrow(KEY_TIME);
					HttpPost httpRequest = new HttpPost(Utilities.getURL(FEEDBACK_URL));
					do {
						long id = c.getLong(idIndex);
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair(KEY_CONTENT, c
								.getString(contentIndex)));
						params.add(new BasicNameValuePair(KEY_EMAIL, c
								.getString(emailIndex)));
						params.add(new BasicNameValuePair(KEY_CATEGORY, c
								.getString(categoryIndex)));
						params.add(new BasicNameValuePair(KEY_TIME, c
								.getString(timeIndex)));
						httpRequest.setEntity(new UrlEncodedFormEntity(params,
								HTTP.UTF_8));

						String deviceInfo = SDKWrapper.getEncodedDeviceInfo();
						httpRequest.addHeader("device", deviceInfo); // add
																		// device
																		// info

						HttpResponse httpResponse = new DefaultHttpClient()
								.execute(httpRequest);

						if (httpResponse.getStatusLine().getStatusCode() == 200) { // connection
																					// success
							String strResult = EntityUtils
									.toString(httpResponse.getEntity());
							JSONObject object = new JSONObject(strResult);
							int code = object.getInt("code");
							if (code == 0) { // commit success

								AppMasterApplication
										.getInstance()
										.getContentResolver()
										.delete(Constants.FEEDBACK_URI,
												Constants.ID + "=" + id, null); // delete
																				// from
																				// db
								success = true;
							} else { // commit fail
								success = false;
								break;
							}
						} else { // connection fail
							success = false;
							break;
						}
					} while (c.moveToNext());
				} catch (Exception e) {
					success = false;
				} finally {
				}
			}
			if (c != null) {
				c.close();
			}
			if (success) { // success, exit thread
				mCommitIndex = -1;
//				dbHelper.close();
			} else if (mCommitIndex < MAX_COMMIT_COUNT) { // fail, retry
			    AppMasterApplication.getInstance().postInAppThreadPool(mCommitTask);
			} else {
//				dbHelper.close();
			}
		}
	};

	public static synchronized FeedbackHelper getInstance() {
		if (sInstance == null) {
			sInstance = new FeedbackHelper();
		}
		return sInstance;
	}

	private FeedbackHelper() {

	}

	/**
	 * Commit feedbacks if any in db
	 */
	public void tryCommit() {
		if (mCommitIndex < 0) {
			mCommitIndex = 0;
			AppMasterApplication.getInstance().postInAppThreadPool(mCommitTask);
		}
	}

	/**
	 * Commit feedbacks with user input
	 * 
	 * @param category
	 * @param email
	 * @param content
	 */
	public void tryCommit(final String category, final String email,
			final String content) {
		mCommitIndex = MAX_COMMIT_COUNT;
		AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {
			@Override
			public void run() {
				boolean success = false;
				try {
					ContentValues values = new ContentValues();
					values.put(KEY_CONTENT, content);
					values.put(KEY_EMAIL, email);
					values.put(KEY_CATEGORY, category);
					values.put(KEY_TIME, System.currentTimeMillis() + "");

					Uri uri = AppMasterApplication.getInstance()
							.getContentResolver()
							.insert(Constants.FEEDBACK_URI, values);
					long id = ContentUris.parseId(uri);
					if (id > -1) {
						mCommitIndex = -1;
						success = true;
					}
				} catch (Exception e) {
				} finally {
					if (success) {
						tryCommit();
					}
				}
			}
		});
	}

}
