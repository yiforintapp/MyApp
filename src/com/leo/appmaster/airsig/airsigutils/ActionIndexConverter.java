package com.leo.appmaster.airsig.airsigutils;

import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.airsig.airsigengmulti.ASEngine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class ActionIndexConverter {
	private final static String KEY_SHARED_PREFERENCES_NAME = "ActionIndexConverter";
	private final static String KEY_ACTION_INDEX_QUERIES = "ActionIndexQueries";
	
	private final static String KEY_ACTION_INDEX = "airsigActionIndex";
	
	private final static int DEFAULT_ACTION_INDEX = 1008;
	
	private Context mContext;
	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mSharedPreferencesEditor;
	private JSONArray mSavedQueries;
	
	public ActionIndexConverter(Context context) {
		mContext = context;
		mSharedPreferences = context.getSharedPreferences(KEY_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mSharedPreferencesEditor = mSharedPreferences.edit();
		mSavedQueries = readQueries();
	}

	@SuppressLint("Assert")
	public int getActionIndex(Map<String, String> query) {
		if (null == query || query.size() == 0) {
			return DEFAULT_ACTION_INDEX;
		}
		
		// find from saved Queries
		if (mSavedQueries.length() > 0) {
			try {
				for (int i=0; i<mSavedQueries.length(); i++) {
					JSONObject q = mSavedQueries.getJSONObject(i);
					if (isMatch(query, q) && q.has(KEY_ACTION_INDEX)) {
						return q.getInt(KEY_ACTION_INDEX);
					}
				}
			} catch(JSONException e) {
				e.printStackTrace();
			}
		}
		
		// not found, insert new query
		try {
			int actionIndex = getLargestActionIndexes() + 1;
			if (actionIndex == DEFAULT_ACTION_INDEX) {
				actionIndex++;
			}
			JSONObject newQuery = new JSONObject();
			for (String key : query.keySet()) {
				newQuery.put(key, query.get(key));
			}
			newQuery.put(KEY_ACTION_INDEX, actionIndex);
			mSavedQueries.put(newQuery);
			saveQueries(mSavedQueries);
			return actionIndex;
		} catch (JSONException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	@SuppressLint("Assert")
	public void deleteQuery(Map<String, String> query) {
		if (null == query || query.size() == 0) {
			return;
		}
		
		// find from saved Queries
		if (mSavedQueries.length() > 0) {
			try {
				JSONArray newSavedQueries = new JSONArray(); 
				for (int i=0; i<mSavedQueries.length(); i++) {
					JSONObject q = mSavedQueries.getJSONObject(i);
					if (!isMatch(query, q) && q.has(KEY_ACTION_INDEX)) {
						newSavedQueries.put(q);
					}
				}
				mSavedQueries = newSavedQueries;
				saveQueries(mSavedQueries);
			} catch(JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void deleteQuery(int actionIndex) {
		// find from saved Queries
		if (mSavedQueries.length() > 0) {
			try {
				JSONArray newSavedQueries = new JSONArray(); 
				for (int i=0; i<mSavedQueries.length(); i++) {
					JSONObject q = mSavedQueries.getJSONObject(i);
					if (q.has(KEY_ACTION_INDEX) && q.getInt(KEY_ACTION_INDEX) != actionIndex) {
						newSavedQueries.put(q);
					}
				}
				mSavedQueries = newSavedQueries;
				saveQueries(mSavedQueries);
			} catch(JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void deleteAll() {
		mSharedPreferencesEditor.remove(KEY_ACTION_INDEX_QUERIES);
		mSharedPreferencesEditor.commit();
		mSavedQueries = readQueries();
	}
	
	private JSONArray readQueries() {
		String protectedString = mSharedPreferences.getString(KEY_ACTION_INDEX_QUERIES, null);
		if (null == protectedString || protectedString.length() < 1) {
			return new JSONArray();
		}
		
		try {
			//TODO: protection mechanism for account data
			String unprotectedString = protectedString;
			
			return new JSONArray(unprotectedString);
		} catch (Exception e) {
			e.printStackTrace();
			return new JSONArray();
		}
	}
	
	private void saveQueries(JSONArray queries) {
		if (null == queries) {
			return;
		}
		
		String unprotectedString = queries.toString();
		//TODO: protection mechanism for account data
		String protectedString = unprotectedString;
		mSharedPreferencesEditor.putString(KEY_ACTION_INDEX_QUERIES, protectedString);
		mSharedPreferencesEditor.commit();
	}
	
	private boolean isMatch(Map<String, String> query, JSONObject q) {
		if (q.length() != (query.size()+1) ) {
			return false;
		}
		
		int matchedCount = 0;
		try {
			for(Iterator<String> keysIter = q.keys(); keysIter.hasNext();) {
				String key = keysIter.next();
				if (key.equals(KEY_ACTION_INDEX)) {
					matchedCount++ ;
				} else if (query.containsKey(key) && q.getString(key).equals(query.get(key))) {
					matchedCount++ ;	
				}	
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return (q.length() > 1 && matchedCount == q.length());
	}
	
	private int getLargestActionIndexes() {
		int result = 0;
		if (mSavedQueries.length() > 0) {
			try {
				for (int i=0; i<mSavedQueries.length(); i++) {
					JSONObject q = mSavedQueries.getJSONObject(i);
					if (q.has(KEY_ACTION_INDEX) && q.getInt(KEY_ACTION_INDEX) > result) {
						result = q.getInt(KEY_ACTION_INDEX);
					}
				}
			} catch(JSONException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
}
