package com.zlf.appmaster.model.industry;

import com.zlf.appmaster.model.stock.StockFavoriteItem;
import com.zlf.appmaster.model.sync.SyncResponse;
import com.zlf.appmaster.model.sync.SyncStockBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 行业
 * Created by Huang on 2015/4/1.
 */
public class IndustryItem implements Serializable,Comparable<IndustryItem> {
    private String mID ="";
    private String mName = "";
    private int mColor;
    private long mCreateTime;
    private long mUpdateTime;

    private List<StockFavoriteItem> mSubStockItems;

    public void copy(IndustryItem item){
        if (null != item) {
            mID = item.mID;
            mName = item.mName;
            mColor = item.mColor;
            mCreateTime = item.mCreateTime;
            mUpdateTime = item.mUpdateTime;
            if (null != mSubStockItems && null != item.mSubStockItems){
                mSubStockItems.clear();
                mSubStockItems.addAll(mSubStockItems);
            }
            else {
                mSubStockItems = item.mSubStockItems;
            }
        }
    }

    public String getID() {
        return mID;
    }

    public void setID(String industryId) {
        this.mID = industryId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        this.mColor = 0xff000000|color;
    }

    public long getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(long createTime) {
        this.mCreateTime = createTime;
    }

    public long getUpdateTime() {
        return mUpdateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.mUpdateTime = updateTime;
    }

    public int getSubStockCount() {
        if (null != mSubStockItems) {
            return mSubStockItems.size();
        }

        return 0;
    }


    /**
     * 填充list时用
     * @return
     */
    public int getItemCount() {
        int len = 1;
        if (null != mSubStockItems){
            len += mSubStockItems.size();
        }

        return len;
    }

    public Object getItem(int position) {
        if (position == 0) { // 位置0相当于根结点
            return this;
        } else {
            if (mSubStockItems != null)
                return mSubStockItems.get(position - 1);
            else
                return null;
        }
    }

    public static List<IndustryItem> resolveAllItems(JSONObject response){
        List<IndustryItem> items = new ArrayList<IndustryItem>();
        try{
            JSONArray industryArray = response.getJSONArray(String.valueOf(SyncStockBean.SYNC_KEY_INDUSTRY));
            int len = industryArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject jsonObject = industryArray.getJSONObject(i);
                IndustryItem item = new IndustryItem();
                item.setID(jsonObject.optString("Id"));
                item.setName(jsonObject.optString("Name"));
                item.setColor(jsonObject.optInt("Color"));
                item.setCreateTime(jsonObject.optLong("Ctime"));
                item.setUpdateTime(jsonObject.optLong("Utime"));
                int delFlag = jsonObject.optInt("DelFlag");
                if(delFlag == SyncResponse.DEL_FlAG_NEW) {
                    items.add(item);
                }

            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        return items;
    }

    public List<StockFavoriteItem> getSubStockIDs() {
        return mSubStockItems;
    }

    public void setSubStockIDs(List<StockFavoriteItem> subStockIDs) {
        this.mSubStockItems = subStockIDs;
    }

    @Override
    public int compareTo(IndustryItem topicItem) {
        int compareThis = 0;
        int compareOther = 0;
        if (this.mSubStockItems != null) {
            compareThis = this.mSubStockItems.size();
        }

        if (topicItem.mSubStockItems != null) {
            compareOther = topicItem.mSubStockItems.size();
        }
        return compareOther - compareThis;
    }
}
