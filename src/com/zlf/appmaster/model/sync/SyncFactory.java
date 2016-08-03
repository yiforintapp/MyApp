package com.zlf.appmaster.model.sync;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.zlf.appmaster.db.stock.QiNiuDBHelper;
import com.zlf.appmaster.db.stock.StockDBHelper;

/**
 * 同步类工厂
 * Created by Deping Huang on 2014/11/20.
 */
public class SyncFactory {
    // 把需要同步的数据模块在此添加
    public static SyncBaseBean newBean(Context context, int command){
        switch (command){
            case SyncBaseBean.SYNC_KEY_STOCK_BASE_DATA:
                return new SyncStockBean(context);
            case SyncBaseBean.SYNC_KEY_INDUSTRY:
                return new SyncIndustryBean(context);
            case SyncBaseBean.SYNC_KEY_FAVORITES:
                return new SyncFavoritesBean(context);
            case SyncBaseBean.SYNC_KEY_MSG:
//                return new SyncMsgBean(context);
            case SyncBaseBean.SYNC_KEY_CONTACTS:
//                return new SyncContactsBean(context);
            case SyncBaseBean.SYNC_KEY_BOOKMARKS:
                return new SyncNewsFavoritesBean(context);
            case SyncBaseBean.SYNC_KEY_TOPIC:
//                return new SyncTopicBean(context);
            case SyncBaseBean.SYNC_KEY_TOPIC_FAVORITE:
                return new SyncTopicFavoriteBean(context);
            case SyncBaseBean.SYNC_KEY_USER_PROFILE:
//                return new SyncUserInfoBean(context);
            case SyncBaseBean.SYNC_KEY_FANS:
//                return new SyncFansBean(context);
            case SyncBaseBean.SYNC_KEY_MY_PROGRAMME:
//                return new SyncMyProgramme(context);
            case SyncBaseBean.SYNC_KEY_SUBSCRIBE_PROGRAMME:
//                return new SyncSubscribeProgramme(context);
        }
        return null;
    }


    /**
     * 获取版本号存取相关的数据库操作对象
     * (每种不同的同步类型，数据可能存储于不同的库)
     * @param command
     * @return
     */
    public static SQLiteOpenHelper getVersionDBHelper(Context context, int command){
        SQLiteOpenHelper sqLiteOpenHelper = null;
        switch (command){
            case SyncBaseBean.SYNC_KEY_STOCK_BASE_DATA:
            case SyncBaseBean.SYNC_KEY_INDUSTRY:
            case SyncBaseBean.SYNC_KEY_INDEX:
            case SyncBaseBean.SYNC_KEY_TOPIC:
            case SyncBaseBean.SYNC_KEY_TOPIC_FAVORITE:
                sqLiteOpenHelper = StockDBHelper.getInstance(context);
                break;
            case SyncBaseBean.SYNC_KEY_FAVORITES:
            case SyncBaseBean.SYNC_KEY_CONTACTS:
            case SyncBaseBean.SYNC_KEY_BOOKMARKS:
            case SyncBaseBean.SYNC_KEY_MSG:
            case SyncBaseBean.SYNC_KEY_USER_PROFILE:
            case SyncBaseBean.SYNC_KEY_FANS:
            case SyncBaseBean.SYNC_KEY_MY_PROGRAMME:
            case SyncBaseBean.SYNC_KEY_SUBSCRIBE_PROGRAMME:
                sqLiteOpenHelper = QiNiuDBHelper.getInstance(context);
                break;
            default:
                sqLiteOpenHelper = StockDBHelper.getInstance(context);
                break;
        }

        return  sqLiteOpenHelper;
    }

}

