package com.zlf.appmaster.model.sync;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.zlf.appmaster.db.stock.StockFavoriteTable;
import com.zlf.appmaster.model.stock.StockFavoriteItem;
import com.zlf.appmaster.model.stock.StockItem;
import com.zlf.appmaster.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * 同步收藏相关的信息， 目前只处理了股票和指数（按兼容以前的方式来处理，待完善）
 * Created by Peter Huang on 2015/11/23.
 */
public class SyncFavoritesBean extends SyncBaseBean {
    /**
     * 0x01：	新闻收藏
     * 0x02：	自选股收藏
     * 0x04：	指数收藏
     * 0x08：	主题收藏
     */
    public final static int TYPE_NEWS = 0x01;
    public final static int TYPE_STOCK = 0x02;
    public final static int TYPE_INDEX = 0x04;
    public final static int TYPE_TOPIC = 0x08;

    private static final String TAG = SyncFavoritesBean.class.getSimpleName();

    public SyncFavoritesBean(Context context) {
        super(context, SYNC_KEY_FAVORITES);
    }


    @Override
    protected boolean resolveJSONData(JSONObject syncData) {
        StockFavoriteTable stockFavoriteTable = new StockFavoriteTable(mContext);
        List<StockFavoriteItem> items = new ArrayList<StockFavoriteItem>();
        List<StockFavoriteItem> delItems = new ArrayList<StockFavoriteItem>();
        try{
            JSONArray stockArray = syncData.getJSONArray(String.valueOf(mCommand));     // 处理本类的Command
            int len = stockArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject jsonObject = stockArray.getJSONObject(i);
                String id = jsonObject.getString("id");
                int type = jsonObject.getInt("type");

                if (type == TYPE_STOCK || type == TYPE_INDEX) {

                    // 转换为内部类型
                    if (type == TYPE_INDEX){
                        type = StockItem.CODE_TYPE_INDEX;
                    }
                    else {
                        type = StockItem.CODE_TYPE_STOCK;
                    }


                    StockFavoriteItem item = new StockFavoriteItem(id, type);
                    item.setAddTime(jsonObject.getLong("ctime"));
                    item.setUpdateTime(jsonObject.getLong("utime"));

                    int delFlag = jsonObject.getInt("del_flg");
                    if (delFlag == SyncResponse.DEL_FlAG_DELETE){
                        delItems.add(item);
                    }
                    else if(delFlag == SyncResponse.DEL_FlAG_NEW) {
                        items.add(item);
                    }
                }

            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        if (items.size() != 0){
            stockFavoriteTable.addArrayByRemote(items);
        }

        if (delItems.size() != 0) {
            stockFavoriteTable.deleteByRemote(delItems);
        }


        return super.resolveJSONData(syncData);
    }

    @Override
    protected void accumulateOperator(JSONObject operatorJSONObject, HashMap<Integer, Object> operatorIDs) {
        /**
         * 服务器只保存已登录用户的自选股
         */
        String uin = Utils.getAccountUin(mContext);
        if (!TextUtils.isEmpty(uin)) {
            final StockFavoriteTable stockFavoriteTable = new StockFavoriteTable(mContext);

            // 添加、删除自选股操作
            if (operatorIDs.containsKey(SyncOperator.ID_FAVORITES)){
                try {
                    Cursor c = stockFavoriteTable.getCommitOperatorCursor();
                    JSONArray jsonArray = new JSONArray();
                    if (null != c){

                        while(c.moveToNext()){
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("id", c.getString(0));

                            // 注意收藏类型的转换
                            int code_type = c.getInt(1);
                            int type = TYPE_STOCK;
                            if (code_type == StockItem.CODE_TYPE_INDEX){
                                type = TYPE_INDEX;
                            }
                            jsonObject.put("type", type);
                            jsonObject.put("del_flg", c.getInt(2));
                            //jsonObject.put("sort", c.getInt(2));
                            jsonArray.put(jsonObject);
                        }

                        operatorJSONObject.accumulate(String.valueOf(SyncOperator.ID_FAVORITES), jsonArray);
                    }
                }
                catch (JSONException e){

                }

            }

        }
    }
}
