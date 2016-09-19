package com.zlf.appmaster.hometab;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.model.HomeBannerInfo;

import org.json.JSONArray;
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
}
