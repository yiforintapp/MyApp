package com.leo.appmaster.airsig.airsigutils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.airsig.airsigengmulti.ASEngine;

public class EventLogger {
	
	/**
	 * Interface definition for event logging.
	 * Please implement this interface to support event logging.
	 */
	public interface EventLoggerDelegte {
		
		/**
		 * Implement this function to decide where to send the event log
		 * 
		 * @param eventName 
		 * 				The name of the event
		 * @param eventProperties 
		 * 				Additional information of the event. 
		 * 				You can convert it to HashMap<String, String> by the utility function {@link Utils#convertJSONToHashMap(JSONObject)} 
		 */
		public void logEvent(String eventName, JSONObject eventProperties);
		
		/**
		 * Implement this function to decide where to send the user information.
		 * If your log system doesn't have this feature, just ignore it.
		 * 
		 * @param userProperties
		 * 				The properties set to the user
		 */
		public void setUserProperties(JSONObject userProperties);
		
		/**
		 * Increase the value of user property.
		 * Implement this function to decide where to send the information.
		 * If your log system doesn't have this feature, just ignore it.
		 * 
		 * @param name 
		 * 				The name of the user property want to increase.
		 * @param amount
		 * 				The increase amount.
		 */
		public void incrementUserProperty(String name, int amount);
	}
	
	// Event names >>>
	public static final String EVENT_NAME_TRAINING = "airsig_training";
	public static final String EVENT_NAME_TUTORIAL = "airsig_tutorial";
	public static final String EVENT_NAME_VERIFY = "airsig_verify";
	public static final String EVENT_NAME_TRAINING_RESET = "airsig_training_reset";
	public static final String EVENT_NAME_TRAINING_CLICK_TUTORIAL = "airsig_training_click_tutorial";
	public static final String EVENT_NAME_TRAINING_CLICK_LEAVE = "airsig_training_click_leave";
	public static final String EVENT_NAME_TRAINING_ERROR = "airsig_training_error";
	public static final String EVENT_NAME_TRAINING_COMPLETE = "airsig_training_complete";
	public static final String EVENT_NAME_VERIFY_VERIFIED = "airsig_verify_verified";
	// Event names <<<
	
	// Event properties >>>
	public static final String EVENT_PROP_TRAINING_SUCCESS = "airsig_training_success";
	public static final String EVENT_PROP_TRAINING_SUCCESS_TIMES = "airsig_training_success_times";
	public static final String EVENT_PROP_TRAINING_SIGN_COUNT = "airsig_training_signature_count";
	public static final String EVENT_PROP_TRAINING_RESULT_STRENGTH = "airsig_training_result_strength";
	public static final String EVENT_PROP_TUTORIAL_STEP = "airsig_tutorial_step";
	public static final String EVENT_PROP_TUTORIAL_CHOOSE_WORD_PRACTICE_TIMES = "airsig_tutorial_choose_word_practice_times";
	public static final String EVENT_PROP_ERROR_CODE = "airsig_error_code";
	public static final String EVENT_PROP_ERROR_MESSAGE = "airsig_error_message";
	public static final String EVENT_PROP_VERIFY_SUCCESS = "airsig_verify_success";
	public static final String EVENT_PROP_VERIFY_TIMES = "airsig_verify_times";
	// Event properties <<<
	
	// User properties >>>
	public static final String USER_PROP_TRAINING_RESULT_STRENGTH = "airsig_training_result_strength";
	public static final String USER_PROP_TRAINING_SUCCESS_TIMES = "airsig_training_success_times";
	public static final String USER_PROP_AIRSIG_VERIFY_PASS_TIMES = "airsig_verify_airsig_pass_times";
	// User properties <<<
	
	// Keys for private event properties
	private static final String EVENT_PROP_ASENGINE_VERSION = "airsig_engine_version";
	private static final String EVENT_PROP_ACTION_DURATION = "airsig_action_duration";
	private static final String EVENT_PROP_FIRST_TIME_EVENT = "airsig_first_time_event";

	private static final String AIRSIG_SHARED_PREFERENCE_USED_EVENTS = "AIRSIG_SHARED_PREFERENCE_USED_EVENTS";
	private static final String SPREF_KEY_USED_EVENT = "***SPREF_KEY_USED_EVENT***";
	
	private static ArrayList<String> mUsedEvents;
	private static SharedPreferences mPref;
	private static EventLoggerDelegte mDelegate;
	private static final Map<String, Long> mActions = new HashMap<String, Long>();
	
	public static void initialize(Context context, EventLoggerDelegte delegate) {
		mPref = context.getSharedPreferences(AIRSIG_SHARED_PREFERENCE_USED_EVENTS, Context.MODE_PRIVATE);
		String usedEvents = mPref.getString(SPREF_KEY_USED_EVENT, "");
		if (usedEvents.length() > 0) {
			mUsedEvents = new ArrayList<String>(Arrays.asList(usedEvents.split(",")));
		} else {
			mUsedEvents = new ArrayList<String>();
		}
		
		mDelegate = delegate;
	}

	public static void logEvent(String eventName, JSONObject eventProperties) {
		if (null == eventName || eventName.length() == 0) {
			assert (false) : "Empty event name";
			return;
		}

		JSONObject properties;
		if (null != eventProperties) {
			properties = eventProperties;
		} else {
			properties = new JSONObject();
		}

		try {
			// set AirSig SDK version
			properties.put(EVENT_PROP_ASENGINE_VERSION, ASEngine.apiVersion());

			if (mUsedEvents.contains(eventName)) {
				properties.put(EVENT_PROP_FIRST_TIME_EVENT, false);
			} else {
				properties.put(EVENT_PROP_FIRST_TIME_EVENT, true);
				mUsedEvents.add(eventName);
				
				String usedEvents = mPref.getString(SPREF_KEY_USED_EVENT, "");
				if (usedEvents.length() > 0) {
					usedEvents += ",";
				}
				usedEvents += eventName;
				SharedPreferences.Editor editor = mPref.edit();
				editor.putString(SPREF_KEY_USED_EVENT, usedEvents);
				editor.commit();
			}
		} catch (JSONException exception) {
			exception.printStackTrace();
		}
		
		// send to delegate
		if (mDelegate != null) {
			mDelegate.logEvent(eventName, properties);
		}
	}

	public static void startAction(String actionName, JSONObject eventProperties, boolean sendEvent) {
		if (null == actionName || actionName.length() == 0) {
			assert (false) : "Empty action name";
			return;
		}

		// record the timestamp of the action
		mActions.put(actionName, System.currentTimeMillis());

		// log event
		if (sendEvent) {
			logEvent(actionName + "_start", eventProperties);
		}
	}

	public static void endAction(String actionName, JSONObject eventProperties) {
		if (null == actionName || actionName.length() == 0) {
			assert (false) : "Empty action name";
			return;
		}

		JSONObject properties;
		if (null != eventProperties) {
			properties = eventProperties;
		} else {
			properties = new JSONObject();
		}

		try {
			if (mActions.containsKey(actionName)) {
				properties.put(EVENT_PROP_ACTION_DURATION, (System.currentTimeMillis() - mActions.get(actionName)) / 1000f);
			}
		} catch (JSONException exception) {
		}

		// log event
		logEvent(actionName + "_end", properties);

		// remove timestamp
		mActions.remove(actionName);
	}
	
	public static void setUserProperties(JSONObject userProperties) {
		// send to delegate
		if (mDelegate != null) {
			mDelegate.setUserProperties(userProperties);
		}
	}
	
	public static void incrementUserProperty(String name, int increment) {
		// send to delegate
		if (mDelegate != null) {
			mDelegate.incrementUserProperty(name, increment);
		}
	}
	
	public static void setLocalIntProperty(String name, int value) {
		SharedPreferences.Editor editor = mPref.edit();
		editor.putInt(name, value);
		editor.commit();
	}
	
	public static void increaseLocalIntProperty(String name, int increase) {
		int value = getLocalIntProperty(name, 0);
		setLocalIntProperty(name, value + increase);
	}
	
	public static int getLocalIntProperty(String name, int defaultValue) {
		return mPref.getInt(name, defaultValue);
	}
	
	public static void setLocalLongProperty(String name, long value) {
		SharedPreferences.Editor editor = mPref.edit();
		editor.putLong(name, value);
		editor.commit();
	}
	
	public static void increaseLocalLongProperty(String name, long increase) {
		int value = getLocalIntProperty(name, 0);
		setLocalLongProperty(name, value + increase);
	}
	
	public static long getLocalLongProperty(String name, long defaultValue) {
		return mPref.getLong(name, defaultValue);
	}
}
