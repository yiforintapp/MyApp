package com.zlf.appmaster.model.stock;

import com.zlf.appmaster.model.sync.SyncResponse;
import com.zlf.appmaster.model.sync.SyncStockBean;
import com.zlf.appmaster.utils.PingYinUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StockItem implements Comparable {
    public static final int CODE_TYPE_STOCK = 0;
    public static final int CODE_TYPE_INDEX = 1;

	private String mCode;
	private String mName;
	private String mJianPin;	    // 拼音首字母
    private String mIndustryId;     // 行业ID
    private int mCodeType = CODE_TYPE_STOCK;              // 股票或指数

    private String mTopicIDs;   // 所属题材

	public String getCode() {
		return mCode;
	}
	public void setCode(String mCode) {
		this.mCode = mCode;
	}
	public String getName() {
		return mName;
	}
	public void setName(String mName) {
		this.mName = mName;
	}
	public String getJianPin() {
		return mJianPin;
	}
	public void setJianPin(String mJianPin) {
		this.mJianPin = mJianPin;
	}
	public static List<StockItem> resolveJsonAllStockItems(JSONObject response) throws JSONException {
        List<StockItem> items = new ArrayList<StockItem>();

        try{
            JSONArray stockArray = response.getJSONArray(String.valueOf(SyncStockBean.SYNC_KEY_STOCK_BASE_DATA));
            int len = stockArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject perItemObject = stockArray.getJSONObject(i);
                int delFlag = perItemObject.optInt("6");  // DelFlag

                if (delFlag == SyncResponse.DEL_FlAG_NEW){ // 新增
                    items.add(SyncStockBean.optData(perItemObject));
                }


            }
        }catch (JSONException e){

        }

        return items;
	}

    /**
     * 获取一组维度比较股票列表
     * @param response
     * @return
     */
    public static ArrayList<StockItem> resloveDimensionStockList(JSONObject response, String removeStockCode){
        ArrayList<StockItem> list = new ArrayList<StockItem>();
        try{
            JSONObject dataObject = response.getJSONObject("data");
            JSONArray stockArray = dataObject.optJSONArray("stockIds");

            for (int i=0;i<stockArray.length();i++){
                JSONObject itemObject = stockArray.getJSONObject(i);
                if (itemObject.optString("stockId").equals(removeStockCode)){
                    continue;
                }

                StockItem info = new StockItem();
                info.setName(itemObject.optString("stockName"));
                info.setCode(itemObject.optString("stockId"));
                info.setJianPin(PingYinUtil.getPingYin(info.getName()).toUpperCase(Locale.getDefault()));
                list.add(info);
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        //按拼音排序
        if (list != null){
            Collections.sort(list);
        }

        return list;
    }

    @Override
    public int compareTo(Object o) {
        StockItem item = (StockItem )o;
        return getJianPin().compareTo(item.getJianPin());
    }

    public String getIndustryId() {
        return mIndustryId;
    }

    public void setIndustryId(String industryId) {
        this.mIndustryId = industryId;
    }


    public int getCodeType() {
        return mCodeType;
    }

    public void setCodeType(int codeType) {
        this.mCodeType = codeType;
    }


    public String getTopicIDs() {
        return mTopicIDs;
    }

    public void setTopicIDs(String topicIDs) {
        this.mTopicIDs = topicIDs;
    }
}
