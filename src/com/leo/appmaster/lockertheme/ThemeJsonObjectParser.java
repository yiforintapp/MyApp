package com.leo.appmaster.lockertheme;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.leo.appmaster.Constants;
import com.leo.appmaster.model.ThemeInfo;
import com.leo.appmaster.utils.LeoLog;

public class ThemeJsonObjectParser {

	public static List<ThemeInfo> parserJsonObject(JSONObject jsonObject) {
		ArrayList<ThemeInfo> list = null;
		if (jsonObject != null) {
			try {
				JSONArray array = jsonObject.getJSONArray("data");
				list = new ArrayList<ThemeInfo>();
				JSONObject temp = null;
				ThemeInfo bean = null;
				for (int i = 0; i < array.length(); i++) {
					try {
						temp = array.getJSONObject(i);
						bean = new ThemeInfo();
						bean.packageName = temp.getString("package_name");
						bean.downloadUrl = temp.getString("download_url");
						bean.themeName = temp.getString("theme_title");
						bean.previewUrl = temp.getString("image_url");
						bean.size = temp.getInt("size");
						bean.tag = temp.getInt("tag");
						bean.themeType = Constants.THEME_TYPE_ONLINE;
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
