package com.zlf.appmaster.utils;

import android.content.Context;

import com.zlf.appmaster.db.stock.IndustryTable;
import com.zlf.appmaster.db.stock.NewsRecentTable;
import com.zlf.appmaster.db.stock.StockTable;
import com.zlf.appmaster.db.stock.TopicTable;


/**
 * 预置数据工具
 * Created by Huang on 2015/5/18.
 */
public class PreDataTool {
    private static final String TAG = PreDataTool.class.getSimpleName();
    private static byte[] mLock = new byte[0];
    private static boolean mCondition = false;

    // 加载预置数据
    public static void loadInitData(final Context context){
        Thread threadSync = new Thread(){
            @Override
            public void run() {
                synchronized(mLock){
                    QLog.i(TAG,"预置数据开始..");
                    mCondition = false;
                    StockTable.loadPreInitData(context);       // 股票数据
                    IndustryTable.loadPreInitData(context);    // 行业数据
                    TopicTable.loadPreInitData(context);       // 题材数据
                    NewsRecentTable.loadPreInitData(context);   // 新闻订阅数据
                    mCondition = true;
                    QLog.i(TAG,"预置数据通知完成..");
                    mLock.notifyAll();
                }
            }
        };
        threadSync.start();
    }

    public static void waitInit(){
        try {
            synchronized(mLock) {
                if (!mCondition){
                    QLog.i(TAG,"预置数据中..");
                    mLock.wait(3000);       // 预置3s超时
                    QLog.i(TAG,"预置数据结束..");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
