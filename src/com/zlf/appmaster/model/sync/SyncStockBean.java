package com.zlf.appmaster.model.sync;

import android.content.Context;
import android.util.Log;

import com.zlf.appmaster.db.stock.StockTable;
import com.zlf.appmaster.model.stock.StockItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 同步股票信息请求
 * @author Huang
 *
 */
public class SyncStockBean extends SyncBaseBean {

    private final String TAG = "SyncStockBean";

	public SyncStockBean(Context context) {
		super(context, SYNC_KEY_STOCK_BASE_DATA);
		// TODO Auto-generated constructor stub
        // 加载预置的本地数据，（内置文件的产生：将版本号设置为0，然后copy resolveJSONData 中的syncData到 assert目录下的json_all_items文件）
        StockTable.loadPreInitData(mContext);
	}


    @Override
    protected boolean resolveJSONData(JSONObject syncData) {

        StockTable stockTable = new StockTable(mContext);
        List<StockItem> items = new ArrayList<StockItem>();
        List<StockItem> delItems = new ArrayList<StockItem>();

        try{
            JSONArray stockArray = syncData.getJSONArray(String.valueOf(mCommand));     // 处理本类的Command
            int len = stockArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject perItemObject = stockArray.getJSONObject(i);
                int delFlag = perItemObject.optInt("6");  // DelFlag

                if (delFlag == SyncResponse.DEL_FlAG_DELETE){   // 删除
                    delItems.add(optData(perItemObject));
                }
                else if (delFlag == SyncResponse.DEL_FlAG_NEW){ // 新增
                    items.add(optData(perItemObject));
                }


            }
        }catch (JSONException e){

        }

        if (delItems.size() != 0) {
            stockTable.deleteStockItems(delItems);
        }

        // 新增
        int addSize = items.size();
        if (addSize != 0){
            Log.i(TAG, "SyncStockBean start... size："+ addSize);
            // 刷新至数据库
            stockTable.saveStockItemArray(items);

            Log.i(TAG, "SyncStockBean end...");
        }

        return super.resolveJSONData(syncData);
    }

    public static StockItem optData(JSONObject data) {

        StockItem item = new StockItem();
        item.setCode(data.optString("1"));                  // StockId
        item.setName(data.optString("3"));                  // Name
        item.setJianPin(data.optString("4"));               // Spelling
        item.setIndustryId(data.optString("5"));            // IndustryId
        item.setTopicIDs(data.optString("7"));              // topics
        return item;
    }


//    @Override
//	protected long getLocalVersion() {
//		// TODO Auto-generated method stub
//		//return 1433908800562L;
//        return  0L;
//	}




}
