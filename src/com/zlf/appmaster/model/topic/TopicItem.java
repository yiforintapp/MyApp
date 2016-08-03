package com.zlf.appmaster.model.topic;

import com.zlf.appmaster.model.industry.IndustryItem;
import com.zlf.appmaster.model.sync.SyncResponse;
import com.zlf.appmaster.model.sync.SyncStockBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 股票题材
 * Created by Huang on 2015/5/25.
 */
public class TopicItem extends IndustryItem {
    private static final String TAG = TopicItem.class.getSimpleName();


    public static List<IndustryItem> resolveAllItems(JSONObject response){
        List<IndustryItem> items = new ArrayList<IndustryItem>();
        try{
            JSONArray industryArray = response.getJSONArray(String.valueOf(SyncStockBean.SYNC_KEY_TOPIC));
            int len = industryArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject jsonObject = industryArray.getJSONObject(i);
                IndustryItem item = new IndustryItem();
                item.setID(jsonObject.getString("Id"));
                item.setName(jsonObject.getString("Name"));
                item.setColor(jsonObject.getInt("Color"));
                item.setCreateTime(jsonObject.getLong("Ctime"));
                item.setUpdateTime(jsonObject.getLong("Utime"));
                int delFlag = jsonObject.getInt("DelFlag");
                if(delFlag == SyncResponse.DEL_FlAG_NEW) {
                    items.add(item);
                }

            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        return items;
    }
}
