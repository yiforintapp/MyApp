
package com.leo.appmaster.appmanage.business;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.model.BusinessItemInfo;
import com.leo.appmaster.utils.LeoLog;

public class BusinessJsonParser {
    public static List<BusinessItemInfo> parserJsonObject(Context ctx,
            JSONObject jsonObject, int type) {
        ArrayList<BusinessItemInfo> list = null;

        if (jsonObject != null) {
            try {
                int code = jsonObject.getInt("code");
                if (code == 0) {
                    if (jsonObject.isNull("data")) {
                        LeoLog.e("parserJsonObject", "data is null");
                        return list;
                    }

                    JSONArray array = jsonObject.getJSONArray("data");
                    list = new ArrayList<BusinessItemInfo>();
                    JSONObject temp = null;
                    BusinessItemInfo bean = null;
                    for (int i = 0; i < array.length(); i++) {
                        try {
                            temp = array.getJSONObject(i);
                            bean = new BusinessItemInfo();
                            bean.iconUrl = temp.getString("app_icon");
                            bean.label = temp.getString("app_name");
                            bean.packageName = temp.getString("package_name");
                            bean.type = BaseInfo.ITEM_TYPE_BUSINESS_APP;
                            bean.appSize = Long.parseLong(temp
                                    .getString("size"));
                            bean.gpPriority = Integer.parseInt(temp
                                    .getString("gp_priority"));
                            bean.containType = type;
                            bean.gpUrl = temp.getString("gp_url");
                            bean.appDownloadUrl = temp
                                    .getString("download_url");
                            bean.desc = temp.getString("description");
                            bean.appDownloadCount = temp.getString("download_number");
                            bean.rating = Float.parseFloat(temp.getString("review_score"));
                            list.add(bean);
                        } catch (JSONException e) {
                            LeoLog.e("parserJsonObject", e.getMessage());
                        }
                    }
                    LeoLog.d("BusinessJsonParser", "list = " + list);
                } else if (code == 1) {
                    list = null;
                }
            } catch (Exception e) {
                LeoLog.e("parserJsonObject", e.getMessage());
                e.printStackTrace();
            }
        }
        return list;
    }

    public static List<BusinessItemInfo> parserGestureData(Context ctx,
            JSONObject jsonObject) {
        ArrayList<BusinessItemInfo> list = null;

        if (jsonObject != null) {
            try {
                int code = jsonObject.getInt("code");
                if (code == 0) {
                    if (jsonObject.isNull("data")) {
                        LeoLog.e("parserJsonObject", "data is null");
                        return list;
                    }

                    JSONArray array = jsonObject.getJSONArray("data");
                    list = new ArrayList<BusinessItemInfo>();
                    JSONObject temp = null;
                    BusinessItemInfo bean = null;
                    for (int i = 0; i < array.length(); i++) {
                        try {
                            temp = array.getJSONObject(i);
                            bean = new BusinessItemInfo();
                            bean.iconUrl = temp.getString("a");
                            bean.label = temp.getString("b");
                            bean.packageName = temp.getString("d");
                            bean.type = BaseInfo.ITEM_TYPE_BUSINESS_APP;
                            bean.appSize = 0;
                            bean.gpPriority = Integer.parseInt(temp
                                    .getString("e"));
                            bean.containType = BusinessItemInfo.CONTAIN_QUICK_GESTURE;
                            bean.gpUrl = temp.getString("f");
                            bean.appDownloadUrl = temp
                                    .getString("c");
                            bean.desc = "";
                            bean.appDownloadCount = "";
                            bean.rating = 0;
                            bean.iconLoaded = false;
                            list.add(bean);
                        } catch (JSONException e) {
                            LeoLog.e("parserJsonObject", e.getMessage());
                        }
                    }
                    LeoLog.d("BusinessJsonParser", "list = " + list);
                } else if (code == 1) {
                    list = null;
                }
            } catch (Exception e) {
                LeoLog.e("parserJsonObject", e.getMessage());
                e.printStackTrace();
            }
        }
        return list;
    }

}
