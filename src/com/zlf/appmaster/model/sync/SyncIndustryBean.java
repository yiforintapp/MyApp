package com.zlf.appmaster.model.sync;

import android.content.Context;

import com.zlf.appmaster.db.stock.IndustryTable;
import com.zlf.appmaster.model.industry.IndustryItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Huang on 2015/3/31.
 */
public class SyncIndustryBean extends SyncBaseBean {

    public SyncIndustryBean(Context context) {
        super(context, SyncBaseBean.SYNC_KEY_INDUSTRY);
    }

    @Override
    protected boolean resolveJSONData(JSONObject syncData) {
        IndustryTable industryTable = new IndustryTable(mContext);
        List<IndustryItem> items = new ArrayList<IndustryItem>();
        List<String> delItems = new ArrayList<String>();
        try{
            JSONArray industryArray = syncData.getJSONArray(String.valueOf(mCommand));     // 处理本类的Command
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
                if (delFlag == SyncResponse.DEL_FlAG_DELETE){
                    delItems.add(item.getID());
                }
                else if(delFlag == SyncResponse.DEL_FlAG_NEW) {
                    items.add(item);
                }

            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        if (items.size() != 0){
            industryTable.saveItems(items);
        }

        if (delItems.size() != 0) {
            industryTable.deleteItems(delItems);
        }


        return super.resolveJSONData(syncData);
    }

//    @Override
//    protected long getLocalVersion() {
//        return 0;
//    }
}
