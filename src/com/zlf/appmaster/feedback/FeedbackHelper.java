package com.zlf.appmaster.feedback;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.zlfandroid.zlfvolley.Response.ErrorListener;
import com.zlfandroid.zlfvolley.Response.Listener;
import com.zlfandroid.zlfvolley.VolleyError;
import com.zlf.appmaster.AppMasterApplication;
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.HttpRequestAgent;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.utils.LeoLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Feedback helper to save and commit feedbacks
 */
public class FeedbackHelper {
    private static final String TAG = "FeedbackHelper";

	public final static String FEEDBACK_URL = "/appmaster/feedback";
	// feedback需要加密接口
	public final static String FEEDBACK_CNCRYPT_URL = "/appmaster/feedbackimage";
	// private final static String FEEDBACK_URL =
	// "http://test.leostat.com/appmaster/feedback";
	protected final static String TABLE_NAME = "feedback";
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
			Cursor c = AppMasterApplication.getInstance().getContentResolver()
					.query(Constants.FEEDBACK_URI, null, null, null, null);

			if (c != null && c.moveToFirst()) {
				try {
					final int idIndex = c.getColumnIndexOrThrow(Constants.ID);
					final int contentIndex = c .getColumnIndexOrThrow(KEY_CONTENT);
					final int emailIndex = c.getColumnIndexOrThrow(KEY_EMAIL);
					final int categoryIndex = c .getColumnIndexOrThrow(KEY_CATEGORY);
					final int timeIndex = c.getColumnIndexOrThrow(KEY_TIME);
					
					// 最多只能报三个，防止数据库数据太多，导致流量崩溃
					// TODO 后台支持批量接口，一次上报多条数据
					int count = 0;
					do {
						long id = c.getLong(idIndex);
						String deviceInfo = "jlhjl";

						Map<String, String> params = new HashMap<String, String>();
						params.put(KEY_CONTENT, c.getString(contentIndex));
						params.put(KEY_EMAIL, c.getString(emailIndex));
						params.put(KEY_CATEGORY, c.getString(categoryIndex));
						params.put(KEY_TIME, c.getString(timeIndex));
						
						Context ctx = AppMasterApplication.getInstance();
						FeedbackListener listener = new FeedbackListener(id);
						// 失败重试逻辑移动到commitFeedback里实现
						HttpRequestAgent.getInstance(ctx).commitFeedback(listener, listener, params, deviceInfo);
					} while (c.moveToNext() && count < 3);
				} catch (Exception e) {
				} finally {
				    if (c != null) {
				        c.close();
				    }
				}
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
			ThreadManager.executeOnAsyncThread(mCommitTask);
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
		ThreadManager.executeOnAsyncThread(new Runnable() {
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
	
	private static class FeedbackListener implements Listener<JSONObject>, ErrorListener {
	    private long id;
	    public FeedbackListener(long id) {
	        this.id = id;
	    }

        @Override
        public void onErrorResponse(VolleyError error) {
            LeoLog.d(TAG, "Feedback error, e" + error.getMessage());
            LeoLog.d("kkl", "上传用户反馈失败！！！");
        }

        @Override
        public void onResponse(JSONObject response, boolean noMidify) {
            LeoLog.d(TAG, "Feedback response: " + response == null ? null : response.toString());
            if (response == null) return;
            
            try {
                int code = response.getInt("code");
                if (code == 0) {
                    // succ, delete from db
                    ContentResolver resolver = AppMasterApplication.getInstance().getContentResolver();
                    resolver.delete(Constants.FEEDBACK_URI, Constants.ID + "=" + id, null); 
                }
                LeoLog.d("kkl", "上传用户反馈成功～～～");
            } catch (JSONException e) {
                LeoLog.e(TAG, "parse feedback ex.", e);
            } 
        }
	    
	}

}
