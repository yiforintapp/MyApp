package com.leo.appmaster.lockertheme;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.leo.appmaster.model.AppLockerThemeBean;
import com.leo.appmaster.utils.LeoLog;

public class ThemeJsonObjectParser {

	public static List<AppLockerThemeBean> parserJsonObject(JSONObject jsonObject) {
		ArrayList<AppLockerThemeBean> list = null;
		if (jsonObject != null) {
			try {
				JSONArray array = jsonObject.getJSONArray("data");
				list = new ArrayList<AppLockerThemeBean>();
				JSONObject temp = null;
				AppLockerThemeBean bean = null;
				for (int i = 0; i < array.length(); i++) {
					try {
						temp = array.getJSONObject(i);
						bean = new AppLockerThemeBean();
						bean.downloadUrl = temp.getString("download_url");
						bean.themeName = temp.getString("theme_name");
						bean.previewUrl = temp.getString("preview_url");
						bean.size = temp.getInt("size");
						bean.tag = temp.getInt("tag");
						list.add(bean);
					} catch (JSONException e) {
						LeoLog.e("parserJsonObject", e.getMessage());
					}
				}
			} catch (JSONException e) {
				LeoLog.e("parserJsonObject", e.getMessage());
				e.printStackTrace();
			}

		}

		return list;
	}
}
