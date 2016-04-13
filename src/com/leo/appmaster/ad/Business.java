package com.leo.appmaster.ad;

import android.content.Context;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.sdk.SDKWrapper;

import java.util.Map;

/**
 * Created by lilibin on 16-3-23.
 */
public class Business {

	/**
	 * add an event that we will push to skyfill server
	 * and use extra channel
	 * @param extra detail of this extra data
	 */
	public static void addEvent(Map<String, String> extra) {

		Context context = AppMasterApplication.getInstance();
		final String exName = "max_ad";
		final String id = "deviceInfo";
		final String description = "baseInfo";

		SDKWrapper.addEvent(context,exName, SDKWrapper.P1, id, description, AppMasterPreference.AD_SDK_SOURCE_USE_MAX, extra);
	}
}
