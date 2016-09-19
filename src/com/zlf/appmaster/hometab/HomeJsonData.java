package com.zlf.appmaster.hometab;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.model.DayNewsItem;
import com.zlf.appmaster.model.HomeBannerInfo;
import com.zlf.appmaster.model.WinTopItem;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.appmaster.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/19.
 */
public class HomeJsonData {

    public static HomeJsonData mHomeJsonData;
    private String mJsonString;

    private HomeJsonData() {

    }

    public static synchronized HomeJsonData getInstance() {
        if (mHomeJsonData == null) {
            mHomeJsonData = new HomeJsonData();
        }
        return  mHomeJsonData;
    }

    public void setData(String jsonString) {
        mJsonString = jsonString;
    }

    public List<HomeBannerInfo> getHomeBannerData() {
        List<HomeBannerInfo> list = new ArrayList<HomeBannerInfo>();
        try {
            JSONObject object = new JSONObject(mJsonString);
            if (!object.isNull(Constants.HOME_PAGE_DATA_BANNER)) {
                JSONArray jsonArray = object.getJSONArray(Constants.HOME_PAGE_DATA_BANNER);
                HomeBannerInfo info;
                for (int i = 0; i < jsonArray.length(); i++) {
                    info = new HomeBannerInfo();
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    if (!jsonObject.isNull(Constants.HOME_PAGE_DATA_BANNER_IVURL)) {
                        info.mIvUrl = jsonObject.getString(Constants.HOME_PAGE_DATA_BANNER_IVURL);
                    }
                    if (!jsonObject.isNull(Constants.HOME_PAGE_DATA_BANNER_ONURL)) {
                        info.mOpenUrl = jsonObject.getString(Constants.HOME_PAGE_DATA_BANNER_ONURL);
                    }
                    list.add(info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<WinTopItem> getHomeWinTop(){
        List<WinTopItem> list = new ArrayList<WinTopItem>();
        try {
            JSONObject object = new JSONObject(mJsonString);
            if (!object.isNull(Constants.HOME_PAGE_DATA_WINTOP)) {
                String wintopString = object.get(Constants.HOME_PAGE_DATA_WINTOP).toString();

                String[] groups = wintopString.split(";");
                for (int i = 0; i < groups.length; i++) {
                    String group = groups[i];
                    if (!Utilities.isEmpty(group)) {
                        WinTopItem item = new WinTopItem();
                        String name = group.split("_")[0];
                        String price = group.split("_")[1];

                        item.setWinName(name);
                        item.setWinPrice(Double.valueOf(price));
                        list.add(item);
                    }
                }

            }
            LeoLog.d("testHome","getHomeWinTop size is : " + list.size());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<DayNewsItem> getDayNews(){
        List<DayNewsItem> list = new ArrayList<DayNewsItem>();

        try {
            JSONObject object = new JSONObject(mJsonString);
            if (!object.isNull(Constants.HOME_PAGE_DATA_DAYNEWS)) {

                JSONArray jsonArray = object.getJSONArray(Constants.HOME_PAGE_DATA_DAYNEWS);
                DayNewsItem info;
                for (int i = 0; i < jsonArray.length(); i++) {
                    info = new DayNewsItem();
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    if (!jsonObject.isNull(Constants.HOME_PAGE_DATA_DAYNEWS_ID)) {
                        info.setId(jsonObject.getInt(Constants.HOME_PAGE_DATA_DAYNEWS_ID));
                    }
                    if (!jsonObject.isNull(Constants.HOME_PAGE_DATA_DAYNEWS_TIME)) {
                        info.setTime(jsonObject.getString(Constants.HOME_PAGE_DATA_DAYNEWS_TIME));
                    }
                    if (!jsonObject.isNull(Constants.HOME_PAGE_DATA_DAYNEWS_TITLE)) {
                        info.setTitle(jsonObject.getString(Constants.HOME_PAGE_DATA_DAYNEWS_TITLE));
                    }
                    if (!jsonObject.isNull(Constants.HOME_PAGE_DATA_DAYNEWS_DESC)) {
                        info.setDesc(jsonObject.getString(Constants.HOME_PAGE_DATA_DAYNEWS_DESC));
                    }
                    list.add(info);
                }

            }
            LeoLog.d("testHome","getDayNews size is : " + list.size());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }
}
