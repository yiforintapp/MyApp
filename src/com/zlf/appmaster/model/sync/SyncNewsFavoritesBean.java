package com.zlf.appmaster.model.sync;

import android.content.Context;

import com.zlf.appmaster.db.stock.NewsFavoriteTable;
import com.zlf.appmaster.utils.QLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by yushian on 15-4-8.
 */
public class SyncNewsFavoritesBean extends SyncBaseBean {
    private static final String TAG = "SyncNewsFavoritesBean";

    public SyncNewsFavoritesBean(Context context) {
        super(context, SYNC_KEY_BOOKMARKS);
    }

    //


    @Override
    protected void accumulateOperator(JSONObject operatorJSONObject, HashMap<Integer, Object> operatorIDs) {

        //添加收藏
        if (operatorIDs.containsKey(SyncOperator.ID_NORMAL_FAVORITE_ADD)){
            //从数据库获取
            NewsFavoriteTable table = new NewsFavoriteTable(mContext);
            JSONArray array = table.getCommitJsonArray(false);

            if (array != null && array.length() > 0){
                try{
                    operatorJSONObject.accumulate(String.valueOf(SyncOperator.ID_NORMAL_FAVORITE_ADD), array);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
            QLog.i(TAG,"ID_NORMAL_FAVORITE_ADD array:"+array);
        }

        //删除收藏
        if (operatorIDs.containsKey(SyncOperator.ID_NORMAL_FAVORITE_DEL)){
            //从数据库获取
            NewsFavoriteTable table = new NewsFavoriteTable(mContext);
            JSONArray array = table.getCommitJsonArray(true);
            if (array != null && array.length() > 0){
                QLog.i(TAG,"ID_NORMAL_FAVORITE_DEL array:"+array);
                try{
                    operatorJSONObject.accumulate(String.valueOf(SyncOperator.ID_NORMAL_FAVORITE_DEL), array);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }

    }


    @Override
    protected boolean resolveJSONData(JSONObject syncData) {
//        QLog.i(TAG,"syncData:"+syncData);
        NewsFavoriteTable table = new NewsFavoriteTable(mContext);
        table.syncData(syncData.optJSONArray(String.valueOf(SyncBaseBean.SYNC_KEY_BOOKMARKS)));
        return super.resolveJSONData(syncData);
    }

/*    //test
    protected long getLocalVersion(){
        return 0;
    }*/
}
